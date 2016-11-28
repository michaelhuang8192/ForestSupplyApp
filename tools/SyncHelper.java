package com.tinyappsdev.tinypos.service;

//Auto-Generated, See Tools

import android.content.ContentResolver;
import android.content.ContentValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tinyappsdev.tinypos.data.ModelHelper;
import com.tinyappsdev.tinypos.rest.ApiCall;
import com.tinyappsdev.tinypos.data.ContentProviderEx;

import java.util.ArrayList;
import java.util.List;

<%
function getContentValueType(type) {
	if(type == 'byte[]')
		return 'object';
	else if(type == 'float')
		return 'double';
	else
		return type;
}

var models = {};
for(var name in this.schemas) {
	var schema = this.schemas[name];
	var model = models[name] = {};

	for(var col in schema) {
		var colMeta = schema[col];
		model[col] = {
			range: colMeta.range,
			type: colMeta.type,
			contentValueType: getContentValueType(colMeta.type)
		}
	}
}
%>

public class SyncHelper {

	public static void syncAll(ApiCall apiCall, ContentResolver contentResolver) throws JSONException {
<%
for(var name in models) {
	if(name in this.noSyncTables) continue;

	if(models[name]._id.range == 1) {
%>
		syncTable(apiCall, contentResolver, "${name}", "_id>=0");
%	} else {
		syncTable(apiCall, contentResolver, "${name}", null);
<%
	}
}
%>
	}

	public static void syncTable(ApiCall apiCall, ContentResolver contentResolver, String tableName, String delSelection) throws JSONException {
		//delete all
		contentResolver.delete(ContentProviderEx.BuildUri(tableName), delSelection, null);

		long fromId = 0;
		while(true) {
			JSONObject resObject = apiCall.callApiSync(
				String.format("%s/getSyncDocs", tableName),
				new Object[][] {
					new Object[] {"pageSize", 100},
					new Object[] {"fromId", fromId},
				}
			);

			JSONArray docs = resObject.getJSONArray("docs");
			if(docs.length() <= 0) break;

			List<ContentValues> contentValuesArray = new ArrayList<ContentValues>();
			for(int i = 0; i < docs.length(); i++) {
				JSONObject doc = docs.getJSONObject(i);
				if(doc.optInt("dbDeleted") > 0) continue;
				contentValuesArray.add(ModelHelper.GetContentValuesFromJson(tableName, doc));
			}
			contentResolver.bulkInsert(
					ContentProviderEx.BuildUri(tableName),
					contentValuesArray.toArray(new ContentValues[contentValuesArray.size()])
			);
			
			if(docs.length() != 100) break;
			fromId = contentValuesArray.get(contentValuesArray.size() - 1).getAsLong("_id");
		}

	}

}
