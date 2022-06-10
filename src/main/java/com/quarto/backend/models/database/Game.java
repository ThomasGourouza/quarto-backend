package com.quarto.backend.models.database;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    private String id;

    @TextIndexed
    private String name;

    @TextIndexed
    private String description;

    @TextIndexed
    private String player1;

    @TextIndexed
    private String player2;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private String date;

    private List<Position> positions;
}
