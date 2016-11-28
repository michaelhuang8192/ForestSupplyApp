


var filterProduct = exports.filterProduct = (js) => {
	if(js == null) return null;

	var newJs = {};
	newJs._id = parseInt(js._id) || 0;
	newJs.inventoryCountId = parseInt(js.inventoryCountId) || 0;
	newJs.productNum = js.productNum == null ? null : String(js.productNum);
	newJs.description = js.description == null ? null : String(js.description);
	newJs.manufacturerNo = js.manufacturerNo == null ? null : String(js.manufacturerNo);
	newJs.onHand = parseFloat(js.onHand) || 0.0;
	newJs.upc = parseInt(js.upc) || 0;
	newJs.uomList = js.uomList == null ? null : js.uomList.map(filterUOM);
	return newJs;
};

var filterUOM = exports.filterUOM = (js) => {
	if(js == null) return null;

	var newJs = {};
	newJs.name = js.name == null ? null : String(js.name);
	newJs.factor = js.factor == null ? null : String(js.factor);
	return newJs;
};

var filterUser = exports.filterUser = (js) => {
	if(js == null) return null;

	var newJs = {};
	newJs._id = parseInt(js._id) || 0;
	newJs.name = js.name == null ? null : String(js.name);
	return newJs;
};

var filterCountRecord = exports.filterCountRecord = (js) => {
	if(js == null) return null;

	var newJs = {};
	newJs._id = parseInt(js._id) || 0;
	newJs.inventoryCountId = parseInt(js.inventoryCountId) || 0;
	newJs.userId = parseInt(js.userId) || 0;
	newJs.productNum = js.productNum == null ? null : String(js.productNum);
	newJs.uom = js.uom == null ? null : String(js.uom);
	newJs.quantity = parseInt(js.quantity) || 0;
	newJs.createdTime = parseInt(js.createdTime) || 0;
	return newJs;
};

var filterInventoryCount = exports.filterInventoryCount = (js) => {
	if(js == null) return null;

	var newJs = {};
	newJs._id = parseInt(js._id) || 0;
	newJs.name = js.name == null ? null : String(js.name);
	newJs.employeeList = js.employeeList == null ? null : js.employeeList.map(filterInventoryCountEmployee);
	newJs.productCount = parseInt(js.productCount) || 0;
	newJs.createdTime = parseInt(js.createdTime) || 0;
	return newJs;
};

var filterInventoryCountEmployee = exports.filterInventoryCountEmployee = (js) => {
	if(js == null) return null;

	var newJs = {};
	newJs._id = parseInt(js._id) || 0;
	newJs.name = js.name == null ? null : String(js.name);
	newJs.teamId = parseInt(js.teamId) || 0;
	return newJs;
};

