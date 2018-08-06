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

	/**
	 * Get the equivalence classes found during the last call of
	 * {@link #findFrequentSubtrees(List, int)}.
	 * 
	 * @return The found equivalence classes
	 */
	public List<EquivalenceClass> getFoundEquivalenceClasses();

	/**
	 * Get which of the training example trees contain which pattern. Returns a
	 * matrix m where m[i][j]=1 indicates that pattern j appears in tree i,
	 * otherwise m[i][j]=0.
	 * 
	 * @return A matrix indicating pattern occurrences
	 */
	double[][] getCharacterizationsOfTrainingExamples();
}
