package bPrime.batch;


public class BatchParameters {
	private String folder;
	private int numberOfThreads;
	
	public BatchParameters() {
		folder = "G:\\PROM";
		numberOfThreads = 0;
	}
	
	public String getFolder() {
		return folder;
	}
	
	public int getNumberOfThreads() {
		return numberOfThreads;
	}
}