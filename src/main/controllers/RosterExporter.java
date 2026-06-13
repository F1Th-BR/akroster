package controllers;

import models.Module;
import models.Operator;
import models.Skill;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Class responsible to write an .md file containing the main information
 * about the operators inside the BTree
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class RosterExporter implements OperatorExporter{

    /* ===== CONSTRUCTORS ===== */
    public RosterExporter() {}

    /* ===== EXPORT ===== */
    @Override
    public void export(List<Operator> operators, String path) {
        exportRosterMd(operators, path);
    }

    /**
     * Exports a human-readable markdown roster table.
     * Each row shows only the operator's default skill and default module.
     * If no default is set, falls back to the last skill and first module.
     *
     * @param allOperators the operators to include in the roster
     * @param path         the output file path
     */
    private static void exportRosterMd(List<Operator> allOperators, String path) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(path))) {
            w.write("| Operator | Class | Subclass | Elite | Level | Skill | Skill Description | Module | Module Level | Module Description | Potential | DP Cost |\n");
            w.write("| :------- | :---: | :------: | :---: | :---: | :---: | :---------------- | :----: | :----------: | :----------------- | :-------: | :-----: |\n");

            for (Operator op : allOperators) {
                String skillCol  = "-";
                String skillDesc = "-";
                String moduleCol  = "-";
                String moduleLvl  = "-";
                String moduleDesc = "-";

                Skill defaultSkill = resolveDefaultSkill(op);
                if (defaultSkill != null) {
                    skillCol  = "S" + defaultSkill.getSkillSlot() + "-" + defaultSkill.getLevel();
                    skillDesc = defaultSkill.getDescription();
                }

                Module defaultModule = resolveDefaultModule(op);
                if (defaultModule != null) {
                    moduleCol  = defaultModule.getSubClassCode() + "-" + defaultModule.getModType();
                    moduleLvl  = String.valueOf(defaultModule.getLevel());
                    moduleDesc = defaultModule.getDescription();
                }

                w.write("| " + op.getName()              +
                        " | " + op.getOperatorClass()    +
                        " | " + op.getOperatorSubClass() +
                        " | E" + op.getElite()           +
                        " | " + op.getLevel()            +
                        " | " + skillCol                 +
                        " | " + skillDesc                +
                        " | " + moduleCol                +
                        " | " + moduleLvl                +
                        " | " + moduleDesc               +
                        " | " + op.getPotential()        +
                        " | **" + op.getDpCost() + "** |\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ===== PRIVATE HELPERS ===== */

    /**
     * Returns the operator's default skill if set,
     * otherwise falls back to the last skill in the list.
     */
    private static Skill resolveDefaultSkill(Operator op) {
        Skill s = op.getDefaultSkill();
        if (s != null) return s;
        List<Skill> skills = op.getSkills();
        return skills.isEmpty() ? null : skills.get(skills.size() - 1);
    }

    /**
     * Returns the operator's default module if set,
     * otherwise falls back to the first module in the list.
     */
    private static Module resolveDefaultModule(Operator op) {
        Module m = op.getDefaultModule();
        if (m != null) return m;
        List<Module> modules = op.getModules();
        return modules.isEmpty() ? null : modules.get(0);
    }
}