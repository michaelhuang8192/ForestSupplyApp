package com.tinyappsdev.tinypos.data;

//Auto-Generated, See Tools

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class ContentProviderEx extends ContentProvider {

	public final static String AUTHORITY = "com.tinyappsdev.tinypos";
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

%var k = 0;
%for(var table in this.schemas) {    
    private final static int URI_${table.toUpperCase()} = ${k++};
    private final static int URI_${table.toUpperCase()}_RID = ${k++};
%}

%var k = 1000;
%for(var join of this.tableJoins) {    
    private final static int URI_${join.join('_').toUpperCase()} = ${k++};
    private final static int URI_${join.join('_').toUpperCase()}_RID = ${k++};
%}

    public final static UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    protected DatabaseOpenHelper mDatabaseOpenHelper;
    protected ContentResolver mContentResolver;

    static {
%for(var table in this.schemas) {
        MATCHER.addURI(AUTHORITY, "${table}", URI_${table.toUpperCase()});
        MATCHER.addURI(AUTHORITY, "${table}/#", URI_${table.toUpperCase()}_RID);
%}
%for(var join of this.tableJoins) {
        MATCHER.addURI(AUTHORITY, "${join.join('_')}", URI_${join.join('_').toUpperCase()});
        MATCHER.addURI(AUTHORITY, "${join.join('_')}/#", URI_${join.join('_').toUpperCase()}_RID);
%}
    }

    static class JoinedTable {
        String tableName;
        boolean isMajor;
        JoinedTable(String tableName, boolean isMajor) {
            this.tableName = tableName;
            this.isMajor = isMajor;
        }
    }
    final static Map<String, JoinedTable[]> JoinedTableMap = new HashMap<String, JoinedTable[]>();
    static {
<%
var tbs = new Map();
for(var join of this.tableJoins) {
    for(var i = 0; i < join.length; i++) {
        if(!tbs.has(join[i])) tbs.set(join[i], []);
        tbs.get(join[i]).push({name: join.join('_'), isMajor: i == 0 ? true : false});
    }
}
for(var tb of tbs) {
%>
        JoinedTableMap.put("${tb[0]}", new JoinedTable[] {
%   for(var i = 0; i < tb[1].length; i++) {
            new JoinedTable("${tb[1][i].name}", ${tb[1][i].isMajor})${i == tb[1].length - 1 ? '' : ','}
%   }
        });
%}
    }

    @Override
    public boolean onCreate() {
        mContentResolver = getContext().getContentResolver();
        mDatabaseOpenHelper = DatabaseOpenHelper.getInstance(getContext());
        return true;
    }

    public static Uri BuildUri(String... paths)
    {
        if(paths == null || paths.length == 0) return BASE_CONTENT_URI;

        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for(String path: paths)
            builder.appendPath(path);

        return builder.build();
    }

    public static Uri BuildUri(String[] paths, Object[][] params)
    {
        if(paths == null && params == null) return BASE_CONTENT_URI;

        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for(String path: paths)
            builder.appendPath(path);

        for(Object[] param: params)
            builder.appendQueryParameter(String.valueOf(param[0]),String.valueOf(param[1]));

        return builder.build();
    }

    @Override
    public String getType(Uri uri) {
        switch(MATCHER.match(uri)) {
%for(var table in this.schemas) {
            case URI_${table.toUpperCase()}: {
                return "vnd.android.cursor.dir/${table}";
            }
            case URI_${table.toUpperCase()}_RID: {
                return "vnd.android.cursor.item/${table}";
            }
%}
%for(var join of this.tableJoins) {
            case URI_${join.join('_').toUpperCase()}: {
                return "vnd.android.cursor.dir/${join.join('_')}";
            }
            case URI_${join.join('_').toUpperCase()}_RID: {
                return "vnd.android.cursor.item/${join.join('_')}";
            }
%}
            default: {
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }
    }

    protected Object[] prepareSelection(Uri uri, String selection, String[] selectionArgs) {
        return prepareSelection(uri, selection, selectionArgs, "_id");
    }
    
    protected Object[] prepareSelection(Uri uri, String selection, String[] selectionArgs, String colId) {
        if(selection != null) {
            selection = String.format("%s=? AND (%s)", colId, selection);
            int argsLen = selectionArgs == null ? 0 : selectionArgs.length;
            String[] new_selectionArgs = new String[1 + argsLen];
            new_selectionArgs[0] = uri.getPathSegments().get(1);
            for(int i = 0; i < argsLen; i++)
                new_selectionArgs[1 + i] = selectionArgs[i];
            selectionArgs = new_selectionArgs;
        } else {
            selection = String.format("_id=?", colId);
            selectionArgs = new String[] {uri.getPathSegments().get(1)};
        }

        return new Object[] {selection, selectionArgs};
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int code = MATCHER.match(uri);
        if(code == -1 || code >= 1000)
            throw new IllegalArgumentException("Unknown URI " + uri);

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        int count = 0;

        String tableName = uri.getPathSegments().get(0);
        if(code % 2 == 0) {
            count = db.update(tableName, values, selection, selectionArgs);
            if(count > 0) notifyChange(uri, code);

        } else {
            Object[] sel = prepareSelection(uri, selection, selectionArgs);
            selection = (String)sel[0];
            selectionArgs = (String[])sel[1];

            count = db.update(tableName, values, selection, selectionArgs);
            if(count > 0) notifyChange(uri, code);

        }

        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int code = MATCHER.match(uri);
        if(code == -1 || code >= 1000)
            throw new IllegalArgumentException("Unknown URI " + uri);

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Uri ret = null;

        String tableName = uri.getPathSegments().get(0);
        long id;
        if(uri.getBooleanQueryParameter("replace", false))
            id = db.replace(tableName, null, contentValues);
        else
            id = db.insert(tableName, null, contentValues);

        if(id >= 0) {
            notifyChange(uri, code);
            ret = BuildUri(tableName, "" + id);
        }

        return ret;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int code = MATCHER.match(uri);
        if(code == -1 || code >= 1000)
            throw new IllegalArgumentException("Unknown URI " + uri);

        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        int count = 0;

        String tableName = uri.getPathSegments().get(0);
        if(code % 2 == 0) {
            count = db.delete(tableName, selection, selectionArgs);
            if(count > 0) notifyChange(uri, code);

        } else {
            Object[] sel = prepareSelection(uri, selection, selectionArgs);
            selection = (String)sel[0];
            selectionArgs = (String[])sel[1];

            count = db.delete(tableName, selection, selectionArgs);
            if(count > 0) notifyChange(uri, code);

        }

        return count;
    }

    protected String[] getTableJoinInfo(int code) {
        switch(code) {
%for(var join of this.tableJoins) {
<%
    var joinSql = join.join(' left join ');
    var onSql = [];
    for(var i = 0; i < join.length - 1; i++) {
        var left = join[i];
        var right = join[i + 1];
        var rightL = right[0].toLowerCase() + right.substr(1);
        onSql.push(`${left}.${rightL}Id=${right}._id`);
    }
    onSql = onSql.join(' AND ');
%>
            case URI_${join.join('_').toUpperCase()}:
            case URI_${join.join('_').toUpperCase()}_RID: {
                return new String[] {
                    "${joinSql} ON (${onSql})",
                    "${join[0]}._id"
                };
            }
%}
        }

        return null;
    }

    protected void notifyChange(Uri uri, int code) {
        mContentResolver.notifyChange(uri, null);

        String tableName = uri.getPathSegments().get(0);
        JoinedTable[] joinedTableArray = JoinedTableMap.get(tableName);
        if(joinedTableArray == null) return;
        
        for(JoinedTable joinedTable : joinedTableArray) {
            if(code % 2 == 0 || !joinedTable.isMajor)
                mContentResolver.notifyChange(BuildUri(joinedTable.tableName), null);
            else
                mContentResolver.notifyChange(BuildUri(joinedTable.tableName, uri.getPathSegments().get(1)), null);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int code = MATCHER.match(uri);
        if(code == -1)
            throw new IllegalArgumentException("Unknown URI " + uri);

        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        Cursor cursor = null;

        String tableName;
        String colId;
        if(code >= 1000) {
            String[] tableJoinInfo = getTableJoinInfo(code);
            tableName = tableJoinInfo[0];
            colId = tableJoinInfo[1];
        } else {
            tableName = uri.getPathSegments().get(0);
            colId = "_id";
        }

        if(code % 2 == 0) {
            cursor = db.query(tableName,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder
            );
            cursor.setNotificationUri(mContentResolver, uri);

        } else {
            Object[] sel = prepareSelection(uri, selection, selectionArgs, colId);
            selection = (String)sel[0];
            selectionArgs = (String[])sel[1];

            cursor = db.query(tableName,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        null
            );
            cursor.setNotificationUri(mContentResolver, uri);

        }

        return cursor;
    }

}
