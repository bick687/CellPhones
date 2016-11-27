package com.example.android.cellphones.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.cellphones.data.PhoneContract.PhoneEntry;

/**
 * Created by jitso on 10/27/2016.
 */

public class PhoneDbHelper extends SQLiteOpenHelper {
    //Tag for log messages
    public static final String LOG_TAG = PhoneDbHelper.class.getSimpleName();

    //Database version number. Increment the version if database schema change
    private static final int DATABASE_VERSION = 1;
    //Name for the database file
    private static final String PHONES_DATABASE_NAME = "inventory.db";

    //Database constructor
    public PhoneDbHelper(Context context) {
        super(context, PHONES_DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Create the database entries using the OnCreate method
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the cellphones table
        String SQL_PHONES_TABLE = "CREATE TABLE " + PhoneEntry.TABLE_NAME + " ("
                + PhoneEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PhoneEntry.COLUMN_PHONE_IMAGE + " TEXT, "
                + PhoneEntry.COLUMN_PHONE_NAME + " TEXT NOT NULL, "
                + PhoneEntry.COLUMN_PHONE_PRICE + " REAL NOT NULL, "
                + PhoneEntry.COLUMN_PHONE_QUANTITY + " INTEGER NOT NULL DEFAULT 0);";
        db.execSQL(SQL_PHONES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + PhoneEntry.TABLE_NAME);

        // create new table
        onCreate(db);
    }
}