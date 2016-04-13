package org.processmining.plugins.InductiveMiner.graphs;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Components<V> {

	private int[] components;
	private int numberOfComponents;
	private TObjectIntHashMap<V> node2index;

	public Components(V[] nodes) {
		components = new int[nodes.length];
		numberOfComponents = nodes.length;
		for (int i = 0; i < components.length; i++) {
			components[i] = i;
		}

		node2index = new TObjectIntHashMap<V>();
		{
			int i = 0;
			for (V node : nodes) {
				node2index.put(node, i);
				i++;
			}
		}
	}

	/**
	 * Merge the components of the two nodes. If they are in the same component,
	 * runs in O(1). If they are not, runs in O(n) (n = number of nodes).
	 * 
	 * @param indexA
	 * @param indexB
	 */
	public void mergeComponentsOf(int indexA, int indexB) {
		int source = components[indexA];
		int target = components[indexB];

		if (source != target) {
			numberOfComponents--;
			for (int i = 0; i < components.length; i++) {
				if (components[i] == source) {
					components[i] = target;
				}
			}
		}
	}

	/**
	 * Merge the components of the two nodes. If they are in the same component,
	 * runs in O(1). If they are not, runs in O(n) (n = number of nodes). Use
	 * the integer variant if possible.
	 * 
	 * @param indexA
	 * @param indexB
	 */
	public void mergeComponentsOf(V nodeA, V nodeB) {
		mergeComponentsOf(node2index.get(nodeA), node2index.get(nodeB));
	}

	public boolean areInSameComponent(int nodeIndexA, int nodeIndexB) {
		return components[nodeIndexA] == components[nodeIndexB];
	}

	/**
	 * Preferably use the integer variant.
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	public boolean areInSameComponent(V nodeA, V nodeB) {
		return areInSameComponent(node2index.get(nodeA), node2index.get(nodeB));
	}

	//	
	//	public int getComponentOf(int node) {
	//		return components[node];
	//	}
	//	
	//	public int getComponentOf(V node) {
	//		return getComponentOf(node2index.get(node));
	//	}

	public int getNumberOfComponents() {
		return numberOfComponents;
	}

	public List<Set<V>> getComponents() {
		final List<Set<V>> result = new ArrayList<Set<V>>(numberOfComponents);

		//prepare a hashmap of components
		final TIntIntHashMap component2componentIndex = new TIntIntHashMap();
		int highestComponentIndex = 0;
		for (int node = 0; node < components.length; node++) {
			int component = components[node];
			if (!component2componentIndex.contains(component)) {
				component2componentIndex.put(component, highestComponentIndex);
				highestComponentIndex++;
				result.add(new THashSet<V>());
			}
		}

		//put each node in its component
		node2index.forEachEntry(new TObjectIntProcedure<V>() {
			public boolean execute(V node, int nodeIndex) {
				int component = components[nodeIndex];
				int componentIndex = component2componentIndex.get(component);
				result.get(componentIndex).add(node);
				return true;
			}
		});

		return result;
	}
}