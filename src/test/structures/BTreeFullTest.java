package structures;
import structures.BTree;

import java.lang.reflect.*;
import java.util.*;

/**
 * Thorough test suite for BTree<K, V>.
 *
 * Beyond correctness checks (insert / search / remove give the right answers),
 * every test also validates the structural B-Tree invariants via a recursive
 * tree inspector exposed through reflection:
 *
 *   • Every non-root node holds between t-1 and 2t-1 keys.
 *   • The root holds between 1 and 2t-1 keys (or 0 when the tree is empty).
 *   • All leaves are at the same depth.
 *   • Keys inside every node are sorted in ascending order.
 *   • For every internal node, keys respect the BST separator property
 *     (child[i] keys < keys[i] < child[i+1] keys).
 *   • No null / stale child pointers exist within the live range.
 */
public class BTreeFullTest {

    // ── ANSI colours ──────────────────────────────────────────────────────────
    static final String GREEN  = "\u001B[32m";
    static final String RED    = "\u001B[31m";
    static final String YELLOW = "\u001B[33m";
    static final String BOLD   = "\u001B[1m";
    static final String RESET  = "\u001B[0m";

    static int passed = 0, failed = 0;

    static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("    " + GREEN + "✔" + RESET + " " + name);
            passed++;
        } else {
            System.out.println("    " + RED + "✘" + RESET + " " + name);
            failed++;
        }
    }

    static void section(String title) {
        System.out.println("\n" + BOLD + "── " + title + RESET);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // STRUCTURAL VALIDATOR  (uses reflection to inspect package-private fields)
    // ═════════════════════════════════════════════════════════════════════════

    static Object getField(Object obj, String name) throws Exception {
        // Walk the class hierarchy so inner classes are also handled
        Class<?> c = obj.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(obj);
            } catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
        throw new NoSuchFieldException(name + " not found in " + obj.getClass());
    }

    /** Returns a human-readable list of all structural violations, or empty list if valid. */
    static List<String> validateTree(BTree<?,?> tree) throws Exception {
        List<String> errors = new ArrayList<>();
        Field rootField = BTree.class.getDeclaredField("root");
        rootField.setAccessible(true);
        Object root = rootField.get(tree);

        Field orderField = BTree.class.getDeclaredField("order");
        orderField.setAccessible(true);
        int order = (int) orderField.get(tree);
        int t = (int) Math.ceil((double) order / 2);

        int rootKeys = (int) getField(root, "numKeys");
        boolean rootLeaf = (boolean) getField(root, "isLeaf");

        if (rootKeys == 0 && !rootLeaf)
            errors.add("Root has 0 keys but is not a leaf");

        // Recursive check; returns leaf depth (-1 = not yet set)
        int[] leafDepth = {-1};
        validateNode(root, t, null, null, 0, leafDepth, errors, true);
        return errors;
    }

    @SuppressWarnings("unchecked")
    static void validateNode(Object node, int t,
                             Comparable<?> lo, Comparable<?> hi,
                             int depth, int[] leafDepth,
                             List<String> errors, boolean isRoot) throws Exception {
        if (node == null) { errors.add("Unexpected null node at depth " + depth); return; }

        int numKeys  = (int)     getField(node, "numKeys");
        boolean isLeaf = (boolean) getField(node, "isLeaf");
        Object[] keys = (Object[]) getField(node, "keys");
        Object[] children = (Object[]) getField(node, "children");

        // ── Key count ──────────────────────────────────────────────────────
        int minKeys = isRoot ? (numKeys == 0 && isLeaf ? 0 : 1) : t - 1;
        int maxKeys = 2 * t - 1;
        if (numKeys < minKeys || numKeys > maxKeys)
            errors.add("Node at depth " + depth + " has " + numKeys
                    + " keys (allowed " + minKeys + ".." + maxKeys + ")");

        // ── Key ordering ──────────────────────────────────────────────────
        for (int i = 0; i < numKeys - 1; i++) {
            if (((Comparable) keys[i]).compareTo(keys[i + 1]) >= 0)
                errors.add("Key ordering violation at depth " + depth
                        + ": keys[" + i + "]=" + keys[i]
                        + " >= keys[" + (i+1) + "]=" + keys[i + 1]);
        }

        // ── BST bounds ────────────────────────────────────────────────────
        if (lo != null && numKeys > 0 && ((Comparable) keys[0]).compareTo(lo) <= 0)
            errors.add("BST lower-bound violation at depth " + depth
                    + ": first key " + keys[0] + " <= bound " + lo);
        if (hi != null && numKeys > 0 && ((Comparable) keys[numKeys - 1]).compareTo(hi) >= 0)
            errors.add("BST upper-bound violation at depth " + depth
                    + ": last key " + keys[numKeys - 1] + " >= bound " + hi);

        // ── Leaf depth consistency ────────────────────────────────────────
        if (isLeaf) {
            if (leafDepth[0] == -1) leafDepth[0] = depth;
            else if (leafDepth[0] != depth)
                errors.add("Leaf depth inconsistency: expected "
                        + leafDepth[0] + " but found " + depth);
            return;
        }

        // ── Children ─────────────────────────────────────────────────────
        if (isLeaf) return;
        int expectedChildren = numKeys + 1;
        for (int i = 0; i < expectedChildren; i++) {
            if (children[i] == null)
                errors.add("Null child[" + i + "] in internal node at depth " + depth
                        + " (numKeys=" + numKeys + ")");
        }
        // Stale pointers beyond live range
        for (int i = expectedChildren; i < children.length; i++) {
            if (children[i] != null)
                errors.add("Stale child[" + i + "] at depth " + depth
                        + " (numKeys=" + numKeys + ", should be null)");
        }

        // Recurse
        for (int i = 0; i <= numKeys; i++) {
            if (children[i] == null) continue; // already reported
            Comparable<?> childLo = i > 0        ? (Comparable<?>) keys[i - 1] : lo;
            Comparable<?> childHi = i < numKeys  ? (Comparable<?>) keys[i]     : hi;
            validateNode(children[i], t, childLo, childHi, depth + 1, leafDepth, errors, false);
        }
    }

    /** Convenience: asserts the tree is structurally valid and prints any violations. */
    static void assertValid(String context, BTree<?,?> tree) {
        try {
            List<String> errors = validateTree(tree);
            if (errors.isEmpty()) {
                check("Structure valid (" + context + ")", true);
            } else {
                check("Structure valid (" + context + ")", false);
                for (String e : errors)
                    System.out.println("      " + RED + "    → " + e + RESET);
            }
        } catch (Exception ex) {
            check("Structure valid (" + context + ")", false);
            System.out.println("      " + RED + "    → Exception: " + ex.getMessage() + RESET);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    /** Insert all entries, verify all are found, verify structure. */
    static <K extends Comparable<K>> BTree<K, Integer> buildAndVerify(
            String label, int order, K[] keys) {
        BTree<K, Integer> t = new BTree<>(order);
        for (int i = 0; i < keys.length; i++) t.insert(keys[i], i);
        boolean allFound = true;
        for (K k : keys) if (t.search(k) == null) { allFound = false; break; }
        check("All keys found after insert (" + label + ")", allFound);
        assertValid("after insert " + label, t);
        return t;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TESTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── 1  NULL / EMPTY GUARDS ───────────────────────────────────────────────
    static void testGuards() {
        section("1  Null / empty guards");
        BTree<Integer, String> t = new BTree<>(4);
        boolean threw = false;
        try { t.insert(null, "x"); } catch (NullPointerException e) { threw = true; }
        check("insert(null, v) throws NPE", threw);

        threw = false;
        try { t.insert(1, null); } catch (NullPointerException e) { threw = true; }
        check("insert(k, null) throws NPE", threw);

        check("search(null) returns null", t.search(null) == null);
        t.remove(null); // must not throw
        check("remove(null) does not throw", true);

        t.insert(42, "hello");
        check("search existing after guard ops", "hello".equals(t.search(42)));
    }

    // ── 2  SEARCH ON EMPTY TREE ──────────────────────────────────────────────
    static void testSearchEmpty() {
        section("2  Search on empty tree");
        BTree<Integer, Integer> t = new BTree<>(3);
        check("search on empty tree returns null", t.search(1) == null);
        assertValid("empty tree", t);
    }

    // ── 3  SINGLE INSERT & SEARCH ────────────────────────────────────────────
    static void testSingleInsert() {
        section("3  Single insert & search");
        BTree<Integer, String> t = new BTree<>(4);
        t.insert(7, "seven");
        check("found inserted key",      "seven".equals(t.search(7)));
        check("absent key returns null", t.search(99) == null);
        assertValid("single key", t);
    }

    // ── 4  SPLIT AT ROOT (order 3, insert 3 keys to force first split) ───────
    static void testRootSplit() {
        section("4  Root split");
        BTree<Integer, Integer> t = new BTree<>(3); // t=2, max 3 keys before split
        for (int i = 1; i <= 3; i++) t.insert(i, i);
        for (int i = 1; i <= 3; i++)
            check("key " + i + " after root split", Integer.valueOf(i).equals(t.search(i)));
        assertValid("after root split", t);
    }

    // ── 5  SEQUENTIAL INSERT (ascending) ────────────────────────────────────
    static void testSequentialAscending() {
        section("5  Sequential insert – ascending");
        int N = 50;
        Integer[] keys = new Integer[N];
        for (int i = 0; i < N; i++) keys[i] = i + 1;
        for (int order : new int[]{3, 4, 5, 6}) {
            BTree<Integer, Integer> t = buildAndVerify("order=" + order + " asc N=" + N, order, keys);
            check("absent key above range",  t.search(N + 1) == null);
            check("absent key below range",  t.search(0)     == null);
        }
    }

    // ── 6  SEQUENTIAL INSERT (descending) ───────────────────────────────────
    static void testSequentialDescending() {
        section("6  Sequential insert – descending");
        int N = 50;
        Integer[] keys = new Integer[N];
        for (int i = 0; i < N; i++) keys[i] = N - i;
        for (int order : new int[]{3, 5, 7}) {
            buildAndVerify("order=" + order + " desc N=" + N, order, keys);
        }
    }

    // ── 7  RANDOM-ORDER INSERT ───────────────────────────────────────────────
    static void testRandomInsert() {
        section("7  Random-order insert");
        int N = 100;
        Integer[] keys = new Integer[N];
        for (int i = 0; i < N; i++) keys[i] = (i * 37 + 13) % N;
        for (int order : new int[]{3, 4, 5, 7}) {
            buildAndVerify("order=" + order + " rand N=" + N, order, keys);
        }
    }

    // ── 8  STRING KEYS ───────────────────────────────────────────────────────
    static void testStringKeys() {
        section("8  String keys");
        String[] words = {"banana","apple","cherry","date","elderberry",
                          "fig","grape","honeydew","kiwi","lemon"};
        BTree<String, Integer> t = new BTree<>(4);
        for (int i = 0; i < words.length; i++) t.insert(words[i], i);
        boolean allFound = true;
        for (String w : words) if (t.search(w) == null) { allFound = false; break; }
        check("all string keys found", allFound);
        check("absent string key", t.search("mango") == null);
        assertValid("string keys", t);
    }

    // ── 9  CASE 1 – REMOVE LEAF KEY ─────────────────────────────────────────
    static void testRemoveLeaf() {
        section("9  Remove – Case 1 (leaf)");
        BTree<Integer, Integer> t = new BTree<>(4);
        for (int i = 1; i <= 15; i++) t.insert(i, i);

        int[] leafTargets = {1, 8, 15};
        for (int k : leafTargets) {
            t.remove(k);
            check("key " + k + " removed", t.search(k) == null);
            assertValid("after remove leaf " + k, t);
        }
        // Remaining keys still present
        boolean intact = true;
        for (int i = 1; i <= 15; i++)
            if (Arrays.binarySearch(leafTargets, i) < 0 && t.search(i) == null) intact = false;
        check("remaining keys intact", intact);
    }

    // ── 10  CASE 2a – PREDECESSOR REPLACEMENT ───────────────────────────────
    static void testRemoveCase2a() {
        section("10  Remove – Case 2a (predecessor replacement)");
        BTree<Integer, Integer> t = new BTree<>(3);
        for (int i = 1; i <= 20; i++) t.insert(i, i);
        // Remove keys that are highly likely to be internal nodes in a full tree
        for (int k : new int[]{8, 12, 16}) {
            t.remove(k);
            check("removed " + k, t.search(k) == null);
            assertValid("after remove " + k, t);
        }
    }

    // ── 11  CASE 2b – SUCCESSOR REPLACEMENT ─────────────────────────────────
    static void testRemoveCase2b() {
        section("11  Remove – Case 2b (successor replacement)");
        BTree<Integer, Integer> t = new BTree<>(3);
        for (int i = 1; i <= 10; i++) t.insert(i, i);
        t.remove(4);
        check("key 4 removed",  t.search(4) == null);
        check("key 3 present",  Integer.valueOf(3).equals(t.search(3)));
        check("key 5 present",  Integer.valueOf(5).equals(t.search(5)));
        assertValid("after case-2b remove", t);
    }

    // ── 12  CASE 2c – MERGE ──────────────────────────────────────────────────
    static void testRemoveCase2c() {
        section("12  Remove – Case 2c (merge)");
        BTree<Integer, Integer> t = new BTree<>(3);
        for (int i = 1; i <= 7; i++) t.insert(i, i);
        t.remove(4); // both children of root likely have t-1 keys
        check("key 4 removed", t.search(4) == null);
        assertValid("after case-2c merge", t);
        for (int i : new int[]{1,2,3,5,6,7})
            check("key " + i + " intact", Integer.valueOf(i).equals(t.search(i)));
    }

    // ── 13  CASE 3a – BORROW FROM LEFT ──────────────────────────────────────
    static void testBorrowLeft() {
        section("13  Remove – Case 3a (borrow from left)");
        BTree<Integer, Integer> t = new BTree<>(3);
        int[] ins = {10,20,30,40,50,5,15,25,35,45};
        for (int k : ins) t.insert(k, k);
        t.remove(5); t.remove(15);
        t.remove(25); // should trigger borrow-from-left
        check("key 25 gone",   t.search(25) == null);
        check("key 20 intact", Integer.valueOf(20).equals(t.search(20)));
        check("key 30 intact", Integer.valueOf(30).equals(t.search(30)));
        assertValid("after borrow-left", t);
    }

    // ── 14  CASE 3a – BORROW FROM RIGHT ─────────────────────────────────────
    static void testBorrowRight() {
        section("14  Remove – Case 3a (borrow from right)");
        BTree<Integer, Integer> t = new BTree<>(3);
        int[] ins = {10,20,30,40,50,5,15,25,35,45,55};
        for (int k : ins) t.insert(k, k);
        t.remove(5); t.remove(10);
        t.remove(15); // should trigger borrow-from-right
        check("key 15 gone",   t.search(15) == null);
        check("key 20 intact", Integer.valueOf(20).equals(t.search(20)));
        assertValid("after borrow-right", t);
    }

    // ── 15  CASE 3b – MERGE WITH SIBLING ────────────────────────────────────
    static void testCase3bMerge() {
        section("15  Remove – Case 3b (merge with sibling)");
        BTree<Integer, Integer> t = new BTree<>(3);
        for (int i = 1; i <= 7; i++) t.insert(i, i);
        t.remove(4); t.remove(2); t.remove(6);
        for (int k : new int[]{2,4,6})
            check("key " + k + " gone", t.search(k) == null);
        for (int k : new int[]{1,3,5,7})
            check("key " + k + " intact", Integer.valueOf(k).equals(t.search(k)));
        assertValid("after case-3b merges", t);
    }

    // ── 16  REMOVE ROOT SHRINK ───────────────────────────────────────────────
    static void testRootShrink() {
        section("16  Root shrink after merge");
        BTree<Integer, Integer> t = new BTree<>(3);
        // Insert exactly enough to force a 2-level tree, then collapse
        for (int i = 1; i <= 3; i++) t.insert(i, i);
        // Now the root was split; remove until root collapses back
        for (int i = 1; i <= 3; i++) {
            t.remove(i);
            assertValid("after removing " + i, t);
        }
        check("tree empty, search returns null", t.search(1) == null);
        t.insert(99, 99);
        check("insert into collapsed tree works", Integer.valueOf(99).equals(t.search(99)));
        assertValid("after re-insert into collapsed tree", t);
    }

    // ── 17  REMOVE ALL KEYS (ascending) ─────────────────────────────────────
    static void testRemoveAllAscending() {
        section("17  Remove all keys – ascending order");
        int N = 40;
        BTree<Integer, Integer> t = new BTree<>(4);
        for (int i = 1; i <= N; i++) t.insert(i, i);
        for (int i = 1; i <= N; i++) {
            t.remove(i);
            assertValid("after remove " + i, t);
        }
        boolean allGone = true;
        for (int i = 1; i <= N; i++) if (t.search(i) != null) allGone = false;
        check("all " + N + " keys gone", allGone);
    }

    // ── 18  REMOVE ALL KEYS (descending) ────────────────────────────────────
    static void testRemoveAllDescending() {
        section("18  Remove all keys – descending order");
        int N = 40;
        BTree<Integer, Integer> t = new BTree<>(4);
        for (int i = 1; i <= N; i++) t.insert(i, i);
        for (int i = N; i >= 1; i--) {
            t.remove(i);
            assertValid("after remove " + i, t);
        }
        boolean allGone = true;
        for (int i = 1; i <= N; i++) if (t.search(i) != null) allGone = false;
        check("all " + N + " keys gone (desc)", allGone);
    }

    // ── 19  REMOVE NON-EXISTING KEY ──────────────────────────────────────────
    static void testRemoveAbsent() {
        section("19  Remove non-existing key");
        BTree<Integer, Integer> t = new BTree<>(4);
        for (int i = 1; i <= 10; i++) t.insert(i, i);
        t.remove(99);
        boolean allIntact = true;
        for (int i = 1; i <= 10; i++) if (t.search(i) == null) allIntact = false;
        check("tree unchanged after remove of absent key", allIntact);
        assertValid("after no-op remove", t);
    }

    // ── 20  RE-INSERT AFTER REMOVE ───────────────────────────────────────────
    static void testReInsert() {
        section("20  Re-insert removed keys");
        BTree<Integer, Integer> t = new BTree<>(3);
        for (int i = 1; i <= 10; i++) t.insert(i, i);
        for (int i = 1; i <= 10; i++) t.remove(i);
        for (int i = 1; i <= 10; i++) t.insert(i, i * 100);
        boolean ok = true;
        for (int i = 1; i <= 10; i++)
            if (!Integer.valueOf(i * 100).equals(t.search(i))) ok = false;
        check("all keys re-inserted with new values", ok);
        assertValid("after re-insert", t);
    }

    // ── 21  VALUE OVERWRITE (duplicate key) ──────────────────────────────────
    static void testDuplicateKey() {
        section("21  Duplicate key insert");
        // The implementation stores both; test it doesn't crash and at least
        // one value is findable
        BTree<Integer, String> t = new BTree<>(4);
        t.insert(5, "first");
        t.insert(5, "second");
        check("key 5 still findable after duplicate", t.search(5) != null);
        assertValid("after duplicate insert", t);
    }

    // ── 22  LARGE RANDOM STRESS (multiple orders) ────────────────────────────
    static void testLargeStress() {
        section("22  Large random stress (insert + partial remove)");
        int[] orders = {3, 4, 5, 6, 7};
        int N = 300;
        for (int order : orders) {
            BTree<Integer, Integer> t = new BTree<>(order);
            // Pseudo-random insert
            for (int i = 0; i < N; i++) t.insert((i * 37 + 13) % N, i);
            assertValid("order=" + order + " after insert N=" + N, t);

            // Remove every other key
            for (int i = 0; i < N; i += 2) t.remove((i * 37 + 13) % N);
            assertValid("order=" + order + " after partial remove", t);

            // Verify remaining keys
            boolean ok = true;
            for (int i = 1; i < N; i += 2)
                if (t.search((i * 37 + 13) % N) == null) { ok = false; break; }
            check("order=" + order + " remaining keys intact", ok);
        }
    }

    // ── 23  FULL REMOVE STRESS (multiple orders) ─────────────────────────────
    static void testFullRemoveStress() {
        section("23  Full remove stress (all keys, multiple orders)");
        int[] orders = {3, 4, 5};
        int N = 100;
        for (int order : orders) {
            BTree<Integer, Integer> t = new BTree<>(order);
            for (int i = 0; i < N; i++) t.insert(i, i);
            for (int i = 0; i < N; i++) t.remove(i);
            boolean allGone = true;
            for (int i = 0; i < N; i++) if (t.search(i) != null) { allGone = false; break; }
            check("order=" + order + " all " + N + " keys removed", allGone);
            assertValid("order=" + order + " after full remove", t);
        }
    }

    // ── 24  INTERLEAVED INSERT / REMOVE ─────────────────────────────────────
    static void testInterleaved() {
        section("24  Interleaved insert and remove");
        BTree<Integer, Integer> t = new BTree<>(4);
        Set<Integer> present = new TreeSet<>();
        Random rng = new Random(42);

        for (int round = 0; round < 200; round++) {
            int key = rng.nextInt(50);
            if (rng.nextBoolean()) {
                t.insert(key, key);
                present.add(key);
            } else {
                t.remove(key);
                present.remove(key);
            }
        }
        boolean ok = true;
        for (int k : present) if (t.search(k) == null) { ok = false; break; }
        check("all tracked keys found after interleaved ops", ok);
        assertValid("after interleaved ops", t);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MAIN
    // ═════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        testGuards();
        testSearchEmpty();
        testSingleInsert();
        testRootSplit();
        testSequentialAscending();
        testSequentialDescending();
        testRandomInsert();
        testStringKeys();
        testRemoveLeaf();
        testRemoveCase2a();
        testRemoveCase2b();
        testRemoveCase2c();
        testBorrowLeft();
        testBorrowRight();
        testCase3bMerge();
        testRootShrink();
        testRemoveAllAscending();
        testRemoveAllDescending();
        testRemoveAbsent();
        testReInsert();
        testDuplicateKey();
        testLargeStress();
        testFullRemoveStress();
        testInterleaved();

        System.out.println();
        System.out.println(BOLD + "══════════════════════════════════════" + RESET);
        String color = failed == 0 ? GREEN : RED;
        System.out.println(BOLD + color
                + "  Results: " + passed + " passed, " + failed + " failed"
                + RESET);
        System.out.println(BOLD + "══════════════════════════════════════" + RESET);
    }
}
