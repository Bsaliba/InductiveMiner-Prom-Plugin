package org.processmining.plugins.InductiveMiner.graphs;

import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections15.IteratorUtils;

public class GraphImplLinearEdgeImportOptimised<V> implements Graph<V> {

	private final TObjectIntMap<V> v2index; //map V to its vertex index
	private final ArrayList<V> index2v; //map vertex index to V

	private final TLongLongMap edges;
	
	private final Class<?> clazz;

	public GraphImplLinearEdgeImportOptimised(Class<?> clazz) {
		v2index = new TObjectIntHashMap<V>();
		index2v = new ArrayList<>();

		edges = new TLongLongHashMap();
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
	
	private long getEdgeIndex(int source, int target) {
        return  (((long) source) << 32) | (target & 0xFFFFFFFFL);
	}

	public void addEdge(int source, int target, long weight) {
		long edgeIndex = getEdgeIndex(source, target);
		edges.adjustOrPutValue(edgeIndex, weight, weight);
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

	public Iterable<Long> getEdges() {
		return new Iterable<Long>() {
			public Iterator<Long> iterator() {
				return IteratorUtils.arrayIterator(edges.keys());
			}
		};
		
	}

	public boolean containsEdge(V source, V target) {
		return containsEdge(v2index.get(source), v2index.get(target));
	}

	public boolean containsEdge(int source, int target) {
		return edges.containsKey(getEdgeIndex(source, target));
	}

	public V getEdgeSource(long edgeIndex) {
		return index2v.get(getEdgeSourceIndex(edgeIndex));
	}

	public int getEdgeSourceIndex(long edgeIndex) {
		return (int) (edgeIndex >> 32);
	}

	public V getEdgeTarget(long edgeIndex) {
		return index2v.get(getEdgeTargetIndex(edgeIndex));
	}

	public int getEdgeTargetIndex(long edgeIndex) {
		return (int) (edgeIndex & 0xFFFFFFFFL);
	}

	public long getEdgeWeight(long edgeIndex) {
		return edges.get(edgeIndex);
	}

	public long getEdgeWeight(int source, int target) {
		return edges.get(getEdgeIndex(source, target));
	}

	public long getEdgeWeight(V source, V target) {
		return getEdgeWeight(v2index.get(source), v2index.get(target));
	}

	public Iterable<Long> getIncomingEdgesOf(V v) {
		throw new RuntimeException("not implemented");
	}

	public Iterable<Long> getOutgoingEdgesOf(V v) {
		throw new RuntimeException("not implemented");
	}
	
	public Iterable<Long> getOutgoingEdgesOf(int v) {
		throw new RuntimeException("not implemented");
	}

	public Iterable<Long> getEdgesOf(V v) {
		return getEdgesOf(v2index.get(v));
	}

	public Iterable<Long> getEdgesOf(int indexOfV) {
		throw new RuntimeException("not implemented");
	}

	public long getWeightOfHeaviestEdge() {
		throw new RuntimeException("not implemented");
	}

	public int getIndexOfVertex(V e) {
		return v2index.get(e);
	}
}
