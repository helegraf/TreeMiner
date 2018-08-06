package treeminer;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

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
	private TreeMap<String, ScopeListRepresentation> scopeLists;

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
		this.scopeLists = new TreeMap<String, ScopeListRepresentation>();
	}

	/**
	 * Creates a new equivalence class with the given prefix.
	 * 
	 * @param prefix
	 *            The prefix of the new equivalence class
	 */
	public EquivalenceClass(String prefix) {
		this.prefix = prefix;
		this.elementList = new ArrayList<Pair<String, Integer>>();
		this.scopeLists = new TreeMap<String, ScopeListRepresentation>();
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
		TreeMap<String, ScopeListRepresentation> newScopeLists = new TreeMap<String, ScopeListRepresentation>();
		scopeLists.forEach((label, list) -> {
			if (list.size() >= minSupport) {
				newScopeLists.put(label, list);
			}
		});
		scopeLists = newScopeLists;

		// Discard the elements that have no scope list anymore
		List<Pair<String, Integer>> newElementList = new ArrayList<Pair<String, Integer>>();
		elementList.forEach(element -> {
			String subTree = TreeRepresentationUtils.addNodeToTree(prefix, element);
			if (scopeLists.get(subTree) != null && scopeLists.get(subTree).size() > 0) {
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
	public ScopeListRepresentation getScopeListFor(String subtree) {
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
	public void addScopeListFor(String subtree, ScopeListRepresentation scopeList) {
		scopeLists.put(subtree, scopeList);
	}

	/**
	 * Get the scope lists for all subtrees represented by this equivalence class.
	 * 
	 * @return The scope lists of this class
	 */
	public TreeMap<String, ScopeListRepresentation> getScopeLists() {
		return scopeLists;
	}

	/**
	 * Set the scope lists for this equivalence class.
	 * 
	 * @param scopeLists
	 *            The new scope lists
	 */
	public void setScopeLists(TreeMap<String, ScopeListRepresentation> scopeLists) {
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
