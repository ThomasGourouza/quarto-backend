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
import com.quarto.backend.models.requests.GamePostRequest;
import com.quarto.backend.models.requests.PositionPostRequest;
import com.quarto.backend.services.AiService;
import com.quarto.backend.services.GameService;

@RestController
@CrossOrigin
@RequestMapping("/games")
public class GameControler {
    private static final String WRONG_JSON = "Wrong JSON";
    private static final String PLAYERS_NAME = "Both players should have a distinct name";
    private static final String NOT_FOUND = "Game doesn't exist";
    private static final String CONFLICT_POSITION = "Impossible move";
    private static final String GAME_OVER = "The game is already over";

    @Autowired
    private GameService gameService;

    @Autowired
    private AiService aiService;

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
            return new ResponseEntity<>(null, gameService.header(NOT_FOUND), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @PostMapping("/")
    ResponseEntity<Game> createGame(@RequestBody GamePostRequest gamePostRequest) {
        if (gameService.isWrongGameJSON(gamePostRequest)) {
            return new ResponseEntity<>(null, gameService.header(WRONG_JSON), HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isBlank(gamePostRequest.getPlayer1()) || StringUtils.isBlank(gamePostRequest.getPlayer2())
                || StringUtils.equals(gamePostRequest.getPlayer1(), gamePostRequest.getPlayer2())) {
            return new ResponseEntity<>(null, gameService.header(PLAYERS_NAME), HttpStatus.METHOD_NOT_ALLOWED);
        }
        Game game = gameService.initGame(gamePostRequest);
        return new ResponseEntity<>(gameService.createGame(game), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    ResponseEntity<Game> putGame(@PathVariable String id, @RequestBody GamePostRequest gamePutRequest) {
        if (gameService.isWrongGameJSON(gamePutRequest)) {
            return new ResponseEntity<>(null, gameService.header(WRONG_JSON), HttpStatus.BAD_REQUEST);
        }
        Game game = gameService.getGame(id);
        if (game == null) {
            return new ResponseEntity<>(null, gameService.header(NOT_FOUND), HttpStatus.NOT_FOUND);
        }
        game.setName(gamePutRequest.getName());
        game.setDescription(gamePutRequest.getDescription());
        game.getPlayers().forEach(player ->
            player.setName(player.getId() == 1 ? gamePutRequest.getPlayer1() : gamePutRequest.getPlayer2())
        );
        return new ResponseEntity<>(gameService.createGame(game), HttpStatus.ACCEPTED);
    }

    @PatchMapping("/{id}/play")
    ResponseEntity<Game> setPosition(@PathVariable String id, @RequestBody PositionPostRequest positionPostRequest) {
        Game game = gameService.getGame(id);
        if (game == null) {
            return new ResponseEntity<>(null, gameService.header(NOT_FOUND), HttpStatus.NOT_FOUND);
        }
        if (gameService.isWrongPositionJSON(positionPostRequest)) {
            return new ResponseEntity<>(null, gameService.header(WRONG_JSON), HttpStatus.BAD_REQUEST);
        }
        if (game.isOver()) {
            return new ResponseEntity<>(null, gameService.header(GAME_OVER), HttpStatus.METHOD_NOT_ALLOWED);
        }
        Position lastPosition = gameService.getLastPosition(game);
        if (gameService.isconflictPosition(lastPosition, positionPostRequest)) {
            return new ResponseEntity<>(null, gameService.header(CONFLICT_POSITION), HttpStatus.CONFLICT);
        }
        Position newPosition = gameService.getNewPosition(
                lastPosition,
                positionPostRequest);
        gameService.addNewPositionToGame(game, newPosition);
        return new ResponseEntity<>(gameService.createGame(game), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}/moves")
    ResponseEntity<List<PositionPostRequest>> getAiMoves(@PathVariable String id) {
        Game game = gameService.getGame(id);
        if (game == null) {
            return new ResponseEntity<>(null, gameService.header(NOT_FOUND), HttpStatus.NOT_FOUND);
        }
        if (game.isOver()) {
            return new ResponseEntity<>(null, gameService.header(GAME_OVER), HttpStatus.METHOD_NOT_ALLOWED);
        }
        return new ResponseEntity<>(aiService.getAiPositions(gameService.getLastPosition(game)), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Game> removeGame(@PathVariable String id) {
        Game gameAlreadySaved = gameService.getGame(id);
        if (gameAlreadySaved == null) {
            return new ResponseEntity<>(null, gameService.header(NOT_FOUND), HttpStatus.NOT_FOUND);
        }
        gameService.removeGame(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/")
    ResponseEntity<Game> removeAllGame() {
        gameService.removeAllGames();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
