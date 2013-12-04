package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;


import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;


import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

import com.luxsoft.sw3.services.Services;

/**
 * 
 * @author Ruben Cancino
 *
 */
public class AnaliticoXMovimientoReportForm extends SWXAction{
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(form.resumen.isSelected())
				ReportUtils2.runReport("invent/AnaliticoXMovResumen.jasper", form.getParametros());
			else
				ReportUtils2.runReport("invent/AnaliticoXMovDetalle.jasper", form.getParametros());
		}
		form.dispose();
	}
	
	public static void runReport(){
		AnaliticoXMovimientoReportForm action=new AnaliticoXMovimientoReportForm();
		action.execute();
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
		private JComboBox tipoBox;
		public JCheckBox resumen;
		
		

		public ReportForm() {
			super("Inventarios");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			Object[] tipos={"DEC","CIM","TRS","DEV"};
			tipoBox=new JComboBox(tipos);
			tipoBox.setEditable(true);
			resumen=new JCheckBox("",true);
			
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g", //Columdas para colocar componentes
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.nextLine(); //En este caso esta linea es opcional
			builder.append("Fecha Final",fechaFinal);
			builder.append("Tipo",tipoBox);
			builder.append("Resumen",resumen);
			
			return builder.getPanel();
		}
		
		/**
		 * Header de la forma (Opcional)
		 * Util para describir el reporte
		 */
		protected JComponent buildHeader(){
			return new Header("Analitico por tipo de movimiento","").getHeader();
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
			Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			parametros.put("SUCURSAL", suc.getId());
			parametros.put("MOV", tipoBox.getSelectedItem());
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				runReport();
				
			}

		});
	}

}
