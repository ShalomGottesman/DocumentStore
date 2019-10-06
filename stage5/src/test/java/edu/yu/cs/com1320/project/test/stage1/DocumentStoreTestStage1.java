package edu.yu.cs.com1320.project.test.stage1;

import static org.junit.Assert.assertTrue;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;
import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.*;

@SuppressWarnings("unused")
public class DocumentStoreTestStage1 {
	final static private boolean Trace = false; //use to trace testing
	final static private String origString = "Shall we play a game? Love to. How about Global Thermonuclear War? Wouldn't you prefer a good game of chess?";
	
	@Test public void initializeDocumentStore() {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 1, 'initializeDocumentStore'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
	}
	
	@Test public void setNewDefaultCompression() {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 2, 'setNewDefaultCompression'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		assertTrue(DocumentStore.CompressionFormat.ZIP == docStore.getDefaultCompressionFormat());
		docStore.setDefaultCompressionFormat(CompressionFormat.GZIP);
		assertTrue(DocumentStore.CompressionFormat.GZIP == docStore.getDefaultCompressionFormat());
	}
	
	@Test public void inputNewDocZIP() throws URISyntaxException, FileNotFoundException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 3, 'inputNewDocZIP'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.ZIP);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn, uri);
	}
	
	@Test public void inputNewDocJAR() throws URISyntaxException, FileNotFoundException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 4, 'inputNewDocJAR'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.JAR);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn, uri);
	}
	
	@Test public void inputNewDocSEVENZ() throws URISyntaxException, FileNotFoundException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 5, 'inputNewDocSEVENZ'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.SEVENZ);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn, uri);
	}
	
	@Test public void inputNewDocGZIP() throws URISyntaxException, FileNotFoundException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 6, 'inputNewDocGZIP'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.GZIP);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn, uri);
	}
	
	@Test public void inputNewDocBZIP2() throws URISyntaxException, FileNotFoundException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 7, 'inputNewDocBZIP2'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.BZIP2);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn, uri);
	}
	
	@Test public void inputNewDocZIPAndRetrieve() throws URISyntaxException, IOException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 8, 'inputNewDocZIPAndRetrieve'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.ZIP);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		String origDoc = IOUtils.toString(streamIn);
		InputStream streamIn2 = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn2, uri);
		String returnedDoc = docStore.getDocument(uri);	
		assertTrue(origDoc.equals(returnedDoc));
	}
	
	@Test public void inputNewDocJARAndRetrieve() throws URISyntaxException, IOException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 9, 'inputNewDocJARAndRetrieve'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.JAR);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		String origDoc = IOUtils.toString(streamIn);
		InputStream streamIn2 = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn2, uri);
		String returnedDoc = docStore.getDocument(uri);	
		assertTrue(origDoc.equals(returnedDoc));
	}
	
	@Test public void inputNewDocSEVENZAndRetrieve() throws URISyntaxException, IOException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 10, 'inputNewDocSEVENZAndRetrieve'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.SEVENZ);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		String origDoc = IOUtils.toString(streamIn);
		InputStream streamIn2 = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn2, uri);
		String returnedDoc = docStore.getDocument(uri);		
		assertTrue(origDoc.equals(returnedDoc));
	}
	
	@Test public void inputNewDocGZIPAndRetrieve() throws URISyntaxException, IOException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 11, 'inputNewDocGZIPAndRetrieve'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.GZIP);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		String origDoc = IOUtils.toString(streamIn);
		InputStream streamIn2 = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn2, uri);
		assertTrue(origDoc.equals(docStore.getDocument(uri)));
	}
	
	@Test public void inputNewDocBZIP2AndRetrieve() throws URISyntaxException, IOException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 12, 'inputNewDocBZIP2AndRetrieve'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.BZIP2);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		String origDoc = IOUtils.toString(streamIn);
		InputStream streamIn2 = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn2, uri);
		String returnedDoc = docStore.getDocument(uri);		
		assertTrue(origDoc.equals(returnedDoc));
	}
	
	@Test public void deleteDoc() throws URISyntaxException, IOException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 13, 'deleteDoc'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.BZIP2);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		String origDoc = IOUtils.toString(streamIn);
		InputStream streamIn2 = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn2, uri);
		String returnedDoc = docStore.getDocument(uri);	
		assertTrue(origDoc.equals(returnedDoc));		
		assertTrue(true == docStore.deleteDocument(uri));
		assertTrue(null == docStore.getDocument(uri));
	}
	
	@Test public void deleteDocContinue() throws URISyntaxException, IOException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 14, 'deleteDocContinue'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.BZIP2);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		String origDoc = IOUtils.toString(streamIn);
		InputStream streamIn2 = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn2, uri);
		String returnedDoc = docStore.getDocument(uri);	
		assertTrue(origDoc.equals(returnedDoc));		
		assertTrue(true == docStore.deleteDocument(uri));
		assertTrue(null == docStore.getDocument(uri));
		InputStream streamIn3 = new ByteArrayInputStream(origString.getBytes());
		docStore.putDocument(streamIn3, uri);
		assertTrue(origDoc.equals(returnedDoc));
	}
	
	@Test public void hashCodeComparison() throws URISyntaxException, IOException {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 1 DocumentStore Test 15, 'hashCodeComparison'");
		}
		DocumentStore docStore = new DocumentStoreImpl();
		docStore.setDefaultCompressionFormat(CompressionFormat.BZIP2);
		URI uri = new URI("file:///Users//Administrator//Desktop//javaTestingDoc.txt");
		InputStream streamIn = new ByteArrayInputStream(origString.getBytes());
		String origDoc = IOUtils.toString(streamIn);
		InputStream streamIn2 = new ByteArrayInputStream(origString.getBytes());
		assertTrue(origDoc.hashCode() == docStore.putDocument(streamIn2, uri));
	}
}
