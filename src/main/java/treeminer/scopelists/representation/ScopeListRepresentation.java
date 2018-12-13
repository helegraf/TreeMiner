package treeminer.scopelists.representation;

import treeminer.Scope;
import treeminer.scopelists.elements.ScopeListElement;

/**
 * Represents the ordered list of occurrences of a pattern.
 * 
 * <p>
 * An element of the list gives the consists of the tree id of the tree the
 * pattern is found in, the match label in that tree, and the scope of the
 * Scopemost node of this pattern occurrence in the tree.
 * </p>
 * 
 * @author Helena Graf
 *
 */
public class ScopeListRepresentation extends AScopeListRepresentation<ScopeListElement> {

	/**
	 * Performs an out scope join of this scope list with another scope list.
	 * 
	 * @param other
	 *            the scope list this list should be joined with
	 * @param attachedTo
	 *            the node that the new node for which this join is done is attached
	 *            to
	 * @return the scope list that results from this join
	 */
	@Override
	public AScopeListRepresentation<ScopeListElement> outScopeJoin(AScopeListRepresentation<ScopeListElement> other,
			int attachedTo) {
		ScopeListRepresentation newScopeList = new ScopeListRepresentation();

		this.forEach(scopeListElementX -> other.forEach(scopeListElementY -> {
			if (scopeListElementX.getTreeIndex() == scopeListElementY.getTreeIndex()
					&& scopeListElementX.getMatchLabel().equals(scopeListElementY.getMatchLabel())
					&& scopeListElementX.getScope().isStrictlyLessThan(scopeListElementY.getScope())) {
				int treeId = scopeListElementY.getTreeIndex();
				String joinedMatchLabel = scopeListElementY.getMatchLabel();
				Scope scope = scopeListElementY.getScope();
				newScopeList.add(new ScopeListElement(treeId, joinedMatchLabel, scope));
			}
		}));

		return newScopeList;
	}

	/**
	 * Performs an in scope join of this scope list with another scope list.
	 * 
	 * @param other
	 *            the scope list this list should be joined with
	 * @return the scope list that results from this join
	 */
	@Override
	public AScopeListRepresentation<ScopeListElement> inScopeJoin(AScopeListRepresentation<ScopeListElement> other) {
		ScopeListRepresentation newScopeList = new ScopeListRepresentation();

		this.forEach(scopeListElementX -> other.forEach(scopeListElementY -> {
			if (scopeListElementX.getTreeIndex() == scopeListElementY.getTreeIndex()
					&& scopeListElementX.getMatchLabel().equals(scopeListElementY.getMatchLabel())
					&& scopeListElementX.getScope().contains(scopeListElementY.getScope())) {
				int treeId = scopeListElementY.getTreeIndex();
				String joinedMatchLabel = scopeListElementY.getMatchLabel();
				Scope scope = scopeListElementY.getScope();
				newScopeList.add(new ScopeListElement(treeId, joinedMatchLabel, scope));
			}
		}));

		return newScopeList;
	}
}
