package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
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
public class CobranzaCredito extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/Cobranza4.jasper"), form.getParametros());
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
		
		private Cliente cliente;
		private Date fechaInicial;
		private Date fechaFinal;
		private int cobrador;
		
		
		private final PresentationModel model;
		
		private JComponent jCliente;
		private JComponent jFechaIni;
		private JComponent jFechaFin;
		private JComponent jcobrador;
		private JComboBox origen=new JComboBox(OrigenDeOperacion.values());
		
		
		

		public ReportForm() {
			super("Cobranza Crédito");
			parametros=new HashMap<String, Object>();
			model=new PresentationModel(this);
		}
		
		private void initComponents(){			
			jCliente=Binder.createClientesBinding(model.getModel("cliente"));
			jFechaIni=Binder.createDateComponent(model.getModel("fechaInicial"));
			jFechaFin=Binder.createDateComponent(model.getModel("fechaFinal"));
			jcobrador=CXCBindings.createCobradorBinding(model.getModel("cobrador"));
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
			//builder.append("Cobrador",jcobrador,true);
			builder.append("Fecha Ini ",jFechaIni);
			builder.append("Fecha Fin ",jFechaFin,true);
			builder.append("Origen",origen);
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			if(model.getValue("cliente")!=null){
				Cliente c=(Cliente)model.getValue("cliente");
				parametros.put("CLIENTE", c.getClave());
			}
			else
				parametros.put("CLIENTE", "%");
			final Date fechaIni=(Date)model.getValue("fechaInicial");
			final Date fechaFin=(Date)model.getValue("fechaFinal");
			parametros.put("FECHA_INI",fechaIni );
			parametros.put("FECHA_FIN",fechaFin );
			OrigenDeOperacion ori=(OrigenDeOperacion)origen.getSelectedItem();
			parametros.put("ORIGEN",ori.name());
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}

		public Cliente getCliente() {
			return cliente;
		}
		public void setCliente(Cliente cliente) {
			Object oldValue=this.cliente;
			this.cliente = cliente;
			firePropertyChange("cliente", oldValue, cliente);
		}

		public Date getFechaInicial() {
			return fechaInicial;
		}
		public void setFechaInicial(Date fechaInicial) {
			Object oldValue=this.fechaInicial;
			this.fechaInicial = fechaInicial;
			firePropertyChange("fechaInicial", oldValue, fechaInicial);
		}

		public int getCobrador() {
			return cobrador;
		}

		public void setCobrador(int cobrador) {
			int oldValue=this.cobrador;
			this.cobrador = cobrador;
			firePropertyChange("cobrador", oldValue, cobrador);
		}

		public Date getFechaFinal() {
			return fechaFinal;
		}

		public void setFechaFinal(Date fechaFinal) {
			Object old=this.fechaFinal;
			this.fechaFinal = fechaFinal;
			firePropertyChange("fechaFinal", old, fechaFinal);
		}

		
		
		
		
	}
	
	public static void run(){
		CobranzaCredito action=new CobranzaCredito();
		action.execute();
	}
	
	public static void main(String[] args) {
		CobranzaCredito action=new CobranzaCredito();
		action.execute();
	}

}
