package treeminer;

public class Scope implements Comparable<Scope> {

	private int upperBound; 
	private int lowerBound;

	public Scope(int lowerBound, int upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	public Scope() {
		this.lowerBound = -1;
		this.upperBound = -1;
	}
	
	public boolean contains (Scope other) {
		if (this.lowerBound <= other.lowerBound && other.upperBound <= this.upperBound) {
			if (other.upperBound == this.upperBound && other.lowerBound == this.lowerBound) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	public boolean isStrictlyLessThan (Scope other) {
		return (this.upperBound < other.lowerBound);
	}

	public int getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}

	public int getLowerBound() {
		return lowerBound;
	}

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
}
