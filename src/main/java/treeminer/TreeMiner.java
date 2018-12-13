package treeminer;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import treeminer.initialization.TreeMinerGeneralInitializer;
import treeminer.scopelists.elements.SimpleScopeListElement;
import treeminer.scopelists.representation.AScopeListRepresentation;
import treeminer.util.ScopeListRepresentationUtils;
import treeminer.util.TreeRepresentationUtils;

/**
 * This class can be used to finds subtrees in a tree or forest with an
 * implementation that is based on the TreeMiner algorithm (Mohammed Javeed
 * Zaki: Efficiently Mining Frequent Trees in a Forest: Algorithms and
 * Applications. IEEE Trans. Knowl. Data Eng. 17(8): 1021-1035 (2005)), except
 * that it does not return embedded subtrees.
 * 
 * @author Helena Graf
 *
 */
public class TreeMiner implements FrequentSubtreeFinder {

	private int minSupport;
	private int numTrees;
	private List<EquivalenceClass> foundEquivalenceClasses;
	private int numFoundPatterns;
	private boolean countMultipleOccurrences = true;

	@Override
	public List<String> findFrequentSubtrees(List<String> trees, int minSupport) {
		this.minSupport = minSupport;
		this.numTrees = trees.size();
		this.foundEquivalenceClasses = new ArrayList<>();

		EquivalenceClass f1 = TreeMinerGeneralInitializer.findFrequentF1Subtrees(trees, minSupport);
		foundEquivalenceClasses.add(f1);

		List<EquivalenceClass> f2Classes = TreeMinerGeneralInitializer.findFrequentF2Subtrees(f1, trees,
				countMultipleOccurrences, minSupport);
		foundEquivalenceClasses.addAll(f2Classes);

		f2Classes.forEach(elem -> findFrequentSubtrees(elem, trees));

		TreeSet<String> foundFrequentTrees = new TreeSet<>();
		foundEquivalenceClasses
				.forEach(foundClass -> foundFrequentTrees.addAll(extractNonEmbeddedFrequentTrees(foundClass, trees)));
		numFoundPatterns = foundFrequentTrees.size();

		return new ArrayList<>(foundFrequentTrees);
	}

	private void findFrequentSubtrees(EquivalenceClass equivalenceClass, List<String> trees) {
		// For (x, i) element P
		for (Pair<String, Integer> XIelement : equivalenceClass.getElementList()) {
			String newPrefix = TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), XIelement);
			if (!ScopeListRepresentationUtils.prefixOccursDirectly(equivalenceClass, trees, newPrefix)) {
				continue;
			}

			EquivalenceClass pXi = new EquivalenceClass(newPrefix);
			findMembersOfEquivalenceClass(equivalenceClass, XIelement, pXi);

			if (!pXi.getElementList().isEmpty()) {
				foundEquivalenceClasses.add(pXi);
				findFrequentSubtrees(pXi, trees);
			}
		}
	}

	private void findMembersOfEquivalenceClass(EquivalenceClass equivalenceClass, Pair<String, Integer> xIElement,
			EquivalenceClass pXi) {
		// For (y, j) element P
		for (Pair<String, Integer> YJElement : equivalenceClass.getElementList()) {
			// i = j case
			if (xIElement.getRight() == YJElement.getRight()) {
				checkCase1(equivalenceClass, xIElement, pXi, YJElement);
				// i > j case
			} else if (xIElement.getRight() > YJElement.getRight()) {
				checkCase2(equivalenceClass, xIElement, pXi, YJElement);
				// i < j case - nothing more can be added so we skip it
			}
		}
	}

	private void checkCase1(EquivalenceClass equivalenceClass, Pair<String, Integer> xIElement, EquivalenceClass pXi,
			Pair<String, Integer> yJElement) {
		// Test (y, n_i)
		AScopeListRepresentation<? extends SimpleScopeListElement> xScopeList = equivalenceClass
				.getScopeListFor(TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), xIElement));
		AScopeListRepresentation<? extends SimpleScopeListElement> yScopeList = equivalenceClass
				.getScopeListFor(TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), yJElement));
		if (xScopeList == null) {
			return;
		}

		AScopeListRepresentation<? extends SimpleScopeListElement> newScopeList = ScopeListRepresentationUtils
				.doInScopeJoin(xScopeList, yScopeList, countMultipleOccurrences);

		if (newScopeList.size() >= minSupport) {
			int numberOfChildrenOfParentNode = TreeRepresentationUtils
					.findNumberOfChildrenOfNode(equivalenceClass.getPrefix(), xIElement.getRight());
			int newXPosition = xIElement.getRight() + 1 + numberOfChildrenOfParentNode;
			Pair<String, Integer> newElement = new ImmutablePair<>(yJElement.getLeft(), newXPosition);
			pXi.addElement(newElement);
			pXi.addScopeListFor(TreeRepresentationUtils.addNodeToTree(pXi.getPrefix(), newElement), newScopeList);
		}

		// Test (y, j)
		newScopeList = ScopeListRepresentationUtils.doOutScopeJoin(yJElement, xScopeList, yScopeList,
				countMultipleOccurrences);

		if (newScopeList.size() >= minSupport) {
			pXi.addElement(yJElement);
			pXi.addScopeListFor(TreeRepresentationUtils.addNodeToTree(pXi.getPrefix(), yJElement), newScopeList);
		}
	}

	private void checkCase2(EquivalenceClass equivalenceClass, Pair<String, Integer> xIElement, EquivalenceClass pXi,
			Pair<String, Integer> yJElement) {
		// Test (y, j)
		AScopeListRepresentation<? extends SimpleScopeListElement> xScopeList = equivalenceClass
				.getScopeListFor(TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), xIElement));
		AScopeListRepresentation<? extends SimpleScopeListElement> yScopeList = equivalenceClass
				.getScopeListFor(TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), yJElement));
		if (xScopeList == null) {
			return;
		}

		AScopeListRepresentation<? extends SimpleScopeListElement> newScopeList = ScopeListRepresentationUtils
				.doOutScopeJoin(yJElement, xScopeList, yScopeList, countMultipleOccurrences);

		if (newScopeList.size() >= minSupport) {
			pXi.addElement(yJElement);
			pXi.addScopeListFor(TreeRepresentationUtils.addNodeToTree(pXi.getPrefix(), yJElement), newScopeList);
		}
	}

	/**
	 * Extracts all the non-embedded frequent subtrees from the given equivalence
	 * class and. Also removes the non-frequent elements from the equivalence
	 * classes.
	 * 
	 * @param equivalenceClass
	 *            The equivalence class containing the found frequent (embedded)
	 *            subtrees
	 * @return The found non-embedded frequent subtrees
	 */
	protected TreeSet<String> extractNonEmbeddedFrequentTrees(EquivalenceClass equivalenceClass, List<String> trees) {
		TreeSet<String> foundTrees = new TreeSet<>();
		SortedMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> newScopeLists = new TreeMap<>();
		List<Pair<String, Integer>> newElementList = new ArrayList<>();

		for (int i = 0; i < equivalenceClass.getElementList().size(); i++) {
			String subTree = TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(),
					equivalenceClass.getElementList().get(i));
			AScopeListRepresentation<? extends SimpleScopeListElement> scopeList = equivalenceClass
					.getScopeListFor(subTree);

			// for each scope list element of a subtree, check if it actually appears in
			// that tree or is just embedded
			int support = 0;
			for (SimpleScopeListElement scopeListElem : scopeList) {
				if (TreeRepresentationUtils.containsSubtree(trees.get(scopeListElem.getTreeIndex()), subTree)) {
					support++;
				}
			}

			if (support >= minSupport) {
				foundTrees.add(subTree);
				newScopeLists.put(subTree, scopeList);
				newElementList.add(equivalenceClass.getElementList().get(i));

			}
		}

		equivalenceClass.setScopeLists(newScopeLists);
		equivalenceClass.setElementList(newElementList);
		return foundTrees;
	}

	@Override
	public List<EquivalenceClass> getFoundEquivalenceClasses() {
		return foundEquivalenceClasses;
	}

	@Override
	public double[][] getCharacterizationsOfTrainingExamples() {
		double[][] treesWithPatternOccurrences = new double[numTrees][numFoundPatterns];

		int currentPattern = 0;
		for (EquivalenceClass equivalenceClass : foundEquivalenceClasses) {
			for (String pattern : equivalenceClass.getScopeLists().keySet()) {
				for (SimpleScopeListElement patternOccurence : equivalenceClass.getScopeListFor(pattern)) {
					treesWithPatternOccurrences[patternOccurence.getTreeIndex()][currentPattern] = 1;
				}
				currentPattern++;
			}
		}

		return treesWithPatternOccurrences;
	}

	/**
	 * Set whether multiple occurrences of a pattern within a tree shall be counted
	 * separately. WARNING: enabling this option can increase memory usage
	 * drastically!
	 * 
	 * @param countMultipleOccurrences
	 *            whether multiple occurrences shall be counted
	 */
	public void setCountMultipleOccurrences(boolean countMultipleOccurrences) {
		this.countMultipleOccurrences = countMultipleOccurrences;
	}
}
