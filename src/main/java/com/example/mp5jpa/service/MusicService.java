package com.example.mp5jpa.service;

import com.example.mp5jpa.model.Song;
import com.example.mp5jpa.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final SongRepository songRepository;
    // ... inne repozytoria

    public List<Song> getAllSongs() {
        return songRepository.findAllWithArtists();
    }
}