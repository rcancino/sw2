package com.luxsoft.siipap.pos.ui.reports;

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
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.services.Services;

/**
 * Genera el reporte de Estado de cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
public class ControlDePagosCODReportForm extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils2.runReport("embarques/ControlDePagosCOD.jasper",form.getParametros());
		}
		form.dispose();
	}
	
	/**
	 * Forma para el reporte de Pagos COD
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros=new HashMap<String, Object>();
		
		
		private JXDatePicker fechaInicial;
		private JComboBox choferes;
		
		

		public ReportForm() {
			super("Pagos COD");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			List<Chofer> data=Services.getInstance().getUniversalDao().getAll(Chofer.class);
			choferes=new JComboBox(data.toArray());
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Chofer",choferes);
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Control de Pagos COD","").getHeader();
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
			parametros.put("FECHA", fechaInicial.getDate());
			Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			Chofer ch=(Chofer)choferes.getSelectedItem();
			if(ch!=null)
				parametros.put("CHOFER", ch.getId());
			parametros.put("SUCURSAL", suc.getNombre());
			System.out.println(parametros);
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
	}
	
	public static void run(){
		ControlDePagosCODReportForm action=new ControlDePagosCODReportForm();
		action.execute();
	}
	
	public static void main(String[] args) {
		run();
	}

}
