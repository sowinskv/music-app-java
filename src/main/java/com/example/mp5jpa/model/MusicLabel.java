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
public class MusicLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Label name cannot be blank.")
    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "musicLabel", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Album> albums = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "artist_label",
            joinColumns = @JoinColumn(name = "label_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Artist> artists = new HashSet<>();

    public MusicLabel(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MusicLabel [id=" + id + ", name=" + name + "]";
    }
}