package com.luxsoft.siipap.inventario.ui.reports;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxp.ui.selectores.ProveedorControl;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Tarea para ejecutar el el reporte de facturas por proveedor
 * 
 * @author Ruben Cancino
 *
 */
public class FacturasPorProveedorForm extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			logger.info("Parametros enviados: "+form.getParametros());
			if(form.todosBox.isSelected())
				ReportUtils.viewReport(ReportUtils.toReportesPath("invent/MercanciaEnTransito.jasper"), form.getParametros());
			else
				ReportUtils.viewReport(ReportUtils.toReportesPath("invent/FacturasAnalizadasPorProveedor.jasper"), form.getParametros());
		}
		form.dispose();
		
	}
	
	public static void run(){
		FacturasPorProveedorForm action=new FacturasPorProveedorForm();
		action.execute();
	}
	
	/**
	 * Forma para el reporte de movimientos de maquila costeados
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog implements PropertyChangeListener{
		
		private final Map<String, Object> parametros;
		
		ProveedorControl control; 
		
		private ValueHolder proveedorHolder=new ValueHolder(null);
		
		private JXDatePicker fechaInicial; 
		
		private JXDatePicker fechaFinal;
		
		private JCheckBox todosBox;
		
		
		

		public ReportForm() {
			super("Facturas por proveedor");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){	
			proveedorHolder.addPropertyChangeListener(this);
			fechaInicial=new JXDatePicker();			
			fechaInicial.setFormats("dd/MM/yyyy");
			
			fechaFinal=new JXDatePicker();			
			fechaFinal.setFormats("dd/MM/yyyy");
			
			todosBox=new JCheckBox("Todos",false);
			buildProveedorControl();
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());			
			final FormLayout layout=new FormLayout(
					"p,2dlu,70dlu,3dlu,p,2dlu,70dlu:g,2dlu,p",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Proveedor",control.getControl(),5);
			builder.append(todosBox);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		private void buildProveedorControl() {
			control=new ProveedorControl(proveedorHolder);
			todosBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					control.getControl().setEnabled(!todosBox.isSelected());
				}
			});
		}
		
		
		
		@Override
		public void doApply() {
			if(proveedorHolder.getValue()!=null){
				Proveedor p=(Proveedor)proveedorHolder.getValue();
				parametros.put("PROVEEDOR", p.getClave());
			}else
				parametros.put("PROVEEDOR", "%");
			parametros.put("FECHA_INI",fechaInicial.getDate());
			parametros.put("FECHA_FIN",fechaFinal.getDate());
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if(!todosBox.isSelected())
				getOKAction().setEnabled(evt.getNewValue()!=null);
			
		}
		
	}
	
	public static void main(String[] args) {
		run();
	}

}
