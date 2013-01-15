package bPrime;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Sets {
	public static <X> Set<X> extend(Set<X> base, X node) {
		Set<X> result = new HashSet<X>(base);
		result.add(node);
		return result;
	}
	
	public static <X> Set<X> difference(Set<X> a, Set<X> b) {
		Set<X> result = new HashSet<X>(a);
		result.removeAll(b);
		return result;
	}
	
	public static <X> Set<X> intersection(Set<X> a, Set<X> b) {
		Set<X> result = new HashSet<X>(a);
		result.retainAll(b);
		return result;
	}
	
	public static <X> Set<X> flatten(Set<Set<X>> set) {
		Set<X> result = new HashSet<X>();
		for (Set<X> node : set) {
			result.addAll(node);
		}
		return result;
	}
	
	public static <X> Set<X> complement(Set<X> set, Set<X> universe) {
		Set<X> result = new HashSet<X>(universe);
		result.removeAll(set);
		return result;
	}
	
	public static <X> Set<X> findComponentWith(Collection<Set<X>> Components, X c) {
		Set<X> result = null;
		for (Set<X> SCC : Components) {
			if (SCC.contains(c)) {
				result = SCC;
			}
		}
		return result;
	}
	
	public static <X> String implode(Collection<X> input, String glueString) {
		String output = "";
		boolean first = true;
		if (input.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (X e : input) {
				if (first) {
					first = false;
				} else {
					sb.append(glueString);
				}
				sb.append(e.toString());
			}
			output = sb.toString();
		}
		return output;
	}
}
