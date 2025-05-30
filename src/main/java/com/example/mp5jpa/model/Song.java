package com.example.mp5jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Song extends AbstractMediaItem {

    private int duration; // in seconds
    private String genre;
    private String audioUrl;
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    public Song(String title, Artist artist, int duration, String genre, String audioUrl, String coverImageUrl) {
        super(title);
        this.artist = artist;
        this.duration = duration;
        this.genre = genre;
        this.audioUrl = audioUrl;
        this.coverImageUrl = coverImageUrl;
    }

    @Override
    public String toString() {
        return "Song [id=" + getId() + ", title=" + getTitle() +
                ", artist=" + (artist != null ? artist.getName() : "N/A") +
                ", album=" + (album != null ? album.getTitle() : "N/A") + "]";
    }
}