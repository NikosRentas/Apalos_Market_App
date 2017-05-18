package com.example.nikolas.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nikolas.inventoryapp.dataBase.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;

public class EditProduct extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private int mNoPhoto = -1;
    private EditText mProductText;
    private EditText mQuantityText;
    private EditText mPriceText;
    private Button mPhotoBtn;
    private byte[] byteArray;
    private Bitmap imageBitmap;
    private boolean mProductHasChanged = false;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mProductText = (EditText) findViewById(R.id.name_edit_view);
        mQuantityText = (EditText) findViewById(R.id.quantity_edit_view);
        mPriceText = (EditText) findViewById(R.id.price_edit_view);
        mPhotoBtn = (Button) findViewById(R.id.photo_btn);

        mPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        mProductText.setOnTouchListener(mTouchListener);
        mQuantityText.setOnTouchListener(mTouchListener);
        mPriceText.setOnTouchListener(mTouchListener);
        mPhotoBtn.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_button:
                addProduct();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditProduct.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditProduct.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");

            // Convert image to array of bytes
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArray = stream.toByteArray();

            mNoPhoto = 0; // Photo exits
        }
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void addProduct() {
        String nameString = mProductText.getText().toString().trim();
        String quantityString = mQuantityText.getText().toString().trim();
        String priceString = mPriceText.getText().toString().trim();

        // Check for empty entry values
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(quantityString)
                || TextUtils.isEmpty(priceString)
                || mNoPhoto == -1) {
            Toast.makeText(this, R.string.no_values, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, Integer.parseInt(quantityString));
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, Double.parseDouble(priceString));
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, byteArray);

        Uri newURI = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        if (newURI == null)
            Toast.makeText(this, R.string.save_err, Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.change_quit);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}

