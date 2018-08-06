package treeminer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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

	@Override
	public List<String> findFrequentSubtrees(List<String> trees, int minSupport) {
		this.minSupport = minSupport;
		this.numTrees = trees.size();
		this.foundEquivalenceClasses = new ArrayList<EquivalenceClass>();

		EquivalenceClass f1 = findFrequentF1Subtrees(trees);
		foundEquivalenceClasses.add(f1);

		List<EquivalenceClass> f2Classes = findFrequentF2Subtrees(f1, trees);
		foundEquivalenceClasses.addAll(f2Classes);

		f2Classes.forEach(f2class -> {
			findFrequentSubtrees(f2class);
		});

		TreeSet<String> foundFrequentTrees = new TreeSet<String>();
		foundEquivalenceClasses.forEach(foundClass -> {
			foundFrequentTrees.addAll(extractNonEmbeddedFrequentTrees(foundClass, trees));
		});
		numFoundPatterns = foundFrequentTrees.size();

		return new ArrayList<String>(foundFrequentTrees);
	}

	/**
	 * Finds the initial equivalence class f1 that has an empty prefix and contains
	 * all single nodes with a frequency of at least the minimum support. Does not
	 * find the scope of nodes, only the elements of the equivalence class.
	 * 
	 * @return The generated EuivalenceClass
	 */
	protected EquivalenceClass findFrequentF1Subtrees(List<String> trees) {
		HashMap<String, Integer> labelFrequencies = new HashMap<String, Integer>();

		// For each tree
		for (String tree : trees) {
			// Break up tree in its labels
			String[] labels = tree.split(TreeRepresentationUtils.treeNodeSeparator);

			// For each label, increase the frequency if found
			for (String label : labels) {
				if (!label.equals(TreeRepresentationUtils.moveUpToken)) {
					if (labelFrequencies.containsKey(label)) {
						labelFrequencies.put(label, labelFrequencies.get(label) + 1);
					} else {
						labelFrequencies.put(label, 1);
					}
				}
			}
		}

		// Check which elements have at least the minimal support
		TreeSet<Pair<String, Integer>> elementList = new TreeSet<Pair<String, Integer>>();
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
		// Generate possible candidate equivalence classes with their elements
		List<EquivalenceClass> candidateEquivalenceClasses = new ArrayList<EquivalenceClass>();
		List<Pair<String, Integer>> f1Elements = f1.getElementList();
		for (int i = 0; i < f1Elements.size(); i++) {
			List<Pair<String, Integer>> elementList = new ArrayList<Pair<String, Integer>>();
			for (int j = 0; j < f1Elements.size(); j++) {
				elementList.add(new ImmutablePair<String, Integer>(f1Elements.get(j).getLeft(), 0));
			}
			candidateEquivalenceClasses.add(new EquivalenceClass(f1Elements.get(i).getLeft(), elementList));
		}

		// Generate candidate scope lists for the candidate equivalence classes
		TreeMap<String, ScopeListRepresentation> mapF2PatternToOccurence = new TreeMap<String, ScopeListRepresentation>();
		TreeMap<String, ScopeListRepresentation> mapF1PatternToOccurence = new TreeMap<String, ScopeListRepresentation>();
		f1.getElementList().forEach(pairX -> {
			mapF1PatternToOccurence.put(pairX.getLeft(), new ScopeListRepresentation());
			f1.getElementList().forEach(pairY -> {
				String pattern = pairX.getLeft() + TreeRepresentationUtils.treeNodeSeparator + pairY.getLeft()
						+ TreeRepresentationUtils.treeNodeSeparator + TreeRepresentationUtils.moveUpToken;
				ScopeListRepresentation occurrences = new ScopeListRepresentation();
				mapF2PatternToOccurence.put(pattern, occurrences);
			});
		});

		// Find candidate scope list frequencies
		// For each tree, iterate over it twice to find scopes of nodes first
		for (int i = 0; i < trees.size(); i++) {
			String tree = trees.get(i);
			String[] treeRepresentation = tree.split(TreeRepresentationUtils.treeNodeSeparator);

			// Find the scope of each node in the tree
			Scope[] nodeScopes = new Scope[(int) Math.ceil(treeRepresentation.length / 2.0)];
			String[] matchLabels = new String[(int) Math.ceil(treeRepresentation.length / 2.0)];
			for (int j = 0; j < nodeScopes.length; j++) {
				nodeScopes[j] = new Scope();
			}
			int atNode = -1;
			List<Integer> openScopes = new ArrayList<Integer>();
			StringBuilder matchLabelBuilder = new StringBuilder();
			for (String treeElement : treeRepresentation) {
				if (!treeElement.equals(TreeRepresentationUtils.moveUpToken)) {
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
				matchLabelBuilder.append(treeElement + TreeRepresentationUtils.treeNodeSeparator);
			}
			// First node has to be closed separately because it doesn't have a moveUpToken
			nodeScopes[0].setUpperBound(nodeScopes.length - 1);

			// Find frequency of candidate elements in f1 and f2
			atNode = -1;
			for (int j = 0; j < treeRepresentation.length; j++) {
				String treeElement = treeRepresentation[j];
				if (!treeElement.equals(TreeRepresentationUtils.moveUpToken)) {
					atNode++;
					// Add the found single pattern
					Triple<Integer, String, Scope> entry = new ImmutableTriple<Integer, String, Scope>(i,
							matchLabels[atNode], nodeScopes[atNode]);
					if (mapF1PatternToOccurence.get(treeElement) != null) {
						mapF1PatternToOccurence.get(treeElement).add(entry);
					}

					// Check for double pattern (find direct and indirect children of a node)
					int childLevel = 0;
					int childNumber = 0;
					for (int k = j + 1; k < treeRepresentation.length; k++) {
						String potentialChild = treeRepresentation[k];
						if (!potentialChild.equals(TreeRepresentationUtils.moveUpToken)) {
							childLevel++;
							childNumber++;

							Triple<Integer, String, Scope> f2entry = new ImmutableTriple<Integer, String, Scope>(i,
									matchLabels[atNode], nodeScopes[atNode + childNumber]);
							if (mapF2PatternToOccurence.get(treeElement + TreeRepresentationUtils.treeNodeSeparator
									+ treeRepresentation[k] + TreeRepresentationUtils.treeNodeSeparator
									+ TreeRepresentationUtils.moveUpToken) != null) {
								mapF2PatternToOccurence.get(treeElement + TreeRepresentationUtils.treeNodeSeparator
										+ treeRepresentation[k] + TreeRepresentationUtils.treeNodeSeparator
										+ TreeRepresentationUtils.moveUpToken).add(f2entry);
							}

						} else {
							childLevel--;
							if (childLevel == -1) {
								break;
							}
						}
					}
				}
			}

		}

		// Set the found scope lists for f1
		f1.setScopeLists(mapF1PatternToOccurence);

		// Assemble scope lists for f2
		List<EquivalenceClass> newEquivalenceClasses = new ArrayList<EquivalenceClass>();
		candidateEquivalenceClasses.forEach(equivalenceClass -> {
			TreeMap<String, ScopeListRepresentation> scopeLists = new TreeMap<String, ScopeListRepresentation>();
			equivalenceClass.getElementList().forEach(element -> {
				String label = TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), element);
				ScopeListRepresentation scopeList = mapF2PatternToOccurence.get(label);
				scopeLists.put(label, scopeList);
			});
			equivalenceClass.setScopeLists(scopeLists);
			equivalenceClass.discardNonFrequentElements(minSupport);
			if (equivalenceClass.getElementList().size() > 0) {
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
	protected void findFrequentSubtrees(EquivalenceClass equivalenceClass) {
		// For (x, i) element P
		for (Pair<String, Integer> XIelement : equivalenceClass.getElementList()) {
			// Create a new empty equivalence class as a result of appending x to node i to
			// the prefix in its equivalence class
			String newPrefix = TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), XIelement);
			EquivalenceClass P_xi = new EquivalenceClass(newPrefix);

			// For (y, j) element P
			for (Pair<String, Integer> YJElement : equivalenceClass.getElementList()) {
				// i = j case
				if (XIelement.getRight() == YJElement.getRight()) {
					// Test for (y, n_i)
					ScopeListRepresentation xScopeList = equivalenceClass.getScopeListFor(
							TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), XIelement));
					ScopeListRepresentation yScopeList = equivalenceClass.getScopeListFor(
							TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), YJElement));
					if (xScopeList == null) {
						continue;
					}

					ScopeListRepresentation newScopeList = xScopeList.inScopeJoin(yScopeList);

					if (newScopeList.size() >= minSupport) {
						int numberOfChildrenOfParentNode = TreeRepresentationUtils
								.findNumberOfChildrenOfNode(equivalenceClass.getPrefix(), XIelement.getRight());
						int newXPosition = XIelement.getRight() + 1 + numberOfChildrenOfParentNode;
						Pair<String, Integer> newElement = new ImmutablePair<String, Integer>(YJElement.getLeft(),
								newXPosition);
						P_xi.addElement(newElement);
						P_xi.addScopeListFor(TreeRepresentationUtils.addNodeToTree(P_xi.getPrefix(), newElement),
								newScopeList);
					}

					// Test for (y, j)
					newScopeList = xScopeList.outScopeJoin(yScopeList);

					if (newScopeList.size() >= minSupport) {
						P_xi.addElement(YJElement);
						P_xi.addScopeListFor(TreeRepresentationUtils.addNodeToTree(P_xi.getPrefix(), YJElement),
								newScopeList);
					}
					// i > j case
				} else if (XIelement.getRight() > YJElement.getRight()) {
					// Test for (y, j)
					ScopeListRepresentation xScopeList = equivalenceClass.getScopeListFor(
							TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), XIelement));
					ScopeListRepresentation yScopeList = equivalenceClass.getScopeListFor(
							TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(), YJElement));
					if (xScopeList == null) {
						continue;
					}

					ScopeListRepresentation newScopeList = xScopeList.outScopeJoin(yScopeList);

					if (newScopeList.size() >= minSupport) {
						P_xi.addElement(YJElement);
						P_xi.addScopeListFor(TreeRepresentationUtils.addNodeToTree(P_xi.getPrefix(), YJElement),
								newScopeList);
					}
					// i < j case
				} else {
					// if i < j, no new tree can be added
					continue;
				}
			}

			// If the found class is not empty
			if (P_xi.getElementList().size() > 0) {
				// Add the found class to all found classes
				foundEquivalenceClasses.add(P_xi);

				// Find the frequent subtrees for the new equivalence class
				findFrequentSubtrees(P_xi);
			}
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
		TreeSet<String> foundTrees = new TreeSet<String>();
		TreeMap<String, ScopeListRepresentation> newScopeLists = new TreeMap<String, ScopeListRepresentation>();
		List<Pair<String, Integer>> newElementList = new ArrayList<Pair<String, Integer>>();

		for (int i = 0; i < equivalenceClass.getElementList().size(); i++) {
			String subTree = TreeRepresentationUtils.addNodeToTree(equivalenceClass.getPrefix(),
					equivalenceClass.getElementList().get(i));
			ScopeListRepresentation scopeList = equivalenceClass.getScopeListFor(subTree);

			// for each scope list element of a subtree, check if it actually appears in
			// that tree or is just embedded
			int support = 0;
			for (Triple<Integer, String, Scope> triple : scopeList) {
				if (TreeRepresentationUtils.containsSubtree(trees.get(triple.getLeft()), subTree)) {
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
			for (String pattern : equivalenceClass.getScopeLists().navigableKeySet()) {
				for (Triple<Integer, String, Scope> patternOccurence : equivalenceClass.getScopeListFor(pattern)) {
					treesWithPatternOccurrences[patternOccurence.getLeft()][currentPattern] = 1;
				}
				currentPattern++;
			}
		}

		return treesWithPatternOccurrences;
	}
}
