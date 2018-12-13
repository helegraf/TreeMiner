package treeminer.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A supplier for generated trees and databases of trees for performance tests.
 * 
 * @author Helena Graf
 *
 */
public class TreeDatabaseSupplier {

	/**
	 * Generate a tree made up of the given labels that in total has the given
	 * number of nodes.
	 * 
	 * @param labels
	 *            the labels that should occur in the tree (it is not guaranteed
	 *            that all labels will occur as they are chosen randomly)
	 * @param numNodes
	 *            the number of nodes that the tree should have in total
	 * @return the newly constructed tree
	 */
	public static String generateTree(int[] labels, int numNodes) {
		List<String> labelsInTree = new ArrayList<>();
		;
		int numMoveUpTokensToDistribute = -1;

		for (int i = 0; i < numNodes; i++) {
			labelsInTree.add(String.valueOf(labels[(int) (Math.random() * labels.length)]));
		}

		String moveUpToken = "-";
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
				numMoveUpTokensToDistribute--;
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

	/**
	 * Generate a database of trees that can be used for a performance test of the
	 * tree miner.
	 * 
	 * @param numLabels
	 *            the total number of distinct labels that should occur in the
	 *            database
	 * @param numNodesPerTree
	 *            how many nodes each tree should have
	 * @param numTrees
	 *            how many trees there should be
	 * @return the newly generated database of trees
	 */
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

}
