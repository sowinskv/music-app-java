package com.example.mp5jpa.repository;

import com.example.mp5jpa.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    @Query("SELECT DISTINCT a FROM Artist a LEFT JOIN FETCH a.songs")
    List<Artist> findAllWithSongs();
}
