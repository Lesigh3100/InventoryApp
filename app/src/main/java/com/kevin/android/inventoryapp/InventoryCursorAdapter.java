package com.kevin.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kevin.android.inventoryapp.Data.InventoryContract;


public class InventoryCursorAdapter extends CursorAdapter {
    private Context mContext;

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContext = context;
    }

    public static class ItemViewHolder {
        public final TextView mName;
        public final TextView mPrice;
        public final TextView mQuantity;
        public final Button mSales;
        public final ImageView mImage;

        public ItemViewHolder(View view) {
            mName = (TextView) view.findViewById(R.id.list_name);
            mPrice = (TextView) view.findViewById(R.id.list_price);
            mQuantity = (TextView) view.findViewById(R.id.list_quantity);
            mSales = (Button) view.findViewById(R.id.list_sale_button);
            mImage = (ImageView) view.findViewById(R.id.list_image);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        view.setTag(itemViewHolder);
        view.isFocusable();
        return view;
    }

    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) view.getTag();
        final long id = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));
        final String name = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME));
        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_CURRENT_QUANTITY));
        final Double price = cursor.getDouble(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE));
        final String imageUri = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IMAGE));

        String IN_STOCK = mContext.getString(R.string.in_stock) + Integer.toString(quantity);
        String PRICE = mContext.getString(R.string.price) + Double.toString(price);
        final Uri uri = InventoryContract.InventoryEntry.getContentUri(id);
        itemViewHolder.mName.setText(name);
        itemViewHolder.mQuantity.setText(IN_STOCK);
        itemViewHolder.mPrice.setText(PRICE);
        itemViewHolder.mSales.setFocusable(false);
        itemViewHolder.mImage.setImageURI(Uri.parse(imageUri));
        itemViewHolder.mSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    ContentValues values = new ContentValues();
                    ContentResolver contentResolver = view.getContext().getContentResolver();
                    int updateQuantity = quantity - 1;
                    values.put(InventoryContract.InventoryEntry.COLUMN_CURRENT_QUANTITY, updateQuantity);
                    contentResolver.update(uri, values, null, null);
                    mContext.getContentResolver().notifyChange(uri, null);
                }
            }
        });

    }


}
