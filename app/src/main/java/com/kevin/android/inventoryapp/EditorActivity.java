package com.kevin.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.android.inventoryapp.Data.InventoryContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private Button mPlusButton;
    private Button mMinusButton;
    private Button mSaleButton;
    private Button mShipmentButton;
    private Button mDeleteButton;
    private Button mSaveButton;
    private TextView mShipmentIncrement;
    private ImageView mImageView;
    private Button mOrderButton;
    int orderIncrement = 0;
    private Uri mCurrentItemUri;
    private Uri imageUri;
    private ContentResolver mContentResolver;
    private Boolean imageSet = false;
    private boolean mItemChanged = false;
    private final int EDIT_LOADER = 3;
    private int quantityUpdate = 0;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int CHOOSE_IMAGE_REQUEST = 2;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        createButtons();
        mContentResolver = this.getContentResolver();
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.new_product));
            mImageView.setImageResource(R.drawable.add);
        } else {
            setTitle(getString(R.string.edit_product));
            getLoaderManager().initLoader(EDIT_LOADER, null, this);
        }
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.add_image_button:
                        openImage();
                        break;
                    case R.id.plus_button:
                        incrementButtons(mPlusButton);
                        break;
                    case R.id.minus_button:
                        incrementButtons(mMinusButton);
                        break;
                    case R.id.edit_sale_button:
                        if (quantityUpdate > 0) {
                            quantityUpdate--;
                            if (mCurrentItemUri == null) {
                                mQuantityTextView.setText(Integer.toString(quantityUpdate));
                            } else {
                                ContentValues values = new ContentValues();
                                ContentResolver contentResolver = view.getContext().getContentResolver();

                                values.put(InventoryContract.InventoryEntry.COLUMN_CURRENT_QUANTITY, quantityUpdate);
                                contentResolver.update(mCurrentItemUri, values, null, null);
                                getContentResolver().notifyChange(mCurrentItemUri, null);
                            }
                        }
                        break;
                    case R.id.receive_shipment_button:
                        if (mCurrentItemUri != null) {
                            ContentValues values = new ContentValues();
                            ContentResolver contentResolver = view.getContext().getContentResolver();
                            int updateQuantity = Integer.parseInt(mQuantityTextView.getText().toString()) + orderIncrement;
                            values.put(InventoryContract.InventoryEntry.COLUMN_CURRENT_QUANTITY, updateQuantity);
                            contentResolver.update(mCurrentItemUri, values, null, null);
                        } else {
                            int updateQuantity = Integer.parseInt(mQuantityTextView.getText().toString()) + orderIncrement;
                            mQuantityTextView.setText(Integer.toString(updateQuantity));
                        }
                        break;
                    case R.id.make_order_button:
                        if (validateFields()) {
                            Intent intent1 = new Intent(Intent.ACTION_SENDTO);
                            intent1.setData(Uri.parse("mailto: LargeSupplyChain@NotTheMafia.org"));
                            intent1.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.need_more) + mNameEditText.getText().toString().trim());
                            intent1.putExtra(Intent.EXTRA_TEXT, getString(R.string.order_more) + mNameEditText.getText().toString().trim() + getString(R.string.please));
                            startActivity(Intent.createChooser(intent1, "Send mail..."));
                        }
                        break;
                    case R.id.save_button:
                        if (validateFields()) {

                            ContentValues values = new ContentValues();

                            int quantity = Integer.parseInt(mQuantityTextView.getText().toString().trim());
                            String name = mNameEditText.getText().toString().trim();
                            String description = mDescriptionEditText.getText().toString().trim();
                            Double price = Double.parseDouble(mPriceEditText.getText().toString().trim());
                            String image = imageUri.toString();

                            values.put(InventoryContract.InventoryEntry.COLUMN_CURRENT_QUANTITY, quantity);
                            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, name);
                            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, description);
                            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, price);
                            values.put(InventoryContract.InventoryEntry.COLUMN_IMAGE, image);
                            if (mCurrentItemUri == null) {

                                Uri uri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
                                if (uri != null) {
                                    Toast.makeText(EditorActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(EditorActivity.this, R.string.failure, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                getContentResolver().update(mCurrentItemUri, values, null, null);
                                Toast.makeText(EditorActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        } else {
                            Toast.makeText(EditorActivity.this, R.string.edit_error, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.delete_button:
                        deleteDialog();
                        break;
                }
            }
        };
        mPlusButton.setOnClickListener(onClickListener);
        mMinusButton.setOnClickListener(onClickListener);
        mSaleButton.setOnClickListener(onClickListener);
        mShipmentButton.setOnClickListener(onClickListener);
        mDeleteButton.setOnClickListener(onClickListener);
        mSaveButton.setOnClickListener(onClickListener);
        mImageView.setOnClickListener(onClickListener);
        mOrderButton.setOnClickListener(onClickListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mShipmentButton.setOnTouchListener(mTouchListener);
        mSaleButton.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
    }

    private boolean validateFields() {
        if (mDescriptionEditText.getText().toString().length() != 0 && mNameEditText.getText().toString().length() != 0 && mPriceEditText.getText().toString().length() != 0 && mQuantityTextView.getText().toString().length() != 0
                && imageSet) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!mItemChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_and_quit);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.continue_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mCurrentItemUri != null) {
            builder.setMessage(R.string.delete_product_message);
        } else {
            builder.setMessage(R.string.delete_progress);
        }
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    if (mCurrentItemUri != null) {
                        ContentResolver contentResolver = getContentResolver();
                        contentResolver.delete(mCurrentItemUri, null, null);
                        contentResolver.notifyChange(mCurrentItemUri, null);
                        finish();
                    } else {
                        mNameEditText.setText(R.string.blank);
                        mPriceEditText.setText(R.string.blank);
                        mDescriptionEditText.setText(R.string.blank);
                        mQuantityTextView.setText(R.string.blank);
                        mImageView.setImageResource(R.drawable.add);
                    }
                    dialogInterface.dismiss();
                }
            }
        });
        builder.setNegativeButton(R.string.continue_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                this,
                mCurrentItemUri,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_CURRENT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
            int imageColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IMAGE);


            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            String imageUriString = cursor.getString(imageColumnIndex);
            Log.v("URI from DB: ", imageUriString);
            final Uri setImageUri = Uri.parse(imageUriString);
            imageUri = setImageUri;

            mNameEditText.setText(name);
            mQuantityTextView.setText(Integer.toString(quantity));
            mPriceEditText.setText(Double.toString(price));
            mDescriptionEditText.setText(description);
            quantityUpdate = quantity;
            mImageView.setImageURI(setImageUri);
            imageSet = true;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.reset();
    }

    private void checkWritePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void openImage() {
        checkWritePermission();
        Intent intent;
        if (Build.VERSION.SDK_INT >= 19) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImage();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getData() != null) {
                imageUri = data.getData();
                mImageView.setImageURI(imageUri);
                mImageView.invalidate();
                imageSet = true;
            }
        }
    }

    private void createButtons() {
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityTextView = (TextView) findViewById(R.id.product_quantity);
        mPlusButton = (Button) findViewById(R.id.plus_button);
        mMinusButton = (Button) findViewById(R.id.minus_button);
        mSaleButton = (Button) findViewById(R.id.edit_sale_button);
        mShipmentButton = (Button) findViewById(R.id.receive_shipment_button);
        mDeleteButton = (Button) findViewById(R.id.delete_button);
        mShipmentIncrement = (TextView) findViewById(R.id.receive_shipment_increment);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mImageView = (ImageView) findViewById(R.id.add_image_button);
        mOrderButton = (Button) findViewById(R.id.make_order_button);
    }

    private void incrementButtons(View view) {
        switch (view.getId()) {
            case R.id.plus_button:
                orderIncrement++;
                mShipmentIncrement.setText(Integer.toString(orderIncrement));
                break;
            case R.id.minus_button:
                if (orderIncrement > 0) {
                    orderIncrement--;
                    mShipmentIncrement.setText(Integer.toString(orderIncrement));
                    break;
                }
        }
    }
}
