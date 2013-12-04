package com.luxsoft.siipap.pos.ui.reports;

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
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.reports.Discrepancias.ReportForm.ParamLabelValue;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

import com.luxsoft.sw3.services.Services;

/**
 * 
 * @author Ruben Cancino
 *
 */
public class SaldosPendienteXAbogadoReportForm extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils2.runReport("cxc/SaldosPendientesPorAbogado.jasper", form.getParametros());
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
		
		private final Map<String, Object> parametros=new HashMap<String, Object>();
		
		
		private JXDatePicker fechaDeCorte;
		private JComboBox abogado;
		
		
		

		public ReportForm() {
			super("Juridico");
		}
private ParamLabelValue getParam(JComboBox box){
			
			return (ParamLabelValue)box.getSelectedItem();
		}
		
		public  class ParamLabelValue{
			
			private final String name;
			private final Object value;
			
			public ParamLabelValue(String name, Object value) {			
				this.name = name;
				this.value = value;
			}

			public String getName() {
				return name;
			}

			public Object getValue() {
				return value;
			}
			
			public String toString(){
				return name;
			}
			
			public String stringValue(){
				return value.toString();
			}
			
			public Integer intValue(){
				return Integer.valueOf(stringValue());
			}
			
			public Number numberValue(){
				return (Number)value;
			}
			
			
		}
		private void initComponents(){
			fechaDeCorte=new JXDatePicker();
			fechaDeCorte.setFormats("dd/MM/yyyy");
			Object abogados[]={//"MARTHA SILVA CASARIN","JOSE ANTONIO JACUINDE GUTIERREZ","CENTRAL DE COBRANZA","FRANCISCO FRIAS ( 2000 PLUS)"
					new ParamLabelValue("MARTHA SILVA CASARIN","MARTHA SILVA CASARIN"),
					new ParamLabelValue("JOSE ANTONIO JACUINDE GUTIERREZ","JOSE ANTONIO JACUINDE GUTIERREZ"),
					new ParamLabelValue("CENTRAL DE COBRANZA","CENTRAL DE COBRANZA"),
					new ParamLabelValue("FRANCISCO FRIAS ( 2000 PLUS)","FRANCISCO FRIAS ( 2000 PLUS)"),
					new ParamLabelValue("TODOS","%")};
			abogado=new JComboBox(abogados);
			
			
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha De Corte",fechaDeCorte);
			builder.append("Abogado",abogado);
			
			
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Saldos pendientes por abogado","").getHeader();
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
			parametros.put("FECHA", fechaDeCorte.getDate());
			parametros.put("ABOGADO",getParam(abogado).getValue());
				
			
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void run(){
		SaldosPendienteXAbogadoReportForm action=new SaldosPendienteXAbogadoReportForm();
		action.execute();
	}
	
	public static void main(String[] args) {
		run();
	}

}
