package com.luxsoft.siipap.reports;

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
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.ventas.model.Vendedor;


/**
 * Genera el reporte de Estado de cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
public class VentasPorVendedorReportForm extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
				
			if(form.jKilos.isSelected())
			ReportUtils.viewReport(ReportUtils.toReportesPath("BI/ClientesPorVendedorEnPesosComp.jasper"), form.getParametros());
			else 
		    ReportUtils.viewReport(ReportUtils.toReportesPath("BI/ClientesPorVendedorEnKilosComp.jasper"), form.getParametros());
				
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
		
		
		/*private Date fechaInicial1;
		private Date fechaFinal1;
		private Date fechaInicial2;
		private Date fechaFinal2;*/
		//private JComboBox sucursalControl;
		private JComboBox vendedorBox;
		private Integer mesInicial;
		private Integer mesfinal;
		private Integer yearPrincipal;
		private Integer yearCompara;
		//public JCheckBox global;
		
		private final PresentationModel model;
		
		//private JComponent jCliente;
		/*private JComponent jFechaIni1;
		private JComponent jFechaFin1;
		private JComponent jFechaIni2;
		private JComponent jFechaFin2;*/
		private JTextField jMesInicial;
		private JTextField jMesFinal;
		private JTextField jYearPrincipal;
		private JTextField jYearCompara;
		private JComboBox jOrigen;
		//private JTextField jClientes;
		private JCheckBox  jKilos;

		public ReportForm() {
			super("Comparativo mejores clientes");
			parametros=new HashMap<String, Object>();
			model=new PresentationModel(this);
		}
		
		private void initComponents(){
			//jClientes=new JTextField();
			/*jFechaIni1=Binder.createDateComponent(model.getModel("fechaInicial1"));
			jFechaFin1=Binder.createDateComponent(model.getModel("fechaFinal1"));
			jFechaIni2=Binder.createDateComponent(model.getModel("fechaInicial2"));
			jFechaFin2=Binder.createDateComponent(model.getModel("fechaFinal2"));*/
			jMesInicial=new JTextField();
			jMesFinal=new JTextField();
			jYearPrincipal=new JTextField();
			jYearCompara=new JTextField();
			Object[] values={"CREDITO","CONTADO","TODOS"};
			jOrigen= new JComboBox(values);
			jKilos =new JCheckBox();
			//sucursalControl=createSucursalControl();
			//global=new JCheckBox("",false);
			
			Object[] vends=ServiceLocator2.getCXCManager().getVendedores().toArray(new Vendedor[0]);
			vendedorBox=new JComboBox(vends);
						
			
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
			builder.append("Vendedor",vendedorBox);
			
			builder.nextLine();
			builder.append("Mes Inicial",jMesInicial,1);
			builder.nextLine();
			builder.append("Mes Final", jMesFinal);
			builder.nextLine();
			builder.append("Año 1 ", jYearPrincipal,1);
			builder.nextLine();
			builder.append("Año 2 ", jYearCompara);
			builder.nextLine();
			builder.append("Origen",jOrigen,3);
			builder.nextLine();
			//builder.append("No. Clientes",jClientes);
			//builder.nextLine();
			builder.append("Kilos",jKilos);
			builder.nextLine();
			//builder.append("Sucursal",sucursalControl);
			//builder.append("Global",global);
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		/*private JComboBox createSucursalControl() {			
			final JComboBox box = new JComboBox(ServiceLocator2.getLookupManager().getSucursalesOperativas().toArray());
			Sucursal local=ServiceLocator2.getConfiguracion().getSucursal();
			for(int index=0;index<box.getModel().getSize();index++){
				Sucursal s=(Sucursal)box.getModel().getElementAt(index);
				if(s.equals(local)){
					box.setSelectedIndex(index);
					break;
				}
			}
			return box;
		}
		
		private Long getSucursal(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			return selected.getId();
		}*/
		@Override
		public void doApply() {			
			super.doApply();
			
		
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
			
			
				Vendedor v=(Vendedor)vendedorBox.getSelectedItem();
				String id=v.getId().toString();
				parametros.put("VENDEDOR",id);
			
				
			/* Integer clientes=Integer.parseInt(jClientes.getText());
			parametros.put("NO_CLIENTES",clientes);
			
			if(global.isSelected())
				parametros.put("SUCURSAL","%");
			else
				parametros.put("SUCURSAL", getSucursal().toString());*/
		}
		
		

		public Map<String, Object> getParametros() {
			return parametros;
		}

		

	

		
		
		
		 
		
	}
	
	public static void run(){
		VentasPorVendedorReportForm action=new VentasPorVendedorReportForm();
		action.execute();
		
	}
	
	public static void main(String[] args) {
		VentasPorVendedorReportForm action=new VentasPorVendedorReportForm();
		action.execute();
	}

}
