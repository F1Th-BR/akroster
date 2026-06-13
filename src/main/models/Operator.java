package models;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Class responsible to define what an operator is.
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class Operator implements Comparable<Operator>{
    /* ===== ATRIBUTES ===== */
    private String name;
    private int rarity;
    private String operatorClass;
    private String operatorSubClass;
    private int elite;
    private int level;
    private int dpCost;
    private int potential;
    private List<Skill> skills;
    private List<Module> modules;
    private int defaultSkillSlot = -1;
    private int defaultModuleIndex = -1;

    /* ===== METHODS ===== */
    /* ===== CONSTRUCTORS ===== */
    private Operator(Builder builder) {
        this.name = builder.name;
        this.rarity = builder.rarity;
        this.operatorClass = builder.operatorClass;
        this.operatorSubClass = builder.operatorSubClass;
        this.dpCost = builder.dpCost;
        this.level = builder.level;
        this.elite = builder.elite;
        this.potential = builder.potential;
        this.skills = new ArrayList<>();
        this.modules = new ArrayList<>();
    }

    /* ===== GETTERS & SETTERS ===== */
        /* ===== GETTERS ===== */
    /**
     * A method that returns the default module of the operator
     * 
     * @return the equiped module of the operator. returns null
     * if no module is equiped
     */
    public Module getDefaultModule() {
        if (defaultModuleIndex >= 0 && defaultModuleIndex < modules.size())
            return modules.get(defaultModuleIndex);
        
        return null;
    }

    /**
     * A methods that returns the index of the default
     * module (module slot in the list)
     * 
     * @return
     */
    public int getDefaultModuleIndex() {
        return defaultModuleIndex;
    }

    /**
     * A method that returns the default skill of the operator
     * 
     * @return the default skill of the operator
     */
    public Skill getDefaultSkill() {
        for (Skill s : skills) {
            if (s.getSkillSlot() == defaultSkillSlot)
                return s;
        }

        return null;
    }

    /**
     * A method that returns the index of the default 
     * skill (skill slot)
     * 
     * @return the index of the default skill
     */
    public int getDefaultSkillSlot() {
        return defaultSkillSlot;
    }

    /**
     * A method that returns the DP (Deployment Points)
     * cost of the operator
     * 
     * @return DP cost of the operator
     */
    public int getDpCost() {
        return dpCost;
    }

    /**
     * A method that returns the rank of the operator
     * 
     * @return the rank of the operator
     */
    public int getElite() {
        return elite;
    }

    /**
     * A method that returns the level of the operator
     * 
     * @return the level of the operator
     */
    public int getLevel() {
        return level;
    }

    /**
     * A method that returns the modules of the operator
     * 
     * @return List of modules of the operator
     */
    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }

    /**
     * A method that returns the name of the operator
     * 
     * @return name of the operator
     */
    public String getName() {
        return name;
    }

    /**
     * A method that returns the class of the operator
     * 
     * @return class of the operator
    */
    public String getOperatorClass() {
        return operatorClass;
    }

    /**
     * A method that returns the subclass/specialization
     * of the operator
     * 
     * @return subclass of the operator
    */
    public String getOperatorSubClass() {
        return operatorSubClass;
    }

    /**
     * A method that returns the potential of the
     * operator.
     * 
     * @return the potential of the operator
     */
    public int getPotential() {
        return potential;
    }

    /**
     * A method that returns the rarity of the operador
     * (1 to 6)
     * 
     * @return rarity of the operator
     */
    public int getRarity() {
        return rarity;
    }

    /**
     * A method that returns the skills of the operator
     * 
     * @return List of skills of the operator
     */
    public List<Skill> getSkills() {
        return new ArrayList<>(skills);
    }

        /* ===== SETTERS ===== */
    /**
     * A method that sets the default module of the
     * operator
     * 
     * @param defaultModuleIndex the index of the module
     * int the list
     */
    public void setDefaultModuleIndex(int defaultModuleIndex) {
        this.defaultModuleIndex = defaultModuleIndex;
    }
    
    /**
     * A method that sets the default skill of the operator
     * 
     * @param defaultSkillSlot the index of the skill
     * in the list
     */
    public void setDefaultSkillSlot(int defaultSkillSlot) {
        this.defaultSkillSlot = defaultSkillSlot;
    }

    /**
     * A method that sets the Deployment Cost (DP)
     * of the operator
     * 
     * @param dpCost the operator's deployment cost
     */
    public void setDpCost(int dpCost) {
        this.dpCost = dpCost;
    }

    /**
     * A method that updates the Elite level
     * (rank) of the operator.
     * 
     * @param elite the operator's rank
     */
    public void setElite(int elite) {
        this.elite = elite;
    }

    /**
     * A method that sets the level of the operator.
     * 
     * @param level the level of the operator
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /* ===== ADD METHODS ===== */
    /**
     * A method to add a skill to a character
     * 
     * @param skill the skill to be added.
     * 
     * For more details on "skill", check
     * the Skill class.
     */
    public void addSkill(Skill skill) {
        this.skills.add(skill);
    }
    
    /**
     * A method to add a module to an operator
     * 
     * @param module the module to be added.
     * 
     * For more details on "module", check
     * the Module class. 
     */
    public void addModule(Module module) {
        this.modules.add(module);
    }

    /* ===== OTHER METHODS ===== */
    /**
     * A public method to increment the operator's
     * elite level.
     * 
     */
    public void incrementElite() {
        int rarity = this.rarity;
        int elite = this.elite;

        if (rarity > 2 && elite == 0)
            this.elite++;
        else if (rarity > 3 && elite == 1)
            this.elite++;
    }

    /**
     * A public method to increase the operator's level
     * in exacty one level.
     * 
     */
    public void incrementLevel() {
        int rarityLevel = this.rarity;
        int eliteLevel = this.elite;
        int maxLevel = 0;

        switch (eliteLevel) {
            case 0:
                switch (rarityLevel) {
                    case 1, 2:
                        maxLevel = 30;
                        break;
                    case 3:
                        maxLevel = 40;
                        break;
                    case 4:
                        maxLevel = 45;
                        break;
                    case 5, 6:
                        maxLevel = 50;
                        break; 
                    default:
                        break;
                }
                break;
            case 1:
                switch (rarityLevel) {
                    case 3:
                        maxLevel = 55;
                        break;
                    case 4:
                        maxLevel = 60;
                        break;
                    case 5:
                        maxLevel = 70;
                        break;
                    case 6:
                        maxLevel = 80;
                        break;                
                    default:
                        break;
                }
                break;
            case 2:
                switch (rarityLevel) {
                    case 4:
                        maxLevel = 70;
                        break;
                    case 5:
                        maxLevel = 80;
                        break;
                    case 6:
                        maxLevel = 90;
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        if (this.level < maxLevel)
            this.level++;
    }

    /**
     * A public method that increase the level of the
     * operator in multiple levels. 
     * 
     * Uses incrementLevel()
     * 
     * @param amount the amount of levels to be increased
     */
    public void incrementLevel(int amount) {
        for (int i = 0; i < amount; i++)
            this.incrementLevel();
    }

    /**
     * A public method that increases the operator's
     * potential in 1.
     * 
     */
    public void incrementPotential() {
        if (this.potential < 6)
            this.potential++;
        else
            return;
    }

    /**
     * A public method that increases the operator's
     * potentials in more than one unit until a max
     * of 6.
     * 
     * @param amount the amount of potentials to be 
     * increased
     */
    public void incrementPotential(int amount) {
        for (int i = 0; i < amount; i++)
            incrementPotential();
    }

    /**
     * A public method to increment the skills level
     * up to 7.
     * 
     */
    public void incrementGlobalSkillLevel() {
        for (Skill s : skills) {
            try {
                int currentLevel = Integer.parseInt(s.getLevel());
                if (currentLevel < 7)
                    s.incrementLevel();
            } catch (NumberFormatException e) {

            }
        }
    }

    /**
     * A public method to train a skill x's mastery. 
     * 
     * @param skillSlot the slot of the skill
     */
    public void trainMastery(int skillSlot) {
        for (Skill s : skills) {
            if (s.getSkillSlot() == skillSlot) {
                s.incrementLevel();
                break;
            }
        }
    }

    /* ===== INTERFACE AND OVERRIDE METHODS ===== */
    /**
     * A method to compare two operators
     * 
     * @param other the second operator in the comparison
     * @return negative value, if this object precedes 
     * other, zero if they're equal and a positive value 
     * if this object succedes other
     */
    @Override
    public int compareTo(Operator other) {
        return this.name.compareTo(other.getName());
    }

    /**
     * A method to check if this object is equal to
     * another object
     * 
     * @param o the compared object
     * @return true, if the compared object is equal to this.
     * False, if not 
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        
        Operator operator = (Operator) o;

        return rarity == operator.rarity &&
            Objects.equals(name, operator.name) &&
            Objects.equals(operatorClass, operator.operatorClass) &&
            Objects.equals(operatorSubClass, operator.operatorSubClass);
    }

    /**
     * A method that generates a hash code for an Operator
     * object.
     * 
     * @return this object's hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, rarity, operatorClass, operatorSubClass);
    }

    /**
     * A toString method.
     * 
     * @return this object in String format
     */
    @Override
    public String toString() {
        return "Operator{" +
                "name='"+name + '\''+
                ", rarity="+rarity+ 
                "*, class='"+operatorClass+
                '\'' + ", subclass='"+operatorSubClass+
                ", level='E"+elite+"lv"+level+
                '\'' + ", DP cost="+dpCost+
                ", potential="+potential+
                "}";
    }

    /* ===== INTERNAL CLASSES ===== */
    public static class Builder {
        /* ===== ATTRIBUTES ===== */
        private final String name;
        private final String operatorClass;

        private int rarity = 1;
        private int level = 1;
        private int elite = 0;
        private String operatorSubClass = "N/A";
        private int dpCost = 1;
        private int potential = 1;

        /* ===== METHODS ===== */
        /* ===== CONSTRUCTORS ===== */
        public Builder(String name, String operatorClass) {
            this.name = name;
            this.operatorClass = operatorClass;
        }
        
        /* ===== OTHER METHODS ===== */
        /**
         * Calls the constructor of the Operator class
         * passing this object Builder as the parameter
         * and creating the Operator object
         * 
         * @return returns a new Operator object
         */
        public Operator build() {
            return new Operator(this);
        }
        
        /**
         * A method that sets the DP cost (Deployment
         * Points) of the operator in the Builder object
         * 
         * @param dpCost the Deployment Points cost of
         *               the operator
         * @return this object with the dpCost set
         */
        public Builder dpCost(int dpCost) {
            this.dpCost = dpCost;
            return this;
        }

        /**
         * A method that sets the elite level of 
         * the operator in the Builder object 
         * 
         * @param elite the rank of the operator
         * @return this object with the elite level set
         */
        public Builder elite(int elite) {
            this.elite = elite;
            return this;
        }

        /**
         * A method that sets the level of the
         * operator in the Builder object
         * 
         * @param level the level of the operator
         * @return this object with the level set
         */
        public Builder level(int level) {
            this.level = level;
            return this;
        }

        /**
         * A method that sets the subclass of the
         * operator in the Builder object
         * 
         * @param operatorSubClass the subclass of the operator
         * @return this object with the subclass set
         */
        public Builder operatorSubClass(String operatorSubClass) {
            this.operatorSubClass = operatorSubClass;
            return this;
        }


        /**
         * A method that sets the potential of the
         * operator in the Builder object
         * 
         * @param potential the potential of the operator
         * @return this object with the potential set
         */
        public Builder potential(int potential) {
            this.potential = potential;
            return this;
        }

        /**
         * A method that sets the rarity of the
         * operator in the Builder object
         * 
         * @param rarity the rarity of the operator
         * @return this object with the rarity set
         */
        public Builder rarity(int rarity) {
            this.rarity = rarity;
            return this;
        }
    } // Builder
} // Operator