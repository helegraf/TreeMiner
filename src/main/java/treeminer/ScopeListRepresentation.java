package treeminer;

import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Represents the ordered list of occurrences of a pattern.
 * 
 * <p>
 * An element of the list gives the consists of the tree id of the tree the
 * pattern is found in, the match label in that tree, and the scope of the
 * rightmost node of this pattern occurrence in the tree.
 * </p>
 * 
 * @author Helena Graf
 *
 */
public class ScopeListRepresentation extends TreeSet<Triple<Integer, String, Scope>> {

	/**
	 * Generated by Eclipse
	 */
	private static final long serialVersionUID = -6692728568369449249L;

	/**
	 * Performs an out scope join of this scope list with another scope list.
	 * 
	 * @param other
	 *            The scope list this list should be joined with
	 * @return The scope list that results from this join
	 */
	public ScopeListRepresentation outScopeJoin(ScopeListRepresentation other) {
		ScopeListRepresentation newScopeList = new ScopeListRepresentation();

		this.forEach(scopeListElementX -> other.forEach(scopeListElementY -> {
			if (scopeListElementX.getLeft().equals(scopeListElementY.getLeft())
					&& scopeListElementX.getMiddle().equals(scopeListElementY.getMiddle())
					&& scopeListElementX.getRight().isStrictlyLessThan(scopeListElementY.getRight())) {
				int treeId = scopeListElementY.getLeft();
				String joinedMatchLabel = scopeListElementY.getMiddle();
				Scope scope = scopeListElementY.getRight();
				newScopeList.add(new ImmutableTriple<Integer, String, Scope>(treeId, joinedMatchLabel, scope));
			}
		}));

		return newScopeList;
	}

	/**
	 * Performs an in scope join of this scope list with another scope list.
	 * 
	 * @param other
	 *            The scope list this list should be joined with
	 * @return The scope list that results from this join
	 */
	public ScopeListRepresentation inScopeJoin(ScopeListRepresentation other) {
		ScopeListRepresentation newScopeList = new ScopeListRepresentation();

		this.forEach(scopeListElementX -> other.forEach(scopeListElementY -> {
			if (scopeListElementX.getLeft().equals(scopeListElementY.getLeft())
					&& scopeListElementX.getMiddle().equals(scopeListElementY.getMiddle())
					&& scopeListElementX.getRight().contains(scopeListElementY.getRight())) {
				int treeId = scopeListElementY.getLeft();
				String joinedMatchLabel = scopeListElementY.getMiddle();
				Scope scope = scopeListElementY.getRight();
				newScopeList.add(new ImmutableTriple<Integer, String, Scope>(treeId, joinedMatchLabel, scope));
			}
		}));

		return newScopeList;
	}
}
