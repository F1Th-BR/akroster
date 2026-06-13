package structures;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic BTree implementation.
 * 
 * @author Thiago Feijó de Albuquerque (https://github.com/F1Th-BR)
 */
public class BTree<K extends Comparable<K>, V> {
    /* ===== ATTRIBUTES ===== */
    private BTreeNode root;
    private final int order;

    /* ===== METHODS ===== */
    /* ===== CONSTRUCTORS ===== */
    public BTree(int order) {
        this.order = order;
        this.root = new BTreeNode(true);
    }

    /* ===== PUBLIC METHODS ===== */
    /**
     * Returns all values in the tree in ascending key order.
     * The result can be used for listing, exporting, or sorting
     * without relying on an external session list.
    *
    * @return list of values in in-order (sorted by key)
    */
   public List<V> inOrderValues() {
       List<V> result = new ArrayList<>();
       inOrderTraversal(this.root, result);
       return result;
    }
    
    /* ===== B-TREE OPERATIONS ===== */
    /**
     * A public method to insert a pair (key, value) in 
     * the B-Tree
     * 
     * @param key the identification
     * @param value the object stored
     */
    public void insert(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("Object contains one or more null fields");
        }

        BTreeNode r = this.root;
        int t = (int) Math.ceil((double) order / 2);
        
        if (r.numKeys == 2 * t - 1) {
            BTreeNode s = new BTreeNode(false);
            this.root = s;
            s.numKeys = 0;
            s.children[0] = r;
            splitNode(s, r, 0);
            insertNotFull(s, key, value);
        } else {
            insertNotFull(r, key, value);
        }
    }

    /**
     * A public method to remove an object within the tree.
     * 
     * @param key the object to be removed
     */
    public void remove(K key) {
        if (key == null || this.root.numKeys == 0)
            return;
        
        removeFromNode(this.root, key);
    
        if (this.root.numKeys == 0 && !this.root.isLeaf)
            this.root = this.root.children[0];
        
    }

    /**
     * A public method to search through the tree for 
     * the key element
     * 
     * @param key the element to be searched
     * @return V if found, null if not
     */
    public V search(K key) {
        if (key == null)
            return null;
        else
            return searchInNode(this.root, key);      
    }

    /* ===== PRIVATE METHODS ===== */
    /**
     * A public method to traverse the tree to return
     * its values
     * 
     * @param x the current node being traversed
     * @param result the list where the elements will
     *              be stored and presented afterwards
     * @return A list with all elements of the tree
     */
    private void inOrderTraversal(BTreeNode x, List<V> result) {
        if (x == null || x.numKeys == 0)
            return;

        for (int i = 0; i < x.numKeys; i++) {
            // Visit left child before key i
            if (!x.isLeaf)
                inOrderTraversal(x.children[i], result);

            result.add(x.values[i]);
        }

        // Visit the rightmost child after the last key
        if (!x.isLeaf)
            inOrderTraversal(x.children[x.numKeys], result);
    }

    /**
     * An internal (private) method used by the 
     * insertion.
     * 
     * @param s the node where the information will be stored
     * @param key the identification
     * @param value the object
     */
    private void insertNotFull(BTreeNode s, K key, V value) {
        if (s.isLeaf) {
            int i = s.numKeys - 1;
            while (i >= 0 && s.keys[i].compareTo(key) > 0) {
                s.keys[i+1] = s.keys[i];
                s.values[i+1] = s.values[i];
                i--;
            }

            if (i >= 0 && s.keys[i] != null && s.keys[i].compareTo(key) == 0) {
                s.values[i] = value;
                return;
            }

            s.keys[i+1] = key;
            s.values[i+1] = value;
            s.numKeys += 1;
        } else {
            int i = s.numKeys - 1;
            int t = (int) Math.ceil((double) order / 2);

            while (i >= 0 && s.keys[i].compareTo(key) > 0) {
                i--;
            }
            
            if (i >= 0 && s.keys[i].compareTo(key) == 0) {
                s.values[i] = value;
                return;
            }
                
            i++;

            if (s.children[i].numKeys == 2 * t - 1) {
                splitNode(s, s.children[i], i);

                if(s.keys[i].compareTo(key) < 0)
                    i++;
            }

            insertNotFull(s.children[i], key, value);
        }
    }

    /**
     * A private method that removes an element from a node.
     * 
     * The removal of an object from the tree has three
     * possible cases:
     *  - Case 1: x contains the key and is a leaf, the
     *      object is just removed;
     *  - Case 2: x contains the key, but is not a leaf.
     *      has 3 subcases (a, b & c);
     *  - Case 3: x doesn't contain the key. has 2
     *      subcases (a & b).
     * 
     * Uses auxiliary methods:
     *    For case 2:
     *      - getHighestKey()/getLowestKey()
     *      - getHighestValue()/getLowestValue()
     *    For case 3:
     *      - borrowFromLeft()/borrowFromRight()
     * 
     *    Both cases can use:
     *      - mergeNodes()
     * 
     * @param x the node being inspected
     * @param key the element to be removed
     */
    private void removeFromNode(BTreeNode x, K key) {
        int i = 0;

        while (i < x.numKeys && x.keys[i].compareTo(key) < 0)
            i++;

        if (i < x.numKeys && x.keys[i].compareTo(key) == 0) {
            // =====================================
            // CASE 1: x CONTAINS key AND IS a LEAF
            // =====================================
            if (x.isLeaf) {
               while (i < x.numKeys - 1) {
                    x.keys[i] = x.keys[i+1];
                    x.values[i] = x.values[i+1];
                    i++;
                }   
                x.keys[x.numKeys - 1] = null;
                x.values[x.numKeys - 1] = null;
                x.numKeys--;
            }
            // =========================================
            // CASE 2: x CONTAINS key AND IS NOT a LEAF
            // =========================================
            else {
                int t = (int) Math.ceil((double) order / 2);
                BTreeNode leftChild = x.children[i];
                BTreeNode rightChild = x.children[i+1];

                // CASE 2a: LEFT children has at least
                // t keys. Replace key with its 
                // predecessor
                if (leftChild.numKeys >= t) {
                    K predKey = getHighestKey(leftChild);
                    V predValue = getHighestValue(leftChild);

                    x.keys[i] = predKey;
                    x.values[i] = predValue;
                    removeFromNode(leftChild, predKey);
                }
                // CASE 2b: RIGHT children has at least
                // t keys. Replace key with its
                // successor
                else if (rightChild.numKeys >= t) {
                    K succKey = getLowestKey(rightChild);
                    V succValue = getLowestValue(rightChild);

                    x.keys[i] = succKey;
                    x.values[i] = succValue;
                    removeFromNode(rightChild, succKey);
                }
                // CASE 2c: BOTH children have t-1 keys. 
                // Move key and ALL keys/values from the 
                // RIGHT child to the LEFT child. Remove 
                // key from LEFT CHILDREN recursively
                else {
                    mergeNodes(x, i, leftChild, rightChild);
                    removeFromNode(leftChild, key);
                }
            }
        }
        // ================================ 
        // CASE 3: x DOES NOT CONTAINS key
        // ================================
        else {
            if (x.isLeaf)
                return;
            
            int t = (int) Math.ceil((double) order/2);
            BTreeNode targetChild = x.children[i];

            if (targetChild.numKeys == t - 1) {
                // CASE 3a: x.child has t - 1 keys, but
                // one of its siblings (x.child[+/- i])
                // has more keys. "Borrow" a key from the
                // respective sibling.
            
                // Check left sibling
                if (i > 0 && x.children[i-1].numKeys >= t) {
                    borrowFromLeft(x, i, targetChild, x.children[i-1]);
                }
                // Check right sibling
                else if (i < x.numKeys && x.children[i+1].numKeys >= t) {
                    borrowFromRight(x, i, targetChild, x.children[i+1]);
                }
                // CASE 3b: x.child and both siblings 
                // have t - 1 keys. Merge x.child with
                // one of its siblings
                else {
                    if (i < x.numKeys) {
                        mergeNodes(x, i, targetChild, x.children[i+1]);
                    } else {
                        mergeNodes(x, i-1, x.children[i-1], targetChild);
                        targetChild = x.children[i-1];
                    }
                }   
            }
            removeFromNode(targetChild, key);
        }
    }

        /* ===== REMOVAL AUXILIARY METHODS =====*/
    /**
     * A private method that merge two nodes.
     * 
     * @param parent the parent node
     * @param i the index of the parent's key 
     * @param y left child
     * @param z right child
     */
    private void mergeNodes(BTreeNode parent, int i, BTreeNode y, BTreeNode z) {
        int t = (int) Math.ceil((double) order/2);

        y.keys[t-1] = parent.keys[i];
        y.values[t-1] = parent.values[i];
        y.numKeys++;

        for (int j = 0; j < z.numKeys; j++) {
            y.keys[j+t] = z.keys[j];
            y.values[j+t] = z.values[j];
            y.numKeys++;
        }

        if (!y.isLeaf) {
            for (int j = 0; j <= z.numKeys; j++) {
                y.children[j+t] = z.children[j];
            }
        }

        for (int j = i; j < parent.numKeys-1; j++) {
            parent.keys[j] = parent.keys[j+1];
            parent.values[j] = parent.values[j+1];
        }

        parent.keys[parent.numKeys-1] = null;
        parent.values[parent.numKeys-1] = null;
        
        for (int j = i + 2; j <= parent.numKeys; j++) {
            parent.children[j-1] = parent.children[j];
        }
        parent.children[parent.numKeys] = null;
        parent.numKeys--;
    }

    /* ===== CASE 2 ===== */
    private K getHighestKey(BTreeNode l) {
        if (!l.isLeaf)
            return getHighestKey(l.children[l.numKeys]);
        else
            return l.keys[l.numKeys-1];
    }

    private K getLowestKey(BTreeNode r) {
        if (!r.isLeaf)
            return getLowestKey(r.children[0]);
        else
            return r.keys[0];
    }

    private V getHighestValue(BTreeNode l) {
        if (!l.isLeaf)
            return getHighestValue(l.children[l.numKeys]);
        else
            return l.values[l.numKeys-1];
    }

    private V getLowestValue(BTreeNode r) {
        if (!r.isLeaf)
            return getLowestValue(r.children[0]);
        else
            return r.values[0];
    }

    /* ===== CASE 3 ===== */
    /**
     * A method to be used in case 3a, if the left 
     * sibing has more than t-1 elements in it.
     * 
     * @param parent the parent node
     * @param i the index of the parent's key
     * @param targetChild the child that'll borrow
     * @param leftSibling the left sibling
     */
    private void borrowFromLeft(BTreeNode parent, int i, BTreeNode targetChild, BTreeNode leftSibling) {
        K lKey = leftSibling.keys[leftSibling.numKeys-1];
        V lValue = leftSibling.values[leftSibling.numKeys-1];

        for (int j = targetChild.numKeys; j > 0; j--) {
            targetChild.keys[j] = targetChild.keys[j-1];
            targetChild.values[j] = targetChild.values[j-1];
        }

        if (!targetChild.isLeaf) {
            for (int j = targetChild.numKeys + 1; j > 0; j--) {
                targetChild.children[j] = targetChild.children[j-1];
            }

            targetChild.children[0] = leftSibling.children[leftSibling.numKeys];
            leftSibling.children[leftSibling.numKeys] = null;
        }

        targetChild.keys[0] = parent.keys[i-1];
        targetChild.values[0] = parent.values[i-1];
        
        parent.keys[i-1] = lKey;
        parent.values[i-1] = lValue;
        
        leftSibling.keys[leftSibling.numKeys-1] = null;
        leftSibling.values[leftSibling.numKeys-1] = null;

        targetChild.numKeys++;
        leftSibling.numKeys--;
    }

    /**
     * A method to be used in case 3a, if the right 
     * sibling has, more than t-1 elements.
     * 
     * @param parent the parent node
     * @param i the index of the parent's kid
     * @param targetChild the child that'll borrow 
     * @param rightSibling the right sibling
     */
    private void borrowFromRight(BTreeNode parent, int i, BTreeNode targetChild, BTreeNode rightSibling) {
        K rKey = rightSibling.keys[0];
        V rValue = rightSibling.values[0];

        targetChild.keys[targetChild.numKeys] = parent.keys[i];
        targetChild.values[targetChild.numKeys] = parent.values[i];

        if (!targetChild.isLeaf) {
            targetChild.children[targetChild.numKeys+1] = rightSibling.children[0];
            
            for (int j = 0; j < rightSibling.numKeys; j++) {
                rightSibling.children[j] = rightSibling.children[j+1];
            }    
            
            rightSibling.children[rightSibling.numKeys] = null;
        }
        
        parent.keys[i] = rKey;
        parent.values[i] = rValue;

        for (int j = 0; j < rightSibling.numKeys-1; j++) {
            rightSibling.keys[j] = rightSibling.keys[j+1];
            rightSibling.values[j] = rightSibling.values[j+1];
        }
        
        rightSibling.keys[rightSibling.numKeys-1] = null;
        rightSibling.values[rightSibling.numKeys-1] = null;

        targetChild.numKeys++;
        rightSibling.numKeys--;
    }

        /* ===== END OF REMOVAL AUXILIARY METHODS ===== */

    /**
     * A private and throughout method to search through 
     * the tree for the key element. Works recursively.
     * 
     * @param x the current node to be searched
     * @param key element to be searched
     * @return V if key is found. Null, if it's not.
     */
    private V searchInNode(BTreeNode x, K key) {      
        // Runs through the array in search of the index 
        // of key as long as i is inside the bounds of 
        // the array and key is bigger than the element 
        // at i
        int i = 0;
        while (i < x.numKeys && x.keys[i].compareTo(key) < 0)
            i += 1;

        // Once "found", if i is inside the bounds of 
        // the array and key is at the i-th position,
        // returns the element in this position
        if (i < x.numKeys && x.keys[i].compareTo(key) == 0)
            return x.values[i];

        // If key wasn't found, then checks if the 
        // current node is a leaf. If it's a leaf, key 
        // isn't on the tree. If the current node isn't 
        // a leaf, calls the search function recursively
        // for the children node at the i-th position.
        if (x.isLeaf)
            return null;
        else {
            return searchInNode(x.children[i], key);
        }
    }

    /**
     * An internal method of the B-Tree, used for 
     * breaking nodes when they get full
     * 
     * @param parent y's parent node
     * @param y the node to be split
     * @param i index of y in parent's children array
     */
    private void splitNode(BTreeNode parent, BTreeNode y, int i) {
        // Creates a new node
        BTreeNode z = new BTreeNode(y.isLeaf);

        // Const t based on the tree's order
        // i.e., the minimum amount of children a node
        // can have
        int t = (int) Math.ceil((double) order/2);

        // Amount of keys that will be transfered to z
        z.numKeys = t-1;

        // Transfer (copy) of the last keys and values
        // from y to z
        for (int j = 0; j < t - 1; j++) {
            z.keys[j] = y.keys[j+t];
            z.values[j] = y.values[j+t];

            y.keys[j+t] = null;
            y.values[j+t] = null;
        }

        // If y isn't leaf, then transfer the  
        // respective children to z
        if(!y.isLeaf) {
            for(int j = 0; j < t; j++) {
                z.children[j] = y.children[j+t];
                y.children[j+t] = null;
            }
        }

        // Adjust remaining keys on y
        y.numKeys = t-1;

        // Since z is a new children of parent, the 
        // children are reorganized (shifted) for z to 
        // be inserted
        for (int j = parent.numKeys; j >= i + 1; j--)
            parent.children[j+1] = parent.children[j];

        // Inserts z on the parent node
        parent.children[i+1] = z;

        // Shifts the parent's keys for the median to be 
        // inserted
        for (int j = parent.numKeys-1; j >= i; j--) {
            parent.keys[j+1] = parent.keys[j];
            parent.values[j+1] = parent.values[j];
        }

        // Copies median from y (index t-1) to parent
        parent.keys[i] = y.keys[t-1];
        parent.values[i] = y.values[t-1];

        // Clears the median, since it went to the parent
        y.keys[t-1] = null;
        y.values[t-1] = null;

        // Increments the parent's key count
        parent.numKeys += 1;
    }

    /* ===== PRIVATE INNER CLASS ===== */
    private class BTreeNode {
        /* ===== ATTRIBUTES ===== */
        public K[] keys;
        public V[] values;
        public BTreeNode[] children;
        public int numKeys;
        public boolean isLeaf;

        @SuppressWarnings("unchecked")
        public BTreeNode(boolean isLeaf) {
            this.isLeaf = isLeaf;
            int t = (int) Math.ceil((double) order /2);
            this.keys = (K[]) new Comparable[2 * t - 1];
            this.values = (V[]) new Object[2 * t - 1];
            this.children = (BTreeNode[]) new BTree.BTreeNode[2 * t];
            this.numKeys = 0;            
        }
        
    }
}
