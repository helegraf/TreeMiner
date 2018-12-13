package treeminer;

/**
 * Represents the scope of a node in a tree.
 * 
 * <p>
 * The scope of a node is an interval [i,j] where i is the depth-first pre-order
 * traversal number of the node, and j is that number for the rightmost node
 * under the node (or i if it has no children).
 * </p>
 * 
 * @author Helena Graf
 *
 */
public class Scope implements Comparable<Scope> {

	private int upperBound;
	private int lowerBound;

	/**
	 * Creates a new Scope and sets the bounds as given.
	 * 
	 * @param lowerBound
	 *            The upper bound of the scope
	 * @param upperBound
	 *            The lower bound of the scope
	 */
	public Scope(int lowerBound, int upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	/**
	 * Creates a new Scopes where lower bound = upper bound = -1 (an invalid value).
	 */
	public Scope() {
		this.lowerBound = -1;
		this.upperBound = -1;
	}

	/**
	 * Checks whether this Scope strictly contains the other Scope.
	 * 
	 * @param other
	 *            The Scope to compare with
	 * @return Whether this scope strictly contains the other Scope
	 */
	public boolean contains(Scope other) {
		if (this.lowerBound <= other.lowerBound && other.upperBound <= this.upperBound) {
			return !(other.upperBound == this.upperBound && other.lowerBound == this.lowerBound);
		} else {
			return false;
		}
	}

	/**
	 * Checks whether this Scope appears strictly before the other Scope.
	 * 
	 * @param other
	 *            The Scope to compare with
	 * @return Whether this Scope appears strictly before the other Scope
	 */
	public boolean isStrictlyLessThan(Scope other) {
		return (this.upperBound < other.lowerBound);
	}

	/**
	 * Get the upper bound of this scope.
	 * 
	 * @return The upper bound of this scope
	 */
	public int getUpperBound() {
		return upperBound;
	}

	/**
	 * Set the upper bound of this scope.
	 * 
	 * @param upperBound
	 *            The new value for the upper bound
	 */
	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * Get the lower bound of this scope.
	 * 
	 * @return The lower bound of this scope
	 */
	public int getLowerBound() {
		return lowerBound;
	}

	/**
	 * Set the lower bound of this scope.
	 * 
	 * @param lowerBound
	 *            The new value for the lower bound
	 */
	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append(lowerBound);
		builder.append(", ");
		builder.append(upperBound);
		builder.append(")");
		return builder.toString();
	}

	@Override
	public int compareTo(Scope o) {
		if (this.lowerBound < o.lowerBound) {
			return -1;
		} else {
			if (this.lowerBound == o.lowerBound) {
				if (this.upperBound < o.lowerBound) {
					return -1;
				} else {
					if (this.upperBound == o.upperBound) {
						return 0;
					} else {
						return 1;
					}
				}
			} else {
				return 1;
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !o.getClass().equals(this.getClass())) {
			return false;
		}

		Scope other = (Scope) o;

		return other.upperBound == this.upperBound && other.lowerBound == this.lowerBound;
	}

	@Override
	public int hashCode() {
		int result = 42;
		result = result * 37 + this.upperBound;
		result = result * 37 + this.lowerBound;
		return result;
	}

	@Override
	public Scope clone() {
		return new Scope(this.lowerBound, this.upperBound);
	}
}
