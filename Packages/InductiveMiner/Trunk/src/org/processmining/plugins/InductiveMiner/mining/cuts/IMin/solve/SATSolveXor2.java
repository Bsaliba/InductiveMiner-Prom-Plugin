package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.solve;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.SATResult;

public class SATSolveXor2 {

	public void solveAll(IMLogInfo logInfo, int l, AtomicResult bestTillNow, MinerState minerState) {
		//make activities table
		int nrOfActivities = logInfo.getActivities().setSize();
		XEventClass[] activities = new XEventClass[nrOfActivities];
		{
			int i = 0;
			for (XEventClass a : logInfo.getActivities()) {
				activities[i] = a;
				i++;
			}

		}
		
		solveXor(logInfo, nrOfActivities, bestTillNow, minerState, nrOfActivities, activities);
		solveParallel(logInfo, nrOfActivities, bestTillNow, minerState, nrOfActivities, activities);
	}

	public void solveXor(IMLogInfo logInfo, int l, AtomicResult bestTillNow, MinerState minerState, int nrOfActivities,
			XEventClass[] activities) {

		//make probabilities table
		long[][] p = new long[nrOfActivities][nrOfActivities];
		for (int i = 0; i < nrOfActivities; i++) {
			for (int j = i + 1; j < nrOfActivities; j++) {
				p[i][j] = (long) (minerState.parameters.getSatProbabilities().getProbabilityXor(logInfo, activities[i],
						activities[j]) * minerState.parameters.getSatProbabilities().doubleToIntFactor);
			}
		}

		performCommutative(nrOfActivities, activities, p, bestTillNow, minerState, Operator.xor);
	}
	
	public void solveParallel(IMLogInfo logInfo, int l, AtomicResult bestTillNow, MinerState minerState, int nrOfActivities,
			XEventClass[] activities) {

		//make probabilities table
		long[][] p = new long[nrOfActivities][nrOfActivities];
		for (int i = 0; i < nrOfActivities; i++) {
			for (int j = i + 1; j < nrOfActivities; j++) {
				p[i][j] = (long) (minerState.parameters.getSatProbabilities().getProbabilityParallel(logInfo, activities[i],
						activities[j]) * minerState.parameters.getSatProbabilities().doubleToIntFactor);
			}
		}

		performCommutative(nrOfActivities, activities, p, bestTillNow, minerState, Operator.parallel);
	}

	public static void performCommutative(final int nrOfActivities, final XEventClass[] activities, final long[][] p,
			final AtomicResult bestTillNow, final MinerState minerState, final Operator operator) {
		for (int l = 1; l < 0.5 + nrOfActivities / 2; l++) {
			final int l2 = l;
			minerState.parameters.getSatPool().addJob(new Runnable() {

				public void run() {
					SATResult result = null;
					do {
						result = solveSingle(l2, nrOfActivities, activities, p,
								minerState.parameters.getSatProbabilities().doubleToIntFactor, bestTillNow, operator);

						//System.out.println("yices finished " + result);

						if (result != null && result.getProbability() >= bestTillNow.get().getProbability()) {
							if (bestTillNow.maximumAndGet(result)) {
								//System.out.println("new maximum yices " + result);
							}
						}
					} while (result != null);
				}
			});
		}
	}

	public static SATResult solveSingle(int l, int nrOfActivities, XEventClass[] activities, long[][] p,
			int doubleToIntFactor, AtomicResult bestTillNow, Operator operator) {

		boolean[] partition = solveSingle2(l, nrOfActivities, activities, p, doubleToIntFactor, bestTillNow);

		//compute partition probability and make cut
		long sumProbability = 0;
		boolean found = false;
		Set<XEventClass> sigma1 = new HashSet<XEventClass>();
		Set<XEventClass> sigma2 = new HashSet<XEventClass>();
		for (int i = 0; i < nrOfActivities; i++) {

			//make cut
			if (partition[i]) {
				sigma1.add(activities[i]);
			} else {
				found = true;
				sigma2.add(activities[i]);
			}

			//compute probability
			for (int j = i + 1; j < nrOfActivities; j++) {
				if (partition[i] != partition[j]) {
					sumProbability += p[i][j];
				}
			}
		}

		if (!found) {
			return null;
		}

		int nrOfEdgesInCut = sigma2.size() * sigma1.size();
		double averageProbability = sumProbability / (nrOfEdgesInCut * doubleToIntFactor * 1.0);
		return new SATResult(sigma1, sigma2, averageProbability, operator);
	}

	public static boolean[] solveSingle2(int l, int nrOfActivities, XEventClass[] activities, long[][] p,
			int doubleToIntFactor, AtomicResult bestTillNow) {

		int nrOfEdgesInCut = (nrOfActivities - l) * l;

		String args[] = new String[1];
		args[0] = new File("d://yices//bin//yices.exe").getAbsolutePath();

		final ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectErrorStream(true);

		Process yicesProcess = null;
		try {
			yicesProcess = pb.start();
			BufferedWriter out2 = new BufferedWriter(new PrintWriter(yicesProcess.getOutputStream()));
			out2.write("(define cut::(-> int bool))");
			//out2.write("(define k::int " + l + ")");
			//out2.write("(assert (> k " + 0 + "))");
			//out2.write("(assert (<= k " + l + "))");

			//number of cut edges must be L
			out2.write("(assert (= (+");
			for (int i = 0; i < nrOfActivities; i++) {
				out2.write("(ite (cut " + i + ") 1 0)");
			}
			out2.write(")" + l + "))");

			//weighted sum must be better
			out2.write("(assert (> (+");
			for (int i = 0; i < nrOfActivities; i++) {
				for (int j = i + 1; j < nrOfActivities; j++) {
					out2.write("(ite (= (cut " + i + ") (cut " + j + ")) 0 " + p[i][j] + ")");
				}
			}
			long scoreTillNow = (long) (nrOfEdgesInCut * doubleToIntFactor * bestTillNow.get().getProbability());
			out2.write(") " + scoreTillNow + "))");

			out2.write("(check)");
			out2.write("(show-model)");
			out2.flush();
			out2.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		//initialise partition
		boolean[] partition = new boolean[nrOfActivities];
		for (int i = 0; i < nrOfActivities; i++) {
			partition[i] = true;
		}

		//InputStream outputOfYices = new BufferedInputStream(yicesProcess.getInputStream());
		BufferedReader outputOfYices = new BufferedReader(new InputStreamReader(yicesProcess.getInputStream()));
		try {
			//FileOutputStream outputStream = new FileOutputStream(new File("d://yices//output.txt"));
			//IOUtils.copy(outputOfYices, outputStream);
			Pattern pattern = Pattern.compile("\\(= \\(cut (\\d+)\\) false\\)");
			String s;
			while ((s = outputOfYices.readLine()) != null) {
				//System.out.println(s);
				Matcher matcher = pattern.matcher(s);
				if (matcher.find()) {
					//this one is false
					partition[Integer.valueOf(matcher.group(1))] = false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return partition;
	}
}
