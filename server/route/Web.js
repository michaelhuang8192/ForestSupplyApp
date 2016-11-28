var gExpress = require('express');
var gCookieParser = require('cookie-parser');
var gBodyParser = require('body-parser');
var gPath = require('path');

var gRouter = gExpress.static(gPath.join(__dirname, '../web'));
module.exports = {path: "/web", route: gRouter, authNotRequired: true, notApi: true};
