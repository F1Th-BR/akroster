package sorting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A generic Merge sort implementation.
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class MergeSort<T> implements Sorter<T>{

    /* ===== CONSTRUCTORS ===== */
    public MergeSort() {}

    /* ===== PUBLIC METHODS ===== */

    /**
     * Sorts a list in-place using the merge sort algorithm.
     * Time complexity:  O(n log n) in all cases.
     * Space complexity: O(n) auxiliary space for the merge step.
     *
     * @param list       the list to be sorted
     * @param comparator defines the ordering criterion
     */
    @Override
    public void sort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() <= 1)
            return;

        mergeSort(list, 0, list.size() - 1, comparator);
    }

    /* ===== PRIVATE METHODS ===== */

    /**
     * Recursively splits the list into halves and merges them back
     * in sorted order.
     *
     * @param list       the list being sorted
     * @param left       start index of the current sublist (inclusive)
     * @param right      end index of the current sublist (inclusive)
     * @param comparator defines the ordering criterion
     */
    private void mergeSort(List<T> list, int left, int right, Comparator<T> comparator) {
        if (left >= right)
            return;

        int mid = left + (right - left) / 2;

        mergeSort(list, left, mid, comparator);
        mergeSort(list, mid + 1, right, comparator);
        merge(list, left, mid, right, comparator);
    }

    /**
     * Merges two adjacent sorted sublists — [left..mid] and [mid+1..right] —
     * back into the original list in sorted order.
     *
     * @param list       the list containing both sublists
     * @param left       start index of the left sublist
     * @param mid        end index of the left sublist
     * @param right      end index of the right sublist
     * @param comparator defines the ordering criterion
     */
    private void merge(List<T> list, int left, int mid, int right, Comparator<T> comparator) {
        // Copy both halves into temporary lists
        List<T> leftHalf  = new ArrayList<>(list.subList(left, mid + 1));
        List<T> rightHalf = new ArrayList<>(list.subList(mid + 1, right + 1));

        int i = 0; // pointer for leftHalf
        int j = 0; // pointer for rightHalf
        int k = left; // pointer for the original list

        // Merge by picking the smaller element from each half
        while (i < leftHalf.size() && j < rightHalf.size()) {
            if (comparator.compare(leftHalf.get(i), rightHalf.get(j)) <= 0)
                list.set(k++, leftHalf.get(i++));
            else
                list.set(k++, rightHalf.get(j++));
        }

        // Drain any remaining elements from either half
        while (i < leftHalf.size())
            list.set(k++, leftHalf.get(i++));

        while (j < rightHalf.size())
            list.set(k++, rightHalf.get(j++));
    }
}