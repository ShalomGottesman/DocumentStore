package edu.yu.cs.com1320.project.test.stage1;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.Impl.HashTableImpl;

@SuppressWarnings("unused")
public class HashTableTestStage1 {
	final static private boolean Trace = false; //use to trace testing
	
	@Test public void initializeHashTable () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 1, 'initializeHashTable'");
		}
		@SuppressWarnings("rawtypes")
		HashTableImpl table = new HashTableImpl();
	}
	@Test  public void initializeTestNullPut1 () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 2, 'initializeTestNullPut1'");
		}
		HashTableImpl<String, String> table = new HashTableImpl<String, String>();
		table.put("hello there", null);
	}
	@Test (expected = RuntimeException . class) public void initializeTestNullPut2 () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 3, 'initializeTestNullPut2'");
		}
		HashTableImpl<String, String> table = new HashTableImpl<String, String>();
		table.put(null, "hello there");
	}
	@Test (expected = RuntimeException . class) public void initializeTestNullPut3 () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 4, 'initializeTestNullPut3'");
		}
		HashTableImpl<String, String> table = new HashTableImpl<String, String>();
		table.put(null, null);
	}
	@Test public void TestPutValid () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 5, 'TestPutValid'");
		}
		HashTableImpl<String, String> table = new HashTableImpl<String, String>();
		assertTrue(null == table.put("hello there", "how are you today?"));
	}
	@Test (expected = RuntimeException . class) public void TestNullGet () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 6, 'TestNullGet'");
		}
		HashTableImpl<String, String> table = new HashTableImpl<String, String>();
		table.put("hello there", "how are you today?");
		table.get(null);
	}
	@Test public void TestGetBasic () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 7, 'TestGetBasic'");
		}
		HashTableImpl<String, String> table = new HashTableImpl<String, String>();
		table.put("hello there", "how are you today");
		if (Trace == true) {
			System.out.println("  value 1: how are you today");
			System.out.println("  value 2: " + table.get("hello there"));
		}
		assertTrue("how are you today" == table.get("hello there"));
	}
	@Test public void TestDuplicateKeys () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 8, 'TestDuplicateKeys'");
		}
		HashTableImpl<String, String> table = new HashTableImpl<String, String>();
		String key = "hello there";
		table.put(key, "how are you today");
		assertTrue("how are you today" == table.get(key));
		assertTrue("how are you today" == table.put(key, "whats up?"));
		assertTrue("whats up?" == table.get(key));
	}
	@Test public void TestDuplicateKeys2 () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 9, 'TestDuplicateKeys2'");
		}
		HashTableImpl<String, String> table = new HashTableImpl<String, String>();
		String key = "hello there";
		table.put(key, "how are you today");
		assertTrue("how are you today" == table.get(key));
		assertTrue(null == table.put(key, null));
		assertTrue(null == table.get(key));
	}	
	@Test public void TestDuplicateKeys3 () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 10, 'TestDuplicateKeys3'");
		}
		HashTableImpl<String, String> table = new HashTableImpl<String, String>();
		String key = "hello there";
		table.put(key, "how are you today");
		assertTrue("how are you today" == table.get(key));
		assertTrue(null == table.put(key, null));
		assertTrue(null == table.get(key));
		assertTrue(null == table.put(key, "hows it going?"));
		assertTrue("hows it going?".equals(table.get(key)));
	}
	@Test public void TestSeperateChaining () {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 HashTable Test 11, 'TestSeperateChaining'");
		}
		HashTableImpl<Integer, String> table = new HashTableImpl<Integer, String>();
		for(int x = 0; x < 50; x++) {
			table.put(x, "entry " + x);
		}
		int x = 15;
		assertTrue(("entry " + x).equals(table.put(15, "whats up?")));
		assertTrue(("entry 45").equals(table.get(45)));
	}	
}
