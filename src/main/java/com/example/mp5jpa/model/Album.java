package com.example.mp5jpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Album extends AbstractMediaItem {

    @PastOrPresent(message = "Release date cannot be in the future.")
    private LocalDate releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musiclabel_id")
    private MusicLabel musicLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    @NotNull(message = "Album must have an artist.")
    private Artist artist;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Song> songs = new ArrayList<>();

    public Album(String albumName, Artist artist, LocalDate releaseDate) {
        super(albumName);
        this.artist = artist;
        this.releaseDate = releaseDate;
    }

    public void addSong(Song song) {
        if (song != null && !this.songs.contains(song)) {
            if (song.getAlbum() != null) {
                song.getAlbum().removeSong(song);
            }
            this.songs.add(song);
            song.setAlbum(this);

            if(song.getArtist() == null) {
                song.setArtist(this.artist);
            } else if (!song.getArtist().equals(this.artist)) {
                throw new IllegalArgumentException("Song artist must match album artist.");
            }
        }
    }

    public void removeSong(Song song) {
        if (song != null && this.songs.contains(song)) {
            this.songs.remove(song);
            song.setAlbum(null);
        }
    }

    @Override
    public String toString() {
        return "Album [id=" + getId() + ", name=" + getTitle() + ", artist=" + (artist != null ? artist.getName() : "N/A") + "]";
    }
}