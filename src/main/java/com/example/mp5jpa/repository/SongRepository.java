package com.example.mp5jpa.repository;

import com.example.mp5jpa.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {
}