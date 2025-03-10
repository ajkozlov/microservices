package com.epam.learning.song.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {
	Long deleteByResourceId(Long id);
}