var gExpress = require('express');
var gCookieParser = require('cookie-parser');
var gBodyParser = require('body-parser');
var gPath = require('path');

var gRouter = gExpress.Router();
module.exports = {path: "/web", route: gRouter, authNotRequired: true, notApi: true};

var WEB_PATH = gPath.join(__dirname, '../web');
gRouter.use(gExpress.static(WEB_PATH));

gRouter.all(/^[-/\w]*$/, (req, res, next) => {
	res.sendFile(WEB_PATH + "/index.html");
});
