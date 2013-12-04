package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Genera el reporte general de antiguedad de saldos
 * 
 * @author Ruben Cancino
 *
 */
public class AntiguedadDeSaldoReportForm extends SWXAction{

	@Override
	public void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/AntiguedadSaldosGral.jasper"), form.getParametros());
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
		private JComboBox box1;
		private JComboBox box2;

		public ReportForm() {
			super("Antigüedad de Saldos");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){			
			box1=new JComboBox(new String[]{"Vencido","Cliente","Atraso Max","Saldo","Por Vencer"});
			box2=new JComboBox(new String[]{"Descendente","Ascendente"});
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout(
					"l:p,3dlu,p, 3dlu, " +
					"l:p,3dlu,f:p:g " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Ordenado por: ",box1,5);
			builder.nextLine();			
			builder.append("En forma: ",box2,true);
			
			
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			String val1=(String)box1.getSelectedItem();
			String val2=(String)box2.getSelectedItem();
			parametros.put("ORDER", getOrden(val1));
			parametros.put("FORMA", getForma(val2));
			System.out.println("Parametros: "+parametros);
		}
		
		public int getOrden(String s){
			if(s.equalsIgnoreCase("Cliente"))	return 1;
			else if(s.equalsIgnoreCase("Atraso Max")) return 6;
			else if(s.equalsIgnoreCase("Saldo")) return 7;
			else if(s.equalsIgnoreCase("Por Vencer")) return 8;
			else if(s.equalsIgnoreCase("Vencido")) return 9;
			return 1;
		}
		
		public String getForma(String s){
			if(s.startsWith("Asc"))
				return "ASC";
			else
				return "DESC";
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}

	}
	
	public static void run(){
		AntiguedadDeSaldoReportForm action=new AntiguedadDeSaldoReportForm();
		action.execute();
	}
	
	public static void main(String[] args) {
		AntiguedadDeSaldoReportForm action=new AntiguedadDeSaldoReportForm();
		action.execute();
	}

}
