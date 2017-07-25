package com.android.inventorystatus.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ProductContract {

    // The content authority constant
    public static final String CONTENT_AUTHORITY = "com.android.inventorystatus";

    // Creating the base URI with the CONTENT_AUTHORITY constant
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    // Empty constructor so the class is not accidentally instantiated
    private ProductContract() {
    }

    public static final class ProductEntry implements BaseColumns {

        // The content URI to access the product data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // The MIME type for a list of multiple products
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // The MIMEtype for a single product
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // The name of the database table
        public final static String TABLE_NAME = "products";

        // A unique ID of type Integer for the products in the database
        public final static String _ID = BaseColumns._ID;

        // Product name of type TEXT in the database
        public final static String COLUMN_PRODUCT_NAME = "product_name";

        // Product SKU of type INTEGER in the database
        public final static String COLUMN_PRODUCT_SKU = "product_sku";

        // Product quantity of type INTEGER in the database
        public final static String COLUMN_QUANTITY = "quantity";

        // Product price of type INTEGER in the database
        public final static String COLUMN_PRICE = "price";

        // Product image of type TEXT in the database
        public final static String COLUMN_IMAGE = "image";

        // The suppliers name of type TEXT in the database
        public final static String COLUMN_SUPPLIER_NAME = "supplier_name";

        // The suppliers email of type TEXT in the database
        public final static String COLUMN_SUPPLIER_EMAIL = "supplier_email";
    }
}
