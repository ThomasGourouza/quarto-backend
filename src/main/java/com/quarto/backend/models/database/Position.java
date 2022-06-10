package com.quarto.backend.models.database;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private Integer rank;
    private String currentPlayer;
    private List<Square> board;
    private List<Square> set;
    private Piece currentPiece;
}
