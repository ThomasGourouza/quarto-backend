package com.quarto.backend.models.database;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    @Id
    public String id;

    @TextIndexed
    public String name;

    @TextIndexed
    public String description;
}
