package com.quarto.backend.models.custom;

import java.util.List;

import com.quarto.backend.models.database.Position;
import com.quarto.backend.models.requests.PositionPostRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Simulation {
    private PositionPostRequest putPiece;
    private PositionPostRequest givePiece;
    private Position position;
    private List<Simulation> simulations;
}
