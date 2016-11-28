var mPath = require("path");

var appPath = mPath.dirname(__filename);
var configPath = mPath.resolve(appPath, "webpack.config.js");

var inputFilename = process.argv[2];
var outputFilename = process.argv[3];
process.argv.splice(2, 2);

process.argv.push.apply(process.argv, ["--config", configPath]);
process.argv.push.apply(process.argv, ["--inline"]);
process.argv.push.apply(process.argv, ["--host", "0.0.0.0"]);
process.argv.push.apply(process.argv, ["--content-base", appPath]);
var outputPath = mPath.dirname(mPath.resolve(process.cwd(), outputFilename));
process.argv.push.apply(process.argv, ["--output-path", outputPath]);
var publicPath = outputPath.substr(appPath.length).replace(/\\/g, '/');
process.argv.push.apply(process.argv, ["--output-public-path", publicPath]);

process.argv.push.apply(process.argv, ["--entry", inputFilename]);
process.argv.push.apply(process.argv, ["--output-file", mPath.basename(outputFilename)]);

require("webpack-dev-server/bin/webpack-dev-server");



