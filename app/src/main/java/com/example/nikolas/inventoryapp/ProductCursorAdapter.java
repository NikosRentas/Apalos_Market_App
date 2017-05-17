package com.example.nikolas.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nikolas.inventoryapp.dataBase.ProductContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {
    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_quantity);
        TextView productNameTextView = (TextView) view.findViewById(R.id.list_item_name);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.product_detail);

        int rowNumber = cursor.getPosition();

        if (rowNumber % 2 == 0)
            linearLayout.setBackgroundResource(R.color.rowOne);
        else
            linearLayout.setBackgroundResource(R.color.rowTwo);

        // Get number of each column
        int priceColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int productNameColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);

        double price = cursor.getDouble(priceColumn);
        int quantity = cursor.getInt(quantityColumn);
        String productName = cursor.getString(productNameColumn);

        priceTextView.setText(String.valueOf(price));
        quantityTextView.setText(String.format("Quantity: %s", String.valueOf(quantity)));
        productNameTextView.setText(productName);
    }
}
