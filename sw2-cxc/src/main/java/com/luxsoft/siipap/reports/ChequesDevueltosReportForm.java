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
public class ChequesDevueltosReportForm extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/DevolucionDeCheque.jasper"), form.getParametros());
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
		
		
		
		
		
		private JComponent jCliente;
		private JXDatePicker jFechaIni;
		private JXDatePicker jFechaFin;

		public ReportForm() {
			super("Cobranza Crédito");
			parametros=new HashMap<String, Object>();
			
		}
		
		private ValueHolder clienteHolder=new ValueHolder(null);
		
		private void initComponents(){			
			jCliente=Binder.createClientesBinding(clienteHolder);
			jFechaIni=new JXDatePicker();
			jFechaIni.setFormats("dd/MM/yyyy");
			jFechaFin=new JXDatePicker();
			jFechaFin.setFormats("dd/MM/yyyy");
		}
		
		

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout(
					"l:40dlu,3dlu,110dlu, 3dlu, " +
					"l:40dlu,3dlu,f:110dlu:g " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Cliente",jCliente,5);
			builder.nextLine();
			builder.append("Fecha Ini ",jFechaIni);
			builder.append("Fecha Fin ",jFechaFin,true);
			
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			Cliente c=(Cliente)clienteHolder.getValue();
			if(c!=null)
				parametros.put("CLIENTE", c.getClave());
			else
				parametros.put("CLIENTE", "%");
			parametros.put("FECHA_INI",jFechaIni.getDate());
			parametros.put("FECHA_FIN",jFechaFin.getDate());
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}	
		
	}
	
	public static void run(){
		ChequesDevueltosReportForm action=new ChequesDevueltosReportForm();
		action.execute();
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				ChequesDevueltosReportForm.run();
			}
			
		});		
	}

}
