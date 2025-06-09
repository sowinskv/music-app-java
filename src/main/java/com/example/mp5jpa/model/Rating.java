package com.example.mp5jpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Rating {

    @EmbeddedId
    private RatingId id = new RatingId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating must be at most 5.")
    @Column(name = "rating_value", nullable = false)
    private int ratingValue;

    @OneToOne(mappedBy = "rating", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Review review;

    public Rating(User user, Song song, int ratingValue) {
        this.user = user;
        this.song = song;
        this.ratingValue = ratingValue;
        this.id.setUserId(user.getId());
        this.id.setSongId(song.getId());
        if (user != null) user.getRatings().add(this);
        if (song != null) song.getRatings().add(this);
    }

    @Override
    public String toString() {
        return "Rating [user=" + (user != null ? user.getUsername() : "?") +
                ", song=" + (song != null ? song.getTitle() : "?") +
                ", ratingValue=" + ratingValue + "]";
    }
}