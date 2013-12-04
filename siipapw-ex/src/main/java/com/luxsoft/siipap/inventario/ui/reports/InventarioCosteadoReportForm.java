package com.luxsoft.siipap.inventario.ui.reports;

import java.awt.BorderLayout;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Forma para ejecutar el reporte de Inventario Costeado
 * 
 * @author Ruben Cancino
 *
 */
public class InventarioCosteadoReportForm extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("invent/InventarioCosteado.jasper"), form.getParametros());
		}
		form.dispose();
		
	}
	
	public static void run(){
		InventarioCosteadoReportForm action=new InventarioCosteadoReportForm();
		action.execute();
	}
	
	/**
	 * Forma especial para el reporte
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		private JComponent yearComponent;
		
		private JComponent mesComponent;
		
		private JComboBox sucursalControl;
		
		

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
			sucursalControl=createSucursalControl();
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());
			//CellConstraints cc=new CellConstraints();
			final FormLayout layout=new FormLayout(
					"50dlu,2dlu,90dlu",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Sucursal",sucursalControl);
			builder.append("Año",yearComponent);
			builder.append("Mes",mesComponent);
			
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		private JComboBox createSucursalControl() {			
			List<Sucursal> sucs=ServiceLocator2.getLookupManager().getSucursalesOperativas();
			final JComboBox box = new JComboBox(sucs.toArray());
			box.addItem("TODAS");
			return box;
		}
		
		private String getSucursalClave(){
			Object selected=sucursalControl.getSelectedItem();
			if(selected instanceof String){
				return "%";
			}else
				return String.valueOf(((Sucursal)selected).getClave());	
			
		}
		
		@Override
		public void doApply() {			
			super.doApply();			
			int mes=(Integer)mesHolder.getValue();
			int year=(Integer)yearHolder.getValue();
			parametros.put("AÑO", (long)year);
			parametros.put("MES", (long)mes);
			parametros.put("SUC", getSucursalClave());
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		private ValueHolder yearHolder;
		private ValueHolder mesHolder;
		
		
		
	}
	
	public static void main(String[] args) {
		run();
	}

}
