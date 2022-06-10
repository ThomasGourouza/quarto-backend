package com.quarto.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.quarto.backend.models.database.Board;

public interface BoardRepository extends MongoRepository<Board, String> {

    List<Board> findByNameContainingAllIgnoreCase(String name);
    
    List<Board> findByNameAndIdNot(String name, String id);

    Optional<Board> findFirstByName(String name);
    
}
