package treeminer.scopelists.representation;

import java.util.Iterator;
import java.util.TreeSet;

import treeminer.scopelists.elements.SimpleScopeListElement;

/**
 * An abstract representation of a scope list that keeps track of pattern
 * occurrences.
 * 
 * @author Helena Graf
 *
 * @param <T>
 *            the type of scope list element contained in the list
 */
public abstract class AScopeListRepresentation<T extends SimpleScopeListElement> implements Iterable<T> {

	private TreeSet<T> elements = new TreeSet<>();

	/**
	 * Perform an in scope join with the other scope list.
	 * 
	 * @param other
	 *            the other scope list representation
	 * @return a new scope list that is the result of the join
	 */
	public abstract AScopeListRepresentation<T> inScopeJoin(AScopeListRepresentation<T> other);

	/**
	 * Perform an out scope join with the other scope list.
	 * 
	 * @param other
	 *            the other scope list representation
	 * @return a new scope list that is the result of the join
	 */
	public abstract AScopeListRepresentation<T> outScopeJoin(AScopeListRepresentation<T> other, int attachedTo);

	/**
	 * Add the given element to this scope list representation.
	 * 
	 * @param element
	 *            the element to add
	 */
	public void add(T element) {
		elements.add(element);
	}

	/**
	 * Get the size of this scope list representation.
	 * 
	 * @return the size
	 */
	public int size() {
		return elements.size();
	}

	/**
	 * Get whether this scope list if empty.
	 * 
	 * @return if this scope list is empty
	 */
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	@Override
	public String toString() {
		return elements.toString();
	}
}
