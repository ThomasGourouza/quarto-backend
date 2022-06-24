package com.quarto.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quarto.backend.models.custom.Trio;
import com.quarto.backend.models.database.Piece;
import com.quarto.backend.models.database.Position;
import com.quarto.backend.models.database.Square;
import com.quarto.backend.models.database.characteristics.*;
import com.quarto.backend.models.requests.PositionPostRequest;

@Service
public class AiService {

    @Autowired
    private CommonService commonService;

    private Random rand = new Random();

    public List<PositionPostRequest> getAiPositions(Position lastPosition) {
        if (lastPosition.getCurrentPiece() == null) {
            return List.of(giveRandomPieceSquare(lastPosition));
        }
        List<Square> winningSquares = getWinningSquares(lastPosition.getBoard(),
                lastPosition.getCurrentPiece());
        if (!winningSquares.isEmpty()) {
            return List.of(getWinningPosition(winningSquares));
        }
        List<Square> remainingSet = getRemainingSquaresWithPiece(lastPosition.getSet());
        if (remainingSet.isEmpty()) {
            return List.of(setPieceOnRandomBoardSquare(lastPosition));
        }
        List<List<PositionPostRequest>> simulations = getSimulations(remainingSet,
                getRemainingSquares(lastPosition.getBoard()),
                lastPosition);
        if (simulations.isEmpty()) {
            return List.of(setPieceOnRandomBoardSquare(lastPosition), giveRandomPieceSquare(lastPosition));
        }
        return getRandomPostRequests(simulations);
    }

    private PositionPostRequest getWinningPosition(List<Square> winningSquares) {
        PositionPostRequest positionPostRequest = new PositionPostRequest();
        Square winningSquare = getRandom(winningSquares);
        positionPostRequest.setRow(winningSquare.getRow());
        positionPostRequest.setColumn(winningSquare.getColumn());
        return positionPostRequest;
    }

    private List<List<PositionPostRequest>> getSimulations(List<Square> remainingSet,
            List<Square> remainingSquares, Position lastPosition) {
        List<List<PositionPostRequest>> simulations = new ArrayList<>();
        remainingSquares.forEach(s -> {
            PositionPostRequest positionPostRequest = new PositionPostRequest(s.getRow(), s.getColumn());
            Position simulPosition = commonService.getNewPosition(lastPosition, positionPostRequest);
            remainingSet.forEach(sp -> {
                List<Square> ws = getWinningSquares(simulPosition.getBoard(), sp.getPiece());
                if (ws.isEmpty()) {
                    PositionPostRequest positionPostRequest2 = new PositionPostRequest(sp.getRow(),
                            sp.getColumn());
                    simulations.add(List.of(positionPostRequest, positionPostRequest2));
                }
            });
        });
        return simulations;
    }

    private PositionPostRequest setPieceOnRandomBoardSquare(Position lastPosition) {
        PositionPostRequest positionPostRequest = new PositionPostRequest();
        Square square = getRandom(
                lastPosition.getBoard().stream().filter(s -> s.getPiece() == null).collect(Collectors.toList()));
        positionPostRequest.setRow(square.getRow());
        positionPostRequest.setColumn(square.getColumn());
        return positionPostRequest;
    }

    private PositionPostRequest giveRandomPieceSquare(Position lastPosition) {
        PositionPostRequest positionPostRequest = new PositionPostRequest();
        Square square = getRandom(
                lastPosition.getSet().stream().filter(s -> s.getPiece() != null).collect(Collectors.toList()));
        positionPostRequest.setRow(square.getRow());
        positionPostRequest.setColumn(square.getColumn());
        return positionPostRequest;
    }

    private Square getRandom(List<Square> squares) {
        return squares.get(rand.nextInt(squares.size()));
    }

    private List<PositionPostRequest> getRandomPostRequests(List<List<PositionPostRequest>> simulations) {
        return simulations.get(rand.nextInt(simulations.size()));
    }

    private List<Square> getRemainingSquares(List<Square> board) {
        return commonService.copyOf(board.stream().filter(square -> square.getPiece() == null).collect(Collectors.toList()));
    }

    private List<Square> getRemainingSquaresWithPiece(List<Square> set) {
        return commonService
                .copyOf(set.stream().filter(square -> square.getPiece() != null).collect(Collectors.toList()));
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
            List<String> matchingCharacteristics = commonService.getMatchingCharacteristics(occupiedSquares);
            boardLine.stream().filter(square -> square.getPiece() == null).findAny()
                    .ifPresent(missingPieceSquare -> trios.add(new Trio(
                            occupiedSquares,
                            missingPieceSquare,
                            matchingCharacteristics)));
        }
    }

}

// A.I.:

// quand je n'ai pas de piece au premier move,
// 	placer au hasard.

// quand j'ai une piece,
// 	gagner, sinon:
// 	creer une simulations_me de :

// 	pour chaque case libre du board,
// 	poser la piece sur la case puis pour chaque piece restante du set,
// 	donner le piece.
// 	Filtrer les simulations en supprimant celles qui font perdre = qui font gagner le tour d'apres.

// 	si vide, joue au hasard.
// 	sinon,

// 	Pour chaque simulation de simulations_me,
// 	lui associer une simulations_you de :

// 	pour chaque case libre du board,
// 	poser la piece sur la case puis pour chaque piece restante du set,
// 	donner la piece.

// 	Filtrer les simulations_me en supprimant celles dont une des simulations_you fait systematiquement perdre = fait gagner le tour d'apres.

// 	si vide, joue au hasard une des simulations_me.
// 	sinon,

// 	refiltrer les simulations_me en ne gardant que celles dont une des simulations_you fait gagner le tour d'apres.

// 	si vide, joue au hasard une des simulations_me filtre une fois.
// 	sinon, joue au hasard une des simulations_me filtre deux fois.
