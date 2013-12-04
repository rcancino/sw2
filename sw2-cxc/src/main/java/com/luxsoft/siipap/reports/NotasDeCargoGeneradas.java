package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Genera el reporte de Pagos Con Nota
 * 
 * @author Ruben Cancino
 *
 */
public class NotasDeCargoGeneradas extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			//ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/NotasDeCredito.jasper"), form.getParametros());
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/NotasDeCargo.jasper"), form.getParametros());
		}
		form.dispose();
	}
	
	/**
	 * Forma para el reporte de cobranza
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;		
		protected JXDatePicker f1;
		protected JXDatePicker f2;
		protected JComboBox operacionBox; 
		
		public ReportForm() {
			super("Notas de Cargo");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){			
			f1=new JXDatePicker();
			f2=new JXDatePicker();
			f1.setFormats(new String[]{"dd/MM/yyyy"});
			f2.setFormats(new String[]{"dd/MM/yyyy"});
			operacionBox=new JComboBox(OrigenDeOperacion.values());
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout(
					"l:40dlu,3dlu,f:p:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",f1,true);			
			builder.append("Fecha Final  ",f2,true);
			builder.append("Tipo  ",operacionBox,true);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("FECHA_INI", f1.getDate());
			parametros.put("FECHA_FIN", f2.getDate());
			OrigenDeOperacion origen=(OrigenDeOperacion)operacionBox.getSelectedItem();
			parametros.put("ORIGEN", origen.name());
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		
	}
	
	public static void run(){
		NotasDeCargoGeneradas action=new NotasDeCargoGeneradas();
		action.execute();
	}
	
	public static void main(String[] args) {
		NotasDeCargoGeneradas action=new NotasDeCargoGeneradas();
		action.execute();
	}

}
