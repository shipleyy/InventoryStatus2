package com.android.inventorystatus.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.android.inventorystatus.R;
import com.android.inventorystatus.data.ProductContract.ProductEntry;

public class ProductProvider extends ContentProvider {

    // Tag for the log messages
    private static final String LOG_TAG = ProductProvider.class.getSimpleName();

    // Constants for the URI matcher
    private static final int PRODUCTS = 100;
    private static final int PRODUCTS_ID = 101;

    // URI matcher to match the URI to one of the codes above
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {

        // Check which type of URI is received
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);

        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCTS_ID);

    }

    // Database helper object
    private ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor holds the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCTS_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the products table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Problem querying the following URI" + uri);
        }

        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


    private Uri insertProduct(Uri uri, ContentValues values) {

        if (values == null) {
            Toast.makeText(getContext(), "Product name cannot be empty", Toast.LENGTH_LONG).show();
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        // Check that the name is not null
        String productName = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (productName == null) {
            Toast.makeText(getContext(), "Product requires a name", Toast.LENGTH_LONG).show();
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that the product SKU is not null
        Integer productSku = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_SKU);
        if (productSku == null && productSku < 0) {
            Toast.makeText(getContext(), "Product requires an SKU", Toast.LENGTH_LONG).show();
            throw new IllegalArgumentException("Product requires an SKU");
        }

        // Check that the quantity is not null
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
        if (quantity == null && quantity < 0) {
            Toast.makeText(getContext(), "Product requires a quantity", Toast.LENGTH_LONG).show();
            throw new IllegalArgumentException("Product requires a quantity");
        }

        // If the price is provided, check that it's greater than or equal to 0
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            Toast.makeText(getContext(), "Product requires a valid price", Toast.LENGTH_LONG).show();
            throw new IllegalArgumentException("Product requires valid price");
        }

        // Check that the product image is not null
        String productImage = values.getAsString(ProductEntry.COLUMN_IMAGE);
        if (productImage == null) {
            Toast.makeText(getContext(), "Product requires an image", Toast.LENGTH_LONG).show();
            throw new IllegalArgumentException("Product requires an image");
        }

        // Check that the contact supplier name is not null
        String supplierName = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            Toast.makeText(getContext(), "Product requires a supplier name", Toast.LENGTH_LONG).show();
            throw new IllegalArgumentException("Product requires a supplier name");
        }

        // Check that the contact supplier email is not null
        String supplierEmail = values.getAsString(ProductEntry.COLUMN_SUPPLIER_EMAIL);
        if (supplierEmail == null) {
            Toast.makeText(getContext(), "Product requires a supplier contact email", Toast.LENGTH_LONG).show();
            throw new IllegalArgumentException("Product requires a supplier contact email");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the received values
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the given URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCTS_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        //if there are no changes
        if (contentValues == null) {
            // Check that the product name is not null
            if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
                Toast.makeText(getContext(), R.string.field_required, Toast.LENGTH_SHORT).show();
                throw new IllegalArgumentException("Products require a name");

            }  // Check that the SKU is not null
            if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_SKU)) {
                throw new IllegalArgumentException("Product requires an SKU");
            }
            // Check that the quantity is not null
            Integer quantity = contentValues.getAsInteger(ProductEntry.COLUMN_QUANTITY);
            if (quantity == null && quantity < 0) {
                throw new IllegalArgumentException("Product requires a quantity");
            }
            // If the price is provided, check that it's greater than or equal to 0
            Integer price = contentValues.getAsInteger(ProductEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
            //Check that the product image is not null
            if (contentValues.containsKey(ProductEntry.COLUMN_IMAGE)) {
                throw new IllegalArgumentException("Product requires an image");
            }
            // Check that the supplier name is not null
            if (contentValues.containsKey(ProductEntry.COLUMN_SUPPLIER_NAME)) {
                throw new IllegalArgumentException("Product requires a supplier name");
            }
            // Check that the product contact supplier emal is not null
            if (contentValues.containsKey(ProductEntry.COLUMN_SUPPLIER_EMAIL)) {
                throw new IllegalArgumentException("Product requires a supplier email");
            }

            // If there are no values to update, then don't try to update the database
            if (contentValues.size() == 0) {
                return 0;
            }
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCTS_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCTS_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}