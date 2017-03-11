package org.processmining.plugins.InductiveMiner.dfgOnly;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;

public interface Dfg {

	/**
	 * Adds an activity to the Dfg.
	 * 
	 * @param activity
	 * @return The index of the inserted activity.
	 */
	public int addActivity(XEventClass activity);

	@Deprecated
	public Graph<XEventClass> getDirectlyFollowsGraph();

	public int[] getActivityIndices();

	/**
	 * 
	 * @return The concurrency graph. Do not edit directly.
	 */
	@Deprecated
	public Graph<XEventClass> getConcurrencyGraph();

	/**
	 * 
	 * @return The number of empty (epsilon) traces.
	 */
	public long getNumberOfEmptyTraces();

	/**
	 * Set the number of empty (epsilon) traces.
	 * 
	 * @param numberOfEmptyTraces
	 */
	public void setNumberOfEmptyTraces(long numberOfEmptyTraces);

	/**
	 * Adds empty traces.
	 * 
	 * @param cardinality
	 */
	public void addEmptyTraces(long cardinality);

	public void addDirectlyFollowsEdge(final XEventClass source, final XEventClass target, final long cardinality);

	public void addParallelEdge(final XEventClass a, final XEventClass b, final long cardinality);

	public void addStartActivity(XEventClass activity, long cardinality);

	public void addEndActivity(XEventClass activity, long cardinality);

	/**
	 * Adds a directly follows graph edge (in each direction) for each parallel
	 * edge.
	 */
	public void collapseParallelIntoDirectly();

	/**
	 * 
	 * @return An unconnected copy of the Dfg.
	 */
	public Dfg clone();

	/**
	 * 
	 * @return The number of activities (as if they were a set).
	 */
	public int getNumberOfActivities();

	/**
	 * 
	 * @param activity
	 * @return The index of the given activity, or -1 if it does not exist.
	 */
	public int getIndexOfActivity(XEventClass activity);

	/**
	 * 
	 * @param activityIndex
	 * @return The activity of the given index.
	 */
	public XEventClass getActivityOfIndex(int activityIndex);

	public boolean hasStartActivities();

	public boolean hasEndActivities();

	/**
	 * 
	 * @return The size of the set of start activities.
	 */
	public int getNumberOfStartActivitiesAsSet();

	/**
	 * 
	 * @return The size of the set of end activities.
	 */
	public int getNumberOfEndActivitiesAsSet();

	/**
	 * 
	 * @param activityIndex
	 * @return Whether the activity with the given index is a start activity.
	 */
	public boolean isStartActivity(int activityIndex);

	/**
	 * 
	 * @param activity
	 * @return Whether the activity is a start activity. If possible, use the
	 *         integer-variant.
	 */
	public boolean isStartActivity(XEventClass activity);

	/**
	 * 
	 * @param activityIndex
	 * @return How often the activity was a start activity.
	 */
	public long getStartActivityCardinality(int activityIndex);

	/**
	 * 
	 * @param activity
	 * @return How often the activity was a start activity. Use the integer
	 *         variant if possible.
	 */
	public long getStartActivityCardinality(XEventClass activity);

	/**
	 * 
	 * @return The number of occurrences of the activity that occurs the most as
	 *         a start activity.
	 */
	public long getMostOccurringStartActivityCardinality();

	/**
	 * 
	 * @param activityIndex
	 * @return Whether the activity with the given index is a end activity.
	 */
	public boolean isEndActivity(int activityIndex);

	/**
	 * 
	 * @param activity
	 * @return Whether the activity is a end activity. If possible, use the
	 *         integer-variant.
	 */
	public boolean isEndActivity(XEventClass activity);

	/**
	 * 
	 * @return The number of occurrences of the activity that occurs the most as
	 *         an end activity.
	 */
	public long getMostOccurringEndActivityCardinality();

	/**
	 * 
	 * @param activityIndex
	 * @return How often the activity was an end activity.
	 */
	public long getEndActivityCardinality(int activityIndex);

	/**
	 * 
	 * @param activity
	 * @return How often the activity was an end activity. Use the integer
	 *         variant if possible.
	 */
	public long getEndActivityCardinality(XEventClass activity);

	// ========= activities ==========

	/**
	 * 
	 * @return An array of the activities. Do not edit this array.
	 */
	public XEventClass[] getActivities();

	// ========= directly follows graph ==========

	public void removeDirectlyFollowsEdge(long edgeIndex);

	/**
	 * Returns an iterable that iterates over all edges; The edges that are
	 * returned are indices. Edges of weight 0 are excluded.
	 * 
	 * @return
	 */
	public Iterable<Long> getDirectlyFollowsEdges();
	
	public boolean containsDirectlyFollowsEdge(int sourceIndex, int targetIndex);
	
	public boolean containsDirectlyFollowsEdge(XEventClass source, XEventClass target);

	public int getDirectlyFollowsEdgeSourceIndex(long edgeIndex);

	public int getDirectlyFollowsEdgeTargetIndex(long edgeIndex);

	public XEventClass getDirectlyFollowsEdgeSource(long edgeIndex);

	public XEventClass getDirectlyFollowsEdgeTarget(long edgeIndex);
	
	public long getDirectlyFollowsEdgeCardinality(long edgeIndex);

	public long getMostOccuringDirectlyFollowsEdgeCardinality();

	// ========= concurrency graph ==========

	public void removeConcurrencyEdge(long edgeIndex);

	/**
	 * Returns an iterable that iterates over all edges; The edges that are
	 * returned are indices. Edges of weight 0 are excluded.
	 * 
	 * @return
	 */
	public Iterable<Long> getConcurrencyEdges();
	
	public boolean containsConcurrencyEdge(int sourceIndex, int targetIndex);
	
	public boolean containsConcurrencyEdge(XEventClass source, XEventClass target);

	public int getConcurrencyEdgeSourceIndex(long edgeIndex);

	public int getConcurrencyEdgeTargetIndex(long edgeIndex);

	public XEventClass getConcurrencyEdgeSource(long edgeIndex);

	public XEventClass getConcurrencyEdgeTarget(long edgeIndex);

	public long getConcurrencyEdgeCardinality(long edgeIndex);
	
	public long getMostOccuringConcurrencyEdgeCardinality();

	// ========= start activities ==========

	/**
	 * Add the start activities in the multiset to the start activities.
	 * 
	 * @param startActivities
	 */
	public void addStartActivities(MultiSet<XEventClass> startActivities);

	/**
	 * Add the start activities in the dfg to the start activities.
	 * 
	 * @param startActivities
	 */
	public void addStartActivities(Dfg dfg);

	/**
	 * Removes the start activity.
	 * 
	 * @param activityIndex
	 */
	public void removeStartActivity(int activityIndex);

	/**
	 * Removes the start activity. Use the integer variant if possible.
	 * 
	 * @param activity
	 */
	public void removeStartActivity(XEventClass activity);

	/**
	 * Return an iterable over the start activities. Use the integer variant if
	 * possible.
	 * 
	 * @return
	 */
	public Iterable<XEventClass> getStartActivities();

	/**
	 * 
	 * @return The indices of the start activities. This array should not be
	 *         edited.
	 */
	public int[] getStartActivityIndices();

	/**
	 * 
	 * @return The number of times that an end activity occurred. Use
	 *         getNumberOfStartActivities() for the set-size.
	 */
	public long getNumberOfStartActivities();

	// ========= end activities ==========

	/**
	 * Add the end activities in the multiset to the end activities.
	 * 
	 * @param endActivities
	 */
	public void addEndActivities(MultiSet<XEventClass> endActivities);

	/**
	 * Add the end activities in the dfg to the end activities.
	 * 
	 * @param startActivities
	 */
	public void addEndActivities(Dfg dfg);

	/**
	 * Removes the end activity.
	 * 
	 * @param activityIndex
	 */
	public void removeEndActivity(int activityIndex);

	/**
	 * Removes the end activity. Use the integer variant if possible.
	 * 
	 * @param activity
	 */
	public void removeEndActivity(XEventClass activity);

	/**
	 * Return an iterable over the start activities. Use the integer variant if
	 * possible.
	 * 
	 * @return
	 */
	public Iterable<XEventClass> getEndActivities();

	/**
	 * 
	 * @return The indices of the start activities. This array should not be
	 *         edited.
	 */
	public int[] getEndActivityIndices();

	/**
	 * 
	 * @return The number of times that an end activity occurred. Use
	 *         getNumberOfEndActivities() for the set-size.
	 */
	public long getNumberOfEndActivities();
}
