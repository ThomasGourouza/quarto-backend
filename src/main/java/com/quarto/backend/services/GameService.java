package com.quarto.backend.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quarto.backend.models.database.Game;
import com.quarto.backend.models.database.Piece;
import com.quarto.backend.models.database.Position;
import com.quarto.backend.models.database.Square;
import com.quarto.backend.models.database.characteristics.*;
import com.quarto.backend.models.requests.GamePostRequest;
import com.quarto.backend.repositories.GameRepository;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game getGame(String id) {
        return gameRepository.findById(id).orElse(null);
    }

    public List<Game> getAllGamesByName(String name) {
        if (StringUtils.isBlank(name)) {
            return gameRepository.findAll();
        }
        return gameRepository.findByNameContainingAllIgnoreCase(name);
    }

    public List<Game> getAllGamesByNameExceptId(String name, String id) {
        return gameRepository.findByNameAndIdNot(name, id);
    }

    public Game getGameByName(String name) {
        return gameRepository.findFirstByName(name).orElse(null);
    }

    public Game createGame(Game game) {
        return gameRepository.save(game);
    }

    public void removeGame(String id) {
        gameRepository.deleteById(id);
    }

    public Game mapToGame(GamePostRequest gamePostRequest) {
        boolean wrongConditions = StringUtils.equals(gamePostRequest.getPlayer1(), null)
                || StringUtils.equals(gamePostRequest.getPlayer2(), null)
                || StringUtils.equals(gamePostRequest.getName(), null)
                || StringUtils.equals(gamePostRequest.getDescription(), null);
        if (wrongConditions) {
            return null;
        }
        Game game = new Game(
                gamePostRequest.getName(),
                gamePostRequest.getDescription(),
                gamePostRequest.getPlayer1(),
                gamePostRequest.getPlayer2());
        Position initialPosition = new Position(
                0,
                gamePostRequest.getPlayer1(),
                buildSquares(),
                buildSquares());
        List<Piece> pieces = getPieces();
        for (int i = 0; i < pieces.size(); i++) {
            initialPosition.getSet().get(i).setPiece(pieces.get(i));
        }
        game.getPositions().add(initialPosition);
        return game;
    }

    private List<Square> buildSquares() {
        List<Square> squares = new ArrayList<>();
        List<Integer> numbers = List.of(1, 2, 3, 4);
        numbers.forEach(row -> numbers.forEach(column -> squares.add(new Square(row, column))));
        return squares;
    }

    private List<Piece> getPieces() {
        List<Piece> pieces = new ArrayList<>();
        pieces.add(new Piece(Color.WHITE, Shape.SQUARE, Size.BIG, Top.FULL));
        pieces.add(new Piece(Color.WHITE, Shape.SQUARE, Size.BIG, Top.HOLE));
        pieces.add(new Piece(Color.WHITE, Shape.SQUARE, Size.SMALL, Top.FULL));
        pieces.add(new Piece(Color.WHITE, Shape.SQUARE, Size.SMALL, Top.HOLE));
        pieces.add(new Piece(Color.WHITE, Shape.ROUND, Size.BIG, Top.FULL));
        pieces.add(new Piece(Color.WHITE, Shape.ROUND, Size.BIG, Top.HOLE));
        pieces.add(new Piece(Color.WHITE, Shape.ROUND, Size.SMALL, Top.FULL));
        pieces.add(new Piece(Color.WHITE, Shape.ROUND, Size.SMALL, Top.HOLE));
        pieces.add(new Piece(Color.BLACK, Shape.SQUARE, Size.BIG, Top.FULL));
        pieces.add(new Piece(Color.BLACK, Shape.SQUARE, Size.BIG, Top.HOLE));
        pieces.add(new Piece(Color.BLACK, Shape.SQUARE, Size.SMALL, Top.FULL));
        pieces.add(new Piece(Color.BLACK, Shape.SQUARE, Size.SMALL, Top.HOLE));
        pieces.add(new Piece(Color.BLACK, Shape.ROUND, Size.BIG, Top.FULL));
        pieces.add(new Piece(Color.BLACK, Shape.ROUND, Size.BIG, Top.HOLE));
        pieces.add(new Piece(Color.BLACK, Shape.ROUND, Size.SMALL, Top.FULL));
        pieces.add(new Piece(Color.BLACK, Shape.ROUND, Size.SMALL, Top.HOLE));
        return pieces;
    }
}
