package com.android.inventorystatus;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.inventorystatus.data.ProductContract.ProductEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // Creating a log tag
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    // Image data loader identifier
    private static final int IMAGE_GALLERY_REQUEST = 20;

    // Product data loader identifier
    private static final int EXISTING_PRODUCT_LOADER = 0;

    // Product image URI loader identifier
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";

    // Declaring the context
    private final Context mContext = this;

    // URI for the product image - null if its a new product
    private Uri mImageUri;

    // Image path from the URI
    private String imagePath;

    // Declaring the views

    private EditText mProductNameEditText;
    private EditText mProductSkuEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private ImageView mProductImage;
    private EditText mContactNameEditText;
    private EditText mContactEmailEditText;
    private Button mAddImage;
    private LinearLayout mOrder;
    private Button mAddStock;
    private Button mSubtractStock;

    // Content URI for the existing product (null if it's a new product)
    private Uri mCurrentProductUri;

    // Boolean to notice if the data has been updated
    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Finding the views
        mProductNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mProductSkuEditText = (EditText) findViewById(R.id.edit_product_sku);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mProductImage = (ImageView) findViewById(R.id.edit_image);
        mContactNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mContactEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);
        mAddImage = (Button) findViewById(R.id.add_image);
        mOrder = (LinearLayout) findViewById(R.id.email_button);
        mAddStock = (Button) findViewById(R.id.plus);
        mSubtractStock = (Button) findViewById(R.id.minus);

        // Check the intent that was used to launch this activity,
        // to see if we're creating a new product or editing an existing one
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a content URI, then create a new product
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add new product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductSkuEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mProductImage.setOnTouchListener(mTouchListener);
        mContactNameEditText.setOnTouchListener(mTouchListener);
        mContactEmailEditText.setOnTouchListener(mTouchListener);
        mAddImage.setOnTouchListener(mTouchListener);
        mOrder.setOnTouchListener(mTouchListener);
        mAddStock.setOnTouchListener(mTouchListener);
        mSubtractStock.setOnTouchListener(mTouchListener);

        // Creating an intent to open the camera when the add image button is pressed
        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Invoke an implicit intent to open the photo gallery
                Intent openPhotoGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Finding the data
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                // Get a String of the pictureDirectoryPath
                String pictureDirectoryPath = pictureDirectory.getPath();

                // Get the Uri representation
                Uri data = Uri.parse(pictureDirectoryPath);

                // Set the data and type
                openPhotoGallery.setDataAndType(data, "image/*");

                // We will invoke this activity and get something back from it
                startActivityForResult(openPhotoGallery, IMAGE_GALLERY_REQUEST);
            }
        });

        //Open the email app to send a message with the data from the fields
        mOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Invoke an implicit intent to send an email
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

                String to = mContactEmailEditText.getText().toString();
                String productName = mProductNameEditText.getText().toString();
                String productSku = mProductSkuEditText.getText().toString();
                String subject = "New order for " + productName + " with SKU " + productSku;
                String supplier = mContactNameEditText.getText().toString();
                String message = "Dear " + supplier + ",\n" + "We would like to place an order of XX more of "
                        + productName + " with the SKU " + productSku + ".\n\n" + "Kind Regards,\n" + "Our company name";
                emailIntent.setData(Uri.parse("mailto:" + to));
                // Adding the subject line and message to the intent
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, message);

                try {
                    startActivity(emailIntent);
                    finish();
                    Log.i(LOG_TAG, "Order email sent to " + to);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(EditorActivity.this, "No email client found", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null)
            outState.putString(STATE_IMAGE_URI, mImageUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_IMAGE_URI) &&
                !savedInstanceState.getString(STATE_IMAGE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI));

            ViewTreeObserver viewTreeObserver = mProductImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mProductImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mProductImage.setImageBitmap(getBitmapFromUri(mImageUri, mContext, mProductImage));
                }
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Request was successful
        if (requestCode == IMAGE_GALLERY_REQUEST && (resultCode == RESULT_OK)) {
            try {
                // This is the address of the image on the sd cards
                mImageUri = data.getData();
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                imagePath = mImageUri.toString();
                // Declare a stream to read the data from the card
                InputStream inputStream;
                // We are getting an input stream based on the Uri of the image
                inputStream = getContentResolver().openInputStream(mImageUri);
                // Get a bitmap from the stream
                /*
      Bitmap value of the image from the Uri
     */
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                // Show the image to the user
                mProductImage.setImageBitmap(image);
                imagePath = mImageUri.toString();
                try {
                    getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                mProductImage.setImageBitmap(getBitmapFromUri(mImageUri, mContext, mProductImage));

            } catch (Exception e) {
                e.printStackTrace();
                //Show the user a Toast message that the image is not available
                Toast.makeText(EditorActivity.this, "Unable to open image", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }

    private Bitmap getBitmapFromUri(Uri uri, Context context, ImageView imageView) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

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

    private void saveProduct() {

        // Read data from the input fields
        String productNameString = mProductNameEditText.getText().toString().trim();
        String productSkuString = mProductSkuEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierNameString = mContactNameEditText.getText().toString().trim();
        String supplierEmailString = mContactEmailEditText.getText().toString().trim();


        if ((!TextUtils.isEmpty(productNameString)) && (!TextUtils.isEmpty(productSkuString)) && (!TextUtils.isEmpty(imagePath)) &&
                (!TextUtils.isEmpty(quantityString)) && (!TextUtils.isEmpty(priceString)) &&
                (!TextUtils.isEmpty(supplierNameString)) && (!TextUtils.isEmpty(supplierEmailString))) {

            // Exit activity only when all the fields have been filled
            finish();

        } else {
            // Check if this is supposed to be a new product
            // and check if all the fields in the editor are blank
            if (mCurrentProductUri == null ||
                    TextUtils.isEmpty(productNameString) || TextUtils.isEmpty(productSkuString) ||
                    TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(priceString) ||
                    TextUtils.isEmpty(supplierNameString) || TextUtils.isEmpty(supplierEmailString)) {
                // if any of the fields are empty le the user know with a Toast message
                Toast.makeText(getApplicationContext(), "Please fill in all the missing fields", Toast.LENGTH_LONG).show();
            }
        }
        //make sure the image uri is not null
        if (mImageUri == null) {
            return;
        }

        // Get the imagePath
        imagePath = mImageUri.toString();

        Log.v(LOG_TAG, "Product image string is: " + imagePath);

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(ProductEntry.COLUMN_PRODUCT_SKU, productSkuString);
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ProductEntry.COLUMN_QUANTITY, quantity);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_IMAGE, imagePath);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);


        // Determine if this is a new or existing product by checking if URI is null or not
        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();

            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_successful),
                        Toast.LENGTH_SHORT).show();

            }
        } else {
            // Otherwise this is an EXISTING product, so update it with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product is not changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
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

    @Override
    public void onBackPressed() {
        // If the product is not changed, continue with handling back button press
        if (!mProductHasChanged) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the products table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_SKU,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_IMAGE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,      // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Exit if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        ViewTreeObserver viewTreeObserver = mProductImage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mProductImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mProductImage.setImageBitmap(getBitmapFromUri(mImageUri, mContext, mProductImage));
            }
        });

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productSkuColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SKU);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);

            // Extract the value from the Cursor for the given column index
            final long productId = cursor.getLong(idColumnIndex);
            String productName = cursor.getString(productNameColumnIndex);
            int productSku = cursor.getInt(productSkuColumnIndex);
            final int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            final String image = cursor.getString(imageColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);

            // Update the views on the screen with the values from the database
            mProductNameEditText.setText(productName);
            mProductSkuEditText.setText(Integer.toString(productSku));
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            mContactNameEditText.setText(supplierName);
            mContactEmailEditText.setText(supplierEmail);
            mProductImage.setImageBitmap(getBitmapFromUri(Uri.parse(image), mContext, mProductImage));
            mImageUri = Uri.parse(image);

            mAddStock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quantity >= 0) {
                        int newQuantity = quantity + 1;
                        ContentValues values = new ContentValues();
                        values.put(ProductEntry.COLUMN_QUANTITY, newQuantity);
                        Uri productUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);
                        int numRowsUpdated = EditorActivity.this.getContentResolver().update(productUri, values, null, null);
                        if (!(numRowsUpdated > 0)) {
                            Log.e(LOG_TAG, EditorActivity.this.getString(R.string.editor_update_product_failed));
                        }
                    }
                    int newQuantity = 0;
                }
            });

            mSubtractStock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quantity >= 1) {
                        int newQuantity = quantity - 1;
                        ContentValues values = new ContentValues();
                        values.put(ProductEntry.COLUMN_QUANTITY, newQuantity);
                        Uri productUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);
                        int numRowsUpdated = EditorActivity.this.getContentResolver().update(productUri, values, null, null);
                        if (!(numRowsUpdated > 0)) {
                            Log.e(LOG_TAG, EditorActivity.this.getString(R.string.editor_update_product_failed));
                        }
                    } else if (!(quantity >= 1)) {
                        Toast.makeText(EditorActivity.this, getString(R.string.negative_stock), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mProductSkuEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mContactNameEditText.setText("");
        mContactEmailEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);


        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Method to delete the current product in the database
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}
