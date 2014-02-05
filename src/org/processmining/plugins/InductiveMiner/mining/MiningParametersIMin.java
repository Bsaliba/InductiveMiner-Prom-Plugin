package org.processmining.plugins.InductiveMiner.mining;

import java.util.Arrays;
import java.util.LinkedList;

import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMi;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMExclusiveChoice;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMParallel;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequence;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.CutFinderIMin;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughFlower;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIMi;

public class MiningParametersIMin extends MiningParameters {
	
	public MiningParametersIMin() {
		//determine algorithm
		
		setBaseCaseFinders(new LinkedList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIMi()
				)));
		
		setCutFinder(new LinkedList<CutFinder>(Arrays.asList(
				new CutFinderIMExclusiveChoice(),
				new CutFinderIMSequence(),
				new CutFinderIMParallel(),
				new CutFinderIMLoop(),
				new CutFinderIMin()
				)));
		
		setLogSplitter(new LogSplitterIMi());
		
		setFallThrough(new FallThroughFlower());
	}
}
