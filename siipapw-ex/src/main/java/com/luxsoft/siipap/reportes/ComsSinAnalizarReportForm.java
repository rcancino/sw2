package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;


import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;


import com.luxsoft.siipap.cxp.ui.selectores.ProveedorControl;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;



/**
 * 
 * @author Ruben Cancino
 *
 */
public class ComsSinAnalizarReportForm extends SWXAction {
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			
			if (form.com.isSelected()){
				System.out.println("Parametros enviados: "+form.getParametros());
				ReportUtils.viewReport(ReportUtils.toReportesPath("invent/ComSinAnalizar.jasper"), form.getParametros());
				}
			if (form.maq.isSelected()){
				System.out.println("Parametros enviados: "+form.getParametros());
				ReportUtils.viewReport(ReportUtils.toReportesPath("invent/MaqSinAnalizar.jasper"), form.getParametros());
				}
			if (form.trs.isSelected()){
				System.out.println("Parametros enviados: "+form.getParametros());
				ReportUtils.viewReport(ReportUtils.toReportesPath("invent/HojeoSinAnalizar.jasper"), form.getParametros());
				}
			if (form.hoj.isSelected()){
				System.out.println("Parametros enviados: "+form.getParametros());
				ReportUtils.viewReport(ReportUtils.toReportesPath("invent/TrsMetSinAnalizar.jasper"), form.getParametros());
				}
			
			
			
			
		}
		form.dispose();
	}
	
	/**
	 * Forma para el reporte de cobranza
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog implements PropertyChangeListener{
		
		private final Map<String, Object> parametros=new HashMap<String, Object>();
		
		ProveedorControl control;
		private ValueHolder proveedorHolder=new ValueHolder(null);
		private JCheckBox todosProv;
		private JXDatePicker fechaInicial;
		private JXDatePicker fechaFinal;
		private JComboBox sucursalControl;
		private JCheckBox todasLasSucursales;
		private JLabel periodolbl=new JLabel(" al ");
		private JCheckBox com;
		private JCheckBox maq;
		private JCheckBox trs;
		private JCheckBox hoj;
		
		

		public ReportForm() {
			super("Compras");
		}
		
		private void initComponents(){
			
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			sucursalControl=createSucursalControl();
			todasLasSucursales=new JCheckBox("",false);
			todasLasSucursales.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					sucursalControl.setEnabled(!todasLasSucursales.isSelected());
				}
			});
			proveedorHolder.addPropertyChangeListener(this);
			todosProv=new JCheckBox("Todos",false);
			buildProveedorControl();
			com=new JCheckBox("",false);
			maq=new JCheckBox("",false);
			trs=new JCheckBox("",false);
			hoj=new JCheckBox("",false);
			
			
			
			
			
		}
		
		private void buildProveedorControl() {
			control=new ProveedorControl(proveedorHolder);
			todosProv.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					control.getControl().setEnabled(!todosProv.isSelected());
				}
			});
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,2dlu,70dlu,3dlu,p,2dlu,70dlu:g,2dlu,p",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Proveedor",control.getControl(),5);
			builder.append(todosProv);
			builder.nextLine();
			builder.append("Fecha Inicial",fechaInicial);
			builder.nextLine();
			builder.append("Fecha Final",fechaFinal);
			builder.nextLine();
			builder.append("Sucursal",sucursalControl);
			builder.append("Todas",todasLasSucursales);
			builder.nextLine();
			builder.append("Com",com);
			builder.append("Maq",maq);
			builder.nextLine();
			builder.append("Trs",trs);
			builder.append("Hojeo",hoj);
			
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Coms Sin Analizar ","").getHeader();
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

		@Override
		protected JComponent buildContent() {			
			JPanel panel=new JPanel(new BorderLayout());			
			panel.add(buildForm(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		private Long getSucursal(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			return selected.getId();
		}
		
		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("FECHA_INI", fechaInicial.getDate());
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			String suc=String.valueOf(getSucursal());
			if(todasLasSucursales.isSelected())
			suc="%";
			parametros.put("SUCURSAL", suc);
			if(proveedorHolder.getValue()!=null){
				Proveedor p=(Proveedor)proveedorHolder.getValue();
				parametros.put("PROVEEDOR", p.getClave());
			}else
				parametros.put("PROVEEDOR", "%");
			
			logger.info("Parametros de reporte:"+parametros);
			
		}
		public void propertyChange(PropertyChangeEvent evt) {
			if(!todosProv.isSelected())
				getOKAction().setEnabled(evt.getNewValue()!=null);
			
		}
		
		
					
		

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void run(){
		ComsSinAnalizarReportForm action=new ComsSinAnalizarReportForm();
		action.execute();
	}
	
	public static void main(String[] args) {
		run();
	}

	

}
