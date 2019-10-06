package edu.yu.cs.com1320.project.Impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;

public class DocumentStoreImpl implements DocumentStore {
	private CompressionFormat compressionFormat;
	private StackImpl<Command> commandStack;
	private TrieImpl<URI> documentTrie;
	private MinHeapImpl<UriTimePair> docHeap;
	private BTreeImpl docTree;
	private docComarator docComparator;
	private CompressionBox compressionBox;
	private int compressedSpaceInMemory;
	private int docCountInMemory;
	private int spaceLimit;
	private int docLimit;
	private DocumentIOImpl docIO;
	private Hashtable<URI, Boolean> memoryTable; //this table will track URIs in the system: True- in memory, False- on disk, Null- not in system(including deleted)
	private Hashtable<URI, UriTimePair> uriToPairMap = new Hashtable<URI, UriTimePair>(); 

	public DocumentStoreImpl() {
		this.compressionFormat = getDefaultCompressionFormat(); //for some reason the default setting is not working, so on instantiation it calls the default setter which returns ZIP if the default is null
		this.commandStack = new StackImpl<Command>();
		this.docComparator = new docComarator();
		this.documentTrie = new TrieImpl<URI>(new uriComparator());	
		this.docHeap = new MinHeapImpl<UriTimePair>();
		this.compressionBox = new CompressionBox();
		this.compressedSpaceInMemory = 0;
		this.docCountInMemory = 0;
		this.spaceLimit = Integer.MAX_VALUE;
		this.docLimit = Integer.MAX_VALUE;
		this.docIO = new DocumentIOImpl();
		this.docTree = new BTreeImpl(docIO);
		this.memoryTable = new Hashtable<URI, Boolean>();
	}
	public DocumentStoreImpl(File dir) {
		this.compressionFormat = getDefaultCompressionFormat(); //for some reason the default setting is not working, so on instantiation it calls the default setter which returns ZIP if the default is null
		this.commandStack = new StackImpl<Command>();
		this.docComparator = new docComarator();
		this.documentTrie = new TrieImpl<URI>(new uriComparator());	
		this.docHeap = new MinHeapImpl<UriTimePair>();
		this.compressionBox = new CompressionBox();
		this.compressedSpaceInMemory = 0;
		this.docCountInMemory = 0;
		this.spaceLimit = Integer.MAX_VALUE;
		this.docLimit = Integer.MAX_VALUE;
		this.docIO = new DocumentIOImpl();
		this.docTree = new BTreeImpl(docIO);
		this.memoryTable = new Hashtable<URI, Boolean>();
		setStorageBaseDirectory(dir);
	}
	
	@Override
	public int putDocument(InputStream input, URI uri, CompressionFormat format) {
		if (uri == null)  //can't have a null key
			throw new IllegalArgumentException();
		if (format == null)
			format = this.getDefaultCompressionFormat(); //and you thought you could catch me by passing null into the putDocument(InputStream, URI, CompressionFormat) API? sorry bud!
		if (input == null) //this is case 1, delete doc associated with this URI
			return putProcessForNullInput(uri);
		if (docExistsInSystem(uri)) //this is case 2, overwrite old doc
			return putProcessForOverWriteInput(uri, input, format);
		else //this is case 3, new document insertion
			return putProcessForNewInput(uri, input, format);
	}
	private int putProcessForNullInput(URI uri) {
		if (docExistsInSystem(uri)) {//and i am actually deleting something
			if (docExistsInMemory(uri)) { //then it has to be removed from memory and all structures
				//get a reference to that doc, and its current TimeStamp
				//remove it from tree, heap, trie
				//DONT remove it from stack
				//change boolean from true to null in URI table
				//add new command for this to reverse these actions
				Document oldDoc = getAndDeleteFromTree(uri);
				long docslastUse = oldDoc.getLastUseTime();
				UriTimePair pair = uriToPairMap.get(oldDoc.getKey());
				removeItemFromHeap(pair);
				deleteFromTrieAndTable(oldDoc);
				createCommandForNullDSIPutDocumentThatDeletesFromMemory(oldDoc, docslastUse);
				return 0;
			} else { //all the same as above, but not from the heap
				Document oldDoc = getAndDeleteFromTree(uri);
				long docslastUse = oldDoc.getLastUseTime();
				deleteStringFromTrie(this.compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat()), oldDoc.getKey());
				deleteUriFromTable(uri);
				createCommandForNullDSIPutDocumentThatDeletesFromDisk(oldDoc, docslastUse);
			}
		} else {//but if i am not actually deleting something
			//considering nothing here actually happens, maybe leave it blank?
			createCommandThatVirtuallyNothing(uri);
		}
		return 0;
	}
	
	private void createCommandForNullDSIPutDocumentThatDeletesFromDisk(Document oldDoc, long docslastUse) {
		//the undo of a null input where something was deleted is to reinsert the document back to how it was
		//note that while this will add the documents information back into the data structures, we do not care how many items are pushed out of memory becuase of the undo
		String origDoc = compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat());
		CompressionFormat format = oldDoc.getCompressionFormat();
		URI uri = oldDoc.getKey();
		//note that the undo has to readd the document to disk, not memory
		//this also means that we do NOT have to manage the memory at the end of this, because memory should not be touched
		Function<URI, Boolean> undoFunction = (URI) ->{
			//create the document
			DocumentImpl redoneDoc = new DocumentImpl(compressionBox.compressionRedirection(new ByteArrayInputStream(origDoc.getBytes()), format), uri, format, origDoc);
			redoneDoc.setLastUseTime(docslastUse);
			//put it back in the tree AND CALL MOVE TO DISK, put back in trie
			//put URI back in the table with false value
			this.docTree.put(uri, redoneDoc);
			try {
				this.docTree.moveToDisk(uri);
			} catch (Exception e) {	e.printStackTrace();}
			addStringToTrie(origDoc, uri);
			moveUriToDisk(uri);
			//no need to manage memory as this document does not exist in memory anymore
			return true;
		};
		Function<URI, Boolean> redoFunction = (URI) ->{
			//delete uri from structures (all but the heap) and remove item from tree
			deleteFromTreeTrieTable(uri, origDoc);
			return true;
		};
		createAndPushCommand(uri, undoFunction, redoFunction);
	}
 	
	private void createCommandForNullDSIPutDocumentThatDeletesFromMemory(Document oldDoc,  long oldDocsTimeStampBeforeDelete) {
		//the undo of a null input where something was deleted is to reinsert the document back to how it was
		//note that while this will add the documents information back into the data structures, we do not care how many items are pushed out of memory becuase of the undo
		String origDoc = compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat());
		CompressionFormat format = oldDoc.getCompressionFormat();
		URI uri = oldDoc.getKey();
		Function<URI, Boolean> undoFunction = (URI) ->{
			//create the document
			DocumentImpl redoneDoc = new DocumentImpl(compressionBox.compressionRedirection(new ByteArrayInputStream(origDoc.getBytes()), format), uri, format, origDoc);
			//put it back in the tree, heap, trie
			//put URI back in the table with true value
			this.docTree.put(uri, redoneDoc);
			redoneDoc.setLastUseTime(oldDocsTimeStampBeforeDelete);
			UriTimePair pair = new UriTimePair(redoneDoc.getKey(), redoneDoc.getLastUseTime());
			uriToPairMap.put(redoneDoc.getKey(), pair);
			this.docHeap.insert(pair);
			addStringToTrie(origDoc, uri);
			addUriToMemory(uri);
			increaseCounters(redoneDoc);
			memoryManagment(); //calls to move documents to memory if we are over the limit
			return true;
		};
		Function<URI,Boolean> redoFunction = (URI) ->{
			//to redo a null put where something was deleted, just re-remove refrences from all the structures and do not create a command for it
			deleteFromTreeTrieTable(uri, origDoc);
			UriTimePair pair = uriToPairMap.get(uri);
			removeItemFromHeap(pair);
			return true;
		};
		createAndPushCommand(uri, undoFunction, redoFunction);
	}
	
	private void createCommandThatVirtuallyNothing(URI uri) {
		Function<URI, Boolean> undoFunction = (URI) ->{
			//nothing to undo
			return false;
		};
		Function<URI,Boolean> redoFunction = (URI) ->{
			//to redo this is to redelete something that is not there
			return true;
		};
		createAndPushCommand(uri, undoFunction, redoFunction);
	}

	private int putProcessForOverWriteInput(URI uri, InputStream newinput, CompressionFormat format) {
		//first determine what we are overwriting, disk or memory:
		//if overwriting a document that currently exists in memory with a new document->tasks: 1. remove current document from heap, trie, and tree and update counters, 2. create new document and add to: heap, trie and tree, and update counters
		//if this is going to be only a disk overwrite -> tasks: 1. remove current document from the tree and the trie, 2. create new document, add to tree (and move to disk) and add to trie, no need to adjust counters or call memory managment
		Document currentDoc = getAndDeleteFromTree(uri);
		if (docExistsInMemory(uri)) {
			UriTimePair pair = uriToPairMap.get(uri);
			removeItemFromHeap(pair);
			decreaseCounters(currentDoc);
		}
		String currentDocStr = compressionBox.decompressionRedirection(currentDoc.getDocument(), currentDoc.getCompressionFormat());
		deleteStringFromTrie(currentDocStr, uri);
		String newStr = byteArrayToString(newinput);
		byte[] compressed = compressionBox.compressionRedirection(new ByteArrayInputStream(newStr.getBytes()), format);
		DocumentImpl newDoc = new DocumentImpl(compressed, uri, format, newStr);
		if (docExistsInMemory(uri)) {
			UriTimePair pair = new UriTimePair(newDoc.getKey(), newDoc.getLastUseTime());
			uriToPairMap.put(newDoc.getKey(), pair);
			this.docHeap.insert(pair);
			increaseCounters(newDoc);
		}
		this.docTree.put(uri, newDoc);
		if (!docExistsInMemory(uri)) {
			try {
				this.docTree.moveToDisk(uri);
			} catch (Exception e) {	e.printStackTrace();}
		}
		addStringToTrie(newStr, uri);
		if (docExistsInMemory(uri)) 
			createCommandForInMemoryOverwrite(uri, currentDoc, newDoc, memoryManagment());
		else 
			createCommandForInDiskOverwrite(uri, currentDoc, newDoc);
		return currentDocStr.hashCode();
	}
		
	private void undoDiskOverwriteFromMem(URI uri, Document overwrittenDoc, Document writtenAsDoc) {
		byte[] oldDocsBytes = overwrittenDoc.getDocument();
		CompressionFormat oldFormat = overwrittenDoc.getCompressionFormat();
		Document currentDoc = this.docTree.get(uri);
		UriTimePair pair = uriToPairMap.get(uri);
		removeItemFromHeap(pair);
		String currentDocStr = compressionBox.decompressionRedirection(currentDoc.getDocument(), currentDoc.getCompressionFormat());
		deleteStringFromTrie(currentDocStr, uri);
		decreaseCounters(currentDoc);
		deleteUriFromTable(uri);
		DocumentImpl newNewDoc = new DocumentImpl(oldDocsBytes, uri, oldFormat, compressionBox.decompressionRedirection(oldDocsBytes, oldFormat));
		this.docTree.put(uri, newNewDoc);
		try {
			this.docTree.moveToDisk(uri);
		} catch (Exception e) { e.printStackTrace();}
		moveUriToDisk(uri);
		addStringToTrie(compressionBox.decompressionRedirection(oldDocsBytes, oldFormat), uri);
	}
	
	private Function<URI, Boolean> undoFuncForDiskOverwrite(URI uri, Document overwrittenDoc, Document writtenAsDoc) {
		byte[] oldDocsBytes = overwrittenDoc.getDocument();
		CompressionFormat oldFormat = overwrittenDoc.getCompressionFormat();
		Function<URI,Boolean> undoFunction = (URI) ->{
			if (docExistsInSystem(uri)) {
				if(docExistsInMemory(uri)) {//then remove from memory and replace document in tree, trie and URI table
					undoDiskOverwriteFromMem(uri,  overwrittenDoc, writtenAsDoc);
					} else { //then just take out of disk and replace it with new doc, dont touch the table, heap, or counters, but update trie
						Document currentDoc = this.docTree.get(uri);
						String currentDocStr = compressionBox.decompressionRedirection(currentDoc.getDocument(), currentDoc.getCompressionFormat());
						deleteStringFromTrie(currentDocStr, uri);
						DocumentImpl newNewDoc = new DocumentImpl(oldDocsBytes, uri, oldFormat, compressionBox.decompressionRedirection(oldDocsBytes, oldFormat));
						this.docTree.put(uri, newNewDoc);
						try {
							this.docTree.moveToDisk(uri);
						} catch (Exception e) { e.printStackTrace();}
						addStringToTrie(compressionBox.decompressionRedirection(oldDocsBytes, oldFormat), uri);
					}
					return true;
			} else {
				return false;
			}
		};
		return undoFunction;
	}
	
	private void redoOverwriteWhenDocIsInMemory(URI uri, Document overwrittenDoc, Document writtenAsDoc) {
		byte[] newDocsBytes = writtenAsDoc.getDocument();
		CompressionFormat newFormat = writtenAsDoc.getCompressionFormat();
		Document currentDoc = getAndDeleteFromTree(uri);
		UriTimePair pair = uriToPairMap.get(uri);
		removeItemFromHeap(pair);
		String oldDocStr = compressionBox.decompressionRedirection(currentDoc.getDocument(), currentDoc.getCompressionFormat());
		deleteStringFromTrie(oldDocStr, uri);
		decreaseCounters(currentDoc);
		deleteUriFromTable(uri);
		
		DocumentImpl newNewDoc = new DocumentImpl(newDocsBytes, uri, newFormat, compressionBox.decompressionRedirection(newDocsBytes, newFormat));
		moveUriToDisk(uri);
		addStringToTrie(compressionBox.decompressionRedirection(newDocsBytes, newFormat), uri);
		this.docTree.put(uri, newNewDoc);
		try {
			this.docTree.moveToDisk(uri);
		} catch (Exception e) {e.printStackTrace();	}
	}
	
	private void redoOverwriteWhenDocIsOnDisk(URI uri, Document overwrittenDoc, Document writtenAsDoc) {
		byte[] newDocsBytes = writtenAsDoc.getDocument();
		CompressionFormat newFormat = writtenAsDoc.getCompressionFormat();
		Document currentDoc = getAndDeleteFromTree(uri);
		String currentStr = compressionBox.decompressionRedirection(currentDoc.getDocument(), currentDoc.getCompressionFormat());
		deleteStringFromTrie(currentStr, uri);

		DocumentImpl newDoc = new DocumentImpl(newDocsBytes, uri, newFormat, compressionBox.decompressionRedirection(newDocsBytes, newFormat));
		this.docTree.put(uri, newDoc);
		try {
			this.docTree.moveToDisk(uri);
		} catch (Exception e) {e.printStackTrace();	}
		addStringToTrie(compressionBox.decompressionRedirection(newDocsBytes, newFormat), uri);
	}
	
	private Function<URI, Boolean> createRedoFunctionForOverwrite(URI uri, Document overwrittenDoc, Document writtenAsDoc){
		Function<URI, Boolean> redoFunction = (URI) ->{
			if (docExistsInSystem(uri)) {	
				if(docExistsInMemory(uri)) {//then remove from memory and overwrite old document on disk to new document value
					redoOverwriteWhenDocIsInMemory(uri, overwrittenDoc, writtenAsDoc);
				} else {//meaning it is still not in memory, so jsut copy the origional work flow and dont create a new command
					redoOverwriteWhenDocIsOnDisk(uri, overwrittenDoc, writtenAsDoc);
				}
				return true;
			} else {
				return false;
			}
		};
		return redoFunction;
	}
	
	private void createCommandForInDiskOverwrite(URI uri, Document overwrittenDoc, Document writtenAsDoc) {	
		Function<URI,Boolean> undoFunction = undoFuncForDiskOverwrite(uri, overwrittenDoc, writtenAsDoc); 
		Function<URI, Boolean> redoFunction = createRedoFunctionForOverwrite(uri, overwrittenDoc, writtenAsDoc);
		createAndPushCommand(uri, undoFunction, redoFunction);
	}
	private void createCommandForInMemoryOverwrite(URI uri, Document oldDoc, Document newDoc, URI[] urisPushedOutOfmemory) {
		//Tasks: undo- obtain new doc from tree, remove from all structures, recreate old doc and add to all locations, but replace time with old stamp
		//       redo- obtain old doc from tree, remove from all structures, recreate new doc and add to all locations, with new time stamp		
		Function<URI,Boolean> undoFunction = createUndoForMemoryOverwrite( uri,  oldDoc,  newDoc, urisPushedOutOfmemory);
		Function<URI,Boolean> redoFunction = createRedoForMemoryOverwrite( uri,  oldDoc,  newDoc, urisPushedOutOfmemory);
		createAndPushCommand(uri, undoFunction, redoFunction);	
	}
	
	private void undoMemOverwriteDocInMem(URI uri, Document oldDoc, Document newDoc, URI[] urisPushedOutOfmemory) {
		byte[] oldDocsBytes = oldDoc.getDocument();
		CompressionFormat oldDocFormat = oldDoc.getCompressionFormat();
		String oldDocString = compressionBox.decompressionRedirection(oldDocsBytes, oldDocFormat);
		long oldTimeStamp = oldDoc.getLastUseTime();
		Document currentDoc = getAndDeleteFromTree(uri);
		UriTimePair pair = uriToPairMap.get(uri);
		removeItemFromHeap(pair);
		String currentStr = compressionBox.decompressionRedirection(currentDoc.getDocument(), currentDoc.getCompressionFormat());
		deleteStringFromTrie(currentStr, uri);
		decreaseCounters(currentDoc);
		deleteUriFromTable(uri);
		pullUrisFromDisk(urisPushedOutOfmemory);
		
		DocumentImpl newOldDoc  = new DocumentImpl(oldDocsBytes, uri, oldDocFormat, oldDocString);
		newOldDoc.setLastUseTime(oldTimeStamp);
		UriTimePair pair2 = new UriTimePair(newOldDoc.getKey(), newOldDoc.getLastUseTime());
		uriToPairMap.put(newOldDoc.getKey(), pair2);
		this.docHeap.insert(pair2);
		addStringToTrie(oldDocString, uri);
		this.docTree.put(uri, newOldDoc);
		increaseCounters(newOldDoc);
		memoryManagment();
		addUriToMemory(uri);
	}
	private Function<URI, Boolean> createUndoForMemoryOverwrite(URI uri, Document oldDoc, Document newDoc, URI[] urisPushedOutOfmemory){
		byte[] oldDocsBytes = oldDoc.getDocument();
		CompressionFormat oldDocFormat = oldDoc.getCompressionFormat();
		String oldDocString = compressionBox.decompressionRedirection(oldDocsBytes, oldDocFormat);
		long oldTimeStamp = oldDoc.getLastUseTime();
		Function<URI,Boolean> undoFunction = (URI) ->{
			if (docExistsInSystem(uri)) {
				if(docExistsInMemory(uri))
					undoMemOverwriteDocInMem( uri,  oldDoc,  newDoc, urisPushedOutOfmemory);
				else {// rewriting to memory but the currnet doc is not in memory
					Document currentDoc = getAndDeleteFromTree(uri);
					String currentStr = compressionBox.decompressionRedirection(currentDoc.getDocument(), currentDoc.getCompressionFormat());
					deleteStringFromTrie(currentStr, uri);
					DocumentImpl newOldDoc  = new DocumentImpl(oldDocsBytes, uri, oldDocFormat, oldDocString);
					newOldDoc.setLastUseTime(oldTimeStamp);
					UriTimePair pair = new UriTimePair(newOldDoc.getKey(), newOldDoc.getLastUseTime());
					uriToPairMap.put(newOldDoc.getKey(), pair);
					this.docHeap.insert(pair);
					addStringToTrie(oldDocString, uri);
					this.docTree.put(uri, newOldDoc);
					increaseCounters(newOldDoc);
					pullUrisFromDisk(urisPushedOutOfmemory);
					memoryManagment();
					addUriToMemory(uri);
				}
				return false;
			} else 
				return false;
		};
		return undoFunction;
	}
	private Function<URI, Boolean> createRedoForMemoryOverwrite(URI uri, Document oldDoc, Document newDoc, URI[] urisPushedOutOfmemory){
		byte[] newDocsBytes = newDoc.getDocument();
		CompressionFormat newDocFormat = newDoc.getCompressionFormat();
		String newDocString = compressionBox.decompressionRedirection(newDocsBytes, newDocFormat);
		Function<URI,Boolean> redoFunction = (URI) ->{
			if (docExistsInMemory(uri)) {
				Document currentDoc = getAndDeleteFromTree(uri);
				UriTimePair pair = uriToPairMap.get(uri);
				removeItemFromHeap(pair);
				String currentDocStr = compressionBox.decompressionRedirection(currentDoc.getDocument(), currentDoc.getCompressionFormat());
				deleteFromTrieAndTable(currentDocStr, uri);
				decreaseCounters(currentDoc);
				
				DocumentImpl newNewDoc = new DocumentImpl(newDocsBytes, uri, newDocFormat, newDocString);
				UriTimePair pair2 = new UriTimePair(newNewDoc.getKey(), newNewDoc.getLastUseTime());
				uriToPairMap.put(pair2.getKey(), pair2);
				this.docHeap.insert(pair2);
				addStringToTrie(newDocString, uri);
				this.docTree.put(uri, newDoc);
				increaseCounters(newNewDoc);
				addUriToMemory(uri);
				pushUrisToDisk(urisPushedOutOfmemory);
				memoryManagment(); 
				return true;
			} else { //redoing a memory overwrite but when the current uri is only on disk
				redoMemOverwriteOnDisk ( uri,  oldDoc,  newDoc, urisPushedOutOfmemory);
				return true;
			}
		};
		return redoFunction;
	}
	
	private void redoMemOverwriteOnDisk(URI uri, Document oldDoc, Document newDoc, URI[] urisPushedOutOfmemory) {
		byte[] newDocsBytes = newDoc.getDocument();
		CompressionFormat newDocFormat = newDoc.getCompressionFormat();
		String newDocString = compressionBox.decompressionRedirection(newDocsBytes, newDocFormat);
		Document currentDoc = getAndDeleteFromTree(uri);
		String currentDocStr = compressionBox.decompressionRedirection(currentDoc.getDocument(), currentDoc.getCompressionFormat());
		deleteFromTrieAndTable(currentDocStr, uri);
		
		DocumentImpl newNewDoc = new DocumentImpl(newDocsBytes, uri, newDocFormat, newDocString);
		UriTimePair pair = new UriTimePair(newNewDoc.getKey(), newNewDoc.getLastUseTime());
		uriToPairMap.put(pair.getKey(), pair);
		this.docHeap.insert(pair);
		addStringToTrie(newDocString, uri);
		this.docTree.put(uri, newDoc);
		increaseCounters(newNewDoc);
		addUriToMemory(uri);
		pushUrisToDisk(urisPushedOutOfmemory);
		memoryManagment(); 
	}

	private void deleteFromTrieAndTable(String doc, URI uri) {
		deleteStringFromTrie(doc, uri);
		deleteUriFromTable(uri);
	}
	
	private void deleteFromTrieAndTable(Document doc) {
		deleteStringFromTrie(this.compressionBox.decompressionRedirection(doc.getDocument(), doc.getCompressionFormat()), doc.getKey());
		deleteUriFromTable(doc.getKey());
	}
	
	private void deleteFromTreeTrieTable(URI uri, String str) {
		this.docTree.delete(uri);
		deleteStringFromTrie(str, uri);
		deleteUriFromTable(uri);
	}
	
	private void pushUrisToDisk(URI[] urisToPush) {
		if (!(urisToPush == null)) {
			for (int x = 0; x < urisToPush.length; x++) {
				Document doc = this.docTree.get(urisToPush[x]);
				try {
					this.docTree.moveToDisk(urisToPush[x]);
				} catch (Exception e) {	e.printStackTrace(); }
				UriTimePair pair = uriToPairMap.get(doc.getKey());
				removeItemFromHeap(pair);
				decreaseCounters(doc);
				moveUriToDisk(urisToPush[x]);
			}
		}
	}
	
	private void pullUrisFromDisk(URI[] urisToPull) {
		if (!(urisToPull == null)) {
			for (int x = 0; x < urisToPull.length; x++) {
				Document doc = this.docTree.get(urisToPull[x]);
				if (docExistsInSystem(urisToPull[x]) && (!docExistsInMemory(urisToPull[x]))) {
					addUriToMemory(urisToPull[x]);
					UriTimePair pair = new UriTimePair(doc.getKey(), doc.getLastUseTime());
					uriToPairMap.put(pair.getKey(), pair);
					this.docHeap.insert(pair);
					increaseCounters(doc);
				}
			}
			memoryManagment();
		}
	}
		
	private int putProcessForNewInput(URI uri, InputStream input, CompressionFormat format) {
		String newOrigDoc = byteArrayToString(input);
		byte[] newDocByteArray = compressionBox.compressionRedirection(new ByteArrayInputStream(newOrigDoc.getBytes()), format);
		DocumentImpl newDoc = new DocumentImpl(newDocByteArray, uri, format, newOrigDoc);
		this.docTree.put(uri, newDoc);
		addStringToTrie(newOrigDoc, uri);
		UriTimePair pair = new UriTimePair(newDoc.getKey(), newDoc.getLastUseTime());
		uriToPairMap.put(pair.getKey(), pair);
		this.docHeap.insert(pair);
		addUriToMemory(uri);
		increaseCounters(newDoc);
		URI[] urisPushedOutOfMemory = memoryManagment();		
		createCommandForNewDSIPut(newDoc, urisPushedOutOfMemory);
		return newOrigDoc.hashCode();
	}
 	private void createCommandForNewDSIPut(DocumentImpl newDoc, URI[] urisPushedOutOfMemory) {
 		URI uri = newDoc.getKey();
 		long origDocTimeStamp = newDoc.getLastUseTime();
 		byte[] newDocsBytes = newDoc.getDocument();
 		CompressionFormat newFormat = newDoc.getCompressionFormat();
 		Function<URI,Boolean> undoFunction = (URI) ->{//the undo of a completely new put is to just remove said document from all structures
 			Document docToDelete = getAndDeleteFromTree(uri);
 			decreaseCounters(docToDelete);
 			deleteStringFromTrie(compressionBox.decompressionRedirection(newDocsBytes, newFormat), uri);
 			UriTimePair pair = uriToPairMap.get(uri);
 			removeItemFromHeap(pair);
 			pullUrisFromDisk(urisPushedOutOfMemory);
 			deleteUriFromTable(uri);
 			memoryManagment();
 			return true;
 		};
 		Function<URI,Boolean> redoFunction = (URI) ->{//the redo of a new put is just that, reput it
 			DocumentImpl newNewDoc = new DocumentImpl(newDocsBytes, uri, newFormat, compressionBox.decompressionRedirection(newDocsBytes, newFormat));
 			this.docTree.put(uri, newNewDoc);
 			addStringToTrie(compressionBox.decompressionRedirection(newDocsBytes, newFormat), uri);
 			UriTimePair pair = new UriTimePair(newNewDoc.getKey(), newNewDoc.getLastUseTime());
 			uriToPairMap.put(pair.getKey(), pair);
 			this.docHeap.insert(pair);
 			addUriToMemory(uri);
 			increaseCounters(newNewDoc);
 			memoryManagment();
 			newNewDoc.setLastUseTime(origDocTimeStamp);
 			updateItemInHeap(pair, origDocTimeStamp);
 			return true;
 		};
 		createAndPushCommand(newDoc.getKey(), undoFunction, redoFunction);
 	}
	
	@Override
	public String getDocument(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException();
		}
		if (!docExistsInSystem(uri)) {
			return null;
		} if (docExistsInMemory(uri)) {
			Document doc = this.docTree.get(uri);
			UriTimePair pair = uriToPairMap.get(uri);
			doc.setLastUseTime(System.currentTimeMillis());
			docTimeUpdateToCurrent(pair);
			return compressionBox.decompressionRedirection(doc.getDocument(), doc.getCompressionFormat());
		} else { //meaning the doc is on disk
			Document doc = this.docTree.get(uri);
			increaseCounters(doc);
 			UriTimePair pair = new UriTimePair(doc.getKey(), doc.getLastUseTime());
 			uriToPairMap.put(pair.getKey(), pair);
			this.docHeap.insert(pair);
			doc.setLastUseTime(System.currentTimeMillis());
			docTimeUpdateToCurrent(pair);
			memoryManagment();
			return compressionBox.decompressionRedirection(doc.getDocument(), doc.getCompressionFormat());
		}
	}

	@Override
	public byte[] getCompressedDocument(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException();
		}
		if (!docExistsInSystem(uri)) {
			return null;
		} if (docExistsInMemory(uri)) {
			Document doc = this.docTree.get(uri);
			UriTimePair pair = uriToPairMap.get(uri);
			doc.setLastUseTime(System.currentTimeMillis());
			docTimeUpdateToCurrent(pair);
			return doc.getDocument();
		} else { //meaning the doc is on disk
			Document doc = this.docTree.get(uri);
			increaseCounters(doc);
 			UriTimePair pair = new UriTimePair(doc.getKey(), doc.getLastUseTime());
 			uriToPairMap.put(pair.getKey(), pair);
			this.docHeap.insert(pair);
			doc.setLastUseTime(System.currentTimeMillis());
			docTimeUpdateToCurrent(pair);
			memoryManagment();
			return doc.getDocument();
		}
	}

	@Override
	public List<String> search(String keyword) {	
		keyword = keyword.toLowerCase();
		docComparator.setKeyString(keyword); 
		List<URI> allUris = documentTrie.getAllSorted(keyword);
		List<String> decompressedDocs = new ArrayList<String>();
		for (int x = 0; x < allUris.size(); x++) {
			decompressedDocs.add(this.getDocument(allUris.get(x)));
		}
		return decompressedDocs;
	}

	@Override
	public List<byte[]> searchCompressed(String keyword) {
		keyword = keyword.toLowerCase();
		docComparator.setKeyString(keyword);
		List<URI> allUris = documentTrie.getAllSorted(keyword);
		List<byte[]> compressedDocs = new ArrayList<byte[]>();
		for (int x = 0; x < allUris.size(); x++) {
			compressedDocs.add(this.getCompressedDocument(allUris.get(x)));
		}
		return compressedDocs;
	}
	
	private void createDeleteCommandThatDoesNothing(URI uri) {
		Function<URI, Boolean> undoFunction = (URI) ->{
			return false;
		};
		Function<URI, Boolean> redoFunction = (URI) ->{
			return false;
		};
		createAndPushCommand(uri, undoFunction, redoFunction);
	}	
	
	@Override
	public boolean deleteDocument(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException();
		}
		if (!docExistsInSystem(uri)) {
			createDeleteCommandThatDoesNothing(uri);
			return false;
		}
		if (docExistsInMemory(uri)) {//must remove it from memory, create command
			Document docToDelete = getAndDeleteFromTree(uri);
			UriTimePair pair = uriToPairMap.get(uri);
			removeItemFromHeap(pair);
			deleteStringFromTrie(compressionBox.decompressionRedirection(docToDelete.getDocument(), docToDelete.getCompressionFormat()), uri);
			deleteUriFromTable(uri);
			decreaseCounters(docToDelete);
			createDeleteFromMemoryCommand(uri, docToDelete);
			return true;
		} else {//remove a doc that is only on disk
			Document docToDelete = getAndDeleteFromTree(uri);
			deleteStringFromTrie(compressionBox.decompressionRedirection(docToDelete.getDocument(), docToDelete.getCompressionFormat()), uri);
			deleteUriFromTable(uri);
			createDeleteFromDiskCommand(uri, docToDelete);
			return true;
		}
	}
	
	private void createDeleteFromDiskCommand(URI uri, Document docThatWasDeleted) {
		byte[] deletedDocsBytes = docThatWasDeleted.getDocument();
		CompressionFormat deletedFormat = docThatWasDeleted.getCompressionFormat();
		long oldTimeStamp = docThatWasDeleted.getLastUseTime();
		
		Function<URI, Boolean> undoFunction = (URI) ->{//to undo the delete from disk is to read the deocument back into disk with old timestamp
			Document newDoc = new DocumentImpl(deletedDocsBytes, uri, deletedFormat, compressionBox.decompressionRedirection(deletedDocsBytes, deletedFormat));
			newDoc.setLastUseTime(oldTimeStamp);
			this.docTree.put(uri, newDoc);
			moveUriToDisk(uri);
			addStringToTrie(compressionBox.decompressionRedirection(deletedDocsBytes, deletedFormat), uri);
			return true;
		};
		Function<URI, Boolean> redoFunction = (URI) ->{//just copy the two scenarios from above without creating new commands
			if (docExistsInMemory(uri)) {//must remove it from memory
				Document docToDelete = getAndDeleteFromTree(uri);
				UriTimePair pair = uriToPairMap.get(uri);
				removeItemFromHeap(pair);
				deleteStringFromTrie(compressionBox.decompressionRedirection(docToDelete.getDocument(), docToDelete.getCompressionFormat()), uri);
				deleteUriFromTable(uri);
				decreaseCounters(docToDelete);
				return true;
			} else {
				Document docToDelete = getAndDeleteFromTree(uri);
				deleteStringFromTrie(compressionBox.decompressionRedirection(docToDelete.getDocument(), docToDelete.getCompressionFormat()), uri);
				deleteUriFromTable(uri);
				return true;
			}
		};
		createAndPushCommand(uri, undoFunction, redoFunction);
	}
	
	private Function<URI, Boolean> undoFuncDeleteFromMem(URI uri, Document docThatWasDeleted){
		byte[] deletedDocsBytes = docThatWasDeleted.getDocument();
		CompressionFormat deletedFormat = docThatWasDeleted.getCompressionFormat();
		long oldTimeStamp = docThatWasDeleted.getLastUseTime();
		Function<URI, Boolean> undoFunction = (URI) ->{//to undo the delete from memory is to read the deocument back into memory with old timestamp
			Document newDoc = new DocumentImpl(deletedDocsBytes, uri, deletedFormat, compressionBox.decompressionRedirection(deletedDocsBytes, deletedFormat));
			this.docTree.put(uri, newDoc);
 			UriTimePair pair = new UriTimePair(newDoc.getKey(), newDoc.getLastUseTime());
 			uriToPairMap.put(pair.getKey(), pair);
			this.docHeap.insert(pair);
			newDoc.setLastUseTime(oldTimeStamp);
			updateItemInHeap(pair, oldTimeStamp);
			addUriToMemory(uri);
			addStringToTrie(compressionBox.decompressionRedirection(deletedDocsBytes, deletedFormat), uri);
			increaseCounters(newDoc);
			memoryManagment();
			return true;
		};
		return undoFunction;
	}
	
	private void createDeleteFromMemoryCommand(URI uri, Document docThatWasDeleted) {		
		Function<URI, Boolean> undoFunction = undoFuncDeleteFromMem(uri, docThatWasDeleted); 
		Function<URI, Boolean> redoFunction = (URI) ->{//just copy the two scenarios from above without creating new commands
			if (docExistsInMemory(uri)) {//must remove it from memory
				Document docToDelete = getAndDeleteFromTree(uri);
				UriTimePair pair = uriToPairMap.get(uri);
				removeItemFromHeap(pair);
				deleteStringFromTrie(compressionBox.decompressionRedirection(docToDelete.getDocument(), docToDelete.getCompressionFormat()), uri);
				deleteUriFromTable(uri);
				decreaseCounters(docToDelete);
				return true;
			} else {
				Document docToDelete = getAndDeleteFromTree(uri);
				deleteStringFromTrie(compressionBox.decompressionRedirection(docToDelete.getDocument(), docToDelete.getCompressionFormat()), uri);
				deleteUriFromTable(uri);
				return true;
			}
		};
		createAndPushCommand(uri, undoFunction, redoFunction);
	}

	//start undo methods
	@Override
	public boolean undo() throws IllegalStateException {
		Command commandToUndo = this.commandStack.pop();//get top of stack
		if (commandToUndo == null) {//if there is nothing on the stack, meaning its empty
			throw new IllegalStateException();
		}
		boolean results = commandToUndo.undo();//else, call the undo function of that command, which returns a boolean
		memoryManagment();
		return results;//return said boolean
	}
	@Override
	public boolean undo(URI uri) throws IllegalStateException {
		int numberOfCommands = this.commandStack.size();//number of commands on the stack
		Command foundCommand = null;//instantiate command to avoid losing object reference when search concludes
		StackImpl<Command> tempStack = new StackImpl<Command>(); //create temporary stack to store other commands while searching for the matching command
		for (int x = 0; x < numberOfCommands; x++) { //for each element in the stack
			Command tempCommand = this.commandStack.pop();//look at top element
			if (tempCommand.getUri().equals(uri)) {//if it is a match
				foundCommand = tempCommand;//save said command for later
				break;//we have our command, stop popping things off old stack
			}
			tempCommand.undo();
			tempStack.push(tempCommand);//if that command was not right, push to the other stack and continue searching
		}
		if (foundCommand == null) {
			for (int x = 0; x < tempStack.size(); x++) {//for each element that was pushed into the temp stack
				Command tempCommand = tempStack.pop();
				tempCommand.redo();
				this.commandStack.push(tempCommand);//put it back in the old one
			}//now that everything is back in order, its the stack we are looking for returned null, meaning it wasnt present
			throw new IllegalStateException();//then the command is not present, oops.
		}//but if it wasn't null, then it was found, in such a case, call its undo method
		boolean results = foundCommand.undo();//take the return of its undo function and return it.
		int tempCommands = tempStack.size();
		for (int x = 0; x < tempCommands; x++) {//for each element that was pushed into the temp stack
			Command tempCommand = tempStack.pop();
			tempCommand.redo();
			this.commandStack.push(tempCommand);//put it back in the old one
		}
		memoryManagment();
		return results;
	}
	//end Undo methods
	
	private String byteArrayToString(InputStream in) {
		if (in == null) {
			return null;
		}
		try {
			return IOUtils.toString(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	//start Trie utility methods
	private void deleteStringFromTrie(String strToDelete, URI uriToRemove) {
		String[] allWords = strToDelete.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		for ( String tempString : allWords ) {
			this.documentTrie.delete(tempString, uriToRemove);
		}
	}
	private void addStringToTrie(String strToAdd, URI uriToAdd) {
		String[] allWords = strToAdd.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		for ( String tempString : allWords ) {
			this.documentTrie.put(tempString, uriToAdd);
		}
	}
	//end Trie utility methods
	//start limit changing methods
	@Override
	public void setMaxDocumentCount(int limit) {
		if (limit < 0) {
			throw new RuntimeException();
		}
		if (limit == 0) {
			this.docLimit = Integer.MAX_VALUE;
		} else {
			this.docLimit = limit;
			memoryManagment();
		}
	}
	@Override
	public void setMaxDocumentBytes(int limit) {
		if (limit < 0) {
			throw new RuntimeException();
		}
		if (limit == 0) {
			this.spaceLimit = Integer.MAX_VALUE;
		} else {
			this.spaceLimit = limit;
			memoryManagment();
		}
	}
	//end limit changing methods
	//start Heap utilities
	private void removeItemFromHeap(UriTimePair tempPair) {
		tempPair.setTimeStamp(1);
		try {
			this.docHeap.reHeapify(tempPair);
			this.docHeap.removeMin();
			uriToPairMap.remove(tempPair);
		} catch (NullPointerException e) {}
	}
	private void updateItemInHeap(UriTimePair pair, long updatedTimeStamp) {
		pair.setTimeStamp(updatedTimeStamp);
		try {
			this.docHeap.reHeapify(pair);
		} catch (NullPointerException e) {}
	}
	private void docTimeUpdateToCurrent(UriTimePair pair) {//this method updates the time stamp on a current document to the current time, and calls reheap on that document
		pair.setTimeStamp(System.currentTimeMillis());
		try {//this program to a degree runs in a loop when forcfully removing a doc. the loop will end here
			this.docHeap.reHeapify(pair);
		} catch (NullPointerException e) {}
	}
	//end Heap utilities
	public CompressionFormat getDefaultCompressionFormat(){
        if (this.compressionFormat == null) {
        	return CompressionFormat.ZIP; //note this also means the user is not able to set the default to null and cause the program to break down through the putDocument(InputStream, URI) API
        } else {
		return this.compressionFormat;
        }
    }
	@Override
	public int putDocument(InputStream input, URI uri) {
		return putDocument(input, uri, this.compressionFormat);
	}
	@Override
	public void setDefaultCompressionFormat(CompressionFormat format) {
		this.compressionFormat = format;
		return;
	}
	public void setStorageBaseDirectory(File dir) {
		docIO.setBaseDirectory(dir);
	}

	public class CompressionBox{
		CompressionBox(){}
		public byte[] compressionRedirection (InputStream in, CompressionFormat format) {//redirects based on format for compression
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
		public String decompressionRedirection(byte[] compressedDoc, CompressionFormat format) { //decompression redirect depending on format
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
		@SuppressWarnings("unused")
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
	}
	private class docComarator implements Comparator<DocumentImpl>{
		public String keyForComparing = "";
		
		@Override
		public int compare(DocumentImpl o1, DocumentImpl o2) {
			return (o2.wordCount(keyForComparing) - (o1.wordCount(keyForComparing)));
		}

		public void setKeyString(String key) {
			this.keyForComparing = key;
		}		
	}
	private class uriComparator implements Comparator<URI>{
		@Override
		public int compare(URI o1, URI o2) {
			return o2.toString().compareTo(o1.toString());
		}
	}
	/**
	 * @param uri to check its status in the system
	 * @return 1- the URI's document is in memory
	 * @return 2- the URI's document is on disk
	 * @return 3- the URI is not currently in the system (this includes if the document has been deleted
	 */
	private int uriStatus(URI uri) {
		if (memoryTable.get(uri) == null){
			return 3;
		}
		if (memoryTable.get(uri) == true){
			return 1;
		} 
		if (memoryTable.get(uri) == false){
			return 2;
		}
		return 0;
	}
	/**
	 * @param uri to check
	 * @return true if URI is in either memory or the disk, false otherwise (calls uriStatus(URI))
	 */
	private boolean docExistsInSystem(URI uri) {
		int x = uriStatus(uri);
		if ((x == 1) || (x == 2)) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * @param uri to check
	 * @return true if URI is in memory, false otherwise (calls uriStatus(URI))
	 */
	private boolean docExistsInMemory(URI uri) {
		int x = uriStatus(uri);
		if (x == 1) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * deletes uri from table completely, a call for this URI would return null
	 * @param uri
	 */
	private void deleteUriFromTable(URI uri) {
		this.memoryTable.remove(uri);
	}
	/**
	 * adds uri to table with true as the paired value
	 * @param uri
	 */
	private void addUriToMemory(URI uri) {
		this.memoryTable.put(uri, true);
	}
	/**
	 * adds uri to table with false value
	 * @param uri
	 */
	private void moveUriToDisk(URI uri) {
		this.memoryTable.put(uri, false);
	}
	private void increaseCounters(Document doc) {
		this.docCountInMemory++;
		this.compressedSpaceInMemory += doc.getDocument().length;
	}
	private void decreaseCounters(Document doc) {
		this.docCountInMemory--;
		this.compressedSpaceInMemory -= doc.getDocument().length;
	}
	private URI[] memoryManagment() {
		URI[] countURIs = null;
		URI[] spaceURIs = null;
		if (this.docLimit < this.docCountInMemory) {

			countURIs = removeDocsCount();
		} 
		if (this.spaceLimit < this.compressedSpaceInMemory) {
			spaceURIs = removeDocsSpace();
		}
		URI[] both = (URI[])ArrayUtils.addAll(countURIs, spaceURIs);
		if (both == null) {
			return new URI[0];
		}
		return both;
	}
	private URI[] removeDocsCount() {
		List<URI> uris = new ArrayList<URI>();
		while (this.docLimit < this.docCountInMemory) {
			UriTimePair pair = this.docHeap.removeMin();
			URI uri = pair.getKey();
			uris.add(uri);
			Document doc = this.docTree.get(uri);
			try {
				this.docTree.moveToDisk(uri);
				moveUriToDisk(uri);
			} catch (Exception e) { e.printStackTrace();}
			decreaseCounters(doc);
		}
		return uris.toArray(new URI[0]);
	}
	private URI[] removeDocsSpace() {
		List<URI> uris = new ArrayList<URI>();
		while (this.spaceLimit < this.compressedSpaceInMemory) {
			UriTimePair pair = this.docHeap.removeMin();
			URI uri = pair.getKey();
			uris.add(uri);
			Document doc = this.docTree.get(uri);
			try {
				this.docTree.moveToDisk(uri);
				moveUriToDisk(uri);
			} catch (Exception e) { e.printStackTrace();}
			decreaseCounters(doc);
		}
		return uris.toArray(new URI[0]);
	}

	private void createAndPushCommand(URI uri, Function<URI, Boolean> undoFunction, Function<URI, Boolean> redoFunction) {
		Command newCommand = new Command (uri, undoFunction, redoFunction);
		this.commandStack.push(newCommand);
	}
	private Document getAndDeleteFromTree(URI uri) {
		Document doc = this.docTree.get(uri);
		this.docTree.delete(uri);
		return doc;

	}

	private class UriTimePair implements Comparable<UriTimePair> {
		private URI uri;
		private long timeStamp;
		public UriTimePair(URI uri, long timeStamp){
			this.uri = uri;
			this.timeStamp = timeStamp;
		}
		
		public URI getKey() {
			return this.uri;
		}
		public long getTimeStamp() {
			return timeStamp;
		}
		public void setTimeStamp(long newStamp) {
			this.timeStamp = newStamp;
		}

		public int compareTo(UriTimePair o) {
			long x =(this.getTimeStamp() - o.getTimeStamp());
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
}