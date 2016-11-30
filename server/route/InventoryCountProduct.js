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


var gRouter = gExpress.Router();
module.exports = {path: "/InventoryCountProduct", route: gRouter};

gRouter.get('/getDocs', getDocs);
gRouter.get('/getDoc', getDoc);
gRouter.get('/search', search);
gRouter.get('/getPage', getPage);
gRouter.get('/getDocByUPC', getDocByUPC);
gRouter.get('/getDocByNum', getDocByNum);


function getDocs(req, res, next) {
	var inventoryCountId = parseInt(req.query.inventoryCountId);
	gDoc.getDocs(req, res, next, 'InventoryCountProduct',
		{inventoryCountId: inventoryCountId},
		{keywords: 0}
	);
}

function getDoc(req, res, next) {
	gDoc.getDoc(req, res, next, 'InventoryCountProduct', {_id: parseInt(req.query._id)}, {keywords: 0});
}

function getDocByUPC(req, res, next) {
	var inventoryCountId = parseInt(req.query.inventoryCountId);
	var upc = parseInt(req.query.UPC);
	gDoc.getDoc(req, res, next, 'InventoryCountProduct',
		{inventoryCountId: inventoryCountId, upc: upc},
		{keywords: 0}
	);
}

function getDocByNum(req, res, next) {
	gDoc.getDoc(req, res, next, 'InventoryCountProduct', 
		{
			inventoryCountId: parseInt(req.query.inventoryCountId),
			productNum: req.query.productNum
		},
		{keywords: 0}
	);
}

function getPage(req, res, next) {
	var inventoryCountId = parseInt(req.query.inventoryCountId);
	gDoc.getPage(req, res, next, "InventoryCountProduct",
		{inventoryCountId: inventoryCountId},
		{keywords: 0}
	);
}

function search(req, res, next) {
	var opts = {
		query: {inventoryCountId: parseInt(req.query.inventoryCountId)}
	};
	return gDoc.getSearchResult(req, res, next, "InventoryCountProduct", opts);
}
