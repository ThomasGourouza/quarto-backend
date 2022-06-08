package com.vegetation.backend.controlers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.vegetation.backend.models.Tree;
import com.vegetation.backend.services.TreeService;

@RestController
@CrossOrigin
@RequestMapping("/tree")
public class TreeControler {
    @Autowired
    private TreeService treeService;

    @GetMapping("/")
    ResponseEntity<List<Tree>> getAllTrees() {
        return new ResponseEntity<>(treeService.getAllTrees(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    ResponseEntity<Tree> getTree(@PathVariable String id) {
        Tree tree = treeService.getTree(id);
        if (tree != null) {
            return new ResponseEntity<>(tree, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/")
    ResponseEntity<Tree> createTree(@RequestBody Tree tree) {
        Tree newTree = treeService.createTree(tree);
        if (newTree != null) {
            return new ResponseEntity<>(newTree, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/{id}")
    ResponseEntity<Tree> updateTree(@PathVariable String id, @RequestBody Tree tree) {
        Tree treeAlreadySaved = treeService.getTree(id);
        if (treeAlreadySaved != null) {
            Tree updatedTree = treeService.updateTree(id, tree);
            if (updatedTree != null) {
                return new ResponseEntity<>(updatedTree, HttpStatus.ACCEPTED);
            }
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<String> removeTree(@PathVariable String id) {
        Tree treeAlreadySaved = treeService.getTree(id);
        if (treeAlreadySaved != null) {
            return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
