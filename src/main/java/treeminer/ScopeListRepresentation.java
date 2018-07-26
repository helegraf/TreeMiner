package treeminer;

import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

public class ScopeListRepresentation extends TreeSet<Triple<Integer, String, Scope>> {

	/**
	 * Generated by Eclipse
	 */
	private static final long serialVersionUID = -6692728568369449249L;

	public ScopeListRepresentation outScopeJoin(ScopeListRepresentation other, String xLabel) {
		ScopeListRepresentation newScopeList = new ScopeListRepresentation();

		this.forEach(scopeListElementX -> {
			other.forEach(scopeListElementY -> {
				if (scopeListElementX.getLeft().equals(scopeListElementY.getLeft())
						&& scopeListElementX.getMiddle().equals(scopeListElementY.getMiddle())
						&& scopeListElementX.getRight().isStrictlyLessThan(scopeListElementY.getRight())) {
					int treeId = scopeListElementY.getLeft();
					String joinedMatchLabel = scopeListElementY.getMiddle();
					Scope scope = scopeListElementY.getRight();
					newScopeList.add(new ImmutableTriple<Integer, String, Scope>(treeId, joinedMatchLabel, scope));
				}
			});
		});

		return newScopeList;
	}

	public ScopeListRepresentation inScopeJoin(ScopeListRepresentation other, String xLabel) {
		ScopeListRepresentation newScopeList = new ScopeListRepresentation();

		this.forEach(scopeListElementX -> {
			other.forEach(scopeListElementY -> {
				System.out.println("In scope test for " + scopeListElementX + " " + scopeListElementY);
				if (scopeListElementX.getLeft().equals(scopeListElementY.getLeft())
						&& scopeListElementX.getMiddle().equals(scopeListElementY.getMiddle())
						&& scopeListElementX.getRight().contains(scopeListElementY.getRight())) {
					int treeId = scopeListElementY.getLeft();
					String joinedMatchLabel = scopeListElementY.getMiddle();
					Scope scope = scopeListElementY.getRight();
					newScopeList.add(new ImmutableTriple<Integer, String, Scope>(treeId, joinedMatchLabel, scope));
				}
			});
		});

		return newScopeList;
	}
}