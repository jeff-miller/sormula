import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

// java -Dswing.defaultlaf=com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel Test
public class Test
{
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = frame.getContentPane();
		System.out.println(c.getLayout());
		JTabbedPane tp = new JTabbedPane();
		c.add(tp);
		
		for (int i = 0; i < 7; ++i)
			tp.addTab("Tab " + i, new JPanel());
		
		frame.pack();
		frame.setVisible(true);
	}
}
