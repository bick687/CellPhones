package com.example.android.cellphones;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.cellphones.data.PhoneContract.PhoneEntry;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

        //Initialize the Cursor Loader
        private static final int URL_LOADER = 0;

        //Initialize the Cursor Adapter
        PhoneCursorAdapter mPhoneAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the cellphone data
        ListView cellphoneListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        cellphoneListView.setEmptyView(emptyView);

        mPhoneAdapter = new PhoneCursorAdapter(this, null);
        cellphoneListView.setAdapter(mPhoneAdapter);

        // Set an onItemClickListener on the ListView
        cellphoneListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                                               @Override
                                               public void onItemClick(AdapterView<?> adapterView,
                                                                       View view, int i, long id) {
                                                   //Create a new intent to go to
                                                   // {@link EditorActivity}
                                                   Intent intent = new Intent(CatalogActivity.this,
                                                           EditorActivity.class);
                                                   // Form the content URI that represents the
                                                   // specific cellphone that was clicked on, by
                                                   // appending the "id" onto the
                                                   // {@link PhoneEntry#CONTENT_URI}
                                                   Uri currentCellphoneUri =
                                                           ContentUris.withAppendedId(
                                                           PhoneEntry.CONTENT_URI, id);

                                                   // Set the URI on the data field of the intent
                                                   intent.setData(currentCellphoneUri);
                                                   startActivity(intent);
                                               }
                                           }
        );

        /*
         * Initializes the CursorLoader. The URL_LOADER value is eventually passed
         * to onCreateLoader().
         */
        getSupportLoaderManager().initLoader(URL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {PhoneEntry._ID,
                PhoneEntry.COLUMN_PHONE_NAME,
                PhoneEntry.COLUMN_PHONE_PRICE,
                PhoneEntry.COLUMN_PHONE_QUANTITY};

        return new CursorLoader(this, PhoneEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPhoneAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPhoneAdapter.swapCursor(null);
    }
}
