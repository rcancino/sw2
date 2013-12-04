package com.luxsoft.cfdi;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.jasperreports.view.JRViewer;

import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Dialogo  para preseentar un CFDI
 * 
 * @author Ruben Cancino Ramos
 *
 */
@SuppressWarnings("serial")
public  class CFDIReportDialog extends SXAbstractDialog{
	
	private final JRViewer reportView;
	private boolean okButton=false;
	
	public CFDIReportDialog(final JRViewer view,String title) {
		super(title);
		this.reportView=view;
	}
	
	public CFDIReportDialog(final JRViewer view,String title,boolean okButton) {
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