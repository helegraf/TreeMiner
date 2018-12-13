package treeminer.scopelists.elements;

import java.util.List;

import treeminer.Scope;

public class ScopeVectorListElement extends SimpleScopeListElement {

	private List<Scope> scopes;
	
	public ScopeVectorListElement (int tree, List<Scope> scopes) {
		super(tree);
		
		this.scopes = scopes;
	}
	
	public List<Scope> getScopes() {
		return scopes;
	}
	
	@Override
	public int compareTo(SimpleScopeListElement o) {
		int compare = super.compareTo(o);
		if (compare == 0) {
			ScopeVectorListElement other = (ScopeVectorListElement)o;
			for (int i = 0; i < this.scopes.size(); i++) {
				if (other.scopes.size() <= i +1) {
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
		if (! (other instanceof ScopeVectorListElement)) {
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
}
