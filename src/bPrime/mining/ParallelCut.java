package bPrime.mining;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import bPrime.Sets;
import bPrime.ThreadPool;

public class ParallelCut {
	public static Set<Set<XEventClass>> findParallelCut(DirectlyFollowsRelation dfr, boolean useMinimumSelfDistance) {
		
		//choose the graph to use: directly-follows or eventually-follows
		DirectedGraph<XEventClass, DefaultWeightedEdge> graph = dfr.getDirectlyFollowsGraph();
		
		//noise filtering can have removed all start and end activities.
		//if that is the case, return
		if (dfr.getStartActivities().toSet().size() == 0 ||
				dfr.getEndActivities().toSet().size() == 0) {
			Set<Set<XEventClass>> emptyResult = new HashSet<Set<XEventClass>>();
			emptyResult.add(graph.vertexSet());
			return emptyResult;
		}
		
		//construct the negated graph
		DirectedGraph<XEventClass, DefaultEdge> negatedGraph = new DefaultDirectedGraph<XEventClass, DefaultEdge>(DefaultEdge.class);
		
		//add the vertices
		for (XEventClass e : graph.vertexSet()) {
			negatedGraph.addVertex(e);
		}
		
		//walk through the edges and negate them
		for (XEventClass e1 : graph.vertexSet()) {
			for (XEventClass e2 : graph.vertexSet()) {
				if (e1 != e2) {
					if (!graph.containsEdge(e1, e2) || !graph.containsEdge(e2, e1)) {
						negatedGraph.addEdge(e1, e2);
						//debug("add negated edge " + e1 + " -> " + e2);
					}
				}
			}
		}
		
		//if wanted, apply an extension to the B' algorithm to account for loops that have the same directly-follows graph as a parallel operator would have
		//make sure that activities on the minimum-self-distance-path are not separated by a parallel operator
		if (useMinimumSelfDistance) {
			for (XEventClass activity : graph.vertexSet()) {
				for (XEventClass activity2 : dfr.getMinimumSelfDistanceBetween(activity)) {
					negatedGraph.addEdge(activity, activity2);
				}
			}
		}
		
		//debug(dfr.debugGraph());
		
		//compute the connected components of the negated graph
		ConnectivityInspector<XEventClass, DefaultEdge> connectedComponentsGraph = new ConnectivityInspector<XEventClass, DefaultEdge>(negatedGraph);
		List<Set<XEventClass>> connectedComponents = connectedComponentsGraph.connectedSets();
		
		//not all connected components are guaranteed to have start and end activities. Merge those that do not.
		List<Set<XEventClass>> ccsWithStartEnd = new LinkedList<Set<XEventClass>>();
		List<Set<XEventClass>> ccsWithStart = new LinkedList<Set<XEventClass>>();
		List<Set<XEventClass>> ccsWithEnd = new LinkedList<Set<XEventClass>>();
		List<Set<XEventClass>> ccsWithNothing = new LinkedList<Set<XEventClass>>();
		for (Set<XEventClass> cc : connectedComponents) {
			Boolean hasStart = true;
			if (Sets.intersection(cc, dfr.getStartActivities().toSet()).size() == 0) {
				hasStart = false;
			}
			Boolean hasEnd = true;
			if (Sets.intersection(cc, dfr.getEndActivities().toSet()).size() == 0) {
				hasEnd = false;
			}
			if (hasStart) {
				if (hasEnd) {
					ccsWithStartEnd.add(cc);
				} else {
					ccsWithStart.add(cc);
				}
			} else {
				if (hasEnd) {
					ccsWithEnd.add(cc);
				} else {
					ccsWithNothing.add(cc);
				}
			}
		}
		//debug("StartEnd " + ccsWithStartEnd.toString());
		//debug("Start " + ccsWithStart.toString());
		//debug("End " + ccsWithEnd.toString());
		//debug("Nothing " + ccsWithNothing.toString());
		//add full sets
		List<Set<XEventClass>> connectedComponents2 = new LinkedList<Set<XEventClass>>(ccsWithStartEnd);
		//add combinations of end-only and start-only components
		Integer startCounter = 0;
		Integer endCounter = 0;
		while (startCounter < ccsWithStart.size() && endCounter < ccsWithEnd.size()) {
			Set<XEventClass> set = new HashSet<XEventClass>();
			set.addAll(ccsWithStart.get(startCounter));
			set.addAll(ccsWithEnd.get(endCounter));
			connectedComponents2.add(set);
			startCounter++;
			endCounter++;
		}
		//the start-only components can be added to any set
		while (startCounter < ccsWithStart.size()) {
			Set<XEventClass> set = connectedComponents2.get(0);
			set.addAll(ccsWithStart.get(startCounter));
			connectedComponents2.set(0, set);
			startCounter++;
		}
		//the end-only components can be added to any set
		while (endCounter < ccsWithEnd.size()) {
			Set<XEventClass> set = connectedComponents2.get(0);
			set.addAll(ccsWithEnd.get(endCounter));
			connectedComponents2.set(0, set);
			endCounter++;
		}
		//the non-start-non-end components can be added to any set
		for (Set<XEventClass> cc : ccsWithNothing) {
			Set<XEventClass> set = connectedComponents2.get(0);
			set.addAll(cc);
			connectedComponents2.set(0, set);
		}
		
		return new HashSet<Set<XEventClass>>(connectedComponents2);
	}
	
	public static void allPossibilities(DirectlyFollowsRelation dfr, ThreadPool pool) {
		DefaultDirectedGraph<XEventClass, DefaultWeightedEdge> graph = dfr.getDirectlyFollowsGraph();
		final double possibilities = Math.pow(2, graph.vertexSet().size() - 1) - 1;
		debug("parallel possibilities: " + possibilities);
		
		final int size = graph.vertexSet().size();
		debug("cut sizes " + size);
		
		//create the activity - index mapping
		HashMap<XEventClass, Integer> activityToIndex = new HashMap<XEventClass, Integer>();
		final HashMap<Integer, XEventClass> indexToActivity = new HashMap<Integer, XEventClass>();
		int k = 0;
		for (XEventClass activity : graph.vertexSet()) {
			activityToIndex.put(activity, k);
			indexToActivity.put(k, activity);
			k++;
		}
		
		//create the edge weight matrix
		final long[][] weightMatrix = new long [size][size];
		final long[] startVector = new long[size];
		final long[] endVector = new long[size];
		int from;
		int to;
		for (from=0;from<size;from++) {
			for (to=0;to<size;to++) {
				weightMatrix[from][to] = 0;
			}
			startVector[from] = dfr.getStartActivities().getCardinalityOf(indexToActivity.get(from));
			endVector[from] = dfr.getEndActivities().getCardinalityOf(indexToActivity.get(from));
		}
		for (DefaultWeightedEdge edge : graph.edgeSet()) {
			from = activityToIndex.get(graph.getEdgeSource(edge));
			to = activityToIndex.get(graph.getEdgeTarget(edge));
			weightMatrix[from][to] = (long) graph.getEdgeWeight(edge);
		}
		
		//prepare the threading
		//final AtomicLong atomicCutNr = new AtomicLong(274877775871L - 1);
		final AtomicLong atomicCutNr = new AtomicLong(-1);
		
		int threads = 1;
		for (int t=0;t<threads;t++) {
			pool.addJob(
					new Runnable() {
			            public void run() {
			            	double bestAverage = 0;
			        		long bestCutNr = 0;
			        		
			            	//walk through the cuts
			        		boolean[] cut = new boolean[size];
			        		int x;
			        		int y;
			        		int i;
			        		int cutSize;
			        		long sum;
			        		long intraIn, intraOut, interIn, interOut;
			        		double interIntraMeasure;
			        		double interIntraMeasureSum;
			        		double numberOfPotentialEdges;
			        		double average;
			        		long cutNr = atomicCutNr.incrementAndGet();
			        		while (cutNr < possibilities) {
			        			//construct the cut
			        			i = size - 1;
			        			cut[i] = true;
			        			cutSize = 1;
			        			i--;
			        		    while (i >= 0) {
			        		        cut[i] = (cutNr & (1L << i)) != 0;
			        		        if (cut[i]) {
			        		        	cutSize++;
			        		        }
			        		        i--;
			        		    }
			        			//debug(Arrays.toString(cut));
			        			
			        			//walk through the weight matrix
			        			sum = 0;
			        			interIntraMeasureSum = 0;
			        			for (x=0;x<size;x++) {
			        				intraIn = 0;
				        			intraOut = 0;
				        			interIn = 0;
				        			interOut = 0;
			        				for (y=0;y<size;y++) {
			        					if (cut[x] != cut[y]) {
			        						//inter
			        						sum += weightMatrix[x][y];
			        						interOut += weightMatrix[x][y];
			        						interIn += weightMatrix[y][x];
			        					} else {
			        						//intra
			        						intraOut += weightMatrix[x][y];
			        						intraIn += weightMatrix[y][x];
			        					}
			        				}
			        				interIntraMeasure = (interOut + interIn) / ((interOut + intraOut + endVector[x])*1.0) / 2;
			        				interIntraMeasureSum += interIntraMeasure;
			        				debug(" " + indexToActivity.get(x) + " inter: " + (interOut + interIn) + " intra: " + (intraOut + intraIn) + " measure: " + interIntraMeasure);
			        			}
			        			
			        			numberOfPotentialEdges = cutSize * (size - cutSize) * 2.0;
			        			
			        			average = sum / numberOfPotentialEdges;
			        			
			        			//if (average > bestAverage) {
			        				debug("");
			        				//debug("new best cut found");
			        				debug(Arrays.toString(cut));
			        				for (int j=0;j<size;j++) {
					        			if (!cut[j]) {
					        				debug(indexToActivity.get(j).toString() + " in 1");
					        			} else {
					        				debug(indexToActivity.get(j).toString() + " in 2");
					        			}
					        		}
			        				debug("cut nr " + cutNr);
			        				debug("total weight cut-crossing edges " + sum);
			        				debug("number of potential cut-crossing edges " + numberOfPotentialEdges );
			        				debug("average weight of cut-crossing edges " + average);
			        				debug("inter-intra measure " + interIntraMeasureSum);
			        				debug("");
			        				//bestAverage = average;
			        				//bestCutNr = cutNr;
			        			//}
			        			
			        			if (cutNr % 1000000 == 0) {
			        				debug("progress " + cutNr + " of " + possibilities);
			        			}
			        			
			        			cutNr = atomicCutNr.incrementAndGet();
			        		}
			        		/*
			        		debug("best cut of this thread " + bestCutNr + " with average weight of " + bestAverage);
			        		//reconstruct the bestcut
		        			i = size - 1;
		        			cut[i] = true;
		        			cutSize = 1;
		        			i--;
		        		    while (i >= 0) {
		        		        cut[i] = (bestCutNr & (1L << i)) != 0;
		        		        if (cut[i]) {
		        		        	cutSize++;
		        		        }
		        		        i--;
		        		    }
		        		    
			        		for (int j=0;j<size;j++) {
			        			if (!cut[j]) {
			        				debug(indexToActivity.get(j).toString() + " on \\Sigma^{\\parallelOp}_1");
			        			} else {
			        				debug(indexToActivity.get(j).toString() + " on \\Sigma^{\\parallelOp}_2");
			        			}
			        		}
			        		*/
			            }
					});
			//debug("average strength of cut " + average);
		}
	}
	
	private static void debug(String x) {
		System.out.println(x);
	}
}
