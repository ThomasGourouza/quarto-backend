package com.quarto.backend.models.database;

import com.quarto.backend.models.database.characteristics.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Piece {
    private Color color;
    private Shape shape;
    private Size size;
    private Top top;
}
