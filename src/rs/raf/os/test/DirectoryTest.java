package rs.raf.os.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import rs.raf.os.dir.Directory;
import rs.raf.os.dir.DirectoryException;
import rs.raf.os.disk.Disk;
import rs.raf.os.disk.SimpleDisk;
import rs.raf.os.fat.FAT16;

public class DirectoryTest {
	
	@Test
	public void AndrijaTest() {
		FAT16 fat = new MockFAT(2, 20);
		Disk disk = new SimpleDisk(10, 45);
		
		Directory dir = new MockDirectory(fat, disk);
		
		byte[] file1 = new byte[50];
		byte[] file2 = new byte[100];
		byte[] file3 = new byte[150];
		
		for (int i=0; i<file1.length; i++) 
			file1[i] = 3;
		
		for (int i=0; i<file2.length; i++)
			file2[i] = 5;
		
		for (int i=0; i<file3.length; i++) 
			file3[i] = 7;
		
		dir.writeFile("f1", file1);
		assertEquals("[3|4|65528|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0]", fat.getString());
		dir.writeFile("f2", file2);
		assertEquals("[3|4|65528|6|7|8|9|65528|0|0|0|0|0|0|0|0|0|0|0|0]", fat.getString());
		dir.writeFile("f3", file3);
		assertEquals("[3|4|65528|6|7|8|9|65528|11|12|13|14|15|16|17|65528|0|0|0|0]", fat.getString());
		
		
		dir.deleteFile("f1");
		dir.writeFile("f3", file3);
		
		byte[] f3 = dir.readFile("f3");
		for (int i=0; i<f3.length; i++) 
			assertEquals(f3[i], file3[i]);
		
	}
	
	
	@Test
	public void simpleWriteReadTest() {
		//4 clusters, each one sector width
		FAT16 fat = new MockFAT(1, 4);
		
		//sectors are 40 bytes, 10 of them on disk
		Disk disk = new SimpleDisk(40, 10);
		
		Directory dir = new MockDirectory(fat, disk);
		
		//50 bytes of data, should take up two clusters, which are two sectors
		byte[] data = new byte[50];
		for(int i = 0; i < 50; i++) {
			data[i] = (byte)(i*2);
		}
		
		//160 allocatable bytes - FAT is smaller than actual disk
		assertEquals(160, dir.getUsableTotalSpace());
		assertEquals(160, dir.getUsableFreeSpace());
		
		if (dir.writeFile("Even", data)) {
			byte[] readData = dir.readFile("Even");
			
			assertEquals(50, readData.length);
			for (int i = 0; i < 50; i++) {
				assertEquals((byte)(i*2), readData[i]);
			}
			
		} else {
			fail("Could not write file");
		}
		
		assertEquals("[3|65528|0|0]", fat.getString());
		
		assertEquals(160, dir.getUsableTotalSpace());
		assertEquals(80, dir.getUsableFreeSpace());
	}
	
	@Test (expected=DirectoryException.class)
	public void deleteTest() {
		//4 clusters, each one sector width
		FAT16 fat = new MockFAT(1, 4);
		
		//sectors are 40 bytes, 10 of them on disk
		Disk disk = new SimpleDisk(40, 10);
		
		Directory dir = new MockDirectory(fat, disk);
		
		//150 bytes of data, should take up four clusters, which are four sectors
		byte[] data = new byte[150];
		for(int i = 0; i < 150; i++) {
			data[i] = (byte)(i*2);
		}
		
		//160 allocatable bytes - FAT is smaller than actual disk
		assertEquals(160, dir.getUsableTotalSpace());
		assertEquals(160, dir.getUsableFreeSpace());
		
		if (dir.writeFile("Even", data)) {
			assertEquals(160, dir.getUsableTotalSpace());
			assertEquals(0, dir.getUsableFreeSpace());
			
			dir.deleteFile("Even");
			
			assertEquals(160, dir.getUsableTotalSpace());
			assertEquals(160, dir.getUsableFreeSpace());
			
			if (!dir.writeFile("Even", data)) {
				fail("Could not write file");
			}
			
		} else {
			fail("Could not write file");
		}
		
//		should throw excepti?on
		dir.deleteFile("asd");
	}
	
	@Test
	public void rewriteTest() {
		//4 clusters, each one sector width
		FAT16 fat = new MockFAT(1, 4);
		
		//sectors are 40 bytes, 10 of them on disk
		Disk disk = new SimpleDisk(40, 10);
		
		Directory dir = new MockDirectory(fat, disk);
		
		//150 bytes of data, should take up four clusters, which are four sectors
		byte[] data = new byte[150];
		for(int i = 0; i < 150; i++) {
			data[i] = (byte)(i*2);
		}
		
		//160 allocatable bytes - FAT is smaller than actual disk
		assertEquals(160, dir.getUsableTotalSpace());
		assertEquals(160, dir.getUsableFreeSpace());
		
		if (dir.writeFile("Even", data)) {
			assertEquals(160, dir.getUsableTotalSpace());
			assertEquals(0, dir.getUsableFreeSpace());
			
			//write different file should fail
			if (dir.writeFile("Odd", data)) {
				fail("Didn't fail writing file to full disk");
			}
			
			for(int i = 0; i < 150; i++) {
				data[i] = (byte)(i);
			}
			
//			overwrite should be ok
			if (dir.writeFile("Even", data)) {
				byte[] readData = dir.readFile("Even");
				
				assertEquals(150, readData.length);
				for (int i = 0; i < 150; i++) {
					assertEquals((byte)(i), readData[i]);
				}
			} else {
				fail("Could not overwrite file");
			}
			
			assertEquals(160, dir.getUsableTotalSpace());
			assertEquals(0, dir.getUsableFreeSpace());
			
			if (!dir.writeFile("Even", data)) {
				fail("Could not write file");
			}
		} else {
			fail("Could not write file");
		}
	}
	
	@Test
	public void bigFATSmallDiskTest() {
		//10 clusters, each two sectors width, can allocate 2000 bytes
		FAT16 fat = new MockFAT(2, 10);
		
		//sectors are 100 bytes, 6 of them on disk, for a total of 600 bytes
		Disk disk = new SimpleDisk(100, 6);
		
		Directory dir = new MockDirectory(fat, disk);
		
		//150 bytes of data, should take up one cluster, which is two sectors
		byte[] data = new byte[150];
		for(int i = 0; i < 150; i++) {
			data[i] = (byte)(i*2);
		}
		
		//600 total bytes - disk is smaller than allocatable FAT space
		assertEquals(600, dir.getUsableTotalSpace());
		assertEquals(600, dir.getUsableFreeSpace());
		
		if (dir.writeFile("Even", data)) {
			byte[] readData = dir.readFile("Even");
			
			assertEquals(150, readData.length);
			for (int i = 0; i < 150; i++) {
				assertEquals((byte)(i*2), readData[i]);
			}
		} else {
			fail("Could not write file");
		}
		
		assertEquals("[65528|0|0|0|0|0|0|0|0|0]", fat.getString());
		
		assertEquals(600, dir.getUsableTotalSpace());
		assertEquals(400, dir.getUsableFreeSpace());
	}
	
	@Test(expected=DirectoryException.class)
	public void actualFAT16Test() {
		//default FAT16 cluster count of 0xFFEF-2, cluster width is 1 sector
		FAT16 fat = new MockFAT(1);
		
		//sectors of size 512 bytes, 2880 of them - totaling up to 1.44MB
		Disk disk = new SimpleDisk();
		
		Directory dir = new MockDirectory(fat, disk);
		
		//disk should be smaller than FAT
		assertEquals(1474560, dir.getUsableTotalSpace());
		
//		800KB file
		byte[] largeFile1 = new byte[1024*800];
		for(int i = 0; i < largeFile1.length; i++) {
			largeFile1[i] = 1;
		}
		
		//200KB file
		byte[] largeFile2 = new byte[1024*200];
		for(int i = 0; i < largeFile2.length; i++) {
			largeFile2[i] = 2;
		}
		
		//600KB file
		byte[] largeFile3 = new byte[1024*600];
		for(int i = 0; i < largeFile3.length; i++) {
			largeFile3[i] = 3;
		}
		
		if (!dir.writeFile("File1", largeFile1)) {
			fail("Could not write File1");
		}
		
		if (!dir.writeFile("File2", largeFile2)) {
			fail("Could not write File2");
		}
		
		if (dir.writeFile("File3", largeFile3)) {
			fail("Could write File3");
		}
		
		dir.deleteFile("File1");
		
		if (!dir.writeFile("File3", largeFile3)) {
			fail("Could not write File3");
		}
		
		byte[] file2 = dir.readFile("File2");
		byte[] file3 = dir.readFile("File3");
		
		assertEquals(2, file2[100]);
		assertEquals(3, file3[100]);
		
		//should throw directory exception
		dir.readFile("File1");
	}
	
	
	

}
