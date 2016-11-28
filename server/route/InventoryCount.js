var gExpress = require('express');
var gCookieParser = require('cookie-parser');
var gBodyParser = require('body-parser');
var gDoc = require('../lib/Doc');
var gUtils = require('../lib/Utils');
var gError= require('../lib/Error');
var gDataModel = require('../lib/DataModel');
var gMulter  = require('multer')();
var gCsvParse = require("csv-parse");

var gJsonParser = gBodyParser.json();
var gUrlencodedParser = gBodyParser.urlencoded({extended: false});


var gRouter = gExpress.Router();
module.exports = {path: "/InventoryCount", route: gRouter};

gRouter.get('/getDocs', getDocs);
gRouter.get('/getDocsByUser', getDocsByUser);
gRouter.get('/getDoc', getDoc);
gRouter.get('/search', search);
gRouter.get('/getPage', getPage);
gRouter.get('/getCountRecords', getCountRecords);
gRouter.post('/updateDoc', adminOnly, gMulter.any(), updateDoc);
gRouter.post('/addCountRecord', gJsonParser, addCountRecord);
gRouter.post('/deleteDoc', adminOnly, gJsonParser, gUrlencodedParser, deleteDoc);


function getDocs(req, res, next) {
	gDoc.getDocs(req, res, next, 'InventoryCount');
}

function getDocsByUser(req, res, next) {
	var user = res.locals.user;
	gDoc.getDocs(req, res, next, 'InventoryCount', {"employeeList._id": user._id}, null);
}

function getDoc(req, res, next) {
	gDoc.getDoc(req, res, next, 'InventoryCount', {_id: parseInt(req.query._id)});
}

function getPage(req, res, next) {
	gDoc.getPage(req, res, next, "InventoryCount");
}

function adminOnly(req, res, next) {
	if(res.locals.user._id == 1) {
		next();
	} else {
		next({error: new gError.UserError(String(err))});
	}
}

function deleteDoc(req, res, next) {
	var _id = parseInt(req.body._id);
	var db = res.app.locals.db;
	return db.collection("InventoryCount").deleteOne({_id: _id})
	.then((r) => {
		if(r.deletedCount)
			return db.collection("InventoryCountProduct").deleteMany({inventoryCountId: _id});
		else
			return r;
	}).then((r) => {
		res.json({success: true, deleted: r.deletedCount != 0});
		
	}).catch(function(err) {
		next({error: err});

	});
}

function updateDoc(req, res, next) {
	var productList = [];
	var db = req.app.locals.db;
	var inventoryCountId;
	var isNew;
	var item = null;
	var employeeList = [];
	var file = (req.files || [])[0];

	new Promise((resolve, reject) => {
		item = JSON.parse(req.body.item) || {};
		inventoryCountId = parseInt(item._id);
		isNew = !inventoryCountId;
		for(var employee of (item.employeeList|| [])) {
			employeeList.push({
				_id: parseInt(employee._id),
				name: employee.name,
				teamId: parseInt(employee.teamId),
			});
		}

		if(file)
			gCsvParse(file.buffer.toString(), (err, output) => {
				if(err || !output)
					reject(new gError.UserError(String(err)));
				else
					resolve(output);
			});
		else
			resolve([]);

	}).then((products) => {
		if(products.length == 0) return;

		var header = products[0];
		var idxProdNum = header.indexOf("Product (15)");
		var idxProdDesc = header.indexOf("Description");
		var idxManufacturerNo = header.indexOf("Manufacturer No");
		var idxUpc = header.indexOf("UPC (14)");
		var idxOnHand = header.indexOf("OnHand");
		var idxUom1 = header.indexOf("Purchase UOM");

		for(var i = 1; i < products.length; i++) {
			var prod = products[i];
			var product = {}

			product.productNum = prod[idxProdNum];
			product.description = prod[idxProdDesc];
			product.manufacturerNo = prod[idxManufacturerNo];
			product.upc = parseInt(prod[idxUpc]) || 0;
			product.onHand = parseFloat(prod[idxOnHand]) || 0.0;
			product.uomList = [
				{name: prod[idxUom1].toLowerCase(), factor: 1},
			];

			if(!product.productNum) continue;

			product.keywords = generateKeywords(product);
			productList.push(product);
		}

	}).then(() => {
		return Promise.all([
			inventoryCountId || gDoc.getNewID(db, "InventoryCount"),
			productList.length ? gDoc.getNewID(db, "InventoryCountProduct", productList.length) : 0
		]);

	}).then((ids) => {
		inventoryCountId = ids[0];
		var inventoryCountProductIds = ids[1];
		inventoryCountProductIds -= productList.length;
		for(var prod of productList) {
			prod._id = ++inventoryCountProductIds;
			prod.inventoryCountId = inventoryCountId;
		}

		if(!isNew && file)
			return db.collection("InventoryCountProduct").deleteMany({inventoryCountId: inventoryCountId});

	}).then(() => {
		if(productList.length) return db.collection("InventoryCountProduct").insertMany(productList);

	}).then((results) => {
		if(isNew)
			return db.collection("InventoryCount").insert({
				_id: inventoryCountId,
				name: item.name,
				employeeList: employeeList,
				productCount: productList.length,
				createdTime: new Date().getTime()
			});
		else {
			var updDoc = {
				name: item.name,
				employeeList: employeeList
			}
			if(file) {
				updDoc.productCount = productList.length;
			}
			return db.collection("InventoryCount").update(
				{_id: inventoryCountId},
				{$set: updDoc}
			);
		}

	}).then((results) => {
		res.json({
			success: true,
			_id: inventoryCountId
		});

	}).catch((err) => {
		next(err);

	});
}

var SearchWeights = [1, 5, 7];
function generateKeywords(doc) {
	var indexes = [[], [], []];

	if(doc.productNum) indexes[2].push(doc.productNum);
	if(doc.manufacturerNo) indexes[2].push(doc.manufacturerNo);
	if(doc.description) indexes[1].push(doc.description);

	var keywordMap = {};
	for(var i = 0; i < indexes.length; i++) {
		var rawKeywords = gUtils.parseTerms(indexes[i].join(' '));
		if(rawKeywords == null) continue;

		for(var kw of rawKeywords) {
			if(Object.prototype.hasOwnProperty.call(keywordMap, kw))
				keywordMap[kw] += SearchWeights[i];
			else
				keywordMap[kw] = SearchWeights[i];
		}
	}
	
	var keywords = [];
	for(var kw in keywordMap)
		keywords.push({name: kw, weight: keywordMap[kw]});

	return keywords;
}


function getCountRecords(req, res, next) {
	var inventoryCountId = parseInt(req.query.inventoryCountId);
	var productNum = req.query.productNum

	gDoc.getDocs(req, res, next, "CountRecord",
		{inventoryCountId: inventoryCountId, productNum: productNum}
	);
}

function addCountRecord(req, res, next) {
	var db = req.app.locals.db;
	var newDoc = req.body;
	//console.log(newDoc);

	return new Promise((resolve, reject) => {
		if(typeof newDoc != 'object')
			resolve(gError.UserErrorPromise("invalid input"));
		else
			resolve(gDataModel.filterCountRecord(newDoc));

	}).then((doc) => {
		newDoc = doc;
		return gDoc.getNewID(db, "CountRecord");
		
	}).then((_id) => {
		newDoc._id = _id;
		newDoc.createdTime = new Date().getTime();
		newDoc.userId = res.locals.user._id;
		return db.collection("CountRecord").insertOne(newDoc)
		.then((r) => {
			res.json({success: true, _id: r.insertedCount > 0 ? _id : null});
		});
	});
}
