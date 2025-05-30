package com.example.mp5jpa.repository;

import com.example.mp5jpa.model.Rating;
import com.example.mp5jpa.model.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, RatingId> {
}