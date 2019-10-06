package edu.yu.cs.com1320.project.Impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.yu.cs.com1320.project.Trie;

public class TrieImpl<Value> implements Trie<Value> {
	private static final int alphabetSize = 256; // extended ASCII
    private Node<Value> root; // root of trie
    protected Node<Value> nodeToCompare;
    private Comparator<Value> valueComparator;

    public static class Node<Value>
    {	
        protected List<Value> val;
        @SuppressWarnings("unchecked")
		protected Node<Value>[] links = new Node[TrieImpl.alphabetSize];
        
        Node() {
        	val = new ArrayList<Value>();
        }        
    }
    
	public TrieImpl(Comparator<Value> comp) {
    	this.root = new Node<Value>();
    	this.valueComparator = comp;
    }
    
    /**
     * Returns the value associated with the given key.
     *
     * @param key the key
     * @return the value associated with the given key if the key is in the trie and {@code null} if not
     */
    @Override
    public List<Value> getAllSorted(String key)
    {
    	key = key.toLowerCase();
        Node<Value> x = this.get(this.root, key, 0);
        if (x == null)
        {
            return null;
        }
        x.val.sort(valueComparator);
        return x.val;
    }

    /**
     * A char in java has an int value.
     * see http://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#getNumericValue-char-
     * see http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2
     */
    private Node<Value> get(Node<Value> x, String key, int d)
    {
        //link was null - return null, indicating a miss
        if (x == null)
        {
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (d == key.length())
        {
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        return this.get(x.links[c], key, d + 1);
    }

    @Override
    public void put(String key, Value val)
    {
    	key = key.toLowerCase();
        //deleteAll the value from this key
        if (val == null)
        {
            this.deleteAll(key);
        }
        else
        {
            this.root = put(this.root, key, val, 0);
        }
    }
    /**
     *
     * @param x
     * @param key
     * @param val
     * @param d
     * @return
     */
    private Node<Value> put(Node<Value> x, String key, Value val, int d)
    {
        //create a new node
        if (x == null)
        {
            x = new Node<Value>();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length())
        {
        	if (!x.val.contains(val)) {
        		x.val.add(val);
        	}
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

	@Override
    public void deleteAll(String key)
    {
		key = key.toLowerCase();
        this.root = deleteAll(this.root, key, 0);
    }

    private Node<Value> deleteAll(Node<Value> x, String key, int d)
    {
        if (x == null)
        {
            return null;
        }
        //we're at the node to del - set the val to null
        if (d == key.length())
        {
        	x.val.clear();
        }
        //continue down the trie to the target node
        else
        {
            char c = key.charAt(d);
            x.links[c] = this.deleteAll(x.links[c], key, d + 1);
        }
        //this node has a val – do nothing, return the node
        if ((!x.val.isEmpty()) )
        {
            return x;
        }
        //remove subtrie rooted at x if it is completely empty	
        for (int c = 0; c <TrieImpl.alphabetSize; c++)
        {
            if (x.links[c] != null)
            {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

	@Override
	public void delete(String key, Value val) {
		key = key.toLowerCase();
		this.root = delete(this.root, key, val, 0);
	}
	
	private Node<Value> delete(Node<Value> x, String key, Value val, int d)
	{
	    if (x == null)
	    {
	        return null;
	    }
	    //we're at the node to delete the value from - remove that value
	    if (d == key.length())
	    {
	    	boolean present = true;
	    	while (present == true) {
	    		present = x.val.remove(val);//this way, all instances of the document is removed from this list, not just one
	    	}
	    
	    }
	    //continue down the trie to the target node
	    else
	    {
	        char c = key.charAt(d);
	        x.links[c] = this.delete(x.links[c], key, val, d + 1);
	    }
	    //this node has a val – do nothing, return the node
	    if ((!x.val.isEmpty()) )//|| (x.val != null))
	    {
	        return x;
	    }
	    //remove subtrie rooted at x if it is completely empty	
	    for (int c = 0; c <TrieImpl.alphabetSize; c++)
	    {
	    	
	    	if (x.links[c] != null)
	        {
	            return x; //not empty
	        }
	    }
	    //empty - set this link to null in the parent
	    return null;
	}
}
