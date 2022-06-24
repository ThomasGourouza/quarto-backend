package com.quarto.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.quarto.backend.models.database.Piece;
import com.quarto.backend.models.database.Position;
import com.quarto.backend.models.database.Square;
import com.quarto.backend.models.database.characteristics.*;
import com.quarto.backend.models.requests.PositionPostRequest;

@Service
public class CommonService {

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

    public List<Square> copyOf(List<Square> squares) {
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

    public List<String> getMatchingCharacteristics(List<Square> squares) {
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

    public void scanVictory(List<Square> board) {
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

    public Position getNextPosition(Position lastPosition) {
        int nextPlayerId = lastPosition.getCurrentPlayerId() == 2 ? 1 : 2;
        return new Position(
                lastPosition.getRank() + 1,
                lastPosition.getCurrentPiece() != null ? lastPosition.getCurrentPlayerId() : nextPlayerId,
                copyOf(lastPosition.getBoard()),
                copyOf(lastPosition.getSet()),
                null);
    }

    public boolean isRequestSquare(Square square, PositionPostRequest positionPostRequest) {
        return square.getRow() == positionPostRequest.getRow()
                && square.getColumn() == positionPostRequest.getColumn();
    }
}
