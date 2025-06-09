package com.example.mp5jpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Review content cannot be blank.")
    @Lob
    private String content;

    @NotNull
    private LocalDate publicationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "rating_user_id", referencedColumnName = "user_id"),
            @JoinColumn(name = "rating_song_id", referencedColumnName = "song_id")
    })
    private Rating rating;

    public Review(String content, User user, Song song) {
        this.content = content;
        this.user = user;
        this.song = song;
        this.publicationDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "Review [id=" + id + ", user=" + (user != null ? user.getUsername() : "?") + ", song=" + (song != null ? song.getTitle() : "?") + "]";
    }
}