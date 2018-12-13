package treeminer.scopelists.elements;

import treeminer.Scope;

/**
 * An scope list element that additionally to the the simple scope list element
 * also keeps track of the match label and the scope of the last node of the
 * pattern.
 * 
 * @author Helena Graf
 *
 */
public class ScopeListElement extends SimpleScopeListElement {

	private String matchLabel;
	private Scope scope;

	/**
	 * Construct a new scope list element with the given parameters.
	 * 
	 * @param tree
	 *            the tree the pattern that this scope list element refers to occurs
	 *            in
	 * @param matchLabel
	 *            the match label for this pattern occurrence in the tree
	 * @param scope
	 *            the scope of the rightmost node of this pattern occurrence
	 */
	public ScopeListElement(int tree, String matchLabel, Scope scope) {
		super(tree);

		this.matchLabel = matchLabel;
		this.scope = scope;
	}

	/**
	 * Get the match label for this scope list element.
	 * 
	 * @return the match label
	 */
	public String getMatchLabel() {
		return matchLabel;
	}

	/**
	 * Get the scope of the rightmost node of this pattern.
	 * 
	 * @return the scope
	 */
	public Scope getScope() {
		return scope;
	}

	@Override
	public int compareTo(SimpleScopeListElement o) {
		int compare = super.compareTo(o);
		if (compare == 0) {
			return this.scope.compareTo(((ScopeListElement) o).scope);
		} else {
			return compare;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ScopeListElement)) {
			return false;
		}
		return this.compareTo((ScopeListElement) other) == 0;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = result * 31 + scope.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append(super.toString());
		builder.append(", ");
		builder.append(matchLabel);
		builder.append(", ");
		builder.append(scope);
		builder.append(")");
		return builder.toString();
	}
}
