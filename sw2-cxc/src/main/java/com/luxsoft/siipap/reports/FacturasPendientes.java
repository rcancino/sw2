package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Genera reporte de facturas pendientes por recibir
 * 
 * @author Ruben Cancino
 *
 */
public class FacturasPendientes extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/RecepcionDeFacturas.jasper"), form.getParametros());
		}
		form.dispose();
	}
	
	/**
	 * Forma para el reporte de diario de entradas
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros=new HashMap<String, Object>();
		
		
		private JComboBox sucursalBox;
		//private JCheckBox todosBox;
		

		public ReportForm() {
			super("Facturas pendientes de recibir");
		}
		
		private void initComponents(){
			Object[] cobs=ServiceLocator2.getLookupManager().getSucursales().toArray(new Sucursal[0]);
			sucursalBox=new JComboBox(cobs);
			//todosBox=new JCheckBox("",false);
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Sucursal",sucursalBox);
			//builder.append("Todos",todosBox);
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Facturas pendientes ","").getHeader();
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
			Sucursal c=(Sucursal)sucursalBox.getSelectedItem();
			if(c!=null){
				//Integer val=todosBox.isSelected()?0:c.getClave();
				parametros.put("SUCURSAL", c.getClave());
			}			
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void main(String[] args) {
		FacturasPendientes action=new FacturasPendientes();
		action.execute();
	}

}
