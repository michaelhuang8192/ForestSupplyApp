

function DataObserver() {
	if(!(this instanceof DataObserver)) return new DataObserver();

	this.members = [];
};

DataObserver.prototype.register = function(onChange) {
	if(this.members.indexOf(onChange) < 0)
		this.members.push(onChange);
};

DataObserver.prototype.unregister = function(onChange) {
	var idx = this.members.indexOf(onChange);
	if(idx < 0)
		this.members.splice(idx, 1);
};

DataObserver.prototype.unregisterAll = function() {
	this.members = [];
};

DataObserver.prototype.notifyChanged = function() {
	for(var onChange of this.members)
		onChange();
};

module.exports = DataObserver;

