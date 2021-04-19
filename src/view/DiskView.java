package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import rs.raf.os.disk.Disk;
import rs.raf.os.fat.FAT16;

@SuppressWarnings("serial")
public class DiskView extends JFrame {
	
	public DiskView(Disk disk, FAT16 fat) {
		setSize(600, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		FlowLayout flow = new FlowLayout(FlowLayout.LEFT, 10, 10);
		JPanel pnl = new JPanel(flow);
		pnl.setPreferredSize(new Dimension(380, 380));
		
		Color color = null;
		for (int i=2; i<fat.getClusters().length; i++) {
			int tmpClusterValue = fat.readCluster(i);
			if (tmpClusterValue != 0)
				color = Color.BLACK;
			else 
				color = Color.WHITE;
				
			int tmpIndex = (i-2)*fat.getClusterWidth();
			for (int j=tmpIndex; j<tmpIndex+fat.getClusterWidth(); j++) 
				pnl.add(new Sector(j, color, disk));
		}
		
		add(pnl);
		setVisible(true);
	}
	
	public class Sector extends JButton {
		public Sector(int value, Color color, Disk disk) {
			setPreferredSize(new Dimension(60, 60));
			setBackground(color);
			JLabel lbl = new JLabel();
			lbl.setText(value+"");
			add(lbl);
			
			addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
						if (value < disk.getSectorCount()) {
						byte[] data = disk.readSector(value);
						for (int i=0; i<data.length; i++) 
							System.out.println("Sector"+value+":   "+data[i]);
					}
				}
			});
		}
	}

}
