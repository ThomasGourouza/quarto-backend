package com.quarto.backend.models.database;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private LocalDateTime date;

    private List<Position> positions;

    public Game(String name, String description, String player1, String player2) {
        this.name = name;
        this.description = description;
        this.player1 = player1;
        this.player2 = player2;
        this.date = LocalDateTime.now();
        this.positions = new ArrayList<>();
    }

}
