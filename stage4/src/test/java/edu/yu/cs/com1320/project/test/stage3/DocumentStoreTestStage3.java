package edu.yu.cs.com1320.project.test.stage3;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Test;

import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl;

@SuppressWarnings("unused")
public class DocumentStoreTestStage3 {
	final static private boolean Trace = false; //use to trace testing
	final static private String doc1 = "aaaa aaaa aaaa aaaa bbb bbb bbb ccccc ccccc ccccc ccccc ccccc 123 !@#$%";
	final static private String doc2 = "aaaa aaaa aaaa aaaa bbb bbb bbb ccccc ccccc ccccc ccccc ccccc 123 !@#$%";
	final static private String doc3 = "aaaa aaaa aaaa aaaa bbb bbb bbb ccccc ccccc ccccc ccccc ccccc 123 !@#$%";
	private static InputStream stream1;
	private static InputStream stream2;
	private static InputStream stream3;
	private static URI uri1;
	private static URI uri2;
	private static URI uri3;
	
	
	/** what to test:
	 * create documentStore, create document to store, find document from keywords
	 * 
	 */
	
	private static void setVars() {
		try {
			stream1 = new ByteArrayInputStream(doc1.getBytes());
			stream2 = new ByteArrayInputStream(doc2.getBytes());
			uri1 = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
			uri2 = new URI("file:///Users//Administrator//Desktop//javaTestingDoc1.txt");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test public void init() {
		if (Trace == true) {
			System.out.println("/nDocumentStore stage 3 Test: Test 1 init");
		}
		setVars();
		DocumentStoreImpl docStore = new DocumentStoreImpl();
	}
	@Test public void put() {
		if (Trace == true) {
			System.out.println("/nDocumentStore stage 3 Test: Test 1 init");
		}
		setVars();
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.putDocument(stream1, uri1);
	}
	@Test public void putAndGet() {
		if (Trace == true) {
			System.out.println("/nDocumentStore stage 3 Test: Test 1 init");
		}
		setVars();
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.putDocument(stream1, uri1);
		docStore.getDocument(uri1);
	}
	@Test public void putAndDelete() {
		if (Trace == true) {
			System.out.println("/nDocumentStore stage 3 Test: Test 1 init");
		}
		setVars();
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.putDocument(stream1, uri1);
		docStore.deleteDocument(uri1);
		assertTrue(null == docStore.getDocument(uri1));
	}
	@Test public void putAndUndo() {
		if (Trace == true) {
			System.out.println("/nDocumentStore stage 3 Test: Test 1 init");
		}
		setVars();
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.putDocument(stream1, uri1);
		assertTrue(doc1.equals(docStore.getDocument(uri1)));
		docStore.undo();
		assertTrue(null == docStore.getDocument(uri1));
	}
	@Test public void putAndUndoURI() {
		if (Trace == true) {
			System.out.println("/nDocumentStore stage 3 Test: Test 1 init");
		}
		setVars();
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.putDocument(stream1, uri1);
		docStore.putDocument(stream2, uri2);
		assertTrue(doc1.equals(docStore.getDocument(uri1)));
		docStore.undo(uri1);
		assertTrue(null == docStore.getDocument(uri1));
	}
	@Test public void putAndSearch() {
		if (Trace == true) {
			System.out.println("/nDocumentStore stage 3 Test: Test 1 init");
		}
		setVars();
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.putDocument(stream1, uri1);
		docStore.putDocument(stream2, uri2);
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add(doc2);
		list1.add(doc1);
		assertTrue(list1.equals(docStore.search("aaaa")));
	}
	
	@Test public void putDeleteSearch() {
		if (Trace == true) {
			System.out.println("/nDocumentStore stage 3 Test: Test 1 init");
		}
		setVars();
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.putDocument(stream1, uri1);
		docStore.putDocument(stream2, uri2);
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add(doc2);
		list1.add(doc1);
		assertTrue(list1.equals(docStore.search("aaaa")));
		docStore.deleteDocument(uri1);
		list1.remove(doc1);
		assertTrue(list1.equals(docStore.search("aaaa")));
	}
	


}
