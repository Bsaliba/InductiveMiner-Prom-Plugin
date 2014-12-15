package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.SimpleDfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderCombination;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderSimple;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThrough;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThroughFlower;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.SimpleDfgSplitter;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.CutFinderIMin;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities.ProbabilitiesEstimatedZ;

public class DfgMiningParametersIMinD extends DfgMiningParameters {
	public DfgMiningParametersIMinD() {
		setDfgBaseCaseFinders(new ArrayList<DfgBaseCaseFinder>(Arrays.asList(
				new SimpleDfgBaseCaseFinder()
				)));

		setDfgCutFinder(new DfgCutFinderCombination(
				new DfgCutFinderSimple(),
				new CutFinderIMin()
				));

		setDfgFallThroughs(new ArrayList<DfgFallThrough>(Arrays.asList(
				new DfgFallThroughFlower()
				)));

		setDfgSplitter(
				new SimpleDfgSplitter()
				);

		setReduce(true);
		setSatProbabilities(new ProbabilitiesEstimatedZ());
		setIncompleteThreshold(0);
	}
}