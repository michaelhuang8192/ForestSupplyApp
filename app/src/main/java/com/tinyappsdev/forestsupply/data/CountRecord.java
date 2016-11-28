package com.tinyappsdev.forestsupply.data;

//Auto-Generated, See Tools

import android.database.sqlite.SQLiteDatabase;
import java.util.Map;
import java.util.List;



public class CountRecord {


	long _id;
	long inventoryCountId;
	long userId;
	String productNum;
	String uom;
	int quantity;
	long createdTime;

	public void set_id(long _id) { setId(_id); }
	public long get_id() { return getId(); }

	public void setId(long pId) {
		this._id = pId;
	}

	public long getId() {
		return this._id;
	}

	public void setInventoryCountId(long pInventoryCountId) {
		this.inventoryCountId = pInventoryCountId;
	}

	public long getInventoryCountId() {
		return this.inventoryCountId;
	}

	public void setUserId(long pUserId) {
		this.userId = pUserId;
	}

	public long getUserId() {
		return this.userId;
	}

	public void setProductNum(String pProductNum) {
		this.productNum = pProductNum;
	}

	public String getProductNum() {
		return this.productNum;
	}

	public void setUom(String pUom) {
		this.uom = pUom;
	}

	public String getUom() {
		return this.uom;
	}

	public void setQuantity(int pQuantity) {
		this.quantity = pQuantity;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setCreatedTime(long pCreatedTime) {
		this.createdTime = pCreatedTime;
	}

	public long getCreatedTime() {
		return this.createdTime;
	}

	public static class Schema {
		public final static String TABLE_NAME = "CountRecord";

		public final static String COL_ID = "_id";
		public final static String COL_INVENTORYCOUNTID = "inventoryCountId";
		public final static String COL_USERID = "userId";
		public final static String COL_PRODUCTNUM = "productNum";
		public final static String COL_UOM = "uom";
		public final static String COL_QUANTITY = "quantity";
		public final static String COL_CREATEDTIME = "createdTime";

		public final static String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS CountRecord (" + 
			"_id INTEGER PRIMARY KEY ASC," +
			"inventoryCountId INTEGER," +
			"userId INTEGER," +
			"productNum TEXT," +
			"uom TEXT," +
			"quantity INTEGER," +
			"createdTime INTEGER" +
			")";


		public static void CreateTable(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLE);
		}

		public static void DropTable(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS CountRecord");
		}

		public static String[] getColNames() {
			return new String[] {
				"CountRecord._id AS CountRecord__id",
				"CountRecord.inventoryCountId AS CountRecord_inventoryCountId",
				"CountRecord.userId AS CountRecord_userId",
				"CountRecord.productNum AS CountRecord_productNum",
				"CountRecord.uom AS CountRecord_uom",
				"CountRecord.quantity AS CountRecord_quantity",
				"CountRecord.createdTime AS CountRecord_createdTime"
			};
		}

	}

}
