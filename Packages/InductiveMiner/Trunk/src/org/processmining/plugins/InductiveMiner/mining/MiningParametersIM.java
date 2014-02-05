package org.processmining.plugins.InductiveMiner.mining;

import java.util.Arrays;
import java.util.LinkedList;

import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMExclusiveChoice;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMParallel;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequence;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughFlower;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIMi;

public class MiningParametersIM extends MiningParameters {
	
	public MiningParametersIM() {
		//determine algorithm
		
		setBaseCaseFinders(new LinkedList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIM()
				)));
		
		setCutFinder(new LinkedList<CutFinder>(Arrays.asList(
				new CutFinderIMExclusiveChoice(),
				new CutFinderIMSequence(),
				new CutFinderIMParallel(),
				new CutFinderIMLoop()
				)));
		
		setLogSplitter(new LogSplitterIMi());
		
		setFallThrough(new FallThroughFlower());
	}
}
