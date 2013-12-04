package com.luxsoft.sw3.impap.services;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.jasperreports.view.JRViewer;

import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Dialogo  para preseentar un reporte
 * 
 * @author Ruben Cancino Ramos
 *
 */
public  class ReportDialog extends SXAbstractDialog{
	
	private final JRViewer reportView;
	private boolean okButton=false;
	
	public ReportDialog(final JRViewer view,String title) {
		super(title);
		this.reportView=view;
	}
	
	public ReportDialog(final JRViewer view,String title,boolean okButton) {
		super(title);
		this.reportView=view;
		this.okButton=okButton;
	}

	@Override
	protected JComponent buildContent() {
		if(okButton){
			JPanel content=new JPanel(new BorderLayout());
			content.add(reportView,BorderLayout.CENTER);
			content.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return content;
		}else
			return reportView;
	}

	@Override
	protected void setResizable() {
	setResizable(true);
	}
	
}