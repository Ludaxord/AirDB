package com.ludaxord.airdb.kotlin

import android.content.Context
import android.util.Log

open class AirDB(
    context: Context,
    name: String,
    tableName: String?,
    columns: HashMap<String, String>?,
    table: HashMap<String, HashMap<String, String>>?
) :
    DB(context, name) {

    init {
        initTables(tableName, columns, table)
    }

    private fun initTables(
        tableName: String?,
        columns: HashMap<String, String>?,
        table: HashMap<String, HashMap<String, String>>?
    ) {
        if (table != null) {
            this.tables = table
        } else if (tableName != null) {
            val newTable = HashMap<String, HashMap<String, String>>()
            if (columns != null) {
                newTable[tableName] = columns
            } else {
                newTable[tableName] = HashMap()
            }
            this.tables = newTable
        }
    }

    fun getTable(key: String): HashMap<String, List<String>> {
        return structure(key)
    }

    fun getTableWhere(key: String, wheres: HashMap<String, String>): HashMap<String, List<String>> {
        val selectionsArgs = ArrayList<String>()
        var selection = ""
        for ((column, value) in wheres) {
            selectionsArgs.add(value)
            selection += "$column = ? "
        }
        selection = selection.substring(0, selection.length - 1)
        return structure(key, selection = selection, selectionArgs = selectionsArgs.toTypedArray())
    }

    fun setValues(tableName: String, props: HashMap<String?, Any?>, primaryKeyName: String) {
        val primaryKey = props[primaryKeyName]
        val structure = structure(
            tableName,
            selection = "$primaryKeyName = ?",
            selectionArgs = arrayOf(primaryKey as String)
        )
        if (structure.isNullOrEmpty()) {
            insertBySQL(tableName, props)
        } else {
            Log.e("airdb", "offer already exists")
        }
    }

    fun updateOffer(tableName: String, props: HashMap<String?, Any?>, primaryKeyName: String) {
        val columns = tables?.get(tableName)
        val primaryKey = props[primaryKeyName]
        columns?.let { cols -> updateBySQL(tableName, props, cols, primaryKeyName, primaryKey) }
    }
}
