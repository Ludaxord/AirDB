package com.ludaxord.airdb.java;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class DB extends SQLiteOpenHelper {

    private Context context;
    private String name;
    private SQLiteDatabase.CursorFactory factory;

    private SQLiteDatabase db;

    @Nullable
    HashMap<String, HashMap<String, String>> tables = null;

    DB(Context context, String name, HashMap<String, HashMap<String, String>> tables, @Nullable SQLiteDatabase.CursorFactory factory, @Nullable Integer version) {
        super(context, name, factory, version);
        this.context = context;
        this.name = name;
        this.tables = tables;
        this.factory = factory;
        this.db = context.openOrCreateDatabase(this.name, Context.MODE_PRIVATE, factory);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = (db != null) ? db : context.openOrCreateDatabase(name, Context.MODE_PRIVATE, factory);
        initTables(null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @SuppressWarnings("unchecked")
    static <E> E[] arrayListToArray(ArrayList<E> list) {
        int s;
        if (list == null || (s = list.size()) < 1)
            return null;
        E[] temp;
        E typeHelper = list.get(0);

        try {
            Object o = Array.newInstance(typeHelper.getClass(), s);
            temp = (E[]) o;

            for (int i = 0; i < list.size(); i++)
                Array.set(temp, i, list.get(i));
        } catch (Exception e) {
            return null;
        }

        return temp;
    }

    private void initTables(@Nullable HashMap<String, HashMap<String, String>> tables) {

        if (tables == null) {
            tables = this.tables;
        }

        Log.d("airdb", String.valueOf(this.tables));
        Log.w("airdb", String.valueOf(tables));

        if (tables != null) {

            for (Map.Entry<String, HashMap<String, String>> entry : tables.entrySet()) {
                String table = entry.getKey();
                HashMap<String, String> columns = entry.getValue();
                boolean exists = isTableExists(table);
                Log.i("airdb", "table: " + table + " -> exists: " + exists);
                if (!exists) {
                    createNewTable(table, columns);
                } else {
                    HashMap<String, List<String>> structure = structure(table, null, null, null, null, null, null);
                    List<String> columnNames = getColumns(table);
                    Log.v("airdb", "structure: " + structure);
                    Log.w("airdb", "columnNames: " + columnNames);
                    checkColumns(table, columnNames, columns);
                }
            }
        }
    }

    @SuppressLint("Recycle")
    private List<String> getColumns(String tableName) {
        String resolveTable;

        switch (tableName) {
            case "transaction":
                resolveTable = "'" + tableName + "'";
            case "group":
                resolveTable = "'" + tableName + "'";
            case "order":
                resolveTable = "'" + tableName + "'";
            default:
                resolveTable = tableName;
        }

        Cursor dbCursor = db.query(resolveTable, null, null, null, null, null, null);
        return Arrays.asList(dbCursor.getColumnNames());
    }

    private void checkColumns(String tableName, List<String> oldColumns, HashMap<String, String> newColumns) {
        for (Map.Entry<String, String> entry : newColumns.entrySet()) {
            String type = entry.getKey();
            String column = entry.getValue();
            boolean columnExists = oldColumns.contains(column);
            if (!columnExists) {
                Log.v("airdb", "column: " + column + " of type: " + type);
                addColumnsBySQL(tableName, column, type);
            }
        }
    }

    public void addColumnsBySQL(String tableName, String column, String type) {
        switch (column) {
            case "transaction":
                db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN '" + column + "' " + type + ";");
            case "group":
                db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN '" + column + "' " + type + ";");
            case "order":
                db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN '" + column + "' " + type + ";");
            default:
                db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + column + " " + type + ";");
        }
    }

    private void createNewTable(String table, HashMap<String, String> columns) {

        StringBuilder columnsInTable = new StringBuilder();
        String query = "";
        String separateChar = ",";
        int i = 0;

        for (Map.Entry<String, String> entry : columns.entrySet()) {
            if (i++ == columns.size() - 1) {
                columnsInTable.append("'").append(entry.getKey()).append("' ").append(entry.getValue());
            } else {
                columnsInTable.append("'").append(entry.getKey()).append("' ").append(entry.getValue()).append(separateChar);
            }
        }

        switch (table) {
            case "transaction":
                query = "CREATE TABLE IF NOT EXISTS '" + table + "'(" + columnsInTable + ")";
            case "group":
                query = "CREATE TABLE IF NOT EXISTS '" + table + "'(" + columnsInTable + ")";
            case "order":
                query = "CREATE TABLE IF NOT EXISTS '" + table + "'(" + columnsInTable + ")";
            default:
                query = "CREATE TABLE IF NOT EXISTS " + table + "(" + columnsInTable + ")";
        }

        db.execSQL(query);
    }

    public void openDB() {
        if (db == null || !db.isOpen()) {
            db = getReadableDatabase();
        }

        if (!db.isReadOnly()) {
            db.close();
        }
    }

    public Cursor selectColumnBySQL(String tableName, List<String> columns) {
        StringBuilder col = new StringBuilder();
        int i = 0;
        for (String column : columns) {
            if (i++ == columns.size() - 1) {
                col.append(column);
            } else {
                col.append(column).append(", ");
            }
        }
        Cursor cursor = db.rawQuery("SELECT $col FROM " + tableName, null);
        cursor.moveToFirst();
        return cursor;
    }

    @SuppressLint("Recycle")
    public HashMap<String, List<String>> structure(String table, @Nullable String orderBy, @Nullable String having, @Nullable String groupBy, @Nullable String[] selectionArgs, @Nullable String selection, @Nullable String[] columns) {
        HashMap<String, List<String>> column = new HashMap<>();
        String resolveTable;
        switch (table) {
            case "transaction":
                resolveTable = "'" + table + "'";
            case "group":
                resolveTable = "'" + table + "'";
            case "order":
                resolveTable = "'" + table + "'";
            default:
                resolveTable = table;
        }

        Log.i("airdb", String.valueOf(db.getPath()));

        Cursor cursor = db.query(resolveTable, columns, selection, selectionArgs, groupBy, having, orderBy);
        String[] columnNames = cursor.getColumnNames();
        for (String columnName : columnNames) {
            if (cursor.moveToFirst()) {
                ArrayList<String> l = new ArrayList<>();
                while (!cursor.isAfterLast()) {
                    String columnValue = cursor.getString(cursor.getColumnIndex(columnName));
                    l.add(columnValue);
                    cursor.moveToNext();
                }
                column.put(columnName, l);
            }
        }
        return column;
    }

    public void insertBySQL(String tableName, HashMap<String, Object> columnValues) {
        String resolveTable = "";
        switch (tableName) {
            case "transaction":
                resolveTable = "'" + tableName + "'";
            case "group":
                resolveTable = "'" + tableName + "'";
            case "order":
                resolveTable = "'" + tableName + "'";
            default:
                resolveTable = tableName;
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
            String column = entry.getKey();
            Object value = entry.getValue();
            switch (column) {
                case "transaction":
                case "group":
                case "order":
                    if (i++ == columnValues.size() - 1) {
                        columns.append("'").append(column).append("'");
                        values.append("'").append(value).append("'");
                    } else {
                        columns.append("'").append(column).append("', ");
                        values.append("'").append(value).append("', ");
                    }
                    break;
                default:
                    if (i++ == columnValues.size() - 1) {
                        columns.append(column);
                        values.append("'").append(value).append("'");
                    } else {
                        columns.append("").append(column).append(", ");
                        values.append("'").append(value).append("', ");
                    }
                    break;
            }
        }

        String sql = "INSERT INTO " + resolveTable + "(" + columns + ")VALUES(" + values + ")";
        db.execSQL(sql);
    }

    public void updateBySQL(
            String tableName,
            HashMap<String, Object> columnValuesInserts,
            HashMap<String, String> tableCredentials,
            String columnWhere,
            Object valueWhere,
            @Nullable HashMap<String, Object> andColumnsValues
    ) {
        String resolveTable = "";
        switch (tableName) {
            case "transaction":
                resolveTable = "'" + tableName + "'";
            case "group":
                resolveTable = "'" + tableName + "'";
            case "order":
                resolveTable = "'" + tableName + "'";
            default:
                resolveTable = tableName;
        }
        for (Map.Entry<String, Object> columnEntry : columnValuesInserts.entrySet()) {
            String columnInsert = columnEntry.getKey();
            Object valueInsert = columnEntry.getValue();
            for (Map.Entry<String, String> tableEntry : tableCredentials.entrySet()) {
                String tableColumn = tableEntry.getKey();
                String columnType = tableEntry.getValue();
                if (tableColumn.equals(columnInsert)) {
                    StringBuilder strSQL = new StringBuilder();
                    switch (columnInsert) {
                        case "transaction":
                            strSQL = new StringBuilder("UPDATE $resolveTable SET '" + columnInsert + "' = '" + valueInsert + "'");
                        case "group":
                            strSQL = new StringBuilder("UPDATE $resolveTable SET '" + columnInsert + "' = '" + valueInsert + "'");
                        case "order":
                            strSQL = new StringBuilder("UPDATE $resolveTable SET '" + columnInsert + "' = '" + valueInsert + "'");
                        default:
                            strSQL = new StringBuilder("UPDATE $resolveTable SET " + columnInsert + " = '" + valueInsert + "'");
                    }

                    if (columnWhere != null && valueWhere != null) {
                        switch (columnWhere) {
                            case "transaction":
                                strSQL.append(" WHERE '").append(columnWhere).append("' = '").append(valueWhere).append("'");
                            case "group":
                                strSQL.append(" WHERE '").append(columnWhere).append("' = '").append(valueWhere).append("'");
                            case "order":
                                strSQL.append(" WHERE '").append(columnWhere).append("' = '").append(valueWhere).append("'");
                            default:
                                strSQL.append(" WHERE ").append(columnWhere).append(" = '").append(valueWhere).append("'");
                        }
                    }

                    if (andColumnsValues != null) {
                        int indx = 0;
                        for (Map.Entry<String, Object> entry : andColumnsValues.entrySet()) {
                            String columns = entry.getKey();
                            Object values = entry.getValue();
                            if (indx == 0) {
                                assert columnWhere != null;
                                switch (columnWhere) {
                                    case "transaction":
                                        strSQL.append(" WHERE '").append(columns).append("' = '").append(values).append("'");
                                    case "group":
                                        strSQL.append(" WHERE '").append(columns).append("' = '").append(values).append("'");
                                    case "order":
                                        strSQL.append(" WHERE '").append(columns).append("' = '").append(values).append("'");
                                    default:
                                        strSQL.append(" WHERE ").append(columns).append(" = '").append(values).append("'");
                                }
                            } else {
                                switch (columns) {
                                    case "transaction":
                                        strSQL.append(" AND '").append(columns).append("' = '").append(values).append("'");
                                    case "group":
                                        strSQL.append(" AND '").append(columns).append("' = '").append(values).append("'");
                                    case "order":
                                        strSQL.append(" AND '").append(columns).append("' = '").append(values).append("'");
                                    default:
                                        strSQL.append(" AND ").append(columns).append(" = '").append(values).append("'");
                                }
                            }
                            indx++;
                        }
                    }
                    db.execSQL(strSQL.toString());
                }
            }
        }
    }

    public void clearColumn(String tableName, String column) {
        ContentValues entries = new ContentValues();
        entries.putNull(column);
        db.update(tableName, entries, null, null);
    }

    private Boolean checkIfValueExistsInColumn(String tableName, String column, Object value) {
        String query = "SELECT * FROM " + tableName + " WHERE $column = $value";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private boolean isTableExists(String tableName) {
        Cursor cursor =
                db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void clearIfNotEmpty(String tableName, String column) {
        if (!isTableEmpty(tableName)) {
            clearColumn(tableName, column);
        }
    }

    @SuppressLint("Recycle")
    public boolean isTableEmpty(String tableName) {
        String query = "SELECT count(*) FROM " + tableName;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        return count <= 0;
    }

}
