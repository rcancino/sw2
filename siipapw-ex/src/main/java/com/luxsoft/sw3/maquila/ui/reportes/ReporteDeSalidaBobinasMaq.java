package com.luxsoft.sw3.maquila.ui.reportes;


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
public class ReporteDeSalidaBobinasMaq extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		 ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){			
			ReportUtils.viewReport(ReportUtils.toReportesPath("maquila/SalidaDeBobinasMaq.jasper"), form.getParametros());
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
		private JXDatePicker fechaInicial;
		private JXDatePicker fechaFinal;
		private JCheckBox general;
		
		

		public ReportForm() {
			super("Reporte Entradas Costeadas");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			almacenControl=createAlmacenControl();
			general=new JCheckBox("",false);
			
			
			
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
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			builder.append("Almacen",almacenControl);
			builder.append("Todos",general);
			
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Ordenes De Entradas Costeadas","").getHeader();
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
			String alm=String.valueOf(getAlmacen());
			if(general.isSelected())
				alm="%";
			parametros.put("MAQUILADOR",alm);
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
	}
	
	public static void run(){
		ReporteDeSalidaBobinasMaq action=new ReporteDeSalidaBobinasMaq();
		action.execute();
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		run();
	}

}
