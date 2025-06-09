package com.example.mp5jpa.service;

import com.example.mp5jpa.model.*;
import com.example.mp5jpa.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (artistRepository.count() == 0) {
            System.out.println("Database is empty, seeding data...");

            Artist theHellp = new Artist("The Hellp");
            Artist enya = new Artist("Enya & Bobby Shmurda");
            Artist skepta = new Artist("Skepta");
            Artist travis = new Artist("Travis Scott");
            artistRepository.saveAll(List.of(theHellp, enya, skepta, travis));

            Album thAlbum = new Album("TH", theHellp, LocalDate.parse("2023-10-01"));
            Genre popGenre = new Genre("Pop");
            Song tuTuNeurotic = new Song("Tu Tu Neurotic", theHellp, 180, popGenre, "/audio/tutuneurotic.wav", "/images/tutu.jpg");
            thAlbum.addSong(tuTuNeurotic);

            Album hotTimeAlbum = new Album("Hot Times", enya, LocalDate.parse("2024-01-15"));
            Genre blendGenre = new Genre("Blend");
            Song onlyTime = new Song("Only Hot Time", enya, 194, blendGenre, "/audio/onlyhottime.wav", "/images/onlyhottime.jpg");
            hotTimeAlbum.addSong(onlyTime);

            albumRepository.saveAll(List.of(thAlbum, hotTimeAlbum));

            Genre grimeGenre = new Genre("Grime");
            Song smoothJazz = new Song("Smooth Jazz", skepta, 210, grimeGenre, "/audio/skeptajazz.wav", "/images/jazz.jpg");
            Genre rapGenre = new Genre("Rap");
            Song sdp = new Song("SDP", travis, 300, rapGenre, "/audio/sdpeyes.wav", "/images/sdp.jpg");
            songRepository.saveAll(List.of(smoothJazz, sdp));

            User user1 = new User("MusicFan123");
            User user2 = new User("CriticGuy");
            userRepository.saveAll(List.of(user1, user2));

            Rating rating1 = new Rating(user1, tuTuNeurotic, 5);
            Rating rating2 = new Rating(user2, tuTuNeurotic, 4);
            Rating rating3 = new Rating(user1, sdp, 5);


            System.out.println("Data seeding complete.");
        } else {
            System.out.println("Database already contains data, skipping seeding.");
        }

        System.out.println("\n--- Fetching Album WITHOUT Songs (Check SQL - should be 1 query for album) ---");
        Album testAlbum = albumRepository.findAllWithoutSongs().stream().findFirst().orElse(null); // Line 73
        if (testAlbum != null) {
            System.out.println("Fetched Album: " + testAlbum.getTitle());
        }

        System.out.println("\n--- Fetching Album WITH Songs (Check SQL - should be 1 query with JOIN) ---");
        if (testAlbum != null) {
            Album fetchedAlbum = albumRepository.findByIdWithSongs(testAlbum.getId()).orElse(null);
            if (fetchedAlbum != null) {
                System.out.println("Fetched Album: " + fetchedAlbum.getTitle());
                System.out.println("Songs: " + fetchedAlbum.getSongs().size());
                fetchedAlbum.getSongs().forEach(s -> System.out.println("  - " + s.getTitle()));
            }
        }
    }
}