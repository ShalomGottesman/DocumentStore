package edu.yu.cs.com1320.project;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
	private HashTableNode<Key, Value>[] hashedKeyArray;
	private int modulo;
	
	public HashTableImpl() {
		this.initialize(); //creates an instance of this class
	}
	
	@SuppressWarnings("unchecked")
	private void initialize() {
		this.hashedKeyArray = new HashTableNode[200]; //the number 20 is arbitrary
		this.modulo = 200; //note that this number what ever it is must always match the size of the hashedKeyArray array
	}
	
	public Value get(Key k) {
		if (k == null) {
			throw new IllegalArgumentException();
		}
		int keyHashCode = k.hashCode();
		int hashBucket = keyHashCode % this.modulo; //find which bucket this key would be stored in
		HashTableNode<Key, Value> bucketHead = this.hashedKeyArray[hashBucket]; //get bucket head
		if (bucketHead == null) {
			return null; // the bucket head was null, therefore this key is not in the table
		}
		if (bucketHead.keyComparison(k)) { //the key at the head was a match, we have our node, return the value
			return bucketHead.getValue();
		}
		HashTableNode<Key, Value> current = bucketHead; //it wasnt the head, but it might be something down the linked list from the head
		while (current.getNextNode() != null) {//for each node that doesnt have the next node as not in the table...
			current = current.getNextNode();//move forward along the list
			if (current.keyComparison(k)) {//found a match, return the value
				return current.getValue();
			}
		}
		return null;//didnt find key in the table, return null
	}
	public Value put(Key k, Value v) {
		if (k == null) { //the key itself cannot be null, what would it hash to?
			throw new IllegalArgumentException();
		}
		HashTableNode<Key, Value> temp = new HashTableNode<Key, Value>(k, v);		
		Value results = this.search(temp);//search the table for this key, if it is present, then exchange the old value for that key with the new one. else return null to have the key value pair added
		if (results == null) {//this key was not present in table
			this.addToTable(temp); //so add it to the table
			return null;// and return null indicating the key was added
		}
		return results; //if the key was found, return the previous results found at that node
	}
	
	private Value search(HashTableNode<Key, Value> node) {//internal search method to find a value for a key
		int hashValue = node.hashCode();
		int hashBucket = hashValue % this.modulo;
		HashTableNode<Key, Value> bucketHead = this.hashedKeyArray[hashBucket];
		if (bucketHead == null) {
			return null;
		}
		if (bucketHead.keyComparison(node.getKey())) {
			Value currentVal = bucketHead.getValue();
			bucketHead.setValue(node.getValue());
			return currentVal;
		}
		HashTableNode<Key, Value> current = bucketHead; //up until here this is a repeat from a different method, find the bucket this key would be in and get the head
		while (current.getNextNode() != null) {//again, follow list down the chain
			current = current.getNextNode();
			if (current.keyComparison(node.getKey())) {//if the key is found..
				Value currentVal = bucketHead.getValue();//save the old value
				current.setValue(node.getValue());//replace the old value with the new one assoicated with this key
				return currentVal;//return the old value
			}
		}
		return null; //key was not found, return null
	}
	
	private void addToTable (HashTableNode<Key, Value> newNode) {//this key is new to the table, add it and its value to the table
		int hashValue = newNode.hashCode();
		int hashBucket = hashValue % this.modulo;
		HashTableNode<Key, Value> bucketHead = this.hashedKeyArray[hashBucket];
		if (bucketHead == null) {//if there is no node at this slot of the array...
			this.hashedKeyArray[hashBucket] = newNode;//make this spot the node
			return; 
		}
		HashTableNode<Key, Value> current = bucketHead; //if there is a node at the head, store it
		while (current.getNextNode() != null) {//follow linked list down to the end of the list
			current = current.getNextNode();
		}
		current.setAsNextNode(newNode);//set the next node field of the last node in the list to the new node
		return;
	}
	
	private class HashTableNode<Key, Value> { //this is the node class, it is not accessible to the public
		private Key key;
		private Value value;
		private HashTableNode<Key, Value> nextNodeInList;
		
		protected HashTableNode(Key k, Value v) {
			this.key = k;
			this.value = v;
			this.nextNodeInList = null;		
		}
		
		protected void setValue(Value v) {
			this.value = v;
		}
		
		protected void setAsNextNode(HashTableNode<Key, Value> node) {
			this.nextNodeInList = node;
		}
		
		protected HashTableNode<Key, Value> getNextNode() {
			return nextNodeInList;
		}
		
		protected Key getKey() {
			return this.key;
		}
		protected Value getValue() {
			return this.value;
		}
		
		protected boolean keyComparison(Key thatKey) {
			if (this.getKey().equals(thatKey)) {
				return true;
			} else {
				return false;
			}
		}
		@Override
		public boolean equals(Object that) {
			if (that == null) {
				return false;
			}
			if (that.getClass() == this.getClass()) { //first verify they are the same class, if false they cannot be the same object
				if ((this.getKey() == ((HashTableNode<Key, Value>) that).getKey()) && (this.getValue() == ((HashTableNode<Key, Value>) that).getValue())){//two nodes are equal only if their keys and values match
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			int result = this.key != null ? this.key.hashCode() : 0;
			//result = 31 * result + (value != null ? value.hashCode() : 0); 
			return result;
		}
		@Override
		public String toString() {
			return ("Key: " + this.getKey().toString() +
					", Value: " + this.getValue().toString());
		}
	}
}
