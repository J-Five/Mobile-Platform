package com.seojunkyo.soma.controlhome.databases;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.seojunkyo.soma.controlhome.R;

/**
 * Created by seojunkyo on 15. 10. 27..
 */
public class CustomCursorAdapter extends CursorAdapter{
    public CustomCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.address_listitem, parent, false);

        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // here we are setting our data
        // that means, take the data from the cursor and put it in views

        TextView textViewPersonName = (TextView) view.findViewById(R.id.listitem_address);
        textViewPersonName.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1))));

        /*TextView textViewPersonPIN = (TextView) view.findViewById(R.id.tv_person_pin);
        textViewPersonPIN.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2))));*/
    }
}
