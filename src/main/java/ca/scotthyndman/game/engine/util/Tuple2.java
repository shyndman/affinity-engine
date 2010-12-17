package ca.scotthyndman.game.engine.util;

/**
 * Represents two objects, related or unrelated, grouped together in a single object.
 * 
 * @author Scott Hyndman
 */
public class Tuple2<T1, T2> {
	public T1 first;
	public T2 second;

	/**
	 * Constructs a new tuple with 2 values.
	 * 
	 * @param first
	 * @param second
	 */
	public Tuple2(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	/* --- Overrides --- */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Tuple2<T1, T2> other = (Tuple2<T1, T2>) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}

}