package com.example.albumclient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albumclient.model.Album;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private final List<Album> albums;
    private final OnAlbumClickListener listener;

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }

    public AlbumAdapter(List<Album> albums, OnAlbumClickListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.artistText.setText(album.getArtist());
        holder.albumText.setText(album.getName());
        holder.cardView.setOnClickListener(v -> listener.onAlbumClick(album));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final TextView artistText;
        final TextView albumText;

        AlbumViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            artistText = itemView.findViewById(R.id.artistText);
            albumText = itemView.findViewById(R.id.albumText);
        }
    }
} 