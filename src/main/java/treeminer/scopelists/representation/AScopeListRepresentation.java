package treeminer.scopelists.representation;

import java.util.Iterator;
import java.util.TreeSet;

import treeminer.scopelists.elements.SimpleScopeListElement;

public abstract class AScopeListRepresentation<T extends SimpleScopeListElement> implements Iterable<T> {

	private TreeSet<T> elements = new TreeSet<>();
	public abstract AScopeListRepresentation<T> inScopeJoin(AScopeListRepresentation<T> other);
	public abstract AScopeListRepresentation<T> outScopeJoin (AScopeListRepresentation<T> other, int attachedTo);
	
	@Override
	public Iterator<T> iterator() {
		return elements.iterator();		
	}
	
	public void add(T element) {
		elements.add(element);
	}
	
	public int size() {
		return elements.size();
	}
	
	public boolean isEmpty() {
		return elements.isEmpty();
	}
}
