package edu.yu.cs.com1320.project.test.stage2;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.yu.cs.com1320.project.Impl.StackImpl;

@SuppressWarnings("unused")
public class StackTestStage2 {
	final static private boolean Trace = false; //use to trace testing
	
	@Test public void initializeStack() {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 Stack Test 1, 'initializeStack'");
		}
		@SuppressWarnings("rawtypes")
		StackImpl stack = new StackImpl();
	}
	@Test (expected = RuntimeException . class) public void StackAddNull() {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 Stack Test 2, 'StackAddNull'");
		}
		StackImpl<String> stack = new StackImpl<String>();
		stack.push(null);
	}
	@Test  public void StackAddPeekPopSingle() {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 Stack Test 3, 'StackAddPeekPopSingle'");
		}
		StackImpl<String> stack = new StackImpl<String>();
		String str = "hello there";
		stack.push(str);
		assertTrue(str.equals(stack.peek()));
		assertTrue(str.equals(stack.pop()));
	}
	@Test  public void StackAddMany() {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 Stack Test 4, 'StackAddMany'");
		}
		StackImpl<String> stack = new StackImpl<String>();
		String str = "hello there";
		for (int x = 0; x < 200; x++) {
		stack.push(str + x);
		}
	}
	@Test  public void StackRemoveMany() {
		if (Trace == true) {
			System.out.println("\ntrace: Stage 2 Stack Test 4, 'StackRemoveMany'");
		}
		StackImpl<String> stack = new StackImpl<String>();
		String str = "hello there";
		for (int x = 0; x < 200; x++) {
			stack.push(str + x);
		}
		for (int x = 0; x < 200; x++) {
			assertTrue((str + (200-x-1)).equals(stack.peek()));
			assertTrue((str + (200-x-1)).equals(stack.pop()));
		}
		assertTrue(null==stack.peek());
		assertTrue(null==stack.pop());
	}
}
