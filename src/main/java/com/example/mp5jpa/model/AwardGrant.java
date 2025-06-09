package com.example.mp5jpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AwardGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Year of the award grant cannot be null.")
    private Integer year;

    @NotBlank(message = "Category cannot be blank.")
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "award_id")
    @NotNull(message = "AwardGrant must be associated with an Award.")
    private Award award;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = true)
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = true)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = true)
    private Song song;

    public AwardGrant(Award award, int year, String category) {
        this.award = award;
        this.year = year;
        this.category = category;
    }

    @Override
    public String toString() {
        return "AwardGrant [id=" + id +
                ", year=" + year +
                ", category='" + category + '\'' +
                ", award=" + (award != null ? award.getName() : "N/A") +
                ", artist=" + (artist != null ? artist.getName() : "N/A") +
                ", album=" + (album != null ? album.getTitle() : "N/A") +
                ", song=" + (song != null ? song.getTitle() : "N/A") +
                ']';
    }
}