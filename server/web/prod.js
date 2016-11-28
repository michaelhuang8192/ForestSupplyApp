var mPath = require("path");

var appPath = mPath.dirname(__filename);
var configPath = mPath.resolve(appPath, "webpack.config.js");

var inputFilename = process.argv[2];
var outputFilename = process.argv[3];
process.argv.splice(2, 2);

process.argv.push.apply(process.argv, ["--config", configPath]);
process.argv.push.apply(process.argv, ["-p"]);
process.argv.push.apply(process.argv, ["--entry", inputFilename]);
process.argv.push.apply(process.argv, ["--output-file", outputFilename]);

require("webpack/bin/webpack");
