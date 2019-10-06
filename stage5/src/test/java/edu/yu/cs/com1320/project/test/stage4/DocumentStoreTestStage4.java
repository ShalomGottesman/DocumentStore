package edu.yu.cs.com1320.project.test.stage4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl;

/**
 * what is new:
 * minHeap
 * time reference in Document
 * limits of space and doc count in documentStore
 */
@SuppressWarnings("unused")
public class DocumentStoreTestStage4 {
	final static boolean Trace = false;
	private String UriBase = "thisIsTheUriBase";
	final static private String origString = "Shall we play a game? Love to. How about Global Thermonuclear War? Wouldn't you prefer a good game of chess?";
	//it is known that ZIP compression will reduce this string to a length of 205
	//tests 1-7 deal with imposing limits on documents and ramifications if exceeded
	
	
	
	@Test public void initialize() {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 1 initialize");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
	}
	
	@Test public void setLimits() {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 2 setLimits");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.setMaxDocumentBytes(1000);
		docStore.setMaxDocumentCount(10);
	}
	
	
	@Test public void setLimitsDontExcede() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 3 setLimitsDontExcede");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.setMaxDocumentBytes(1000);
		docStore.setMaxDocumentCount(10);
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		URI uri = new URI(UriBase);
		docStore.putDocument(streamIn, uri);
		String retreivedDoc = docStore.getDocument(uri);
		assertTrue(retreivedDoc.equals(origString));
	}
	/* test wont pass in stage 5
	@Test public void setLimitsAndExcedeDocLimit() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 4 setLimitsAndExcedeDocLimit");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.setMaxDocumentCount(1);
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		URI uri = new URI(UriBase + 1);
		docStore.putDocument(streamIn, uri);
		String retreivedDoc = docStore.getDocument(uri);
		assertTrue(retreivedDoc.equals(origString));
		
		InputStream streamIn2 = new ByteArrayInputStream((origString + 2).getBytes());
		URI uri2 = new URI(UriBase + 2);
		docStore.putDocument(streamIn2, uri2);
		
		String retreivedDoc1 = docStore.getDocument(uri);
		assertTrue(null == retreivedDoc1);
		
		String retreivedDoc2 = docStore.getDocument(uri2);
		assertTrue(retreivedDoc2.equals(origString + 2));
	}
	@Test public void setLimitsAndExcedeDocLimit2() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 5 setLimitsAndExcedeDocLimit2");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.setMaxDocumentCount(4);
		URI[] uris = new URI[5];
		for (int x = 0; x < 5; x++) {
			InputStream streamIn = new ByteArrayInputStream((origString + x).getBytes());
			URI uri = new URI(UriBase + x);
			uris[x] = uri;
			docStore.putDocument(streamIn, uri);
		}
		for (int x = 4; x >= 0; x--) {
			String temp = docStore.getDocument(uris[x]);
			if (x > 0) {
				assertTrue(temp.equals(origString + x));
			}
			if (x == 0) {
				assertTrue(temp == null);
			}
		}
	}
	
	
	@Test public void setLimitsAndExcedeSpaceLimit() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 6 setLimitsAndExcedeSpaceLimit");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.setMaxDocumentBytes(500);
		
		URI[] uris = new URI[3];
		for (int x = 0; x < 3; x++) {
			InputStream streamIn = new ByteArrayInputStream((origString + x).getBytes());
			URI uri = new URI(UriBase + x);
			uris[x] = uri;
			docStore.putDocument(streamIn, uri);
		}
		
		for (int x = 2; x >= 0; x--) {
			String temp = docStore.getDocument(uris[x]);
			if (x > 0) {
				assertTrue(temp.equals(origString + x));
			}
			if ( x == 0) {
				assertTrue(temp == null);
			}
		}
	}
	
	
	@Test (expected = IllegalStateException . class)public void findUriAfterLimit() throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 7 setLimitsAndExcedeDocLimit2");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.setMaxDocumentCount(4);
		URI[] uris = new URI[5];
		for (int x = 0; x < 5; x++) {
			InputStream streamIn = new ByteArrayInputStream((origString + x).getBytes());
			URI uri = new URI(UriBase + x);
			uris[x] = uri;
			docStore.putDocument(streamIn, uri);
		}
		docStore.undo(uris[0]);
	}
	*/

	
	//need to test with the search method
	
	@Test public void updateTimeWithSearch() throws URISyntaxException, InterruptedException {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 8 updateTimeWithSearch");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.setMaxDocumentCount(4);
		String[] docString = {	"of course I would, where would you like to go",
								"havnt the slightest clue, I will let you decide",
								"would you like to go for a walk today",
								"hello there my name is shalom gottesman"};//note that string 1 and 2 have "I"
		URI[] uris = new URI[4];
		for (int x = 0; x < 4; x++) {
			InputStream streamIn = new ByteArrayInputStream((docString[x]).getBytes());
			URI uri = new URI(UriBase + x);
			uris[x] = uri;
			docStore.putDocument(streamIn, uri);//doc 1 and 2 were put in last, so they are closest to the top of the heap
		}
		Thread.sleep(5);//to prevent docs even after search stil having the same timestamp
		docStore.search("i");//this will move docs one and two to the bottom of the heap
		for (int x = 0; x < 2; x++) {//doing this twice will remove two documents from the system
			InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
			URI uri = new URI(UriBase + 5 + x);
			docStore.putDocument(streamIn, uri);
		}
		//to show that search updated the times of elements returned, see that they are still in the system
		assertTrue(docStore.getDocument(uris[1]).equals(docString[1]));
		assertTrue(docStore.getDocument(uris[0]).equals(docString[0]));
	}
	
	@Test public void checkOldDocAfterUndo() throws URISyntaxException, InterruptedException {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 9 checkOldDocAfterUndo");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.setMaxDocumentCount(4);
		String[] docString = {	"of course I would, where would you like to go",
								"havnt the slightest clue, I will let you decide"};//note that string 1 and 2 have "I"
		URI uri = new URI(UriBase);
		InputStream streamIn = new ByteArrayInputStream(docString[0].getBytes());
		docStore.putDocument(streamIn, uri);
		
		assertTrue(docString[0].equals(docStore.getDocument(uri)));
		
		InputStream streamIn2 = new ByteArrayInputStream(docString[0].getBytes());
		docStore.putDocument(streamIn, uri);
		
		assertFalse(docString[0].equals(docStore.getDocument(uri)));
		
		docStore.undo();
		
		assertTrue(docString[0].equals(docStore.getDocument(uri)));
	}
	
	@Test public void checkOldDocAfterUndo2() throws URISyntaxException, InterruptedException {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 10 checkOldDocAfterUndo2");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String[] docString = {	"of course I would, where would you like to go",
								"havnt the slightest clue, I will let you decide",
								"would you like to go for a walk today"};//note that string 1 and 2 have "I"
		URI uri = new URI(UriBase);
		InputStream streamIn = new ByteArrayInputStream(docString[0].getBytes());
		docStore.putDocument(streamIn, uri);
		for (int x = 0; x < 10; x++) {
			URI uri2 = new URI(UriBase + x);
			InputStream strmIn = new ByteArrayInputStream(docString[2].getBytes());
			docStore.putDocument(strmIn, uri2);
		}
		assertTrue(docString[0].equals(docStore.getDocument(uri)));
		InputStream streamIn2 = new ByteArrayInputStream(docString[0].getBytes());
		docStore.putDocument(streamIn, uri);
		assertFalse(docString[0].equals(docStore.getDocument(uri)));
		for (int x = 10; x < 20; x++) {
			URI uri2 = new URI(UriBase + x);
			InputStream strmIn = new ByteArrayInputStream(docString[2].getBytes());
			docStore.putDocument(strmIn, uri2);
		}
		assertFalse(docString[0].equals(docStore.getDocument(uri)));
		docStore.undo(uri);
		assertTrue(docString[0].equals(docStore.getDocument(uri)));
		/**basic rundown:
		 * add doc 1
		 * add a bunch over it
		 * overwrite doc 1
		 * add a bunch more over
		 * undo the overwrite
		 * check for doc one in original form
		 */
	}
	/* test wont pass in stage 5
	@Test (expected = IllegalArgumentException . class)public void () throws URISyntaxException {
		if (Trace == true) {
			System.out.println("\nDocumentStore stage 4 Test: Test 11 insertAboveLimit");
		}
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		docStore.setMaxDocumentBytes(100);
		URI uri = new URI(UriBase);
		InputStream streamIn = new ByteArrayInputStream((origString).getBytes());
		docStore.putDocument(streamIn, uri);
	}
	*/
	
	
	
	
	//still have to test undo for old command and how that interacts with limits
	
}
