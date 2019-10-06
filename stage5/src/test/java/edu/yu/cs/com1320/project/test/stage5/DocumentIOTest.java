package edu.yu.cs.com1320.project.test.stage5;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;
import edu.yu.cs.com1320.project.Impl.DocumentIOImpl;
import edu.yu.cs.com1320.project.Impl.DocumentImpl;
import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl;

public class DocumentIOTest {
	final static boolean Trace = false;
	private File testingSubDirectory;
	
	@Test public void initialize() {
		if (Trace) {System.out.println("\nDocumentIO stage 5 Test: Test 1 initialize");}
		DocumentIOImpl docIO = new DocumentIOImpl();
	}
	@Test public void setDir() {
		if (Trace) {System.out.println("\nDocumentIO stage 5 Test: Test 2 setDir");}
		DocumentIOImpl docIO = new DocumentIOImpl();
		File newDir = new File(System.getProperty("user.dir") + File.separatorChar + "jsonFile\\helloThere");
		docIO.setBaseDirectory(newDir);
	}
	@Test public void serialize() throws URISyntaxException, UnsupportedEncodingException {
		if (Trace) {System.out.println("\nDocumentIO stage 5 Test: Test 3 serialize");}
		DocumentIOImpl docIO = new DocumentIOImpl();
		File newDir = new File(System.getProperty("user.dir") + File.separatorChar + "jsonFiles");
		docIO.setBaseDirectory(newDir);
		String path = "https://www.yu.edu.shalom/this/Is/A/Test.docp";
		URI uri = new URI(path);
		Document doc1 = new DocumentImpl(new byte[10], uri, DocumentStore.CompressionFormat.BZIP2, "hi there");
		docIO.serialize(doc1);
	}
	
	@Test public void deserialize() throws URISyntaxException, UnsupportedEncodingException {
		if (Trace) {System.out.println("\nDocumentIO stage 5 Test: Test 4 deserialize");}
		DocumentIOImpl docIO = new DocumentIOImpl();
		File newDir = new File(System.getProperty("user.dir") + File.separatorChar + "jsonFiles");
		docIO.setBaseDirectory(newDir);
		String path = "https://www.yu..edu.//this/Is/A/Test7.docp";
		URI uri = new URI(path);
		byte[] bytes = new byte[]{01, 02, 03, 04, 05};
		Document doc1 = new DocumentImpl(bytes, uri, DocumentStore.CompressionFormat.BZIP2, "hi there");
		docIO.serialize(doc1);
		Document doc2 = docIO.deserialize(uri);
		for (int x = 0; x < 5; x++) {
			assertTrue(bytes[x] == doc2.getDocument()[x]);
		}
	}
	
	@Test public void addMany() throws URISyntaxException, UnsupportedEncodingException {
		if (Trace) {System.out.println("\nDocumentIO stage 5 Test: Test 5 addMany");}
		DocumentIOImpl docIO = new DocumentIOImpl();
		File newDir = new File(System.getProperty("user.dir") + File.separatorChar + "jsonFiles");
		docIO.setBaseDirectory(newDir);
		String path = "https://www.yu..edu.//this/is/going/to/be/fun";
		String extention = "coa";
		for (int x = 0; x < 5; x++) {
			URI uri = new URI(path + x +extention);
			byte[] bytes = new byte[] {(byte) x, (byte) (2* x), (byte) (3* x), (byte) (4* x), (byte) (5* x)};
			Document doc1 = new DocumentImpl(bytes, uri, DocumentStore.CompressionFormat.BZIP2, "hello there my name is shalom" + x+10);
			docIO.serialize(doc1);
		}
	}
	@Test public void getMany() throws URISyntaxException, UnsupportedEncodingException {
		if (Trace) {System.out.println("\nDocumentIO stage 5 Test: Test 6 getMany");}
		DocumentIOImpl docIO = new DocumentIOImpl();
		File newDir = new File(System.getProperty("user.dir") + File.separatorChar + "jsonFiles");
		docIO.setBaseDirectory(newDir);
		String path = "https://www.yu..edu.//this/is/going/to/be/fun";
		String extention = "coa";
		URI[] uris = new URI[5];
		for (int x = 0; x < 5; x++) {
			URI uri = new URI(path + x + "." + extention);
			uris[x] = uri;
			byte[] bytes = new byte[] {(byte) x, (byte) (2* x), (byte) (3* x), (byte) (4* x), (byte) (5* x)};
			Document doc1 = new DocumentImpl(bytes, uri, DocumentStore.CompressionFormat.BZIP2, "hello there my name is shalom" + x+10);
			docIO.serialize(doc1);
		}
		//assert true that all files exist,
		for (int x = 0; x < 5; x++) {
			File newFile = new File(newDir + "/www.yu..edu./this/is/going/to/be/fun" + x + ".coa.json");
			assertTrue(newFile.exists());
		}
		//test that all files are what they should be
		for (int x = 0; x < 5; x++) {
			Document doc2 = docIO.deserialize(uris[x]);
			byte[] bytes = new byte[] {(byte) x, (byte) (2* x), (byte) (3* x), (byte) (4* x), (byte) (5* x)};
			//assertTrue(bytes == (doc2.getDocument()));
			Arrays.equals(bytes, doc2.getDocument());
		}
	}
	@Test public void getManyV2() throws URISyntaxException, UnsupportedEncodingException {
		if (Trace) {System.out.println("\nDocumentIO stage 5 Test: Test 7 getManyV2");}
		File newDir = new File(System.getProperty("user.dir") + File.separatorChar + "jsonFiles8");
		DocumentIOImpl docIO = new DocumentIOImpl(newDir);
		String path = "https://www.yu..edu.//this/is/going/to/be/fun";
		String extention = "coa";
		URI[] uris = new URI[5];
		for (int x = 0; x < 5; x++) {
			URI uri = new URI(path + x + "." + extention);
			uris[x] = uri;
			byte[] bytes = new byte[] {(byte) x, (byte) (2* x), (byte) (3* x), (byte) (4* x), (byte) (5* x)};
			Document doc1 = new DocumentImpl(bytes, uri, DocumentStore.CompressionFormat.BZIP2, "hello there my name is shalom" + x+10);
			docIO.serialize(doc1);
		}
		//assert true that all files exist,
		for (int x = 0; x < 5; x++) {
			File newFile = new File(newDir + "/www.yu..edu./this/is/going/to/be/fun" + x + ".coa.json");
			assertTrue(newFile.exists());
		}
		//test that all files are what they should be
		for (int x = 0; x < 5; x++) {
			Document doc2 = docIO.deserialize(uris[x]);
			byte[] bytes = new byte[] {(byte) x, (byte) (2* x), (byte) (3* x), (byte) (4* x), (byte) (5* x)};
			//assertTrue(bytes == (doc2.getDocument()));
			Arrays.equals(bytes, doc2.getDocument());
		}
	}
}
