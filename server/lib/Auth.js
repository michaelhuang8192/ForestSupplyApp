var gCrypto = require('crypto');


function Auth(app) {
	if(!(this instanceof Auth)) return new Auth(app);

	this.app = app;
	this.boundRouteCallback = this.routeCallback.bind(this);
}
module.exports = Auth;

Auth.prototype.getBoundRouteCallback = function() {
	return this.boundRouteCallback;
}

function sha256(str) {
	return gCrypto.createHash('sha256').update(str).digest('base64');
}

Auth.prototype.routeCallback = function(req, res, next) {
	var db = this.app.locals.db;
	var serverAuth = String(req.cookies.serverAuth).split(":");
	var userId = parseInt(serverAuth[0]);
	var userHash = serverAuth[1];

	this.getUserByAuth(userId, userHash)
	.then((doc) => {
		if(doc == null) {
			if(req.cookies.serverAuth) res.clearCookie("serverAuth");
			res.json({success: true, authFailed: true});
		} else {
			res.locals.user = doc;
			next();
		}

	}).catch((err) => {
		next({error: err});
	});
}

Auth.prototype.getUserByAuth = function(userId, userHash) {

	var db = this.app.locals.db;
	return db.collection('User').findOne({_id: userId})
	.then((doc) => {
		if(doc == null || sha256(doc.password + doc.secret) !== userHash) {
			return null;
		} else {
			return doc;
		}
	});
}

Auth.prototype.getServerAuth = function(userName, userPassword) {
	var db = this.app.locals.db;
	return db.collection('User').findOne({name: userName})
	.then((doc) => {
		if(doc == null || sha256(userPassword) !== doc.password) {
			return null;
		} else {
			return {"serverAuth": doc._id + ":" + sha256(doc.password + doc.secret)};
		}
	});
}