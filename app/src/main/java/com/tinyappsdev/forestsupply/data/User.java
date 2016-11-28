package com.tinyappsdev.forestsupply.data;

//Auto-Generated, See Tools

import android.database.sqlite.SQLiteDatabase;
import java.util.Map;
import java.util.List;



public class User {


	long _id;
	String name;

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

	public static class Schema {
		public final static String TABLE_NAME = "User";

		public final static String COL_ID = "_id";
		public final static String COL_NAME = "name";

		public final static String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS User (" + 
			"_id INTEGER PRIMARY KEY ASC," +
			"name TEXT" +
			")";


		public static void CreateTable(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLE);
		}

		public static void DropTable(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS User");
		}

		public static String[] getColNames() {
			return new String[] {
				"User._id AS User__id",
				"User.name AS User_name"
			};
		}

	}

}
