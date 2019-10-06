package edu.yu.cs.com1320.project.Impl;

import java.net.URI;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;

public class DocumentImpl implements Document {
	@SuppressWarnings("unused")
	private byte[] compressedDocument;
	private URI documentKey;
	private DocumentStore.CompressionFormat compressionFormat;
	private int origionalStringHashcode;
	
	DocumentImpl(byte[] compressedDoc, URI docKey, int uncompressedStringHashcode, CompressionFormat compressionFormat) {
		if (docKey == null) {
			throw new IllegalArgumentException();
		}
		this.compressedDocument = compressedDoc;
		this.documentKey = docKey;
		this.compressionFormat = compressionFormat;
		this.origionalStringHashcode = uncompressedStringHashcode;
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
}
