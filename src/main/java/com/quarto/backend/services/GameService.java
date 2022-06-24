package com.quarto.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.quarto.backend.models.database.Game;
import com.quarto.backend.models.database.Piece;
import com.quarto.backend.models.database.Position;
import com.quarto.backend.models.database.Square;
import com.quarto.backend.models.database.characteristics.*;
import com.quarto.backend.models.requests.GamePostRequest;
import com.quarto.backend.models.requests.PositionPostRequest;
import com.quarto.backend.repositories.GameRepository;

@Service
public class GameService {
    
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CommonService commonService;
    
    private static final String ERROR_MESSAGE = "errorMessage";

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

    public void removeAllGames() {
        gameRepository.deleteAll();
    }

    public Game initGame(GamePostRequest gamePostRequest) {
        Game game = new Game(
                gamePostRequest.getName(),
                gamePostRequest.getDescription(),
                gamePostRequest.getPlayer1(),
                gamePostRequest.getPlayer2());
        Position initialPosition = new Position(
                0,
                1,
                buildSquares(),
                buildSquares());
        List<Piece> pieces = getPieces();
        for (int i = 0; i < pieces.size(); i++) {
            initialPosition.getSet().get(i).setPiece(pieces.get(i));
        }
        game.getPositions().add(initialPosition);
        return game;
    }

    public boolean isWrongPositionJSON(PositionPostRequest positionPostRequest) {
        return positionPostRequest.getRow() == 0
                || positionPostRequest.getColumn() == 0;
    }

    public boolean isWrongGameJSON(GamePostRequest gamePostRequest) {
        return StringUtils.equals(gamePostRequest.getPlayer1(), null)
                || StringUtils.equals(gamePostRequest.getPlayer2(), null)
                || StringUtils.equals(gamePostRequest.getName(), null)
                || StringUtils.equals(gamePostRequest.getDescription(), null);
    }

    public Position getNewPosition(Position lastPosition, PositionPostRequest positionPostRequest) {
        Position newPosition = commonService.getNextPosition(lastPosition);
        if (lastPosition.getCurrentPiece() != null) {
            newPosition.getBoard().forEach(square -> {
                if (commonService.isRequestSquare(square, positionPostRequest)) {
                    square.setPiece(lastPosition.getCurrentPiece());
                }
            });
            commonService.scanVictory(newPosition.getBoard());
        } else {
            newPosition.getSet().forEach(square -> {
                if (commonService.isRequestSquare(square, positionPostRequest)) {
                    newPosition.setCurrentPiece(square.getPiece());
                    square.setPiece(null);
                }
            });
        }
        return newPosition;
    }

    public boolean isconflictPosition(Position lastPosition, PositionPostRequest positionPostRequest) {
        if (lastPosition.getCurrentPiece() != null) {
            Optional<Square> targetSquareOpt = lastPosition.getBoard().stream()
                    .filter(square -> square.getRow() == positionPostRequest.getRow()
                            && square.getColumn() == positionPostRequest.getColumn())
                    .findAny();
            return targetSquareOpt.isEmpty() || targetSquareOpt.get().getPiece() != null;
        }
        Optional<Square> targetSquareOpt = lastPosition.getSet().stream()
                .filter(square -> square.getRow() == positionPostRequest.getRow()
                        && square.getColumn() == positionPostRequest.getColumn())
                .findAny();
        return targetSquareOpt.isEmpty() || targetSquareOpt.get().getPiece() == null;
    }

    public Position getLastPosition(Game game) {
        List<Position> positions = game.getPositions();
        return positions.get(positions.size() - 1);
    }

    public boolean isWin(Position newPosition) {
        return newPosition.getBoard().stream().anyMatch(Square::isWinner);
    }

    public boolean isGameEnd(Position newPosition) {
        return newPosition.getSet().stream().allMatch(square -> square.getPiece() == null)
                && newPosition.getCurrentPiece() == null;
    }

    public void addNewPositionToGame(Game game, Position newPosition) {
        game.setOver(isWin(newPosition) || isGameEnd(newPosition));
        if (game.isOver() && isWin(newPosition)) {
            game.getPlayers().stream().filter(player -> player.getId() == newPosition.getCurrentPlayerId()).findAny()
                    .ifPresent(player -> player.setWinner(true));
        }
        game.getPositions().add(newPosition);
    }

    public HttpHeaders header(String message) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ERROR_MESSAGE, message);
        return responseHeaders;
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
