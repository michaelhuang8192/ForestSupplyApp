var gExpress = require('express');
var gBodyParser = require('body-parser');
var gDoc = require('../lib/Doc');

var gJsonParser = gBodyParser.json();
var gUrlencodedParser = gBodyParser.urlencoded({extended: false});

var gRouter = gExpress.Router();
module.exports = {path: "/Auth", route: gRouter, authNotRequired: true};

gRouter.post('/getAuth', gJsonParser, gUrlencodedParser, getAuth);
gRouter.get('/checkAuth', checkAuth);


function getAuth(req, res, next) {
	req.app.locals.auth.getServerAuth(req.body.userName, req.body.userPassword)
	.then((credential) => {
		if(credential == null) {
			res.json({success: true, authFailed: true});
		} else {
			res.cookie("serverAuth", credential.serverAuth);
			res.json({success: true});
		}
	}).catch((err) => {
		next({error: err});
	});
}

function checkAuth(req, res, next) {
	var db = req.app.locals.db;
	var serverAuth = String(req.cookies.serverAuth).split(":");
	var userId = parseInt(serverAuth[0]);
	var userHash = serverAuth[1];

	req.app.locals.auth.getUserByAuth(userId, userHash)
	.then((doc) => {
		res.json({success: true, authFailed: doc == null});

	}).catch((err) => {
		next({error: err});
	});
}
