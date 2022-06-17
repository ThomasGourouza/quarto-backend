package com.quarto.backend.controlers;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quarto.backend.models.database.Game;
import com.quarto.backend.models.database.Position;
import com.quarto.backend.models.database.Square;
import com.quarto.backend.models.requests.GamePostRequest;
import com.quarto.backend.models.requests.PositionPostRequest;
import com.quarto.backend.services.GameService;

@RestController
@CrossOrigin
@RequestMapping("/games")
public class GameControler {
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String WRONG_JSON = "Wrong JSON";
    private static final String NAME_EXISTS = "Name already exists";
    private static final String NOT_FOUND = "Game doesn't exist";
    private static final String SAME_PLAYERS = "The two players have the same name";
    private static final String CONFLICT_POSITION = "Impossible move";

    @Autowired
    private GameService gameService;

    @GetMapping("/")
    ResponseEntity<List<Game>> getAllGames() {
        return new ResponseEntity<>(gameService.getAllGames(), HttpStatus.OK);
    }

    @GetMapping("/search")
    ResponseEntity<List<Game>> getAllGamesByName(@RequestParam String name) {
        return new ResponseEntity<>(gameService.getAllGamesByName(name), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    ResponseEntity<Game> getGame(@PathVariable String id) {
        Game game = gameService.getGame(id);
        if (game == null) {
            return headers(NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @PostMapping("/")
    ResponseEntity<Game> createGame(@RequestBody GamePostRequest gamePostRequest) {
        Game game = gameService.initGame(gamePostRequest);
        if (game == null) {
            return headers(WRONG_JSON, HttpStatus.BAD_REQUEST);
        }
        Game gameAlreadySaved = gameService.getGameByName(game.getName());
        if (gameAlreadySaved != null) {
            return headers(NAME_EXISTS, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(gameService.createGame(game), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/play")
    ResponseEntity<Game> setPosition(@PathVariable String id, @RequestBody PositionPostRequest positionPostRequest) {
        Game game = gameService.getGame(id);
        if (game == null) {
            return headers(NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        if (gameService.isWrongPositionJSON(positionPostRequest)) {
            return headers(WRONG_JSON, HttpStatus.BAD_REQUEST);
        }
        Position lastPosition = gameService.getLastPosition(game);
        if (gameService.isconflictPosition(lastPosition, positionPostRequest)) {
            return headers(CONFLICT_POSITION, HttpStatus.CONFLICT);
        }
        Position newPosition = gameService.getNewPosition(
            lastPosition,
            positionPostRequest,
            game.getPlayer1(),
            game.getPlayer2()
        );
        game.setOver(newPosition.getBoard().stream().anyMatch(Square::isWinner));
        game.getPositions().add(newPosition);
        return new ResponseEntity<>(gameService.createGame(game), HttpStatus.ACCEPTED);
    }

    @PatchMapping("/{id}/play/ai")
    ResponseEntity<Game> setAiPosition(@PathVariable String id) {
        Game game = gameService.getGame(id);
        if (game == null) {
            return headers(NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        Position lastPosition = gameService.getLastPosition(game);
        List<Position> newPositions = gameService.getAiPositions(lastPosition, game.getPlayer1(), game.getPlayer2());
        game.getPositions().addAll(newPositions);
        return new ResponseEntity<>(gameService.createGame(game), HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}")
    ResponseEntity<Game> putGame(@PathVariable String id, @RequestBody GamePostRequest gamePutRequest) {
        if (gameService.isWrongGameJSON(gamePutRequest)) {
            return headers(WRONG_JSON, HttpStatus.BAD_REQUEST);
        }
        Game game = gameService.getGame(id);
        if (game == null) {
            return headers(NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        List<Game> sameNameGames = gameService.getAllGamesByNameExceptId(gamePutRequest.getName(), id);
        if (!sameNameGames.isEmpty()) {
            return headers(NAME_EXISTS, HttpStatus.CONFLICT);
        }
        game.setName(gamePutRequest.getName());
        game.setDescription(gamePutRequest.getDescription());
        if (StringUtils.equals(gamePutRequest.getPlayer1(), gamePutRequest.getPlayer2())) {
            return headers(SAME_PLAYERS, HttpStatus.CONFLICT);
        }
        game.getPositions().forEach(position -> {
            if (StringUtils.equals(position.getCurrentPlayer(), game.getPlayer1())) {
                position.setCurrentPlayer(gamePutRequest.getPlayer1());
            }
            if (StringUtils.equals(position.getCurrentPlayer(), game.getPlayer2())) {
                position.setCurrentPlayer(gamePutRequest.getPlayer2());
            }
        });
        game.setPlayer1(gamePutRequest.getPlayer1());
        game.setPlayer2(gamePutRequest.getPlayer2());
        return new ResponseEntity<>(gameService.createGame(game), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Game> removeGame(@PathVariable String id) {
        Game gameAlreadySaved = gameService.getGame(id);
        if (gameAlreadySaved == null) {
            return headers(NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        gameService.removeGame(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private ResponseEntity<Game> headers(String message, HttpStatus status) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ERROR_MESSAGE, message);
        return new ResponseEntity<>(null, responseHeaders, status);
    }

}
