package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class ClienteCreditoDetalleReport extends SWXAction {

	

	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/ClienteCreDET.jasper"), form.getParametros());
		}
		form.dispose();
		
	}
	
	
	public class ReportForm extends SXAbstractDialog{
		public  Map<String, Object>parametros;
		private JXDatePicker fecha;
		private JComponent cliente;
		ValueModel clienteModel;

		public ReportForm() {
			super("Ventas  Credito Contado");
			parametros=new HashMap<String, Object>();
		}
		
		
		@Override
		protected void setResizable() {
			setResizable(true);
		}


		private void initComponents(){
			clienteModel=new ValueHolder();
			cliente=Binder.createClientesBinding(clienteModel);
			fecha=new JXDatePicker();
			fecha.setFormats(new String[]{"dd/MM/yyyy"});

			
			
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel =new JPanel(new BorderLayout());
			panel.add(buildForma(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return panel;
		}

		private JComponent buildForma() {
			FormLayout layout=new FormLayout(
					"50dlu,4dlu,50dlu,40dlu,50dlu,50dlu,4dlu,50dlu,40dlu",
					"pref,20dlu,pref");
			PanelBuilder builder=new PanelBuilder(layout);
			CellConstraints cc=new CellConstraints();
			builder.add(new JLabel("Clave"),cc.xyw(1, 2, 2));
			builder.add(cliente,cc.xyw(3, 2, 7));
			builder.add(new JLabel("Fecha"),cc.xyw(1,3,2));
			builder.add(fecha,cc.xyw(3, 3, 2));
			return builder.getPanel();
		}
		
		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		
		public void doApply(){
			parametros.put("FECHA",fecha.getDate());
			Cliente a=(Cliente)clienteModel.getValue();
			if(a!=null){
				parametros.put("CLIENTE",a.getClave());
			}
					
			System.out.println("Parametros: "+parametros);
		}
		
	

		@Override
		protected JComponent buildHeader() {
			return new HeaderPanel("Reporte de Clientes Credito Detalle ","Reporte  Clientes Credito Detalle");
		}
	
		
		
		
	}
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		ClienteCreditoDetalleReport c=new ClienteCreditoDetalleReport();
		c.execute();
	}
	
	

}
