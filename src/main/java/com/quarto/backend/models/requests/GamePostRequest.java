package com.quarto.backend.models.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamePostRequest {
    private String name;
    private String description;
    private String player1;
    private String player2;
}
