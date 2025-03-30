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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.albumclient.model.Album;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AlbumObserver albumObserver;
    private static final Uri CONTENT_URI = Uri.parse("content://com.example.albummanager.provider/albums");
    private ListView listView;
    private Map<Integer, Album> positionToAlbumMap = new HashMap<>();

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
        try {
            // Get input values
            String artist = ((EditText) findViewById(R.id.textName1)).getText().toString();
            String name = ((EditText) findViewById(R.id.textName2)).getText().toString();

            // Validate input
            if (artist.isEmpty() || name.isEmpty()) {
                Toast.makeText(getBaseContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create new album
            Album newAlbum = new Album(artist, name);
            
            // Insert into database
            Uri result = getContentResolver().insert(CONTENT_URI, newAlbum.toContentValues());
            
            if (result != null) {
                Toast.makeText(getBaseContext(), "New Record Inserted", Toast.LENGTH_LONG).show();
                // Clear input fields
                ((EditText) findViewById(R.id.textName1)).setText("");
                ((EditText) findViewById(R.id.textName2)).setText("");
            } else {
                Toast.makeText(getBaseContext(), "Failed to insert record", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onClickShowDetails(View view) {
        try {
            // Query the database
            Cursor cursor = getContentResolver().query(CONTENT_URI, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                ArrayList<String> albumRowsList = new ArrayList<>();
                positionToAlbumMap.clear(); // Clear the previous mapping
                
                int listPosition = 0;
                while (!cursor.isAfterLast()) {
                    // Create Album object from cursor
                    Album album = Album.fromCursor(cursor);
                    positionToAlbumMap.put(listPosition, album);
                    
                    // Add display string to list
                    albumRowsList.add(album.getDisplayString());
                    cursor.moveToNext();
                    listPosition++;
                }
                cursor.close();

                listView.setOnItemClickListener((parent, view1, position, id) -> {
                    // Get the album from our mapping
                    Album album = positionToAlbumMap.get(position);
                    if (album == null) {
                        Toast.makeText(getBaseContext(), "Error: Could not find album", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showEditPopup(view1, album);
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
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showEditPopup(View view, Album album) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        EditText popText1 = popupView.findViewById(R.id.popName1);
        popText1.setText(album.getArtist(), TextView.BufferType.EDITABLE);
        EditText popText2 = popupView.findViewById(R.id.popName2);
        popText2.setText(album.getName(), TextView.BufferType.EDITABLE);

        // Delete button
        Button deleteButton = popupView.findViewById(R.id.btnDelete);
        deleteButton.setOnClickListener(v -> {
            try {
                Uri contentUri = CONTENT_URI;
                String selection = "id=?";
                String[] selectionArgs = new String[]{String.valueOf(album.getId())};
                int deleted = getContentResolver().delete(contentUri, selection, selectionArgs);
                
                if (deleted > 0) {
                    Toast.makeText(getBaseContext(), "Record Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Failed to delete record", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                popupWindow.dismiss();
            }
        });

        // Submit button
        Button submitButton = popupView.findViewById(R.id.btnSubmit);
        submitButton.setOnClickListener(v -> {
            try {
                // Update album with new values
                album.setArtist(popText1.getText().toString());
                album.setName(popText2.getText().toString());

                Uri contentUri = CONTENT_URI;
                String selection = "id=?";
                String[] selectionArgs = new String[]{String.valueOf(album.getId())};
                int updated = getContentResolver().update(contentUri, album.toContentValues(), selection, selectionArgs);
                
                if (updated > 0) {
                    Toast.makeText(getBaseContext(), "Record Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Failed to update record", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                popupWindow.dismiss();
            }
        });

        // Close button
        ImageButton btnClose = popupView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> popupWindow.dismiss());
    }
}