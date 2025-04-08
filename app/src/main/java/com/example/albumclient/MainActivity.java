package com.example.albumclient;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albumclient.model.Album;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextInputEditText artistInput;
    private TextInputEditText albumInput;
    private RecyclerView albumList;
    private AlbumAdapter adapter;
    private List<Album> albums;
    private ContentObserver contentObserver;
    private boolean isSelfChange = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        artistInput = findViewById(R.id.textName1);
        albumInput = findViewById(R.id.textName2);
        albumList = findViewById(R.id.list);
        FloatingActionButton fab = findViewById(R.id.fab);

        // Setup RecyclerView
        albums = new ArrayList<>();
        adapter = new AlbumAdapter(albums, this::showEditPopup);
        albumList.setLayoutManager(new LinearLayoutManager(this));
        albumList.setAdapter(adapter);

        // Setup FAB click listener
        fab.setOnClickListener(v -> onClickAddDetails(null));

        // Setup ContentObserver
        contentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (!isSelfChange) {
                    mainHandler.post(() -> {
                        Toast.makeText(MainActivity.this, "Data changed in AlbumManager", Toast.LENGTH_SHORT).show();
                        loadAlbums();
                    });
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register ContentObserver
        getContentResolver().registerContentObserver(
            Uri.parse("content://com.example.albummanager.provider/albums"),
            true,
            contentObserver
        );
        loadAlbums();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister ContentObserver
        getContentResolver().unregisterContentObserver(contentObserver);
    }

    public void onClickAddDetails(View view) {
        String artist = artistInput.getText().toString().trim();
        String name = albumInput.getText().toString().trim();

        if (artist.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please enter both artist and album names", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("artist", artist);
        values.put("name", name);

        try {
            isSelfChange = true;
            Uri uri = getContentResolver().insert(Uri.parse("content://com.example.albummanager.provider/albums"), values);
            if (uri != null) {
                Toast.makeText(this, R.string.album_added, Toast.LENGTH_SHORT).show();
                artistInput.setText("");
                albumInput.setText("");
                loadAlbums();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        } finally {
            isSelfChange = false;
        }
    }

    private void loadAlbums() {
        mainHandler.post(() -> {
            try {
                Cursor cursor = getContentResolver().query(
                    Uri.parse("content://com.example.albummanager.provider/albums"),
                    null, null, null, null);

                if (cursor != null) {
                    albums.clear();
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndex("id"));
                        String artist = cursor.getString(cursor.getColumnIndex("artist"));
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        albums.add(new Album(id, artist, name));
                    }
                    cursor.close();
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditPopup(Album album) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_album, null);
        TextInputEditText editArtist = dialogView.findViewById(R.id.editArtist);
        TextInputEditText editAlbum = dialogView.findViewById(R.id.editAlbum);

        editArtist.setText(album.getArtist());
        editAlbum.setText(album.getName());

        Dialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.edit_album)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            try {
                isSelfChange = true;
                Uri contentUri = Uri.parse("content://com.example.albummanager.provider/albums");
                String selection = "id=?";
                String[] selectionArgs = new String[]{String.valueOf(album.getId())};
                int deleted = getContentResolver().delete(contentUri, selection, selectionArgs);
                
                if (deleted > 0) {
                    Toast.makeText(this, R.string.album_deleted, Toast.LENGTH_SHORT).show();
                    loadAlbums();
                } else {
                    Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            } finally {
                isSelfChange = false;
            }
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String newArtist = editArtist.getText().toString().trim();
            String newName = editAlbum.getText().toString().trim();

            if (newArtist.isEmpty() || newName.isEmpty()) {
                Toast.makeText(this, "Please enter both artist and album names", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                isSelfChange = true;
                ContentValues values = new ContentValues();
                values.put("artist", newArtist);
                values.put("name", newName);

                Uri contentUri = Uri.parse("content://com.example.albummanager.provider/albums");
                String selection = "id=?";
                String[] selectionArgs = new String[]{String.valueOf(album.getId())};
                int updated = getContentResolver().update(contentUri, values, selection, selectionArgs);
                
                if (updated > 0) {
                    Toast.makeText(this, R.string.album_updated, Toast.LENGTH_SHORT).show();
                    loadAlbums();
                } else {
                    Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            } finally {
                isSelfChange = false;
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}