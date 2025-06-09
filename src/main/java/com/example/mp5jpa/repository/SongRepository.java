package com.example.mp5jpa.repository;

import com.example.mp5jpa.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    @Query("SELECT s FROM Song s JOIN FETCH s.artist")
    List<Song> findAllWithArtists();
}