package com.kevin.android.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.kevin.android.inventoryapp.Data.InventoryContract;
import com.kevin.android.inventoryapp.Data.InventoryDbHelper;

import static com.kevin.android.inventoryapp.Data.InventoryContract.CONTENT_AUTHORITY;
import static com.kevin.android.inventoryapp.Data.InventoryContract.PATH_INVENTORY;

public class InventoryProvider extends ContentProvider {
    private InventoryDbHelper mDbHelper;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;

    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.illegal_argument));
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(getContext().getString(R.string.unkown_uri) + uri + getContext().getString(R.string.with_match) + match);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateItem(uri, values, selection, selectionArgs);

            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.update_failed));
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        if (values.size() == 0) {
            return 0;
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {

            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION)) {
            String description = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
            if (description == null) {
                throw new IllegalArgumentException(getContext().getString(R.string.illegal_argument));
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Double price = values.getAsDouble(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.illegal_argument));
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_CURRENT_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_CURRENT_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.illegal_argument));
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_IMAGE)) {
            String image = values.getAsString(InventoryContract.InventoryEntry.COLUMN_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException(getContext().getString(R.string.illegal_argument));
            }
        }
        int rows = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertItem(uri, values);

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.illegal_argument));
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        String description = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
        Double price = values.getAsDouble(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_CURRENT_QUANTITY);
        String image = values.getAsString(InventoryContract.InventoryEntry.COLUMN_IMAGE);

        if (name == null || description == null || price == null || price < 0 || quantity == null || quantity < 0 || image == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.illegal_argument));
        }
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Toast.makeText(getContext(), R.string.insert_failed, Toast.LENGTH_SHORT).show();
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.deletion_failure) + uri);
        }
    }
}


