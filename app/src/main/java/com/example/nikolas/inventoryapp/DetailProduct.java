package com.example.nikolas.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nikolas.inventoryapp.dataBase.ProductContract.ProductEntry;

public class DetailProduct extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 1;
    private Uri mCurrentUri;
    private Button mAddBtn;
    private Button mMinusBtn;
    private Button mSaleBtn;
    private Button mOrderBtn;
    private Button mDeleteBtn;
    private EditText mOrderValue;
    private EditText mQuantityValue;
    private ImageView mProductImage;
    private String mName;
    private int mQuantity;
    private byte[] mImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        getLoaderManager().initLoader(LOADER_ID, null, this);

        Log.v("uri", mCurrentUri.toString());
        // Take buttons
        mAddBtn = (Button) findViewById(R.id.plus_btn);
        mMinusBtn = (Button) findViewById(R.id.minus_btn);
        mSaleBtn = (Button) findViewById(R.id.sale_btn);
        mOrderBtn = (Button) findViewById(R.id.order_btn);
        mDeleteBtn = (Button) findViewById(R.id.delete_btn);
        mProductImage = (ImageView) findViewById(R.id.product_photo);

        // Take Edit texts
        mOrderValue = (EditText) findViewById(R.id.order_value);
        mQuantityValue = (EditText) findViewById(R.id.quantity_value);

        // Add button listener
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quant = mQuantityValue.getText().toString().trim();
                if (TextUtils.isEmpty(quant)) {
                    mQuantity++;
                    saveProduct();
                    return;
                }
                mQuantity += Integer.parseInt(quant);
                saveProduct();
                mQuantityValue.setText("");
                stopKeyboardFocus();
            }
        });

        // Minus button listener
        mMinusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quant = mQuantityValue.getText().toString().trim();
                if (mQuantity > 0) {
                    if (TextUtils.isEmpty(quant)) { // Default value cause no input
                        mQuantity--;
                        saveProduct();
                        return;
                    }

                    mQuantity -= Integer.parseInt(mQuantityValue.getText().toString().trim());
                    if (mQuantity < 0)
                        mQuantity = 0;

                    saveProduct();
                    mQuantityValue.setText("");
                    stopKeyboardFocus();
                }
            }
        });

        // Sale button listener
        mSaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantity > 0) {
                    mQuantity--;
                    saveProduct();
                }
            }
        });

        // Order button listener
        mOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = mOrderValue.getText().toString().trim();
                sendEmail(amount);
                stopKeyboardFocus();
            }
        });

        // Delete button listener
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteConfirm();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this,
                mCurrentUri,
                projection,
                null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int quantityIndexColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int nameProductIndexColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int imageIndexColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            mQuantity = cursor.getInt(quantityIndexColumn);
            mName = cursor.getString(nameProductIndexColumn);
            mImage = cursor.getBlob(imageIndexColumn);

            // Display image
            mProductImage.setImageBitmap(BitmapFactory.decodeByteArray(mImage, 0, mImage.length));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductImage.setImageBitmap(null);
    }

    /**
     * Save the current product
     */
    private void saveProduct() {
        int rowUpdated;
        ContentValues values = new ContentValues();

        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, mQuantity);
        rowUpdated = getContentResolver().update(mCurrentUri, values, null, null);

        if (rowUpdated == 0)
            Toast.makeText(this, R.string.update_err, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, getString(R.string.update_ok) + String.valueOf(mQuantity), Toast.LENGTH_SHORT).show();
    }


    /**
     * Delete dialog
     */
    private void deleteConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_this_product);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Delete the product from database
     */
    private void deleteProduct() {
        if (mCurrentUri != null) {
            int rowDeleted = getContentResolver().delete(mCurrentUri, null, null);

            if (rowDeleted == 0)
                Toast.makeText(this, R.string.delete_problem, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
        }

        // Return to home activity
        finish();
    }

    /**
     * Send e-mail to order supplies
     *
     * @param amount The quantity that user needs.
     */
    private void sendEmail(String amount) {
        if (TextUtils.isEmpty(amount)) {
            Toast.makeText(this, R.string.amount_order, Toast.LENGTH_SHORT).show();
            return;
        }

        String text = "Stock of " + mName + "\nQuantity: "
                + mQuantity + ".\nI need "
                + amount + " as soon as possible!";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"supplier@kappakeepo.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "I need supplies for : " + mName);
        intent.putExtra(Intent.EXTRA_TEXT, text);

        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.no_email_client, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stop keyboard focus
     */
    private void stopKeyboardFocus() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
