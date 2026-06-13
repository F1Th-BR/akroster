package controllers;

import models.Operator;

import java.util.List;

/**
 * Exporter inteface
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public interface OperatorExporter {
    /**
     * Exports a list of operators to a file at the given path.
     * The format is determined by the implementing class.
     *
     * @param operators the operators to export
     * @param path      the output file path
     */
    void export(List<Operator> operators, String path);
}