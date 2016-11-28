package com.tinyappsdev.forestsupply.data;

//Auto-Generated, See Tools

import android.database.sqlite.SQLiteDatabase;
import java.util.Map;
import java.util.List;



public class InventoryCount {


	long _id;
	String name;
	List<InventoryCountEmployee> employeeList;
	int productCount;
	long createdTime;

	public void set_id(long _id) { setId(_id); }
	public long get_id() { return getId(); }

	public void setId(long pId) {
		this._id = pId;
	}

	public long getId() {
		return this._id;
	}

	public void setName(String pName) {
		this.name = pName;
	}

	public String getName() {
		return this.name;
	}

	public void setEmployeeList(List<InventoryCountEmployee> pEmployeeList) {
		this.employeeList = pEmployeeList;
	}

	public List<InventoryCountEmployee> getEmployeeList() {
		return this.employeeList;
	}

	public void setProductCount(int pProductCount) {
		this.productCount = pProductCount;
	}

	public int getProductCount() {
		return this.productCount;
	}

	public void setCreatedTime(long pCreatedTime) {
		this.createdTime = pCreatedTime;
	}

	public long getCreatedTime() {
		return this.createdTime;
	}

	public static class Schema {
		public final static String TABLE_NAME = "InventoryCount";

		public final static String COL_ID = "_id";
		public final static String COL_NAME = "name";
		public final static String COL_EMPLOYEELIST = "employeeList";
		public final static String COL_PRODUCTCOUNT = "productCount";
		public final static String COL_CREATEDTIME = "createdTime";

		public final static String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS InventoryCount (" + 
			"_id INTEGER PRIMARY KEY ASC," +
			"name TEXT," +
			"employeeList TEXT," +
			"productCount INTEGER," +
			"createdTime INTEGER" +
			")";


		public static void CreateTable(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLE);
		}

		public static void DropTable(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS InventoryCount");
		}

		public static String[] getColNames() {
			return new String[] {
				"InventoryCount._id AS InventoryCount__id",
				"InventoryCount.name AS InventoryCount_name",
				"InventoryCount.employeeList AS InventoryCount_employeeList",
				"InventoryCount.productCount AS InventoryCount_productCount",
				"InventoryCount.createdTime AS InventoryCount_createdTime"
			};
		}

	}

}
