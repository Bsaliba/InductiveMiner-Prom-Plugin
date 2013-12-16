package org.processmining.plugins.InductiveMiner.mining.kSuccessorRelations;

import java.util.HashSet;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Matrix;

public class UpToKSuccessorMatrix {
	private Matrix<String, Integer> matrix;

	public UpToKSuccessorMatrix(Set<String> activities) {
		matrix = new Matrix<String, Integer>(activities, true);
	}

	public Integer getKSuccessor(String from, String to) {
		return matrix.get(from, to);
	}

	public void feedKSuccessor(String from, String to, Integer newValue) {
		Integer old = matrix.get(from, to);
		
		if (old == null || newValue < old) {
			matrix.set(from, to, newValue);
		}
	}

	public Set<String> getActivities() {
		return new HashSet<String>(matrix.getActivities());
	}

	public String toString() {
		return matrix.toString();
	}

	public String toString(boolean useHTML) {
		return matrix.toString(useHTML);
	}
}
