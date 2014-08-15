package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.SimpleDfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderNoiseFiltering;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderSimple;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThrough;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThroughFlower;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.SimpleDfgSplitter;

public class DfgMiningParametersPartialOrder extends DfgMiningParameters {

	public DfgMiningParametersPartialOrder() {
		setDfgBaseCaseFinders(new ArrayList<DfgBaseCaseFinder>(Arrays.asList(
				new SimpleDfgBaseCaseFinder()
				)));
		
		setDfgCutFinders(new ArrayList<DfgCutFinder>(Arrays.asList(
				new DfgCutFinderSimple(),
				new DfgCutFinderNoiseFiltering()
				)));
		
		setDfgFallThroughs(new ArrayList<DfgFallThrough>(Arrays.asList(
				new DfgFallThroughFlower()
				)));
		
		setDfgSplitter(new SimpleDfgSplitter());
		
		setReduce(true);
		setDebug(true);
		setNoiseThreshold(0.2f);
	}

}
