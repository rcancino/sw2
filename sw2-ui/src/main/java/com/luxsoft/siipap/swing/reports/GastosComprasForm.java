package com.luxsoft.siipap.swing.reports;



import javax.swing.JComponent;
import javax.swing.JLabel;



public class GastosComprasForm extends ReportForm{

	public GastosComprasForm() {
		super("Reporte de prueba");
	
	}

	@Override
	protected JComponent buildContent() {
		return new JLabel("OK");
	}

}
