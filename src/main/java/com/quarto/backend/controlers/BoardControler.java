package com.quarto.backend.controlers;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.quarto.backend.models.Board;
import com.quarto.backend.services.BoardService;

@RestController
@CrossOrigin
@RequestMapping("/board")
public class BoardControler {
    @Autowired
    private BoardService boardService;

    @GetMapping("/")
    ResponseEntity<List<Board>> getAllBoards() {
        return new ResponseEntity<>(boardService.getAllBoards(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    ResponseEntity<Board> getBoard(@PathVariable String id) {
        Board board = boardService.getBoard(id);
        if (board != null) {
            return new ResponseEntity<>(board, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/")
    ResponseEntity<Board> createBoard(@RequestBody Board board) {
        Board newBoard = boardService.createBoard(board);
        if (newBoard != null) {
            return new ResponseEntity<>(newBoard, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @PutMapping("/{id}")
    ResponseEntity<Board> updateBoard(@PathVariable String id, @RequestBody Board board) {
        Board boardAlreadySaved = boardService.getBoard(id);
        if (boardAlreadySaved != null) {
            Board updatedBoard = boardService.updateBoard(id, board);
            if (updatedBoard != null) {
                return new ResponseEntity<>(updatedBoard, HttpStatus.ACCEPTED);
            }
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<String> removeBoard(@PathVariable String id) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("name", "value");
        if (boardService.removeBoard(id)) {
            return new ResponseEntity<>("Deleted", responseHeaders, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>("This board doesn't exist", responseHeaders, HttpStatus.NOT_FOUND);
    }

}
