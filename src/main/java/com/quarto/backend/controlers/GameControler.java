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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quarto.backend.models.database.Game;
import com.quarto.backend.models.requests.GamePostRequest;
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
        Game game = gameService.mapToGame(gamePostRequest);
        if (game == null) {
            return headers(WRONG_JSON, HttpStatus.BAD_REQUEST);
        }
        Game gameAlreadySaved = gameService.getGameByName(game.getName());
        if (gameAlreadySaved != null) {
            return headers(NAME_EXISTS, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(gameService.createGame(game), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    ResponseEntity<Game> putGame(@PathVariable String id, @RequestBody GamePostRequest gamePutRequest) {
        Game newGame = gameService.mapToGame(gamePutRequest);
        if (newGame == null) {
            return headers(WRONG_JSON, HttpStatus.BAD_REQUEST);
        }
        Game gameAlreadySaved = gameService.getGame(id);
        if (gameAlreadySaved == null) {
            return headers(NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        List<Game> sameNameGames = gameService.getAllGamesByNameExceptId(newGame.getName(), id);
        if (!sameNameGames.isEmpty()) {
            return headers(NAME_EXISTS, HttpStatus.CONFLICT);
        }
        gameAlreadySaved.setName(newGame.getName());
        gameAlreadySaved.setDescription(newGame.getDescription());
        if (StringUtils.equals(newGame.getPlayer1(), newGame.getPlayer2())) {
            return headers(SAME_PLAYERS, HttpStatus.CONFLICT);
        }
        gameAlreadySaved.getPositions().forEach(position -> {
            if (StringUtils.equals(position.getCurrentPlayer(), gameAlreadySaved.getPlayer1())) {
                position.setCurrentPlayer(newGame.getPlayer1());
            }
            if (StringUtils.equals(position.getCurrentPlayer(), gameAlreadySaved.getPlayer2())) {
                position.setCurrentPlayer(newGame.getPlayer2());
            }
        });
        gameAlreadySaved.setPlayer1(newGame.getPlayer1());
        gameAlreadySaved.setPlayer2(newGame.getPlayer2());
        return new ResponseEntity<>(gameService.createGame(gameAlreadySaved), HttpStatus.ACCEPTED);
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
