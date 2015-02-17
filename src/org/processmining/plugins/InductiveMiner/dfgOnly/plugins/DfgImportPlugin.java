package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

@Plugin(name = "Import a CSV file and convert it to dfg", parameterLabels = { "Filename" }, returnLabels = { "Directly-follows graph" }, returnTypes = { Dfg.class })
@UIImportPlugin(description = "Directly-follows graph", extensions = { "dfg", "csv" })
public class DfgImportPlugin extends AbstractImportPlugin {

	private static final int BUFFER_SIZE = 8192 * 4;
	private static final char SEPARATOR = ',';
	private static final String CHARSET = Charset.defaultCharset().name();

	protected Dfg importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {

		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(input, CHARSET), BUFFER_SIZE),
				SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, 0, false, false, true);

		Dfg dfg;
		try {
			//add activities
			String[] sActivities = reader.readNext();
			XEventClass[] activities = new XEventClass[sActivities.length];
			for (int a = 0; a < sActivities.length; a++) {
				activities[a] = new XEventClass(sActivities[a], a);
			}

			dfg = new Dfg(sActivities.length);
			for (int a = 0; a < sActivities.length; a++) {
				dfg.addActivity(activities[a]);
			}

			//start activities
			String[] sStartActivities = reader.readNext();
			for (int a = 0; a < sActivities.length; a++) {
				long cardinality = Long.valueOf(sStartActivities[a]);
				if (cardinality > 0) {
					dfg.getStartActivities().add(activities[a], cardinality);
				}
			}

			//end activities
			String[] sEndActivities = reader.readNext();
			for (int a = 0; a < sActivities.length; a++) {
				long cardinality = Long.valueOf(sEndActivities[a]);
				if (cardinality > 0) {
					dfg.getEndActivities().add(activities[a], cardinality);
				}
			}

			//edges
			for (int a1 = 0; a1 < sActivities.length; a1++) {
				String[] row = reader.readNext();
				for (int a2 = 0; a2 < sActivities.length; a2++) {
					long cardinality = Long.valueOf(row[a2]);
					dfg.addDirectlyFollowsEdge(activities[a1], activities[a2], cardinality);
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Invalid directly-follows graph file", "Invalid file",
					JOptionPane.ERROR_MESSAGE);
			context.getFutureResult(0).cancel(false);
			return null;
		} finally {
			reader.close();
		}

		return dfg;
	}
}
