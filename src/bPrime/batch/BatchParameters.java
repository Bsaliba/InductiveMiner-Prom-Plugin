package bPrime.batch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class BatchParameters {
	private String folder;
	private int numberOfConcurrentFiles;
	private Set<String> extensions;
	private boolean measurePrecision;
	
	public BatchParameters() {
		//folder = "d:\\datasets\\boek\\chapter 5";
		folder = "D:\\datasets\\generatedLogs";
		numberOfConcurrentFiles = 3;
		extensions = new HashSet<String>(Arrays.asList(".xes", ".xml"));
		measurePrecision = false;
	}
	
	public void setFolder(String folder2) {
		folder = folder2;
	}
	
	public String getFolder() {
		return folder;
	}
	
	public int getNumberOfConcurrentFiles() {
		return numberOfConcurrentFiles;
	}
	
	public Set<String> getExtensions() {
		return extensions;
	}
	
	public boolean getMeasurePrecision() {
		return measurePrecision;
	}
}