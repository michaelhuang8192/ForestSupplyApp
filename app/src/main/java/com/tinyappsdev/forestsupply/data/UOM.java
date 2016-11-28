package com.tinyappsdev.forestsupply.data;

//Auto-Generated, See Tools

import android.database.sqlite.SQLiteDatabase;
import java.util.Map;
import java.util.List;



public class UOM {


	String name;
	String factor;


	public void setName(String pName) {
		this.name = pName;
	}

	public String getName() {
		return this.name;
	}

	public void setFactor(String pFactor) {
		this.factor = pFactor;
	}

	public String getFactor() {
		return this.factor;
	}

	public static class Schema {
		public final static String TABLE_NAME = "UOM";

		public final static String COL_NAME = "name";
		public final static String COL_FACTOR = "factor";

		public final static String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS UOM (" + 
			"name TEXT," +
			"factor TEXT" +
			")";


		public static void CreateTable(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLE);
		}

		public static void DropTable(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS UOM");
		}

		public static String[] getColNames() {
			return new String[] {
				"UOM.name AS UOM_name",
				"UOM.factor AS UOM_factor"
			};
		}

	}

}
