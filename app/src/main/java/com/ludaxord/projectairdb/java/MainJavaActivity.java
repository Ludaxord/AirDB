package com.ludaxord.projectairdb.java;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ludaxord.airdb.java.AirDB;
import com.ludaxord.projectairdb.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainJavaActivity extends AppCompatActivity {

    private AirDB airDB;

    public AirDB getAirDB() {
        return airDB;
    }

    public void setAirDB(AirDB airDB) {
        this.airDB = airDB;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_java);

        HashMap<String, String> columns = new HashMap<>();
        HashMap<String, HashMap<String, String>> table = new HashMap<>();
        table.put("example_java_table", columns);
        airDB = new AirDB(getApplicationContext(), "example_database", null, null, table, 1);
        HashMap<String, List<String>> structure = airDB.structure("example_java_table", null, null, null, null, null, null);
        for (Map.Entry<String, List<String>> entry : structure.entrySet()) {
            String columnName = entry.getKey();
            List<String> columnValues = entry.getValue();
            Log.i("AirDB", columnName);
            for (String value : columnValues) {
                Log.i("AirDB", value);
            }
        }
    }
}
