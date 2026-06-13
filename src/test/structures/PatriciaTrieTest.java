package structures;
import structures.PatriciaTrie;

import java.lang.reflect.*;
import java.util.*;


/**
 * Thorough test suite for PatriciaTrie.
 *
 * Beyond correctness (insert / search / remove), every test also validates
 * the structural Patricia Trie invariants via reflection:
 *
 *   • No internal node (non-root) has an empty prefix.
 *   • Every node's first character matches its key in the parent's children map.
 *   • No node that is not a word has zero children (useless node).
 *   • No non-root, non-word internal node has exactly one child
 *     (should have been compressed by fuseWithOnlyChild).
 *   • The children map key always equals the first char of the child's prefix.
 */
public class PatriciaTrieTest {

    // ── ANSI colours ──────────────────────────────────────────────────────────
    static final String GREEN  = "\u001B[32m";
    static final String RED    = "\u001B[31m";
    static final String BOLD   = "\u001B[1m";
    static final String RESET  = "\u001B[0m";

    static int passed = 0, failed = 0;

    static void check(String name, boolean condition) {
        if (condition) { System.out.println("    " + GREEN + "✔" + RESET + " " + name); passed++; }
        else           { System.out.println("    " + RED   + "✘" + RESET + " " + name); failed++; }
    }

    static void section(String title) {
        System.out.println("\n" + BOLD + "── " + title + RESET);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // STRUCTURAL VALIDATOR
    // ═════════════════════════════════════════════════════════════════════════

    static Object getField(Object obj, String name) throws Exception {
        Class<?> c = obj.getClass();
        while (c != null) {
            try { Field f = c.getDeclaredField(name); f.setAccessible(true); return f.get(obj); }
            catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
        throw new NoSuchFieldException(name);
    }

    static List<String> validateTrie(PatriciaTrie trie) throws Exception {
        List<String> errors = new ArrayList<>();
        Field rootField = PatriciaTrie.class.getDeclaredField("root");
        rootField.setAccessible(true);
        Object root = rootField.get(trie);
        validateNode(root, null, true, errors, "root");
        return errors;
    }

    @SuppressWarnings("unchecked")
    static void validateNode(Object node, Character mapKey, boolean isRoot,
                             List<String> errors, String path) throws Exception {
        if (node == null) { errors.add("Null node at path: " + path); return; }

        String prefix   = (String)  getField(node, "prefix");
        boolean isWord  = (boolean) getField(node, "isWord");
        Map<Character, Object> children = (Map<Character, Object>) getField(node, "children");

        // Non-root prefix must not be empty
        if (!isRoot && (prefix == null || prefix.isEmpty()))
            errors.add("Empty prefix at path: " + path);

        // Map key must match first char of prefix
        if (!isRoot && mapKey != null && !prefix.isEmpty() && mapKey != prefix.charAt(0))
            errors.add("Key mismatch at path " + path
                    + ": map key='" + mapKey + "' but prefix starts with '" + prefix.charAt(0) + "'");

        // Non-word node with no children is a ghost node (should not exist after remove)
        if (!isWord && children.isEmpty() && !isRoot)
            errors.add("Useless non-word leaf at path: " + path + " (prefix='" + prefix + "')");

        // Non-word, non-root internal node with exactly one child should have been compressed
        if (!isWord && !isRoot && children.size() == 1)
            errors.add("Uncompressed single-child non-word node at path: " + path
                    + " (prefix='" + prefix + "')");

        // Recurse
        for (Map.Entry<Character, Object> e : children.entrySet()) {
            validateNode(e.getValue(), e.getKey(), false, errors,
                    path + " -> '" + e.getKey() + "'");
        }
    }

    static void assertValid(String context, PatriciaTrie trie) {
        try {
            List<String> errors = validateTrie(trie);
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
    // TESTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── 1  NULL / EMPTY GUARDS ───────────────────────────────────────────────
    static void testGuards() {
        section("1  Null / empty guards");
        PatriciaTrie t = new PatriciaTrie();
        t.insert(null);  t.insert("");   // must not throw
        t.remove(null);  t.remove("");
        check("search(null) returns false",  !t.search(null));
        check("search(\"\") returns false",  !t.search(""));
        check("insert null/empty does not throw", true);
        check("remove null/empty does not throw", true);
        t.insert("hello");
        check("normal insert after guard ops", t.search("hello"));
        assertValid("after guard ops", t);
    }

    // ── 2  SEARCH ON EMPTY TRIE ──────────────────────────────────────────────
    static void testSearchEmpty() {
        section("2  Search on empty trie");
        PatriciaTrie t = new PatriciaTrie();
        check("search on empty returns false", !t.search("anything"));
        assertValid("empty trie", t);
    }

    // ── 3  SINGLE WORD ───────────────────────────────────────────────────────
    static void testSingleWord() {
        section("3  Single word insert & search");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("hello");
        check("found 'hello'",             t.search("hello"));
        check("'hell' not a word",         !t.search("hell"));
        check("'helloo' not a word",       !t.search("helloo"));
        check("absent word returns false", !t.search("world"));
        assertValid("single word", t);
    }

    // ── 4  WORDS WITH COMMON PREFIX ──────────────────────────────────────────
    static void testCommonPrefix() {
        section("4  Words sharing a common prefix");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("apple");
        t.insert("app");
        t.insert("application");
        t.insert("apply");
        t.insert("apt");

        check("search 'apple'",       t.search("apple"));
        check("search 'app'",         t.search("app"));
        check("search 'application'", t.search("application"));
        check("search 'apply'",       t.search("apply"));
        check("search 'apt'",         t.search("apt"));
        check("'ap' not a word",      !t.search("ap"));
        check("'appl' not a word",    !t.search("appl"));
        assertValid("common prefix words", t);
    }

    // ── 5  WORDS THAT ARE PREFIXES OF EACH OTHER ────────────────────────────
    static void testPrefixOfEachOther() {
        section("5  Words that are prefixes of each other");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("t");
        t.insert("te");
        t.insert("tes");
        t.insert("test");
        t.insert("testi");
        t.insert("testin");
        t.insert("testing");

        for (String w : new String[]{"t","te","tes","test","testi","testin","testing"})
            check("search '" + w + "'", t.search(w));
        check("'testings' not a word", !t.search("testings"));
        assertValid("chain of prefix words", t);
    }

    // ── 6  WORDS WITH NO COMMON PREFIX ──────────────────────────────────────
    static void testNoCommonPrefix() {
        section("6  Words with no common prefix");
        PatriciaTrie t = new PatriciaTrie();
        String[] words = {"alpha","beta","gamma","delta","epsilon"};
        for (String w : words) t.insert(w);
        for (String w : words) check("search '" + w + "'", t.search(w));
        check("absent word", !t.search("zeta"));
        assertValid("no common prefix", t);
    }

    // ── 7  SINGLE CHARACTER WORDS ────────────────────────────────────────────
    static void testSingleCharWords() {
        section("7  Single character words");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("a"); t.insert("b"); t.insert("c");
        t.insert("ab"); t.insert("abc");
        check("'a' found",   t.search("a"));
        check("'b' found",   t.search("b"));
        check("'c' found",   t.search("c"));
        check("'ab' found",  t.search("ab"));
        check("'abc' found", t.search("abc"));
        assertValid("single char words", t);
    }

    // ── 8  DUPLICATE INSERT ──────────────────────────────────────────────────
    static void testDuplicateInsert() {
        section("8  Duplicate insert");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("hello");
        t.insert("hello");
        check("'hello' still found", t.search("hello"));
        check("'hell' still not a word", !t.search("hell"));
        assertValid("after duplicate insert", t);
    }

    // ── 9  REMOVE LEAF WORD ──────────────────────────────────────────────────
    static void testRemoveLeaf() {
        section("9  Remove leaf word");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("cat"); t.insert("car"); t.insert("card");

        t.remove("card");
        check("'card' removed",  !t.search("card"));
        check("'car' intact",     t.search("car"));
        check("'cat' intact",     t.search("cat"));
        assertValid("after remove leaf 'card'", t);
    }

    // ── 10  REMOVE WORD THAT IS PREFIX OF ANOTHER ───────────────────────────
    static void testRemovePrefixWord() {
        section("10  Remove word that is a prefix of another");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("test"); t.insert("testing");

        t.remove("test");
        check("'test' removed",   !t.search("test"));
        check("'testing' intact",  t.search("testing"));
        assertValid("after remove prefix word", t);
    }

    // ── 11  REMOVE LONGER WORD, KEEP PREFIX ──────────────────────────────────
    static void testRemoveLongerWord() {
        section("11  Remove longer word, keep shorter prefix");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("test"); t.insert("testing");

        t.remove("testing");
        check("'testing' removed", !t.search("testing"));
        check("'test' intact",      t.search("test"));
        assertValid("after remove longer word", t);
    }

    // ── 12  REMOVE TRIGGERS COMPRESSION ─────────────────────────────────────
    static void testRemoveCompression() {
        section("12  Remove triggers node compression");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("apple"); t.insert("application");

        t.remove("apple");
        check("'apple' removed",       !t.search("apple"));
        check("'application' intact",   t.search("application"));
        assertValid("after compression", t);

        // New insert should still work after compression
        t.insert("apply");
        check("'apply' insertable after compression", t.search("apply"));
        assertValid("after insert post-compression", t);
    }

    // ── 13  REMOVE ONLY WORD ─────────────────────────────────────────────────
    static void testRemoveOnlyWord() {
        section("13  Remove the only word");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("only");
        t.remove("only");
        check("'only' removed",        !t.search("only"));
        assertValid("after removing only word", t);
        t.insert("only");
        check("re-insert into emptied trie", t.search("only"));
        assertValid("after re-insert", t);
    }

    // ── 14  REMOVE NON-EXISTING WORD ─────────────────────────────────────────
    static void testRemoveAbsent() {
        section("14  Remove non-existing word");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("hello");
        t.remove("world");  // absent
        t.remove("hell");   // prefix but not a word
        t.remove("helloo"); // extension, not in trie
        check("'hello' untouched", t.search("hello"));
        assertValid("after no-op removes", t);
    }

    // ── 15  REMOVE ALL WORDS ──────────────────────────────────────────────────
    static void testRemoveAll() {
        section("15  Remove all words");
        PatriciaTrie t = new PatriciaTrie();
        String[] words = {"the","their","there","they","then","cat","car","card"};
        for (String w : words) t.insert(w);
        for (String w : words) t.remove(w);

        boolean allGone = true;
        for (String w : words) if (t.search(w)) { allGone = false; break; }
        check("all words removed", allGone);
        assertValid("after removing all words", t);

        // Trie should accept new inserts cleanly
        t.insert("fresh");
        check("insert into emptied trie", t.search("fresh"));
        assertValid("after insert into emptied trie", t);
    }

    // ── 16  MID-EDGE SPLIT ON INSERT ─────────────────────────────────────────
    static void testMidEdgeSplit() {
        section("16  Mid-edge split on insert");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("romane");
        t.insert("romanus");   // shares "roman" with "romane"
        t.insert("romulus");   // shares "rom" then diverges
        t.insert("rubens");    // shares only "r"
        t.insert("ruber");
        t.insert("rubicon");
        t.insert("rubicundus");

        for (String w : new String[]{"romane","romanus","romulus","rubens","ruber","rubicon","rubicundus"})
            check("search '" + w + "'", t.search(w));
        check("'roman' not a word",  !t.search("roman"));
        check("'rub' not a word",    !t.search("rub"));
        check("'r' not a word",      !t.search("r"));
        assertValid("after mid-edge splits", t);
    }

    // ── 17  LARGE WORD SET ───────────────────────────────────────────────────
    static void testLargeWordSet() {
        section("17  Large word set");
        PatriciaTrie t = new PatriciaTrie();
        String[] words = {
            "the","their","there","they","then","than","that","this","those","them",
            "car","card","care","careful","careless","cat","catalog","catch","cave",
            "run","runner","running","ran","rank","range","rapid",
            "in","inn","into","interest","internal","internet","interval",
            "be","been","beer","bear","beat","beam","bean","bead",
            "do","done","door","dog","dot","double","doubt"
        };
        for (String w : words) t.insert(w);

        boolean allFound = true;
        for (String w : words) if (!t.search(w)) { allFound = false; System.out.println("      missing: " + w); }
        check("all " + words.length + " words found", allFound);
        check("absent word 'xyz'", !t.search("xyz"));
        check("prefix 'ca' not a word", !t.search("ca"));
        assertValid("large word set", t);
    }

    // ── 18  SELECTIVE REMOVE FROM LARGE SET ──────────────────────────────────
    static void testSelectiveRemove() {
        section("18  Selective remove from large set");
        PatriciaTrie t = new PatriciaTrie();
        String[] words = {
            "the","their","there","they","then",
            "car","card","care","careful","careless",
            "run","runner","running","ran","rank"
        };
        for (String w : words) t.insert(w);

        // Remove every other word
        for (int i = 0; i < words.length; i += 2) t.remove(words[i]);

        boolean removedGone = true, keptPresent = true;
        for (int i = 0; i < words.length; i++) {
            if (i % 2 == 0 &&  t.search(words[i])) { removedGone  = false; System.out.println("      should be gone: "    + words[i]); }
            if (i % 2 == 1 && !t.search(words[i])) { keptPresent  = false; System.out.println("      should be present: " + words[i]); }
        }
        check("removed words gone",     removedGone);
        check("remaining words intact", keptPresent);
        assertValid("after selective remove", t);
    }

    // ── 19  INTERLEAVED INSERT / REMOVE ─────────────────────────────────────
    static void testInterleaved() {
        section("19  Interleaved insert and remove");
        PatriciaTrie t = new PatriciaTrie();
        Set<String> present = new HashSet<>();
        String[] pool = {"alpha","beta","gamma","delta","epsilon","zeta","eta",
                         "theta","iota","kappa","lambda","mu","nu","xi","omicron"};
        Random rng = new Random(7);

        for (int round = 0; round < 200; round++) {
            String word = pool[rng.nextInt(pool.length)];
            if (rng.nextBoolean()) { t.insert(word);  present.add(word); }
            else                   { t.remove(word);  present.remove(word); }
        }

        boolean ok = true;
        for (String w : present) if (!t.search(w)) { ok = false; System.out.println("      missing: " + w); }
        check("all tracked words found after interleaved ops", ok);
        assertValid("after interleaved ops", t);
    }

    // ── 20  CASE SENSITIVITY ─────────────────────────────────────────────────
    static void testCaseSensitivity() {
        section("20  Case sensitivity");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("Hello"); t.insert("hello"); t.insert("HELLO");
        check("'Hello' found",  t.search("Hello"));
        check("'hello' found",  t.search("hello"));
        check("'HELLO' found",  t.search("HELLO"));
        check("'hELLO' absent", !t.search("hELLO"));
        assertValid("case sensitivity", t);
    }

    // ── 21  WORDS WITH SPECIAL / NUMERIC CHARS ───────────────────────────────
    static void testSpecialChars() {
        section("21  Words with digits and special characters");
        PatriciaTrie t = new PatriciaTrie();
        t.insert("abc123"); t.insert("abc456"); t.insert("123abc");
        t.insert("hello_world"); t.insert("hello_there");
        check("'abc123' found",       t.search("abc123"));
        check("'abc456' found",       t.search("abc456"));
        check("'123abc' found",       t.search("123abc"));
        check("'hello_world' found",  t.search("hello_world"));
        check("'hello_there' found",  t.search("hello_there"));
        check("'abc' not a word",     !t.search("abc"));
        check("'hello_' not a word",  !t.search("hello_"));
        assertValid("special chars", t);
    }

    // ── 22  RE-INSERT AFTER REMOVE ───────────────────────────────────────────
    static void testReInsert() {
        section("22  Re-insert after remove");
        PatriciaTrie t = new PatriciaTrie();
        String[] words = {"test","testing","tester","tested"};
        for (String w : words) t.insert(w);
        for (String w : words) t.remove(w);
        for (String w : words) t.insert(w);

        boolean allFound = true;
        for (String w : words) if (!t.search(w)) { allFound = false; break; }
        check("all words re-inserted correctly", allFound);
        assertValid("after re-insert", t);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MAIN
    // ═════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        testGuards();
        testSearchEmpty();
        testSingleWord();
        testCommonPrefix();
        testPrefixOfEachOther();
        testNoCommonPrefix();
        testSingleCharWords();
        testDuplicateInsert();
        testRemoveLeaf();
        testRemovePrefixWord();
        testRemoveLongerWord();
        testRemoveCompression();
        testRemoveOnlyWord();
        testRemoveAbsent();
        testRemoveAll();
        testMidEdgeSplit();
        testLargeWordSet();
        testSelectiveRemove();
        testInterleaved();
        testCaseSensitivity();
        testSpecialChars();
        testReInsert();

        System.out.println();
        System.out.println(BOLD + "══════════════════════════════════════" + RESET);
        String color = failed == 0 ? GREEN : RED;
        System.out.println(BOLD + color + "  Results: " + passed + " passed, " + failed + " failed" + RESET);
        System.out.println(BOLD + "══════════════════════════════════════" + RESET);
    }
}
