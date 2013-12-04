package com.luxsoft.siipap.inventario.ui.reports;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
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
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Tarea para ejecutar el el reporte de inventario de bobinas
 * 
 * @author Ruben Cancino
 *
 */
public class Maquila_InventariosCosteadosForm extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}if("BOBINA".equals(form.tipoBox.getSelectedItem())){
				ReportUtils.runReportInOracle(ReportUtils.toReportesPath("maquila/INVENTARIO_BOB_MAQ.jasper"), form.getParametros());
			}else
				ReportUtils.runReportInOracle(ReportUtils.toReportesPath("maquila/INVENTARIO_HOJEO_MAQ.jasper"), form.getParametros());
			    
			
				
		}
		form.dispose();
		
	}
	
	public static void run(){
		Maquila_InventariosCosteadosForm action=new Maquila_InventariosCosteadosForm();
		action.execute();
	}
	
	/**
	 * Forma para el reporte de movimientos de maquila costeados
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		private JComboBox maquiladorComponent; //MAQUILADOR
		
		private JXDatePicker fechaComponent; //FECHA_CORTE
		
		private JCheckBox todosBox;
		
		private JComboBox tipoBox;
		

		public ReportForm() {
			super("Inventarios de Maquila");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){	
			fechaComponent=new JXDatePicker();
			fechaComponent.setFormats("dd/MM/yyyy");
			todosBox=new JCheckBox("Todos",false);
			maquiladorComponent=buildProveedorControl();
			tipoBox=new JComboBox(new String[]{"BOBINA","HOJEADO"});
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());			
			final FormLayout layout=new FormLayout(
					"p,2dlu,70dlu,3dlu,p,2dlu,70dlu:g,2dlu,p",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Maquilador",maquiladorComponent,5);
			builder.append(todosBox);
			builder.append("Fecha Corte",fechaComponent);
			builder.append("Tipo",tipoBox);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		private JComboBox buildProveedorControl() {
			todosBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					maquiladorComponent.setEnabled(!todosBox.isSelected());
				}
			});
			
			final List<String> data=ServiceLocator2.getAnalisisJdbcTemplate().queryForList("select NOMBRE from SW_ALMACENES", String.class);			
			final JComboBox box = new JComboBox(data.toArray(new String[0]));
			
			box.setEnabled(!todosBox.isSelected());
			return box;
		}
		
		private String getMaquilador(){
			if(todosBox.isSelected())
				return "%";
			return (String)maquiladorComponent.getSelectedItem();
		}
		
		@Override
		public void doApply() {
			parametros.put("MAQUILADOR", getMaquilador());
			parametros.put("FECHA_CORTE",fechaComponent.getDate());
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
	}
	
	public static void main(String[] args) {
		run();
	}

}
