package com.example.android.cellphones;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.cellphones.data.PhoneContract.PhoneEntry;

/**
 * Created by jitso on 10/27/2016.
 */

public class PhoneCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link PhoneCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public PhoneCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Fill out this method and return the list item view (instead of null)
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    /**
     * This method binds the cellphone data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current cellphone can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Fill out this method
        //Find views to inflate the list
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        //Find the column attributes
        int nameColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_NAME);
        int priceColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_QUANTITY);

        //Extract properties from the cursor
        String name = cursor.getString(nameColumnIndex);
        String price = cursor.getString(priceColumnIndex);
        String quantity = cursor.getString(quantityColumnIndex);

        //Populate the list with extracted name and summary
        nameTextView.setText(name);
        priceTextView.setText(price);
        quantityTextView.setText(quantity);
    }
}