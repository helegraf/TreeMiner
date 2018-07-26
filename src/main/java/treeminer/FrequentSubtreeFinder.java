package treeminer;

import java.util.List;

/**
 * A FrequentSubtreeFinder is an algorithm that can be used to find subtrees in
 * a given tree or forest.
 * 
 * <p>
 * Trees are represented as Strings.
 * </p>
 * 
 * @author Helena Graf
 *
 */
public interface FrequentSubtreeFinder {

	/**
	 * Checks whether the given subtree occurs in the given tree.
	 * 
	 * @param tree
	 *            The tree to check for the occurrence of a subtree
	 * @param subtree
	 *            The subtree which is searched for in the given tree
	 * @return Whether the given subtree occurrs in the given tree at least once
	 */
	public boolean containsSubtree(String tree, String subtree);

	/**
	 * Finds all frequent subtrees in the given forest.
	 * 
	 * <p>
	 * A tree is frequent if it appears in at least minSupport many trees.
	 * </p>
	 * 
	 * @param trees
	 *            The given forest which is searched for frequent subtrees
	 * @param minSupport
	 *            The minimum support a subtree must have to be considered frequent
	 * @return The found frequent subtrees
	 */
	public List<String> findFrequentSubtrees(List<String> trees, int minSupport);
}
