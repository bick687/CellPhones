package com.example.android.cellphones.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jitso on 10/27/2016.
 */

public class PhoneContract {
    /** Content Authority URI */
    public static final String CONTENT_AUTHORITY = "com.example.android.cellphones";

    /** Concatenate URI scheme */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Name of the table in the database */
    public static final String PATH_PHONES = "cellphones";

    private PhoneContract(){}

    public static final class PhoneEntry implements BaseColumns{
        /** Complete content URI with the table name "cellphones" */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PHONES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of cellphones.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;

        /** Create the table "cellphones" */
        public static final String TABLE_NAME = "cellphones";

        /** Create the columns(ID, image, name, price quantity) for the table "cellphones" */
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PHONE_IMAGE = "image";
        public static final String COLUMN_PHONE_NAME = "name";
        public static final String COLUMN_PHONE_PRICE = "price";
        public static final String COLUMN_PHONE_QUANTITY = "quantity";

    }
}
