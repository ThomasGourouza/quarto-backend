package com.vegetation.backend.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.vegetation.backend.models.Tree;

public interface TreeRepository extends MongoRepository<Tree, String> {

    Optional<Tree> findByName(String name);

    Page<Tree> findAllBy(TextCriteria criteria, Pageable pageable);
    
}
