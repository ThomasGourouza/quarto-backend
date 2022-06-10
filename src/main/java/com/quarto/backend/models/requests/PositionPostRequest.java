package com.quarto.backend.models.requests;

import com.quarto.backend.models.database.Coordinate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionPostRequest {
    private String gameId;
    private Coordinate coordinate;
    private Boolean toBoard;
}
