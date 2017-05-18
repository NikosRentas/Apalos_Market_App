package com.example.nikolas.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nikolas.inventoryapp.dataBase.ProductContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = ProductCursorAdapter.class.getName();
    private ContentResolver res;

    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final Button btn = (Button) view.findViewById(R.id.list_item_sale_btn);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_quantity);
        TextView productNameTextView = (TextView) view.findViewById(R.id.list_item_name);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.product_detail);

        int rowNumber = cursor.getPosition();
        if (rowNumber % 2 == 0) {
            linearLayout.setBackgroundResource(R.color.even);
        } else {
            linearLayout.setBackgroundResource(R.color.odd);
        }


        // Get number of each column
        int idIndexColumn = cursor.getColumnIndex(ProductEntry._ID);
        int priceColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int productNameColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);

        final int id = cursor.getInt(idIndexColumn);
        final int quantity = cursor.getInt(quantityColumn);
        double price = cursor.getDouble(priceColumn);
        final String productName = cursor.getString(productNameColumn);

        priceTextView.setText(String.valueOf(price));
        quantityTextView.setText(String.format("Quantity: %s", String.valueOf(quantity)));
        productNameTextView.setText(productName);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                res = v.getContext().getContentResolver();
                saveProduct(id, quantity);
            }
        });
    }

    /**
     * Save the current product
     */
    private void saveProduct(int id, int mQuantity) {
        int rowUpdated;

        mQuantity--;
        if (mQuantity < 0)
            mQuantity = 0;
        ContentValues values = new ContentValues();
        values.put(ProductEntry._ID, id);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, mQuantity);

        rowUpdated = res.update(ProductEntry.CONTENT_URI, values, "_id=" + id, null);

        if (rowUpdated == 0)
            Log.e(LOG_TAG, "Error with product update");
        else
            Log.i(LOG_TAG, "Product updated!");

    }
}
