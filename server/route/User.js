var gExpress = require('express');
var gCookieParser = require('cookie-parser');
var gBodyParser = require('body-parser');
var gDoc = require('../lib/Doc');
var gUtils = require('../lib/Utils');
var gError= require('../lib/Error');
var gDataModel = require('../lib/DataModel');
var gMulter  = require('multer')();
var gCsvParse = require("csv-parse");
var gCrypto = require('crypto');

var gJsonParser = gBodyParser.json();
var gUrlencodedParser = gBodyParser.urlencoded({extended: false});

var gRouter = gExpress.Router();
module.exports = {path: "/User", route: gRouter};

gRouter.get('/getDocs', getDocs);
gRouter.get('/getDoc', getDoc);
gRouter.get('/getPage', getPage);
gRouter.post('/newDoc', adminOnly, gJsonParser, newDoc);
gRouter.post('/updateDoc', adminOnly, gJsonParser, updateDoc);
gRouter.post('/deleteDoc', adminOnly, gJsonParser, gUrlencodedParser, deleteDoc);


function getDocs(req, res, next) {
	gDoc.getDocs(req, res, next, 'User', null, {password: 0, secret: 0});
}

function getDoc(req, res, next) {
	gDoc.getDoc(req, res, next, 'User', {_id: parseInt(req.query._id)}, {password: 0, secret: 0});
}

function getPage(req, res, next) {
	gDoc.getPage(req, res, next, "User", null, {password: 0, secret: 0});
}

function deleteDoc(req, res, next) {
	var _id = parseInt(req.body._id);
	if(_id <= 1) {
		next({error: new gError.UserError("Invalid Id")});
		return;
	}
	gDoc.deleteDoc(req, res, next, "User", {_id: _id});
}


function sha256(str) {
	return gCrypto.createHash('sha256').update(str).digest('base64');
}

function adminOnly(req, res, next) {
	if(res.locals.user._id == 1) {
		next();
	} else {
		next({error: new gError.UserError(String(err))});
	}
}

function newDoc(req, res, next) {
	var db = req.app.locals.db;
	var user = req.body || {};

	return new Promise((resolve, reject) => {
		if(!user.name && !user.password)
			reject(new gError.UserError("Empty Name And Password"));
		else {
			var pendingId = gDoc.getNewID(db, "User");
			gCrypto.randomBytes(256, (err, buf) => {
				if(err)
					reject(new gError.UserError(String(err)));
				else {
					user.secret = buf.toString('base64');
					resolve(pendingId);
				}
			});
		}

	}).then((_id) => {
		user._id = _id;
		return db.collection("User").insert({
			_id: user._id,
			name: user.name,
			password: sha256(user.password),
			secret: user.secret,
			createdTime: new Date().getTime()
		});
	}).then(() => {
		res.json({
			success: true,
			_id: user._id
		});

	}).catch((err) => {
		next(err);

	});
}

function updateDoc(req, res, next) {
	var db = req.app.locals.db;
	var user = req.body || {};
	var userId = parseInt(user._id) || 0;

	new Promise((resolve, reject) => {
		if(!userId || !user.name && !user.password)
			reject(new gError.UserError("Invalid Input"));
		else if(user.password) {
			gCrypto.randomBytes(256, (err, buf) => {
				if(err)
					reject(new gError.UserError(String(err)));
				else
					resolve(buf.toString('base64'));
			});
		} else 
			resolve(null);

	}).then((secret) => {
		var updDoc = {};
		if(user.name) updDoc.name = user.name;
		if(user.password) {
			updDoc.password = sha256(user.password);
			updDoc.secret = secret;
		}
		return db.collection("User").updateOne(
			{_id: userId},
			{$set: updDoc}
		);

	}).then((result) => {
		res.json({
			success: result.matchedCount > 0,
			_id: userId
		});

	}).catch((err) => {
		next(err);

	});
}

