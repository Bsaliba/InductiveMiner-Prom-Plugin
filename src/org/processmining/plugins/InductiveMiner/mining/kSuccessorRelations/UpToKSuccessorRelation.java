package org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.Filteredlog;

public class UpToKSuccessorRelation {

	public class KSuccessorMatrix {
		private Integer[][] kSuccessorMatrix;
		private HashMap<XEventClass, Integer> activity2index;

		public KSuccessorMatrix(Set<XEventClass> activities) {
			kSuccessorMatrix = new Integer[activities.size() + 1][activities.size() + 1];
			activity2index = new HashMap<XEventClass, Integer>();
			int i = 0;
			List<XEventClass> list = new ArrayList<XEventClass>(activities);
			Collections.sort(list);
			for (XEventClass a : list) {
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
		
		public Set<XEventClass> getActivities() {
			return activity2index.keySet();
		}

		public String toString() {
			StringBuilder s = new StringBuilder();
			
			//titles
			s.append("     -S-");
			for (XEventClass from : activity2index.keySet()) {
				s.append(activity2string(from));
			}
			s.append(" -E-");
			s.append("\n");

			{
				s.append(" -S-  . ");
				for (XEventClass to : activity2index.keySet()) {
					Integer x = getKSuccessor(null, to);
					if (x != null) {
						s.append(String.format("%3d ", x));
					} else {
						s.append(" .  ");
					}
				}

				Integer x = getKSuccessor(null, null);
				if (x != null) {
					s.append(String.format("%3d ", x));
				} else {
					s.append("  . ");
				}

				s.append("\n");
			}

			for (XEventClass from : activity2index.keySet()) {
				s.append(activity2string(from));
				s.append("  . ");
				for (XEventClass to : activity2index.keySet()) {
					Integer x = getKSuccessor(from, to);
					if (x != null) {
						s.append(String.format("%3d ", x));
					} else {
						s.append("  . ");
					}
				}

				Integer x = getKSuccessor(from, null);
				if (x != null) {
					s.append(String.format("%3d ", x));
				} else {
					s.append(" .  ");
				}

				s.append("\n");
			}
			
			//end row
			s.append(" -E-");
			for (XEventClass from : activity2index.keySet()) {
				s.append("  . ");
			}
			s.append("  .   . \n");
			
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
	
	private String activity2string(XEventClass a) {
		String s = a.toString().substring(0, Math.min(a.toString().length(), 3));
		return String.format("%1$" + 4 + "s", s);
	}

	private KSuccessorMatrix kSuccessors;
	
	public UpToKSuccessorRelation(Set<XEventClass> activities) {
		kSuccessors = new KSuccessorMatrix(activities);
	}

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
				kSuccessors.feedKSuccessor(null, currentEvent, pos + 1);

				pos += 1;
			}

			for (XEventClass seen : eventSeenAt.keySet()) {
				kSuccessors.feedKSuccessor(seen, null, pos - eventSeenAt.get(seen));
			}

			kSuccessors.feedKSuccessor(null, null, 1 + pos);
		}
	}

	public KSuccessorMatrix getkSuccessors() {
		return kSuccessors;
	}

	public void setkSuccessors(KSuccessorMatrix kSuccessors) {
		this.kSuccessors = kSuccessors;
	}

	public String toString() {
		return kSuccessors.toString();
	}
}
