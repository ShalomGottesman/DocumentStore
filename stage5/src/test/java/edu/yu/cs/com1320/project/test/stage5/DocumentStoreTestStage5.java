package edu.yu.cs.com1320.project.test.stage5;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl;
import edu.yu.cs.com1320.project.JDCompressionUtility;

@SuppressWarnings("unused")
public class DocumentStoreTestStage5 {
	final static private boolean Trace = false; //use to trace testing
	private File testingSubDirectory;
	
	
	@Test public void initialize() {
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 1 initialize");}
		DocumentStoreImpl dsi = new DocumentStoreImpl();
	}
	@Test public void addSingle() throws URISyntaxException {
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 2 addSingle");}
		DocumentStoreImpl dsi = new DocumentStoreImpl();
		URI uri = new URI("one.doca");
		InputStream in = new ByteArrayInputStream("this is a document".getBytes());
		dsi.putDocument(in, uri);
	}
	
	@Test public void undoURIpulledAndPushed() throws URISyntaxException {//this tests undo of a new put doc
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 3 undoURIpulledAndPushed");}
		DocumentStoreImpl dsi = new DocumentStoreImpl();
		int x = setBaseDir(dsi);
		//test a put sends two documents to disk, one is called via a search, then undo the origional
		//create and add doc1
        String str1 = "this is doc#1";
        URI uri1 = new URI("http://www.yu.edu/doc1"); //size: 127
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        sleep();
        //create and add doc2
        String str2 = "this is doc#2 this is doc#2"; //size, 131
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        sleep();
        //create and add doc3
        String str3 = "this is doc#3 this is doc#3 this is doc#3"; //size, 131
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        sleep();
        //create and add doc4
        String str4 = "this is doc#4 this is doc#4 this is doc#4 this is doc#4"; //size, 132
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        sleep();
        
        dsi.setMaxDocumentBytes(525);
        
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc5.json").exists());
        
        //create and add doc5, this should push out 1 and 2
        String str5 = "this is a document that will push out the others to disk. hoooooraaaa"; //size, 172
        URI uri5 = new URI("http://www.yu.edu/doc5");
        bis = new ByteArrayInputStream(str5.getBytes());
        dsi.putDocument(bis,uri5);
        sleep();
        
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc5.json").exists());
        
        dsi.getDocument(uri1);//pushs out doc 3
        
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertTrue  (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertTrue  (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc5.json").exists());
        
        dsi.undo(uri5);//this should guarantee that doc 1 and 2 should be in memory and a get on doc 5 should return null
        
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertFalse  (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertTrue  (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc5.json").exists());
        assertNull  ("after undoing document 5, doc5 should no longer exist in the dsi to be called, but returned a value", dsi.getDocument(uri5));
        cleanUpFolder();
	}
	
	@Test public void undoURIpulledAndPushedOfOverwritePut() throws URISyntaxException, IOException {//this tests undo of an overwrite put doc
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 4 undoURIpulledAndPushedOfOverwritePut");}
		DocumentStoreImpl dsi = new DocumentStoreImpl();
		int x = setBaseDir(dsi);
		//test a put sends two documents to disk, one is called via a search, then undo the origional
		//create and add doc1
        String str1 = "this is doc#1"; //size: 127
        URI uri1 = new URI("http://www.yu.edu/doc1"); 
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        sleep();
        //create and add doc2
        String str2 = "this is doc#2 this is doc#2"; //size, 131
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        sleep();
        //create and add doc3
        String str3 = "this is doc#3 this is doc#3 this is doc#3"; //size, 131
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        sleep();
        //create and add doc4
        String str4 = "this is doc#4 this is doc#4 this is doc#4 this is doc#4"; //size, 132
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        sleep();
        
        
        dsi.setMaxDocumentBytes(525);
        
        
        //create and readd doc4, this should push out 1 
        String str5 = "this is a document that will push out the others to disk. hoooooraaaa" +
        				"and it is going to use uri4, so it must push everything over the edge while" +
        				"doing an overwrite"; //size, 243
        bis = new ByteArrayInputStream(str5.getBytes());
        dsi.putDocument(bis,uri4);
        sleep();
        
        assertTrue  (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertTrue  (dsi.getDocument(uri4).equals(str5));
        
        dsi.undo(uri4);
        
        assertFalse  (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertFalse  (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertTrue  (dsi.getDocument(uri4).equals(str4));
        cleanUpFolder();
    }
	
	@Test public void testUndoNullInput() throws URISyntaxException, IOException {//this tests undo of a null overwrite put doc
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 5 testUndoNullInput");}
		DocumentStoreImpl dsi = new DocumentStoreImpl();
		int x = setBaseDir(dsi);
		//test a put sends two documents to disk, one is called via a search, then undo the origional
		//create and add doc1
        String str1 = "this is doc#1"; //size: 127
        URI uri1 = new URI("http://www.yu.edu/doc1"); 
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        sleep();
        //create and add doc2
        String str2 = "this is doc#2 this is doc#2"; //size, 131
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        sleep();
        //create and add doc3
        String str3 = "this is doc#3 this is doc#3 this is doc#3"; //size, 131
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        sleep();
        //create and add doc4
        String str4 = "this is doc#4 this is doc#4 this is doc#4 this is doc#4"; //size, 132
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        sleep();
        
        dsi.putDocument(null, uri4);
        assertTrue(str1.equals(dsi.getDocument(uri1)));
        assertTrue(str2.equals(dsi.getDocument(uri2)));
        assertTrue(str3.equals(dsi.getDocument(uri3)));
        assertFalse(str4.equals(dsi.getDocument(uri4)));
        
        dsi.undo(uri4);
        
        assertTrue(str1.equals(dsi.getDocument(uri1)));
        assertTrue(str2.equals(dsi.getDocument(uri2)));
        assertTrue(str3.equals(dsi.getDocument(uri3)));
        assertTrue(str4.equals(dsi.getDocument(uri4)));
        cleanUpFolder();
	}
	
	@Test public void testUndoDelete() throws URISyntaxException, IOException {//this tests undo of a null overwrite put doc
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 6 testUndoDelete");}
		DocumentStoreImpl dsi = new DocumentStoreImpl();
		int x = setBaseDir(dsi);
		//test a put sends two documents to disk, one is called via a search, then undo the origional
		//create and add doc1
        String str1 = "this is doc#1"; //size: 127
        URI uri1 = new URI("http://www.yu.edu/doc1"); 
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        sleep();
        //create and add doc2
        String str2 = "this is doc#2 this is doc#2"; //size, 131
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        sleep();
        //create and add doc3
        String str3 = "this is doc#3 this is doc#3 this is doc#3"; //size, 131
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        sleep();
        //create and add doc4
        String str4 = "this is doc#4 this is doc#4 this is doc#4 this is doc#4"; //size, 132
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        sleep();
        
        dsi.deleteDocument(uri4);
        assertTrue(str1.equals(dsi.getDocument(uri1)));
        assertTrue(str2.equals(dsi.getDocument(uri2)));
        assertTrue(str3.equals(dsi.getDocument(uri3)));
        assertFalse(str4.equals(dsi.getDocument(uri4)));
        
        dsi.undo(uri4);
        
        assertTrue(str1.equals(dsi.getDocument(uri1)));
        assertTrue(str2.equals(dsi.getDocument(uri2)));
        assertTrue(str3.equals(dsi.getDocument(uri3)));
        assertTrue(str4.equals(dsi.getDocument(uri4)));
        cleanUpFolder();
	}
	
	@Test public void undoCallsDeletedDoc() throws URISyntaxException {
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 7 undoCallsDeletedDoc");}
		DocumentStoreImpl dsi = new DocumentStoreImpl();
		int x = setBaseDir(dsi);
		dsi.setMaxDocumentCount(3);
		String[] strs = createStrings(5);
		URI[] uris = createURIs(5);
		for (int z = 0; z < 5; z++) {
			ByteArrayInputStream bis = new ByteArrayInputStream(strs[z].getBytes());
			dsi.putDocument(bis, uris[z]);
			sleep();
		}
		assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc5.json").exists());
        
        dsi.deleteDocument(uris[1]);//deletes doc 2
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertNull(dsi.getDocument(uris[1]));
        
        
        dsi.undo(uris[4]);//undoes doc 5, which would reput doc 2
        assertNull(dsi.getDocument(uris[4]));
        assertNull(dsi.getDocument(uris[1]));
        cleanUpFolder();
	} 
	
	@Test public void  buriedUndo() throws URISyntaxException {
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 8 buriedUndo");}
		Random rand = new Random();
		int x  = rand.nextInt();
		File dir = new File(System.getProperty("user.dir") + "/testing/" + x + "/");
		testingSubDirectory = dir;		
		DocumentStoreImpl dsi = new DocumentStoreImpl(dir);
		dsi.setMaxDocumentCount(3);
		String[] strs = createStrings(9);
		URI[] uris = createURIs(9);
		for (int z = 0; z < 9; z++) {
			ByteArrayInputStream bis = new ByteArrayInputStream(strs[z].getBytes());
			dsi.putDocument(bis, uris[z]);
			sleep();
		}
		
		assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc5.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc6.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc7.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc8.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc9.json").exists());
        
        dsi.undo(uris[4]);
        sleep();
        
        
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc5.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc6.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc7.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc8.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc9.json").exists());
        cleanUpFolder();
	}
	
	@Test public void  buriedUndoV2() throws URISyntaxException {
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 9 buriedUndoV2");}
		DocumentStoreImpl dsi = new DocumentStoreImpl();
		int x = setBaseDir(dsi);
		dsi.setMaxDocumentCount(3);
		String[] strs = createStrings(9);
		URI[] uris = createURIs(9);
		for (int z = 0; z < 9; z++) {
			ByteArrayInputStream bis = new ByteArrayInputStream(strs[z].getBytes());
			dsi.putDocument(bis, uris[z]);
			sleep();
		}
		
		assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc5.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc6.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc7.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc8.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc9.json").exists());
        
        dsi.undo(uris[4]);
        sleep();
        
        
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc5.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc6.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc7.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc8.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc9.json").exists());
        cleanUpFolder();
	}

	private String[] createStrings(int x) {
		String[] strs = new String[x]; 
		for (int y = 1; y <= x; y++) {
			strs[y - 1] = generateString(y);
		}
		return strs;
	}
	
	private String generateString(int x) {
		String str = "";
		for (int y = 0; y < x; y++) {
			str = str + "this is doc " + x + " ";
		}
		return str;
	}
	
	private URI[] createURIs(int x) throws URISyntaxException {
		URI[] uris = new URI[x];
		for (int y = 1; y <= x; y++) {
			URI uri = new URI("http://www.yu.edu/doc" + y);
			uris[y - 1] = uri;
		}
		return uris;
	}
	private void cleanUpFolder() {
		try {
			FileUtils.deleteDirectory(testingSubDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}//this will delete all the directory the test creates after the test is completed
	}
	
	private int setBaseDir(DocumentStoreImpl dsi) {
		Random rand = new Random();
		int x  = rand.nextInt();
		File dir = new File(System.getProperty("user.dir") + "/testing/" + x + "/");
		testingSubDirectory = dir;
		dsi.setStorageBaseDirectory(dir);
		return x;
	}
	
	private void sleep() {
    	try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
