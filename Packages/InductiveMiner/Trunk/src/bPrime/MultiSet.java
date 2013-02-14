package bPrime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class MultiSet<X> implements Iterable<Pair<X, Integer>> {
	
	private HashMap<X, Integer> cardinalities;
	private int size;
	
	public MultiSet() {
		cardinalities = new HashMap<X, Integer>();
		size = 0;
	}
	
	public boolean add(X element) {
		add(element, 1);
		return true;
	}
	
	public boolean add(X element, Integer cardinality) {
		if (!cardinalities.containsKey(element)) {
			cardinalities.put(element, cardinality);
		} else {
			Integer newCardinality = cardinalities.get(element) + cardinality;
			cardinalities.put(element, newCardinality);
		}
		size += cardinality;
		return true;
	}
	
	public boolean addAll(Collection<X> collection) {
		for (X element : collection) {
			add(element);
		}
		return true;
	}
	
	public int size() {
		return size;
	}
	
	public Set<X> toSet() {
		return cardinalities.keySet();
	}
	
	public boolean contains(X element) {
		return cardinalities.containsKey(element);
	}
	
	public Integer getCardinalityOf(X element) {
		if (contains(element)) {
			return cardinalities.get(element);
		} else {
			return 0;
		}
	}

	public Iterator<Pair<X, Integer>> iterator() {
		Iterator<Pair<X, Integer>> it = new Iterator<Pair<X, Integer>>() {

			private Iterator<Entry<X, Integer>> it = cardinalities.entrySet().iterator();

            @Override
            public boolean hasNext() {
            	return it.hasNext();
            }

            @Override
            public Pair<X, Integer> next() {
            	Entry<X, Integer> n = it.next();
                return new Pair<X, Integer>(n.getKey(), n.getValue());
            }

			public void remove() {
				it.remove();
			}
        };
        return it;
	}
	
	public String toString() {
		return cardinalities.toString();
	}
}
