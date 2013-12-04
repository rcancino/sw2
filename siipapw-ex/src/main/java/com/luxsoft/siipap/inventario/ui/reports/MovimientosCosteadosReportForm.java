package com.luxsoft.siipap.inventario.ui.reports;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Forma para ejecutar el reporte de calculo de costo promedio
 * 
 * @author Ruben Cancino
 *
 */
public class MovimientosCosteadosReportForm extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("invent/MovInvCosteado.jasper"), form.getParametros());
		}
		form.dispose();
		
	}
	
	public static void run(){
		MovimientosCosteadosReportForm action=new MovimientosCosteadosReportForm();
		action.execute();
	}
	
	/**
	 * Forma para el reporte de cobranza
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		private JComponent yearComponent;
		
		private JComponent mesComponent;
		
		private JComboBox productoControl;
		
		private JCheckBox todosBox;
		

		public ReportForm() {
			super("Entradas de material");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){		
			final Date fecha=new Date();
			yearHolder=new ValueHolder(Periodo.obtenerYear(fecha));
			mesHolder=new ValueHolder(Periodo.obtenerMes(fecha));
			
			yearComponent=Binder.createYearBinding(yearHolder);
			mesComponent=Binder.createMesBinding(mesHolder);
			productoControl=buildProveedorControl();
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());			
			final FormLayout layout=new FormLayout(
					"p,2dlu,70dlu,3dlu,p,2dlu,70dlu:g,2dlu,p",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Prodicto",productoControl,5);
			builder.append(todosBox);
			builder.append("Año",yearComponent);
			builder.append("Mes",mesComponent);
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		private JComboBox buildProveedorControl() {
			todosBox=new JCheckBox("Todos",true);
			todosBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					productoControl.setEnabled(!todosBox.isSelected());
					if(!todosBox.isSelected()){
						validarArticuloSeleccionado();
					}
				}
			});
			getOKAction().setEnabled(true);
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2.getProductoManager().getAll());
			final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave" ,"descripcion"});
			AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setCorrectsCase(true);
			box.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {					
					validarArticuloSeleccionado();
				}
				
			});
			box.setEnabled(!todosBox.isSelected());
			return box;
		}
		
		private void validarArticuloSeleccionado(){
			Object selected=productoControl.getSelectedItem();			
			if((selected!=null) && (selected instanceof Producto)) 
				getOKAction().setEnabled(true);
			else
				getOKAction().setEnabled(false);			
		}
		
		private String getProductoClave(){
			if(todosBox.isSelected())
				return "%";
			Object selected=productoControl.getSelectedItem();
			if(selected!=null)
				return ((Producto)selected).getClave();
			return "";
		}
		
		@Override
		public void doApply() {			
			super.doApply();
			
			int mes=(Integer)mesHolder.getValue();
			int year=(Integer)yearHolder.getValue();
			
			int mes_ini=mes;
			int year_ini=year;
			
			if(mes==1){
				mes_ini=12;
				year_ini=year-1;
			}else{
				mes_ini=mes-1;
			}
			Periodo periodo=Periodo.getPeriodoEnUnMes(mes-1, year);
			DateFormat df=new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			DateFormat df2=new SimpleDateFormat("yyyy-MM-dd 23:00:00");
			parametros.put("FECHA_INI", df.format(periodo.getFechaInicial()));
			parametros.put("FECHA_FIN", df2.format(periodo.getFechaFinal()));
			parametros.put("AÑO", (long)year);
			parametros.put("MES", (long)mes);
			parametros.put("MES_INI", (long)mes_ini);
			parametros.put("AÑO_INI", (long)year_ini);			
			parametros.put("ARTICULOS", getProductoClave());
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		private ValueHolder yearHolder;
		private ValueHolder mesHolder;
		
		
		
	}
	
	public static void main(String[] args) {
		try {
			SwingUtilities.invokeAndWait(new Runnable(){

				public void run() {
					MovimientosCosteadosReportForm.run();
				}
				
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
