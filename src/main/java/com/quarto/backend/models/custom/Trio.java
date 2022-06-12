package com.quarto.backend.models.custom;

import java.util.List;

import com.quarto.backend.models.database.Square;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trio {
    private List<Square> matchingThreeSquares;
    private Square missingPieceSquare;
    private List<String> matchingPieceCharacteristics;
}
