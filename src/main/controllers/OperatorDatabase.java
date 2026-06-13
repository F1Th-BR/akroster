package controllers;

import structures.BTree;
import structures.KMP;
import structures.PatriciaTrie;
import models.Operator;
import models.Skill;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible to manage both the BTree
 * and the PATRICIA trie. Provides insert, delete, search,
 * and KMP based skill keyword search. 
 * 
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */

public class OperatorDatabase {
    /* ===== ATTRIBUTES ===== */
    private final BTree<String, Operator> mainRegistry;
    private final PatriciaTrie nameTrie;

    /* ===== CONSTRUCTORS ===== */
    public OperatorDatabase(int bTreeOrder) {
        this.mainRegistry = new BTree<>(bTreeOrder);
        this.nameTrie = new PatriciaTrie();
    }

    /* ===== CRUD ===== */

    /**
     * Inserts an operator into the BTree and PatriciaTrie.
     *
     * @param op the operator to insert
     */
    public void insertOperator(Operator op) {
        if (op == null || op.getName() == null)
            return;
        mainRegistry.insert(op.getName(), op);
        nameTrie.insert(op.getName());
    }

    /**
     * Removes an operator from both structures by name.
     *
     * @param name the operator's name
     */
    public void deleteOperator(String name) {
        if (name == null || name.isEmpty())
            return;
        mainRegistry.remove(name);
        nameTrie.remove(name);
    }

    /**
     * Exact-match lookup via BTree.
     *
     * @param name the operator's name
     * @return the operator, or null if not found
     */
    public Operator getOperatorExact(String name) {
        return mainRegistry.search(name);
    }

    /**
     * Returns the elements in the BTree
     * 
     * @return the list of operators in the BTree
     */
    public List<Operator> inOrderValues() {
        return mainRegistry.inOrderValues();
    }

    /* ===== UPGRADES ===== */

    /**
     * Increments all skills of an operator up to level 7.
     *
     * @param name the operator's name
     */
    public void upgradeGlobalSkills(String name) {
        Operator op = mainRegistry.search(name);
        if (op != null)
            op.incrementGlobalSkillLevel();
        else
            System.out.println("Operator not found.");
    }

    /**
     * Increments the mastery of a specific skill slot.
     *
     * @param name      the operator's name
     * @param skillSlot the skill slot to train (1, 2 or 3)
     */
    public void upgradeOperatorMastery(String name, int skillSlot) {
        Operator op = mainRegistry.search(name);
        if (op != null)
            op.trainMastery(skillSlot);
        else
            System.out.println("Operator not found.");
    }

    /* ===== SEARCH ===== */

    /**
     * Searches operators whose skill names or descriptions
     * contain the given keyword, using KMP string matching.
     *
     * @param allOperators the list of operators to search through
     * @param keyword      the search term
     * @return list of matching operators
     */
    public List<Operator> searchBySkillKeyword(List<Operator> allOperators, String keyword) {
        List<Operator> matches = new ArrayList<>();
        for (Operator op : allOperators) {
            for (Skill skill : op.getSkills()) {
                if (!KMP.KMPSearch(skill.getName().toLowerCase(), keyword.toLowerCase()).isEmpty() ||
                    !KMP.KMPSearch(skill.getDescription().toLowerCase(), keyword.toLowerCase()).isEmpty()) {
                    matches.add(op);
                    break;
                }
            }
        }
        return matches;
    }
}