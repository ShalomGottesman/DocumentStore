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
import edu.yu.cs.com1320.project.test.stage4.DocumentStoreTestStage4;
import edu.yu.cs.com1320.project.test.stage4.MinHeapTestStage4;

	@RunWith(Suite.class)
	@SuiteClasses({
		HashTableTestStage1.class,
		DocumentStoreTestStage1.class,
		DocumentStoreTestStage2.class,
		StackTestStage2.class,
		DocumentStoreTestStage3.class,
		TrieTestStage3.class,
		MinHeapTestStage4.class,
		DocumentStoreTestStage4.class
	})

	public class TestSuit {
		
	}


