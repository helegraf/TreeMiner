package treeminer.scopelists.elements;

import java.util.List;

import treeminer.Scope;

/**
 * A scope list element that additionally to the simple scope list element keeps
 * track of the node scope on the rightmost path of the occurring pattern.
 * 
 * @author Helena Graf
 *
 */
public class ScopeVectorListElement extends SimpleScopeListElement {

	private List<Scope> scopes;

	/**
	 * Construct a new scope vector list element with the given parameters.
	 * 
	 * @param tree
	 *            the tree that the pattern occurs in
	 * @param scopes
	 *            the node scopes of the rightmost path of the pattern
	 */
	public ScopeVectorListElement(int tree, List<Scope> scopes) {
		super(tree);
		this.scopes = scopes;
	}

	/**
	 * Get the node scopes on the rightmost path of the pattern.
	 * 
	 * @return the node scopes
	 */
	public List<Scope> getScopes() {
		return scopes;
	}

	@Override
	public int compareTo(SimpleScopeListElement o) {
		int compare = super.compareTo(o);
		if (compare == 0) {
			ScopeVectorListElement other = (ScopeVectorListElement) o;
			for (int i = 0; i < this.scopes.size(); i++) {
				if (other.scopes.size() <= i + 1) {
					return -1;
				} else {
					int compareScopes = this.scopes.get(i).compareTo(other.scopes.get(i));
					if (compareScopes != 0) {
						return compareScopes;
					}
				}
			}
			return 0;
		} else {
			return compare;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ScopeVectorListElement)) {
			return false;
		}
		return this.compareTo((ScopeVectorListElement) other) == 0;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = result * 31 + scopes.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append(super.toString());
		builder.append(", ");
		builder.append(scopes);
		builder.append(")");
		return builder.toString();
	}
}
