package treeminer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import treeminer.util.TreeRepresentationUtils;

/**
 * Test the correct functionality of the TreeMiner implementation.
 * 
 * @author Helena Graf
 *
 */
public class TreeMinerTest {

	/**
	 * Test the functionality of the contains subtree method that checks whether a
	 * tree contains a subtree.
	 */
	@Test
	public void testContainsSubtree() {
		assertEquals(true, TreeRepresentationUtils.containsSubtree("A B -", "A"));
		assertEquals(true, TreeRepresentationUtils.containsSubtree("A B - C -", "A C -"));
		assertEquals(true, TreeRepresentationUtils.containsSubtree("A B - C -", "A B - C -"));

		assertEquals(false, TreeRepresentationUtils.containsSubtree("A B -", "C"));
		assertEquals(false, TreeRepresentationUtils.containsSubtree("A B C - -", "A C -"));
		assertEquals(false, TreeRepresentationUtils.containsSubtree("A B - C -", "A c - B -"));
	}

	/**
	 * Test adding a node to the last node of a subtree.
	 */
	@Test
	public void testAddNodeToPrefixLastNode() {
		assertEquals("a b c - -",
				TreeRepresentationUtils.addNodeToTree("a b -", new ImmutablePair<String, Integer>("c", 1)));
	}

	/**
	 * Test adding a node to an intermediate node of a subtree.
	 */
	@Test
	public void testAddNodeToPrefixIntermediate() {
		assertEquals("a b - c -",
				TreeRepresentationUtils.addNodeToTree("a b -", new ImmutablePair<String, Integer>("c", 0)));
	}

	/**
	 * Test adding a node to a subtree that only consist of one node.
	 */
	@Test
	public void testAddNodeToRoot() {
		assertEquals("a c -", TreeRepresentationUtils.addNodeToTree("a", new ImmutablePair<String, Integer>("c", 0)));
	}

	/**
	 * Test adding a node to a tree that has a spaghetti-like structure.
	 */
	@Test
	public void testAddNodeToLongTree() {
		assertEquals("a b c - e - d - - -",
				TreeRepresentationUtils.addNodeToTree("a b c - e - - -", new ImmutablePair<String, Integer>("d", 1)));
	}

	/**
	 * Test correctness of tree miner when finding non-distinct pattern occurrences.
	 */
	@Test
	public void testFindSubtreesNonDistinct() {
		TreeMiner treeMiner = new TreeMiner();
		treeMiner.setCountMultipleOccurrences(false);
		List<String> foundTrees = treeMiner.findFrequentSubtrees(Arrays.asList("A B - C -", "A", "A C -", "A B D - -"),
				1);
		double[][] characterizations = treeMiner.getCharacterizationsOfTrainingExamples();

		assertEquals(Arrays.asList("A", "A B -", "A B - C -", "A B D - -", "A C -", "B", "B D -", "C", "D"),
				foundTrees);
		assertArrayEquals(new double[] { 1, 1, 1, 0, 1, 1, 0, 1, 0 }, characterizations[0], 0);
		assertArrayEquals(new double[] { 1, 0, 0, 0, 0, 0, 0, 0, 0 }, characterizations[1], 0);
		assertArrayEquals(new double[] { 1, 0, 1, 0, 0, 1, 0, 0, 0 }, characterizations[2], 0);
		assertArrayEquals(new double[] { 1, 1, 0, 1, 1, 0, 1, 0, 1 }, characterizations[3], 0);
	}

	/**
	 * Test correctness of tree miner when finding distinct pattern occurrences.
	 */
	@Test
	public void testFindSubtreesDistinct() {
		TreeMiner treeMiner = new TreeMiner();
		treeMiner.setCountMultipleOccurrences(true);
		List<String> foundTrees = treeMiner.findFrequentSubtrees(Arrays.asList("A B - C -", "A", "A C -", "A B D - -"),
				1);
		double[][] characterizations = treeMiner.getCharacterizationsOfTrainingExamples();

		assertEquals(Arrays.asList("A", "A B -", "A B - C -", "A B D - -", "A C -", "B", "B D -", "C", "D"),
				foundTrees);
		assertArrayEquals(new double[] { 1, 1, 1, 0, 1, 1, 0, 1, 0 }, characterizations[0], 0);
		assertArrayEquals(new double[] { 1, 0, 0, 0, 0, 0, 0, 0, 0 }, characterizations[1], 0);
		assertArrayEquals(new double[] { 1, 0, 1, 0, 0, 1, 0, 0, 0 }, characterizations[2], 0);
		assertArrayEquals(new double[] { 1, 1, 0, 1, 1, 0, 1, 0, 1 }, characterizations[3], 0);
	}
}
