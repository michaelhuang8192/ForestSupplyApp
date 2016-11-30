var gExpress = require('express');

var gRouter = exports.DefaultHandler = gExpress.Router();

gRouter.all('/', (req, res, next) => {
	res.redirect('/web/');
});

gRouter.all('/web/', (req, res, next) => {
	res.redirect('/web/');
});
