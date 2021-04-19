package rs.raf.os.test;

import rs.raf.os.fat.FAT16;
import rs.raf.os.fat.FATException;

public class MockFAT implements FAT16 {

	private int clusterWidth;
	private int clusterCount;
	private int[] clustersData;

	public static final int MAX_CLUSTERS = 65528;
	public static final int END_OF_CHAIN = 0xFFF8;
	public static final int FREE_CLUSTER = 0;
	
	public MockFAT(int clusterWidth) {
		this(clusterWidth, MAX_CLUSTERS);
	}
	
	public MockFAT(int clusterWidth, int clusterCount) {
		this.clusterWidth = clusterWidth;
		this.clusterCount = clusterCount > MAX_CLUSTERS ? 
				MAX_CLUSTERS : clusterCount;
		this.clustersData = new int[this.clusterCount+2];
	}
	
	@Override
	public int[] getClusters() {
		return clustersData;
	}
	
	@Override
	public int getEndOfChain() {
		return 65528;
	}

	@Override
	public int getClusterCount() {
		return clusterCount;
	}
	
	@Override
	public int getClusterWidth() {
		return clusterWidth;
	}

	@Override
	public int readCluster(int clusterID) throws FATException {
		if (clusterID < 2 || clusterID > (MAX_CLUSTERS+1) || clusterID >= clustersData.length) {
			throw new FATException("ClusterID can't be lower than 2 and greater "
					+ "than "+(MAX_CLUSTERS+1)+" or FAT's clusters size");
		}
		return clustersData[clusterID];
	}

	@Override
	public void writeCluster(int clusterID, int valueToWrite) throws FATException {
		if (clusterID < 2 || clusterID > (MAX_CLUSTERS+1) || clusterID >= clustersData.length) {
			throw new FATException("ClusterID can't be lower than 2 and greater "
					+ "than "+(MAX_CLUSTERS+1)+" or FAT's clusters size");
		}
		
		clustersData[clusterID] = valueToWrite;
	}

	@Override
	public String getString() {
		String str = "[";
		for (int i=2; i<clustersData.length-1; i++) {
			str += clustersData[i] + "|";
		}
		str += clustersData[clustersData.length-1]+"]";
		return str;
	}

}
