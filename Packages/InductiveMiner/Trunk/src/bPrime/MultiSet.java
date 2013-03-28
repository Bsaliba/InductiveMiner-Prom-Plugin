package bPrime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class MultiSet<X> implements Iterable<X> {
	
	private HashMap<X, Integer> cardinalities;
	private int size;
	
	public MultiSet() {
		//use a linked hash map here, as it provides O(1) iteration complexity
		cardinalities = new LinkedHashMap<X, Integer>();
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
	
	public boolean addAll(MultiSet<X> collection) {
		for (X element : collection) {
			add(element, collection.getCardinalityOf(element));
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
	
	/*
	 * 
	 * Iterator over the elements of the multiset as if it were a set
	 * Get cardinalities using getCardinality().
	 * 
	 */
	public Iterator<X> iterator() {
		
		Iterator<X> it = new Iterator<X>() {

			private Iterator<Entry<X, Integer>> it2 = cardinalities.entrySet().iterator();

            @Override
            public boolean hasNext() {
            	return it2.hasNext();
            }

            @Override
            public X next() {
            	Entry<X, Integer> n = it2.next();
                return n.getKey();
            }

			public void remove() {
				it2.remove();
			}
        };
        return it;
	}
	
	public String toString() {
		return cardinalities.toString();
	}
}
