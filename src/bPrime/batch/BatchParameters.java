package bPrime.batch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class BatchParameters {
	private String folder;
	private int numberOfConcurrentFiles;
	private Set<String> extensions;
	
	public BatchParameters() {
		folder = "d:\\datasets\\boek\\chapter 5";
		numberOfConcurrentFiles = 4;
		extensions = new HashSet<String>(Arrays.asList(".xes", ".xml"));
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
}