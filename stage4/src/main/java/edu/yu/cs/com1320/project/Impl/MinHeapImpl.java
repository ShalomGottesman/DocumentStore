package edu.yu.cs.com1320.project.Impl;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl<T extends Comparable> extends MinHeap<T>{
	
	@SuppressWarnings("unchecked")
	public MinHeapImpl() {
		Object array = new Comparable[2];
		this.elements  = (T[]) array;
		this.elementsToArrayIndex = new HashMap<T, Integer>();
	}

	@Override
	public void reHeapify(Comparable element) {
		//this method assumes the heap is in logical order with a possible exception of one node
		int position = this.getArrayIndex(element);
		if (position == -1) {
			return;
		}
		if ((position*2+1 <= this.elements.length) && //check right child
			(this.elements[position*2] != null) &&	
			(this.elements[position].compareTo((T) this.elements[position*2]) > 0)) {
				this.downHeap(position);
				return;
		}
		if ((position*2+2 <= this.elements.length) && //left child
			(this.elements[position*2+1] != null) && 
			(this.elements[position].compareTo((T) this.elements[position*2 + 1]) > 0)) {
			this.downHeap(position);
				return;
		}
			
		if ((this.elements[position/2] != null) && //parent
			this.elements[position].compareTo(
					(T) this.elements[position/2]) < 0) {
			this.upHeap(position);
			return;
		}
		//if the recently edited doc is neither greater than its parent or less than either of its children, 
		//it is in the right place, no work needed
		return;
	}

	/**
	 * @return if present, position in array, else, -1
	 */
	@Override
	protected int getArrayIndex(Comparable element) {
		/*
		for (int x = 0; x < this.elements.length; x++) {
			//in the MinHeap, the zero slot is the "root" and is left null
			if (this.elements[x] == null) {
				continue;
			}
			if (this.elements[x].equals(element)) {
				return x;
			}
		}
		return -1;
		*/
		if (this.elementsToArrayIndex.get(element) == null) {
			return -1;
		} 
		return this.elementsToArrayIndex.get(element);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doubleArraySize() {
		int currentSize = this.elements.length;
		T[] newArray = (T[]) new Comparable[currentSize*2];
		for (int x = 0; x < currentSize; x++) {
			newArray[x] = this.elements[x];
		}
		this.elements = (T[]) newArray;
	}

	private void swap(int i, int j) {
		T temp1 = this.elements[i];
		T temp2 = this.elements[j];
		this.elements[i] = this.elements[j];
		this.elements[j] = temp1;
		
		this.elementsToArrayIndex.put(temp1, j);//reverse mappings of i and j
		this.elementsToArrayIndex.put(temp2, i);		
	}
	
	@Override
	public void insert(T x) {
		// double size of array if necessary
		if (this.count >= this.elements.length - 1) {
			this.doubleArraySize();
		}
		
		for (int y = 0; y < this.count; y++) {
			if (x.equals(this.elements[y]))
				System.out.println("***WARNING, DOUBLE OF ELEMENT IN HEAP INSERTED***");
		}
		
		
		//add x to the bottom of the heap
		this.elements[++this.count] = x;
		this.elementsToArrayIndex.put(x, this.count);//note count has already been updated, so just this.count is needed
		//percolate it up to maintain heap order property
		this.upHeap(this.count);
	}
	
	@Override
	protected void upHeap(int k) {
		while (k > 1 && this.isGreater(k / 2, k)) {
			this.swap(k, k / 2);
			k = k / 2;
		}
	}
	
	private boolean isGreater(int i, int j) {
		return this.elements[i].compareTo(this.elements[j]) > 0;
	}
	
	protected void downHeap(int k) {
		while (2 * k <= this.count) {
			//identify which of the 2 children are smaller
			int j = 2 * k;
			if (j < this.count && this.isGreater(j, j + 1)) {
				j++;
			}
			//if the current value is < the smaller child, we're done
			if (!this.isGreater(k, j)) {
				break;
			}
			//if not, swap and continue testing
			this.swap(k, j);
			k = j;
        }
    }

	public T removeMin() {
	    if (isEmpty()) {
	        throw new NoSuchElementException("Heap is empty");
	    }
	    T min = this.elements[1];
	    //swap root with last, decrement count
	    this.swap(1, this.count--);
	    //move new root down as needed
	    this.downHeap(1);
	    this.elements[this.count + 1] = null; //null it to prepare for GC
	    return min;
	}
	
	private boolean isEmpty() {
		return this.count == 0;
	}
}
