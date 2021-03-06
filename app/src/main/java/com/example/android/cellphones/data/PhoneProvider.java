package com.example.android.cellphones.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.cellphones.data.PhoneContract.PhoneEntry;

/**
 * Created by jitso on 10/27/2016.
 */

public class PhoneProvider extends ContentProvider {

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PhoneProvider.class.getSimpleName();
    /**
     * URI matcher code for the content URI for the cellphones table
     */
    private static final int CELLPHONES = 100;
    /**
     * URI matcher code for the content URI for a single phone in the cellphones table
     */
    private static final int CELLPHONE_ID = 101;

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        //The content URI "content://com.example.android.cellphones/cellphones" will map to the
        // integer code {@link #PHONES}. This URI provides access to the whole cellphones table.
        sUriMatcher.addURI(PhoneContract.CONTENT_AUTHORITY, PhoneContract.PATH_PHONES, CELLPHONES);

        //The content URI "content://com.example.android.cellphones/cellphones/id" will map to the
        // integer
        //code {@link #CELLPHONE_ID}. This URI provides access to only single row of the cellphones
        // table.
        sUriMatcher.addURI(PhoneContract.CONTENT_AUTHORITY, PhoneContract.PATH_PHONES + "/#",
                CELLPHONE_ID);
    }

    /**
     * Database helper
     */
    private PhoneDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PhoneDbHelper(getContext());
        // Create and initialize a PhoneDbHelper object to gain access to the cellphones database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection
     * arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CELLPHONES:
                // For the CELLPHONES code, query the cellphones table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the cellphones table.
                // Perform database query on cellphones table
                cursor = database.query(PhoneEntry.TABLE_NAME, projection, selection,
                        selectionArgs,
                        null, null, sortOrder);

                break;
            case CELLPHONE_ID:
                // For the CELLPHONE_ID code, extract out the ID from the URI.
                // For an example URI such as
                // "content://com.example.android.cellphones/cellphones/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the cellphones table where the _id equals
                // 3 to return a Cursor containing that row of the table.
                cursor = database.query(PhoneEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the cursor
        // If the data changes at this URI then update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CELLPHONES:
                return insertCellPhone(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a Cellphone into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertCellPhone(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(PhoneEntry.COLUMN_PHONE_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Cellphone requires a name");
        }

        // If the price is provided, check that it's greater than or equal to 0 kg
        Integer price = values.getAsInteger(PhoneEntry.COLUMN_PHONE_PRICE);

        if (price != null && price < 0) {
            throw new IllegalArgumentException("Cellphone requires valid price");
        }

        Integer quantity = values.getAsInteger(PhoneEntry.COLUMN_PHONE_QUANTITY);

        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Cellphone requires valid quantity");
        }

        // Check that the image is not null
        String image = values.getAsString(PhoneEntry.COLUMN_PHONE_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Cellphone requires an image");
        }

        // Insert a new cellphone into the cellphones database table with the given ContentValues
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new cellphone with the given values
        long id = db.insert(PhoneEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the cellphone content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CELLPHONES:
                return updateCellPhone(uri, contentValues, selection, selectionArgs);
            case CELLPHONE_ID:
                // For the CELLPHONE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateCellPhone(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update cellphones in the database with the given content values. Apply the changes
     * to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more
     * cellphones).
     * Return the number of rows that were successfully updated.
     */
    private int updateCellPhone(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {

        // Update the selected phones in the cellphones database table with the given ContentValues
        //Check if name already exist in the database or name is not null
        if (values.containsKey(PhoneEntry.COLUMN_PHONE_NAME)) {
            String name = values.getAsString(PhoneEntry.COLUMN_PHONE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Cellphone requires a name");
            }
        }

        //Check if the price is less than zero(negative).
        if (values.containsKey(PhoneEntry.COLUMN_PHONE_PRICE)) {
            Integer price = values.getAsInteger(PhoneEntry.COLUMN_PHONE_PRICE);
            if (price != null && price <= 0) {
                throw new IllegalArgumentException("Cellphone requires valid price");
            }
        }

        //Check if the quantity already exist or if its valid
        if (values.containsKey(PhoneEntry.COLUMN_PHONE_QUANTITY)) {
            Integer quantity = values.getAsInteger(PhoneEntry.COLUMN_PHONE_PRICE);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Cellphone requires valid quantity");
            }
        }

        //If there are no values to update, then do not try to update the database
        if (values.size() == 0) {
            return 0;
        }

        //Otherwise, get writable database to update the database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(PhoneEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Notify all listeners that the data has changed for the cellphone content URI
        getContext().getContentResolver().notifyChange(uri, null);

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CELLPHONES:

                int rowsDeleted = database.delete(PhoneEntry.TABLE_NAME, selection, selectionArgs);

                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows deleted
                return rowsDeleted;
            case CELLPHONE_ID:
                // Delete a single row given by the ID in the URI
                selection = PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(PhoneEntry.TABLE_NAME, selection, selectionArgs);

                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                // Return the number of rows deleted
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CELLPHONES:
                return PhoneEntry.CONTENT_LIST_TYPE;
            case CELLPHONE_ID:
                return PhoneEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}