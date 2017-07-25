package com.android.inventorystatus;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.inventorystatus.data.ProductContract.ProductEntry;

class ProductCursorAdaptor extends CursorAdapter {

    // Declaring a log tag
    private static final String LOG_TAG = ProductCursorAdaptor.class.getSimpleName();

    // Making the ImageView global
    private ImageView saleImageView;

    // Declaring the constructor
    public ProductCursorAdaptor(Context context, Cursor cursor) {
        super(context, cursor, 0);
        Context mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Reference the child views for later
        ViewHolder holder;
        holder = new ViewHolder();

        // Find individual views that we want to modify in the list item layout
        holder.productNameTextView = (TextView) view.findViewById(R.id.product_name);
        holder.supplierNameTextView = (TextView) view.findViewById(R.id.product_supplier);
        holder.quantityTextView = (TextView) view.findViewById(R.id.quantiy);
        holder.priceTextView = (TextView) view.findViewById(R.id.price);
        holder.saleImageView = (ImageView) view.findViewById(R.id.sale_button);

        // Find the columns of the attributes that we need
        int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
        int priceNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);

        // Get the attributes from the Cursor for the current product
        final String productName = cursor.getString(productNameColumnIndex);
        final String supplierName = cursor.getString(supplierNameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int price = cursor.getInt(priceNameColumnIndex);
        final long recordId = cursor.getLong(idColumnIndex);
        final int newQuantity;

        // Update the TextViews with the attributes for the current product
        holder.productNameTextView.setText(productName);
        holder.supplierNameTextView.setText(supplierName);
        holder.quantityTextView.setText(Integer.toString(quantity));
        holder.priceTextView.setText(Integer.toString(price));

        holder.saleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (quantity >= 1) {
                    int newQuantity = quantity - 1;

                    // Update table with new stock of the product
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ProductEntry.COLUMN_QUANTITY, newQuantity);
                    Uri recordUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, recordId);

                    int numRowsUpdated = context.getContentResolver().update(recordUri, contentValues, null, null);
                    Log.i(LOG_TAG, "Number Rows Updated: " + numRowsUpdated);

                    if (!(numRowsUpdated > 0)) {
                        Log.e(LOG_TAG, context.getString(R.string.editor_update_product_failed));
                    }
                } else if (!(quantity >= 1)) {
                    int quantity = 0;
                    Toast.makeText(context, R.string.sold_out, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public class ViewHolder {

        TextView productNameTextView;
        TextView supplierNameTextView;
        TextView quantityTextView;
        TextView priceTextView;
        ImageView saleImageView;
    }
}


