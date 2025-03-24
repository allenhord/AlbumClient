package com.example.albumclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private AlbumObserver albumObserver;
    private static final Uri CONTENT_URI = Uri.parse("content://com.demo.user.provider/users");

    ListView l;

//    int getItemPosition(View view) {
//        return (int) view.getTag(view.getId());
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register ContentObserver
        albumObserver = new AlbumObserver(new Handler());
        getContentResolver().registerContentObserver(CONTENT_URI, true, albumObserver);
    }

    // Define ContentObserver to listen for database changes
    class AlbumObserver extends ContentObserver {
        public AlbumObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Toast.makeText(getBaseContext(), "Database Updated!", Toast.LENGTH_SHORT).show();
            // Optionally refresh data
            onClickShowDetails(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (albumObserver != null) {
            getContentResolver().unregisterContentObserver(albumObserver);
        }
    }

    public void onClickAddDetails(View view) {

        // class to add values in the database
        ContentValues values = new ContentValues();

        // fetching text from user
        values.put("artist", ((EditText) findViewById(R.id.textName1)).getText().toString());
        values.put("name", ((EditText) findViewById(R.id.textName2)).getText().toString());

        // inserting into database through content URI
        getContentResolver().insert(Uri.parse("content://com.demo.user.provider/users"), values);

        // displaying a toast message
        Toast.makeText(getBaseContext(), "New Record Inserted", Toast.LENGTH_LONG).show();
    }

    public void onClickShowDetails(View view) {
        // creating a cursor object of the
        // content URI
        Cursor cursor = getContentResolver().query(Uri.parse("content://com.demo.user.provider/users"), null, null, null, null);

        // iteration of the cursor
        // to print whole table
        if (cursor.moveToFirst()) {
            ArrayList<String> albumRowsList = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                albumRowsList.add(cursor.getString(cursor.getColumnIndex("id"))
                        + "-" + cursor.getString(cursor.getColumnIndex("artist"))
                        + "-" + cursor.getString(cursor.getColumnIndex("name")));
                cursor.moveToNext();
            }
            l = findViewById(R.id.list);

            l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String rowText = albumRowsList.get(position);
                    String sub = rowText.substring(0, 1);

                    // Extract text between hyphens
                    String[] parts = rowText.split("-");
                    String extractedText1 = (parts.length >= 3) ? parts[1] : "";
                    String extractedText2 = (parts.length >= 3) ? parts[parts.length - 1] : "";

                    // inflate the layout of the popup window
                    LayoutInflater inflater = (LayoutInflater)
                            getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.popup_window, null);

                    // create the popup window
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = true; // lets taps outside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    // show the popup window
                    // which view you pass in doesn't matter, it is only used for the window tolken
                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                    EditText popText1 = popupView.findViewById(R.id.popName1);
                    popText1.setText(extractedText1, TextView.BufferType.EDITABLE);
                    EditText popText2 = popupView.findViewById(R.id.popName2);
                    popText2.setText(extractedText2, TextView.BufferType.EDITABLE);

                    // Find the button inside the popup and set click listener
                    Button closeButton = popupView.findViewById(R.id.btnDelete);
                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri contentUri = Uri.parse("content://com.demo.user.provider/users");
                            String selection = "id=?";
                            String[] selectionArgs = new String[]{sub};
                            getContentResolver().delete(contentUri, selection, selectionArgs);
                            popupWindow.dismiss();
                            Toast.makeText(getBaseContext(), "Record Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Find the submit button inside the popup and set click listener
                    Button submitButton = popupView.findViewById(R.id.btnSubmit);
                    submitButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri contentUri = Uri.parse("content://com.demo.user.provider/users");
                            ContentValues values = new ContentValues();
                            values.put("artist", popText1.getText().toString());
                            values.put("name", popText2.getText().toString());
                            String selection = "id=?";
                            String[] selectionArgs = new String[]{sub};
                            getContentResolver().update(contentUri, values, selection, selectionArgs);
                            popupWindow.dismiss();
                            Toast.makeText(getBaseContext(), "Record Updated", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // dismiss the popup window when touched
                    popupView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            popupWindow.dismiss();
                            return true;
                        }
                    });
                }
            });
            ArrayAdapter<String> arr;

            arr = new ArrayAdapter<String>(this,
                    R.layout.support_simple_spinner_dropdown_item, albumRowsList);
            l.setAdapter(arr);
        } else {
            // resultView.setText("No Records Found");
            Toast.makeText(getBaseContext(), "No Records Found", Toast.LENGTH_LONG).show();
        }
    }
}