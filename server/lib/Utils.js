
var TermRegex = /[^a-z0-9]+/gim;
exports.parseTerms = function(terms) {
	terms = terms.toLowerCase().replace(TermRegex, ' ').trim();
	if(!terms.length) return null;
	return Array.from(new Set(terms.split(' ')));
}

var hasKey = exports.hasKey = (obj, key) => {
	return Object.hasOwnProperty.call(obj, key);
}

var getObjectDiff = exports.getObjectDiff = (newObj, oldObj, ignoredKeys) => {
	var diffObj = {};
	for(var key in newObj) {
		if(hasKey(ignoredKeys, key)) continue;
		var newVal = newObj[key];
		if(newVal === oldObj[key]) continue;
		diffObj[key] = newObj[key];
	}

	return diffObj;
}

/*
	round(0.02499999999, 2, 0) => 0.02
	round(0.02499999999, 2) => 0.03
	round(0.02499999999, 2, 1e-5) => 0.03

var round = exports.round = (value, precision, tolerance) => {
	if(tolerance == null) tolerance = 1 / Math.pow(10, precision + 5);
	if(value < 0) tolerance -= tolerance;

	return Math.round((value + tolerance) * Math.pow(10, precision)) / 100;
}
*/
var round = exports.round = (value, precision) => {
	return Number(value.toFixed(precision));
}