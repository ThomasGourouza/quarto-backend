package com.quarto.backend.models.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Square {
    private int row;
    private int column;
    private Piece piece;
    private boolean winner;

    public Square(int row, int column) {
        this.row = row;
        this.column = column;
        this.piece = null;
        this.winner = false;
    }

}
