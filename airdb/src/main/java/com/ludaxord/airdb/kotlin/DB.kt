package com.ludaxord.airdb.kotlin

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.util.Log.w

abstract class DB(
    private val context: Context,
    private val name: String,
    private val factory: SQLiteDatabase.CursorFactory? = null,
    dbVersion: Int = 1
) : SQLiteOpenHelper(context, name, factory, dbVersion) {

    private var db: SQLiteDatabase = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, factory)

    open var tables: HashMap<String, HashMap<String, String>>? = null

    override fun onCreate(db: SQLiteDatabase?) {
        this.db = db ?: context.openOrCreateDatabase(name, Context.MODE_PRIVATE, factory)
        initTables()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    private fun initTables(tables: HashMap<String, HashMap<String, String>>? = this.tables) {
        if (tables != null) {
            for ((table, columns) in tables) {
                val exists = isTableExists(table)
                Log.i("airdb", "table: $table -> exists: $exists")
                if (!exists) {
                    createNewTable(table, columns)
                } else {
                    val structure = structure(table)
                    val columnNames = getColumns(table)
                    Log.v("airdb", "structure: $structure")
                    Log.w("airdb", "columnNames: $columnNames")
                    checkColumns(table, columnNames, columns)
                }
            }
        }
    }

    @SuppressLint("Recycle")
    private fun getColumns(tableName: String): List<String> {
        val resolveTable = when (tableName) {
            "transaction" -> "'$tableName'"
            "group" -> "'$tableName'"
            "order" -> "'$tableName'"
            else -> tableName
        }
        val dbCursor = db.query(resolveTable, null, null, null, null, null, null)
        return dbCursor.columnNames.toList()
    }

    private fun checkColumns(
        tableName: String,
        oldColumns: List<String>,
        newColumns: HashMap<String, String>
    ) {
        for ((column, type) in newColumns) {
            val columnExists = oldColumns.contains(column)
            if (!columnExists) {
                Log.v("airdb", "column: $column of type: $type")
                addColumnsBySQL(tableName, column, type)
            }
        }
    }

    fun addColumnsBySQL(tableName: String, column: String, type: String) {
        when (column) {
            "transaction" -> db.execSQL("ALTER TABLE $tableName ADD COLUMN '$column' $type;")
            "group" -> db.execSQL("ALTER TABLE $tableName ADD COLUMN '$column' $type;")
            "order" -> db.execSQL("ALTER TABLE $tableName ADD COLUMN '$column' $type;")
            else -> db.execSQL("ALTER TABLE $tableName ADD COLUMN $column $type;")
        }
    }

    private fun createNewTable(table: String, columns: HashMap<String, String>) {

        var columnsInTable = ""

        val separateChar = ","

        var i = 0

        for (column in columns) {
            columnsInTable += if (column.key == "transaction") {
                if (i++ == columns.size - 1) {
                    "'${column.key}' ${column.value}"
                } else {
                    "'${column.key}' ${column.value}$separateChar "
                }
            } else if (column.key == "group") {
                if (i++ == columns.size - 1) {
                    "'${column.key}' ${column.value}"
                } else {
                    "'${column.key}' ${column.value}$separateChar "
                }
            } else if (column.key == "order") {
                if (i++ == columns.size - 1) {
                    "'${column.key}' ${column.value}"
                } else {
                    "'${column.key}' ${column.value}$separateChar "
                }
            } else {
                if (i++ == columns.size - 1) {
                    "${column.key} ${column.value}"
                } else {
                    "${column.key} ${column.value}$separateChar "
                }
            }
        }

        val query = when (table) {
            "transaction" -> "CREATE TABLE IF NOT EXISTS '$table'($columnsInTable)"
            "group" -> "CREATE TABLE IF NOT EXISTS '$table'($columnsInTable)"
            "order" -> "CREATE TABLE IF NOT EXISTS '$table'($columnsInTable)"
            else -> "CREATE TABLE IF NOT EXISTS $table($columnsInTable)"
        }

        db.execSQL(query)
    }

    fun openDB() {
        if (db == null || !db.isOpen) {
            db = readableDatabase
        }

        if (!db.isReadOnly) {
            db.close()
        }
    }

    fun selectColumnBySQL(tableName: String, columns: List<String>): Cursor {
        var col = ""
        var i = 0
        for (column in columns) {
            col += if (i++ == columns.size - 1) {
                column
            } else {
                "$column, "
            }
        }
        val cursor = db.rawQuery("SELECT $col FROM $tableName", null)
        cursor.moveToFirst()
        return cursor
    }

    @SuppressLint("Recycle")
    fun structure(
        table: String,
        orderBy: String? = null,
        having: String? = null,
        groupBy: String? = null,
        selectionArgs: Array<String>? = null,
        selection: String? = null,
        columns: Array<String>? = null
    ): HashMap<String, List<String>> {
        val column = HashMap<String, List<String>>()
        val resolveTable = when (table) {
            "transaction" -> "'$table'"
            "group" -> "'$table'"
            "order" -> "'$table'"
            else -> table
        }
        val cursor =
            db.query(resolveTable, columns, selection, selectionArgs, groupBy, having, orderBy)
        val columnNames = cursor.columnNames
        for (columnName in columnNames) {
            if (cursor.moveToFirst()) {
                val l = ArrayList<String>()
                while (!cursor.isAfterLast) {
                    val columnValue = cursor.getString(cursor.getColumnIndex(columnName))
                    l.add(columnValue)
                    cursor.moveToNext()
                }
                column[columnName] = l.toList()
            }
        }
        return column
    }

    fun insertBySQL(tableName: String, columnValues: HashMap<String?, Any?>) {
        val resolveTable = when (tableName) {
            "transaction" -> "'$tableName'"
            "group" -> "'$tableName'"
            "order" -> "'$tableName'"
            else -> tableName
        }
        val executedQuery = "INSERT INTO $resolveTable"
        var columns = ""
        var values = ""
        var i = 0
        for ((column, value) in columnValues) {
            if (column == "transaction") {
                if (i++ == columnValues.size - 1) {
                    columns += "'$column'"
                    values += "'$value'"
                } else {
                    columns += "'$column', "
                    values += "'$value', "
                }
            } else if (column == "group") {
                if (i++ == columnValues.size - 1) {
                    columns += "'$column'"
                    values += "'$value'"
                } else {
                    columns += "'$column', "
                    values += "'$value', "
                }
            } else if (column == "order") {
                if (i++ == columnValues.size - 1) {
                    columns += "'$column'"
                    values += "'$value'"
                } else {
                    columns += "'$column', "
                    values += "'$value', "
                }
            } else {
                if (i++ == columnValues.size - 1) {
                    columns += column
                    values += "'$value'"
                } else {
                    columns += "$column, "
                    values += "'$value', "
                }
            }
        }

        val sql = "$executedQuery($columns) VALUES ($values)"
        db.execSQL(sql)
    }

    fun updateBySQL(
        tableName: String,
        columnValuesInserts: HashMap<String?, Any?>,
        tableCredentials: HashMap<String, String>,
        columnWhere: String?,
        valueWhere: Any?,
        andColumnsValues: HashMap<String?, Any?>? = null
    ) {
        val resolveTable = when (tableName) {
            "transaction" -> "'$tableName'"
            "group" -> "'$tableName'"
            "order" -> "'$tableName'"
            else -> tableName
        }
        for ((columnInsert, valueInsert) in columnValuesInserts) {
            for ((tableColumn, columnType) in tableCredentials) {
                if (tableColumn == columnInsert) {
                    var strSQL = when (columnInsert) {
                        "transaction" -> "UPDATE $resolveTable SET '$columnInsert' = '$valueInsert'"
                        "group" -> "UPDATE $resolveTable SET '$columnInsert' = '$valueInsert'"
                        "order" -> "UPDATE $resolveTable SET '$columnInsert' = '$valueInsert'"
                        else -> "UPDATE $resolveTable SET $columnInsert = '$valueInsert'"
                    }

                    if (columnWhere != null && valueWhere != null) {
                        strSQL += when (columnWhere) {
                            "transaction" -> " WHERE '$columnWhere' = '$valueWhere'"
                            "group" -> " WHERE '$columnWhere' = '$valueWhere'"
                            "order" -> " WHERE '$columnWhere' = '$valueWhere'"
                            else -> " WHERE $columnWhere = '$valueWhere'"
                        }
                    }

                    if (andColumnsValues != null) {
                        var indx = 0
                        for ((columns, values) in andColumnsValues) {
                            strSQL += if (indx == 0) {
                                when (columnWhere) {
                                    "transaction" -> " WHERE '$columns' = '$values'"
                                    "group" -> " WHERE '$columns' = '$values'"
                                    "order" -> " WHERE '$columns' = '$values'"
                                    else -> " WHERE $columns = '$values'"
                                }
                            } else {
                                when (columns) {
                                    "transaction" -> " AND '$columns' = '$values'"
                                    "group" -> " AND '$columns' = '$values'"
                                    "order" -> " AND '$columns' = '$values'"
                                    else -> " AND $columns = '$values'"
                                }
                            }
                            indx++
                        }
                    }
                    db.execSQL(strSQL)
                }
            }
        }
    }


    fun removeSingleRow(tableName: String, column: String, value: Any) {
        db.execSQL("DELETE FROM $tableName WHERE $column= '$value'")
    }

    fun clear(tableName: String) {
        db.execSQL("DELETE FROM $tableName")
    }

    fun clearColumn(tableName: String, column: String) {
        val entries = ContentValues()
        entries.putNull(column)
        db.update(tableName, entries, null, null)
    }

    private fun sortToContentValues(
        values: ContentValues,
        columnName: String,
        type: String,
        value: Any
    ) {
        when (type) {
            "TEXT" -> {
                values.put(columnName, value as String)
            }
            "BLOB" -> {
                values.put(columnName, value as Byte)
            }
            "BOOLEAN" -> {
                values.put(columnName, value as Boolean)
            }
            "INTEGER" -> {
                values.put(columnName, value as Int)
            }
            "NUMERIC" -> {
                values.put(columnName, value as Double)
            }
            "REAL" -> {
                values.put(columnName, value as Long)
            }
        }
    }

    private fun checkIfValueExistsInColumn(tableName: String, column: String, value: Any): Boolean {
        val query = "SELECT * FROM $tableName WHERE $column = $value"
        val cursor = db.rawQuery(query, null)
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    private fun isTableExists(tableName: String): Boolean {
        val cursor =
            db.rawQuery(
                "select DISTINCT tbl_name from sqlite_master where tbl_name = '$tableName'",
                null
            )
        if (cursor != null) {
            if (cursor.count > 0) {
                cursor.close()
                return true
            }
            cursor.close()
        }
        return false
    }

    fun clearIfNotEmpty(tableName: String, column: String) {
        if (!isTableEmpty(tableName)) {
            clearColumn(tableName, column)
        }
    }

    @SuppressLint("Recycle")
    fun isTableEmpty(tableName: String): Boolean {
        val query = "SELECT count(*) FROM $tableName"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        return count <= 0
    }

    fun insertValues(
        tableName: String,
        keyValueHashMap: HashMap<String, HashMap<String, Any>>
    ): HashMap<String, HashMap<String, Any>> {
        val values = ContentValues()
        for ((columnName, typeValueHash) in keyValueHashMap) {
            for ((type, value) in typeValueHash) {
                val exists = checkIfValueExistsInColumn(tableName, columnName, value)
                if (!exists) {
                    sortToContentValues(values, columnName, type, value)
                }
            }
        }

        db.insert(tableName, null, values)

        return keyValueHashMap
    }

    fun insertValuesIfDoesntExists(
        tableName: String,
        keyValueHashMap: HashMap<String, HashMap<String, Any>>
    ): HashMap<String, HashMap<String, Any>> {
        val values = ContentValues()
        for ((columnName, typeValueHash) in keyValueHashMap) {
            for ((type, value) in typeValueHash) {
                sortToContentValues(values, columnName, type, value)
            }
        }

        db.insert(tableName, null, values)

        return keyValueHashMap
    }

    fun insertValueWithRemoveExistingValue(
        tableName: String,
        column: String,
        type: String,
        value: Any,
        clearTable: Boolean = false
    ) {
        if (clearTable) {
            w("airdb", "clearTable $clearTable")
            clearIfNotEmpty(tableName, column)
        }
        val contentValues = ContentValues()
        sortToContentValues(contentValues, column, type, value)
        w("airdb", "clearTable $contentValues")
        db.insert(tableName, null, contentValues)
    }

}
