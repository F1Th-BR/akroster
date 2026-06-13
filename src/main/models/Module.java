package models;

import java.util.Objects;

/**
 * Class responsible to define what a module is.
 * Used by Operator.
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class Module {
    /* ===== ATTRIBUTES ===== */
    private final String name;
    private final String subClassCode;
    private final String modType;
    private int level;
    private final String description;

    /* ===== METHODS ===== */
    /* ===== CONSTRUCTORS ===== */
    public Module(String name, String subClassCode, String modType, int level, String description) {
        this.name = name;
        this.subClassCode = subClassCode;
        this.modType = modType;
        this.level = level;
        this.description = description;
    }

    /* ===== GETTERS & SETTERS ===== */
    /**
     * A method that returns the name of the module
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * A method that returns the subclass code
     * (e.g. LOR, CCR, PHY, XAH)
     * 
     * @return subClassCode
     */
    public String getSubClassCode() {
        return subClassCode;
    }

    /**
     * A method that returns the module type
     * (e.g. X, Y, alpha)
     * 
     * @return modType
     */
    public String getModType() {
        return modType;
    }

    /**
     * A method that returns the level of the module
     * 
     * @return level
     */
    public int getLevel() {
        return level;
    }

    /**
     * A method that returns the description of the
     * module
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }
    
    /* ===== OTHER METHODS ===== */
    public void incrementLevel() {
        if (this.level < 3) {
            this.level += 1;
        }
    }

    /* ===== OVERRIDE METHODS ===== */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return Objects.equals(name, module.name) && 
               Objects.equals(subClassCode, module.subClassCode) && 
               Objects.equals(modType, module.modType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, subClassCode, modType);
    }

    @Override
    public String toString() {
        return "Module{" +
                "name='" + name + '\'' +
                ", type='" + modType + '\'' +
                ", level=" + level +
                '}';
    }
}
