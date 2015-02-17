package org.processmining.plugins.InductiveMiner.graphs;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class GraphImplQuadratic<V> implements Graph<V> {
	private int vertices; //number of vertices
	private long[][] edges; //matrix of weights of edges
	private TObjectIntMap<V> v2index; //map V to its vertex index
	private V[] index2x; //map vertex index to V

	public GraphImplQuadratic(Class<?> clazz) {
		this(clazz, 1);
	}

	@SuppressWarnings("unchecked")
	public GraphImplQuadratic(Class<?> clazz, int initialSize) {
		vertices = 0;
		edges = new long[initialSize][initialSize];
		v2index = new TObjectIntHashMap<V>();

		index2x = (V[]) Array.newInstance(clazz, 0);
	}

	/**
	 * Add a vertex to the graph. Has no effect if the vertex is already in the
	 * graph.
	 * 
	 * @param x
	 */
	public void addVertex(V x) {
		if (!v2index.containsKey(x)) {
			int newNumber = vertices;
			v2index.put(x, newNumber);
			vertices++;
			increaseSizeTo(vertices);
			index2x[newNumber] = x;
		}
	}

	public void addVertices(Collection<V> xs) {
		increaseSizeTo(vertices + xs.size());
		for (V x : xs) {
			addVertex(x);
		}
	}

	public void addVertices(V[] xs) {
		increaseSizeTo(vertices + xs.length);
		for (V x : xs) {
			addVertex(x);
		}
	}

	public void addEdge(int source, int target, long weight) {
		edges[source][target] += weight;
	}

	public void addEdge(V source, V target, long weight) {
		edges[v2index.get(source)][v2index.get(target)] += weight;
	}

	public V getVertexOfIndex(int index) {
		return index2x[index];
	}

	public V[] getVertices() {
		return index2x;
	}

	public int[] getVertexIndices() {
		int[] result = new int[vertices];
		for (int i = 0; i < vertices; i++) {
			result[i] = i;
		}
		return result;
	}

	public int getNumberOfVertices() {
		return vertices;
	}

	/**
	 * Gives an iterable that iterates over all edges that have a weight > 0;
	 * The edges that returns are indices.
	 * 
	 * @return
	 */
	public Iterable<Integer> getEdges() {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new EdgeIterator();
			}
		};
	}

	/**
	 * Returns whether the graph contains an edge between source and target.
	 * 
	 * @return
	 */
	public boolean containsEdge(V source, V target) {
		return edges[v2index.get(source)][v2index.get(target)] > 0;
	}

	/**
	 * Returns whether the graph contains an edge between source and target.
	 * 
	 * @return
	 */
	public boolean containsEdge(int source, int target) {
		return edges[source][target] > 0;
	}

	/**
	 * Returns the vertex the edgeIndex comes from.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public V getEdgeSource(int edgeIndex) {
		return index2x[getEdgeSourceIndex(edgeIndex)];
	}

	public int getEdgeSourceIndex(int edgeIndex) {
		return edgeIndex / vertices;
	}

	/**
	 * Returns the vertex the edgeIndex points to.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public V getEdgeTarget(int edgeIndex) {
		return index2x[getEdgeTargetIndex(edgeIndex)];
	}

	public int getEdgeTargetIndex(int edgeIndex) {
		return edgeIndex % vertices;
	}

	/**
	 * Returns the weight of an edge.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public long getEdgeWeight(int edgeIndex) {
		return edges[getEdgeSourceIndex(edgeIndex)][getEdgeTargetIndex(edgeIndex)];
	}
	
	public long getEdgeWeight(int source, int target) {
		return edges[source][target];
	}

	/**
	 * Returns the weight of an edge.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public long getEdgeWeight(V source, V target) {
		return edges[v2index.get(source)][v2index.get(target)];
	}

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * target.
	 * 
	 * @param v
	 * @return
	 */
	public int[] getIncomingEdgesOf(V v) {
		int column = v2index.get(v);
		int count = 0;
		for (int row = 0; row < vertices; row++) {
			if (edges[row][column] > 0) {
				count++;
			}
		}
		int[] result = new int[count];
		count = 0;
		for (int row = 0; row < vertices; row++) {
			if (edges[row][column] > 0) {
				result[count] = row * vertices + column;
				count++;
			}
		}
		return result;
	}

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * source.
	 * 
	 * @param v
	 * @return
	 */
	public int[] getOutgoingEdgesOf(V v) {
		int row = v2index.get(v);
		int count = 0;
		for (int column = 0; column < vertices; column++) {
			if (edges[row][column] > 0) {
				count++;
			}
		}
		int[] result = new int[count];
		count = 0;
		for (int column = 0; column < vertices; column++) {
			if (edges[row][column] > 0) {
				result[count] = row * vertices + column;
				count++;
			}
		}
		return result;
	}

	/**
	 * Return an array of edgeIndex containing all edges of which v is a source
	 * or a target.
	 * 
	 * @param v
	 * @return
	 */
	public int[] getEdgesOf(V v) {
		return getEdgesOf(v2index.get(v));
	}

	public int[] getEdgesOf(int indexOfV) {
		//first count every edge, count a self-edge only in the row-run
		int count = 0;
		for (int column = 0; column < vertices; column++) {
			if (column != indexOfV && edges[indexOfV][column] > 0) {
				count++;
			}
		}
		for (int row = 0; row < vertices; row++) {
			if (edges[row][indexOfV] > 0) {
				count++;
			}
		}

		int[] result = new int[count];
		count = 0;
		for (int column = 0; column < vertices; column++) {
			if (column != indexOfV && edges[indexOfV][column] > 0) {
				result[count] = indexOfV * vertices + column;
				count++;
			}
		}
		for (int row = 0; row < vertices; row++) {
			if (edges[row][indexOfV] > 0) {
				result[count] = row * vertices + indexOfV;
				count++;
			}
		}
		return result;
	}

	/**
	 * Returns the weight of the edge with the highest weight.
	 * 
	 * @return
	 */
	public long getWeightOfHeaviestEdge() {
		long max = Long.MIN_VALUE;
		for (long[] v : edges) {
			for (long w : v) {
				if (w > max) {
					max = w;
				}
			}
		}
		return max;
	}

	private void increaseSizeTo(int size) {
		//see if the matrix can still accomodate this vertex
		if (size >= edges.length) {
			int newLength = edges.length * 2;
			while (size >= newLength) {
				newLength = newLength * 2;
			}
			long[][] newEdges = new long[newLength][newLength];

			//copy old values
			for (int i = 0; i < vertices; i++) {
				for (int j = 0; j < vertices; j++) {
					newEdges[i][j] = edges[i][j];
				}
			}
			edges = newEdges;
		}
		index2x = Arrays.copyOf(index2x, size);
	}

	public class EdgeIterator implements Iterator<Integer> {
		int currentIndex = 0;
		int nextIndex = 0;

		public EdgeIterator() {
			//walk to the first non-zero edge
			while (currentIndex < vertices * vertices && edges[currentIndex / vertices][currentIndex % vertices] <= 0) {
				currentIndex++;
			}
			//and to the next
			nextIndex = currentIndex;
			while (nextIndex < vertices * vertices && edges[nextIndex / vertices][nextIndex % vertices] <= 0) {
				nextIndex++;
			}
		}

		public void remove() {
			edges[getEdgeSourceIndex(currentIndex)][getEdgeTargetIndex(currentIndex)] = 0;
		}

		public Integer next() {
			currentIndex = nextIndex;
			nextIndex++;
			while (nextIndex < vertices * vertices && edges[nextIndex / vertices][nextIndex % vertices] <= 0) {
				nextIndex++;
			}
			return currentIndex;
		}

		public boolean hasNext() {
			return nextIndex < vertices * vertices;
		}
	};
}
