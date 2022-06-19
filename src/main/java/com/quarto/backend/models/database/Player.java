package com.quarto.backend.models.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private int id;
    private String name;
    private boolean winner;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.winner = false;
    }
}
