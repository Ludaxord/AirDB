package com.ludaxord.projectairdb.kotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ludaxord.airdb.kotlin.AirDB
import com.ludaxord.projectairdb.R

class MainKotlinActivity : AppCompatActivity() {

    private lateinit var airDB: AirDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        airDB = AirDB(
            applicationContext,
            "example_database",
            "example_kotlin_table",
            hashMapOf(),
            null
        )
        val structure = airDB.structure("example_kotlin_table")
        Log.i("airdb", structure.toString())

    }
}
