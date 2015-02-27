package org.processmining.plugins.InductiveMiner.graphs;

import java.util.Collection;

public interface Graph<V> {

	/**
	 * Add a vertex to the graph. Has no effect if the vertex is already in the
	 * graph.
	 * 
	 * @param x
	 */
	public void addVertex(V x);

	public void addVertices(Collection<V> xs);
	
	public void addVertices(V[] xs);

	public void addEdge(int source, int target, long weight);

	public void addEdge(V source, V target, long weight);

	public V getVertexOfIndex(int index);

	public V[] getVertices();

	public int[] getVertexIndices();

	public int getNumberOfVertices();

	/**
	 * Gives an iterable that iterates over all edges that have a weight > 0;
	 * The edges that are returned are indices.
	 * 
	 * @return
	 */
	public Iterable<Long> getEdges() ;

	/**
	 * Returns whether the graph contains an edge between source and target.
	 * 
	 * @return
	 */
	public boolean containsEdge(V source, V target);

	/**
	 * Returns whether the graph contains an edge between source and target.
	 * 
	 * @return
	 */
	public boolean containsEdge(int source, int target);

	/**
	 * Returns the vertex the edgeIndex comes from.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public V getEdgeSource(long edgeIndex);

	public int getEdgeSourceIndex(long edgeIndex);

	/**
	 * Returns the vertex the edgeIndex points to.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public V getEdgeTarget(long edgeIndex);

	/**
	 * Returns the index of the vertex the edgeIndex points to.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public int getEdgeTargetIndex(long edgeIndex);

	/**
	 * Returns the weight of an edge.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public long getEdgeWeight(long edgeIndex);
	
	/**
	 * Returns the weight of an edge between source and target
	 * @param source
	 * @param target
	 * @return
	 */
	public long getEdgeWeight(int source, int target);

	/**
	 * Returns the weight of an edge.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public long getEdgeWeight(V source, V target);

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * target.
	 * 
	 * @param v
	 * @return
	 */
	public Iterable<Long> getIncomingEdgesOf(V v);

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * source.
	 * 
	 * @param v
	 * @return
	 */
	public Iterable<Long> getOutgoingEdgesOf(V v);

	/**
	 * Return an array of edgeIndex containing all edges of which v is a source
	 * or a target.
	 * 
	 * @param v
	 * @return
	 */
	public Iterable<Long> getEdgesOf(V v) ;

	public Iterable<Long> getEdgesOf(int indexOfV) ;

	/**
	 * Returns the weight of the edge with the highest weight.
	 * 
	 * @return
	 */
	public long getWeightOfHeaviestEdge();

	/**
	 * 
	 * @param e
	 * @return the index of the given vertex
	 */
	public int getIndexOfVertex(V v);
}
