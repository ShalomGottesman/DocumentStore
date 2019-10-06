package edu.yu.cs.com1320.project.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

public class stage1TestSuit {
	@RunWith(Suite.class)
	@Suite.SuiteClasses({
		HashTableTestStage1.class,
		DocumentStoreTestStage1.class,
	})

	public class Stage1TestSuit {
		
	}
}