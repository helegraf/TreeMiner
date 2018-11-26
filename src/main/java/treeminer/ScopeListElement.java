package treeminer;

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
}
