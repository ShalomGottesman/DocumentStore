package edu.yu.cs.com1320.project.test.stage3;

import static org.junit.Assert.assertTrue;

import java.awt.List;
import java.util.ArrayList;
import java.util.Comparator;

import org.junit.Test;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.Impl.TrieImpl;

@SuppressWarnings("unused")
public class TrieTestStage3 {
	final static private boolean Trace = false; //use to trace testing
	final static private String str1 = "aaaa aaaa aaaa aaaa bbb bbb bbb ccccc ccccc ccccc ccccc ccccc 123 !@#$%";
	final static private String str2 = "row row row your boat gently down the stream";
	final static private String str3 = "merrily merily merily life is but a dream";
	final static private String key1 = "how";
	final static private String key2 = "Howard";
	final static private String key3 = "HoW";
	
	private TrieImpl<String> costructTrie(){
		Comparator<String> strComp = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				for (int x = 0; x < Math.min(o1.length(), o2.length()); x++ ){
					if (o1.charAt(x) == (o2.charAt(x))) {
						continue;
					} else {
						return o1.charAt(x) - (o2.charAt(x));
					}
				}
				if (o1.length() == o2.length()) {
					return 0;
				}
				if (o1.length() < o2.length()) {
					return o2.charAt(o1.length());
				} else {
					return o1.charAt(o2.length());
				}
			}
		};
		TrieImpl<String> trie = new TrieImpl<String>(strComp);
		return trie;
	}
	
	@Test public void initializeTrie() {
		if (Trace == true) {
			System.out.println("/nTrie stage 3 Test: Test 1 initializeTrie");
		}
		costructTrie();
	}
	
	@Test public void addToTrie() {
		if (Trace == true) {
			System.out.println("/nTrie stage 3 Test: Test 2 addToTrie");
		}
		Trie<String> trie = costructTrie();
		trie.put(key1, str1);
	}
	
	@Test public void addAndDelete() {
		if (Trace == true) {
			System.out.println("/nTrie stage 3 Test: Test 3 addAndDelete");
		}
		Trie<String> trie = costructTrie();
		assertTrue(null == trie.getAllSorted(key1));
		trie.put(key1, str1);
		ArrayList<String> list = new ArrayList<String>();
		list.add(str1);
		assertTrue(list.equals(trie.getAllSorted(key1)));
		trie.delete(key1, str1);
		assertTrue(null == trie.getAllSorted(key1));
	}
	
	@Test public void addAndDelete2() {
		if (Trace == true) {
			System.out.println("/nTrie stage 3 Test: Test 4 addAndDelete2");
		}
		Trie<String> trie = costructTrie();
		assertTrue(null == trie.getAllSorted(key1));
		trie.put(key1, str1);
		trie.put(key2, str2);
		ArrayList<String> list = new ArrayList<String>();
		list.add(str1);
		assertTrue(list.equals(trie.getAllSorted(key1)));
		trie.delete(key1, str1);
		assertTrue(trie.getAllSorted(key1).isEmpty());
		ArrayList<String> list2 = new ArrayList<String>();
		list2.add(str2);
		assertTrue(list2.equals(trie.getAllSorted(key2)));
	}
	/**
	 * test two values at same key, delteing one, sorting them, deleting both
	 * repeat all tests above for two instances, bottom of key chain, and not bottom of chain
	 * test for null inputs?
	 * 
	 */
	@Test public void duplicateKeys() {
		if (Trace == true) {
			System.out.println("/nTrie stage 3 Test: Test 5 duplicateKeys");
		}
		Trie<String> trie = costructTrie();
		trie.put(key1, str2);
		trie.put(key1, str1);
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add(str1);
		list1.add(str2);
		assertTrue(list1.equals(trie.getAllSorted(key1)));
	}
	@Test public void duplicateKeysAndDelete() {
		if (Trace == true) {
			System.out.println("/nTrie stage 3 Test: Test 6 duplicateKeysAndDelete");
		}
		Trie<String> trie = costructTrie();
		trie.put(key1, str2);
		trie.put(key1, str1);
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add(str1);
		list1.add(str2);
		assertTrue(list1.equals(trie.getAllSorted(key1)));
		trie.delete(key1, str1);
		list1.remove(str1);
		assertTrue(list1.equals(trie.getAllSorted(key1)));
	}
	
	@Test public void duplicateKeysAndDeleteAll() {
		if (Trace == true) {
			System.out.println("/nTrie stage 3 Test: Test 7 duplicateKeysAndDeleteAll");
		}
		Trie<String> trie = costructTrie();
		trie.put(key1, str2);
		trie.put(key1, str1);
		trie.deleteAll(key1);
		assertTrue(null == trie.getAllSorted(key1));
		
	}
	@Test public void duplicateKeysAndDeleteAll2() {
		if (Trace == true) {
			System.out.println("/nTrie stage 3 Test: Test 8 duplicateKeysAndDeleteAll2");
		}
		Trie<String> trie = costructTrie();
		trie.put(key1, str2);
		trie.put(key1, str1);
		trie.put(key2, str2);
		trie.put(key2, str3);
		trie.deleteAll(key1);
		assertTrue(trie.getAllSorted(key1).isEmpty());
	}
	
	@Test public void caseNotSensitiveKeys() {
		if (Trace == true) {
			System.out.println("/nTrie stage 3 Test: Test 9 caseNotSensitiveKeys");
		}
		Trie<String> trie = costructTrie();
		trie.put(key1, str2);
		trie.put(key1, str1);
		trie.put(key3, str3);
		trie.put(key2, str3);
		trie.deleteAll(key1);
		assertTrue(trie.getAllSorted(key1).isEmpty());
		assertTrue(trie.getAllSorted(key3).isEmpty());
	}
	
	@Test public void reputAfterDeleteAll() {
		if (Trace == true) {
			System.out.println("/nTrie stage 3 Test: Test 10 reputAfterDeleteAll");
		}
		Trie<String> trie = costructTrie();
		trie.put(key1, str2);
		trie.put(key1, str1);
		trie.put(key3, str3);
		trie.put(key2, str3);
		trie.deleteAll(key1);
		assertTrue(trie.getAllSorted(key1).isEmpty());
		assertTrue(trie.getAllSorted(key3).isEmpty());
		trie.put(key1, str1);
		trie.put(key1, str2);
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add(str1);
		list1.add(str2);
		assertTrue(trie.getAllSorted(key1).equals(list1));
	}
	
}
