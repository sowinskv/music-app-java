package com.example.mp5jpa.repository;

import com.example.mp5jpa.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
}