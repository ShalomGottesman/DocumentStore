package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T>{
	private T[] stack; //the stack itself in the form of an array
	private int counter; //counter of how many elements are in the stack
		//note the counter will always be pointing to the next empty element in the array, not the last entered one, that position is x-1
	@SuppressWarnings("unchecked")
	public StackImpl() {
		this.stack = (T[]) new Object[10];
		this.counter = 0; 
	}

	@Override
	public void push(T element) {
		if (element == null) {
			throw new IllegalArgumentException();
		}
		this.arrayResizingUp();//catch if array requires to be enlarged
		this.stack[counter] = element; // add new element to array at position counter
		counter++; //increase the counter as there is a new element
		return;
	}

	@Override
	public T pop() {
		if (counter == 0) { //if there is nothing, there is nothing
			return null;
		}
		T oldElement = stack[counter - 1]; //Retrieve last added element;
		this.stack[counter - 1] = null; // set that old position to null
		this.counter--;
		arrayResizingDown();
		if (this.counter == 0) {
		}
		return oldElement; //return old element
	}

	@Override
	public T peek() {
		if (counter == 0) { //if there is nothing, there is nothing
			return null;
		}
		return stack[counter - 1]; //Retrieve last added element;
	}

	@Override
	public int size() {
		return counter;//number of elements in stack
	}
	
	@SuppressWarnings("unchecked")
	private void arrayResizingUp() {//this method will be called as a check to make sure the array can hold the new element, if it is too small, double, else do nothing
		//System.out.println("elements in stack: " + this.counter);
		//System.out.println("stack array size : " + this.stack.length);
		int currentStackSize = this.stack.length;
		if (currentStackSize-3 < this.counter) {//when stack is near full
			//System.out.println("array doubling");
			T[] newStack =  (T[]) new Object[currentStackSize*2]; //create new stack of double the size
			for (int x  = 0; x < this.counter; x++) {//for each element in the old stack
				newStack[x] = this.stack[x]; //copy it into the new one
			}
			this.stack = newStack;//copy reference of new stack into the stack field, now this stack object is larger
			//System.out.println("new size: " + this.stack.length);
		}
		return;
	}
	
	@SuppressWarnings("unchecked")
	private void arrayResizingDown() {//this method will be called to make sure the array is not too large in comparison to the data it is holding;
		//System.out.println("elements in stack: " + this.counter);
		//System.out.println("stack array size : " + this.stack.length);
		int currentStackSize = this.stack.length;
		if (currentStackSize < 52) {
			return;
		}
		if (currentStackSize/4 > this.counter) {//when stack is less than a quarter full
			//System.out.println("array reducing");
			T[] newStack = (T[]) new Object[currentStackSize/2];//create new stack of half the size
			for (int x = 0; x < this.counter; x++) {//for each...
				newStack[x] = this.stack[x];//copy it over
			}
			this.stack = newStack;//copy the reference
			//System.out.println("new size: " + this.stack.length);
		}
	}
}

