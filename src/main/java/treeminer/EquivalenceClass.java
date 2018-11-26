package treeminer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;

import treeminer.util.TreeRepresentationUtils;

/**
 * Represents an equivalence class of found subtrees that all begin with the
 * same prefix and only differ in the last node appended to this prefix.
 * 
 * @author Helena Graf
 *
 */
public class EquivalenceClass {

	private String prefix;
	private List<Pair<String, Integer>> elementList;
	private SortedMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> scopeLists;

	/**
	 * Creates a new equivalence class with the given prefix and list of elements it
	 * contains (labels of the nodes attached to the prefix together with the
	 * position where they are attached).
	 * 
	 * @param prefix
	 *            The prefix of the new equivalence class
	 * @param elementList
	 *            The elements of the new equivalence class
	 */
	public EquivalenceClass(String prefix, List<Pair<String, Integer>> elementList) {
		this.prefix = prefix;
		this.elementList = elementList;
		this.scopeLists = new TreeMap<>();
	}

	/**
	 * Creates a new equivalence class with the given prefix.
	 * 
	 * @param prefix
	 *            The prefix of the new equivalence class
	 */
	public EquivalenceClass(String prefix) {
		this.prefix = prefix;
		this.elementList = new ArrayList<>();
		this.scopeLists = new TreeMap<>();
	}

	/**
	 * Checks the support for all subtrees contained in this class and discards
	 * their elements as well as scope lists if they are not frequent.
	 * 
	 * @param minSupport
	 *            The minimal number of occurrences a subtree must have to be
	 *            considered frequent
	 */
	public void discardNonFrequentElements(int minSupport) {
		// Check the minimum support in the scope lists
		TreeMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> newScopeLists = new TreeMap<>();
		scopeLists.forEach((label, list) -> {
			// Discard scope lists with not enough occurrences
			if (list.size() >= minSupport) {
				// Discard scope lists with not enough distinct occurrences
				HashSet<Integer> distinctOccurrences = new HashSet<>();
				list.forEach(scope -> distinctOccurrences.add(scope.getTreeIndex()));
				if (distinctOccurrences.size() >= minSupport) {
					newScopeLists.put(label, list);
				}
			}
		});
		scopeLists = newScopeLists;

		// Discard the elements that have no scope list anymore
		List<Pair<String, Integer>> newElementList = new ArrayList<>();
		elementList.forEach(element -> {
			String subTree = TreeRepresentationUtils.addNodeToTree(prefix, element);
			if (scopeLists.get(subTree) != null && !scopeLists.get(subTree).isEmpty()) {
				newElementList.add(element);
			}
		});
		elementList = newElementList;
	}

	/**
	 * Get the list of elements in this equivalence class.
	 * 
	 * @return The list of elements
	 */
	public List<Pair<String, Integer>> getElementList() {
		return elementList;
	}

	/**
	 * Set the list of elements in this equivalence class.
	 * 
	 * @param elementList
	 *            The list of element
	 */
	public void setElementList(List<Pair<String, Integer>> elementList) {
		this.elementList = elementList;
	}

	/**
	 * Add an element to the list of elements in this equivalence class.
	 * 
	 * @param element
	 *            The element to be added
	 */
	public void addElement(Pair<String, Integer> element) {
		elementList.add(element);
	}

	/**
	 * Gets the scope list of the subtree.
	 * 
	 * @param subtree
	 *            The subtree for which to get the scope list
	 * @return The scope list of the subtree
	 */
	public AScopeListRepresentation<? extends SimpleScopeListElement> getScopeListFor(String subtree) {
		return scopeLists.get(subtree);
	}

	/**
	 * Adds a subtree together with its occurrences
	 * 
	 * @param subtree
	 *            The subtree to be added
	 * @param scopeList
	 *            The occurrences of the subtree given by a scope list
	 */
	public void addScopeListFor(String subtree, AScopeListRepresentation<? extends SimpleScopeListElement> scopeList) {
		scopeLists.put(subtree, scopeList);
	}

	/**
	 * Get the scope lists for all subtrees represented by this equivalence class.
	 * 
	 * @return The scope lists of this class
	 */
	public SortedMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> getScopeLists() {
		return scopeLists;
	}

	/**
	 * Set the scope lists for this equivalence class.
	 * 
	 * @param scopeLists
	 *            The new scope lists
	 */
	public void setScopeLists(SortedMap<String, AScopeListRepresentation<? extends SimpleScopeListElement>> scopeLists) {
		this.scopeLists = scopeLists;
	}

	/**
	 * Get the prefix of this equivalence class.
	 * 
	 * @return The prefix of this class
	 */
	public String getPrefix() {
		return prefix;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Equivalence Class with Prefix: ");
		builder.append(prefix);
		builder.append("\n");
		builder.append("Elements: \n");
		elementList.forEach(pair -> {
			builder.append(pair);
			builder.append(" ");
		});
		builder.append("\nScopes: \n");
		scopeLists.forEach((label, scopeList) -> {
			builder.append(label);
			builder.append(" ");
			builder.append(scopeList);
			builder.append("\n");
		});
		return builder.toString();
	}
}
