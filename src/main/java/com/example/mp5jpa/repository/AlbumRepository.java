package com.example.mp5jpa.repository;

import com.example.mp5jpa.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("SELECT a FROM Album a")
    List<Album> findAllWithoutSongs();

    @Query("SELECT a FROM Album a LEFT JOIN FETCH a.songs WHERE a.id = :id")
    Optional<Album> findByIdWithSongs(@Param("id") Long id);
}