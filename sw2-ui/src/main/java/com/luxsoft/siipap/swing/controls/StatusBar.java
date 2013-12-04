package com.luxsoft.siipap.swing.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class StatusBar{
	
	private StatusBarPanel statusPanel;
	
	public StatusBar(){		
	}

	public JPanel getStatusPanel() {
		if(statusPanel==null){
			statusPanel=new StatusBarPanel();			
			//statusPanel.setMainLeftComponent(getComponents());
			statusPanel.add(getComponents());
		}
		return statusPanel;
	}
	
	private JComponent getComponents(){
		FormLayout layout=new FormLayout("p,2dlu,p","p");
		PanelBuilder builder=new PanelBuilder(layout);
		CellConstraints cc=new CellConstraints();
		final DataBaseLocator dbLocator=new DataBaseLocator();
		final MemoryMonitor monitor=new MemoryMonitor();			
		builder.add(dbLocator.getControl(),cc.xy(1, 1));
		builder.add(monitor.getControl(),cc.xy(3, 1));
		return builder.getPanel();
	}

	
	class StatusBarPanel extends JPanel{
		
		private JPanel contentPanel;
		
		public StatusBarPanel(){
			setPreferredSize(new Dimension(getWidth(),23));
			initComponents();
		}
		
		private void initComponents(){
			setLayout(new BorderLayout(5,5));
			JLabel resizeIconLabel=new JLabel(new TrinagleSquareWindowsCornerIcon());
			resizeIconLabel.setOpaque(false);
			JPanel rightPanel=new JPanel(new BorderLayout());
			rightPanel.setOpaque(false);
			rightPanel.add(resizeIconLabel,BorderLayout.SOUTH);
			add(rightPanel,BorderLayout.EAST);
			
			contentPanel=new JPanel();
			add(contentPanel,BorderLayout.CENTER);
			
			FormLayout layout=null;
			layout = new com.jgoodies.forms.layout.FormLayout(
			           "2dlu, pref:grow",
			           "3dlu, fill:10dlu, 2dlu");

			contentPanel.setLayout(layout);
			

			
		}
		
		public void setMainLeftComponent(JComponent component){
	        contentPanel.add(component, new CellConstraints(2, 2));
	    }

		
		public void paintComponent(Graphics g) { 
	        super.paintComponent(g);

	        int y = 0;
	        g.setColor(new Color(156, 154, 140));
	        g.drawLine(0, y, getWidth( ), y);
	        y++;
	        g.setColor(new Color(196, 194, 183));
	        g.drawLine(0, y, getWidth( ), y);
	        y++;
	        g.setColor(new Color(218, 215, 201));
	        g.drawLine(0, y, getWidth( ), y);
	        y++;
	        g.setColor(new Color(233, 231, 217));
	        g.drawLine(0, y, getWidth( ), y);
	        
	        y = getHeight( ) - 3;
	        g.setColor(new Color(233, 232, 218));
	        g.drawLine(0, y, getWidth( ), y);
	        y++;
	        g.setColor(new Color(233, 231, 216));
	        g.drawLine(0, y, getWidth( ), y);
	        y = getHeight( ) - 1;
	        g.setColor(new Color(221, 221, 220));
	        g.drawLine(0, y, getWidth( ), y);

	    }
		
		public class SeparatorPanel extends JPanel { 
	        private Color leftColor; 
	        private Color rightColor;

	        public SeparatorPanel(Color left, Color right) {
	            this.leftColor = left;
	            this.rightColor = right;
	            setOpaque(false);
	        }

	        protected void paintComponent(Graphics g) {
	            g.setColor(leftColor);
	            g.drawLine(0,0, 0,getHeight( ));
	            g.setColor(rightColor);
	            g.drawLine(1,0, 1,getHeight( ));

	        }
	}



	}
	
	
	
}

