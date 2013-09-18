package org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.FilterResults;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.Filteredlog;

public class Exhaustive {

	public class Result {
		int distance;
		public String cutType;
		public Collection<Set<XEventClass>> cut;
		public Collection<Filteredlog> sublogs;
		UpToKSuccessorRelation successor0;
		UpToKSuccessorRelation successor1;
	}

	private UpToKSuccessorRelation kSuccessor;
	private Filteredlog log;
	private MiningParameters parameters;

	public Exhaustive(Filteredlog log, UpToKSuccessorRelation kSuccessor, MiningParameters parameters) {
		this.kSuccessor = kSuccessor;
		this.log = log;
		this.parameters = parameters;
	}

	public Result tryAll() {
		Result result = new Result();
		result.distance = Integer.MAX_VALUE;
		Result result2;
		int nrOfBits = log.getEventClasses().size();

		XEventClass[] activities = new XEventClass[log.getEventClasses().size()];
		int i = 0;
		for (XEventClass e : log.getEventClasses()) {
			activities[i] = e;
			i++;
		}

		List<Set<XEventClass>> cut;
		for (int cutNr = 1; cutNr < Math.pow(2, nrOfBits) - 1 && result.distance > 0; cutNr++) {
			cut = generateCut(cutNr, nrOfBits, activities);
			
			//parallel
			result2 = processCutParallel(cut);
			if (result.distance > result2.distance) {
				result = result2;
				debug(cut.toString() + " " + result2.cutType + ": " + result2.distance);
			}
			
			//loop
			result2 = processCutLoop(cut);
			if (result.distance > result2.distance) {
				result = result2;
				debug(cut.toString() + " " + result2.cutType + ": " + result2.distance);
			}
		}

		return result;
	}

	public Result processCutParallel(Collection<Set<XEventClass>> cut) {

		Result result = new Result();

		//split log
		FilterResults filterResults = log.applyFilterParallel(new HashSet<Set<XEventClass>>(cut));
		result.sublogs = filterResults.sublogs;

		//make k-successor relations
		Iterator<Filteredlog> it = result.sublogs.iterator();
		result.successor0 = new UpToKSuccessorRelation(it.next(), parameters);
		result.successor1 = new UpToKSuccessorRelation(it.next(), parameters);

		//combine the logs
		UpToKSuccessorRelation combinedParallel = (new CombineParallel()).combine(result.successor0.getkSuccessors(),
				result.successor1.getkSuccessors());

		result.distance = (new DistanceEuclidian()).computeDistance(kSuccessor.getkSuccessors(),
				combinedParallel.getkSuccessors());

		result.cut = cut;
		result.cutType = "parallel";

		return result;
	}
	
	public Result processCutLoop(Collection<Set<XEventClass>> cut) {

		Result result = new Result();

		//split log
		FilterResults filterResults = log.applyFilterLoop(new ArrayList<Set<XEventClass>>(cut));
		result.sublogs = filterResults.sublogs;

		//make k-successor relations
		Iterator<Filteredlog> it = result.sublogs.iterator();
		result.successor0 = new UpToKSuccessorRelation(it.next(), parameters);
		result.successor1 = new UpToKSuccessorRelation(it.next(), parameters);

		//combine the logs
		UpToKSuccessorRelation combinedLoop = (new CombineLoop()).combine(result.successor0.getkSuccessors(),
				result.successor1.getkSuccessors());

		result.distance = (new DistanceEuclidian()).computeDistance(kSuccessor.getkSuccessors(),
				combinedLoop.getkSuccessors());

		result.cut = cut;
		result.cutType = "loop";

		return result;
	}

	public List<Set<XEventClass>> generateCut(int input, int nrOfBits, XEventClass[] activities) {

		List<Set<XEventClass>> result = new LinkedList<Set<XEventClass>>();
		Set<XEventClass> a = new HashSet<XEventClass>();
		Set<XEventClass> b = new HashSet<XEventClass>();

		for (int i = nrOfBits - 1; i >= 0; i--) {
			if ((input & (1 << i)) != 0) {
				a.add(activities[i]);
			} else {
				b.add(activities[i]);
			}
		}

		result.add(a);
		result.add(b);

		return result;
	}

	private void debug(String x) {
		System.out.println(x);
	}
}
