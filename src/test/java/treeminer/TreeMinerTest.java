package treeminer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;

public class TreeMinerTest {
	
	public static TreeMiner treeMiner;
	
	@Before
	public void initializeTreeMiner() {
		treeMiner = new TreeMiner();
	}
	
	public void testContainsSubtree() {
		treeMiner.containsSubtree("A B -1", "A");
	}
	
	@Test
	public void testAddNodeToPrefixLastNode() {
		System.out.println(treeMiner.addNodeToPrefix("a b -1", new ImmutablePair<String,Integer>("c", 1)));
	}
	
	@Test
	public void testAddNodeToPrefixIntermediate() {
		System.out.println(treeMiner.addNodeToPrefix("a b -1", new ImmutablePair<String,Integer>("c", 0)));
	}
	
	public void testAddNodeToRoot() {
		System.out.println(treeMiner.addNodeToPrefix("a", new ImmutablePair<String,Integer>("c", 0)));
	}
	
	public void testAddNodeToLongTree() {
		System.out.println(treeMiner.addNodeToPrefix("a b c -1 e -1 -1 -1", new ImmutablePair<String,Integer>("d", 1)));
	}
}
