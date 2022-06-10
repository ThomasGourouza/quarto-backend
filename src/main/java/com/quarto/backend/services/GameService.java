package com.quarto.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quarto.backend.models.customs.Method;
import com.quarto.backend.models.database.Game;
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

    public Game mapToGame(GamePostRequest gamePostRequest, Method method) {
        boolean condition = (Method.PATCH.equals(method))
                ? (StringUtils.equals(gamePostRequest.getName(), null)
                        && StringUtils.equals(gamePostRequest.getDescription(), null))
                : (StringUtils.equals(gamePostRequest.getName(), null)
                        || StringUtils.equals(gamePostRequest.getDescription(), null));
        if (condition) {
            return null;
        }
        Game game = new Game();
        game.setName(gamePostRequest.getName());
        game.setDescription(gamePostRequest.getDescription());
        return game;
    }
}
