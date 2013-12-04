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
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
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
public class ComparativoVentasPorSucursal extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			
			ReportUtils.viewReport(ReportUtils.toReportesPath("BI/VentasPorSucursalComp.jasper"), form.getParametros());
			
				
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
		
		
		
		private JComboBox sucursalControl;
		private Integer mesInicial;
		private Integer mesfinal;
		private Integer yearPrincipal;
		private Integer yearCompara;
		public JCheckBox global;
		
		private final PresentationModel model;
		
		
		private JTextField jMesInicial;
		private JTextField jMesFinal;
		private JTextField jYearPrincipal;
		private JTextField jYearCompara;
		private JComboBox jOrigen;
		private JTextField jClientes;
	

		public ReportForm() {
			super("Comparativo Ventas Por Sucursal");
			parametros=new HashMap<String, Object>();
			model=new PresentationModel(this);
		}
		
		private void initComponents(){
			
			jMesInicial=new JTextField();
			jMesFinal=new JTextField();
			jYearPrincipal=new JTextField();
			jYearCompara=new JTextField();
			Object[] values={"CREDITO","CONTADO","TODOS"};
			jOrigen= new JComboBox(values);
		
			sucursalControl=createSucursalControl();
			global=new JCheckBox("",false);
			
			
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			
			final FormLayout layout=new FormLayout(
					"l:40dlu,30dlu,60dlu, 3dlu, " +
					"l:40dlu,30dlu,p:g,2dlu,p,2dlu,p " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
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
			builder.append("Sucursal",sucursalControl);
			builder.append("Global",global);
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		private JComboBox createSucursalControl() {			
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
		
		private String getSucursal(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			return selected.getId().toString();
		}
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
				
			if(global.isSelected())
				parametros.put("SUCURSAL","%");
			else
				parametros.put("SUCURSAL", getSucursal());
		}
		
		

		public Map<String, Object> getParametros() {
			return parametros;
		}


		
	}
	
	public static void run(){
		ComparativoVentasPorSucursal action=new ComparativoVentasPorSucursal();
		action.execute();
		
	}
	
	public static void main(String[] args) {
		ComparativoVentasPorSucursal action=new ComparativoVentasPorSucursal();
		action.execute();
	}

}
