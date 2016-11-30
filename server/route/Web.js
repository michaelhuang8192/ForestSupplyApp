var gExpress = require('express');
var gCookieParser = require('cookie-parser');
var gBodyParser = require('body-parser');
var gPath = require('path');

var gRouter = gExpress.Router();
module.exports = {path: "/web", route: gRouter, authNotRequired: true, notApi: true};

gRouter.use(gExpress.static(gPath.join(__dirname, '../web')));

gRouter.all(/\/[\w]*$/, (req, res, next) => {
	res.redirect('/web/index.html');
});
