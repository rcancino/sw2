package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Genera el reporte de Estado de cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
public class ComparativoVentasXLineaXCte extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
				
			if(form.jKilos.isSelected())
			ReportUtils.viewReport(ReportUtils.toReportesPath("BI/VentasXLineaXCteEnKilosComp.jasper"), form.getParametros());
			else 
		    ReportUtils.viewReport(ReportUtils.toReportesPath("BI/VentasXLineaXCteEnPesosComp.jasper"), form.getParametros());
				
		}
		form.dispose();
	}
	
	/**
	 * Forma para el reporte de Comparativo mejores clientes
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		private Cliente cliente;
		/*private Date fechaInicial1;
		private Date fechaFinal1;
		private Date fechaInicial2;
		private Date fechaFinal2;*/
		private Integer mesInicial;
		private Integer mesfinal;
		private Integer yearPrincipal;
		private Integer yearCompara;
		
		
		private final PresentationModel model;
		
		private JComponent jCliente;
		/*private JComponent jFechaIni1;
		private JComponent jFechaFin1;
		private JComponent jFechaIni2;
		private JComponent jFechaFin2;*/
		private JTextField jMesInicial;
		private JTextField jMesFinal;
		private JTextField jYearPrincipal;
		private JTextField jYearCompara;
		private JComboBox jOrigen;
		
		private JCheckBox  jKilos;

		public ReportForm() {
			super("Comparativo Ventas Por Linea");
			parametros=new HashMap<String, Object>();
			model=new PresentationModel(this);
		}
		
		private void initComponents(){
			
			/*jFechaIni1=Binder.createDateComponent(model.getModel("fechaInicial1"));
			jFechaFin1=Binder.createDateComponent(model.getModel("fechaFinal1"));
			jFechaIni2=Binder.createDateComponent(model.getModel("fechaInicial2"));
			jFechaFin2=Binder.createDateComponent(model.getModel("fechaFinal2"));*/
			jCliente=Binder.createClientesBinding(model.getModel("cliente"));
			jMesInicial=new JTextField();
			jMesFinal=new JTextField();
			jYearPrincipal=new JTextField();
			jYearCompara=new JTextField();
			Object[] values={"CREDITO","CONTADO","TODOS"};
			jOrigen= new JComboBox(values);
			jKilos =new JCheckBox();
			
			
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			//CellConstraints cc=new CellConstraints();
			final FormLayout layout=new FormLayout(
					"l:40dlu,30dlu,60dlu, 3dlu, " +
					"l:40dlu,30dlu,p:g,2dlu,p,2dlu,p " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
			/*builder.append("Fecha Inicial1", jFechaIni1);
			builder.append("Fecha Final1",jFechaFin1);
			builder.nextLine();
			builder.append("Fecha Inicial2", jFechaIni2);
			builder.append("Fecha Final2",jFechaFin2);*/
			builder.append("Cliente",jCliente,5);
			builder.nextLine();
			builder.append("Mes Inicial",jMesInicial,1);
			builder.append("Mes Final", jMesFinal);
			builder.nextLine();
			builder.append("Año 1 ", jYearPrincipal,1);
			builder.append("Año 2 ", jYearCompara);
			builder.nextLine();
			builder.append("Origen",jOrigen,3);
			builder.nextLine();
			//builder.append("No. Clientes",jClientes);
			//builder.nextLine();
			builder.append("Kilos",jKilos);
			
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			
		
			
			/*parametros.put("FECHA_INI", model.getValue("fechaInicial1"));
			parametros.put("FECHA_FIN", model.getValue("fechaFinal1"));
			parametros.put("FECHA_INI_2", model.getValue("fechaInicial2"));
			parametros.put("FECHA_FIN_2", model.getValue("fechaFinal2"));*/
			Cliente c=(Cliente)model.getValue("cliente");
			parametros.put("CLIENTE_ID", c.getId());
			Integer mesIni=Integer.parseInt(jMesInicial.getText());
			parametros.put("MES_INI", mesIni);
			Integer mesFin=Integer.parseInt(jMesFinal.getText());
			parametros.put("MES_FIN",mesFin);
			Integer year1=Integer.parseInt(jYearPrincipal.getText());
			parametros.put("YEAR1",year1);
			Integer year2=Integer.parseInt(jYearCompara.getText());
			parametros.put("YEAR2",year2);
			String ori;
			if(jOrigen.getSelectedItem().equals("TODOS"))
				ori="%";
			else
				ori=(String) jOrigen.getSelectedItem();
			parametros.put("ORIGEN",ori);
		//	 Integer clientes=Integer.parseInt(jClientes.getText());
		//	parametros.put("NO_CLIENTES",clientes);
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}

		public Cliente getCliente() {
			return cliente;
		}
		public void setCliente(Cliente cliente) {
			Object oldValue=this.cliente;
			this.cliente = cliente;
			firePropertyChange("cliente", oldValue, cliente);
		}

/*
		public Date getFechaFinal1() {
			return fechaFinal1;
		}
		public void setFechaFinal1(Date fechaFinal1) {
			Object oldValue=this.fechaFinal1;
			this.fechaFinal1 = fechaFinal1;
			firePropertyChange("fechaFinal", oldValue, fechaFinal1);
		}

		public Date getFechaInicial1() {
			return fechaInicial1;
		}
		public void setFechaInicial1(Date fechaInicial1) {
			Object oldValue=this.fechaInicial1;
			this.fechaInicial1 = fechaInicial1;
			firePropertyChange("fechaInicial1", oldValue, fechaInicial1);
		}

		public Date getFechaFinal2() {
			return fechaFinal2;
		}
		public void setFechaFinal2(Date fechaFinal2) {
			Object oldValue=this.fechaFinal2;
			this.fechaFinal2 = fechaFinal2;
			firePropertyChange("fechaFinal", oldValue, fechaFinal2);
		}

		public Date getFechaInicial2() {
			return fechaInicial2;
		}
		public void setFechaInicial2(Date fechaInicial2) {
			Object oldValue=this.fechaInicial2;
			this.fechaInicial2 = fechaInicial2;
			firePropertyChange("fechaInicial2", oldValue, fechaInicial2);
		}*/
	

		
		
		
		 
		
	}
	
	public static void run(){
		ComparativoVentasXLineaXCte action=new ComparativoVentasXLineaXCte();
		action.execute();
		
	}
	
	public static void main(String[] args) {
		ComparativoVentasXLineaXCte action=new ComparativoVentasXLineaXCte();
		action.execute();
	}

}
