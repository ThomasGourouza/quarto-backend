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

import com.quarto.backend.models.customs.Method;
import com.quarto.backend.models.database.Board;
import com.quarto.backend.models.requests.BoardRequest;
import com.quarto.backend.services.BoardService;

@RestController
@CrossOrigin
@RequestMapping("/boards")
public class BoardControler {
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String WRONG_JSON = "Wrong JSON";
    private static final String NAME_EXISTS = "Name already exists";
    private static final String NOT_FOUND = "Tree doesn't exist";

    @Autowired
    private BoardService boardService;

    @GetMapping("/")
    ResponseEntity<List<Board>> getAllBoards() {
        return new ResponseEntity<>(boardService.getAllBoards(), HttpStatus.OK);
    }

    @GetMapping("/search")
    ResponseEntity<List<Board>> getAllBoardsByName(@RequestParam String name) {
        return new ResponseEntity<>(boardService.getAllBoardsByName(name), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    ResponseEntity<Board> getBoard(@PathVariable String id) {
        Board board = boardService.getBoard(id);
        if (board == null) {
            return headers(NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(board, HttpStatus.OK);
    }

    @PostMapping("/")
    ResponseEntity<Board> createBoard(@RequestBody BoardRequest boardRequest) {
        Board board = boardService.mapToBoard(boardRequest, Method.POST);
        if (board == null) {
            return headers(WRONG_JSON, HttpStatus.BAD_REQUEST);
        }
        Board boardAlreadySaved = boardService.getBoardByName(board.getName());
        if (boardAlreadySaved != null) {
            return headers(NAME_EXISTS, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(boardService.createBoard(board), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    ResponseEntity<Board> putBoard(@PathVariable String id, @RequestBody BoardRequest boardRequest) {
        Board newBoard = boardService.mapToBoard(boardRequest, Method.PUT);
        if (newBoard == null) {
            return headers(WRONG_JSON, HttpStatus.BAD_REQUEST);
        }
        Board boardAlreadySaved = boardService.getBoard(id);
        if (boardAlreadySaved == null) {
            return headers(NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        List<Board> sameNameBoards = boardService.getAllBoardsByNameExceptId(newBoard.getName(), id);
        if (!sameNameBoards.isEmpty()) {
            return headers(NAME_EXISTS, HttpStatus.CONFLICT);
        }
        boardAlreadySaved.setName(newBoard.getName());
        boardAlreadySaved.setDescription(newBoard.getDescription());
        return new ResponseEntity<>(boardService.createBoard(boardAlreadySaved), HttpStatus.ACCEPTED);
    }

    @PatchMapping("/{id}")
    ResponseEntity<Board> patchBoard(@PathVariable String id, @RequestBody BoardRequest boardRequest) {
        Board newBoard = boardService.mapToBoard(boardRequest, Method.PATCH);
        if (newBoard == null) {
            return headers(WRONG_JSON, HttpStatus.BAD_REQUEST);
        }
        Board boardAlreadySaved = boardService.getBoard(id);
        if (boardAlreadySaved == null) {
            return headers(NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        if (StringUtils.isNotBlank(newBoard.getName())) {
            List<Board> sameNameBoards = boardService.getAllBoardsByNameExceptId(newBoard.getName(), id);
            if (!sameNameBoards.isEmpty()) {
                return headers(NAME_EXISTS, HttpStatus.CONFLICT);
            }
            boardAlreadySaved.setName(newBoard.getName());
        }
        if (StringUtils.isNotBlank(newBoard.getDescription())) {
            boardAlreadySaved.setDescription(newBoard.getDescription());
        }
        return new ResponseEntity<>(boardService.createBoard(boardAlreadySaved), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Board> removeBoard(@PathVariable String id) {
        Board boardAlreadySaved = boardService.getBoard(id);
        if (boardAlreadySaved == null) {
            return headers(NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        boardService.removeBoard(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private ResponseEntity<Board> headers(String message, HttpStatus status) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ERROR_MESSAGE, message);
        return new ResponseEntity<>(null, responseHeaders, status);
    }

}
