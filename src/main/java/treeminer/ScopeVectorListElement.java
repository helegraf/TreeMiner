package treeminer;

import java.util.List;

public class ScopeVectorListElement extends SimpleScopeListElement {

	private List<Scope> scopes;
	
	public ScopeVectorListElement (int tree, List<Scope> scopes) {
		super(tree);
		
		this.scopes = scopes;
	}
	
	public List<Scope> getScopes() {
		return scopes;
	}
}
