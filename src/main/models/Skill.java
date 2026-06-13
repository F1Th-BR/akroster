package models;

import java.util.Objects;

/**
 * Class responsible to define the skills of an operator.
 * Used by Operator.
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class Skill {
    /* ===== ATTRIBUTES ===== */
    private final int skillSlot;
    private final String name;
    private final String skillType;

    private String level;
    private String description;
    private String recovery;

    /* ===== METHODS ===== */
    /* ===== CONSTRUCTORS ===== */
    public Skill(int skillSlot, String level, String name, String description, String skillType, String recovery) {
        this.skillSlot = skillSlot;
        this.level = level;
        this.name = name;
        this.description = description;
        this.skillType = skillType;
        this.recovery = recovery;
    }

    /* ===== GETTERS & SETTERS ===== */
    /**
     * A method that returns the slot of the skill (1, 2 
     * or 3)
     * 
     * @return skillSlot
     */
    public int getSkillSlot() {
        return skillSlot;
    }

    /**
     * A method that returns the level of the skill.
     * The level also serves to mastery (Mi, i in [1,3]),
     * hence why its type is String
     * 
     * @return level
     */
    public String getLevel() {
        return level;
    }

    /**
     * A method that returns the name of the skill
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * A method that retuns the description of a skill
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * A method that returns the type of the skill 
     * (auto, manual or passive)
     * 
     * @return skillType
     */
    public String getSkillType() {
        return skillType;
    }

    /**
     * A method that returns the form of recovery of the
     * skill (e.g. Offensive Recovery, Auto Recovery).
     * 
     * Alternatively, if the skillType is "passive", it 
     * returns the amount of time the skill lasts
     * (e.g. 18 seconds) 
     * 
     * @return recovery
     */
    public String getRecovery() {
        return recovery;
    }

    /**
     * A method that sets/updates the description of a 
     * skill
     * 
     * @param description the description of the skill.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /* ===== OTHER METHODS ===== */
    /**
     * A method to increment the level of a skill from
     * 1 to 7. If a skill is upgraded beyond 7, method
     * incrementMastery() is called.
     * 
     */
    public void incrementLevel() {
        try {
            int currentNumericLevel = Integer.parseInt(this.level);

            if (currentNumericLevel < 7) {
                this.level = String.valueOf(currentNumericLevel + 1);
            } else if (currentNumericLevel == 7) {
                this.level = "M1";
            }
        } catch (NumberFormatException e) {
            incrementMastery();
        }
    }

    /**
     * A method that increments the mastery of a skill.
     * 
     */
    private void incrementMastery() {
        if (this.level.startsWith("M") && this.level.length() == 2) {
            int currentMasteryLevel = Character.getNumericValue(this.level.charAt(1));

            if (currentMasteryLevel < 3) {
                this.level = "M" + (currentMasteryLevel + 1);
            }
        }
    }

    /* ===== OVERRIDE METHODS ===== */
    
    /**
     * Override of the equals method
     * 
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Skill skill = (Skill) o;

        return Objects.equals(name, skill.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, skillSlot, skillType);
    }

    @Override
    public String toString() {
        if (level.length() == 1)
            return "S"+skillSlot+"-"+level+": "+"'"+name+"'";
        else
            return "S"+skillSlot+level+": "+"'"+name+"'";
    }
}
