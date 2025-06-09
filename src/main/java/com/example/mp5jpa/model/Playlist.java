package com.example.mp5jpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Playlist name cannot be blank.")
    @Column(nullable = false)
    private String name;

    // Relacja: Jeden użytkownik (User) ma wiele playlist (Playlist)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relacja: Wiele playlist (Playlist) ma wiele utworów (Song)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "playlist_song", // Nazwa tabeli łączącej
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private Set<Song> songs = new HashSet<>();

    public Playlist(String name, User user) {
        this.name = name;
        this.user = user;
    }

    public void addSong(Song song) {
        this.songs.add(song);
        song.getPlaylists().add(this);
    }

    public void removeSong(Song song) {
        this.songs.remove(song);
        song.getPlaylists().remove(this);
    }

    @Override
    public String toString() {
        return "Playlist [id=" + id + ", name=" + name + ", user=" + (user != null ? user.getUsername() : "N/A") + "]";
    }
}