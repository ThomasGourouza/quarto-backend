package com.vegetation.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vegetation.backend.models.Tree;
import com.vegetation.backend.repositories.TreeRepository;

@Service
public class TreeService {
    @Autowired
    private TreeRepository treeRepository;

    public List<Tree> getAllTrees() {
        return treeRepository.findAll();
    }

    public Tree getTree(String id) {
        return treeRepository.findById(id).orElse(null);
    }

    public Tree createTree(Tree tree) {
        // récupérer la liste de tous les trees qu'on a dans la DB
        List<Tree> trees = getAllTrees();
        // vérifier que aucun tree n'a le même name que celui qu'on essaye d'enregistrer
        boolean isTreeInDb = trees.stream()
                .anyMatch(treeAlreadySaved -> treeAlreadySaved.getName().equals(tree.getName()));
        // si l'arbre est déjà dans la DB
        if (isTreeInDb) {
            // on ne sauvegarde pas
            return null;
        }
        {
            return treeRepository.save(tree);
        }
    }

    public Tree updateTree(String id, Tree newTree) {
        Tree oldTree = treeRepository.findById(id).orElse(null);
        if (oldTree != null) {
            // récupérer la liste de tous les trees sauf celui qu'on cherche à modifier
            List<Tree> trees = getAllTrees().stream().filter(t -> 
                t.getId() != oldTree.getId()
            ).collect(Collectors.toList());
            // vérifier que aucun autre tree n'a le même name que celui qu'on cherche à modifier
            boolean isTreeInDb = trees.stream()
                    .anyMatch(treeAlreadySaved -> treeAlreadySaved.getName().equals(newTree.getName()));
            // si l'arbre est déjà dans la DB
            if (isTreeInDb) {
                // on ne sauvegarde pas
                return null;
            } else {
                oldTree.setName(newTree.getName());
                oldTree.setDescription(newTree.getDescription());
                return treeRepository.save(oldTree);
            }
        }
        return null;
    }

    public String removeTree(String id) {
        treeRepository.findById(id).ifPresent(tree -> treeRepository.deleteById(id));
        return "Tree " + id + " deleted.";
    }
}
