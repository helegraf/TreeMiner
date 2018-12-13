package treeminer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;

import treeminer.util.TreeRepresentationUtils;

public class TreeMinerTest {

	public static TreeMiner treeMiner;

	@Before
	public void initializeTreeMiner() {
		treeMiner = new TreeMiner();
	}

	@Test
	public void testContainsSubtree() {
		assertEquals(true, TreeRepresentationUtils.containsSubtree("A B -1", "A"));
		assertEquals(true, TreeRepresentationUtils.containsSubtree("A B -1 C -1", "A C -1"));
		assertEquals(true, TreeRepresentationUtils.containsSubtree("A B -1 C -1", "A B -1 C -1"));
	}

	@Test
	public void testAddNodeToPrefixLastNode() {
		System.out.println(TreeRepresentationUtils.addNodeToTree("a b -1", new ImmutablePair<String, Integer>("c", 1)));
	}

	@Test
	public void testAddNodeToPrefixIntermediate() {
		System.out.println(TreeRepresentationUtils.addNodeToTree("a b -1", new ImmutablePair<String, Integer>("c", 0)));
	}

	@Test
	public void testAddNodeToRoot() {
		System.out.println(TreeRepresentationUtils.addNodeToTree("a", new ImmutablePair<String, Integer>("c", 0)));
	}

	@Test
	public void testAddNodeToLongTree() {
		System.out.println(TreeRepresentationUtils.addNodeToTree("a b c -1 e -1 -1 -1",
				new ImmutablePair<String, Integer>("d", 1)));
	}

	@Test
	public void testFindSubtrees() {
		System.out
				.println(treeMiner.findFrequentSubtrees(Arrays.asList("A B -1 C -1", "A", "A C -1", "A B D -1 -1"), 1));
		for (double[] chara : treeMiner.getCharacterizationsOfTrainingExamples()) {
			for (double number : chara) {
				System.out.print(number + ", ");
			}
			System.out.println();
		}
		
		treeMiner = new TreeMiner();
		treeMiner.setCountMultipleOccurrences(false);
		System.out
		.println(treeMiner.findFrequentSubtrees(Arrays.asList("A B -1 C -1", "A", "A C -1", "A B D -1 -1"), 1));
for (double[] chara : treeMiner.getCharacterizationsOfTrainingExamples()) {
	for (double number : chara) {
		System.out.print(number + ", ");
	}
	System.out.println();
}
	}

	public static String generateTree(int[] labels, int numNodes) {
		List<String> labelsInTree = new ArrayList<>();
		;
		int numMoveUpTokensToDistribute = -1;

		for (int i = 0; i < numNodes; i++) {
			labelsInTree.add(String.valueOf(labels[(int) (Math.random() * labels.length)]));
		}

		String moveUpToken = "-1";
		String separator = " ";
		StringBuilder builder = new StringBuilder();

		while (!labelsInTree.isEmpty() || numMoveUpTokensToDistribute > 0) {
			// If we have no moveUpTokens, add a label
			if (numMoveUpTokensToDistribute <= 0) {
				builder.append(separator);
				builder.append(labelsInTree.get(0));
				labelsInTree.remove(0);
				numMoveUpTokensToDistribute++;
			} else if (labelsInTree.isEmpty()) {
				// If we have moveUpTokens, but no labels, add a moveUpToken
				builder.append(separator);
				builder.append(moveUpToken);
				numMoveUpTokensToDistribute --;
			} else {
				// if we have both moveuptokens & labels choose randomly what to do
				if (Math.random() > 0.5) {
					builder.append(separator);
					builder.append(labelsInTree.get(0));
					labelsInTree.remove(0);
					numMoveUpTokensToDistribute++;
				} else {
					builder.append(separator);
					builder.append(moveUpToken);
					numMoveUpTokensToDistribute--;
				}
			}
		}
		
		return builder.toString().trim();
	}

	public static List<String> generateTreesForPerformanceTest(int numLabels, int numNodesPerTree, int numTrees) {
		int[] labels = new int[numLabels];
		for (int i = 0; i < numLabels; i++) {
			labels[i] = i;
		}
		
		List<String> trees = new ArrayList<>();
		for (int i = 0; i < numTrees; i++) {
			trees.add(generateTree(labels, numNodesPerTree));
		}
		
		return trees;
	}
	
	public static void performanceTest(int numLabels, int numNodesPerTree, int numTrees, int minSupport) {
		TreeMiner miner = new TreeMiner();
		long time = System.currentTimeMillis();
		miner.findFrequentSubtrees(generateTreesForPerformanceTest(numLabels, numNodesPerTree, numTrees), minSupport);
		long elapsed = System.currentTimeMillis() - time;
		
		System.out.println("Time for test: " + elapsed + "ms.");
	}

	public static void main(String[] args) {
		System.out.println(generateTreesForPerformanceTest(10, 3, 5));
		performanceTest(10, 3, 5, 1);
		performanceTest(100, 20, 10, 1);
		performanceTest(300, 50, 50, 1);
		performanceTest(500, 50, 2000, 1);
		performanceTest(500, 100, 80000, 80000 / 100 * 50);
	}
}
