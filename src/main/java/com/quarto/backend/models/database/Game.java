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

    private List<Player> players;

    private boolean over;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;

    private List<Position> positions;

    public Game(String name, String description, String player1, String player2) {
        this.name = name;
        this.description = description;
        this.players = List.of(new Player(1, player1), new Player(2, player2));
        this.over = false;
        this.date = LocalDateTime.now();
        this.positions = new ArrayList<>();
    }

}
