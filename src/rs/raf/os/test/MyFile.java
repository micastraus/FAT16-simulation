package rs.raf.os.test;

public class MyFile {
	
	private String name;
	private int size;
	private int firstCluster;
	private int totalSize;
	
	public MyFile(String name, int size, int startingCluster, int totalSize) {
		this.name = name;
		this.size = size;
		this.firstCluster = startingCluster;
		this.totalSize = totalSize;
	}
	
	public String getName() {
		return name;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getTotalSize() {
		return totalSize;
	}
	
	public int getFirstCluster() {
		return firstCluster;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
