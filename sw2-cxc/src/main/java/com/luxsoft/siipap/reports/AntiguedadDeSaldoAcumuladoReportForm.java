package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Genera el reporte general de antiguedad de saldos
 * 
 * @author Ruben Cancino
 *
 */
public class AntiguedadDeSaldoAcumuladoReportForm extends SWXAction{

	@Override
	public void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/AntiguedadSaldosAcumuladoMensual.jasper"), form.getParametros());
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
		private JTextField year;
		private Cliente cliente;
		private JCheckBox todos;
		private JComponent jCliente;
		private final PresentationModel model;
		

		public ReportForm() {
			super("Antigüedad de Saldos");
			parametros=new HashMap<String, Object>();
			model=new PresentationModel(this);
		}
		
		private void initComponents(){	
			todos=new JCheckBox();
			todos.setSelected(false);
			jCliente=Binder.createClientesBinding(model.getModel("cliente"));
			box1=new JComboBox(new String[]{"Vencido","Cliente","Atraso Max","Saldo","Por Vencer"});
			box2=new JComboBox(new String[]{"Descendente","Ascendente"});
			year=new JTextField();
			
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
			builder.append("Cliente",jCliente,5);
			builder.append("Todos",todos);
			builder.nextLine();
			builder.append("Año"  ,year);
			builder.nextLine();
			builder.append("Ordenado por: ",box1,5);
			builder.nextLine();			
			builder.append("En forma: ",box2,true);
			builder.nextLine();
			//builder.append("Fecha De Corte  ",jFecha);
			
			
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
			parametros.put("YEAR",year.getText());
			Cliente c=(Cliente)model.getValue("cliente");
			if(todos.isSelected())
				parametros.put("CLIENTE","%" );
			else
			{
			 
			 parametros.put("CLIENTE", c.getId().toString());
			 parametros.put("NOMBRE", c.getNombre());
			 System.out.println("Parametros: "+parametros);
			}
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
		
		public Cliente getCliente() {
			return cliente;
		}
		public void setCliente(Cliente cliente) {
			Object oldValue=this.cliente;
			this.cliente = cliente;
			firePropertyChange("cliente", oldValue, cliente);
		}
		
		
		public JCheckBox getTodos() {
			return todos;
		}

		public void setTodos(JCheckBox todos) {
			this.todos = todos;
		}

	}
	

	
	
	public static void run(){
		AntiguedadDeSaldoAcumuladoReportForm action=new AntiguedadDeSaldoAcumuladoReportForm();
		action.execute();	
	}
	
	public static void main(String[] args) {
		AntiguedadDeSaldoAcumuladoReportForm action=new AntiguedadDeSaldoAcumuladoReportForm();
		action.execute();
	}

}
