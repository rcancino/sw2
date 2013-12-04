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
import com.luxsoft.siipap.model.Sucursal;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.ventas.model.Vendedor;



/**
 * 
 * @author Ruben Cancino
 *
 */
public class VentasPorVendedorBI extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils.viewReport(ReportUtils.toReportesPath("BI/VentasPorVendedor.jasper"), form.getParametros());
			
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
		private JComboBox origenBox;
		private JComboBox vendedorBox;
		
		

		public ReportForm() {
			super("Ventas Diarias");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			Object[] values={"CREDITO","CONTADO","TODOS"};
			origenBox=new JComboBox(values);
			Object[] vends=ServiceLocator2.getCXCManager().getVendedores().toArray(new Vendedor[0]);
			vendedorBox=new JComboBox(vends);
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			builder.append("Vendedor",vendedorBox);
			builder.append("Tipo",origenBox);
			
			return builder.getPanel();
		}
		
		
		
		
		
		protected JComponent buildHeader(){
			return new Header("Ventas Por Vendedor","").getHeader();
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
		

			
			
			String origen=origenBox.getSelectedItem().toString();
			if(origen=="CREDITO")
				origen="CRE";
			if(origen=="CONTADO")
				origen="%M%";
			if(origen=="TODOS")
				origen="%";				
			parametros.put("ORIGEN",origen);
			
			
			Vendedor v=(Vendedor)vendedorBox.getSelectedItem();
			Long id=v.getId();
			parametros.put("VENDEDOR", id);
			
			logger.info("Parametros de reporte:"+parametros);
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void run(){
		VentasPorVendedorBI action=new VentasPorVendedorBI();
		action.execute();
	}
	

	
	public static void main(String[] args) {
		run();
	}

}
