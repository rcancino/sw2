package com.luxsoft.siipap.swing.reports;

import java.util.Map;

import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

public abstract class ReportForm extends SXAbstractDialog{
	
	public ReportForm(String title) {
		super(title);
		
	}

	protected Map parametros;

	public Map getParametros() {
		return parametros;
	}

	public void setParametros(Map parametros) {
		this.parametros = parametros;
	}
	
	
	

}
