package sorting;

import java.util.Comparator;
import java.util.List;

/**
 * A sort interface to be used by sort algorithms.
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public interface Sorter<T> {
    /**
     * Sorts a list in-place according to the given comparator.
     *
     * @param list       the list to sort
     * @param comparator defines the ordering criterion
     */
    void sort(List<T> list, Comparator<T> comparator);
}