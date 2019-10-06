package edu.yu.cs.com1320.project.Impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.yu.cs.com1320.project.*;

public class DocumentIOImpl extends DocumentIO{
	//private HashMap<URI, Long> timeMap = new HashMap<URI, Long>();
	private String workingDir;
	private String subWorkingDir;//note this String when updated should include the workingDir String as well
	
	public DocumentIOImpl() {
		workingDir = System.getProperty("user.dir");
	}
	
	public DocumentIOImpl(File dir) {
		workingDir = System.getProperty("user.dir");
		setBaseDirectory(dir);
	}
	
	public void setBaseDirectory(File dir) {
		if (dir == null) {
			throw new IllegalArgumentException("");
		}
		dir.mkdirs();
		workingDir = dir.toPath().toString();
	}

	@Override
	public File serialize(Document doc) {	
		URI fullDocName = doc.getKey();
		//long docsLastUsedTime = doc.getLastUseTime();
		//timeMap.put(fullDocName, docsLastUsedTime);		
		establishSubDir(fullDocName);//set up and store the sub directory from the working one based on URI prefixs
		String docName = getDocName(fullDocName);//derive the document name by removing prefixs
		documentExclusionStrategy docStrat = new documentExclusionStrategy();
		GsonBuilder gbuild = new GsonBuilder();
		gbuild.addSerializationExclusionStrategy(docStrat);
		String serializedJson = gbuild.create().toJson(doc);
		File newFile = new File(subWorkingDir + File.separator + docName + ".json");
	    try {
	    	BufferedWriter writer = new BufferedWriter(new FileWriter(newFile, false));
			writer.write(serializedJson);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newFile;
	}

	@Override
	public Document deserialize(URI uri) {
		establishSubDir(uri);
		String docName = getDocName(uri);
		
		File file = new File(subWorkingDir + File.separator + docName + ".json");
		if (!file.exists()) {
			return null;
		}
		String json = null;
		try {
			json = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		documentExclusionStrategy docStrat = new documentExclusionStrategy();
		GsonBuilder gbuild = new GsonBuilder();
		gbuild.addDeserializationExclusionStrategy(docStrat);
		Document doc = gbuild.create().fromJson(json, DocumentImpl.class);
		doc.setLastUseTime(System.currentTimeMillis());
		//doc.setLastUseTime(timeMap.get(doc.getKey()));	
		//timeMap.remove(doc.getKey());		
		file.delete();
		return doc;
	}
	
	private void establishSubDir(URI uri) {
		String uriString = uri.toString();
		String subDirPath = dirPrefixSuffixCut(uriString);//this method must remove the first and last directory seperation charecter
		//String fullDirPath = workingDir + File.separatorChar + subDirPath;
		String fullDirPath = workingDir + File.separator + subDirPath;
		File dir = new File(fullDirPath);
		dir.mkdirs();
		subWorkingDir = dir.toPath().toString();
	}
	
	private String dirPrefixSuffixCut(String uriToCut) {
		String removedPrefix = removePrefix(uriToCut);
		String bothRemoved = removeSuffix(removedPrefix);
		return bothRemoved;
	}
	
	private String getDocName(URI uri) {
		String tempStr = null;
		String uriToCut = uri.toString();
		int strLength = uriToCut.length();
		int dirSepIndex = strLength;
		for(int x = (strLength - 1); x >= 0; x--) {
			Character temp = uriToCut.charAt(x);
			//Character temp2 = File.separatorChar;
			Character temp2 = '/';
			if (temp == temp2) {
				dirSepIndex = x;
				break;
			}
		}
		if (dirSepIndex != strLength) {
			tempStr = uriToCut.substring(dirSepIndex + 1, strLength);
		} else {
			tempStr = uriToCut;
		}
		return tempStr;
	}
	
	private String removeSuffix(String uriToCut) {
		String tempStr = null;
		int strLength = uriToCut.length();
		int dirSepIndex = strLength;
		for(int x = (strLength - 1); x >= 0; x--) {
			Character temp = uriToCut.charAt(x);
			//Character temp2 = File.separatorChar;
			Character temp2 = '/';
			if (temp == temp2) {
				dirSepIndex = x;
				break;
			}
		}
		tempStr = uriToCut.substring(0, dirSepIndex);
		return tempStr;
	}
	
	private String removePrefix(String uriToCut) {
		String tempStr = null;
		boolean prefixRemoved = false;
		String subStr1 = uriToCut.substring(0, 8);
		if (subStr1.equals("https://")) {
			tempStr = uriToCut.substring(8, uriToCut.length());
			prefixRemoved = true;
		} else {
			String subStr2 = uriToCut.substring(0, 7);
			if (subStr2.equals("http://")) {
				tempStr = uriToCut.substring(7, uriToCut.length());
				prefixRemoved = true;
			}
		}
		if (!prefixRemoved) {
			tempStr = uriToCut;
		}
		return tempStr;
	}
	
	class documentExclusionStrategy implements ExclusionStrategy{
		documentExclusionStrategy(){}
		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			return f.getName().equals("lastEdited");
		}
		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	}
}
