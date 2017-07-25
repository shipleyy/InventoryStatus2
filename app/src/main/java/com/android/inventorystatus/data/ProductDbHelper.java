package com.android.inventorystatus.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.inventorystatus.data.ProductContract.ProductEntry;

class ProductDbHelper extends SQLiteOpenHelper {

    // Setting a log tag
    private static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    // The name of the database file
    private static final String DATABASE_NAME = "products.db";

    // Setting the database version
    private static final int DATABASE_VERSION = 1;

    // The constructor
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);


    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_PRODUCTS_TABLE =
                "CREATE TABLE " + ProductEntry.TABLE_NAME + "(" +
                        ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                        ProductEntry.COLUMN_PRODUCT_SKU + " INTEGER NOT NULL, " +
                        ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                        ProductEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0, " +
                        ProductEntry.COLUMN_IMAGE + " TEXT NOT NULL, " +
                        ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, " +
                        ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL);";

        Log.v(LOG_TAG, "SQL statement sent = " + SQL_CREATE_PRODUCTS_TABLE);

        // Execute the SQL code
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
