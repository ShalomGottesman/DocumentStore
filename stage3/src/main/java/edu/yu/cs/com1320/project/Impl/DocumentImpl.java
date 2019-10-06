package edu.yu.cs.com1320.project.Impl;

import java.net.URI;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;

public class DocumentImpl implements Document {
	private byte[] compressedDocument;
	private URI documentKey;
	private DocumentStore.CompressionFormat compressionFormat;
	private int origionalStringHashcode;
	private HashTableImpl<String, Integer> wordsInDoc;
	
	DocumentImpl(byte[] compressedDoc, URI docKey, int uncompressedStringHashcode, CompressionFormat compressionFormat, String origionalDoc) {
		if (docKey == null) {
			throw new IllegalArgumentException();
		}
		this.compressedDocument = compressedDoc;
		this.documentKey = docKey;
		this.compressionFormat = compressionFormat;
		this.origionalStringHashcode = uncompressedStringHashcode;
		this.wordsInDoc = hashAllWords(origionalDoc);
	}

	@Override
	public byte[] getDocument() {
		return compressedDocument;
	}

	@Override
	public int getDocumentHashCode() {
		return this.origionalStringHashcode;
	}

	@Override
	public URI getKey() {
		return documentKey;
	}

	@Override
	public CompressionFormat getCompressionFormat() {
		 return compressionFormat;
	}
	

	@Override
	public int hashCode() { // hashcode of a document object is defined by all it's fields
		int result = compressedDocument !=null ? compressedDocument.hashCode() : 0;
		result += documentKey != null ? documentKey.hashCode() : 0;
		result += compressionFormat != null ? compressionFormat.hashCode() : 0;
		result += origionalStringHashcode;
		return result;
	}
	
	@Override
	public boolean equals(Object that) {
		if ((this.getClass() == that.getClass()) && 
			(this.getKey() == ((DocumentImpl) that).getKey()) && 
			(this.origionalStringHashcode == ((DocumentImpl) that).origionalStringHashcode)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int wordCount(String word) {
		return wordsInDoc.get(word.toLowerCase());//Because of case insensitivity, search number of instances by lower case as that is also how they are stored
	}
	
	private HashTableImpl<String, Integer> hashAllWords(String origionalDoc){
		String[] allWords = origionalDoc.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+"); //this will remove non letters, then split along spaces.
		//String[] allWords = origionalDoc.split("[\\p{Punct}\\p{Space}[^\\w]]+");
				//https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#sum
		HashTableImpl<String, Integer> wordTable = new HashTableImpl<String, Integer>();//create new table to map strings to the number of times they appear
		for (int x = 0; x < allWords.length; x++) {//for each separated word:
			String tempStr = allWords[x].toLowerCase();//create a new string of that word in lower case form
			int temp = 0;
			if (wordTable.get(tempStr) != null){//find the number of times the word appears in the document
				temp = wordTable.get(tempStr);
			}
			wordTable.put(tempStr, temp+1);//then add said word and increase current count by 1
		}
		return wordTable;//return the new instance of HashTableImpl for storage as class field.
	}
	
	
}
