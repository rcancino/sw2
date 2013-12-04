package com.luxsoft.siipap.reports;

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
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.ventas.model.Cobrador;

/**
 * Genera el reporte de Estado de cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
public class RelacionDePagos extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/RelacionDePagos.jasper"), form.getParametros());
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
		private JComboBox origen;
		private JComboBox cobradorBox;
		private JCheckBox todosBox;
		private JCheckBox anticipoBox;
		

		public ReportForm() {
			super("Relación de pagos");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			Object[] cobs=ServiceLocator2.getCXCManager().getCobradores().toArray(new Cobrador[0]);
			cobradorBox=new JComboBox(cobs);
			todosBox=new JCheckBox("",false);
			origen=new JComboBox(OrigenDeOperacion.values());
			anticipoBox=new JCheckBox("",false);
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			builder.append("Cobrador",cobradorBox);
			builder.append("Todos",todosBox);
			builder.append("Origen",origen);
			builder.append("Anticipo",anticipoBox);
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Relación de fichas de deposito","").getHeader();
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
			OrigenDeOperacion ori=(OrigenDeOperacion)origen.getSelectedItem();
			parametros.put("ORIGEN", ori.name());
			Cobrador c=(Cobrador)cobradorBox.getSelectedItem();
			if(c!=null){
				String val=todosBox.isSelected()?"%":String.valueOf(c.getId());
				parametros.put("COBRADOR", val);
			}
			String tipo="PAGOS";
			if(anticipoBox.isSelected())
				tipo="ANTICIPOS";
			parametros.put("TIPO", tipo);
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void main(String[] args) {
		RelacionDePagos action=new RelacionDePagos();
		action.execute();
	}

}
