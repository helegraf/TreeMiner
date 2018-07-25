package treeminer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

public class TreeMinerTest {
	
	@Test
	public void testAddNodeToPrefixLastNode() {
		TreeMiner treeMiner = new TreeMiner();
		System.out.println(treeMiner.addNodeToPrefix("a b -1", new ImmutablePair<String,Integer>("c", 1)));
	}
	
	@Test
	public void testAddNodeToPrefixIntermediate() {
		TreeMiner treeMiner = new TreeMiner();
		System.out.println(treeMiner.addNodeToPrefix("a b -1", new ImmutablePair<String,Integer>("c", 0)));
	}
	
	public void testAddNodeToRoot() {
		TreeMiner treeMiner = new TreeMiner();
		System.out.println(treeMiner.addNodeToPrefix("a", new ImmutablePair<String,Integer>("c", 0)));
	}
	
	public void testAddNodeToLongTree() {
		TreeMiner treeMiner = new TreeMiner();
		System.out.println(treeMiner.addNodeToPrefix("a b c -1 e -1 -1 -1", new ImmutablePair<String,Integer>("d", 1)));
	}
}
