package com.example.mp5jpa.service;

import com.example.mp5jpa.model.*;
import com.example.mp5jpa.repository.ArtistRepository;
import com.example.mp5jpa.repository.RatingRepository;
import com.example.mp5jpa.repository.SongRepository;
import com.example.mp5jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Artist> getAllArtistsWithSongs() {
        return artistRepository.findAllWithSongs();
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void rateSong(Long userId, Long songId, int ratingValue) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Song not found with id: " + songId));

        Optional<Rating> existingRatingOpt = user.getRatings().stream()
                .filter(r -> r.getSong().getId().equals(songId))
                .findFirst();

        if (existingRatingOpt.isPresent()) {
            Rating existingRating = existingRatingOpt.get();
            existingRating.setRatingValue(ratingValue);
        } else {
            Rating newRating = new Rating(user, song, ratingValue);
            ratingRepository.save(newRating);
        }
    }
}