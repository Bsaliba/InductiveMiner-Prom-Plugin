package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class FallThroughSaveLog implements FallThrough {
	
	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MiningParameters parameters) {
		if (parameters.getOutputFlowerLogFileName() != null) {
			XLog xLog = log.toXLog();
			XSerializer logSerializer = new XesXmlSerializer();
			try {
				File file = new File(parameters.getOutputFlowerLogFileName() + "" + logInfo.getActivities());
				FileOutputStream out = new FileOutputStream(file);
				logSerializer.serialize(xLog, out);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
