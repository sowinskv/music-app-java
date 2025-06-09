package com.example.mp5jpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Artist name cannot be blank.")
    @Size(min = 1, max = 100, message = "Artist name must be between 1 and 100 characters.")
    @Column(nullable = false, unique = true)
    private String name;

    @Min(value = 0, message = "Age must be non-negative.")
    @Max(value = 150, message = "Age seems too high.")
    private Integer age;

    @OneToMany(mappedBy = "artist", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Album> albums = new HashSet<>();

    @OneToMany(mappedBy = "artist", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Song> songs = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "artist_genre",
            joinColumns = @JoinColumn(name = "artist_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany(mappedBy = "artists")
    private Set<MusicLabel> musicLabels = new HashSet<>();

    public Artist(String name) {
        this.name = name;
    }

    public Artist(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Artist [id=" + id + ", name=" + name + ", age=" + (age != null ? age : "N/A") + "]";
    }
}