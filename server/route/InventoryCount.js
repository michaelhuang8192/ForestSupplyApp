var gExpress = require('express');
var gCookieParser = require('cookie-parser');
var gBodyParser = require('body-parser');
var gDoc = require('../lib/Doc');
var gUtils = require('../lib/Utils');
var gError= require('../lib/Error');
var gDataModel = require('../lib/DataModel');
var gMulter  = require('multer')();
var gCsvParse = require("csv-parse");
var gCsvStringify = require('csv-stringify');


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
gRouter.post('/deleteDoc', adminOnly, gJsonParser, gUrlencodedParser, deleteDoc);
gRouter.post('/addCountRecord', gJsonParser, addCountRecord);
gRouter.post('/editCountRecord', gJsonParser, editCountRecord);
gRouter.post('/deleteCountRecord', gJsonParser, deleteCountRecord);
gRouter.get('/getProductCountStat', getProductCountStat);
gRouter.get('/getCountReport', getCountReport);
gRouter.get('/downloadCountReport', downloadCountReport);


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
			var teamId = parseInt(employee.teamId);
			if(teamId != 2) teamId = 1;

			employeeList.push({
				_id: parseInt(employee._id),
				name: employee.name,
				teamId: teamId,
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
		var idxUom2 = header.indexOf("Display UOM");
		var idxUom3 = header.indexOf("Price UOM");

		for(var i = 1; i < products.length; i++) {
			var prod = products[i];
			var product = {}

			var uom1 = prod[idxUom1].toLowerCase().trim();
			var uom2 = prod[idxUom2].toLowerCase().trim();
			var uom3 = prod[idxUom3].toLowerCase().trim();

			var uomSet = new Set([uom1, uom2, uom3]);
			uomSet.delete("");
			uomSet.delete(uom1);
			var uomList = [{name: uom1, factor: 1}];
			for(var uom of uomSet) uomList.push({name: uom, factor: 1});


			product.productNum = prod[idxProdNum];
			product.description = prod[idxProdDesc];
			product.manufacturerNo = prod[idxManufacturerNo];
			product.upc = parseInt(prod[idxUpc]) || 0;
			product.onHand = parseFloat(prod[idxOnHand]) || 0.0;
			product.uomList = uomList;

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
	var productNum = req.query.productNum;
	var teamId = parseInt(req.query.teamId);

	gDoc.getDocs(req, res, next, "CountRecord",
		{inventoryCountId: inventoryCountId, productNum: productNum, teamId: teamId}
	);
}


function validateCountRecord(user, db, newDoc) {
	return new Promise((resolve, reject) => {
		if(typeof newDoc != 'object')
			resolve(gError.UserErrorPromise("invalid input"));
		else
			resolve(gDataModel.filterCountRecord(newDoc));

	}).then((doc) => {
		newDoc = doc;
		return db.collection("InventoryCount").findOne({_id: newDoc.inventoryCountId});

	}).then((_invDoc) => {
		if(_invDoc == null) return gError.UserErrorPromise("Inventory Count Session Not Found");

		var found = false;
		newDoc.userId = user._id;
		newDoc.userName = user.name;
		for(var employee of _invDoc.employeeList) {
			if(employee._id != newDoc.userId) continue;
			newDoc.teamId = employee.teamId;
			found = true;
			break;
		}
		if(!found) return gError.UserErrorPromise("User Not In List");

		return newDoc;
	});
}

function addCountRecord(req, res, next) {
	var db = req.app.locals.db;
	var newDoc = req.body;

	return validateCountRecord(res.locals.user, db, newDoc)
	.then((doc) => {
		newDoc = doc;
		return gDoc.getNewID(db, "CountRecord");

	}).then((_id) => {
		newDoc._id = _id;
		newDoc.createdTime = new Date().getTime();
		return db.collection("CountRecord").insertOne(newDoc)
		.then((r) => {
			res.json({success: true, _id: r.insertedCount > 0 ? _id : null});
		});
	}).catch(function(err) {
		next({error: err});

	});
}

function editCountRecord(req, res, next) {
	var db = req.app.locals.db;
	var newDoc = req.body;

	return validateCountRecord(res.locals.user, db, newDoc)
	.then((newDoc) => {
		var _id = newDoc._id;
		delete newDoc._id;
		newDoc.createdTime = new Date().getTime();
		return db.collection("CountRecord")
		.updateOne(
			{_id: _id, inventoryCountId: newDoc.inventoryCountId, teamId: newDoc.teamId}
			, {$set: newDoc}
		)
		.then((r) => {
			res.json({success: true, _id: r.matchedCount > 0 ? _id : null});
		});
	}).catch(function(err) {
		next({error: err});

	});
}

function deleteCountRecord(req, res, next) {
	var db = req.app.locals.db;
	var newDoc = req.body;

	return validateCountRecord(res.locals.user, db, newDoc)
	.then((newDoc) => {
		var _id = newDoc._id;
		return db.collection("CountRecord")
		.deleteOne(
			{_id: _id, inventoryCountId: newDoc.inventoryCountId, teamId: newDoc.teamId}
		)
		.then((r) => {
			res.json({success: true, _id: r.deletedCount > 0 ? _id : null});
		});
	}).catch(function(err) {
		next({error: err});

	});
}


function getProductCountStat(req, res, next) {
	var db = req.app.locals.db;
	return db.collection("CountRecord").aggregate([
		{
			$match: {
				productNum: String(req.query.productNum),
				inventoryCountId: parseInt(req.query.inventoryCountId),
				teamId: parseInt(req.query.teamId)
			}
		},
		{
			
			$group : {
				_id: "$uom",
				quantity: {$sum: "$quantity"}
			}
		}
	])
	.toArray()
	.then((docs) => {
		res.json({success: true, docs: docs});

	}).catch(function(err) {
		next({error: err});

	});
}


function _getCountReport(db, inventoryCountId, compactMode) {
	return db.collection("CountRecord").aggregate([
		{
			$match: {
				inventoryCountId: inventoryCountId,
			}
		},
		{
			$group : {
				_id: {productNum: "$productNum", teamId: "$teamId", uom: "$uom"},
				quantity: {$sum: "$quantity"},
				createdTime: {$max: "$createdTime"}
			}
		},
		{
			$group : {
				_id: {productNum: "$_id.productNum", teamId: "$_id.teamId"},
				records: {$push: {quantity: "$quantity", uom: "$_id.uom"}},
				createdTime: {$max: "$createdTime"}
			}
		},
		{
			$group : {
				_id: {productNum: "$_id.productNum"},
				teamRecords: {$push: {teamId: "$_id.teamId", records: "$records"}},
				createdTime: {$max: "$createdTime"}
			}
		},
		{ $sort : {createdTime : -1} }
	])
	.toArray()
	.then((docs) => {
		var retDocs = [];
		for(var doc of docs) {
			var teamRecords = [{}, {}];
			for(var teamRecord of doc.teamRecords) {
				for(var record of teamRecord.records) {
					if(teamRecord.teamId != 1 && teamRecord.teamId != 2) continue;
					teamRecords[teamRecord.teamId - 1][record.uom] = record.quantity;
				}
			}

			retDocs.push({
				num: doc._id.productNum,
				records: teamRecords
			});
		}

		if(compactMode) {
			for(var doc of retDocs) {
				var team1 = [];
				var team1_ = [];
				for(var k in doc.records[0]) {
					team1.push(`${doc.records[0][k]}${k}`);
					if(doc.records[0][k]) team1_.push(`${doc.records[0][k]}${k}`);
				}
				team1.sort();
				team1_.sort();
				doc.team1 = team1.join(', ');
				team1_ = team1_.join(', ');

				var team2 = [];
				var team2_ = [];
				for(var k in doc.records[1]) {
					team2.push(`${doc.records[1][k]}${k}`);
					if(doc.records[1][k]) team2_.push(`${doc.records[1][k]}${k}`);
				}
				team2.sort();
				team2_.sort();
				doc.team2 = team2.join(', ');
				team2_ = team2_.join(', ');

				doc.matched = team1_ == team2_;
			}
			delete doc.records;
		}

		return retDocs;
	});
}

function getCountReport(req, res, next) {
	var db = req.app.locals.db;
	var inventoryCountId = parseInt(req.query.inventoryCountId);

	return _getCountReport(db, inventoryCountId, 1)
	.then((retDocs) => {
		res.json({success: true, docs: retDocs});

	}).catch(function(err) {
		next({error: err});

	});
}

function downloadCountReport(req, res, next) {
	var db = req.app.locals.db;
	var inventoryCountId = parseInt(req.query.inventoryCountId);

	return _getCountReport(db, inventoryCountId, 1)
	.then((retDocs) => {
		return new Promise((resolve, reject) => {
			gCsvStringify(
				retDocs,
				{header: true, columns: ['num', 'matched', 'team1', 'team2']},
				(err, output) => {
					if(err) return reject(err);
					return resolve(output);
				}
			);
		});
		
	}).then((csvBuffer) => {
		res.set({
			'Content-Disposition': 'attachment; filename=report.csv',
			'Content-Type': 'application/csv; charset=utf-8'
		});

		res.end(csvBuffer);

	}).catch(function(err) {
		next({error: err});

	});
}

