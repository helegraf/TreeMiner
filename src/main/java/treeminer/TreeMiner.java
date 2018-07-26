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

public class TreeMiner implements FrequentSubtreeFinder {

	List<String> trees;
	int minSupport;
	public static final String treeNodeSeparator = " ";
	public static final String moveUpToken = "-1";
	private List<EquivalenceClass> foundEquivalenceClasses = new ArrayList<EquivalenceClass>();
	private TreeSet<String> foundFrequentTrees = new TreeSet<String>();

	@Override
	public boolean containsSubtree(String tree, String subtree) {
		String[] treeRepresentation = tree.split(treeNodeSeparator);
		String[] subTreeRepresentation = subtree.split(treeNodeSeparator);
		// This doesn't find embedded subtrees only direct ones
		// Represents at which node we are of the subtreerepresentation
		int nextNode = 0;
		int childNum = 0;
		for (int i = 0; i < treeRepresentation.length; i++) {
			if (treeRepresentation[i].equals(subTreeRepresentation[nextNode])) {
				if (childNum == 0) {
					// Found fitting node
					nextNode++;
					if (nextNode == subTreeRepresentation.length) {
						return true;
					}
				} else {
					if (treeRepresentation[i].equals(moveUpToken)) {
						childNum--;
					} else {
						childNum++;
					}
				}
			} else {
				if (nextNode != 0) {
					if (treeRepresentation[i].equals(moveUpToken)) {
						if (childNum > 0) {
							childNum--;
						} else {
							nextNode--;
						}
					} else {
						childNum++;
					}
				}
			}
		}
		return false;
	}

	@Override
	public List<String> findFrequentSubtrees(List<String> trees, int minSupport) {
		this.trees = trees;
		this.minSupport = minSupport;

		// Find elements ONLY of f1 (not the scopes yet!)
		EquivalenceClass f1 = findElementsOfEquivalenceClassWithEmptyPrefix();
		foundEquivalenceClasses.add(f1);
		List<EquivalenceClass> f2Classes = findFrequentF2Subtrees(f1);
		foundEquivalenceClasses.addAll(f2Classes);
		f2Classes.forEach(f2class -> {
			findFrequentSubtrees(f2class);
		});
		foundEquivalenceClasses.forEach(foundClass -> {
			foundFrequentTrees.addAll(extractFrequentTrees(foundClass));
			System.out.println(foundClass);
		});
		return new ArrayList<String>(foundFrequentTrees);
	}

	/**
	 * Finds the initial equivalence class that has an empty prefix and contains all
	 * single nodes with a frequency of at least the minimum support.
	 * 
	 * @return The generated EuivalenceClass
	 */
	protected EquivalenceClass findElementsOfEquivalenceClassWithEmptyPrefix() {
		HashMap<String, Integer> labelFrequencies = new HashMap<String, Integer>();

		// For each tree
		for (String tree : trees) {
			// Break up tree in its labels
			String[] labels = tree.split(treeNodeSeparator);

			// For each label, increase the frequency
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

	protected List<EquivalenceClass> findFrequentF2Subtrees(EquivalenceClass f1) {
		// Generate candidate equivalence classes for each label as a root
		List<EquivalenceClass> candidateEquivalenceClasses = new ArrayList<EquivalenceClass>();
		List<Pair<String, Integer>> f1Elements = f1.getElementList();
		for (int i = 0; i < f1Elements.size(); i++) {
			List<Pair<String, Integer>> elementList = new ArrayList<Pair<String, Integer>>();
			for (int j = 0; j < f1Elements.size(); j++) {
				elementList.add(new ImmutablePair<String, Integer>(f1Elements.get(j).getLeft(), 0));
			}
			candidateEquivalenceClasses.add(new EquivalenceClass(f1Elements.get(i).getLeft(), elementList));
		}

		// The HashMap saves the scopelistrepresentations for all possibly generated
		// trees
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

		for (int i = 0; i < trees.size(); i++) {
			String tree = trees.get(i);
			String[] treeRepresentation = tree.split(treeNodeSeparator);

			// Find the scope of each node
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

			// Match single + double patterns
			atNode = -1;
			for (int j = 0; j < treeRepresentation.length; j++) {
				String treeElement = treeRepresentation[j];
				if (!treeElement.equals(moveUpToken)) {
					atNode++;
					// add the single pattern
					Triple<Integer, String, Scope> entry = new ImmutableTriple<Integer, String, Scope>(i,
							matchLabels[atNode], nodeScopes[atNode]);
					if (mapF1PatternToOccurence.get(treeElement) != null) {
						mapF1PatternToOccurence.get(treeElement).add(entry);
					}

					// check for double pattern (find direct and indirect children)
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

		// set the found scope lists for f1
		f1.setScopeLists(mapF1PatternToOccurence);

		// assemble scope lists for f2
		List<EquivalenceClass> newEquivalenceClasses = new ArrayList<EquivalenceClass>();
		candidateEquivalenceClasses.forEach(equivalenceClass -> {
			// For each pattern add the scope lists
			TreeMap<String, ScopeListRepresentation> scopeLists = new TreeMap<String, ScopeListRepresentation>();
			equivalenceClass.getElementList().forEach(element -> {
				String label = addNodeToPrefix(equivalenceClass.getPrefix(), element);
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

	protected void findFrequentSubtrees(EquivalenceClass equivalenceClass) {
		// For (x, i) element P
		for (Pair<String, Integer> XIelement : equivalenceClass.getElementList()) {
			// Create a new empty equivalence class as a result of appending x to node i in
			// its equivalence class
			String newPrefix = addNodeToPrefix(equivalenceClass.getPrefix(), XIelement);
			EquivalenceClass P_xi = new EquivalenceClass(newPrefix);

			// For (y, j) element P
			for (Pair<String, Integer> YJElement : equivalenceClass.getElementList()) {
				// R = join (x, i), (y, j)
				if (XIelement.getRight() == YJElement.getRight()) {
					// add (y, j+1) if it is supported
					ScopeListRepresentation xScopeList = equivalenceClass
							.getScopeListFor(addNodeToPrefix(equivalenceClass.getPrefix(), XIelement));
					ScopeListRepresentation yScopeList = equivalenceClass
							.getScopeListFor(addNodeToPrefix(equivalenceClass.getPrefix(), YJElement));

					if (xScopeList == null) {
						continue;
					}

					System.out.println("old prefix class " + equivalenceClass.getPrefix() + " new prefix class "
							+ P_xi.getPrefix());
					ScopeListRepresentation newScopeList = xScopeList.inScopeJoin(yScopeList, XIelement.getLeft());

					if (newScopeList.size() >= minSupport) {
						// not yjelement.getright, but is attached to the new position of x
						int numberOfChildrenOfParentNode = findNumberOfChildrenOfNode(equivalenceClass.getPrefix(),
								XIelement.getRight());
						int newXPosition = XIelement.getRight() + 1 + numberOfChildrenOfParentNode;
						Pair<String, Integer> newElement = new ImmutablePair<String, Integer>(YJElement.getLeft(),
								newXPosition);
						P_xi.addElement(newElement);
						P_xi.addScopeListFor(addNodeToPrefix(P_xi.getPrefix(), newElement), newScopeList);
					}

					// add (y, j)
					xScopeList = equivalenceClass
							.getScopeListFor(addNodeToPrefix(equivalenceClass.getPrefix(), XIelement));
					yScopeList = equivalenceClass
							.getScopeListFor(addNodeToPrefix(equivalenceClass.getPrefix(), YJElement));

					if (xScopeList == null) {
						continue;
					}

					newScopeList = xScopeList.outScopeJoin(yScopeList, XIelement.getLeft());

					if (newScopeList.size() >= minSupport) {
						P_xi.addElement(YJElement);
						P_xi.addScopeListFor(addNodeToPrefix(P_xi.getPrefix(), YJElement), newScopeList);
					}
				} else if (XIelement.getRight() > YJElement.getRight()) {
					// add (y, j)
					ScopeListRepresentation xScopeList = equivalenceClass
							.getScopeListFor(addNodeToPrefix(equivalenceClass.getPrefix(), XIelement));
					ScopeListRepresentation yScopeList = equivalenceClass
							.getScopeListFor(addNodeToPrefix(equivalenceClass.getPrefix(), YJElement));

					if (xScopeList == null) {
						continue;
					}

					ScopeListRepresentation newScopeList = xScopeList.outScopeJoin(yScopeList, XIelement.getLeft());

					if (newScopeList.size() >= minSupport) {
						P_xi.addElement(YJElement);
						P_xi.addScopeListFor(addNodeToPrefix(P_xi.getPrefix(), YJElement), newScopeList);
					}
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

	protected static String addNodeToPrefix(String prefix, Pair<String, Integer> node) {
		// Check if empty prefix
		if (prefix == null || prefix.equals("")) {
			return node.getLeft();
		}

		// Check if only root node
		if (prefix.length() == 1) {
			return prefix + treeNodeSeparator + node.getLeft() + treeNodeSeparator + moveUpToken;
		}

		StringBuilder builder = new StringBuilder();
		String[] tree = prefix.split(treeNodeSeparator);
		boolean foundNode = false;
		boolean attachedNode = false;
		int atNode = -1;
		for (int i = 0; i < tree.length - 1; i++) {
			String treeElement = tree[i];

			// Find the node where the node shall be attached
			if (!foundNode) {
				// If the element is a node
				if (!treeElement.equals(moveUpToken)) {
					// Increase the count
					atNode++;
					if (atNode == node.getRight()) {
						foundNode = true;
						atNode = 0;
					}
				}
				// Append this to the prefix
				builder.append(treeElement + treeNodeSeparator);
			} else if (!attachedNode) {
				// Find out if we can directly attach the child now
				if (atNode == 0) {
					// Check if there are no more children (would move up again here!)
					if (tree[i].equals(moveUpToken)) {
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
				builder.append(tree[tree.length - 1]);
			} else {

				builder.append(tree[tree.length - 1] + treeNodeSeparator);
				builder.append(node.getLeft() + treeNodeSeparator + moveUpToken);
			}
		} else {
			builder.append(tree[tree.length - 1]);
		}

		return builder.toString();

	}

	/**
	 * Finds all the non-embedded frequent subtrees.
	 * 
	 * @param eq
	 * @return
	 */
	TreeSet<String> extractFrequentTrees(EquivalenceClass eq) {
		TreeSet<String> foundTrees = new TreeSet<String>();
		TreeMap<String,ScopeListRepresentation> newScopeLists = new TreeMap<String, ScopeListRepresentation>();
		
		eq.getScopeLists().forEach((subTree, scopeList) -> {
			// for each scope list element of a subtree, check if actually appears in that tree!
			int support = 0;
			for (Triple<Integer,String,Scope> triple : scopeList) {
				if (containsSubtree(trees.get(triple.getLeft()), subTree)) {
					support++;
				}
			}
			
			if (support >= minSupport) {
				foundTrees.add(subTree);
				newScopeLists.put(subTree, scopeList);
			} 
		});
		eq.setScopeLists(newScopeLists);
		return foundTrees;
	}

	int findNumberOfChildrenOfNode(String subtree, int attachedToNode) {
		String[] tree = subtree.split(" ");
		int atNode = -1;
		boolean foundNode = false;
		int numChildren = 0;
		for (String treeElement : tree) {
			if (!foundNode) {
				if (!treeElement.equals(moveUpToken)) {
					atNode++;
					if (atNode == attachedToNode) {
						foundNode = true;
						// atNode now represents depth
						atNode = 0;
					}
				}
			} else {
				if (!treeElement.equals(moveUpToken)) {
					atNode++;
					numChildren++;
				} else {
					if (atNode == 0) {
						break;
					} else {
						atNode--;
					}
				}
			}
		}
		return numChildren;
	}
}
