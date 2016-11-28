
<%
var defaultValueFunc = (colName, colMeta) => {
	if(colMeta.isJson) {
		var className = /^List<([a-z]+)>$/gi.exec(colMeta.type);
		if(className) {
			if(this.schemas[className[1]])
				return `js.${colName} == null ? null : js.${colName}.map(filter${className[1]})`;
		} else if(this.schemas[colMeta.type])
			return `js.${colName} == null ? null : filter${colMeta.type}(js.${colName})`;
	}

	if(colMeta.type == 'int' || colMeta.type == 'long')
		return `parseInt(js.${colName}) || 0`;
	else if(colMeta.type == 'double')
		return `parseFloat(js.${colName}) || 0.0`;
	else if(colMeta.type == 'String')
		return `js.${colName} == null ? null : String(js.${colName})`;

	return `js.${colName}`;
}
%>

%for(var tableName in this.schemas) {
<%
	var constVals = [];
	for(var colName in this.schemas[tableName]) {
		var colMeta = this.schemas[tableName][colName];
		if(!colMeta.value) continue;
		for(var valKey in colMeta.value) {
			constVals.push([`${colName.toUpperCase()}_${valKey}`, colMeta.value[valKey]]);
		}
	}
	if(constVals.length <= 0) continue;
%>
var ${tableName} = exports.${tableName} = {
%	for(var constVal of constVals) {
	${constVal[0]}: ${constVal[1]},
%	}
};
%}

%for(var tableName in this.schemas) {
var filter${tableName} = exports.filter${tableName} = (js) => {
	if(js == null) return null;

	var newJs = {};
%	for(var colName in this.schemas[tableName]) {
%		var colMeta = this.schemas[tableName][colName];
%		if(colMeta.system) continue;
	newJs.${colName} = ${defaultValueFunc(colName, colMeta)};
%	}
	return newJs;
};

%}