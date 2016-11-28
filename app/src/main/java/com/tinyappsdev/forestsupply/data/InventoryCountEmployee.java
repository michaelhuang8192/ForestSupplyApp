package com.tinyappsdev.forestsupply.data;

//Auto-Generated, See Tools

import android.database.sqlite.SQLiteDatabase;
import java.util.Map;
import java.util.List;



public class InventoryCountEmployee {


	long _id;
	String name;
	int teamId;

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

	public void setTeamId(int pTeamId) {
		this.teamId = pTeamId;
	}

	public int getTeamId() {
		return this.teamId;
	}

	public static class Schema {
		public final static String TABLE_NAME = "InventoryCountEmployee";

		public final static String COL_ID = "_id";
		public final static String COL_NAME = "name";
		public final static String COL_TEAMID = "teamId";

		public final static String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS InventoryCountEmployee (" + 
			"_id INTEGER PRIMARY KEY ASC," +
			"name TEXT," +
			"teamId INTEGER" +
			")";


		public static void CreateTable(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLE);
		}

		public static void DropTable(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS InventoryCountEmployee");
		}

		public static String[] getColNames() {
			return new String[] {
				"InventoryCountEmployee._id AS InventoryCountEmployee__id",
				"InventoryCountEmployee.name AS InventoryCountEmployee_name",
				"InventoryCountEmployee.teamId AS InventoryCountEmployee_teamId"
			};
		}

	}

}
