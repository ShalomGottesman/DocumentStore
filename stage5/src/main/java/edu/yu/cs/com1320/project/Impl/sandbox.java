package edu.yu.cs.com1320.project.Impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Hashtable;

import edu.yu.cs.com1320.project.DocumentStore;

public class sandbox {
	public static void main(String[] args) {
	String str = "hi My name is sHalOm";
	String[] ary = str.replaceAll("[^a-zA-Z\\s]", "").toLowerCase().split("\\s+");
	System.out.println(Arrays.toString(ary));
	
}
}
