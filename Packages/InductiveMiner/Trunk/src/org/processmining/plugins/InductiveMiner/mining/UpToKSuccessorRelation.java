package org.processmining.plugins.InductiveMiner.mining;

import java.util.HashMap;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.Filteredlog;

public class UpToKSuccessorRelation {

	private class KSuccessorMatrix {
		private Integer[][] kSuccessorMatrix;
		private HashMap<XEventClass, Integer> activity2index;

		public KSuccessorMatrix(Set<XEventClass> activities) {
			kSuccessorMatrix = new Integer[activities.size() + 1][activities.size() + 1];
			activity2index = new HashMap<XEventClass, Integer>();
			int i = 0;
			for (XEventClass a : activities) {
				activity2index.put(a, i);
				i++;
			}
			for (int a = 0; a <= activities.size(); a++) {
				for (int b = 0; b <= activities.size(); b++) {
					kSuccessorMatrix[a][b] = null;
				}
			}
		}

		public Integer getKSuccessor(XEventClass from, XEventClass to) {
			return kSuccessorMatrix[getIndex(from)][getIndex(to)];
		}

		public void feedKSuccessor(XEventClass from, XEventClass to, Integer newValue) {
			int iFrom = getIndex(from);
			int iTo = getIndex(to);
			if (kSuccessorMatrix[iFrom][iTo] == null || newValue < kSuccessorMatrix[iFrom][iTo]) {
				kSuccessorMatrix[iFrom][getIndex(to)] = newValue;
			}
		}

		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append("    ");
			for (XEventClass from : activity2index.keySet()) {
				s.append(String.format("%3s", from));
			}
			s.append("-E-");
			s.append("\n");

			{
				s.append("-S- ");
				for (XEventClass to : activity2index.keySet()) {
					Integer x = getKSuccessor(null, to);
					if (x != null) {
						s.append(String.format("%2d ", x));
					} else {
						s.append(" . ");
					}
				}
				
				Integer x = getKSuccessor(null, null);
				if (x != null) {
					s.append(String.format("%2d ", x));
				} else {
					s.append(" . ");
				}
				
				s.append("\n");
			}
			for (XEventClass from : activity2index.keySet()) {
				s.append(String.format("%3s ", from));
				for (XEventClass to : activity2index.keySet()) {
					Integer x = getKSuccessor(from, to);
					if (x != null) {
						s.append(String.format("%2d ", x));
					} else {
						s.append(" . ");
					}
				}
				
				Integer x = getKSuccessor(from, null);
				if (x != null) {
					s.append(String.format("%2d ", x));
				} else {
					s.append(" . ");
				}
				
				s.append("\n");
			}
			return s.toString();
		}

		private int getIndex(XEventClass a) {
			if (a == null) {
				return activity2index.keySet().size();
			} else {
				return activity2index.get(a);
			}
		}
	}

	KSuccessorMatrix kSuccessors;

	public UpToKSuccessorRelation(Filteredlog log, MiningParameters parameters) {

		//initialise, read the log
		kSuccessors = new KSuccessorMatrix(log.getEventClasses());

		XEventClass currentEvent;

		//walk trough the log
		log.initIterator();
		HashMap<XEventClass, Integer> eventSeenAt;

		while (log.hasNextTrace()) {
			log.nextTrace();

			currentEvent = null;

			int pos = 0;
			eventSeenAt = new HashMap<XEventClass, Integer>();

			while (log.hasNextEvent()) {

				currentEvent = log.nextEvent();

				for (XEventClass seen : eventSeenAt.keySet()) {
					kSuccessors.feedKSuccessor(seen, currentEvent, pos - eventSeenAt.get(seen));
				}

				eventSeenAt.put(currentEvent, pos);
				kSuccessors.feedKSuccessor(null, currentEvent, pos+1);

				pos += 1;
			}
			
			kSuccessors.feedKSuccessor(null, null, pos);
		}
	}

	public String toString() {
		return kSuccessors.toString();
	}

	private void debug(String x) {
		System.out.println(x);
	}

}
