package treeminer.initialization;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import treeminer.EquivalenceClass;
import treeminer.Scope;
import treeminer.scopelists.elements.ScopeListElement;
import treeminer.scopelists.elements.SimpleScopeListElement;
import treeminer.scopelists.representation.AScopeListRepresentation;
import treeminer.scopelists.representation.ScopeListRepresentation;
import treeminer.util.TreeRepresentationUtils;

public class TreeMinerNonDistinctInitializer {
	
	public static List<EquivalenceClass> initialize(EquivalenceClass f1, List<String> trees, int minSupport) {
		// Generate candidate scope lists for the candidate equivalence classes
		TreeMap<String, AScopeListRepresentation<ScopeListElement>> mapF2PatternToOccurence = new TreeMap<>();
		TreeMap<String, AScopeListRepresentation<ScopeListElement>> mapF1PatternToOccurence = new TreeMap<>();
		
		generateCandidateScopeListsF1F2(f1, mapF2PatternToOccurence, mapF1PatternToOccurence);
		mapF1PatternToOccurence.forEach(f1::addScopeListFor);

		// Find candidate scope list frequencies
		for (int i = 0; i < trees.size(); i++) {
			findPatternsInTree(trees.get(i), mapF2PatternToOccurence, mapF1PatternToOccurence, i);
		}

		// Assemble scope lists for f2
		List<EquivalenceClass> candidateEquivalenceClasses = TreeMinerGeneralInitializer.generateCandidateEquivalenceClassesF2(f1);
		return TreeMinerNonDistinctInitializer.filterF2CandidateClassesByPatternOccurrences(candidateEquivalenceClasses,
				mapF2PatternToOccurence, minSupport);
	}
	
	private static void generateCandidateScopeListsF1F2(EquivalenceClass f1,
			TreeMap<String, AScopeListRepresentation<ScopeListElement>> f2ScopeLists,
			TreeMap<String, AScopeListRepresentation<ScopeListElement>> f1ScopeLists) {
		// Fill the scope lists with candidates
		f1.getElementList().forEach(pairX -> {

			f1ScopeLists.put(pairX.getLeft(), new ScopeListRepresentation());

			f1.getElementList().forEach(pairY -> {
				String pattern = String.format("%s%s%s%s%s", pairX.getLeft(),
						TreeRepresentationUtils.TREE_NODE_SEPARATOR, pairY.getLeft(),
						TreeRepresentationUtils.TREE_NODE_SEPARATOR, TreeRepresentationUtils.MOVE_UP_TOKEN);
				ScopeListRepresentation occurrences = new ScopeListRepresentation();
				f2ScopeLists.put(pattern, occurrences);
			});
		});
	}
	
	private static void findPatternsInTree(String tree,
			TreeMap<String, AScopeListRepresentation<ScopeListElement>> mapF2PatternToOccurence,
			TreeMap<String, AScopeListRepresentation<ScopeListElement>> mapF1PatternToOccurence, int i) {
		String[] treeRepresentation = tree.split(TreeRepresentationUtils.TREE_NODE_SEPARATOR);

		// Find the scope of each node in the tree
		Scope[] nodeScopes = new Scope[(int) Math.ceil(treeRepresentation.length / 2.0)];

		String[] matchLabels = new String[(int) Math.ceil(treeRepresentation.length / 2.0)];
		findNodeScopes(treeRepresentation, nodeScopes, matchLabels);

		// Find frequency of candidate elements in f1 and f2
		findCandidateFrequencies(mapF2PatternToOccurence, mapF1PatternToOccurence, i, treeRepresentation, nodeScopes,
				matchLabels);

	}
	
	private static void findNodeScopes(String[] treeRepresentation, Scope[] nodeScopes, String[] matchLabels) {
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
	
	private static void findCandidateFrequencies(
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

	private static void addNewPattern(TreeMap<String, AScopeListRepresentation<ScopeListElement>> mapF1PatternToOccurence,
			int i, Scope[] nodeScopes, String[] matchLabels, int atNode, String treeElement) {
		// Add the found single pattern
		ScopeListElement entry = new ScopeListElement(i, matchLabels[atNode], nodeScopes[atNode]);
		if (mapF1PatternToOccurence.get(treeElement) != null) {
			mapF1PatternToOccurence.get(treeElement).add(entry);
		}
	}

	private static void checkForDoublePattern(
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
				AScopeListRepresentation<ScopeListElement> list = mapF2PatternToOccurence.get(String.format(
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

	public static List<EquivalenceClass> filterF2CandidateClassesByPatternOccurrences(List<EquivalenceClass> candidateEquivalenceClasses,
			TreeMap<String, AScopeListRepresentation<ScopeListElement>> mapF2PatternToOccurence, int minSupport) {
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
