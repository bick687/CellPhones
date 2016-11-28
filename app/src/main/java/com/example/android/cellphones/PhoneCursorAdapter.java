package com.example.android.cellphones;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.cellphones.data.PhoneContract.PhoneEntry;

import static com.example.android.cellphones.R.id.quantity;

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
     * list item layout. For example, the name for the current cellphone can be set on the name
     * TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find the current id of the cellphone in the list using cursor.
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(PhoneEntry._ID));
        // Implement the view holder
        final ViewHolder holder = new ViewHolder();
        // Fill out this method
        //Find views to inflate the list
        holder.nameTextView = (TextView) view.findViewById(R.id.name);
        holder.quantityTextView = (TextView) view.findViewById(quantity);
        holder.priceTextView = (TextView) view.findViewById(R.id.price);
        holder.salesTextView = (TextView) view.findViewById(R.id.sales);

        //Find the column attributes
        int nameColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_PRICE);
        int salesColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_SALES);

        //Extract properties from the cursor
        String name = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        Double prices = cursor.getDouble(priceColumnIndex);
        final int sales = cursor.getInt(salesColumnIndex);

        //Populate the list with extracted name and summary
        holder.nameTextView.setText(name);
        holder.quantityTextView.setText("Quantity: " + quantity);
        holder.priceTextView.setText("Price: $" + String.format("%.2f", prices));
        holder.salesTextView.setText("Sold(Units): " + sales);

        // Handle the sales button and add onClickListeners
        Button salesButton = (Button) view.findViewById(R.id.sales_button);
        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();

                // If there are any cellphones to sell. Update the quantity and sale field
                // simultaneously.
                if (quantity > 0) {
                    int mQuantity;
                    mQuantity = quantity - 1;
                    values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, mQuantity);
                    Uri uri = ContentUris.withAppendedId(PhoneEntry.CONTENT_URI, id);
                    context.getContentResolver().update(uri, values, null, null);
                    holder.quantityTextView.setText("Quantity: " + mQuantity);

                    int mSales;
                    mSales = sales + 1;
                    values.put(PhoneEntry.COLUMN_PHONE_SALES, mSales);
                    uri = ContentUris.withAppendedId(PhoneEntry.CONTENT_URI, id);
                    context.getContentResolver().update(uri, values, null, null);
                    holder.salesTextView.setText("Sold(Units): " + mSales);
                }
                context.getContentResolver().notifyChange(PhoneEntry.CONTENT_URI, null);
            }
        });
    }

    // Define the ViewHolder method
    private static class ViewHolder {
        TextView nameTextView;
        TextView quantityTextView;
        TextView priceTextView;
        TextView salesTextView;
    }
}
