package com.quarto.backend.models.database;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private int rank;
    private int currentPlayerId;
    private List<Square> board;
    private List<Square> set;
    private Piece currentPiece;

    public Position(int rank, int currentPlayerId, List<Square> board, List<Square> set) {
        this.rank = rank;
        this.currentPlayerId = currentPlayerId;
        this.board = board;
        this.set = set;
        this.currentPiece = null;
    }

}
