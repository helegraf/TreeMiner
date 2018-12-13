package treeminer.initialization;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import treeminer.EquivalenceClass;
import treeminer.Scope;
import treeminer.scopelists.elements.ScopeVectorListElement;
import treeminer.scopelists.elements.SimpleScopeListElement;
import treeminer.scopelists.representation.AScopeListRepresentation;
import treeminer.scopelists.representation.ScopeVectorListRepresentation;
import treeminer.util.TreeRepresentationUtils;

/**
 * Initialized the TreeMiner for the case that only distinct occurrences of
 * patterns are counted, i.e. several occurrences of the same pattern within one
 * tree are counted as one occurrence only.
 * 
 * @author Helena Graf
 *
 */
public class TreeMinerDistinctInitializer {

	private TreeMinerDistinctInitializer() {
	}

	/**
	 * Finds the scopes for the elements in the equivalence class F1 and generates
	 * all valid classes for F2 on the premise that only one occurrence of a pattern
	 * within a tree is counted.
	 * 
	 * @param f1
	 *            the equivalence class with the empty prefix
	 * @param trees
	 *            the trees in the given database
	 * @param minSupport
	 *            the minimum (absolute) support for a patternt to be considered
	 *            frequent in the database
	 * @return all valid equivalence classes with a one-node prefix
	 */
	public static List<EquivalenceClass> initialize(EquivalenceClass f1, List<String> trees, int minSupport) {
		// Generate candidate scope lists for the candidate equivalence classes
		TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF2PatternToOccurence = new TreeMap<>();
		TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF1PatternToOccurence = new TreeMap<>();

		generateCandidateScopeListsF1F2NoMatchLabel(f1, mapF2PatternToOccurence, mapF1PatternToOccurence);
		mapF1PatternToOccurence.forEach(f1::addScopeListFor);

		// Find candidate scope list frequencies
		for (int i = 0; i < trees.size(); i++) {
			findPatternsInTreeNoMatchLabel(trees.get(i), mapF2PatternToOccurence, mapF1PatternToOccurence, i);
		}

		// Assemble scope lists for f2
		List<EquivalenceClass> candidateEquivalenceClasses = TreeMinerGeneralInitializer
				.generateCandidateEquivalenceClassesF2(f1);
		return TreeMinerDistinctInitializer.filterF2CandidateClassesByPatternOccurrencesNoMatchLabel(
				candidateEquivalenceClasses, mapF2PatternToOccurence, minSupport);
	}

	private static void generateCandidateScopeListsF1F2NoMatchLabel(EquivalenceClass f1,
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> f2ScopeLists,
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> f1ScopeLists) {
		// Fill the scope lists with candidates
		f1.getElementList().forEach(pairX -> {

			f1ScopeLists.put(pairX.getLeft(), new ScopeVectorListRepresentation());

			f1.getElementList().forEach(pairY -> {
				String pattern = String.format("%s%s%s%s%s", pairX.getLeft(),
						TreeRepresentationUtils.TREE_NODE_SEPARATOR, pairY.getLeft(),
						TreeRepresentationUtils.TREE_NODE_SEPARATOR, TreeRepresentationUtils.MOVE_UP_TOKEN);
				ScopeVectorListRepresentation occurrences = new ScopeVectorListRepresentation();
				f2ScopeLists.put(pattern, occurrences);
			});
		});
	}

	private static void findPatternsInTreeNoMatchLabel(String tree,
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF2PatternToOccurence,
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF1PatternToOccurence, int i) {
		String[] treeRepresentation = tree.split(TreeRepresentationUtils.TREE_NODE_SEPARATOR);

		// Find the scope of each node in the tree
		Scope[] nodeScopes = new Scope[(int) Math.ceil(treeRepresentation.length / 2.0)];

		findNodeScopesNoMatchLabel(treeRepresentation, nodeScopes);
		findCandidateFrequenciesNoMatchLabel(mapF2PatternToOccurence, mapF1PatternToOccurence, i, treeRepresentation,
				nodeScopes);

	}

	private static void findNodeScopesNoMatchLabel(String[] treeRepresentation, Scope[] nodeScopes) {
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

	private static void findCandidateFrequenciesNoMatchLabel(
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF2PatternToOccurence,
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF1PatternToOccurence, int i,
			String[] treeRepresentation, Scope[] nodeScopes) {
		int atNode = -1;
		for (int j = 0; j < treeRepresentation.length; j++) {
			String treeElement = treeRepresentation[j];
			if (!treeElement.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
				atNode++;
				addNewPatternNoMatchLabel(mapF1PatternToOccurence, i, nodeScopes, atNode, treeElement);

				checkForDoublePatternNoMatchLabel(mapF2PatternToOccurence, i, treeRepresentation, nodeScopes, atNode, j,
						treeElement);
			}
		}
	}

	private static void addNewPatternNoMatchLabel(
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF1PatternToOccurence, int i,
			Scope[] nodeScopes, int atNode, String treeElement) {
		// Add the found single pattern
		ArrayList<Scope> scopes = new ArrayList<>();
		scopes.add(nodeScopes[atNode]);
		ScopeVectorListElement entry = new ScopeVectorListElement(i, scopes);
		if (mapF1PatternToOccurence.get(treeElement) != null) {
			mapF1PatternToOccurence.get(treeElement).add(entry);
		}
	}

	private static void checkForDoublePatternNoMatchLabel(
			TreeMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF2PatternToOccurence, int i,
			String[] treeRepresentation, Scope[] nodeScopes, int atNode, int j, String treeElement) {
		// Check for double pattern (find direct and indirect children of a node)
		int childLevel = 0;
		int childNumber = 0;
		for (int k = j + 1; k < treeRepresentation.length; k++) {
			String potentialChild = treeRepresentation[k];
			if (!potentialChild.equals(TreeRepresentationUtils.MOVE_UP_TOKEN)) {
				childLevel++;
				childNumber++;
				List<Scope> scopes = new ArrayList<>();
				// parent scope
				scopes.add(nodeScopes[atNode]);
				// this node scope
				scopes.add(nodeScopes[atNode + childNumber]);
				// Scope = scope of parent (treeElement) + Scope of treeRepresentation[k]
				ScopeVectorListElement f2entry = new ScopeVectorListElement(i, scopes);
				AScopeListRepresentation<ScopeVectorListElement> list = mapF2PatternToOccurence.get(String.format(
						"%s%s%s%s%s", treeElement, TreeRepresentationUtils.TREE_NODE_SEPARATOR, treeRepresentation[k],
						TreeRepresentationUtils.TREE_NODE_SEPARATOR, TreeRepresentationUtils.MOVE_UP_TOKEN));
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

	/**
	 * Filter the given candidate equivalence classes F2 so that only valid classes
	 * remain.
	 * 
	 * @param candidateEquivalenceClasses
	 *            the candidate equivalence classes with a one-node prefix
	 * @param mapF2PatternToOccurence
	 *            pattern occurrences in F2
	 * @param minSupport
	 *            the minimum support of a pattern to be considered frequent
	 * @return the actual equivalence classes F2
	 */
	public static List<EquivalenceClass> filterF2CandidateClassesByPatternOccurrencesNoMatchLabel(
			List<EquivalenceClass> candidateEquivalenceClasses,
			SortedMap<String, AScopeListRepresentation<ScopeVectorListElement>> mapF2PatternToOccurence,
			int minSupport) {
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
}
