package treeminer.scopelists.elements;

public class SimpleScopeListElement implements Comparable<SimpleScopeListElement> {

	private int tree;

	public SimpleScopeListElement(int tree) {
		this.tree = tree;
	}

	public int getTreeIndex() {
		return tree;
	}

	@Override
	public int compareTo(SimpleScopeListElement o) {
		return this.tree-o.tree;
	}

	@Override
	public boolean equals(Object other) {
		if (! (other instanceof SimpleScopeListElement)) {
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

}
