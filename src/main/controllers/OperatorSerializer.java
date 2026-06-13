package controllers;

import models.Module;
import models.Operator;
import models.Skill;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Class responsible to parse the data for import or export
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class OperatorSerializer implements OperatorExporter, OperatorImporter{

    /* ===== CONSTRUCTORS ===== */
    public OperatorSerializer() {}

    /* ===== EXPORT ===== */
    @Override
    public void export(List<Operator> operators, String path) {
        exportDat(operators, path);
    }

    /**
     * Exports a list of operators to a tagged, semicolon-delimited .dat file.
     * Full fidelity — every field is preserved for later reimport.
     *
     * Format per operator:
     *   [OPERATOR]
     *   name=...;class=...;subclass=...;rarity=...;elite=...;level=...;dp=...;potential=...;defaultSkill=...;defaultModule=...;
     *   [SKILLS]
     *   slot=...;level=...;name=...;type=...;recovery=...;desc=...;
     *   [MODULES]
     *   subclass=...;type=...;level=...;name=...;desc=...;
     *   [END_OPERATOR]
     *
     * @param allOperators the operators to export
     * @param path         the output file path
     */
    private static void exportDat(List<Operator> allOperators, String path) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(path))) {
            for (Operator op : allOperators) {
                w.write("[OPERATOR]\n");
                w.write(
                    "name="          + op.getName()               + ";" +
                    "class="         + op.getOperatorClass()      + ";" +
                    "subclass="      + op.getOperatorSubClass()   + ";" +
                    "rarity="        + op.getRarity()             + ";" +
                    "elite="         + op.getElite()              + ";" +
                    "level="         + op.getLevel()              + ";" +
                    "dp="            + op.getDpCost()             + ";" +
                    "potential="     + op.getPotential()          + ";" +
                    "defaultSkill="  + op.getDefaultSkillSlot()   + ";" +
                    "defaultModule=" + op.getDefaultModuleIndex() + ";\n"
                );

                w.write("[SKILLS]\n");
                for (Skill s : op.getSkills()) {
                    w.write(
                        "slot="     + s.getSkillSlot()   + ";" +
                        "level="    + s.getLevel()       + ";" +
                        "name="     + s.getName()        + ";" +
                        "type="     + s.getSkillType()   + ";" +
                        "recovery=" + s.getRecovery()    + ";" +
                        "desc="     + s.getDescription() + ";\n"
                    );
                }

                w.write("[MODULES]\n");
                for (Module m : op.getModules()) {
                    w.write(
                        "subclass=" + m.getSubClassCode() + ";" +
                        "type="     + m.getModType()      + ";" +
                        "level="    + m.getLevel()        + ";" +
                        "name="     + m.getName()         + ";" +
                        "desc="     + m.getDescription()  + ";\n"
                    );
                }

                w.write("[END_OPERATOR]\n\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ===== IMPORT ===== */
    @Override
    public List<Operator> importData(String path, OperatorDatabase db,
            Function<Operator, ConflictResolution> onConflict) {
        return importDat(path, db, onConflict);
    }

    /**
     * Imports operators from a .dat file produced by exportDat().
     * When a name collision is found, the onConflict callback is invoked
     * so the caller can prompt the user and return a resolution.
     *
     * @param path       path to the .dat file
     * @param db         the database to insert into
     * @param onConflict called with the incoming operator on name collision;
     *                   must return SKIP or OVERWRITE
     * @return list of operators that were actually inserted
     */
    private static List<Operator> importDat(String path, OperatorDatabase db,
            Function<Operator, ConflictResolution> onConflict) {
        List<Operator> imported = new ArrayList<>();

        try (BufferedReader r = new BufferedReader(new FileReader(path))) {
            String line;
            Operator current = null;

            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                switch (line) {
                    case "[OPERATOR]" -> current = null;
                    case "[SKILLS]", "[MODULES]" -> { /* section markers, no action needed */ }
                    case "[END_OPERATOR]" -> {
                        if (current == null) break;

                        Operator existing = db.getOperatorExact(current.getName());
                        if (existing != null) {
                            ConflictResolution resolution = onConflict.apply(current);
                            if (resolution == ConflictResolution.SKIP)
                                break;
                            db.deleteOperator(current.getName());
                        }

                        db.insertOperator(current);
                        imported.add(current);
                        current = null;
                    }
                    default -> {
                        if (line.startsWith("name=") && current == null)
                            current = parseOperatorLine(line);
                        else if (line.startsWith("slot=") && current != null)
                            current.addSkill(parseSkillLine(line));
                        else if (line.startsWith("subclass=") && current != null)
                            current.addModule(parseModuleLine(line));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imported;
    }

    /* ===== PRIVATE PARSING ===== */

    private static Operator parseOperatorLine(String line) {
        String name       = extractField(line, "name");
        String opClass    = extractField(line, "class");
        String opSubClass = extractField(line, "subclass");
        int rarity        = extractInt(line, "rarity",        1);
        int elite         = extractInt(line, "elite",         0);
        int level         = extractInt(line, "level",         1);
        int dp            = extractInt(line, "dp",            1);
        int potential     = extractInt(line, "potential",     0);
        int defaultSkill  = extractInt(line, "defaultSkill",  -1);
        int defaultModule = extractInt(line, "defaultModule", -1);

        Operator op = new Operator.Builder(name, opClass)
                .operatorSubClass(opSubClass)
                .rarity(rarity)
                .elite(elite)
                .level(level)
                .dpCost(dp)
                .potential(potential)
                .build();

        op.setDefaultSkillSlot(defaultSkill);
        op.setDefaultModuleIndex(defaultModule);
        return op;
    }

    private static Skill parseSkillLine(String line) {
        int    slot     = extractInt(line,   "slot",     1);
        String level    = extractField(line, "level");
        String name     = extractField(line, "name");
        String type     = extractField(line, "type");
        String recovery = extractField(line, "recovery");
        String desc     = extractField(line, "desc");
        return new Skill(slot, level, name, desc, type, recovery);
    }

    private static Module parseModuleLine(String line) {
        String subclass = extractField(line, "subclass");
        String type     = extractField(line, "type");
        int    level    = extractInt(line,   "level", 1);
        String name     = extractField(line, "name");
        String desc     = extractField(line, "desc");
        return new Module(name, subclass, type, level, desc);
    }

    /**
     * Extracts the string value for a key from a "key=value;key=value;" line.
     * Returns an empty string if the key is not found.
     */
    private static String extractField(String line, String key) {
        String search = key + "=";
        int start = line.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = line.indexOf(";", start);
        if (end == -1) end = line.length();
        return line.substring(start, end).trim();
    }

    /**
     * Extracts an int value for a key from a "key=value;" line.
     * Returns defaultValue if the key is missing or unparseable.
     */
    private static int extractInt(String line, String key, int defaultValue) {
        String val = extractField(line, key);
        if (val.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}