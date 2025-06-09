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
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Genre name cannot be blank.")
    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "genre", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Song> songs = new HashSet<>();

    @ManyToMany(mappedBy = "genres")
    private Set<Artist> artists = new HashSet<>();


    public Genre(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Genre [id=" + id + ", name=" + name + "]";
    }
}