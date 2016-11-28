package com.tinyappsdev.forestsupply.data;

//Auto-Generated, See Tools

import android.database.sqlite.SQLiteDatabase;
import java.util.Map;
import java.util.List;



public class Product {


	long _id;
	long inventoryCountId;
	String productNum;
	String description;
	String manufacturerNo;
	double onHand;
	long upc;
	List<UOM> uomList;

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

	public void setProductNum(String pProductNum) {
		this.productNum = pProductNum;
	}

	public String getProductNum() {
		return this.productNum;
	}

	public void setDescription(String pDescription) {
		this.description = pDescription;
	}

	public String getDescription() {
		return this.description;
	}

	public void setManufacturerNo(String pManufacturerNo) {
		this.manufacturerNo = pManufacturerNo;
	}

	public String getManufacturerNo() {
		return this.manufacturerNo;
	}

	public void setOnHand(double pOnHand) {
		this.onHand = pOnHand;
	}

	public double getOnHand() {
		return this.onHand;
	}

	public void setUpc(long pUpc) {
		this.upc = pUpc;
	}

	public long getUpc() {
		return this.upc;
	}

	public void setUomList(List<UOM> pUomList) {
		this.uomList = pUomList;
	}

	public List<UOM> getUomList() {
		return this.uomList;
	}

	public static class Schema {
		public final static String TABLE_NAME = "Product";

		public final static String COL_ID = "_id";
		public final static String COL_INVENTORYCOUNTID = "inventoryCountId";
		public final static String COL_PRODUCTNUM = "productNum";
		public final static String COL_DESCRIPTION = "description";
		public final static String COL_MANUFACTURERNO = "manufacturerNo";
		public final static String COL_ONHAND = "onHand";
		public final static String COL_UPC = "upc";
		public final static String COL_UOMLIST = "uomList";

		public final static String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS Product (" + 
			"_id INTEGER PRIMARY KEY ASC," +
			"inventoryCountId INTEGER," +
			"productNum TEXT," +
			"description TEXT," +
			"manufacturerNo TEXT," +
			"onHand REAL," +
			"upc INTEGER," +
			"uomList TEXT" +
			")";


		public static void CreateTable(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLE);
		}

		public static void DropTable(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS Product");
		}

		public static String[] getColNames() {
			return new String[] {
				"Product._id AS Product__id",
				"Product.inventoryCountId AS Product_inventoryCountId",
				"Product.productNum AS Product_productNum",
				"Product.description AS Product_description",
				"Product.manufacturerNo AS Product_manufacturerNo",
				"Product.onHand AS Product_onHand",
				"Product.upc AS Product_upc",
				"Product.uomList AS Product_uomList"
			};
		}

	}

}
