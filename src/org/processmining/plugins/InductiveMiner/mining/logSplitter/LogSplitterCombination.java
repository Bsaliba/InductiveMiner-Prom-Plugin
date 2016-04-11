package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class LogSplitterCombination implements LogSplitter {

	public final LogSplitter xor;
	public final LogSplitter sequence;
	public final LogSplitter parallel;
	public final LogSplitter loop;
	public final LogSplitter maybeInterleaved;
	public final LogSplitter interleaved;

	public LogSplitterCombination(LogSplitter xor, LogSplitter sequence, LogSplitter parallel, LogSplitter loop,
			LogSplitter maybeInterleaved, LogSplitter interleaved) {
		this.xor = xor;
		this.sequence = sequence;
		this.parallel = parallel;
		this.loop = loop;
		this.maybeInterleaved = maybeInterleaved;
		this.interleaved = interleaved;
	}
	
	@Deprecated
	public LogSplitterCombination(LogSplitter xor, LogSplitter sequence, LogSplitter parallel, LogSplitter loop,
			LogSplitter maybeInterleaved) {
		this.xor = xor;
		this.sequence = sequence;
		this.parallel = parallel;
		this.loop = loop;
		this.maybeInterleaved = maybeInterleaved;
		this.interleaved = parallel;
	}

	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		switch (cut.getOperator()) {
			case xor :
				return xor.split(log, logInfo, cut, minerState);
			case sequence :
				return sequence.split(log, logInfo, cut, minerState);
			case parallel :
				return parallel.split(log, logInfo, cut, minerState);
			case loop :
				return loop.split(log, logInfo, cut, minerState);
			case maybeInterleaved :
				return maybeInterleaved.split(log, logInfo, cut, minerState);
			case interleaved :
				return interleaved.split(log, logInfo, cut, minerState);
		}
		throw new NotImplementedException();
	}
}
