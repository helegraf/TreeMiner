package treeminer.initialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import treeminer.EquivalenceClass;
import treeminer.util.TreeRepresentationUtils;

/**
 * Provides general initialization functionality for the TreeMiner, depending on
 * its configuration.
 * 
 * @author Helena Graf
 *
 */
public class TreeMinerGeneralInitializer {

	private TreeMinerGeneralInitializer() {
	}

	/**
	 * Finds the initial equivalence class f1 that has an empty prefix and contains
	 * all single nodes with a frequency of at least the minimum support. Does not
	 * find the scope of nodes, only the elements of the equivalence class.
	 * 
	 * @param trees
	 *            the trees in the database for which to find the frequent subtrees
	 * @param minSupport
	 *            the absolute minimum support for a tree to be considered frequent
	 * @return the generated EuivalenceClass, minus the node scopes
	 */
	public static EquivalenceClass findFrequentF1Subtrees(List<String> trees, int minSupport) {
		HashMap<String, Integer> labelFrequencies = new HashMap<>();

		// For each tree
		for (String tree : trees) {
			// Break up tree in its labels
			String[] labels = tree.split(TreeRepresentationUtils.TREE_NODE_SEPARATOR);

			// For each label, increase the frequency if found
			for (String label : labels) {
				if (!label.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
					if (labelFrequencies.containsKey(label)) {
						labelFrequencies.put(label, labelFrequencies.get(label) + 1);
					} else {
						labelFrequencies.put(label, 1);
					}
				}
			}
		}

		// Check which elements have at least the minimal support
		TreeSet<Pair<String, Integer>> elementList = new TreeSet<>();
		labelFrequencies.forEach((label, frequency) -> {
			if (frequency >= minSupport) {
				elementList.add(new ImmutablePair<String, Integer>(label, -1));
			}
		});

		// Create equivalence class with empty prefix
		return new EquivalenceClass("", new ArrayList<Pair<String, Integer>>(elementList));
	}

	/**
	 * Finds the equivalence classes in F2 derived from the initial equivalence
	 * class F1. Also finds the scopes of the elements in F1.
	 * 
	 * @param f1
	 *            the initial equivalence class f1
	 * @param trees
	 *            the trees in the database for which to find the frequent subtrees
	 * @param countMultipleOccurrences
	 *            whether multiple occurrences of a pattern within a tree shall be
	 *            counted
	 * @param minSupport
	 *            the minimum support for a pattern to be considered frequent
	 * @return the list of equivalence classes derived from F1 (F2)
	 */
	public static List<EquivalenceClass> findFrequentF2Subtrees(EquivalenceClass f1, List<String> trees,
			boolean countMultipleOccurrences, int minSupport) {

		if (countMultipleOccurrences) {
			return TreeMinerNonDistinctInitializer.initialize(f1, trees, minSupport);
		} else {
			return TreeMinerDistinctInitializer.initialize(f1, trees, minSupport);
		}
	}

	/**
	 * Generate all possible equivalence classes with a one-node prefix from the
	 * equivalence class F1 that has an empty prefix.
	 * 
	 * @param f1
	 *            the initial equivalence class with an empty prefix
	 * @return candidate equivalence classes F2
	 */
	public static List<EquivalenceClass> generateCandidateEquivalenceClassesF2(EquivalenceClass f1) {
		List<EquivalenceClass> candidateEquivalenceClasses = new ArrayList<>();
		List<Pair<String, Integer>> f1Elements = f1.getElementList();
		for (int i = 0; i < f1Elements.size(); i++) {
			List<Pair<String, Integer>> elementList = new ArrayList<>();
			for (int j = 0; j < f1Elements.size(); j++) {
				elementList.add(new ImmutablePair<String, Integer>(f1Elements.get(j).getLeft(), 0));
			}
			candidateEquivalenceClasses.add(new EquivalenceClass(f1Elements.get(i).getLeft(), elementList));
		}
		return candidateEquivalenceClasses;
	}

}
