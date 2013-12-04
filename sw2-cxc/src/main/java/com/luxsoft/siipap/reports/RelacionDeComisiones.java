package com.luxsoft.siipap.reports;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.ventas.model.Cobrador;
import com.luxsoft.siipap.ventas.model.Vendedor;

/**
 * Genera el reporte de Estado de cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
public class RelacionDeComisiones extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/Comisiones.jasper"), form.getParametros());
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
		
		
		private JXDatePicker fechaInicial;
		private JXDatePicker fechaFinal;
		private JComboBox tipo;
		private JComboBox cobradorBox;
		private JComboBox vendedorBox;
		private JCheckBox todosBox;
		
		

		public ReportForm() {
			super("Reporte de comisiones");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			
			Object[] cobs=ServiceLocator2.getCXCManager().getCobradores().toArray(new Cobrador[0]);			
			cobradorBox=new JComboBox(cobs);
			cobradorBox.setEnabled(false);
			Object[] vends=ServiceLocator2.getCXCManager().getVendedores().toArray(new Vendedor[0]);
			vendedorBox=new JComboBox(vends);
			
			todosBox=new JCheckBox("",false);
			todosBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					boolean val=todosBox.isSelected();
					if(val){
						vendedorBox.setEnabled(false);
						cobradorBox.setEnabled(false);
					}else{
						updateTipo();
					}
					
				}
			});
			tipo=new JComboBox(new String[]{"VENDEDOR","COBRADOR"});
			tipo.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					updateTipo();
				}
			});
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			builder.append("Tipo",tipo);
			builder.append("Cobrador",cobradorBox);
			builder.append("Vendedor",vendedorBox);
			builder.append("Todos",todosBox);
			
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Reporte de comisiones","").getHeader();
		}

		@Override
		protected JComponent buildContent() {			
			JPanel panel=new JPanel(new BorderLayout());			
			panel.add(buildForm(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return panel;
		}
		
		private void updateTipo(){
			String t=tipo.getSelectedItem().toString();
			boolean res=t.startsWith("COB");
			cobradorBox.setEnabled(res);
			vendedorBox.setEnabled(!res);
			
		}

		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("FECHA_INI", fechaInicial.getDate());
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			parametros.put("TIPO", tipo.getSelectedItem().toString());
			if(todosBox.isSelected()){
				parametros.put("ID", "%");
			}else{
				String t=tipo.getSelectedItem().toString();
				boolean res=t.startsWith("COB");
				Long id=null;
				if(res){
					Cobrador c=(Cobrador)cobradorBox.getSelectedItem();
					id=c.getId();
				}else{
					Vendedor v=(Vendedor)vendedorBox.getSelectedItem();
					id=v.getId();
				}
				parametros.put("ID", String.valueOf(id));
			}
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void run(){
		RelacionDeComisiones action=new RelacionDeComisiones();
		action.execute();
		
	}
	
	public static void main(String[] args) {
		run();
	}

}
