package edu.yu.cs.com1320.project.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.yu.cs.com1320.project.test.stage1.DocumentStoreTestStage1;
import edu.yu.cs.com1320.project.test.stage1.HashTableTestStage1;
import edu.yu.cs.com1320.project.test.stage2.DocumentStoreTestStage2;
import edu.yu.cs.com1320.project.test.stage2.StackTestStage2;
import edu.yu.cs.com1320.project.test.stage3.DocumentStoreTestStage3;
import edu.yu.cs.com1320.project.test.stage3.TrieTestStage3;

	@RunWith(Suite.class)
	@SuiteClasses({
		HashTableTestStage1.class,
		DocumentStoreTestStage1.class,
		DocumentStoreTestStage2.class,
		StackTestStage2.class,
		DocumentStoreTestStage3.class,
		TrieTestStage3.class
	})

	public class TestSuit {
		
	}


