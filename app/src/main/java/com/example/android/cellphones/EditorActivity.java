package com.example.android.cellphones;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.cellphones.data.PhoneContract.PhoneEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.android.cellphones.data.PhoneProvider.LOG_TAG;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    //Initialize the Cursor Loader
    private static final int URL_LOADER = 0;
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int SEND_MAIL_REQUEST = 1;
    private static final String STATE_URI = "STATE_URI";
    /**
     * EditText field to enter the cellphone's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the cellphone's quantity
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the cellphone's price
     */
    private EditText mPriceEditText;
    /**
     * Content URI for the existing cellphone (null if it's a new cellphone)
     */
    private Uri mCurrentPhoneUri;

    // Initialize boolean to check for any changes to cellphone specs
    private boolean mCellPhoneHasChanged = false;

    // ImageView for the cellphone image
    private ImageView mCellphoneImageView;

    // Link to the cellphone image to be stored in the database
    String mImageLink;

    // Add image button
    private Button mAddImageButton;

    // Set up an onTouchListener
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCellPhoneHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all the views need to read user input
        mNameEditText = (EditText) findViewById(R.id.edit_cell_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_cell_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_cell_price);
        mAddImageButton = (Button) findViewById(R.id.image_upload_button);
        mCellphoneImageView = (ImageView) findViewById(R.id.cellphone_image);


        //mCellphoneImageView.setImageBitmap(getBitmapFromUri(mCurrentPhoneUri));

        // Examine the intent that was used to launch this activity
        // In order to figure out if new cellphone needs to created or edit an existing one.
        Intent intent = getIntent();
        mCurrentPhoneUri = intent.getData();

        // If the Intent does not contain a cellphone content URI, then we know that we are
        // creating a new cellphone.
        if (mCurrentPhoneUri == null) {
            // This is a new cellphone, so change the app bar to say "Add a cellphone"
            setTitle(getString(R.string.add_new_cellphone));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a cellphone that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing cellphone. Change the title to "Edit a Cellphone"
            setTitle(getString(R.string.edit_cellphone));

                    /*
         * Initializes the CursorLoader. The URL_LOADER value is eventually passed
         * to onCreateLoader().
         */
            getSupportLoaderManager().initLoader(URL_LOADER, null, this);
        }

        // Set onTouchListener on the user input field to check for changes
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mAddImageButton.setOnTouchListener(mTouchListener);

    }

    // Check if user entered the information correctly
    public void checkSubmission() {
        String name = mNameEditText.getText().toString();
        String quantity = mQuantityEditText.getText().toString();
        String price = mPriceEditText.getText().toString();

        if (name.length() == 0) {
            Toast.makeText(this, "Please enter cellphone name", Toast.LENGTH_SHORT).show();
            mNameEditText.setError("Please enter cellphone name");
            return;
        } else if (quantity.length() == 0) {
            Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
            mQuantityEditText.setError("No quantity added");
            return;
        } else if (price.length() == 0) {
            Toast.makeText(this, "Please enter price", Toast.LENGTH_SHORT).show();
            mPriceEditText.setError("No price added");
            return;
        } else if (mAddImageButton.getText().toString().length() == 0) {
            Toast.makeText(this, "Please upload image", Toast.LENGTH_SHORT).show();
            return;
        } else {
            saveCellPhone();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    private void saveCellPhone() {
        // Get the user inputs and store them as strings
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String imageString = mImageLink;

        // If no cellphone was added, return to previous activity without saving
        // any data
        if (mCurrentPhoneUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString)) {
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and cellphone attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(PhoneEntry.COLUMN_PHONE_NAME, nameString);
        values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, quantityString);
        values.put(PhoneEntry.COLUMN_PHONE_PRICE, priceString);
        values.put(PhoneEntry.COLUMN_PHONE_IMAGE, imageString);

        if (mCurrentPhoneUri == null) {
            // Insert a new phone into the provider, returning the content URI for the new cellphone
            Uri newUri = getContentResolver().insert(PhoneEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.cellphone_insert_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.cellphone_insert_successful,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING cellphone, so update the cellphone with content URI
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPhoneUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentPhoneUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, R.string.cellphone_update_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, R.string.cellphone_update_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.cellphone_deleted_successfully);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the cellphone.
                deleteCellPhone();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the cellphone.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the cellphone in the database.
     */
    private void deleteCellPhone() {
        // Only perform the delete if this is an existing cellphone.
        if (mCurrentPhoneUri != null) {
            // Call the ContentResolver to delete the cellphone at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPhoneUri
            // content URI already identifies the cellphone that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPhoneUri, null, null);

            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, R.string.cellphone_deletion_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, R.string.cellphone_deletion_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Insert cellphone information
                checkSubmission();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Show the confirmation dialog
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the cellphone hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mCellPhoneHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Then, in onPrepareOptionsMenu will get called and you can modify the Menu object by hiding
    // the delete menu option if it’s a new cellphone
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new cellphone, hide the "Delete" menu item.
        if (mCurrentPhoneUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all cellphone attributes, define a projection that contains
        // all columns from the cellphone table
        String[] projection = {PhoneEntry._ID,
                PhoneEntry.COLUMN_PHONE_NAME,
                PhoneEntry.COLUMN_PHONE_QUANTITY,
                PhoneEntry.COLUMN_PHONE_PRICE,
                PhoneEntry.COLUMN_PHONE_IMAGE};

        return new CursorLoader(this, mCurrentPhoneUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // if the cursor is null or there is less than one row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of cellphone attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getInt(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Double.toString(price));
            mCellphoneImageView.setImageURI(Uri.parse(image));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
    }

    // On click method for the image upload
    public void UploadImage(View v) {
            openImageSelector();
        }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
             // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
            // If the request code seen here doesn't match, it's the response to some other intent,
            // and the below code shouldn't run at all.

            if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

                Uri selectedImage = resultData.getData();
                mImageLink = selectedImage.toString();

                try {
                    MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    // Find the ImageView
                    ImageView imageView = (ImageView) findViewById(R.id.cellphone_image);
                    // Set the image in the imageView after decoding the String
                    imageView.setImageBitmap(getBitmapFromUri(selectedImage));

                } catch (IOException e) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                            .show();
                }
            }
        }


    public Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mCellphoneImageView.getWidth();
        int targetH = mCellphoneImageView.getHeight();

        InputStream input = null;

        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCurrentPhoneUri != null)
            outState.putString(STATE_URI, mCurrentPhoneUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            mCurrentPhoneUri = Uri.parse(savedInstanceState.getString(STATE_URI));
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing_msg, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the cellphone.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the cellphone hasn't changed, continue with handling back button press
        if (!mCellPhoneHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}
