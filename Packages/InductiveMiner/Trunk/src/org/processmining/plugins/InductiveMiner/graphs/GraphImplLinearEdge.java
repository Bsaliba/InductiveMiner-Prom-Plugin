package org.processmining.plugins.InductiveMiner.graphs;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GraphImplLinearEdge<V> implements Graph<V> {

	private final TObjectIntMap<V> v2index; //map V to its vertex index
	private final ArrayList<V> index2v; //map vertex index to V

	private final TIntArrayList sources;
	private final TIntArrayList targets;
	private final TLongArrayList weights;
	
	private final Class<?> clazz;

	public GraphImplLinearEdge(Class<?> clazz) {
		v2index = new TObjectIntHashMap<V>();
		index2v = new ArrayList<>();

		sources = new TIntArrayList();
		targets = new TIntArrayList();
		weights = new TLongArrayList();
		this.clazz = clazz;
	}

	public void addVertex(V x) {
		if (!v2index.containsKey(x)) {
			int newNumber = index2v.size();
			v2index.put(x, newNumber);
			index2v.add(x);
		}
	}

	public void addVertices(Collection<V> xs) {
		for (V x : xs) {
			addVertex(x);
		}
	}

	public void addVertices(V[] xs) {
		for (V x : xs) {
			addVertex(x);
		}
	}

	public void addEdge(int source, int target, long weight) {
		
		//idea: keep the sources sorted
		int from = sources.binarySearch(source);
		if (from >= 0) {
			//the source is already present
			for (int e = from; e < sources.size(); e++) {
				if (sources.get(e) != source) {
					break;
				}
				if (targets.get(e) == target) {
					weights.set(e, weights.get(e) + weight);
					return;
				}
			}
			//binary search might end up in the middle; walk back as well
			for (int e = from; e >= 0 ; e--) {
				if (sources.get(e) != source) {
					break;
				}
				if (targets.get(e) == target) {
					weights.set(e, weights.get(e) + weight);
					return;
				}
			}
		} else {
			from = -from - 1;
		}
		
		sources.insert(from, source);
		targets.insert(from, target);
		weights.insert(from, weight);
	}

	public void addEdge(V source, V target, long weight) {
		addEdge(v2index.get(source), v2index.get(target), weight);
	}

	public V getVertexOfIndex(int index) {
		return index2v.get(index);
	}

	public V[] getVertices() {
		@SuppressWarnings("unchecked")
		V[] result = (V[]) Array.newInstance(clazz, index2v.size());
		return index2v.toArray(result);
	}

	public int[] getVertexIndices() {
		int[] result = new int[index2v.size()];
		for (int i = 0; i < index2v.size(); i++) {
			result[i] = i;
		}
		return result;
	}

	public int getNumberOfVertices() {
		return index2v.size();
	}

	public Iterable<Integer> getEdges() {
		List<Integer> ints = new ArrayList<Integer>();
		for (int i = 0; i < sources.size(); i++) {
			ints.add(i);
		}
		return ints;
	}

	public boolean containsEdge(V source, V target) {
		return containsEdge(v2index.get(source), v2index.get(target));
	}

	public boolean containsEdge(int source, int target) {
		for (int e = 0; e < sources.size(); e++) {
			if (sources.get(e) == source && targets.get(e) == target) {
				return true;
			}
		}
		return false;
	}

	public V getEdgeSource(int edgeIndex) {
		return index2v.get(sources.get(edgeIndex));
	}

	public int getEdgeSourceIndex(int edgeIndex) {
		return sources.get(edgeIndex);
	}

	public V getEdgeTarget(int edgeIndex) {
		return index2v.get(targets.get(edgeIndex));
	}

	public int getEdgeTargetIndex(int edgeIndex) {
		return targets.get(edgeIndex);
	}

	public long getEdgeWeight(int edgeIndex) {
		return weights.get(edgeIndex);
	}

	public long getEdgeWeight(int source, int target) {
		int from = sources.binarySearch(source);
		if (from < 0) {
			return 0;
		}
		for (int e = from; e < sources.size(); e++) {
			if (sources.get(e) != source) {
				return 0;
			}
			if (targets.get(e) == target) {
				return weights.get(e);
			}
		}
		return 0;
	}

	public long getEdgeWeight(V source, V target) {
		return getEdgeWeight(v2index.get(source), v2index.get(target));
	}

	public int[] getIncomingEdgesOf(V v) {
		TIntArrayList result = new TIntArrayList();
		int target = v2index.get(v);
		for (int e = 0; e < sources.size(); e++) {
			if (targets.get(e) == target) {
				result.add(e);
			}
		}

		int[] result2 = new int[result.size()];
		for (int i = 0; i < result.size(); i++) {
			result2[i] = result.get(i);
		}
		return result2;
	}

	public int[] getOutgoingEdgesOf(V v) {
		TIntArrayList result = new TIntArrayList();
		int source = v2index.get(v);
		
		int from = sources.binarySearch(source);
		if (from >= 0) {
			for (int e = from; e < sources.size(); e++) {
				if (sources.get(e) == source) {
					result.add(e);
				} else {
					break;
				}
			}
		}

		int[] result2 = new int[result.size()];
		for (int i = 0; i < result.size(); i++) {
			result2[i] = result.get(i);
		}
		return result2;
	}

	public int[] getEdgesOf(V v) {
		return getEdgesOf(v2index.get(v));
	}

	public int[] getEdgesOf(int indexOfV) {
		TIntArrayList result = new TIntArrayList();
		//no point in binary search here
		for (int e = 0; e < sources.size(); e++) {
			if (targets.get(e) == indexOfV || sources.get(e) == indexOfV) {
				result.add(e);
			}
		}

		int[] result2 = new int[result.size()];
		for (int i = 0; i < result.size(); i++) {
			result2[i] = result.get(i);
		}
		return result2;
	}

	public long getWeightOfHeaviestEdge() {
		long max = 0;
		for (int e = 0; e < weights.size(); e++) {
			max = Math.max(max, weights.get(e));
		}
		return max;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < sources.size();i++) {
			result.append(sources.get(i) + "->" + targets.get(i) + "x" + weights.get(i));
			result.append(", ");
		}
		return result.toString();
	}
}
