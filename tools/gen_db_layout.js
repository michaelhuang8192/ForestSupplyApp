

var schemas = {
	Product: {
		_id: {index: "primary", type: 'long'},
		inventoryCountId: {type: 'long'},
		productNum: {type: 'String'},
		description: {type: 'String'},
		manufacturerNo: {type: 'String'},
		onHand: {type: 'double'},
		upc: {type: 'long'},
		uomList: {type: 'List<UOM>', isJson: true},
	},

	UOM: {
		name: {type: 'String'},
		factor: {type: 'String'},
	},

	User: {
		_id: {index: "primary", type: 'long'},
		name: {type: 'String'},
	},

	CountRecord: {
		_id: {index: "primary", type: 'long'},
		inventoryCountId: {type: 'long'},
		teamId: {type: 'int'},
		userId: {type: 'long'},
		userName: {type: 'String'},
		productNum: {type: 'String'},
		uom: {type: 'String'},
		quantity: {type: 'int'},
		createdTime: {type: 'long'},
	},

	InventoryCount: {
		_id: {index: "primary", type: 'long'},
		name: {type: 'String'},
		employeeList: {type: 'List<InventoryCountEmployee>', isJson: true},
		productCount: {type: 'int'},
		createdTime: {type: 'long'}
	},

	InventoryCountEmployee: {
		_id: {index: "primary", type: 'long'},
		name: {type: 'String'},
		teamId: {type: 'int'}
	}

};

var tableJoins = [
	//["DineTable", "Ticket"]
];

var noSyncTables = {
	//TicketFood: true,
};


function capitalize(s) {
	return s
	.replace(/(?:^|[^a-z0-9]+)([a-z0-9])/gi, (a, p1) => { return p1.toUpperCase() });
}

function _capitalize(s) {
	s = s.replace(/[^0-9a-z]/gi, '');
	return s.substr(0, 1).toUpperCase() + s.substr(1);
}

var gFs = require('fs');

var dstDir = "../app/src/main/java/com/tinyappsdev/forestsupply/data";
var gTmpl = require("./tiny_template")(null, null, {
	capitalize: capitalize,
	console: console
});

for(var name in schemas) {
	var res = gTmpl.render("Schema.java", {cols: schemas[name], name: name});
	gFs.writeFileSync(dstDir + "/" + capitalize(name) + ".java", res);
}

//var res = gTmpl.render("DatabaseOpenHelper.java", {schemas: schemas, version: 30});
//gFs.writeFileSync(dstDir + "/DatabaseOpenHelper.java", res);

//var res = gTmpl.render("provider_base.java", {});
//gFs.writeFileSync(dstDir + "/ProviderBase.java", res);
/*
for(var name in schemas) {
	var res = gTmpl.render("Provider.java", {tableName: name});
	gFs.writeFileSync(dstDir + "/" + capitalize(name) + "Provider.java", res);
}
*/

//var res = gTmpl.render("ContentProviderEx.java", {schemas: schemas, tableJoins: tableJoins});
//gFs.writeFileSync(dstDir + "/ContentProviderEx.java", res);

var res = gTmpl.render("ModelHelper.java", {schemas: schemas, noSyncTables: noSyncTables});
gFs.writeFileSync(dstDir + "/ModelHelper.java", res);


//var res = gTmpl.render("SyncHelper.java", {schemas: schemas, noSyncTables: noSyncTables});
//gFs.writeFileSync(dstDir + "/../service/SyncHelper.java", res);


var res = gTmpl.render("DataModel.js", {schemas: schemas});
gFs.writeFileSync("../server/lib/DataModel.js", res);

