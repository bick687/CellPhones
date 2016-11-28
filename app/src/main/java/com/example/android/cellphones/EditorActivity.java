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
import android.widget.TextView;
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
    private static final String STATE_URI = "STATE_URI";
<<<<<<< HEAD
    /**     * EditText field to enter the cellphone's name
=======
    // Add image button
    public Button mAddImageButton;
    // Link to the cellphone image to be stored in the database
    String mImageLink;
    /**
     * EditText field to enter the cellphone's name
>>>>>>> refs/remotes/origin/Cellphonesbeta
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
     * TextView field to show cellphone sales
     */
    private TextView mSalesTextView;
    /**
     * Content URI for the existing cellphone (null if it's a new cellphone)
     */
    private Uri mCurrentPhoneUri;
    // Initialize boolean to check for any changes to cellphone specs
    private boolean mCellPhoneHasChanged = false;
    // ImageView for the cellphone image
    private ImageView mCellphoneImageView;
    // Initialize boolean to check for any changes to cellphone quantity.
    private boolean mQuantityHasChanged = false;

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
        mSalesTextView = (TextView) findViewById(R.id.cellphone_sales);


        // Examine the intent that was used to launch this activity
        // In order to figure out if new cellphone needs to created or edit an existing one.
        Intent intent = getIntent();
        mCurrentPhoneUri = intent.getData();

        // Load cursor and get image link only when editing a cellphone
        if (mCurrentPhoneUri != null) {
            Cursor c = getContentResolver().query(mCurrentPhoneUri, null, null, null, null);
            if (c.moveToNext()) {
                mImageLink = c.getString(c.getColumnIndexOrThrow(PhoneEntry.COLUMN_PHONE_IMAGE));
                c.close();
            }
        }
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
            Toast.makeText(this, "Update or Save Failed. \nCellphone name required.",
                    Toast.LENGTH_SHORT).show();
            mNameEditText.setError("Please enter cellphone name");
            return;
        } else if (quantity.length() < 0) {
            Toast.makeText(this, "Update or Save Failed. \nQuantity cannot be negative.",
                    Toast.LENGTH_SHORT).show();
            mQuantityEditText.setError("No quantity added");
            return;
        } else if (price.length() == 0) {
            Toast.makeText(this, "Update or Save Failed. \nPrice cannot be zero.",
                    Toast.LENGTH_SHORT).show();
            mPriceEditText.setError("No price added");
            return;
        } else if (mImageLink == null) {
            Toast.makeText(this, "Update or Save Failed. \nCellphone image required.",
                    Toast.LENGTH_SHORT).show();
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

        MenuItem saleItem = menu.findItem(R.id.action_sale);
        saleItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mQuantityHasChanged = true;
                return false;
            }
        });

        MenuItem receivedItem = menu.findItem(R.id.action_receive);
        receivedItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mQuantityHasChanged = true;
                return false;
            }
        });
        return true;
    }

    // Create method for sale of a cellphone. Update the quantity field with the new quantity
    private void saleCellPhone() {
        String quantityString = mQuantityEditText.getText().toString().trim();
        // Check if the field is empty. If empty return.
        if (quantityString.matches("")) {
            Toast.makeText(this, "Quantity field is empty.", Toast.LENGTH_SHORT).show();
        } else {
            int quantityInteger = Integer.parseInt(quantityString);

            if (quantityInteger == 0) {
                Toast.makeText(this, "There are no more items to sell.", Toast.LENGTH_SHORT).show();
            } else {
                // Update the quantity if the cellphone sale is successful.
                int updatedQuantity = quantityInteger - 1;
                ContentValues values = new ContentValues();
                values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, updatedQuantity);
                mQuantityEditText.setText(Integer.toString(updatedQuantity));

                // Update the sale if the cellphone sale is successful.
                String salesString = mSalesTextView.getText().toString().trim();
                int salesInteger = Integer.parseInt(salesString);
                int updatedSales = salesInteger + 1;
                values.put(PhoneEntry.COLUMN_PHONE_SALES, updatedSales);
                mSalesTextView.setText(Integer.toString(updatedSales));

                if (mCurrentPhoneUri != null) {
                    getContentResolver().update(mCurrentPhoneUri, values, null, null);
                    getContentResolver().notifyChange(PhoneEntry.CONTENT_URI, null);
                }
            }
        }
    }

    // Create method to update the quantity when shipment is received.
    private void shipmentReceived() {
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantityInteger = Integer.parseInt(quantityString);
        int updatedQuantity = quantityInteger + 100;
        ContentValues values = new ContentValues();
        values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, updatedQuantity);
        mQuantityEditText.setText(Integer.toString(updatedQuantity));
    }

    private void saveCellPhone() {
        if (mCurrentPhoneUri != null && mQuantityHasChanged) {
            String quantityString = mQuantityEditText.getText().toString().trim();
            ContentValues values = new ContentValues();
            values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, quantityString);
            int rowsAffected = getContentResolver().update(mCurrentPhoneUri, values, null, null);
            Toast.makeText(this, "Cellphone sale successful.", Toast.LENGTH_SHORT).show();

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

        // If there are no changes detected, return to previous activity without saving.
        if (!mCellPhoneHasChanged) {
            return;
        }
        // Get the user inputs and store them as strings
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        Double priceDouble =
                Double.parseDouble(String.format(mPriceEditText.getText().toString(), 2));
        String salesString = mSalesTextView.getText().toString().trim();
        String imageString = mImageLink;

        // If no cellphone was added, return to previous activity without saving
        // any data
        if (mCurrentPhoneUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(imageString)) {
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and cellphone attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(PhoneEntry.COLUMN_PHONE_NAME, nameString);
        values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, quantityString);
        values.put(PhoneEntry.COLUMN_PHONE_PRICE, priceDouble);
        values.put(PhoneEntry.COLUMN_PHONE_IMAGE, imageString);
        values.put(PhoneEntry.COLUMN_PHONE_SALES, salesString);

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

    public void orderCellphonePack() {
        String subject = "Reorder 100 pack of " + mNameEditText.getText();
        String message = "Product Name: " + mNameEditText.getText() +
                "\nProduct Price: " + mPriceEditText.getText() +
                "\nQuantity to be ordered: 100 pack";
        String email = "vendor@gmail.com";
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, email);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
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
        builder.setNegativeButton(R.string.cancel, null);

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
            // Respond to a click on the "Sale" menu option
            case R.id.action_sale:
                saleCellPhone();
                return true;
            // Respond to a click on the "Order 100 pack" menu option
            case R.id.action_order:
                orderCellphonePack();
                return true;
            // Respond to a click on the "Receive 100 pack" menu option
            case R.id.action_receive:
                shipmentReceived();
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
                if (!mCellPhoneHasChanged && !mQuantityHasChanged) {
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
                PhoneEntry.COLUMN_PHONE_IMAGE,
                PhoneEntry.COLUMN_PHONE_SALES};

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
            int priceColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_IMAGE);
            int salesColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_SALES);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            int sales = cursor.getInt(salesColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(String.format("%.2f", price));
            mSalesTextView.setText(Integer.toString(sales));

            // If there's no image uploaded, do not try to parse the image link. Set the ImageView
            // null.
            if (image == null) {
                mCellphoneImageView.setImageURI(null);
                mCellphoneImageView.setVisibility(View.GONE);

            } else {
                mCellphoneImageView.setImageURI(Uri.parse(image));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mCellphoneImageView.setImageURI(null);
        mSalesTextView.setText("");
    }

    // On click method for the image upload
    public void uploadImage(View v) {
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

            // URI for the image to be uploaded. Convert the link to a string
            Uri selectedImage = resultData.getData();
            mImageLink = selectedImage.toString();

            try {
                Bitmap d = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                // Find the ImageView
                ImageView imageView = (ImageView) findViewById(R.id.cellphone_image);
                // Set the image in the imageView after decoding the String
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(getBitmapFromUri(selectedImage));

            } catch (IOException e) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    // Convert image to bitmap and scale down the image to fit the ImageView
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
        if (!mCellPhoneHasChanged && !mQuantityHasChanged) {
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