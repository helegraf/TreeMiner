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

	/**
	 * The token that this algorithm assumes is used to separate tree nodes in the
	 * String representation of a tree
	 */
	public static final String treeNodeSeparator = " ";

	/**
	 * The token that his algorithms assumes is used to represent moving up to a
	 * parent node in the String representation of a tree
	 */
	public static final String moveUpToken = "-1";

	private List<String> trees;
	private int minSupport;
	private List<EquivalenceClass> foundEquivalenceClasses = new ArrayList<EquivalenceClass>();
	private TreeSet<String> foundFrequentTrees = new TreeSet<String>();

	@Override
	public boolean containsSubtree(String tree, String subtree) {
		// Split the given trees in their elements
		String[] treeElements = tree.split(treeNodeSeparator);
		String[] subTreeElements = subtree.split(treeNodeSeparator);

		// Represents the next node from the subtree we are trying to match in the tree
		int nextNode = 0;
		// Represents the number of children a current parent node has that are not
		// found in the subtree
		int childNum = 0;

		// Iterate over the tree representation trying ot match the subtree
		// representation
		for (int i = 0; i < treeElements.length; i++) {
			if (treeElements[i].equals(subTreeElements[nextNode])) {
				if (childNum == 0) {
					// If we are not currently in an unrelated branch in the tree, and have found a
					// matching node, we can start searching for the next one
					nextNode++;
					if (nextNode == subTreeElements.length) {
						return true;
					}
				} else {
					// We have found a matching node, but are in an unrelated branch of the tree
					if (treeElements[i].equals(moveUpToken)) {
						// We are moving back to the parent
						childNum--;
					} else {
						// We have found another child
						childNum++;
					}
				}
			} else {
				// We have found an unrelated node. If we haven't found the start of the pattern
				// yet, proceed iteration.
				if (nextNode != 0) {
					if (treeElements[i].equals(moveUpToken)) {
						if (childNum > 0) {
							// If we have found a moveUpToken, and the current matched node has children
							// that aren't in the pattern, we have moved closer to the parent again
							childNum--;
						} else {
							// If there are no children of the current matched node anymore, but the pattern
							// is not completely matched, it means that we have followed a branch that does
							// not contain the whole pattern and need to retreat
							nextNode--;
						}
					} else {
						// We have found another child not in the pattern that we want to match
						childNum++;
					}
				}
			}
		}

		// We iterated over the whole tree without finding the pattern
		return false;
	}

	@Override
	public List<String> findFrequentSubtrees(List<String> trees, int minSupport) {
		this.trees = trees;
		this.minSupport = minSupport;

		EquivalenceClass f1 = findFrequentF1Subtrees();
		foundEquivalenceClasses.add(f1);

		List<EquivalenceClass> f2Classes = findFrequentF2Subtrees(f1);
		foundEquivalenceClasses.addAll(f2Classes);

		f2Classes.forEach(f2class -> {
			findFrequentSubtrees(f2class);
		});

		foundEquivalenceClasses.forEach(foundClass -> {
			foundFrequentTrees.addAll(extractNonEmbeddedFrequentTrees(foundClass));
		});

		return new ArrayList<String>(foundFrequentTrees);
	}

	/**
	 * Finds the initial equivalence class f1 that has an empty prefix and contains
	 * all single nodes with a frequency of at least the minimum support. Does not
	 * find the scope of nodes, only the elements of the equivalence class.
	 * 
	 * @return The generated EuivalenceClass
	 */
	protected EquivalenceClass findFrequentF1Subtrees() {
		HashMap<String, Integer> labelFrequencies = new HashMap<String, Integer>();

		// For each tree
		for (String tree : trees) {
			// Break up tree in its labels
			String[] labels = tree.split(treeNodeSeparator);

			// For each label, increase the frequency if found
			for (String label : labels) {
				if (!label.equals(moveUpToken)) {
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
	protected List<EquivalenceClass> findFrequentF2Subtrees(EquivalenceClass f1) {
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
				String pattern = pairX.getLeft() + treeNodeSeparator + pairY.getLeft() + treeNodeSeparator
						+ moveUpToken;
				ScopeListRepresentation occurrences = new ScopeListRepresentation();
				mapF2PatternToOccurence.put(pattern, occurrences);
			});
		});

		// Find candidate scope list frequencies
		// For each tree, iterate over it twice to find scopes of nodes first
		for (int i = 0; i < trees.size(); i++) {
			String tree = trees.get(i);
			String[] treeRepresentation = tree.split(treeNodeSeparator);

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
				if (!treeElement.equals(moveUpToken)) {
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
				matchLabelBuilder.append(treeElement + treeNodeSeparator);
			}
			// First node has to be closed separately because it doesn't have a moveUpToken
			nodeScopes[0].setUpperBound(nodeScopes.length - 1);

			// Find frequency of candidate elements in f1 and f2
			atNode = -1;
			for (int j = 0; j < treeRepresentation.length; j++) {
				String treeElement = treeRepresentation[j];
				if (!treeElement.equals(moveUpToken)) {
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
						if (!potentialChild.equals(moveUpToken)) {
							childLevel++;
							childNumber++;

							Triple<Integer, String, Scope> f2entry = new ImmutableTriple<Integer, String, Scope>(i,
									matchLabels[atNode], nodeScopes[atNode + childNumber]);
							if (mapF2PatternToOccurence.get(treeElement + treeNodeSeparator + treeRepresentation[k]
									+ treeNodeSeparator + moveUpToken) != null) {
								mapF2PatternToOccurence.get(treeElement + treeNodeSeparator + treeRepresentation[k]
										+ treeNodeSeparator + moveUpToken).add(f2entry);
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
				String label = addNodeToTree(equivalenceClass.getPrefix(), element);
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
			String newPrefix = addNodeToTree(equivalenceClass.getPrefix(), XIelement);
			EquivalenceClass P_xi = new EquivalenceClass(newPrefix);

			// For (y, j) element P
			for (Pair<String, Integer> YJElement : equivalenceClass.getElementList()) {
				// i = j case
				if (XIelement.getRight() == YJElement.getRight()) {
					// Test for (y, n_i)
					ScopeListRepresentation xScopeList = equivalenceClass
							.getScopeListFor(addNodeToTree(equivalenceClass.getPrefix(), XIelement));
					ScopeListRepresentation yScopeList = equivalenceClass
							.getScopeListFor(addNodeToTree(equivalenceClass.getPrefix(), YJElement));
					if (xScopeList == null) {
						continue;
					}

					ScopeListRepresentation newScopeList = xScopeList.inScopeJoin(yScopeList);

					if (newScopeList.size() >= minSupport) {
						int numberOfChildrenOfParentNode = findNumberOfChildrenOfNode(equivalenceClass.getPrefix(),
								XIelement.getRight());
						int newXPosition = XIelement.getRight() + 1 + numberOfChildrenOfParentNode;
						Pair<String, Integer> newElement = new ImmutablePair<String, Integer>(YJElement.getLeft(),
								newXPosition);
						P_xi.addElement(newElement);
						P_xi.addScopeListFor(addNodeToTree(P_xi.getPrefix(), newElement), newScopeList);
					}

					// Test for (y, j)
					newScopeList = xScopeList.outScopeJoin(yScopeList);

					if (newScopeList.size() >= minSupport) {
						P_xi.addElement(YJElement);
						P_xi.addScopeListFor(addNodeToTree(P_xi.getPrefix(), YJElement), newScopeList);
					}
					// i > j case
				} else if (XIelement.getRight() > YJElement.getRight()) {
					// Test for (y, j)
					ScopeListRepresentation xScopeList = equivalenceClass
							.getScopeListFor(addNodeToTree(equivalenceClass.getPrefix(), XIelement));
					ScopeListRepresentation yScopeList = equivalenceClass
							.getScopeListFor(addNodeToTree(equivalenceClass.getPrefix(), YJElement));
					if (xScopeList == null) {
						continue;
					}

					ScopeListRepresentation newScopeList = xScopeList.outScopeJoin(yScopeList);

					if (newScopeList.size() >= minSupport) {
						P_xi.addElement(YJElement);
						P_xi.addScopeListFor(addNodeToTree(P_xi.getPrefix(), YJElement), newScopeList);
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
	 * class.
	 * 
	 * @param equivalenceClass
	 *            The equivalence class containing the found frequent (embedded)
	 *            subtrees
	 * @return The found non-embedded frequent subtrees
	 */
	protected TreeSet<String> extractNonEmbeddedFrequentTrees(EquivalenceClass equivalenceClass) {
		TreeSet<String> foundTrees = new TreeSet<String>();
		TreeMap<String, ScopeListRepresentation> newScopeLists = new TreeMap<String, ScopeListRepresentation>();

		equivalenceClass.getScopeLists().forEach((subTree, scopeList) -> {
			// for each scope list element of a subtree, check if it actually appears in
			// that tree or is just embedded
			int support = 0;
			for (Triple<Integer, String, Scope> triple : scopeList) {
				if (containsSubtree(trees.get(triple.getLeft()), subTree)) {
					support++;
				}
			}

			if (support >= minSupport) {
				foundTrees.add(subTree);
				newScopeLists.put(subTree, scopeList);
			}
		});
		equivalenceClass.setScopeLists(newScopeLists);
		return foundTrees;
	}

	/**
	 * Adds the given node to the given tree and returns the new tree.
	 * 
	 * @param tree
	 *            The tree to which the node is added
	 * @param node
	 *            The node that is added to the tree
	 * @return The new tree after the node has been added
	 */
	protected static String addNodeToTree(String tree, Pair<String, Integer> node) {
		// Check if empty prefix
		if (tree == null || tree.equals("")) {
			return node.getLeft();
		}

		// Check if the tree consists of only a root node
		if (tree.length() == 1) {
			return tree + treeNodeSeparator + node.getLeft() + treeNodeSeparator + moveUpToken;
		}

		StringBuilder builder = new StringBuilder();
		String[] treeElements = tree.split(treeNodeSeparator);
		boolean foundNode = false;
		boolean attachedNode = false;
		int atNode = -1;
		for (int i = 0; i < treeElements.length - 1; i++) {
			String treeElement = treeElements[i];

			// Find the node where the node shall be attached
			if (!foundNode) {
				// If the element is a node
				if (!treeElement.equals(moveUpToken)) {
					// Increase the count
					atNode++;
					if (atNode == node.getRight()) {
						// We have found the node where we want to attach
						foundNode = true;
						atNode = 0;
					}
				}
				// Append the current node to the tree
				builder.append(treeElement + treeNodeSeparator);
			} else if (!attachedNode) {
				// Find out if we can directly attach the child now
				if (atNode == 0) {
					// Check if there are no more children (would move up again here!)
					if (treeElements[i].equals(moveUpToken)) {
						builder.append(node.getLeft() + treeNodeSeparator + moveUpToken + treeNodeSeparator);
						attachedNode = true;
					} else {
						atNode--;
					}
				} else {
					atNode = (treeElement.equals(moveUpToken)) ? atNode + 1 : atNode - 1;
				}
				builder.append(treeElement + treeNodeSeparator);
			} else {
				// In this case we have found and attached the node and just need to add all the
				// -1
				builder.append(treeElement + treeNodeSeparator);
			}
		}

		// Last node
		if (!attachedNode) {
			if (atNode == 0) {
				builder.append(node.getLeft() + treeNodeSeparator + moveUpToken + treeNodeSeparator);
				builder.append(treeElements[treeElements.length - 1]);
			} else {

				builder.append(treeElements[treeElements.length - 1] + treeNodeSeparator);
				builder.append(node.getLeft() + treeNodeSeparator + moveUpToken);
			}
		} else {
			builder.append(treeElements[treeElements.length - 1]);
		}

		return builder.toString();
	}

	/**
	 * Gives the number of children of a given node in a given tree.
	 * 
	 * @param tree
	 *            The tree in which the node is contained
	 * @param node
	 *            The node in the tree identified by its position in the depth-first
	 *            pre-order traversal of the tree
	 * @return The number of children the node has in the tree
	 */
	protected static int findNumberOfChildrenOfNode(String tree, int node) {
		String[] treeElements = tree.split(" ");

		// Represents the latest node we have found in the given tree
		int atNode = -1;
		boolean foundNode = false;
		int numChildren = 0;
		for (String treeElement : treeElements) {
			if (!foundNode) {
				// If we haven't found the node, keep searching
				if (!treeElement.equals(moveUpToken)) {
					atNode++;
					if (atNode == node) {
						foundNode = true;
						// atNode now represents depth we are under
						atNode = 0;
					}
				}
			} else {
				if (!treeElement.equals(moveUpToken)) {
					// We have found a new child
					atNode++;
					numChildren++;
				} else {
					if (atNode == 0) {
						// We have found all children
						break;
					} else {
						// We are moving closer to the node again
						atNode--;
					}
				}
			}
		}

		return numChildren;
	}
}
