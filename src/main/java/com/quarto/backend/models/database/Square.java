package com.quarto.backend.models.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Square {
    private Coordinate coordinate;
    private Piece piece;

    public Square(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.piece = null;
    }

}
