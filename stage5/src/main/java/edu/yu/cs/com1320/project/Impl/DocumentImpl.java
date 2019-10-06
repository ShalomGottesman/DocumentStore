package edu.yu.cs.com1320.project.Impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;

public class DocumentImpl implements Document {
	private byte[] compressedDocument;
	private URI documentKey;
	private DocumentStore.CompressionFormat compressionFormat;
	private int origionalStringHashcode;
	private Map<String, Integer> wordsInDoc;
	private long lastEdited;
	
	public DocumentImpl(byte[] compressedDoc, URI docKey, CompressionFormat compressionFormat, String origionalDoc) {
		if (docKey == null) {
			throw new IllegalArgumentException();
		}
		this.compressedDocument = compressedDoc;
		this.documentKey = docKey;
		this.compressionFormat = compressionFormat;
		this.origionalStringHashcode = origionalDoc.hashCode();
		this.wordsInDoc = hashAllWords(origionalDoc);
		this.lastEdited = System.currentTimeMillis(); 
	}
	

	
	@Override
	public Map<String, Integer> getWordMap() { 
		return this.wordsInDoc;
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
			(this.getKey() == ((DocumentImpl) that).getKey())  /*&& 
			(this.origionalStringHashcode == ((DocumentImpl) that).origionalStringHashcode)*/)  {
			return true;
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		return "[" + this.documentKey.toString() + ":" + lastEdited +"]";
	}

	@Override
	public int wordCount(String word) {
		return wordsInDoc.get(word.toLowerCase());//Because of case insensitivity, search number of instances by lower case as that is also how they are stored
	}
	
	private Map<String, Integer> hashAllWords(String origionalDoc){
		String[] allWords = origionalDoc.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+"); //this will remove non letters, then split along spaces.
		//String[] allWords = origionalDoc.split("[\\p{Punct}\\p{Space}[^\\w]]+");
				//https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#sum
		Map<String, Integer> wordTable = new HashMap<String, Integer>();//create new table to map strings to the number of times they appear
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

	@Override
	public long getLastUseTime() {
		return lastEdited;
	}

	@Override
	public void setLastUseTime(long timeInMilliseconds) {
		lastEdited = timeInMilliseconds;
	}

	@Override
	public void setWordMap(Map<String, Integer> wordMap) {
		this.wordsInDoc = wordMap;
	}
	
	
	@Override
	public int compareTo(Document o) {
		long x =(this.getLastUseTime() - o.getLastUseTime());
		int y = 0;
		if (x < 0) {
			y = qualifyNegetive(x);
		}
		if (x > 0) {
			y = qualifyPositive(x);
		}
		return y;
	}
	
	private int qualifyPositive(long posNumberToQualify) {
		while (posNumberToQualify > 2147483647) {
			posNumberToQualify = posNumberToQualify - 2147483647;
		}
		return (int) (posNumberToQualify);
	}
	
	private int qualifyNegetive(long negNumberToQualify) {
		while (negNumberToQualify < -2147483648) {
			negNumberToQualify = negNumberToQualify + 2147483647;
		}
		return (int) (negNumberToQualify);
	}
}
