package treeminer.scopelists.elements;

/**
 * Represents a simple scope list element that only keeps track of the tree a
 * pattern occurs in.
 * 
 * @author Helena Graf
 *
 */
public abstract class SimpleScopeListElement implements Comparable<SimpleScopeListElement> {

	private int tree;

	/**
	 * Constructs a new simple scope list element that keeps track of the tree the
	 * patter this element references occurs in.
	 * 
	 * @param tree
	 *            the tree this pattern occurs in
	 */
	public SimpleScopeListElement(int tree) {
		this.tree = tree;
	}

	/**
	 * Get the tree this pattern occurs in.
	 * 
	 * @return the tree
	 */
	public int getTreeIndex() {
		return tree;
	}

	@Override
	public int compareTo(SimpleScopeListElement o) {
		return this.tree - o.tree;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SimpleScopeListElement)) {
			return false;
		}
		return this.compareTo((SimpleScopeListElement) other) == 0;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + tree;
		return result;
	}

	@Override
	public String toString() {
		return String.valueOf(tree);
	}

}
