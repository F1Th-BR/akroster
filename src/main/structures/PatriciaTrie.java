package structures;

import java.util.HashMap;
import java.util.Map;

/**
 * A PATRICIA trie implementation.
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class PatriciaTrie {
    /* ===== ATTRIBUTES ===== */
    private final PatriciaNode root;

    /* ===== METHODS ===== */
    /* ===== CONSTRUCTORS ===== */
    /**
     * Creates a dummy root with an empty string,
     * ensuring that words without a common prefix
     * simply branch out from here
     */
    public PatriciaTrie() {
        this.root = new PatriciaNode("", false);
    }

    /* ===== PUBLIC METHODS ===== */
    /**
     * A public insertion method.
     * @param word the word to be added in the tree.
     */
    public void insert(String word) {
        if (word == null || word.isEmpty())
            return;
        insertNode(root, word);
    }

    public void remove(String word) {
        if (word == null || word.isEmpty())
            return;

        removeNode(root, word);
    }

    /**
     * 
     * @param word
     * @return
     */
    public boolean search(String word) {
        if (word == null || word.isEmpty())
            return false;
        else
            return searchInNode(this.root, word);
    }
    
    /* ===== PRIVATE METHODS ===== */
    /**
     * A method to fuse a parent node with its only 
     * child, compressing the tree.
     * 
     * @param node the node that'll fuse with its child.
     */
    private void fuseWithOnlyChild(PatriciaNode node) {
        // Get reference to single child.
        PatriciaNode onlyChild = node.children.values().iterator().next();

        // Concatenates the prefixes
        node.prefix = node.prefix + onlyChild.prefix;
        
        // Adopt word status
        node.isWord = onlyChild.isWord;

        // Takes over the child's children pointers
        node.children = onlyChild.children;
    }

    /**
     * A private method to insert a word in the tree, 
     * creating the proper nodes if needed.
     * 
     * @param current the current node being analyzed
     * @param word the word to be added
     */
    private void insertNode(PatriciaNode current, String word) {
        // ===========================================
        // CASE 1: Current node doesn't have a child 
        // starting with the first letter of "word". 
        // Creates a new child holding the entire word, 
        // marks it as isWord = true and exits.
        // ===========================================
        char firstChar = word.charAt(0);

        if (!current.children.containsKey(firstChar)) {
            current.children.put(firstChar, new PatriciaNode(word, true));
            return;
        }
    
        // ==============================================
        // CASE 2: Current node has a child with the 
        // starting letter. Checks how much of the 
        // edge's prefix (prefix of the word already in 
        // the trie) matches the new word.
        // ==============================================
        PatriciaNode child = current.children.get(firstChar);
        String edge = child.prefix;

        int commonLength = 0;
        int minLength = Math.min(edge.length(), word.length());
        while (commonLength < minLength && edge.charAt(commonLength) == word.charAt(commonLength)) {
            commonLength++;
        }

        // CASE 2a: The existing node is a full match. 
        // Checks if the new word is an exact match of 
        // edge then flips the isWord flag to true, 
        // else, if the existing edge is a substring of 
        // word then recursively calls insertNode on the 
        // child passing the rest of the word as the 
        // argument.
        if (commonLength == edge.length()) {
            if (commonLength == word.length()) {
                child.isWord = true;
            } else {
                insertNode(child, word.substring(commonLength));
            }
        }
        // CASE 2b: The existing node is not a full 
        // match. The new word and the edge diverge 
        // midway. Splits the existing node in two, 
        // creates a new parent holding the prefix, 
        // updates the existing node to hold edge's 
        // suffix and inserts the remaining suffix of 
        // word on the trie in a new node.
        else {
            String commonPrefix = edge.substring(0, commonLength);
            PatriciaNode newParent = new PatriciaNode(commonPrefix, false);

            current.children.put(firstChar, newParent);

            child.prefix = edge.substring(commonLength);
            newParent.children.put(child.prefix.charAt(0), child);

            if (commonLength == word.length()) {
                newParent.isWord = true;
            } else {
                String remainingSuffix = word.substring(commonLength);
                newParent.children.put(remainingSuffix.charAt(0), new PatriciaNode(remainingSuffix, true));
            }
        }
    }

    /**
     * A private method to remove a word and its node.
     * 
     * @param current the current node being verified
     * @param word the word to be removed from the trie
     * @return true, if the word was removed. False, if not.
     */
    private boolean removeNode(PatriciaNode current, String word) {
        // Checks for a starting match.
        char firstChar = word.charAt(0);
        
        // if current node doesn't have a child with that starting letter, the word isn't in the tree.
        if (!current.children.containsKey(firstChar))
            return false;
        
        PatriciaNode child = current.children.get(firstChar);
        String edge = child.prefix;

        // Checks if the edge string matches the 
        // beginning of the target word.
        if (word.startsWith(edge)) {
           // If the length matches, we found the node 
           // representing the word.
            if (word.length() == edge.length()) {
                // If the string was found, but it 
                // exists a prefix, then it can't be 
                // removed.
                if (!child.isWord)
                        return false;
                
                // Else, the node is marked for deletion
                child.isWord = false;

                // CASE A: node doesn't have children. 
                // Delete the node.
                if (child.children.isEmpty())
                    current.children.remove(firstChar);
                // CASE B: node has one child. Compress 
                // it.
                else if (child.children.size() == 1)
                    fuseWithOnlyChild(child);

                return true;
            } else {
                // The word is longer than the edge. Cut 
                // the common prefix and search deeper.
                boolean removed = removeNode(child, word.substring(edge.length()));

                // Clean up the tree after word deletion.
                if (removed && !child.isWord && child.children.size() == 1)
                    fuseWithOnlyChild(child);

                return removed;
            }
        }
        return false;

    }

    /**
     * 
     * @param current
     * @param word
     * @return
     */
    private boolean searchInNode(PatriciaNode current, String word) {
        char firstChar = word.charAt(0);
        if(!current.children.containsKey(firstChar))
            return false;

        PatriciaNode child = current.children.get(firstChar);
        String edge = child.prefix;
    
        if (word.startsWith(edge)) {
            if (word.length() == edge.length())
                return child.isWord;
            else
                return searchInNode(child, word.substring(edge.length()));
        } else {
            return false;
        }
    }

    /* ===== PRIVATE INNER CLASS ===== */
    private class PatriciaNode {
        /* ===== ATTRIBUTES ===== */
        String prefix;
        boolean isWord;
        Map<Character, PatriciaNode> children;

        /* ===== METHODS ===== */
        /* ===== CONSTRUCTORS ===== */
        public PatriciaNode(String prefix, boolean isWord) {
            this.prefix = prefix;
            this.isWord = isWord;
            this.children = new HashMap<>();
        }
    }
}