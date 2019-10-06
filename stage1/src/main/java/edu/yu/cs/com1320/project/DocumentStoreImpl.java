package edu.yu.cs.com1320.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class DocumentStoreImpl implements DocumentStore {
	private HashTable<URI,Document> hashTable;
	private CompressionFormat compressionFormat;
	
	public DocumentStoreImpl() {
		this.hashTable = new HashTableImpl<URI,Document>();
		this.compressionFormat = getDefaultCompressionFormat(); //for some reason the default setting is not working, so on instantiation it calls the default setter which returns ZIP if the default is null
	}
	
	@Override
	public void setDefaultCompressionFormat(CompressionFormat format) {
		this.compressionFormat = format;
		return;
	}
	
	public CompressionFormat getDefaultCompressionFormat(){
        if (this.compressionFormat == null) {
        	return CompressionFormat.ZIP; //note this also means the user is not able to set the defualt to null and cause the program to break down through the putDocument(InputStream, URI) API
        } else {
		return this.compressionFormat;
        }
    }

	@Override
	public int putDocument(InputStream input, URI uri) {
		return putDocument(input, uri, this.compressionFormat);
	}
	
	@Override
	public int putDocument(InputStream input, URI uri, CompressionFormat format) {
		if (uri == null) { //can't have a null key
			throw new IllegalArgumentException();
		}
		if (format == null) {
			format = this.getDefaultCompressionFormat(); //and you thought you could catch me by passing null into the putDocument(InputStream, URI, CompressionFormat) API? sorry bud!
		}
		String docString = byteArrayToString(input); //first build a string of the input stream
		InputStream input2 = new ByteArrayInputStream( docString.getBytes()); //and then remake an input stream of that string to be compressed
		int origDocHashCode = docString.hashCode();//store the hashcode for the document object
		Document temp = hashTable.get(uri); //first check to see if this key is already on the table
		if (temp != null) { //if it is not null, then the key is already in the table
			if(origDocHashCode == temp.getDocumentHashCode()) {
				return temp.getDocumentHashCode();
			}
		}//this key an value pair is not yet on the table, now add this pair
		byte[] compressedByteArray = compressionRedirection(input2, format);//get compressed byte array based on the compression format
		
		// remove grey out to print out the compressed and uncompressed bytes for comparison
		System.out.println("");
		System.out.println("format: " + format.toString());
		System.out.println("String form : " + docString);
		System.out.println("uncompressed: " + Arrays.toString(docString.getBytes()));
		System.out.println("compressed  : " + Arrays.toString(compressedByteArray));
		
		DocumentImpl newDoc = new DocumentImpl(compressedByteArray, uri, origDocHashCode, format);//create new doc
		this.hashTable.put(uri, newDoc);//add the doc with the URI
		return newDoc.getDocumentHashCode();//return the hashcode of the original string 
		}
	
	private byte[] compressionRedirection (InputStream in, CompressionFormat format) {//redirects based on format for compression
		byte[] compressedInput = null;
		try {
			if (format == DocumentStore.CompressionFormat.ZIP) {
				compressedInput = documentZIPJARCompression("zip", in);
			}
			if (format == DocumentStore.CompressionFormat.JAR) {
				compressedInput = documentZIPJARCompression("jar", in);
			}
			if (format == DocumentStore.CompressionFormat.SEVENZ) {
				compressedInput = documentSEVENZCompression(in);
			}
			if (format == DocumentStore.CompressionFormat.GZIP) {
				compressedInput = documentGBZIP2Compression("gz", in);
			}
			if (format == DocumentStore.CompressionFormat.BZIP2) {
				compressedInput = documentGBZIP2Compression("bzip2", in);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CompressorException e) {
			e.printStackTrace();
		} catch (ArchiveException e) {
			e.printStackTrace();
		}
		return compressedInput;//return the compressed string
	}
	
	private byte[] documentZIPJARCompression(String compressionFormat, InputStream in) throws IOException, ArchiveException {//this compresses both ZIP and JAR
		OutputStream compressedOut = new ByteArrayOutputStream(); //output stream to hold the compressed bytes
		ArchiveOutputStream archOutStrm = new ArchiveStreamFactory().createArchiveOutputStream(compressionFormat, compressedOut); //compressed bytes that are fed in via an algorithm determined by the compression format input
		ArchiveEntry archiveEntry = null; //create an entry for the factory to write to, this is the "session" of this compression
		if (compressionFormat.equals("zip")) { // Determine which type of entry this is (not sure this is necessary)
			archiveEntry = new ZipArchiveEntry("");
		}
		if (compressionFormat.equals("jar")) {
			archiveEntry = new JarArchiveEntry("");
		}
		archOutStrm.putArchiveEntry(archiveEntry); //inform factory that we are going to use this session to compress information into the outputstream
		IOUtils.copy(in, archOutStrm);//copy from the input into the factory which dumps compressed bytes into the outputstream
		archOutStrm.closeArchiveEntry();//close archive entry
		archOutStrm.close();//close factory. these both prevent read errors on decompression and free system resources
		return ((ByteArrayOutputStream) compressedOut).toByteArray();	
	}
	
	private byte[] documentGBZIP2Compression(String compressionFormat, InputStream in) throws IOException, CompressorException {// read documentZIPJARCompression documentation	
		OutputStream compressedDocOut = new ByteArrayOutputStream();
		CompressorStreamFactory factory = new CompressorStreamFactory();
		CompressorOutputStream compressOut = factory.createCompressorOutputStream(compressionFormat, compressedDocOut);
		IOUtils.copy(in, compressOut);
		compressOut.close();
		return ((ByteArrayOutputStream) compressedDocOut).toByteArray();
	}	
	
	private byte[] documentSEVENZCompression(InputStream in) throws IOException { //same process as before, only now have to create a file to write to
		File tempFileTarget = File.createTempFile("CompSciProject-tempFileTargetIn", "file");//create file in temprorary folder
		tempFileTarget.deleteOnExit();//this file will now be deleted on completion of the program
		SevenZOutputFile fileOut = new SevenZOutputFile(tempFileTarget);//calls file to be written to
		ArchiveEntry archEnrty = fileOut.createArchiveEntry(tempFileTarget, "entry1");//same as before
		fileOut.putArchiveEntry(archEnrty);//set entry to be written to
		fileOut.write(IOUtils.toByteArray(in));//write it out
		fileOut.closeArchiveEntry();//close for decompression readability
		fileOut.close();
		
		InputStream archiveFileIn  = new FileInputStream(tempFileTarget);
		return IOUtils.toByteArray(archiveFileIn);
	}

	private String decompressionRedirection(byte[] compressedDoc, CompressionFormat format) { //decompression redirect depending on format
		String decompressedDoc = null;
		try {
			if (format == DocumentStore.CompressionFormat.ZIP) {
				decompressedDoc = documentZipJarDecompression("zip", compressedDoc);
			}
			if (format == DocumentStore.CompressionFormat.JAR) {
				decompressedDoc = documentZipJarDecompression("jar", compressedDoc);
			}
			if (format == DocumentStore.CompressionFormat.SEVENZ) {
				decompressedDoc = documentSEVENZDecompression(compressedDoc);
			}
			if (format == DocumentStore.CompressionFormat.GZIP) {
				decompressedDoc = documentGBZIP2Decompression("gz", compressedDoc);
			}
			if (format == DocumentStore.CompressionFormat.BZIP2) {
				decompressedDoc = documentGBZIP2Decompression("bzip2", compressedDoc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CompressorException e) {
			e.printStackTrace();
		} catch (ArchiveException e) {
			e.printStackTrace();
		}
		return decompressedDoc;
	}
	
	private String documentZipJarDecompression(String compressionFormat, byte[] compressedDoc) throws ArchiveException, IOException {//same logic as compression but opposite direction
		InputStream compressedIn = new ByteArrayInputStream(compressedDoc);
		ArchiveInputStream archInpStrm = new ArchiveStreamFactory().createArchiveInputStream(compressionFormat, compressedIn);
		ZipArchiveEntry entry = (ZipArchiveEntry)archInpStrm.getNextEntry();
		OutputStream decompressOut = new ByteArrayOutputStream();
		IOUtils.copy(archInpStrm, decompressOut);
		archInpStrm.close();
		byte[] deceompressedDocBytes = ((ByteArrayOutputStream) decompressOut).toByteArray();
		String uncompressedDoc = new String(deceompressedDocBytes);
		return uncompressedDoc;
	}
	
	private String documentGBZIP2Decompression(String compressionForm, byte[] compressedDoc) throws IOException, CompressorException {
		InputStream compressedDocIn = new ByteArrayInputStream(compressedDoc);
		OutputStream decompressedDocOut = new ByteArrayOutputStream();
		CompressorInputStream compInpStream = new CompressorStreamFactory().createCompressorInputStream(compressionForm, compressedDocIn);
		IOUtils.copy(compInpStream, decompressedDocOut);
		byte[] decompressedDocByteArray = ((ByteArrayOutputStream) decompressedDocOut).toByteArray();
		String str = new String(decompressedDocByteArray);
		compInpStream.close();
		decompressedDocOut.close();		
		return str;
	}

	private String documentSEVENZDecompression(byte[] compressedDoc) throws IOException {
		File tempFile = File.createTempFile("CompSciProject-tempFileTargetOut-", "file");
		tempFile.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(tempFile);
		fos.write(compressedDoc);
		SevenZFile fileIn = new SevenZFile(tempFile);
		SevenZArchiveEntry entry = fileIn.getNextEntry();
		byte[] decompressedDoc = new byte[(int) entry.getSize()];
		fileIn.read(decompressedDoc);
		InputStream decompressedByteArray = new ByteArrayInputStream(decompressedDoc);
		String str = byteArrayToString(decompressedByteArray);
		fileIn.close();
		fos.close();
		return str;
	}

	private String byteArrayToString(InputStream in) {
		try {
			return IOUtils.toString(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getDocument(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException();
		}
		Document docInstance = this.hashTable.get(uri);//get docInstance based off key
		if ((docInstance == null) || (null == docInstance.getDocument())) {//if either the document itself is null or the contents are null, then return null
			return null;
		}
		String decompressedDoc = decompressionRedirection(docInstance.getDocument(), docInstance.getCompressionFormat()); //else, get the decompressed version of the string and return it
		return decompressedDoc;
	}

	@Override
	public byte[] getCompressedDocument(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException();
		}
		Document docInstance = this.hashTable.get(uri);//get docInstance based off key
		if ((docInstance == null) || (null == docInstance.getDocument())) {//if either the document itself is null or the contents are null, then return null
			return null;
		} else {
		return docInstance.getDocument(); //returns the compressed byte array of the document without converting it back to a string
		}
	}

	@Override
	public boolean deleteDocument(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException();
		}
		if (this.hashTable.get(uri) == null) { //if the search returned null, the key is not present. nothing to do, return false
			return false;
		} else {
			this.hashTable.put(uri, null);//doc was found, replace value of this key with null
			return true;
		}
	}
	
	// DO NOT IMPLEMENT IN STAGE 1 OF THE PROJECT. THIS IS FOR STAGE 2
	@Override
	public boolean undo() throws IllegalStateException {
		// TODO Auto-generated method stub
		return false;
	}
	// DO NOT IMPLEMENT IN STAGE 1 OF THE PROJECT. THIS IS FOR STAGE 2
	@Override
	public boolean undo(URI uri) throws IllegalStateException {
		// TODO Auto-generated method stub
		return false;
	}
}
