package edu.yu.cs.com1320.project.test.stage2;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import edu.yu.cs.com1320.project.DocumentStore;
import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl;

@SuppressWarnings("unused")
public class DocumentStoreTestStage2 {
	final static private boolean Trace = false; //use to trace testing
	final static private String origString = "Shall we play a game? Love to. How about Global Thermonuclear War? Wouldn't you prefer a good game of chess?";

	
	@Test public void initializeAndPut() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 DocumentStore Test 1, 'initializeAndPut'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn, uri);
	}
	@Test public void initializeAndPutAndDelete() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 DocumentStore Test 2, 'initializeAndPutAndDelete'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn, uri);
		docStore.deleteDocument(uri);
	}
	@Test public void PutAndUndoSingle() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 DocumentStore Test 3, 'PutAndUndoSingle'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn, uri);
		docStore.undo();
		assertTrue(null == docStore.getDocument(uri));
		assertTrue(null == docStore.getCompressedDocument(uri));
	}
	@Test public void deleteAndUndoSingle() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 DocumentStore Test 4, 'deleteAndUndoSingle'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn, uri);
		docStore.deleteDocument(uri);
		docStore.undo();
		assertTrue(origString.equals(docStore.getDocument(uri)));
	}
	@Test (expected = RuntimeException . class) public void justUndo() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 DocumentStore Test 5, 'justUndo'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.undo();
	}
	@Test (expected = RuntimeException . class) public void undoNotPresent() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 DocumentStore Test 6, 'undoNotPresent'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		String uriBase = "file:///Users//Administrator//Desktop//javaTestingDoc";
		for (int x = 0; x < 5; x++) {
			URI uri = new URI(uriBase + x + ".txt");
			InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
			docStore.putDocument(streamIn, uri);	
		}
		docStore.undo(new URI(uriBase + 7 + ".txt"));
	}
	
	@Test public void undoPutCommandFound() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 DocumentStore Test 7, 'undoPutCommandFound'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		String uriBase = "file:///Users//Administrator//Desktop//javaTestingDoc";
		String uriEnd = ".txt";
		URI uriToFind = null;
		for (int x = 0; x < 10; x++) {
			URI uri = new URI(uriBase + x + uriEnd);
			if (x == 5) {
				uriToFind = uri;
			}
			String doc = origString + x;
			InputStream streamIn = new ByteArrayInputStream(doc.getBytes());
			docStore.putDocument(streamIn, uri);		
		}
		assertTrue(true == docStore.undo(uriToFind));
		assertTrue(null == docStore.getDocument(uriToFind));
	}
	
	@Test public void undoPutCommandFoundButFalse() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 DocumentStore Test 8, 'undoPutCommandFoundButFalse'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		String uriBase = "file:///Users//Administrator//Desktop//javaTestingDoc";
		String uriEnd = ".txt";
		URI uriToFind = null;
		for (int x = 0; x < 10; x++) {
			URI uri = new URI(uriBase + x + uriEnd);
			if (x == 5) {
				uriToFind = uri;
			}
			docStore.putDocument(null, uri);		
		}
		assertTrue(null == docStore.getDocument(uriToFind));
		assertTrue(false == docStore.undo(uriToFind));
	}
	@Test public void undoDeleteCommandMidStack() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 DocumentStore Test 8, 'undoPutCommandFoundButFalse'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		String uriBase = "file:///Users//Administrator//Desktop//javaTestingDoc";
		String uriEnd = ".txt";
		URI uriToFind = null;
		for (int x = 0; x < 6; x++) {
			URI uri = new URI(uriBase + x + uriEnd);
			if (x == 5) {
				uriToFind = uri;
			}
			String doc = origString + x;
			InputStream streamIn = new ByteArrayInputStream(doc.getBytes());
			docStore.putDocument(streamIn, uri);		
		}
		docStore.deleteDocument(uriToFind);
		for (int x = 6; x < 10; x++) {
			URI uri = new URI(uriBase + x + uriEnd);
			String doc = origString + x;
			InputStream streamIn = new ByteArrayInputStream(doc.getBytes());
			docStore.putDocument(streamIn, uri);
		}
		docStore.undo(uriToFind);
		assertTrue((origString + 5).equals(docStore.getDocument(uriToFind)));
	}
	
}
