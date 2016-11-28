var gCfg = require('../config');
var gWinston = require('winston');

var transports = [
	new gWinston.transports.Console()
];
if(gCfg.logFilename) {
	transports.push(new (gWinston.transports.File)({ filename: gCfg.logFilename }));
}

var logger = module.exports = new gWinston.Logger({ transports: transports });
