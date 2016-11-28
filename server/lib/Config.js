var gDocEvent = require('./DocEvent');
var gDoc = require('./Doc');
var gCache = require('./Cache');

var Config = module.exports = function Config(app) {
	if(!(this instanceof Config)) return new Config(app);

	gCache.Cache.call(this);
	this.app = app;
}

Config.prototype = Object.create(gCache.Cache.prototype);
Config.prototype.constructor = Config;

Config.prototype.getValue = function(key) {
	return this.app.locals.db.collection('Config')
	.findOne(
		{key: key, dbDeleted: {$ne: 1}},
		{fields: {val: 1}}
	).then((doc) => {
		return doc != null ? doc.val : undefined;
	});
}

Config.prototype.setValue = function(key, val) {
	var curTs = parseInt((new Date()).getTime() / 1000);
	var db = this.app.locals.db;

	return db.collection('Config')
	.findOneAndUpdate(
		{key: key, dbDeleted: {$ne: 1}},
		{$set: {val: val, dbModifiedTime: curTs}, $inc: {dbChanged: 1}},
		{projection: {val: 1}, returnOriginal: false}
	).then((result) => {
		var doc = result.value;
		if(doc != null) {
			gDocEvent.recordDocEvent(this.app, "Config", doc._id, 0);
			return doc.val;
		} else
			return gDoc.getNewID(db, "Config")
			.then((id) => {
				return db.collection('Config')
				.findOneAndUpdate(
					{key: key, dbDeleted: {$ne: 1}},
					{
						$set: {val: val, dbModifiedTime: curTs},
						$inc: {dbChanged: 1},
						$setOnInsert: {_id: id}
					},
					{
						projection: {val: 1},
						returnOriginal: false,
						upsert: true
					}
				);
			}).then((result) => {
				var doc = result.value;
				if(doc != null) {
					gDocEvent.recordDocEvent(this.app, "Config", doc._id, 0);
					return doc.val;
				}
			});
	});
}

Config.prototype.deleteValue = function(key) {
	var curTs = parseInt((new Date()).getTime() / 1000);
	var db = this.app.locals.db;

	return db.collection('Config')
	.findOneAndUpdate(
		{key: key, dbDeleted: {$ne: 1}},
		{$set: {dbDeleted: 1, dbModifiedTime: curTs}, $inc: {dbChanged: 1}},
		{projection: {_id: 1}}
	).then((result) => {
		var doc = result.value;
		if(doc != null)
			gDocEvent.recordDocEvent(this.app, "Config", doc._id, 1);
		return undefined;
	});
}