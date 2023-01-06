package com.example.currency_converter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DatabaseMaster {
    private AdminDatabase sqliteDbJgh;
    public ArrayList<String> currenciesFromDbJgh = new ArrayList<String>();
    public ArrayList<String> ratesFromDbJgh = new ArrayList<String>();

    public DatabaseMaster(AdminDatabase sqliteDbJgh) {
        this.sqliteDbJgh = sqliteDbJgh;
    }

    public void insertData(String[] currencies, String[] rates) {
        SQLiteDatabase db = sqliteDbJgh.getReadableDatabase();
        for (int i = 0; i < currencies.length; i++) {
            ContentValues entryJgh = new ContentValues();
            entryJgh.put("id", i);
            entryJgh.put("currency_name", currencies[i]);
            entryJgh.put("ratio", rates[i]);
            db.insert("currencies", null, entryJgh);
        }
    }

    public void wipeData() {
        SQLiteDatabase db = sqliteDbJgh.getReadableDatabase();
        db.execSQL("delete from currencies");
    }

    public void retrieveData() {
        String queryJgh = "select * from currencies";
        SQLiteDatabase db = sqliteDbJgh.getReadableDatabase();
        Cursor c = db.rawQuery(queryJgh, null);
        Integer i = 0;
        while (c.moveToNext()) {
            String presentCurrencyJgh = c.getString(c.getColumnIndex("currency_name"));
            String presentRateJgh = c.getString(c.getColumnIndex("ratio"));
            currenciesFromDbJgh.add(presentCurrencyJgh);
            ratesFromDbJgh.add(presentRateJgh);
        }
    }
}
