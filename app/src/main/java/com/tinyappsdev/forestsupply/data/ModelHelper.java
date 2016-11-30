package com.tinyappsdev.forestsupply.data;

//Auto-Generated, See Tools

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import org.json.JSONException;
import org.json.JSONObject;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinyappsdev.forestsupply.helper.TinyMap;


public class ModelHelper {

	public final static TypeReference LIST_UOM_TYPEREF = new TypeReference<List<UOM>>(){};
	public final static TypeReference LIST_INVENTORYCOUNTEMPLOYEE_TYPEREF = new TypeReference<List<InventoryCountEmployee>>(){};

	final static ObjectMapper sObjectMapper = new ObjectMapper();
    static {
        sObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


	public static ObjectMapper getObjectMapper() { return sObjectMapper; }

	public static String toJson(Object obj) {
		try {
			return obj == null ? null : sObjectMapper.writeValueAsString(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <V> V fromJson(String jsonStr, Class<V> type) {
		try {
			return jsonStr == null ? null : sObjectMapper.readValue(jsonStr, type);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object fromJson(String jsonStr, TypeReference typeReference) {
		try {
			return jsonStr == null ? null : sObjectMapper.readValue(jsonStr, typeReference);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Product ProductFromMap(Map map) {
		Product obj = new Product();
		obj.setId((long)map.get("_id"));
		obj.setInventoryCountId((long)map.get("inventoryCountId"));
		obj.setProductNum((String)map.get("productNum"));
		obj.setDescription((String)map.get("description"));
		obj.setManufacturerNo((String)map.get("manufacturerNo"));
		obj.setOnHand((double)map.get("onHand"));
		obj.setUpc((long)map.get("upc"));
		obj.setUomList((List<UOM>)map.get("uomList"));

		return obj;
    }

    public static Map ProductToMap(Product obj) {
		Map map = new HashMap();
		map.put("_id", obj.getId());
		map.put("inventoryCountId", obj.getInventoryCountId());
		map.put("productNum", obj.getProductNum());
		map.put("description", obj.getDescription());
		map.put("manufacturerNo", obj.getManufacturerNo());
		map.put("onHand", obj.getOnHand());
		map.put("upc", obj.getUpc());
		map.put("uomList", obj.getUomList());

		return map;
    }

    public static Product ProductFromCursor(Cursor cursor) {
    	return ProductFromCursor(cursor, "");
    }

    public static Product ProductFromCursor(Cursor cursor, String prefix) {
		Product m = new Product();

		m.setId(cursor.getLong(cursor.getColumnIndex(prefix + "_id")));
		m.setInventoryCountId(cursor.getLong(cursor.getColumnIndex(prefix + "inventoryCountId")));
		m.setProductNum(cursor.getString(cursor.getColumnIndex(prefix + "productNum")));
		m.setDescription(cursor.getString(cursor.getColumnIndex(prefix + "description")));
		m.setManufacturerNo(cursor.getString(cursor.getColumnIndex(prefix + "manufacturerNo")));
		m.setOnHand(cursor.getDouble(cursor.getColumnIndex(prefix + "onHand")));
		m.setUpc(cursor.getLong(cursor.getColumnIndex(prefix + "upc")));
		m.setUomList((List<UOM>)fromJson(
			cursor.getString(cursor.getColumnIndex(prefix + "uomList")),
			LIST_UOM_TYPEREF
		));

		return m;
    }

    public static ContentValues ProductContentValuesFromJsonMap(TinyMap map) {
    	ContentValues m = new ContentValues();

		m.put("_id", map.getLong("_id"));
		m.put("inventoryCountId", map.getLong("inventoryCountId"));
		m.put("productNum", map.getString("productNum"));
		m.put("description", map.getString("description"));
		m.put("manufacturerNo", map.getString("manufacturerNo"));
		m.put("onHand", map.getDouble("onHand"));
		m.put("upc", map.getLong("upc"));
		m.put("uomList", toJson(map.get("uomList")));

		return m;
    }

	public static UOM UOMFromMap(Map map) {
		UOM obj = new UOM();
		obj.setName((String)map.get("name"));
		obj.setFactor((String)map.get("factor"));

		return obj;
    }

    public static Map UOMToMap(UOM obj) {
		Map map = new HashMap();
		map.put("name", obj.getName());
		map.put("factor", obj.getFactor());

		return map;
    }

    public static UOM UOMFromCursor(Cursor cursor) {
    	return UOMFromCursor(cursor, "");
    }

    public static UOM UOMFromCursor(Cursor cursor, String prefix) {
		UOM m = new UOM();

		m.setName(cursor.getString(cursor.getColumnIndex(prefix + "name")));
		m.setFactor(cursor.getString(cursor.getColumnIndex(prefix + "factor")));

		return m;
    }

    public static ContentValues UOMContentValuesFromJsonMap(TinyMap map) {
    	ContentValues m = new ContentValues();

		m.put("name", map.getString("name"));
		m.put("factor", map.getString("factor"));

		return m;
    }

	public static User UserFromMap(Map map) {
		User obj = new User();
		obj.setId((long)map.get("_id"));
		obj.setName((String)map.get("name"));

		return obj;
    }

    public static Map UserToMap(User obj) {
		Map map = new HashMap();
		map.put("_id", obj.getId());
		map.put("name", obj.getName());

		return map;
    }

    public static User UserFromCursor(Cursor cursor) {
    	return UserFromCursor(cursor, "");
    }

    public static User UserFromCursor(Cursor cursor, String prefix) {
		User m = new User();

		m.setId(cursor.getLong(cursor.getColumnIndex(prefix + "_id")));
		m.setName(cursor.getString(cursor.getColumnIndex(prefix + "name")));

		return m;
    }

    public static ContentValues UserContentValuesFromJsonMap(TinyMap map) {
    	ContentValues m = new ContentValues();

		m.put("_id", map.getLong("_id"));
		m.put("name", map.getString("name"));

		return m;
    }

	public static CountRecord CountRecordFromMap(Map map) {
		CountRecord obj = new CountRecord();
		obj.setId((long)map.get("_id"));
		obj.setInventoryCountId((long)map.get("inventoryCountId"));
		obj.setTeamId((int)map.get("teamId"));
		obj.setUserId((long)map.get("userId"));
		obj.setUserName((String)map.get("userName"));
		obj.setProductNum((String)map.get("productNum"));
		obj.setUom((String)map.get("uom"));
		obj.setQuantity((int)map.get("quantity"));
		obj.setCreatedTime((long)map.get("createdTime"));

		return obj;
    }

    public static Map CountRecordToMap(CountRecord obj) {
		Map map = new HashMap();
		map.put("_id", obj.getId());
		map.put("inventoryCountId", obj.getInventoryCountId());
		map.put("teamId", obj.getTeamId());
		map.put("userId", obj.getUserId());
		map.put("userName", obj.getUserName());
		map.put("productNum", obj.getProductNum());
		map.put("uom", obj.getUom());
		map.put("quantity", obj.getQuantity());
		map.put("createdTime", obj.getCreatedTime());

		return map;
    }

    public static CountRecord CountRecordFromCursor(Cursor cursor) {
    	return CountRecordFromCursor(cursor, "");
    }

    public static CountRecord CountRecordFromCursor(Cursor cursor, String prefix) {
		CountRecord m = new CountRecord();

		m.setId(cursor.getLong(cursor.getColumnIndex(prefix + "_id")));
		m.setInventoryCountId(cursor.getLong(cursor.getColumnIndex(prefix + "inventoryCountId")));
		m.setTeamId(cursor.getInt(cursor.getColumnIndex(prefix + "teamId")));
		m.setUserId(cursor.getLong(cursor.getColumnIndex(prefix + "userId")));
		m.setUserName(cursor.getString(cursor.getColumnIndex(prefix + "userName")));
		m.setProductNum(cursor.getString(cursor.getColumnIndex(prefix + "productNum")));
		m.setUom(cursor.getString(cursor.getColumnIndex(prefix + "uom")));
		m.setQuantity(cursor.getInt(cursor.getColumnIndex(prefix + "quantity")));
		m.setCreatedTime(cursor.getLong(cursor.getColumnIndex(prefix + "createdTime")));

		return m;
    }

    public static ContentValues CountRecordContentValuesFromJsonMap(TinyMap map) {
    	ContentValues m = new ContentValues();

		m.put("_id", map.getLong("_id"));
		m.put("inventoryCountId", map.getLong("inventoryCountId"));
		m.put("teamId", map.getInt("teamId"));
		m.put("userId", map.getLong("userId"));
		m.put("userName", map.getString("userName"));
		m.put("productNum", map.getString("productNum"));
		m.put("uom", map.getString("uom"));
		m.put("quantity", map.getInt("quantity"));
		m.put("createdTime", map.getLong("createdTime"));

		return m;
    }

	public static InventoryCount InventoryCountFromMap(Map map) {
		InventoryCount obj = new InventoryCount();
		obj.setId((long)map.get("_id"));
		obj.setName((String)map.get("name"));
		obj.setEmployeeList((List<InventoryCountEmployee>)map.get("employeeList"));
		obj.setProductCount((int)map.get("productCount"));
		obj.setCreatedTime((long)map.get("createdTime"));

		return obj;
    }

    public static Map InventoryCountToMap(InventoryCount obj) {
		Map map = new HashMap();
		map.put("_id", obj.getId());
		map.put("name", obj.getName());
		map.put("employeeList", obj.getEmployeeList());
		map.put("productCount", obj.getProductCount());
		map.put("createdTime", obj.getCreatedTime());

		return map;
    }

    public static InventoryCount InventoryCountFromCursor(Cursor cursor) {
    	return InventoryCountFromCursor(cursor, "");
    }

    public static InventoryCount InventoryCountFromCursor(Cursor cursor, String prefix) {
		InventoryCount m = new InventoryCount();

		m.setId(cursor.getLong(cursor.getColumnIndex(prefix + "_id")));
		m.setName(cursor.getString(cursor.getColumnIndex(prefix + "name")));
		m.setEmployeeList((List<InventoryCountEmployee>)fromJson(
			cursor.getString(cursor.getColumnIndex(prefix + "employeeList")),
			LIST_INVENTORYCOUNTEMPLOYEE_TYPEREF
		));
		m.setProductCount(cursor.getInt(cursor.getColumnIndex(prefix + "productCount")));
		m.setCreatedTime(cursor.getLong(cursor.getColumnIndex(prefix + "createdTime")));

		return m;
    }

    public static ContentValues InventoryCountContentValuesFromJsonMap(TinyMap map) {
    	ContentValues m = new ContentValues();

		m.put("_id", map.getLong("_id"));
		m.put("name", map.getString("name"));
		m.put("employeeList", toJson(map.get("employeeList")));
		m.put("productCount", map.getInt("productCount"));
		m.put("createdTime", map.getLong("createdTime"));

		return m;
    }

	public static InventoryCountEmployee InventoryCountEmployeeFromMap(Map map) {
		InventoryCountEmployee obj = new InventoryCountEmployee();
		obj.setId((long)map.get("_id"));
		obj.setName((String)map.get("name"));
		obj.setTeamId((int)map.get("teamId"));

		return obj;
    }

    public static Map InventoryCountEmployeeToMap(InventoryCountEmployee obj) {
		Map map = new HashMap();
		map.put("_id", obj.getId());
		map.put("name", obj.getName());
		map.put("teamId", obj.getTeamId());

		return map;
    }

    public static InventoryCountEmployee InventoryCountEmployeeFromCursor(Cursor cursor) {
    	return InventoryCountEmployeeFromCursor(cursor, "");
    }

    public static InventoryCountEmployee InventoryCountEmployeeFromCursor(Cursor cursor, String prefix) {
		InventoryCountEmployee m = new InventoryCountEmployee();

		m.setId(cursor.getLong(cursor.getColumnIndex(prefix + "_id")));
		m.setName(cursor.getString(cursor.getColumnIndex(prefix + "name")));
		m.setTeamId(cursor.getInt(cursor.getColumnIndex(prefix + "teamId")));

		return m;
    }

    public static ContentValues InventoryCountEmployeeContentValuesFromJsonMap(TinyMap map) {
    	ContentValues m = new ContentValues();

		m.put("_id", map.getLong("_id"));
		m.put("name", map.getString("name"));
		m.put("teamId", map.getInt("teamId"));

		return m;
    }


	public static class ProductCursor {
		private Cursor mCursor;
		private String mPrefix;
		public ProductCursor(Cursor cursor) { this(cursor, ""); }
		public ProductCursor(Cursor cursor, String prefix) {
			mCursor = cursor;
			mPrefix = prefix;
		}

		public long getId() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "_id"));
		}
		public long getInventoryCountId() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "inventoryCountId"));
		}
		public String getProductNum() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "productNum"));
		}
		public String getDescription() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "description"));
		}
		public String getManufacturerNo() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "manufacturerNo"));
		}
		public double getOnHand() {
			return mCursor.getDouble(mCursor.getColumnIndex(mPrefix + "onHand"));
		}
		public long getUpc() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "upc"));
		}
		public List<UOM> getUomList() {
			return (List<UOM>)fromJson(
				mCursor.getString(mCursor.getColumnIndex(mPrefix + "uomList")),
				LIST_UOM_TYPEREF
			);
		}

	}

	public static class UOMCursor {
		private Cursor mCursor;
		private String mPrefix;
		public UOMCursor(Cursor cursor) { this(cursor, ""); }
		public UOMCursor(Cursor cursor, String prefix) {
			mCursor = cursor;
			mPrefix = prefix;
		}

		public String getName() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "name"));
		}
		public String getFactor() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "factor"));
		}

	}

	public static class UserCursor {
		private Cursor mCursor;
		private String mPrefix;
		public UserCursor(Cursor cursor) { this(cursor, ""); }
		public UserCursor(Cursor cursor, String prefix) {
			mCursor = cursor;
			mPrefix = prefix;
		}

		public long getId() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "_id"));
		}
		public String getName() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "name"));
		}

	}

	public static class CountRecordCursor {
		private Cursor mCursor;
		private String mPrefix;
		public CountRecordCursor(Cursor cursor) { this(cursor, ""); }
		public CountRecordCursor(Cursor cursor, String prefix) {
			mCursor = cursor;
			mPrefix = prefix;
		}

		public long getId() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "_id"));
		}
		public long getInventoryCountId() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "inventoryCountId"));
		}
		public int getTeamId() {
			return mCursor.getInt(mCursor.getColumnIndex(mPrefix + "teamId"));
		}
		public long getUserId() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "userId"));
		}
		public String getUserName() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "userName"));
		}
		public String getProductNum() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "productNum"));
		}
		public String getUom() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "uom"));
		}
		public int getQuantity() {
			return mCursor.getInt(mCursor.getColumnIndex(mPrefix + "quantity"));
		}
		public long getCreatedTime() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "createdTime"));
		}

	}

	public static class InventoryCountCursor {
		private Cursor mCursor;
		private String mPrefix;
		public InventoryCountCursor(Cursor cursor) { this(cursor, ""); }
		public InventoryCountCursor(Cursor cursor, String prefix) {
			mCursor = cursor;
			mPrefix = prefix;
		}

		public long getId() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "_id"));
		}
		public String getName() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "name"));
		}
		public List<InventoryCountEmployee> getEmployeeList() {
			return (List<InventoryCountEmployee>)fromJson(
				mCursor.getString(mCursor.getColumnIndex(mPrefix + "employeeList")),
				LIST_INVENTORYCOUNTEMPLOYEE_TYPEREF
			);
		}
		public int getProductCount() {
			return mCursor.getInt(mCursor.getColumnIndex(mPrefix + "productCount"));
		}
		public long getCreatedTime() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "createdTime"));
		}

	}

	public static class InventoryCountEmployeeCursor {
		private Cursor mCursor;
		private String mPrefix;
		public InventoryCountEmployeeCursor(Cursor cursor) { this(cursor, ""); }
		public InventoryCountEmployeeCursor(Cursor cursor, String prefix) {
			mCursor = cursor;
			mPrefix = prefix;
		}

		public long getId() {
			return mCursor.getLong(mCursor.getColumnIndex(mPrefix + "_id"));
		}
		public String getName() {
			return mCursor.getString(mCursor.getColumnIndex(mPrefix + "name"));
		}
		public int getTeamId() {
			return mCursor.getInt(mCursor.getColumnIndex(mPrefix + "teamId"));
		}

	}


}
