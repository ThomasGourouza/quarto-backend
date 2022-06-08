package com.quarto.backend.services;

import java.util.List;
import java.util.Optional;
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
        List<Board> boards = getAllBoards();
        boolean isBoardInDb = boards.stream()
            .anyMatch(boardAlreadySaved -> boardAlreadySaved.getName().equals(board.getName()));
        if (isBoardInDb) {
            return null;
        }
        return boardRepository.save(board);
    }

    public Board updateBoard(String id, Board newBoard) {
        Board oldBoard = boardRepository.findById(id).orElse(null);
        if (oldBoard != null) {
            List<Board> boards = getAllBoards().stream().filter(board -> 
                board.getId() != oldBoard.getId()
            ).collect(Collectors.toList());
            boolean isBoardInDb = boards.stream()
                .anyMatch(boardAlreadySaved -> boardAlreadySaved.getName().equals(newBoard.getName()));
            if (isBoardInDb) {
                return null;
            }
            oldBoard.setName(newBoard.getName());
            oldBoard.setDescription(newBoard.getDescription());
            return boardRepository.save(oldBoard);
        }
        return null;
    }

    public boolean removeBoard(String id) {
        Board boardAlreadySaved = getBoard(id);
        if (boardAlreadySaved != null) {
            boardRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
