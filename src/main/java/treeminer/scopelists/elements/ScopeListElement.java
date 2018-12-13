package treeminer.scopelists.elements;

import treeminer.Scope;

public class ScopeListElement extends SimpleScopeListElement {
	
	private String matchLabel;
	private Scope scope;
	
	public ScopeListElement(int tree, String matchLabel, Scope scope) {
		super (tree);
		
		this.matchLabel = matchLabel;
		this.scope = scope;
	}
	
	public String getMatchLabel() {
		return matchLabel;
	}
	
	public Scope getScope() {
		return scope;
	}
	
	@Override
	public int compareTo(SimpleScopeListElement o) {
		int compare = super.compareTo(o);
		if (compare == 0) {
			return this.scope.compareTo(((ScopeListElement)o).scope);
		} else {
			return compare;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (! (other instanceof ScopeListElement)) {
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
}
