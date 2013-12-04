package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Reporte para la impresion de Cargos tipo cheque deuelto
 * 
 * @author Ruben Cancino
 *
 */
public class EstadoDeCtaGralCheForm extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/EstadoDeCuentaGralChe.jasper"), form.getParametros());
		}
		form.dispose();
	}
	
	/**
	 * Reporte para la impresion de Cargos tipo cheque deuelto
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		
		
		
		
		
		private JXDatePicker jFecha;


		public ReportForm() {
			super("Estado de Cuenta Gral.");
			parametros=new HashMap<String, Object>();
			
		}
		
		private ValueHolder clienteHolder=new ValueHolder(null);
		
		private void initComponents(){			
			
			jFecha=new JXDatePicker();
			jFecha.setFormats("dd/MM/yyyy");
			
		}
		
		

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout(
					"l:60dlu,3dlu,90dlu, 3dlu, " +
					"l:60dlu,3dlu,f:90dlu:g " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
			builder.append("Fecha De Corte  ",jFecha);
			
			
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			
			parametros.put("FECHA",jFecha.getDate());
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}	
		
	}
	
	public static void run(){
		EstadoDeCtaGralCheForm action=new EstadoDeCtaGralCheForm();
		action.execute();
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				EstadoDeCtaGralCheForm.run();
			}
			
		});		
	}

}
