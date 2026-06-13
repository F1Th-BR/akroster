package sorting;

import models.Operator;

import java.util.Comparator;

/**
 * Class responsible to define comparator methods for the
 * sort methods.
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class OperatorComparators {

    /* ===== CONSTRUCTORS ===== */
    private OperatorComparators() {}

    /* ===== COMPARATORS ===== */

    /**
     * Alphabetical order by operator name (A → Z).
     */
    public static Comparator<Operator> byName() {
        return Comparator.comparing(Operator::getName);
    }

    /**
     * Descending rarity (6★ first, 1★ last).
     */
    public static Comparator<Operator> byRarity() {
        return Comparator.comparingInt(Operator::getRarity).reversed();
    }

    /**
     * Descending level (highest level first).
     */
    public static Comparator<Operator> byLevel() {
        return Comparator.comparingInt(Operator::getLevel).reversed();
    }

    /**
     * Descending elite/promotion (E2 first, E0 last).
     */
    public static Comparator<Operator> byElite() {
        return Comparator.comparingInt(Operator::getElite).reversed();
    }

    /**
     * Ascending DP cost (cheapest to deploy first).
     */
    public static Comparator<Operator> byDpCost() {
        return Comparator.comparingInt(Operator::getDpCost);
    }

    /**
     * Descending elite, then descending level within the same elite tier.
     * Useful for a "most progressed" ordering.
     */
    public static Comparator<Operator> byEliteThenLevel() {
        return Comparator.comparingInt(Operator::getElite).reversed()
                         .thenComparingInt(Operator::getLevel).reversed();
    }

    /**
     * Descending rarity, then alphabetical by name as a tiebreaker.
     * Mirrors the default ordering used in most Arknights roster views.
     */
    public static Comparator<Operator> byRarityThenName() {
        return Comparator.comparingInt(Operator::getRarity).reversed()
                         .thenComparing(Operator::getName);
    }
}