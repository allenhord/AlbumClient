package com.example.albumclient.model;

public class Album {
    private long id;
    private String artist;
    private String name;

    // Default constructor
    public Album() {
    }

    // Constructor with all fields
    public Album(long id, String artist, String name) {
        this.id = id;
        this.artist = artist;
        this.name = name;
    }

    // Constructor without id (for new albums)
    public Album(String artist, String name) {
        this.artist = artist;
        this.name = name;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Convert to ContentValues for database operations
    public android.content.ContentValues toContentValues() {
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("artist", artist);
        values.put("name", name);
        return values;
    }

    // Create Album from Cursor
    public static Album fromCursor(android.database.Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        Album album = new Album();
        album.setId(cursor.getLong(cursor.getColumnIndex("id")));
        album.setArtist(cursor.getString(cursor.getColumnIndex("artist")));
        album.setName(cursor.getString(cursor.getColumnIndex("name")));
        return album;
    }

    // Get display string for list view
    public String getDisplayString() {
        return artist + " - " + name;
    }

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", artist='" + artist + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return id == album.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
} 