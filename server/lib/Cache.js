
function Cache() {
	if(!(this instanceof Cache)) return new Cache();

	this._cache = new Map();
}

exports.Cache = Cache;

Cache.prototype.clear = function() {
	this._cache.clear();
}

Cache.prototype.getValue = function(key) {

}

Cache.prototype.setValue = function(key, value) {

}

Cache.prototype.deleteValue = function(key) {

}

function wakeUpCallers(waitList, index, value) {
	for(var waiter of waitList) {
		try {
			waiter[index](value);
		} catch(e) {}
	}
}

Cache.prototype._startRequest = function(key, item) {
	if(item.activeReq == null && item.pendingReq == null) return;

	var p;
	if(item.activeReq.type == 0)
		p = this.getValue(key);
	else if(item.activeReq.type == 1)
		p = this.setValue(key, item.activeReq.value);
	else if(item.activeReq.type == 2)
		p = this.deleteValue(key);

	return p
	.then((value) => {
		item.value = value;
		item.isReady = 1;
		return [0, value];

	}, (err) => {
		item.isReady = 0;
		return [1, err];

	}).then((data) => {
		//console.log(item);
		var waitList = item.activeReq.waitList;
		item.activeReq = item.pendingReq;
		item.pendingReq = null;
		wakeUpCallers(waitList, data[0], data[1]);
		//console.log("+++", data[0], data[1]);

		this._startRequest(key, item);
	});
}

function newCacheItem() {
	return {value: undefined, isReady: 0, activeReq: null, pendingReq: null};
}

Cache.prototype.get = function(key, defaultValue) {
	var item = this._cache.get(key);
	if(item == null) {
		item = newCacheItem();
		this._cache.set(key, item);
	}

	if(item.isReady)
		return Promise.resolve(item.value === undefined ? defaultValue : item.value);
	else {
		if(item.activeReq == null) {
			item.activeReq = {
				type: 0,
				waitList: [],
			}
			this._startRequest(key, item);
		}

		return new Promise((resolve, reject) => {
			item.activeReq.waitList.push([resolve, reject]);
		}).then((value) => {
			return value === undefined ? defaultValue : value;
		});
	}
}

Cache.prototype.set = function(key, value) {
	var item = this._cache.get(key);
	if(item == null) {
		item = newCacheItem();
		this._cache.set(key, item);
	}

	var req;
	if(item.activeReq != null) {
		if(item.activeReq.type == 1 && item.activeReq.value === value) {
			req = item.activeReq;
		} else {
			req = item.pendingReq = {
				type: 1,
				waitList: item.pendingReq != null ? item.pendingReq.waitList : [],
				value: value
			};

		}
	} else {
		req = item.activeReq = {
			type: 1,
			waitList: [],
			value: value
		};
		this._startRequest(key, item);
	}

	return new Promise((resolve, reject) => {
		req.waitList.push([resolve, reject]);
	});
}


Cache.prototype.delete = function(key) {
	var item = this._cache.get(key);
	if(item == null) {
		item = newCacheItem();
		this._cache.set(key, item);
	}

	var req;
	if(item.activeReq != null) {
		if(item.activeReq.type == 2) {
			req = item.activeReq;
		} else {
			req = item.pendingReq = {
				type: 2,
				waitList: item.pendingReq != null ? item.pendingReq.waitList : [],
			};
		}
	} else {
		req = item.activeReq = {
			type: 2,
			waitList: [],
		};
		this._startRequest(key, item);
	}

	return new Promise((resolve, reject) => {
		req.waitList.push([resolve, reject]);
	});
}

