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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AlbumObserver albumObserver;
    private static final Uri CONTENT_URI = Uri.parse("content://com.demo.album.provider/albums");
    private ListView listView;
    private Map<Integer, Long> positionToIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ListView
        listView = findViewById(R.id.list);

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
        getContentResolver().insert(CONTENT_URI, values);

        // displaying a toast message
        Toast.makeText(getBaseContext(), "New Record Inserted", Toast.LENGTH_LONG).show();
    }

    public void onClickShowDetails(View view) {
        // creating a cursor object of the content URI
        Cursor cursor = getContentResolver().query(CONTENT_URI, null, null, null, null);

        // iteration of the cursor to print whole table
        if (cursor != null && cursor.moveToFirst()) {
            ArrayList<String> albumRowsList = new ArrayList<>();
            positionToIdMap.clear(); // Clear the previous mapping
            
            int listPosition = 0;
            while (!cursor.isAfterLast()) {
                // Store the ID mapping
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                positionToIdMap.put(listPosition, id);
                
                albumRowsList.add(cursor.getString(cursor.getColumnIndex("artist"))
                        + " - " + cursor.getString(cursor.getColumnIndex("name")));
                cursor.moveToNext();
                listPosition++;
            }
            cursor.close();

            listView.setOnItemClickListener((parent, view1, position, id) -> {
                // Get the actual database ID from our mapping
                Long databaseId = positionToIdMap.get(position);
                if (databaseId == null) {
                    Toast.makeText(getBaseContext(), "Error: Could not find album ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the artist and name from the list
                String rowText = albumRowsList.get(position);
                String[] parts = rowText.split(" - ");
                String artist = parts[0];
                String name = parts[1];

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
                popupWindow.showAtLocation(view1, Gravity.CENTER, 0, 0);

                EditText popText1 = popupView.findViewById(R.id.popName1);
                popText1.setText(artist, TextView.BufferType.EDITABLE);
                EditText popText2 = popupView.findViewById(R.id.popName2);
                popText2.setText(name, TextView.BufferType.EDITABLE);

                // Find the button inside the popup and set click listener
                Button closeButton = popupView.findViewById(R.id.btnDelete);
                closeButton.setOnClickListener(v -> {
                    Uri contentUri = CONTENT_URI;
                    String selection = "id=?";
                    String[] selectionArgs = new String[]{String.valueOf(databaseId)};
                    getContentResolver().delete(contentUri, selection, selectionArgs);
                    popupWindow.dismiss();
                    Toast.makeText(getBaseContext(), "Record Deleted", Toast.LENGTH_SHORT).show();
                });

                // Find the submit button inside the popup and set click listener
                Button submitButton = popupView.findViewById(R.id.btnSubmit);
                submitButton.setOnClickListener(v -> {
                    Uri contentUri = CONTENT_URI;
                    ContentValues values = new ContentValues();
                    values.put("artist", popText1.getText().toString());
                    values.put("name", popText2.getText().toString());
                    String selection = "id=?";
                    String[] selectionArgs = new String[]{String.valueOf(databaseId)};
                    getContentResolver().update(contentUri, values, selection, selectionArgs);
                    popupWindow.dismiss();
                    Toast.makeText(getBaseContext(), "Record Updated", Toast.LENGTH_SHORT).show();
                });

                // close button to dismiss the popup window
                ImageButton btnClose = popupView.findViewById(R.id.btnClose);
                btnClose.setOnClickListener(v -> popupWindow.dismiss());
            });

            ArrayAdapter<String> arr = new ArrayAdapter<>(this,
                    R.layout.support_simple_spinner_dropdown_item, albumRowsList);
            listView.setAdapter(arr);
        } else {
            Toast.makeText(getBaseContext(), "No Records Found", Toast.LENGTH_LONG).show();
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}