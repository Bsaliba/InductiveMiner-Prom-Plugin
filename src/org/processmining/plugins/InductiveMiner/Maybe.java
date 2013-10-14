package org.processmining.plugins.InductiveMiner;

public class Maybe<X> {
	private X x;

	public Maybe(X x) {
		this.x = x;
	}

	public X get() {
		return x;
	}

	@Override
	public int hashCode() {
		if (x != null) {
			return x.hashCode();
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (o instanceof Maybe<?>) {
			Maybe<?> o2 = (Maybe<?>) o;
			if (get() == null && o2.get() == null) {
				return true;
			} else if (get() == null || o2.get() == null) {
				return false;
			}
			return this.get().equals(o2.get());
		}
		return false;
	}
}
