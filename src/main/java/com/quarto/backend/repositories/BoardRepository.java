package com.quarto.backend.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.quarto.backend.models.Board;

public interface BoardRepository extends MongoRepository<Board, String> {

    Optional<Board> findByName(String name);

    Page<Board> findAllBy(TextCriteria criteria, Pageable pageable);
    
}
