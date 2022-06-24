package com.quarto.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quarto.backend.models.custom.Simulation;
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
        // si pas de piece au premier move, placer au hasard.
        if (lastPosition.getCurrentPiece() == null) {
            return List.of(giveRandomPieceSquare(lastPosition));
        }
        // sinon,
        List<Square> winningSquares = getWinningSquares(lastPosition.getBoard(),
                lastPosition.getCurrentPiece());
        // si la victoire est possible, gagner.
        if (!winningSquares.isEmpty()) {
            return List.of(getWinningPosition(winningSquares));
        }
        // sinon regarder les cases restantes.
        List<Square> remainingSquares = getRemainingSquares(lastPosition.getBoard());
        List<Square> remainingSet = getRemainingSquaresWithPiece(lastPosition.getSet());
        // si il n'en reste qu'une, placer la derniere piece.
        if (remainingSquares.size() == 1) {
            PositionPostRequest positionPostRequest = new PositionPostRequest();
            remainingSquares.stream().filter(square -> square.getPiece() == null).findAny().ifPresent(square -> {
                positionPostRequest.setColumn(square.getColumn());
                positionPostRequest.setRow(square.getRow());
            });
            return List.of(positionPostRequest);
        }
        // sinon, creer des simulations de toutes les combinaisons de coups possibles:
        List<Simulation> simulations = getSimulations(remainingSet, remainingSquares, lastPosition);
        // Filtrer les simulations en supprimant celles qui font gagner l'adversaire
        List<Simulation> simulationsNoLoose = simulations.stream()
                .filter(simulation -> getWinningSquares(simulation.getPosition().getBoard(),
                        simulation.getPosition().getCurrentPiece()).isEmpty())
                .collect(Collectors.toList());
        // Si il n'y en a pas, jouer au hasard
        if (simulationsNoLoose.isEmpty()) {
            return getRandomMoves(simulations);
        }
        // sinon, si il ne reste qu'un coup au tour suivant ou si il reste plus de 8 squares, jouer une simulation
        if (remainingSquares.size() == 2 || remainingSquares.size() >= 8) {
            return getRandomMoves(simulationsNoLoose);
        }
        // sinon, pour chaque simulation,
        // lui associer d'autres simulations de tous les coups suivants possibles:
        simulationsNoLoose.forEach(simulation -> {
            Position lastPosition2 = simulation.getPosition();
            List<Square> remainingSquares2 = getRemainingSquares(lastPosition2.getBoard());
            List<Square> remainingSet2 = getRemainingSquaresWithPiece(lastPosition2.getSet());
            simulation.setSimulations(getSimulations(remainingSet2, remainingSquares2, lastPosition2));
        });
        // si une des simulations de simulations est une victoire forcee, jouer cette
        // simulation
        List<Simulation> simulationsForcedWin = simulationsNoLoose.stream()
                .filter(simulation -> simulation.getSimulations().stream()
                        .allMatch(nextSimulation -> canWin(nextSimulation.getPosition())))
                .collect(Collectors.toList());
        if (!simulationsForcedWin.isEmpty()) {
            return getRandomMoves(simulationsForcedWin);
        }
        // sinon, filtrer les simulations en supprimant celles dont une des simulations
        // fait systematiquement perdre = fait gagner le tour d'apres.
        // List<Simulation> simulationsNoForceLoose = simulationsNoLoose.stream()
        //         .filter(simulation -> simulation.getSimulations().stream()
        //                 .noneMatch(nextSimulation -> canOpponentWin(nextSimulation.getPosition())))
        //         .collect(Collectors.toList());
        // // Si il n'y en a pas
        // if (simulationsNoForceLoose.isEmpty()) {
        //     // jouer un coup qui fait gagner le tour d'apres.
        //     // Filtrer les simulations en supprimant celles dont une des simulations
        //     // fait systematiquement perdre = fait gagner le tour d'apres.
        //     List<Simulation> simulationsCanWin = simulationsNoLoose.stream()
        //             .filter(simulation -> simulation.getSimulations().stream()
        //                     .anyMatch(nextSimulation -> canWin(nextSimulation.getPosition())))
        //             .collect(Collectors.toList());
        //     if (!simulationsCanWin.isEmpty()) {
        //         return getRandomMoves(simulationsCanWin);
        //     }
        //     // sinon jouer au hasard
        //     return getRandomMoves(simulationsNoLoose);
        // }

        // sinon, refiltrer les simulations en ne gardant que celles dont une des
        // simulations fait gagner le tour d'apres.
        List<Simulation> simulationsCanWin = simulationsNoLoose.stream()
                .filter(simulation -> simulation.getSimulations().stream()
                        .anyMatch(nextSimulation -> canWin(nextSimulation.getPosition())))
                .collect(Collectors.toList());
        if (!simulationsCanWin.isEmpty()) {
            return getRandomMoves(simulationsCanWin);
        }
        // sinon, joue au hasard une des simulations
        return getRandomMoves(simulationsNoLoose);
    }

    private boolean canOpponentWin(Position position) {
        // quelque soit ou je place ma piece, la piece que je donnerai gagnera
        List<Square> remainingSquares = getRemainingSquares(position.getBoard());
        List<Square> remainingSet = getRemainingSquaresWithPiece(position.getSet());
        if (remainingSquares.isEmpty() || remainingSet.isEmpty()) {
            return false;
        }
        return getSimulations(remainingSet, remainingSquares, position).stream()
                .noneMatch(simulation -> getWinningSquares(simulation.getPosition().getBoard(),
                        simulation.getPosition().getCurrentPiece()).isEmpty());
    }

    private boolean canWin(Position position) {
        return !getWinningSquares(position.getBoard(), position.getCurrentPiece()).isEmpty();
    }

    private PositionPostRequest getWinningPosition(List<Square> winningSquares) {
        PositionPostRequest positionPostRequest = new PositionPostRequest();
        Square winningSquare = getRandom(winningSquares);
        positionPostRequest.setRow(winningSquare.getRow());
        positionPostRequest.setColumn(winningSquare.getColumn());
        return positionPostRequest;
    }

    private List<PositionPostRequest> getRandomMoves(List<Simulation> simulations) {
        Simulation randomSimulation = getRandomSimulation(simulations);
        return List.of(randomSimulation.getPutPiece(), randomSimulation.getGivePiece());
    }

    private List<Simulation> getSimulations(List<Square> remainingSet, List<Square> remainingSquares,
            Position lastPosition) {
        List<Simulation> simulations = new ArrayList<>();
        // pour chaque case libre du board,
        remainingSquares.forEach(remainingSquare -> {
            // poser la piece sur la case puis pour chaque piece restante du set,
            PositionPostRequest positionPostRequest = new PositionPostRequest(remainingSquare.getRow(),
                    remainingSquare.getColumn());
            Position simulPosition = commonService.getNewPosition(lastPosition, positionPostRequest);
            remainingSet.forEach(remainingPiece -> {
                // donner le piece.
                PositionPostRequest positionPostRequest2 = new PositionPostRequest(remainingPiece.getRow(),
                        remainingPiece.getColumn());
                Position simulPosition2 = commonService.getNewPosition(simulPosition, positionPostRequest2);
                simulations.add(
                        new Simulation(positionPostRequest, positionPostRequest2, simulPosition2, new ArrayList<>()));
            });
        });
        return simulations;
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

    private Simulation getRandomSimulation(List<Simulation> simulations) {
        return simulations.get(rand.nextInt(simulations.size()));
    }

    private List<PositionPostRequest> getRandomPostRequests(List<List<PositionPostRequest>> simulations) {
        return simulations.get(rand.nextInt(simulations.size()));
    }

    private List<Square> getRemainingSquares(List<Square> board) {
        return commonService
                .copyOf(board.stream().filter(square -> square.getPiece() == null).collect(Collectors.toList()));
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
