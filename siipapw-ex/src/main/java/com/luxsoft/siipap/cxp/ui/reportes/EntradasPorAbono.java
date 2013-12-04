package com.luxsoft.siipap.cxp.ui.reportes;

import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Entradas de material por periodo y proveedor
 * 
 * @author Ruben Cancino
 *
 */
public class EntradasPorAbono extends SWXAction{

	@Override
	public void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxp/EntradasPorAbono.jasper"), form.getParametros());
		}
		form.dispose();
	}
	
	/**
	 * Forma para el reporte de cobranza
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		private JFormattedTextField abonoId;
		

		public ReportForm() {
			super("Entradas de material");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){		
			NumberFormatter formatter=new NumberFormatter(NumberFormat.getIntegerInstance());
			formatter.setAllowsInvalid(false);
			formatter.setCommitsOnValidEdit(true);
			formatter.setValueClass(Long.class);
			abonoId=new JFormattedTextField(formatter);
			
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			//CellConstraints cc=new CellConstraints();
			final FormLayout layout=new FormLayout(
					"l:40dlu,30dlu,60dlu, 3dlu, " +
					"l:40dlu,30dlu,p:g " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Id",abonoId,5);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("NUMERO", abonoId.getValue());
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}				 
		
	}
	
	public static void main(String[] args) {
		EntradasPorAbono action=new EntradasPorAbono();
		action.execute();
	}

}
