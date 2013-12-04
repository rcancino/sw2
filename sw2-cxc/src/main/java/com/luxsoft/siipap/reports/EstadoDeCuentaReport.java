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
public class EstadoDeCuentaReport extends SWXAction{

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			if(form.chequesDevueltos.isSelected()){				
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/EstadoDeCuentaGralChe.jasper"), form.getParametros());
				return;
				
			}
			if(form.getCliente()!=null && form.getTodos().isSelected()==false){				
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/EstadoDeCuentaCte.jasper"), form.getParametros());
				
			}
			if(form.getCliente()==null && form.getTodos().isSelected()==true ){
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/EstadoDeCuentaCte.jasper"), form.getParametros());
				
			}		
			if(form.getCliente()==null && form.getTodos().isSelected()==false ){				
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/EstadoDeCuentaGral.jasper"), form.getParametros());
			}
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
		private JCheckBox todos;
		private JTextField comentario;
		
		private final PresentationModel model;
		
		private JComponent jCliente;
		private JComponent jFechaIni;
		@SuppressWarnings("unused")
		private JComponent jFechaFin;
		private JComboBox origen=new JComboBox(OrigenDeOperacion.values());
		
		private JCheckBox chequesDevueltos;
		

		public ReportForm() {
			super("Estado de Cuenta");
			parametros=new HashMap<String, Object>();
			model=new PresentationModel(this);
		}
		
		private void initComponents(){
			
			todos=new JCheckBox();
			todos.setSelected(false);
			jCliente=Binder.createClientesBinding(model.getModel("cliente"));
			jFechaIni=Binder.createDateComponent(model.getModel("fechaInicial"));
			jFechaFin=Binder.createDateComponent(model.getModel("fechaFinal"));
			comentario=new JTextField(40);
			chequesDevueltos=new JCheckBox("",false);
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
			builder.append("Cliente",jCliente,5);
			builder.append("Todos",todos);
			builder.nextLine();
			builder.append("Fecha corte ",jFechaIni);
			builder.append("Origen",origen);
			builder.append("Che Dev",chequesDevueltos);
			builder.append("Comentario",comentario,5);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			if(model.getValue("cliente")!=null && todos.isSelected()==false){
				Cliente c=(Cliente)model.getValue("cliente");
				parametros.put("CLIENTE", c.getClave());
				parametros.put("FECHA", model.getValue("fechaInicial"));
			}
			if(model.getValue("cliente")==null && todos.isSelected()==true){
				parametros.put("CLIENTE", "%");
				parametros.put("FECHA", model.getValue("fechaInicial"));
				System.out.println("Opcion 2");
			}
			else if(model.getValue("cliente")==null && todos.isSelected()==false){
				parametros.put("CLIENTE", "%");
				parametros.put("FECHA", model.getValue("fechaInicial"));
			
			}
			parametros.put("COMENTARIO", comentario.getText());
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

		public JCheckBox getTodos() {
			return todos;
		}

		public void setTodos(JCheckBox todos) {
			this.todos = todos;
		}
		
		
		 
		
	}
	
	public static void run(){
		EstadoDeCuentaReport action=new EstadoDeCuentaReport();
		action.execute();
	}
	
	public static void main(String[] args) {
		EstadoDeCuentaReport action=new EstadoDeCuentaReport();
		action.execute();
	}

}
