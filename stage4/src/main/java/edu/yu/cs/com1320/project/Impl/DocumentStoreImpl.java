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
import java.util.List;
import java.util.function.Function;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;

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
	private CompressionFormat compressionFormat;
	private HashTableImpl<URI,DocumentImpl> hashTable;
	private StackImpl<Command> commandStack;
	private TrieImpl<DocumentImpl> documentTrie;
	private MinHeapImpl<DocumentImpl> docHeap;
	private docComarator docComparator;
	private CompressionBox compressionBox;
	private int compressedSpaceInMemory;
	private int docCountInMemory;
	private int spaceLimit;
	private int docLimit;
	
	public DocumentStoreImpl() {
		this.hashTable = new HashTableImpl<URI,DocumentImpl>();
		this.compressionFormat = getDefaultCompressionFormat(); //for some reason the default setting is not working, so on instantiation it calls the default setter which returns ZIP if the default is null
		this.commandStack = new StackImpl<Command>();
		this.docComparator = new docComarator();
		this.documentTrie = new TrieImpl<DocumentImpl>(docComparator);	
		this.docHeap = new MinHeapImpl<DocumentImpl>();
		this.compressionBox = new CompressionBox();
		this.compressedSpaceInMemory = 0;
		this.docCountInMemory = 0;
		this.spaceLimit = Integer.MAX_VALUE;
		this.docLimit = Integer.MAX_VALUE;
		
	}
	@Override
	public int putDocument(InputStream input, URI uri, CompressionFormat format) {
		if (uri == null)  //can't have a null key
			throw new IllegalArgumentException();
		if (format == null)
			format = this.getDefaultCompressionFormat(); //and you thought you could catch me by passing null into the putDocument(InputStream, URI, CompressionFormat) API? sorry bud!
		if (input == null) //this is case 1, delete doc associated with this URI
			return putProcessForNullInput(uri);
		if (docExistsInHashtable(uri)) //this is case 2, overwrite old doc
			return putProcessForOverWriteInput(uri, input, format);
		else //this is case 3, new document insertion
			return putProcessForNewInput(uri, input, format);
	}
	private int putProcessForNullInput(URI uri) {
		if (docExistsInHashtable(uri)) {//and i am actually deleting something
			//get a refernce to that doc, and its current TimeStamp
			//remove it from hashtable
			//remove it from heap
			//remove it from trie
			//DONT remove it from stack
			//add new command for this to reverse these actions
			DocumentImpl oldDoc = this.hashTable.get(uri);
			long docslastUse = oldDoc.getLastUseTime();
			this.hashTable.put(uri, null);
			removeItemFromHeap(oldDoc);
			deleteStringFromTrie(this.compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat()), oldDoc);
			createCommandForNullDSIPutDocumentThatDeletes(oldDoc, docslastUse);
			return 0;
		} else {//but if i am not actually deleting something
			//considering nothing here actaully happnes, maybe leave it blank
			createCommandThatVirtuallyNothing(uri);
		}
		return 0;
	}
	private void createCommandThatVirtuallyNothing(URI uri) {
		Function<URI, Boolean> undoFunction = (URI) ->{
			//nothing to undo
			return false;
		};
		Function<URI,Boolean> redoFunction = (URI) ->{
			//to redo this is to delete this doc from all strucres
			DocumentImpl docSearch = this.hashTable.get(uri);
			if (docSearch == null) {
				return false;
			} else {
				this.hashTable.put(uri, null);
				deleteStringFromTrie(compressionBox.decompressionRedirection(docSearch.getDocument(), docSearch.getCompressionFormat()), docSearch);
				removeItemFromHeap(docSearch);
				return true;
			}
		};
		Command newCommand = new Command (uri, undoFunction, redoFunction);
		this.commandStack.push(newCommand);
	}
	private void createCommandForNullDSIPutDocumentThatDeletes(DocumentImpl oldDoc, long oldDocsTimeStampBeforeDelete) {
		//the undo of a null input where soemthing was deleted is to reinsert the document back to how it was
		String origDoc = compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat());
		Function<URI, Boolean> undoFunction = (URI) ->{
			//put it back in the hashtable
			//put it back in the heap,
			//put it back in the trie
			//create command for the stack
			this.hashTable.put(oldDoc.getKey(), oldDoc);
			oldDoc.setLastUseTime(oldDocsTimeStampBeforeDelete);
			this.docHeap.insert(oldDoc);
			addStringToTrie(origDoc, oldDoc);
			return true;
		};
		Function<URI,Boolean> redoFunction = (URI) ->{
			//to redo a null put where something was deleted, just re-remove refrences from all the stuff
			DocumentImpl results = this.hashTable.put(oldDoc.getKey(), null);
			removeItemFromHeap(oldDoc);
			deleteStringFromTrie(this.compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat()), oldDoc);
			return (results != null);
		};
		Command newCommand = new Command (oldDoc.getKey(), undoFunction, redoFunction);
		this.commandStack.push(newCommand);
	}
	
	private int putProcessForOverWriteInput(URI uri, InputStream input, CompressionFormat format) {
		//get reference to old doc, and its time stamp
		//remove it from the hashtable
		//remove it from the trie
		//dont remove it from the stack
		//verify space for new doc
		
		//create new document for this document
		//add new doc to hashtable
		//add new doc to trie
		//add new doc to heap
		//add new command to stack to reverse these actions
		DocumentImpl oldDoc = this.hashTable.get(uri);
		long oldDocslastUse = oldDoc.getLastUseTime();
		this.hashTable.put(uri, null);
		removeItemFromHeap(oldDoc);
		deleteStringFromTrie(this.compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat()), oldDoc);
		
		String newOrigDoc = byteArrayToString(input);
		byte[] newDocByteArray = compressionBox.compressionRedirection(new ByteArrayInputStream(newOrigDoc.getBytes()), format);
		addingToStorage(newDocByteArray.length - oldDoc.getDocument().length, 0);
		DocumentImpl newDoc = new DocumentImpl(newDocByteArray, uri, format, newOrigDoc);
		this.hashTable.put(uri, newDoc);
		addStringToTrie(newOrigDoc, newDoc);
		this.docHeap.insert(newDoc);
		createCommandForDSIPutDocumentOverwrite(uri, newDoc, oldDoc, oldDocslastUse);	
		return newOrigDoc.hashCode();
	}
	private void createCommandForDSIPutDocumentOverwrite(URI uri, DocumentImpl newDoc, DocumentImpl oldDoc, long oldDocLastRecordedTime) {
		//to undo this instance, have to remove all refernces of the new doc and replace the old doc back into the system
		Function<URI,Boolean> undoFunction = (URI) ->{
			//first remove the newDoc
			this.hashTable.put(uri, null);
			deleteStringFromTrie(compressionBox.decompressionRedirection(newDoc.getDocument(),newDoc.getCompressionFormat()), newDoc);
			removeItemFromHeap(newDoc);
			
			//then add the oldDoc
			this.hashTable.put(uri, oldDoc);
			addStringToTrie(compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat()), oldDoc);
			this.docHeap.insert(oldDoc);
			updateItemInHeap(oldDoc, oldDocLastRecordedTime);
			return true;
		};
		Function<URI,Boolean> redoFunction = (URI) ->{
			//redo what was done in hte origional method
			this.hashTable.put(uri, null);
			removeItemFromHeap(oldDoc);
			deleteStringFromTrie(this.compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat()), oldDoc);
			
			this.hashTable.put(uri, newDoc);
			addStringToTrie(compressionBox.decompressionRedirection(newDoc.getDocument(), newDoc.getCompressionFormat()), newDoc);
			this.docHeap.insert(newDoc);
			return true;
		};
		Command newCommand = new Command(uri, undoFunction, redoFunction);
		this.commandStack.push(newCommand);
	}
 	
	private int putProcessForNewInput(URI uri, InputStream input, CompressionFormat format) {
		String newOrigDoc = byteArrayToString(input);
		byte[] newDocByteArray = compressionBox.compressionRedirection(new ByteArrayInputStream(newOrigDoc.getBytes()), format);
		addingToStorage(newDocByteArray.length, 1);
		DocumentImpl newDoc = new DocumentImpl(newDocByteArray, uri, format, newOrigDoc);
		this.hashTable.put(uri, newDoc);
		addStringToTrie(newOrigDoc, newDoc);
		this.docHeap.insert(newDoc);
		createCommandForNewDSIPut(newDoc);
		return newOrigDoc.hashCode();
	}
 	private void createCommandForNewDSIPut(DocumentImpl newDoc) {
 		long origDocTimeStamp = newDoc.getLastUseTime();
 		Function<URI,Boolean> undoFunction = (URI) ->{
 			//the undo of a completely new put is to just remove said document
 			this.hashTable.put(newDoc.getKey(), null);
 			deleteStringFromTrie(compressionBox.decompressionRedirection(newDoc.getDocument(), newDoc.getCompressionFormat()), newDoc);
 			removeItemFromHeap(newDoc);
 			return true;
 		};
 		Function<URI,Boolean> redoFunction = (URI) ->{
 			//the redo of a new put is just that, reput it
 			this.hashTable.put(newDoc.getKey(), newDoc);
 			addStringToTrie(compressionBox.decompressionRedirection(newDoc.getDocument(), newDoc.getCompressionFormat()), newDoc);
 			this.docHeap.insert(newDoc);
 			updateItemInHeap(newDoc, origDocTimeStamp);
 			return true;
 		};
 		Command newCommand = new Command(newDoc.getKey(), undoFunction, redoFunction);
		this.commandStack.push(newCommand);
 	}
	
	@Override
	public String getDocument(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException();
		}
		DocumentImpl docInstance = this.hashTable.get(uri);//get docInstance based off key
		if ((docInstance == null) || (null == docInstance.getDocument())) {//if either the document itself is null or the contents are null, then return null
			return null;
		}
		String decompressedDoc = compressionBox.decompressionRedirection(docInstance.getDocument(), docInstance.getCompressionFormat()); //else, get the decompressed version of the string and return it
		docTimeUpdateToCurrent(docInstance);
		return decompressedDoc;
	}

	@Override
	public byte[] getCompressedDocument(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException();
		}
		DocumentImpl docInstance = this.hashTable.get(uri);//get docInstance based off key
		if ((docInstance == null) || (null == docInstance.getDocument())) {//if either the document itself is null or the contents are null, then return null
			return null;
		} else {
			docTimeUpdateToCurrent(docInstance);//only uncomment if timeupdate is used in a situation of a get
			return docInstance.getDocument(); //returns the compressed byte array of the document without converting it back to a string
		}
	}

	@Override
	public boolean deleteDocument(URI uri) {
		return deleteDocument(uri, true);//pass forward to real implementation and create a command instance if applicable
	}

	@Override
	public List<String> search(String keyword) {	
		keyword = keyword.toLowerCase();
		docComparator.setKeyString(keyword); 
		List<DocumentImpl> allDocs = documentTrie.getAllSorted(keyword);
		List<String> decompressedDocs = new ArrayList<String>();
		long currentTime = System.currentTimeMillis(); 
		for (int x = 0; x < allDocs.size(); x++) {
			DocumentImpl tempDoc = allDocs.get(x);
			updateItemInHeap(tempDoc, currentTime);
			decompressedDocs.add(this.getDocument(tempDoc.getKey()));
		}
		return decompressedDocs;
	}

	@Override
	public List<byte[]> searchCompressed(String keyword) {
		keyword = keyword.toLowerCase();
		docComparator.setKeyString(keyword);
		List<DocumentImpl> allDocs = documentTrie.getAllSorted(keyword);
		List<byte[]> compressedDocs = new ArrayList<byte[]>();
		long currentTime = System.currentTimeMillis();
		for (int x = 0; x < allDocs.size(); x++) {
			DocumentImpl tempDoc = allDocs.get(x);
			updateItemInHeap(tempDoc, currentTime);
			compressedDocs.add(this.getCompressedDocument(tempDoc.getKey()));
		}
		return compressedDocs;
	}
	
	private boolean deleteDocument(URI uri, boolean commandStatus) {
		if (uri == null) {
			throw new IllegalArgumentException();
		}
		DocumentImpl tempDoc = this.hashTable.get(uri);
		if (tempDoc == null) { //if the search returned null, the key is not present. nothing to do, return false
			return false;
		} else {
			long oldDocTimeStamp = tempDoc.getLastUseTime();
			String stringToDelete = compressionBox.decompressionRedirection(tempDoc.getDocument(), tempDoc.getCompressionFormat());
			removingFromStorage(tempDoc.getDocument().length, 1);
			this.hashTable.put(uri, null);//doc was found, replace value of this key with null, effectively deleting the document
			deleteStringFromTrie(stringToDelete, tempDoc);
			removeItemFromHeap(tempDoc);
			createDeleteCommandWhereItemWasFound(tempDoc, oldDocTimeStamp);
			return true;
		}
	}
	private void createDeleteCommandWhereItemWasFound(DocumentImpl oldDoc, long oldDocTimeStamp) {
		Function<URI, Boolean> undoFunction = (URI) -> {
			//to undo a delete is to reput the document
			this.hashTable.put(oldDoc.getKey(), oldDoc);
			addStringToTrie(compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat()), oldDoc);
			this.docHeap.insert(oldDoc);
			updateItemInHeap(oldDoc, oldDocTimeStamp);
			return true;
		};
		Function<URI, Boolean> redoFunction = (URI) -> {	
			//to redo is to redelete refrences from all structures
			DocumentImpl tempDoc = this.hashTable.get(oldDoc.getKey());
			this.hashTable.put(oldDoc.getKey(), null);
			deleteStringFromTrie(compressionBox.decompressionRedirection(oldDoc.getDocument(), oldDoc.getCompressionFormat()), oldDoc);
			removeItemFromHeap(oldDoc);
			return (tempDoc != null);
		};
		Command newCommand = new Command(oldDoc.getKey(), undoFunction, redoFunction);
		this.commandStack.push(newCommand);
	}
	
 	private boolean addingToStorage(int memoryToBeTaken, int numberOfDocsToAdd) {
 		if (memoryToBeTaken > this.spaceLimit) {
 			throw new IllegalArgumentException();
 		}
		if (this.compressedSpaceInMemory + memoryToBeTaken > this.spaceLimit) {
			removeSpaceFromMemory(memoryToBeTaken);
		}
		this.compressedSpaceInMemory += memoryToBeTaken;
		
		if (this.docCountInMemory + numberOfDocsToAdd > this.docLimit) {
			removeDocsFromMemory(numberOfDocsToAdd);
		}
		this.docCountInMemory += numberOfDocsToAdd;
		return true;
	}
	
	private void removingFromStorage(int memoryRelease, int numberOfDocs) {//this method just subtracts from the counters, not so complex
		this.compressedSpaceInMemory -= memoryRelease;
		this.docCountInMemory -= numberOfDocs;
	}
	
	private void removeDocsFromMemory(int numberOfDocsToFit) {
		while (this.docCountInMemory + numberOfDocsToFit > this.docLimit) {
			DocumentImpl removedDoc = this.docHeap.removeMin();
			this.deleteDocumentByForce(removedDoc);
		}
	}
	
	private void removeSpaceFromMemory(int memoryToAdd) {
		while (this.compressedSpaceInMemory + memoryToAdd > this.spaceLimit) {
			DocumentImpl removedDoc = this.docHeap.removeMin();
			this.deleteDocumentByForce(removedDoc);
		}
	}
	
	private void deleteDocumentByForce(DocumentImpl docToRemove) {
		//note the reference has already been removed from the heap
		this.docCountInMemory--;
		this.compressedSpaceInMemory -= docToRemove.getDocument().length;
		this.hashTable.put(docToRemove.getKey(), null);
		deleteStringFromTrie(compressionBox.decompressionRedirection(docToRemove.getDocument(), docToRemove.getCompressionFormat()), docToRemove);
		removeURIFromStack(docToRemove.getKey());//removes from stack
	}
	
	@Override
	public boolean undo() throws IllegalStateException {
		Command commandToUndo = this.commandStack.pop();//get top of stack
		if (commandToUndo == null) {//if there is nothing on the stack, meaning its empty
			throw new IllegalStateException();
		}
		boolean results = commandToUndo.undo();//else, call the undo function of that command, which returns a boolean
		updateDocCount();
		addingToStorage(0,0);
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
		for (int x = 0; x < tempStack.size(); x++) {//for each element that was pushed into the temp stack
			Command tempCommand = tempStack.pop();
			tempCommand.redo();
			this.commandStack.push(tempCommand);//put it back in the old one
		}
		updateDocCount();
		addingToStorage(0,0);
		return results;
	}
	private void removeURIFromStack(URI uriToRemove) {
		int stackSize = this.commandStack.size();
		StackImpl<Command> tempStack = new StackImpl<Command>();
		for (int x = 0; x < stackSize; x++) {//for all elements in the current stack (including the one we just deleted/added)
			Command temp = this.commandStack.pop();//get the most resent
			if (!temp.getUri().equals(uriToRemove)) {//if that command's URI does NOT equals the one we are removing from memory
				tempStack.push(temp);//add it to a temp stack
			}//this will filter all commands with that URI from the stack
		}
		for (int x = 0; x < tempStack.size(); x++) {//for all commands that made it through the filter
			Command temp = tempStack.pop();//take the top one
			this.commandStack.push(temp);//add it back to the stack
		}
	}
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

	private void deleteStringFromTrie(String strToDelete, DocumentImpl docToRemove) {
		String[] allWords = strToDelete.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		for ( String tempString : allWords ) {
			this.documentTrie.delete(tempString, docToRemove);
		}
	}
	
	private void addStringToTrie(String strToAdd, DocumentImpl docToAdd) {
		String[] allWords = strToAdd.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		for ( String tempString : allWords ) {
			this.documentTrie.put(tempString, docToAdd);
		}
	}
	@Override
	public void setMaxDocumentCount(int limit) {
		if (limit < 0) {
			throw new RuntimeException();
		}
		if (limit == 0) {
			this.docLimit = Integer.MAX_VALUE;
		} else {
			this.docLimit = limit;
			addingToStorage(0,0);
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
			addingToStorage(0,0);
		}
	}
	private void removeItemFromHeap(DocumentImpl tempDoc) {
		tempDoc.setLastUseTime(1);
		try {
			this.docHeap.reHeapify(tempDoc);
			this.docHeap.removeMin();
		} catch (NullPointerException e) {}
	}
	private void updateItemInHeap(DocumentImpl doc, long updatedTimeStamp) {
		doc.setLastUseTime(updatedTimeStamp);
		try {
			this.docHeap.reHeapify(doc);
		} catch (NullPointerException e) {}
	}
	private void docTimeUpdateToCurrent(DocumentImpl doc) {//this method updates the time stamp on a current document to the current time, and calls reheap on that document
		doc.setLastUseTime(System.currentTimeMillis());
		try {//this program to a degree runs in a loop when forcfully removing a doc. the loop will end here
			this.docHeap.reHeapify(doc);
		} catch (NullPointerException e) {}
	}
	
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
	private boolean docExistsInHashtable(URI uri) {
		DocumentImpl doc = this.hashTable.get(uri);
		if (doc != null) {
			return true;
		} else {
			return false;
		}
	}
	private void updateDocCount() {
		this.docCountInMemory = this.hashTable.getSize();
	}
	@Override
	public void setDefaultCompressionFormat(CompressionFormat format) {
		this.compressionFormat = format;
		return;
	}
	private class CompressionBox{
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
}