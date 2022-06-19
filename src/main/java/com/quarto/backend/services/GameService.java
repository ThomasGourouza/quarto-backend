package com.quarto.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quarto.backend.models.custom.Trio;
import com.quarto.backend.models.database.Game;
import com.quarto.backend.models.database.Piece;
import com.quarto.backend.models.database.Position;
import com.quarto.backend.models.database.Square;
import com.quarto.backend.models.database.characteristics.*;
import com.quarto.backend.models.requests.GamePostRequest;
import com.quarto.backend.models.requests.GamePutRequest;
import com.quarto.backend.models.requests.PositionPostRequest;
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

    public boolean isWrongGameJSON(GamePutRequest gamePostRequest) {
        return StringUtils.equals(gamePostRequest.getName(), null)
                || StringUtils.equals(gamePostRequest.getDescription(), null);
    }

    public Position getNewPosition(Position lastPosition, PositionPostRequest positionPostRequest) {
        Position newPosition = getNextPosition(lastPosition);
        if (lastPosition.getCurrentPiece() != null) {
            newPosition.getBoard().forEach(square -> {
                if (isRequestSquare(square, positionPostRequest)) {
                    square.setPiece(lastPosition.getCurrentPiece());
                }
            });
            scanVictory(newPosition.getBoard());
        } else {
            newPosition.getSet().forEach(square -> {
                if (isRequestSquare(square, positionPostRequest)) {
                    newPosition.setCurrentPiece(square.getPiece());
                    square.setPiece(null);
                }
            });
        }
        return newPosition;
    }

    private void scanVictory(List<Square> board) {
        List<List<Square>> tableOfLines = new ArrayList<>();
        List.of(1, 2, 3, 4).forEach(number -> {
            List<Square> row = board.stream().filter(square -> square.getRow() == number)
                    .collect(Collectors.toList());
            List<Square> column = board.stream().filter(square -> square.getColumn() == number)
                    .collect(Collectors.toList());
            tableOfLines.add(row);
            tableOfLines.add(column);
        });
        List<Square> firstDiag = board.stream().filter(square -> square.getRow() == square.getColumn())
                .collect(Collectors.toList());
        List<Square> secondDiag = board.stream().filter(square -> square.getRow() + square.getColumn() == 5)
                .collect(Collectors.toList());
        tableOfLines.add(firstDiag);
        tableOfLines.add(secondDiag);

        tableOfLines.stream().forEach(line -> {
            if (line.stream().allMatch(square -> square.getPiece() != null)
                    && !getMatchingCharacteristics(line).isEmpty()) {
                line.forEach(square -> square.setWinner(true));
            }
        });
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

    public List<Position> getAiPositions(Position lastPosition) {
        List<Position> positions = new ArrayList<>();
        Position newPosition = getNextPosition(lastPosition);

        if (lastPosition.getCurrentPiece() != null) {
            // TODO: deux moves -> placer piece sur board puis choisir piece dans set
            // partie 1: placer la piece
            List<Square> winningSquares = getWinningSquares(lastPosition.getBoard(),
                    lastPosition.getCurrentPiece());
            // ... sur une case gagnante
            if (!winningSquares.isEmpty()) {
                // choisir au hasard une des square pour y poser la piece
                Square chosenSquare = winningSquares.get(0);
                newPosition.getBoard().stream().filter(square -> square.getRow() == chosenSquare.getRow()
                        && square.getColumn() == chosenSquare.getColumn()).findAny()
                        .ifPresent(square -> square.setPiece(lastPosition.getCurrentPiece()));
            } else {
                // ... ailleurs: fait des simulations de placements au hasard puis regarde pour
                // chaque simulation
                // les trios dont on peut choisir une piece à donner qui ne gagne pas

                List<List<Position>> simulations = new ArrayList<>();
                // pour chaque case restantes du board où poser la piece, associer une liste de
                // piece à donner
                getRemainingSquares(lastPosition.getBoard()).forEach(s -> {
                    PositionPostRequest positionPostRequest = new PositionPostRequest(s.getRow(), s.getColumn());
                    Position simulPosition = getNewPosition(lastPosition, positionPostRequest);
                    // pour cette position, chercher les pieces du set qui ne gagnent pas
                    simulPosition.getSet().forEach(sp -> {
                        List<Square> ws = getWinningSquares(simulPosition.getBoard(), lastPosition.getCurrentPiece());
                        if (ws.isEmpty()) {
                            PositionPostRequest positionPostRequest2 = new PositionPostRequest(sp.getRow(), sp.getColumn());
                            Position simulPosition2 = getNewPosition(simulPosition, positionPostRequest2);
                            simulations.add(List.of(simulPosition, simulPosition2));
                        }
                    });
                });
                // choisir au hasard un item de simulations, si non vide
                simulations.stream().findAny().ifPresent(pos -> positions.addAll(pos));
            }
        } else {
            // TODO: un seul move -> choisir piece dans set
        }
        positions.add(newPosition);
        return positions;
    }

    private List<Square> getRemainingSquares(List<Square> board) {
        return board.stream().filter(square -> square.getPiece() == null).collect(Collectors.toList());
    }

    private List<Square> getWinningSquares(List<Square> board, Piece currentPiece) {
        return getAllTrios(board).stream()
                .filter(trio -> isMatchingPiece(currentPiece, trio.getMatchingPieceCharacteristics()))
                .map(Trio::getMissingPieceSquare).collect(Collectors.toList());
    }

    private boolean isMatchingPiece(Piece currentPiece, List<String> matchingPieceCharacteristics) {
        return matchingPieceCharacteristics.stream().anyMatch(characteristic -> List.of(
                currentPiece.getColor().toString(),
                currentPiece.getSize().toString(),
                currentPiece.getShape().toString(),
                currentPiece.getTop().toString()).contains(characteristic));
    }

    private boolean areMatchingSquares(List<Square> squares) {
        return squares.stream().allMatch(square -> Color.WHITE.equals(square.getPiece().getColor())
                || Color.BLACK.equals(square.getPiece().getColor())
                || Size.BIG.equals(square.getPiece().getSize())
                || Size.SMALL.equals(square.getPiece().getSize())
                || Shape.SQUARE.equals(square.getPiece().getShape())
                || Shape.ROUND.equals(square.getPiece().getShape())
                || Top.FULL.equals(square.getPiece().getTop())
                || Top.HOLE.equals(square.getPiece().getTop()));
    }

    private List<Trio> getAllTrios(List<Square> board) {
        List<Trio> trios = new ArrayList<>();
        List<Integer> numbers = List.of(1, 2, 3, 4);
        numbers.forEach(number -> {
            fillMatchingTrioList(board, trios, "rows", number);
            fillMatchingTrioList(board, trios, "columns", number);
        });
        fillMatchingTrioList(board, trios, "firstDiagonal", 0);
        fillMatchingTrioList(board, trios, "secondDiagonal", 0);
        return trios;
    }

    private void fillMatchingTrioList(List<Square> board, List<Trio> trios, String direction, Integer number) {
        List<Square> boardLine = board.stream().filter(square -> {
            switch (direction) {
                case "firstDiagonal":
                    return square.getRow() == square.getColumn();
                case "secondDiagonal":
                    return square.getRow() + square.getColumn() == 5;
                case "rows":
                    return number.intValue() == square.getRow();
                case "columns":
                    return number.intValue() == square.getRow();
                default:
                    return false;
            }
        }).collect(Collectors.toList());
        List<Square> occupiedSquares = boardLine.stream().filter(square -> square.getPiece() != null)
                .collect(Collectors.toList());
        if (occupiedSquares.size() == 3 && areMatchingSquares(occupiedSquares)) {
            List<String> matchingCharacteristics = getMatchingCharacteristics(occupiedSquares);
            boardLine.stream().filter(square -> square.getPiece() == null).findAny()
                    .ifPresent(missingPieceSquare -> trios.add(new Trio(
                            occupiedSquares,
                            missingPieceSquare,
                            matchingCharacteristics)));
        }
    }

    private List<String> getMatchingCharacteristics(List<Square> squares) {
        List<String> matchingCharacteristics = new ArrayList<>();
        if (squares.stream().allMatch(square -> Color.WHITE.equals(square.getPiece().getColor()))) {
            matchingCharacteristics.add(Color.WHITE.toString());
        }
        if (squares.stream().allMatch(square -> Color.BLACK.equals(square.getPiece().getColor()))) {
            matchingCharacteristics.add(Color.BLACK.toString());
        }
        if (squares.stream().allMatch(square -> Size.BIG.equals(square.getPiece().getSize()))) {
            matchingCharacteristics.add(Size.BIG.toString());
        }
        if (squares.stream().allMatch(square -> Size.SMALL.equals(square.getPiece().getSize()))) {
            matchingCharacteristics.add(Size.SMALL.toString());
        }
        if (squares.stream().allMatch(square -> Shape.SQUARE.equals(square.getPiece().getShape()))) {
            matchingCharacteristics.add(Shape.SQUARE.toString());
        }
        if (squares.stream().allMatch(square -> Shape.ROUND.equals(square.getPiece().getShape()))) {
            matchingCharacteristics.add(Shape.ROUND.toString());
        }
        if (squares.stream().allMatch(square -> Top.FULL.equals(square.getPiece().getTop()))) {
            matchingCharacteristics.add(Top.FULL.toString());
        }
        if (squares.stream().allMatch(square -> Top.HOLE.equals(square.getPiece().getTop()))) {
            matchingCharacteristics.add(Top.HOLE.toString());
        }
        return matchingCharacteristics;
    }

    private List<Square> buildSquares() {
        List<Square> squares = new ArrayList<>();
        List<Integer> numbers = List.of(1, 2, 3, 4);
        numbers.forEach(row -> numbers.forEach(column -> squares.add(new Square(row, column))));
        return squares;
    }

    private boolean isRequestSquare(Square square, PositionPostRequest positionPostRequest) {
        return square.getRow() == positionPostRequest.getRow()
                && square.getColumn() == positionPostRequest.getColumn();
    }

    private Position getNextPosition(Position lastPosition) {
        int nextPlayerId = lastPosition.getCurrentPlayerId() == 2 ? 1 : 2;
        return new Position(
                lastPosition.getRank() + 1,
                lastPosition.getCurrentPiece() != null ? lastPosition.getCurrentPlayerId() : nextPlayerId,
                copyOf(lastPosition.getBoard()),
                copyOf(lastPosition.getSet()),
                null);
    }

    private List<Square> copyOf(List<Square> squares) {
        List<Square> boarsquaresCopy = new ArrayList<>();
        squares.forEach(square -> {
            Piece pieceCopy = new Piece();
            if (square.getPiece() != null) {
                pieceCopy.setColor(square.getPiece().getColor());
                pieceCopy.setShape(square.getPiece().getShape());
                pieceCopy.setSize(square.getPiece().getSize());
                pieceCopy.setTop(square.getPiece().getTop());
            } else {
                pieceCopy = null;
            }
            Square squareCopy = new Square(square.getRow(), square.getColumn(), pieceCopy, false);
            boarsquaresCopy.add(squareCopy);
        });
        return boarsquaresCopy;
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

}
