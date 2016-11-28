var gCfg = require('../config');
var gWinston = require('winston');

var transports = [
];
if(gCfg.logFilename) {
	transports.push(new (gWinston.transports.File)({ filename: gCfg.logFilename }));
} else {
	transports.push(new gWinston.transports.Console());
}

var logger = module.exports = new gWinston.Logger({ transports: transports });
