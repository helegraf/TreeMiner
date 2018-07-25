package treeminer;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class EquivalenceClass {
	private String prefix;
	private List<Pair<String, Integer>> elementList;
	private TreeMap<String, ScopeListRepresentation> scopeLists;

	public EquivalenceClass(String prefix, List<Pair<String, Integer>> elementList) {
		this.prefix = prefix;
		this.elementList = elementList;
		this.scopeLists = new TreeMap<String, ScopeListRepresentation>();
	}

	public EquivalenceClass(String prefix) {
		this.prefix = prefix;
		this.elementList = new ArrayList<Pair<String, Integer>>();
		this.scopeLists = new TreeMap<String, ScopeListRepresentation>();
	}

	public void discardNonFrequentElements(int minSupport) {
		// Check the min support in the scope lists
		TreeMap<String, ScopeListRepresentation> newScopeLists = new TreeMap<String, ScopeListRepresentation>();
		scopeLists.forEach((label, list) -> {
			if (list.size() >= minSupport) {
				newScopeLists.put(label, list);
			}
		});
		scopeLists = newScopeLists;

		// discard the elements that have no scope list
		List<Pair<String, Integer>> newElementList = new ArrayList<Pair<String, Integer>>();
		elementList.forEach(element -> {
			String subTree = TreeMiner.addNodeToPrefix(prefix, element);
			if (scopeLists.get(subTree) != null && scopeLists.get(subTree).size() > 0) {
				newElementList.add(element);
			}
		});
		elementList = newElementList;
	}

	public List<Pair<String, Integer>> getElementList() {
		return elementList;
	}

	public void addElement(Pair<String, Integer> element) {
		elementList.add(element);
	}

	public ScopeListRepresentation getScopeListFor(String node) {
		return scopeLists.get(node);
	}

	public void addScopeListFor(String node, ScopeListRepresentation scopeList) {
		scopeLists.put(node, scopeList);
	}

	public TreeMap<String, ScopeListRepresentation> getScopeLists() {
		return scopeLists;
	}

	public void setScopeLists(TreeMap<String, ScopeListRepresentation> scopeLists) {
		this.scopeLists = scopeLists; 
	}

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
