package com.quarto.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quarto.backend.models.Board;
import com.quarto.backend.repositories.BoardRepository;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;

    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    public Board getBoard(String id) {
        return boardRepository.findById(id).orElse(null);
    }

    public Board createBoard(Board board) {
        // récupérer la liste de tous les boards qu'on a dans la DB
        List<Board> boards = getAllBoards();
        // vérifier que aucun board n'a le même name que celui qu'on essaye d'enregistrer
        boolean isBoardInDb = boards.stream()
                .anyMatch(boardAlreadySaved -> boardAlreadySaved.getName().equals(board.getName()));
        // si l'arbre est déjà dans la DB
        if (isBoardInDb) {
            // on ne sauvegarde pas
            return null;
        }
        {
            return boardRepository.save(board);
        }
    }

    public Board updateBoard(String id, Board newBoard) {
        Board oldBoard = boardRepository.findById(id).orElse(null);
        if (oldBoard != null) {
            // récupérer la liste de tous les boards sauf celui qu'on cherche à modifier
            List<Board> boards = getAllBoards().stream().filter(t -> 
                t.getId() != oldBoard.getId()
            ).collect(Collectors.toList());
            // vérifier que aucun autre board n'a le même name que celui qu'on cherche à modifier
            boolean isBoardInDb = boards.stream()
                    .anyMatch(boardAlreadySaved -> boardAlreadySaved.getName().equals(newBoard.getName()));
            // si l'arbre est déjà dans la DB
            if (isBoardInDb) {
                // on ne sauvegarde pas
                return null;
            } else {
                oldBoard.setName(newBoard.getName());
                oldBoard.setDescription(newBoard.getDescription());
                return boardRepository.save(oldBoard);
            }
        }
        return null;
    }

    public String removeBoard(String id) {
        boardRepository.findById(id).ifPresent(board -> boardRepository.deleteById(id));
        return "Board " + id + " deleted.";
    }
}
