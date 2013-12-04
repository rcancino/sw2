package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.embarque.Chofer;


/**
 * Genera el reporte de Comisiones por chofer
 * 
 * @author Ruben Cancino
 *
 */
public class ControlDeTiemposDeEnvioReport extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){			
			ReportUtils.viewReport(ReportUtils.toReportesPath("embarques/ControlDeTiemposDeEnvio.jasper"), form.getParametros());
		}
		form.dispose();
	}
	
	/**
	 * Forma para el reporte de comisiones por chofer
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros=new HashMap<String, Object>();
		
		
		private JXDatePicker fechaInicial;
		private JXDatePicker fechaFinal;
		private JComboBox choferes;
		private JCheckBox todos;
		private JComboBox sucursalControl;
		private JCheckBox global;
		

		public ReportForm() {
			super("Control De Embarques");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			
			List<Chofer> data=ServiceLocator2.getUniversalDao().getAll(Chofer.class);
			choferes=new JComboBox(data.toArray());
			sucursalControl=createSucursalControl();
			todos=new JCheckBox("",false);
			global=new JCheckBox("",false);
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

		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			builder.append("Chofer",choferes);
			builder.append("Todos",todos);
			builder.append("Sucursal",sucursalControl);
			builder.append("Todas",global);
			
			
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Control De Embarques","").getHeader();
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
			if(global.isSelected())
				parametros.put("SUCURSAL","%");
			else
			parametros.put("SUCURSAL", getSucursal().toString());
			Chofer ch=(Chofer)choferes.getSelectedItem();
			 if(todos.isSelected())
				 parametros.put("CHOFER", "%");	
			 else{
				 if(ch!=null)
				  parametros.put("CHOFER", ch.getId().toString()); 
			 }
			 
			
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
	}
	
	public static void run(){
		ControlDeTiemposDeEnvioReport action=new ControlDeTiemposDeEnvioReport();
		action.execute();
	}
	
	public static void main(String[] args) {
		run();
	}

}
