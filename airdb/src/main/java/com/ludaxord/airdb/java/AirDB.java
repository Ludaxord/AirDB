package com.ludaxord.airdb.java;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AirDB extends DB {

    public AirDB(Context context, String name, String tableName, @Nullable HashMap<String, String> columns, @Nullable HashMap<String, HashMap<String, String>> table, int version) {
        super(context, name, table, null, version);
        initTables(tableName, columns, table);
    }

    private void initTables(@Nullable String tableName, @Nullable HashMap<String, String> columns, @Nullable HashMap<String, HashMap<String, String>> table) {
        if (table != null) {
            this.tables = table;
        } else if (tableName != null) {
            HashMap<String, HashMap<String, String>> newTable = new HashMap<>();
            if (columns != null) {
                newTable.put(tableName, columns);
            } else {
                newTable.put(tableName, new HashMap<String, String>());
            }
            this.tables = newTable;
        }
    }

    public HashMap<String, List<String>> getTable(String key) {
        return structure(key, null, null, null, null, null, null);
    }

    public HashMap<String, List<String>> getTableWhere(String key, HashMap<String, String> wheres) {
        ArrayList<String> selectionsArgs = new ArrayList<>();
        StringBuilder selection = new StringBuilder();
        for (Map.Entry<String, String> entry : wheres.entrySet()) {
            String column = entry.getKey();
            String value = entry.getKey();
            selectionsArgs.add(value);
            selection.append(column).append(" = ? ");
        }
        selection = new StringBuilder(selection.substring(0, selection.toString().length() - 1));
        return structure(key, null, null, null, arrayListToArray(selectionsArgs), selection.toString(), null);
    }

    public void setValues(String tableName, HashMap<String, Object> props, String primaryKeyName) {
        Object primaryKey = props.get(primaryKeyName);
        ArrayList<String> selectionArgs = new ArrayList<>();
        selectionArgs.add((String) primaryKey);
        HashMap<String, List<String>> structure = structure(tableName, null, null, null, arrayListToArray(selectionArgs), primaryKeyName + " = ?", null);
        if (structure.isEmpty()) {
            insertBySQL(tableName, props);
        } else {
            Log.e("airdb", "offer already exists");
        }
    }

    public void updateOffer(String tableName, HashMap<String, Object> props, String primaryKeyName) {
        assert tables != null;
        HashMap<String, String> columns = tables.get(tableName);
        Object primaryKey = props.get(primaryKeyName);
        updateBySQL(tableName, props, columns, primaryKeyName, primaryKey, null);
    }
}
