package treeminer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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

		EquivalenceClass f1 = findFrequentF1Subtrees(trees, minSupport);
		foundEquivalenceClasses.add(f1);

		List<EquivalenceClass> f2Classes = findFrequentF2Subtrees(f1, trees);
		foundEquivalenceClasses.addAll(f2Classes);

		f2Classes.forEach(elem -> findFrequentSubtrees(elem, trees));

		TreeSet<String> foundFrequentTrees = new TreeSet<>();
		foundEquivalenceClasses
				.forEach(foundClass -> foundFrequentTrees.addAll(extractNonEmbeddedFrequentTrees(foundClass, trees)));
		numFoundPatterns = foundFrequentTrees.size();

		return new ArrayList<>(foundFrequentTrees);
	}

	/**
	 * Finds the initial equivalence class f1 that has an empty prefix and contains
	 * all single nodes with a frequency of at least the minimum support. Does not
	 * find the scope of nodes, only the elements of the equivalence class.
	 * 
	 * @return The generated EuivalenceClass
	 */
	protected EquivalenceClass findFrequentF1Subtrees(List<String> trees, int minSupport) {
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
	 * Finds the equivalence classes in f2 derived from the initial equivalence
	 * class f1. Also finds the scopes of the elements in f1.
	 * 
	 * @param f1
	 *            The initial equivalence class f1
	 * @return The list of equivalence classes derived from f1 (f2)
	 */
	protected List<EquivalenceClass> findFrequentF2Subtrees(EquivalenceClass f1, List<String> trees) {
		// Generate candidate scope lists for the candidate equivalence classes
		TreeMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> mapF2PatternToOccurence = new TreeMap<>();
		TreeMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> mapF1PatternToOccurence = new TreeMap<>();
		f1.setScopeLists(mapF1PatternToOccurence);
		generateCandidateScopeListsF1F2(f1, mapF2PatternToOccurence, mapF1PatternToOccurence);

		// Find candidate scope list frequencies
		for (int i = 0; i < trees.size(); i++) {
			findPatternsInTree(trees.get(i), mapF2PatternToOccurence, mapF1PatternToOccurence, i);
		}

		// Assemble scope lists for f2
		List<EquivalenceClass> candidateEquivalenceClasses = generateCandidateEquivalenceClassesF2(f1);
		return filterF2CandidateClassesByPatternOccurrences(candidateEquivalenceClasses, mapF2PatternToOccurence);
	}

	private void generateCandidateScopeListsF1F2(EquivalenceClass f1,
			TreeMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> f2ScopeLists,
			TreeMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> f1ScopeLists) {
		//Fill the scope lists with candidates
		f1.getElementList().forEach(pairX -> {
			if (countMultipleOccurrences) {
				f1ScopeLists.put(pairX.getLeft(), new ScopeListRepresentation());
			} else {
				f2ScopeLists.put(pairX.getLeft(), new ScopeVectorListRepresentation());
			}
			f1.getElementList().forEach(pairY -> {
				String pattern = String.format("%s%s%s%s%s", pairX.getLeft(),
						TreeRepresentationUtils.TREE_NODE_SEPARATOR, pairY.getLeft(),
						TreeRepresentationUtils.TREE_NODE_SEPARATOR, TreeRepresentationUtils.MOVE_UP_TOKEN);
				ScopeListRepresentation occurrences = new ScopeListRepresentation();
				f2ScopeLists.put(pattern, occurrences);
			});
		});
	}

	private void findPatternsInTree(String tree,
			TreeMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> mapF2PatternToOccurence,
			TreeMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> mapF1PatternToOccurence,
			int i) {
		String[] treeRepresentation = tree.split(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
	
		// Find the scope of each node in the tree
		Scope[] nodeScopes = new Scope[(int) Math.ceil(treeRepresentation.length / 2.0)];
		
		if (countMultipleOccurrences) {
			String[] matchLabels =  new String[(int) Math.ceil(treeRepresentation.length / 2.0)];
			findNodeScopes(treeRepresentation, nodeScopes, matchLabels);
			
			// Find frequency of candidate elements in f1 and f2
			findCandidateFrequencies((TreeMap<String, AScopeListRepresentation<ScopeListElement>>)mapF2PatternToOccurence, mapF1PatternToOccurence, i, treeRepresentation, nodeScopes,
					matchLabels);
		} else {
			findNodeScopesNoMatchLabel(treeRepresentation, nodeScopes);
			findCandidateFrequenciesNoMatchLabel(mapF2PatternToOccurence, mapF1PatternToOccurence, i, treeRepresentation, nodeScopes);
		}
	}
	
	private void findNodeScopes(String[] treeRepresentation, Scope[] nodeScopes, String[] matchLabels) {
		for (int j = 0; j < nodeScopes.length; j++) {
			nodeScopes[j] = new Scope();
		}
		int atNode = -1;
		List<Integer> openScopes = new ArrayList<>();
		StringBuilder matchLabelBuilder = new StringBuilder();
		for (String treeElement : treeRepresentation) {
			if (!treeElement.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
				// Start the scope of the current node
				atNode++;
				nodeScopes[atNode].setLowerBound(atNode);
				openScopes.add(atNode);
				matchLabels[atNode] = matchLabelBuilder.toString().trim();
			} else {
				// End the scope of the most recently not closed scopes
				int closeScopeIndex = openScopes.get(openScopes.size() - 1);
				nodeScopes[closeScopeIndex].setUpperBound(atNode);
				openScopes.remove(openScopes.get(openScopes.size() - 1));
			}
			matchLabelBuilder.append(treeElement);
			matchLabelBuilder.append(TreeRepresentationUtils.TREE_NODE_SEPARATOR);
		}
		// First node has to be closed separately because it doesn't have a moveUpToken
		nodeScopes[0].setUpperBound(nodeScopes.length - 1);
	}

	private void findNodeScopesNoMatchLabel(String [] treeRepresentation, Scope[] nodeScopes) {
		for (int j = 0; j < nodeScopes.length; j++) {
			nodeScopes[j] = new Scope();
		}
		int atNode = -1;
		List<Integer> openScopes = new ArrayList<>();
		for (String treeElement : treeRepresentation) {
			if (!treeElement.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
				// Start the scope of the current node
				atNode++;
				nodeScopes[atNode].setLowerBound(atNode);
				openScopes.add(atNode);
			} else {
				// End the scope of the most recently not closed scopes
				int closeScopeIndex = openScopes.get(openScopes.size() - 1);
				nodeScopes[closeScopeIndex].setUpperBound(atNode);
				openScopes.remove(openScopes.get(openScopes.size() - 1));
			}
		}
		// First node has to be closed separately because it doesn't have a moveUpToken
		nodeScopes[0].setUpperBound(nodeScopes.length - 1);
	}

	private void findCandidateFrequencies(
			TreeMap<String, AScopeListRepresentation<ScopeListElement>> mapF2PatternToOccurence,
			TreeMap<String, AScopeListRepresentation<ScopeListElement>> mapF1PatternToOccurence, int i,
			String[] treeRepresentation, Scope[] nodeScopes, String[] matchLabels) {
		int atNode = -1;
		for (int j = 0; j < treeRepresentation.length; j++) {
			String treeElement = treeRepresentation[j];
			if (!treeElement.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
				atNode++;
				addNewPattern(mapF1PatternToOccurence, i, nodeScopes, matchLabels, atNode, treeElement);
	
				checkForDoublePattern(mapF2PatternToOccurence, i, treeRepresentation, nodeScopes, matchLabels, atNode,
						j, treeElement);
			}
		}
	}

	private void addNewPattern(
			TreeMap<String, AScopeListRepresentation<ScopeListElement>> mapF1PatternToOccurence, int i,
			Scope[] nodeScopes, String[] matchLabels, int atNode, String treeElement) {
		// Add the found single pattern
		ScopeListElement entry = new ScopeListElement(i, matchLabels[atNode], nodeScopes[atNode]);
		if (mapF1PatternToOccurence.get(treeElement) != null) {
			mapF1PatternToOccurence.get(treeElement).add(entry);
		}
	}

	private void checkForDoublePattern(
			TreeMap<String, AScopeListRepresentation<ScopeListElement>> mapF2PatternToOccurence, int i,
			String[] treeRepresentation, Scope[] nodeScopes, String[] matchLabels, int atNode, int j,
			String treeElement) {
		// Check for double pattern (find direct and indirect children of a node)
		int childLevel = 0;
		int childNumber = 0;
		for (int k = j + 1; k < treeRepresentation.length; k++) {
			String potentialChild = treeRepresentation[k];
			if (!potentialChild.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
				childLevel++;
				childNumber++;
	
				ScopeListElement f2entry = new ScopeListElement(i, matchLabels[atNode],
						nodeScopes[atNode + childNumber]);
				AScopeListRepresentation<ScopeListElement> list = mapF2PatternToOccurence
						.get(String.format("%s%s%s%s%s", treeElement, TreeRepresentationUtils.TREE_NODE_SEPARATOR,
								treeRepresentation[k], TreeRepresentationUtils.TREE_NODE_SEPARATOR,
								TreeRepresentationUtils.MOVE_UP_TOKEN));
				if (list != null) {
					list.add(f2entry);
				}
	
			} else {
				childLevel--;
				if (childLevel == -1) {
					break;
				}
			}
		}
	}
	
	private void findCandidateFrequenciesNoMatchLabel(
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF2PatternToOccurence,
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF1PatternToOccurence, int i,
			String[] treeRepresentation, Scope[] nodeScopes) {
		int atNode = -1;
		for (int j = 0; j < treeRepresentation.length; j++) {
			String treeElement = treeRepresentation[j];
			if (!treeElement.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
				atNode++;
				addNewPatternNoMatchLabel(mapF1PatternToOccurence, i, nodeScopes, atNode, treeElement);
	
				checkForDoublePatternNoMatchLabel(mapF2PatternToOccurence, i, treeRepresentation, nodeScopes, atNode,
						j, treeElement);
			}
		}
	}
	
	private void addNewPatternNoMatchLabel(
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF1PatternToOccurence, int i,
			Scope[] nodeScopes, int atNode, String treeElement) {
		// Add the found single pattern
		ScopeVectorListElement entry = new ScopeVectorListElement(i, matchLabels[atNode], nodeScopes[atNode]);
		if (mapF1PatternToOccurence.get(treeElement) != null) {
			mapF1PatternToOccurence.get(treeElement).add(entry);
		}
	}
	
	private void checkForDoublePatternNoMatchLabel(TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF2PatternToOccurence, int i,
			String[] treeRepresentation, Scope[] nodeScopes, int atNode, int j,
			String treeElement) {
		// Check for double pattern (find direct and indirect children of a node)
		int childLevel = 0;
		int childNumber = 0;
		for (int k = j + 1; k < treeRepresentation.length; k++) {
			String potentialChild = treeRepresentation[k];
			if (!potentialChild.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
				childLevel++;
				childNumber++;
	
				ScopeVectorListElement f2entry = new ScopeVectorListElement(i, matchLabels[atNode],
						nodeScopes[atNode + childNumber]);
				AScopeListRepresentation<ScopeVectorListElement> list = mapF2PatternToOccurence
						.get(String.format("%s%s%s%s%s", treeElement, TreeRepresentationUtils.TREE_NODE_SEPARATOR,
								treeRepresentation[k], TreeRepresentationUtils.TREE_NODE_SEPARATOR,
								TreeRepresentationUtils.MOVE_UP_TOKEN));
				if (list != null) {
					list.add(f2entry);
				}
	
			} else {
				childLevel--;
				if (childLevel == -1) {
					break;
				}
			}
		}
	}

	private List<EquivalenceClass> generateCandidateEquivalenceClassesF2(EquivalenceClass f1) {
		// Generate possible candidate equivalence classes with their elements
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

	private List<EquivalenceClass> filterF2CandidateClassesByPatternOccurrences(List<EquivalenceClass> candidateEquivalenceClasses,
			TreeMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> mapF2PatternToOccurence) {
		List<EquivalenceClass> newEquivalenceClasses = new ArrayList<>();
		candidateEquivalenceClasses.forEach(equivalenceClass -> {
			SortedMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> scopeLists = new TreeMap<>();
			equivalenceClass.getElementList().forEach(element -> {
				String label = TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), element);
				AScopeListRepresentation<? extends SimpleScopeListElement> scopeList = mapF2PatternToOccurence
						.get(label);
				scopeLists.put(label, scopeList);
			});
			equivalenceClass.setScopeLists(scopeLists);
			equivalenceClass.discardNonFrequentElements(minSupport);
			if (!equivalenceClass.getElementList().isEmpty()) {
				newEquivalenceClasses.add(equivalenceClass);
			}
		});
		return newEquivalenceClasses;
	}

	/**
	 * Finds all equivalence classes derived from the given equivalence class and
	 * adds them to the list of found equivalence classes.
	 * 
	 * @param equivalenceClass
	 *            The equivalence class from which the new classes are derived
	 */
	protected void findFrequentSubtrees(EquivalenceClass equivalenceClass, List<String> trees) {
		// For (x, i) element P
		for (Pair<String, Integer> XIelement : equivalenceClass.getElementList()) {
			String newPrefix = TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), XIelement);
			if (!prefixOccursDirectly(equivalenceClass, trees, newPrefix)) {
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

	private boolean prefixOccursDirectly(EquivalenceClass equivalenceClass, List<String> trees, String newPrefix) {
		boolean occurrsDirectly = false;
		for (SimpleScopeListElement scopeListElement : equivalenceClass.getScopeListFor(newPrefix)) {
			if (TreeRepresentationUtils.containsSubtree(newPrefix, trees.get(scopeListElement.getTreeIndex()))) {
				occurrsDirectly = true;
			}
		}
		return occurrsDirectly;
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

		AScopeListRepresentation<? extends SimpleScopeListElement> newScopeList = ScopeListRepresentationUtils.doInScopeJoin(xScopeList, yScopeList, countMultipleOccurrences);

		if (newScopeList.size() >= minSupport) {
			int numberOfChildrenOfParentNode = TreeRepresentationUtils
					.findNumberOfChildrenOfNode(equivalenceClass.getPrefix(), xIElement.getRight());
			int newXPosition = xIElement.getRight() + 1 + numberOfChildrenOfParentNode;
			Pair<String, Integer> newElement = new ImmutablePair<>(yJElement.getLeft(), newXPosition);
			pXi.addElement(newElement);
			pXi.addScopeListFor(TreeRepresentationUtils.addNodeToTree(pXi.getPrefix(), newElement), newScopeList);
		}

		// Test (y, j)
		newScopeList = ScopeListRepresentationUtils.doOutScopeJoin(yJElement, xScopeList, yScopeList, countMultipleOccurrences);

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

		AScopeListRepresentation<? extends SimpleScopeListElement> newScopeList = ScopeListRepresentationUtils.doOutScopeJoin(yJElement, xScopeList,
				yScopeList, countMultipleOccurrences);

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
		// TODO remove this method, shouldn't be necessary with the new improvements
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
}
