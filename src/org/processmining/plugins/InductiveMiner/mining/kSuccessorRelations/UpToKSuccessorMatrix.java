package org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class UpToKSuccessorMatrix {
	private Integer[][] kSuccessorMatrix;
	private HashMap<String, Integer> activity2index;
	private List<String> activities;

	public UpToKSuccessorMatrix(Set<String> activities) {
		kSuccessorMatrix = new Integer[activities.size() + 1][activities.size() + 1];
		activity2index = new HashMap<String, Integer>();
		int i = 0;
		this.activities = new ArrayList<String>(activities);
		Collections.sort(this.activities);
		for (String a : this.activities) {
			activity2index.put(a, i);
			i++;
		}
		for (int a = 0; a <= activities.size(); a++) {
			for (int b = 0; b <= activities.size(); b++) {
				kSuccessorMatrix[a][b] = null;
			}
		}

	}

	public Integer getKSuccessor(String from, String to) {
		return kSuccessorMatrix[getIndex(from)][getIndex(to)];
	}

	public void feedKSuccessor(String from, String to, Integer newValue) {
		int iFrom = getIndex(from);
		int iTo = getIndex(to);
		if (kSuccessorMatrix[iFrom][iTo] == null || newValue < kSuccessorMatrix[iFrom][iTo]) {
			kSuccessorMatrix[iFrom][iTo] = newValue;
		}
	}

	public Set<String> getActivities() {
		return activity2index.keySet();
	}

	public String toString() {
		return toString(false);
	}

	public String toString(boolean useHTML) {
		StringBuilder s = new StringBuilder();

		String newLine = "\n";
		String newCell = "";
		if (useHTML) {
			newLine = "\n<tr>";
			newCell = "<td>";
			s.append("<table>");
		}

		//titles
		s.append(newLine);
		s.append(newCell);
		s.append("    ");
		s.append(newCell);
		s.append(" -S-");
		for (String from : activities) {
			s.append(newCell);
			s.append(shortenString(from));
		}
		s.append(newCell);
		s.append(" -E-");
		s.append(newLine);

		{
			s.append(newCell);
			s.append(" -S-");
			s.append(newCell);
			s.append("  . ");
			for (String to : activities) {
				s.append(newCell);
				Integer x = getKSuccessor(null, to);
				if (x != null) {
					s.append(String.format("%3d ", x));
				} else {
					s.append(" .  ");
				}
			}

			Integer x = getKSuccessor(null, null);
			s.append(newCell);
			if (x != null) {
				s.append(String.format("%3d ", x));
			} else {
				s.append("  . ");
			}

			s.append(newLine);
		}

		for (String from : activities) {
			s.append(newCell);
			s.append(shortenString(from));
			s.append(newCell);
			s.append("  . ");
			for (String to : activities) {
				Integer x = getKSuccessor(from, to);
				s.append(newCell);
				if (x != null) {
					s.append(String.format("%3d ", x));
				} else {
					s.append("  . ");
				}
			}

			Integer x = getKSuccessor(from, null);
			s.append(newCell);
			if (x != null) {
				s.append(String.format("%3d ", x));
			} else {
				s.append(" .  ");
			}

			s.append(newLine);
		}

		//end row
		s.append(newCell);
		s.append(" -E-");
		for (String from : activities) {
			s.append(newCell);
			s.append("  . ");
		}
		s.append(newCell);
		s.append("  . ");
		s.append(newCell);
		s.append("  . ");
		s.append(newLine);

		return s.toString();
	}

	private int getIndex(String a) {
		if (a == null) {
			return activities.size();
		} else {
			return activity2index.get(a);
		}
	}

	public static String shortenString(String name) {
		String s = name.substring(0, Math.min(name.length(), 3));
		return String.format("%1$" + 4 + "s", s);
	}
}
