var gExpress = require('express');
var gCookieParser = require('cookie-parser');
var gBodyParser = require('body-parser');
var gDoc = require('../lib/Doc');
var gUtils = require('../lib/Utils');
var gError= require('../lib/Error');

var gRouter = gExpress.Router();
module.exports = {path: "/Sys", route: gRouter};

gRouter.get('/getSysInfo', getSysInfo);


function getSysInfo(req, res, next) {
	var db = req.app.locals.db;

	return Promise.all([
		db.collection("Ticket").count(),
		db.collection("Customer").count(),
		db.collection("Food").count()
	])
	.then((counts) => {
		res.json({
			success: true,
			counts: {
				Ticket: counts[0],
				Customer: counts[1],
				Food: counts[2]
			}
		});

	}).catch(function(err) {
		next(err);

	});
}

