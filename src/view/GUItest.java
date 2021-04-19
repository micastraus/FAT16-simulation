package view;

import rs.raf.os.dir.Directory;
import rs.raf.os.disk.Disk;
import rs.raf.os.disk.SimpleDisk;
import rs.raf.os.fat.FAT16;
import rs.raf.os.test.MockDirectory;
import rs.raf.os.test.MockFAT;

public class GUItest {
	
	public static void main(String[] args) {
		FAT16 fat = new MockFAT(2, 20);
		Disk disk = new SimpleDisk(10, 45);
		
		Directory dir = new MockDirectory(fat, disk);
		
		byte[] file1 = new byte[50];
		byte[] file2 = new byte[100];
		byte[] file3 = new byte[150];
		
		int i = 0;
		for (i=0; i<file1.length; i++) 
			file1[i] = 3;
		
		for (i=0; i<file2.length; i++)
			file2[i] = 5;
		
		for (i=0; i<file3.length; i++) 
			file3[i] = 7;
		
		dir.writeFile("f1", file1);
		dir.writeFile("f2", file2);
		dir.writeFile("f3", file3);
		
		
		dir.deleteFile("f1");
		dir.writeFile("f3", file3);
		
		
		new FatView(fat, dir.getFatHash().get("f3"));
		new DiskView(disk, fat);
	}

}
