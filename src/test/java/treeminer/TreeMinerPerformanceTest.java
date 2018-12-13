package treeminer;

import java.util.ArrayList;
import java.util.List;

import treeminer.util.TreeDatabaseSupplier;

/**
 * Class for testing the performance of the tree miner. Non-Unit test.
 * 
 * @author Helena Graf
 *
 */
public class TreeMinerPerformanceTest {

	/**
	 * Execute a number of performance test.
	 * 
	 * @param args
	 *            ignored
	 */
	public static void main(String[] args) {
		TreeMinerPerformanceTest.comparePerformanceOfVariants(10, 3, 5, 1, true);
		TreeMinerPerformanceTest.comparePerformanceOfVariants(10, 3, 5, 1, true);
		TreeMinerPerformanceTest.comparePerformanceOfVariants(10, 3, 5, 1, true);
		TreeMinerPerformanceTest.comparePerformanceOfVariants(10, 3, 5, 1, true);
		TreeMinerPerformanceTest.comparePerformanceOfVariants(10, 3, 5, 1, true);
		TreeMinerPerformanceTest.comparePerformanceOfVariants(100, 20, 10, 1, false);
		TreeMinerPerformanceTest.comparePerformanceOfVariants(300, 50, 50, 5, false);
		TreeMinerPerformanceTest.comparePerformanceOfVariants(500, 50, 2000, 20, false);
	}

	/**
	 * Execute a performance test according to the given parameters.
	 * 
	 * @param minSupport
	 *            the minimum support a pattern should have to be considered
	 *            frequent for this test
	 * @param countMultipleOccurrences
	 *            whether the tree miner should be configured to count multiple
	 *            occurrences of a pattern in a tree for this test
	 */
	public static List<String> performanceTestDistinct(List<String> generatedTrees, int minSupport,
			boolean countMultipleOccurrences) {
		TreeMiner miner = new TreeMiner();
		miner.setCountMultipleOccurrences(countMultipleOccurrences);
		List<String> trees = miner.findFrequentSubtrees(generatedTrees, minSupport);
		return trees;
	}

	/**
	 * @param numLabels
	 *            the number of distinct labels in the database used in the test
	 * @param numNodesPerTree
	 *            the number of nodes in each tree in the database used in the test
	 * @param numTrees
	 *            the number of trees in the database used in the test
	 */
	public static void comparePerformanceOfVariants(int numLabels, int numNodesPerTree, int numTrees, int minSupport,
			boolean printTrees) {

		System.out.println("Start experiment: " + numLabels + " labels, " + numNodesPerTree + " nodes/tree, " + numTrees
				+ " trees, " + minSupport + " min. support.");

		List<String> generatedTrees = TreeDatabaseSupplier.generateTreesForPerformanceTest(numLabels, numNodesPerTree,
				numTrees);
		if (printTrees) {
			System.out.println(generatedTrees);
		}

		long time = System.currentTimeMillis();
		List<String> trees1 = performanceTestDistinct(generatedTrees, minSupport, true);
		long elapsed = System.currentTimeMillis() - time;
		System.out.println("time: " + elapsed + " count_distinct: true");

		time = System.currentTimeMillis();
		List<String> trees2 = performanceTestDistinct(generatedTrees, minSupport, false);
		elapsed = System.currentTimeMillis() - time;
		System.out.println("time: " + elapsed + " count_distinct: false");

		int notFound = 0;
		List<String> unfound = new ArrayList<>();
		for (String tree1 : trees1) {
			if (!trees2.contains(tree1)) {
				notFound++;
				unfound.add(tree1);
			}
		}
		int additionallyFound = 0;
		List<String> addFound = new ArrayList<>();
		for (String tree2 : trees2) {
			if (!trees1.contains(tree2)) {
				additionallyFound++;
				addFound.add(tree2);
			}
		}
		System.out.println(trees1.size() + " vs " + trees2.size());
		System.out.println("Additionally found: " + additionallyFound + " not found " + notFound);
		if (printTrees) {
			System.out.println(trees1);
			System.out.println(trees2);
			System.out.println("Additional: " + addFound);
			System.out.println("Missing: " + unfound);
		}
	}

}
