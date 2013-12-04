package com.luxsoft.sw3.contabilidad.ui.reportes;

import javax.swing.JLabel;
import javax.swing.JProgressBar;


import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;

public class ReportWindow extends JXFrame{
	
	private JXStatusBar status;
	private JLabel info=new JLabel("Listo");
	private JProgressBar progress;
	
	private ReportTaskPanel content;
	
	public ReportWindow(){
		super("Reportes de contabilidad",false);
		setStartPosition(StartPosition.CenterInScreen);
		init();
	}
	
	
	
	private void init(){
		content=new ReportTaskPanel();
		addComponent(content);
		setStartPosition(StartPosition.CenterInScreen);
		
		
		status=new JXStatusBar();
		
		JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(); 
	    c1.setFixedWidth(200);
		status.add(info,c1);
		
		progress=new JProgressBar();	
		progress.setStringPainted(true);
		progress.setString("");
		progress.setBorderPainted(false);
		progress.setOpaque(false);
		
		JXStatusBar.Constraint c2 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FIXED);
		status.add(progress,c2);
		
		setStatusBar(status);
		
	}
	
	public void open(){
		pack();
		setVisible(true);
	}
	
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				ReportWindow window=new ReportWindow();
				window.open();
				
			}

		});
	}

}
