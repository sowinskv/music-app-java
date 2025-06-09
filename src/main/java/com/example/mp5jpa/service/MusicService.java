package com.example.mp5jpa.service;

import com.example.mp5jpa.model.Artist;
import com.example.mp5jpa.model.Song;
import com.example.mp5jpa.repository.ArtistRepository;
import com.example.mp5jpa.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;

    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Artist> getAllArtistsWithSongs() {
        return artistRepository.findAllWithSongs();
    }
}