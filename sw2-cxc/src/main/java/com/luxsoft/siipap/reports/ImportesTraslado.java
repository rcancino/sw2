package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Genera el reporte de Estado de cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
public class ImportesTraslado extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils.viewReport(ReportUtils.toReportesPath("tesoreria/DepositoValores.jasper"), form.getParametros());
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
		
		private final Map<String, Object> parametros=new HashMap<String, Object>();
		
		
		private JXDatePicker fechaInicial;
		private JXDatePicker fechaFinal;
		

		public ReportForm() {
			super("Relaci�n de depositos");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Relaci�n de fichas de deposito","").getHeader();
		}

		@Override
		protected JComponent buildContent() {			
			JPanel panel=new JPanel(new BorderLayout());			
			panel.add(buildForm(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("FECHA_INI", fechaInicial.getDate());
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			parametros.put("SUCURSAL", ServiceLocator2.getConfiguracion().getSucursal().getNombre());
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void main(String[] args) {
		ImportesTraslado action=new ImportesTraslado();
		action.execute();
	}

}
