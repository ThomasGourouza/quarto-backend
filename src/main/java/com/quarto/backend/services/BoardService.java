package com.quarto.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quarto.backend.models.customs.Method;
import com.quarto.backend.models.database.Board;
import com.quarto.backend.models.requests.BoardRequest;
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

    public List<Board> getAllBoardsByName(String name) {
        if (StringUtils.isBlank(name)) {
            return boardRepository.findAll();
        }
        return boardRepository.findByNameContainingAllIgnoreCase(name);
    }

    public List<Board> getAllBoardsByNameExceptId(String name, String id) {
        return boardRepository.findByNameAndIdNot(name, id);
    }

    public Board getBoardByName(String name) {
        return boardRepository.findFirstByName(name).orElse(null);
    }

    public Board createBoard(Board board) {
        return boardRepository.save(board);
    }

    public void removeBoard(String id) {
        boardRepository.deleteById(id);
    }

    public Board mapToBoard(BoardRequest boardRequest, Method method) {
        boolean condition = (Method.PATCH.equals(method))
                ? (StringUtils.equals(boardRequest.getName(), null)
                        && StringUtils.equals(boardRequest.getDescription(), null))
                : (StringUtils.equals(boardRequest.getName(), null)
                        || StringUtils.equals(boardRequest.getDescription(), null));
        if (condition) {
            return null;
        }
        Board board = new Board();
        board.setName(boardRequest.getName());
        board.setDescription(boardRequest.getDescription());
        return board;
    }
}
