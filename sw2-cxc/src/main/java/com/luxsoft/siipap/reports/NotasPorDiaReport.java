package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.reports.EstadoDeCtaGralCheForm.ReportForm;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Genera el reporte de Estado de cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
public class NotasPorDiaReport extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			System.out.println("Parametros:  "+form.getParametros());
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/NotasDeBonificacionXDia.jasper"), form.getParametros());
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
		
		
		private Date fechaInicial;
		private Date fechaFinal;
		private JComboBox tipoBox;
		private JComboBox modoBox;

		
		private final PresentationModel model;
		
	
		private JComponent jFechaIni;
		@SuppressWarnings("unused")
		private JComponent jFechaFin;

		
		public ReportForm() {
			super("Modificaciones clientes credito");
			parametros=new HashMap<String, Object>();
			model=new PresentationModel(this);
		}
		
		private void initComponents(){
			
		
			
			jFechaIni=Binder.createDateComponent(model.getModel("fechaInicial"));
			jFechaFin=Binder.createDateComponent(model.getModel("fechaFinal"));
			Object[] values={"CREDITO","CONTADO"};
			modoBox= new JComboBox(values);
		
			Object[] values1={"NOTA_BON","NOTA_DEV"};
			tipoBox= new JComboBox(values1);
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			//CellConstraints cc=new CellConstraints();
			final FormLayout layout=new FormLayout(
					"l:40dlu,30dlu,60dlu, 3dlu, " +
					"l:40dlu,30dlu,p:g,2dlu,p,2dlu,p " +
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Modo Venta",modoBox,5);
			builder.nextLine();
			builder.append("Tipo Nota",tipoBox,5);
			
			builder.nextLine();
			builder.append("Fecha Inicio ",jFechaIni);
			builder.append("Fecha Fin ",jFechaFin);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
		
				
				parametros.put("FECHA_INI", model.getValue("fechaInicial"));
				parametros.put("FECHA_FIN", model.getValue("fechaFinal"));
				parametros.put("MODO", modoBox.getSelectedItem().toString());
				parametros.put("TIPO_NOTA", tipoBox.getSelectedItem().toString());
				
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}


		public Date getFechaFinal() {
			return fechaFinal;
		}
		public void setFechaFinal(Date fechaFinal) {
			Object oldValue=this.fechaFinal;
			this.fechaFinal = fechaFinal;
			firePropertyChange("fechaFinal", oldValue, fechaFinal);
		}

		public Date getFechaInicial() {
			return fechaInicial;
		}
		public void setFechaInicial(Date fechaInicial) {
			Object oldValue=this.fechaInicial;
			this.fechaInicial = fechaInicial;
			firePropertyChange("fechaInicial", oldValue, fechaInicial);
		}

		
		
		
		 
		
	}
	
	public static void run(){
		NotasPorDiaReport action=new NotasPorDiaReport();
		action.execute();
	}
	
	public static void main(String[] args) {
		NotasPorDiaReport action=new NotasPorDiaReport();
		action.execute();
	}

}
