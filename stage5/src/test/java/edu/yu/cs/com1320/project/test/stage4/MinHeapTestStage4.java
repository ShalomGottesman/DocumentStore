package edu.yu.cs.com1320.project.test.stage4;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.yu.cs.com1320.project.Impl.MinHeapImpl;

@SuppressWarnings("unused")
public class MinHeapTestStage4 {
	final static boolean Trace = false;
	private String str1 = "hellothere";
	private String str2 = "jellothere"; //"j" comes after h, so it is 'after" str1
	
	
	@Test public void initialize() {
		if (Trace == true) {
			System.out.println("\nMinHeap stage 4 Test: Test 1 initialize");
		}
		MinHeapImpl<String> heap = new MinHeapImpl<String>();
	}
	
	@Test public void add1Element() {
		if (Trace == true) {
			System.out.println("\nMinHeap stage 4 Test: Test 2 add1Element");
		}
		MinHeapImpl<String> heap = new MinHeapImpl<String>();
		heap.insert(str1);
	}
	
	@Test public void addAndGet() {
		if (Trace == true) {
			System.out.println("\nMinHeap stage 4 Test: Test 3 addAndGet");
		}
		MinHeapImpl<String> heap = new MinHeapImpl<String>();
		heap.insert(str1);
		String newStr = (String) heap.removeMin();
		assertTrue(newStr.equals(str1));
	}
	
	@Test public void add2Elements() {
		if (Trace == true) {
			System.out.println("\nMinHeap stage 4 Test: Test 4 add2Elements");
		}
		MinHeapImpl<String> heap = new MinHeapImpl<String>();
		heap.insert(str1);
		heap.insert(str2);
	}
	
	@Test public void add2ElementsAndGet() {
		if (Trace == true) {
			System.out.println("\nMinHeap stage 4 Test: Test 5 add2ElementsAndGet");
		}
		MinHeapImpl<String> heap = new MinHeapImpl<String>();
		heap.insert(str1);
		heap.insert(str2);
		String str3 = heap.removeMin();
		assertTrue(str3.equals(str1));
	}
	
	//ends basic testing, now test with objects
	private class comparableObject implements Comparable<comparableObject>{
		public String thisString;
		
		comparableObject(String str) {
			this.thisString = str;
		}
		
		public String getString() {
			return this.thisString;
		}
		
		public void setString(String newString) {
			this.thisString = newString;
		}
		@Override
		public int compareTo(comparableObject o) {
			return this.getString().compareTo(o.getString());
		}
		@Override
		public boolean equals(Object o) {
			if (this.getClass() == o.getClass()) {
				if (this.getString().equals(((comparableObject) o).getString())) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
	
	@Test public void initForObjectsAndAdd() {
		if (Trace == true) {
			System.out.println("\nMinHeap stage 4 Test: Test 6 initForObjectsAndAdd");
		}
		MinHeapImpl<comparableObject> heap = new MinHeapImpl<comparableObject>();
		comparableObject obj1 = new comparableObject(str1);
		comparableObject obj2 = new comparableObject(str2);
		heap.insert(obj1);
		heap.insert(obj2);
		comparableObject obj3 = heap.removeMin();
		assertTrue(obj3.equals(obj1));
	}
	
	@Test public void addAndChange() {
		if (Trace == true) {
			System.out.println("\nMinHeap stage 4 Test: Test 7 addAndChange");
		}
		MinHeapImpl<comparableObject> heap = new MinHeapImpl<comparableObject>();
		comparableObject obj1 = new comparableObject(str1);
		comparableObject obj2 = new comparableObject(str2);
		heap.insert(obj1);
		heap.insert(obj2);
		obj1.setString("zoo");
		comparableObject obj3 = heap.removeMin();
		assertTrue(obj3.equals(obj1));
	}
	
	@Test public void changeAndReheapifyDown() {
		if (Trace == true) {
			System.out.println("\nMinHeap stage 4 Test: Test 8 changeAndReheapifyDown");
		}
		MinHeapImpl<comparableObject> heap = new MinHeapImpl<comparableObject>();
		comparableObject obj1 = new comparableObject(str1);
		comparableObject obj2 = new comparableObject(str2);
		heap.insert(obj1);
		heap.insert(obj2);
		String newStr = "zoo";
		obj1.setString(newStr);
		heap.reHeapify(obj1);
		comparableObject obj3 = heap.removeMin();
		assertTrue(!obj3.equals(obj1));
	}
	
	@Test public void changeAndReheapifyUp() {
		if (Trace == true) {
			System.out.println("\nMinHeap stage 4 Test: Test 9 changeAndReheapifyUp");
		}
		MinHeapImpl<comparableObject> heap = new MinHeapImpl<comparableObject>();
		comparableObject obj1 = new comparableObject(str1);
		comparableObject obj2 = new comparableObject(str2);
		heap.insert(obj1);
		heap.insert(obj2);
		String newStr = "azoo";
		obj2.setString(newStr);
		heap.reHeapify(obj2);		
		comparableObject obj3 = heap.removeMin();
		assertTrue(!obj3.equals(obj1));
	}
	
	@Test public void manyObjects() {
		if (Trace == true) {
			System.out.println("\nMinHeap stage 4 Test: Test 10 manyObjects");
		}
		String alph = "bbcdefghijfklmnopqrstuvwxyz";
		MinHeapImpl<comparableObject> heap = new MinHeapImpl<comparableObject>();
		comparableObject obj0 = null;
		comparableObject obj10 = null;
		comparableObject obj26 = null;
		for (int x = 0; x < 26; x++) {
			comparableObject obj = new comparableObject(str1 + alph.charAt(x));
			if (x == 0 ) {
				obj0 = obj;
			}
			if (x == 10) {
				obj10 = obj;
			}
			if (x == 25) {
				obj26 = obj;
			}
			heap.insert(obj);
		}
		obj10.setString(str1 + "a");
		heap.reHeapify(obj10);
		comparableObject tempObj = heap.removeMin();
		assertTrue(tempObj.equals(obj10));
		
		obj26.setString(str1);
		heap.reHeapify(obj26);
		assertTrue(heap.removeMin().equals(obj26));
		
		obj0.setString(str1 + "zz");
		heap.reHeapify(obj0);
		assertTrue(!heap.removeMin().equals(obj0));
	}
}
