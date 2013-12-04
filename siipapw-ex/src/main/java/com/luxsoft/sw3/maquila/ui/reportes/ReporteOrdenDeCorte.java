package com.luxsoft.sw3.maquila.ui.reportes;


import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw3.maquila.model.Almacen;



/**
 * Forma para la ejecuccion del reporte de mermas en maquila
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class ReporteOrdenDeCorte extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		 ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){			
			ReportUtils.viewReport(ReportUtils.toReportesPath("maquila/Orden_de_Corte.jasper"), form.getParametros());
		}
		form.dispose();
	}
	
	/**
	 * 
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros=new HashMap<String, Object>();
		
		private JComboBox almacenControl;
		private JXDatePicker fecha;
		//private JXDatePicker fechaFinal;
		
		

		public ReportForm() {
			super("Orden de Corte");
		}
		
		private void initComponents(){
			fecha=new JXDatePicker();
			fecha.setFormats("dd/MM/yyyy");
			//fechaFinal=new JXDatePicker();
			//fechaFinal.setFormats("dd/MM/yyyy");
			almacenControl=createAlmacenControl();
			
			
		}
		
		private JComboBox createAlmacenControl() {
			List data=ServiceLocator2.getHibernateTemplate().find("from Almacen a ");
			final JComboBox box = new JComboBox(data.toArray(new Object[0]));
			return box;
		}
		
		private Long getAlmacen(){
			Almacen selected=(Almacen)almacenControl.getSelectedItem();
			return selected.getId();
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha ",fecha);
			//builder.append("Fecha Final",fechaFinal);
			builder.append("Almacen",almacenControl);
			
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Orden de Corte","").getHeader();
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
			parametros.put("FECHA", fecha.getDate());
			//parametros.put("FECHA_FIN", fechaFinal.getDate());
			parametros.put("ALMACEN_ID", getAlmacen());
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
	}
	
	public static void run(){
		ReporteOrdenDeCorte action=new ReporteOrdenDeCorte();
		action.execute();
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		run();
	}

}
