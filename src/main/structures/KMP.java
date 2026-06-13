package structures;

import java.util.ArrayList;
import java.util.List;

/**
 * A KMP algorithm implementation.
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class KMP {
    /* ===== METHODS ===== */
    /* ===== CONSTRUCTORS ===== */
    private KMP() {}

    /**
     * 
     * @param text
     * @param pattern
     * @return
     */
    public static List<Integer> KMPSearch(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();

        if (text == null || pattern == null || pattern.isEmpty())
            return matches;

        int n = text.length();
        int m = pattern.length();

        int[] lps = computeLPSArray(pattern);

        int i = 0;
        int j = 0;

        while (i < n) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }

            if (j == m) {
                matches.add(i - j);
                j = lps[j - 1];
            } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0)
                    j = lps[j - 1];
                else
                    i++;
            }
        }

        return matches;
    }

    private static int[] computeLPSArray(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];

        int len = 0;
        int i = 1;      // start at 1: lps[0] is always 0 by definition
        lps[0] = 0;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0)
                    len = lps[len - 1];
                else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }
}