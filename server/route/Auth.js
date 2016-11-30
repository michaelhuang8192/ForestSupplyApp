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
	.then((auth) => {
		if(auth == null) {
			res.json({success: true, authFailed: true});
		} else {
			res.cookie("serverAuth", auth.serverAuth);
			res.json({
				success: true,
				userId: auth.user._id,
				userName: auth.user.name
			});
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
		var ret = {
			success: true,
			authFailed: doc == null,
			userId: 0,
			userName: null
		};
		if(doc != null) {
			ret.userId = doc._id;
			ret.userName = doc.name;
		}
		res.json(ret);

	}).catch((err) => {
		next({error: err});
	});
}
