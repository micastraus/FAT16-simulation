package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import rs.raf.os.fat.FAT16;
import rs.raf.os.test.MyFile;

@SuppressWarnings("serial")
public class FatView extends JFrame {
	
	public FatView(FAT16 fat, MyFile fileToMark) {
		setSize(400, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 10, 10);
		JPanel pnl = new JPanel(layout);
		pnl.setPreferredSize(new Dimension(380, 380));
		
		int startClusterIndex = fileToMark.getFirstCluster();
		
		for (int i=2; i<fat.getClusters().length; i++) {
			Cluster cl = new Cluster(fat.getClusters()[i]);
			if (i==startClusterIndex) {
				cl.setBackground(Color.green);
				startClusterIndex = fat.readCluster(startClusterIndex);
			}
			else 
				cl.setBackground(Color.LIGHT_GRAY);
			pnl.add(cl);
		}
		
		add(pnl);
		setVisible(true);
	}
	
	public class Cluster extends JPanel {
		
		public Cluster(int val) {	
			setPreferredSize(new Dimension(50, 50));
			JLabel lbl = new JLabel();
			lbl.setText(val+"");
			add(lbl);
		}
		
	}
	
	
	
}
