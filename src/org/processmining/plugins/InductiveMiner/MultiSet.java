package org.processmining.plugins.InductiveMiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class MultiSet<X> implements Iterable<X> {
	
	protected HashMap<X, Long> cardinalities;
	protected long size;
	
	public MultiSet() {
		//use a linked hash map here, as it provides O(1) iteration complexity
		cardinalities = new LinkedHashMap<X, Long>();
		size = 0;
	}
	
	public boolean add(X element) {
		add(element, 1);
		return true;
	}
	
	public boolean equals(Object a) {
		if (!(a instanceof MultiSet<?>)) {
			return false;
		}
		
		MultiSet<?> aM = (MultiSet<?>) a;
		
		for (Object e : this) {
			if (aM.getCardinalityOf(e) != this.getCardinalityOf(e)) {
				return false;
			}
		}
		
		for (Object e : aM) {
			if (aM.getCardinalityOf(e) != this.getCardinalityOf(e)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean add(X element, long cardinality) {
		if (!cardinalities.containsKey(element)) {
			cardinalities.put(element, cardinality);
		} else {
			Long newCardinality = cardinalities.get(element) + cardinality;
			cardinalities.put(element, newCardinality);
		}
		size += cardinality;
		return true;
	}
	
	public boolean remove(X element, Long cardinality) {
		assert(cardinality >= 0);
		
		Long oldCardinality = getCardinalityOf(element);
		if (oldCardinality - cardinality > 0) {
			cardinalities.put(element, cardinalities.get(element) - cardinality);
			size -= cardinality;
		} else {
			cardinalities.remove(element);
			size -= oldCardinality;
		}
		
		return true;
	}
	
	public boolean remove(X element) {
		return remove(element, getCardinalityOf(element));
	}
	
	public boolean addAll(Collection<X> collection) {
		for (X element : collection) {
			add(element);
		}
		return true;
	}
	
	public boolean addAll(Collection<X> collection, long cardinality) {
		for (X element : collection) {
			add(element, cardinality);
		}
		return true;
	}
	
	public boolean addAll(MultiSet<X> collection) {
		for (X element : collection) {
			add(element, collection.getCardinalityOf(element));
		}
		return true;
	}
	
	public void empty() {
		cardinalities = new LinkedHashMap<X, Long>();
		size = 0;
	}
	
	public long size() {
		return size;
	}
	
	public int setSize() {
		return cardinalities.keySet().size();
	}
	
	public Set<X> toSet() {
		return cardinalities.keySet();
	}
	
	public boolean contains(Object a) {
		return cardinalities.containsKey(a);
	}
	
	public long getCardinalityOf(Object e) {
		if (contains(e)) {
			return cardinalities.get(e);
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

			private Iterator<Entry<X, Long>> it2 = cardinalities.entrySet().iterator();

            @Override
            public boolean hasNext() {
            	return it2.hasNext();
            }

            @Override
            public X next() {
            	Entry<X, Long> n = it2.next();
                return n.getKey();
            }

			public void remove() {
				it2.remove();
			}
        };
        return it;
	}
	
	public MultiSet<X> copy() {
		MultiSet<X> result = new MultiSet<X>();
		for (X element : cardinalities.keySet()) {
			result.add(element, cardinalities.get(element));
		}
		return result;
	}
	
	private class CardinalityComparator implements Comparator<X> {
		public int compare(X arg0, X arg1) {
			return Long.valueOf(getCardinalityOf(arg0)).compareTo(Long.valueOf(getCardinalityOf(arg1)));
		}
	}
	
	public List<X> sortByCardinality() {
		List<X> result = new ArrayList<X>(toSet());
		Collections.sort(result, new CardinalityComparator());
		return result;
	}
	
	public String toString() {
		return cardinalities.toString();
	}
}
