package edu.yu.cs.com1320.project.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import edu.yu.cs.com1320.project.test.stage1.DocumentStoreTestStage1;
import edu.yu.cs.com1320.project.test.stage1.HashTableTestStage1;

public class stage2TestSuit {
	@RunWith(Suite.class)
	@Suite.SuiteClasses({
		HashTableTestStage1.class,
		DocumentStoreTestStage1.class,
	})

	public class Stage2TestSuit {
		
	}
}