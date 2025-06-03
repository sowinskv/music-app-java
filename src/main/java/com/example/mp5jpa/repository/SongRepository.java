package com.example.mp5jpa.repository;

import com.example.mp5jpa.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    @Query("SELECT s FROM Song s LEFT JOIN FETCH s.artist")
    List<Song> findAllWithArtists();
}