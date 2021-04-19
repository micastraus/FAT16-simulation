package rs.raf.os.test;

import java.util.Hashtable;

import rs.raf.os.dir.AbstractDirectory;
import rs.raf.os.dir.DirectoryException;
import rs.raf.os.disk.Disk;
import rs.raf.os.disk.DiskUtil;
import rs.raf.os.fat.FAT16;

public class MockDirectory extends AbstractDirectory {
	
	private Hashtable<String, MyFile> fileHashTable = new Hashtable<>();
	private int usableTotalSpace;
	private int usableFreeSpace;
	private int usedSpace;
	private int clusterSectorSize;
	
	public MockDirectory(FAT16 fat, Disk disk) {
		super(fat, disk);
		this.usableTotalSpace = Math.min(fat.getClusterCount()*fat.getClusterWidth()*disk.getSectorSize(),
				disk.diskSize());
		this.usableFreeSpace = this.usableTotalSpace;
		this.usedSpace = 0;
		this.clusterSectorSize = fat.getClusterWidth() * disk.getSectorSize();
	}
	
	@Override
	public boolean writeFile(String name, byte[] data) {
		MyFile f = fileHashTable.get(name);
		if ((f==null && getUsableFreeSpace() < data.length) ||
			(f!=null && getUsableFreeSpace()+f.getTotalSize() < data.length)) {
			System.out.println("Free disk space "+getUsableFreeSpace()+""
					+ "B is lower than required file size "+data.length+"B");
			return false;
		}
		
		if (f != null) {
			deleteFile(name);
			fileHashTable.remove(name);
		}
		
		MyFile fajl = null;
		
		for (int i=2; i<fat.getClusters().length; i++) {
			if (fat.readCluster(i) == MockFAT.FREE_CLUSTER) {
				int prevIndex = i;
				int clustersNeeded = data.length/clusterSectorSize;
				
				
				if (data.length%clusterSectorSize != 0) 
					clustersNeeded++;
				
				fajl = new MyFile(name, data.length, i, clustersNeeded*clusterSectorSize);
				fileHashTable.put(name, fajl);

				usableFreeSpace -= (clustersNeeded*clusterSectorSize);
				usedSpace 		+= (clustersNeeded*clusterSectorSize);

				byte[] completeFileData = DiskUtil.slice(data, 0, clustersNeeded*clusterSectorSize);
				disk.writeSectors((i-2)*fat.getClusterWidth(), fat.getClusterWidth(), completeFileData);
				int nextSector = clusterSectorSize;
				byte[] nextDataChunk = DiskUtil.slice(completeFileData, nextSector, clusterSectorSize);
				
				clustersNeeded--;
				
				for (int j=prevIndex+1; j<fat.getClusters().length && clustersNeeded>0; j++) {
					if (fat.readCluster(j) == MockFAT.FREE_CLUSTER) {
						fat.writeCluster(prevIndex, j);
						prevIndex = j;
						disk.writeSectors((j-2)*fat.getClusterWidth(), fat.getClusterWidth(), nextDataChunk);
						nextSector += clusterSectorSize;
						nextDataChunk = DiskUtil.slice(completeFileData, nextSector, clusterSectorSize);
						clustersNeeded--;
					}
				}
				
				fat.writeCluster(prevIndex, MockFAT.END_OF_CHAIN);
				
				break;
			}
		}
		return true;
	}

	@Override
	public byte[] readFile(String name) throws DirectoryException {
		if (! fileHashTable.containsKey(name)) {
			System.out.println("Can't read a file that doesn't exist.");
			throw new DirectoryException("Can't read a file that doesn't exist.");
		}
		
		MyFile fajl = fileHashTable.get(name);
		byte[] toRead = new byte[fajl.getSize()];
		
		int tmpClusterIndex = fajl.getFirstCluster();
		int tmpClusterValue = fat.readCluster(tmpClusterIndex);
		byte[] tmpData = disk.readSectors((tmpClusterIndex-2)*fat.getClusterWidth(), fat.getClusterWidth());
		
		int tmpByte = 0;
		while (tmpClusterValue != MockFAT.END_OF_CHAIN) {

			for (int i=tmpByte,j=0; i<clusterSectorSize+tmpByte; i++,j++) 
				toRead[i] = tmpData[j];
			
			tmpByte += clusterSectorSize;
			
			tmpClusterIndex = tmpClusterValue;
			tmpClusterValue = fat.readCluster(tmpClusterIndex);
			tmpData = disk.readSectors((tmpClusterIndex-2)*fat.getClusterWidth(), fat.getClusterWidth());
		}
		
		int restBytes = fajl.getSize()%clusterSectorSize;
		restBytes = (restBytes == 0) ? clusterSectorSize : restBytes;
		
		for (int i=tmpByte,j=0; i<restBytes+tmpByte; i++,j++) 
			toRead[i] = tmpData[j];
		
		return toRead;
	}

	@Override
	public void deleteFile(String name) throws DirectoryException {
		if (!fileHashTable.containsKey(name)) {
			System.out.println("Can't delete a file ("+name+") that doesn't exist.");
			throw new DirectoryException("Can't delete a file ("+name+") that doesn't exist.");
		}
		
		MyFile fajl = fileHashTable.get(name);
		
		usedSpace -= fajl.getTotalSize();
		usableFreeSpace += fajl.getTotalSize();
		
		int tmpClusterIndex = fajl.getFirstCluster();
		int tmpClusterValue = fat.readCluster(tmpClusterIndex);
		
		
		while (tmpClusterValue != MockFAT.END_OF_CHAIN) {
			fat.writeCluster(tmpClusterIndex, MockFAT.FREE_CLUSTER);
			tmpClusterIndex = tmpClusterValue;
			tmpClusterValue = fat.readCluster(tmpClusterValue);
		}
		
		fat.writeCluster(tmpClusterIndex, MockFAT.FREE_CLUSTER);
		
		fileHashTable.remove(name);
	}

	@Override
	public String[] listFiles() {
		String[] fileNames = new String[fileHashTable.size()];
		int i = 0;
		System.out.print("Listing files:");
		for (MyFile fajl : fileHashTable.values()) {
			fileNames[i] = fajl.getName();
			System.out.print("["+fileNames[i]+"]");
			i++;
		}
		System.out.println();
		return fileNames;
	}

	/**
	 * @return Total disk space in bytes.
	 */
	@Override
	public int getUsableTotalSpace() {
		return usableTotalSpace;
	}
	
	/**
	 * @return Used disk space in bytes.
	 */
	public int getUsedSpace() {
		return usedSpace;
	}

	/**
	 * @return Free disk space in bytes.
	 */
	@Override
	public int getUsableFreeSpace() {
		return usableFreeSpace;
	}
	
	/**
	 * 
	 * @return Size of (clusterWidth * sectorSize) in bytes.
	 */
	public int getClusterSectorSize() {
		return clusterSectorSize;
	}
	
	@Override
	public Hashtable<String, MyFile> getFatHash() {
		return fileHashTable;
	}

}
