package controllers;

import models.Operator;

import java.util.List;
import java.util.function.Function;

/**
 * Importer interface
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public interface OperatorImporter {
    /**
     * Imports operators from a file at the given path into the database.
     * When a name collision occurs, the onConflict callback is invoked
     * so the caller can decide whether to skip or overwrite.
     *
     * @param path       the input file path
     * @param db         the database to insert operators into
     * @param onConflict called with the incoming operator on name collision;
     *                   must return a ConflictResolution decision
     * @return the list of operators that were actually inserted
     */
    List<Operator> importData(String path, OperatorDatabase db,
        Function<Operator, ConflictResolution> onConflict);
}