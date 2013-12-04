package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Forma para ejecutar el reporte de Inventario Costeado
 * 
 * @author Ruben Cancino
 *
 */
public class InventarioCertificadoReportForm extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			
				logger.debug("Parametros enviados: "+form.getParametros());
				if(form.fsc.isSelected()){
					form.parametros.put("CERTIFICACION", "FSC");
					ReportUtils2.runReport("invent/InventarioPorCategoriasCert.jasper", form.getParametros());
				}
				if(form.sfi.isSelected()){
					form.parametros.put("CERTIFICACION", "SFI");
					ReportUtils2.runReport("invent/InventarioPorCategoriasCert.jasper", form.getParametros());
				}
				if(form.pefc.isSelected()){
					form.parametros.put("CERTIFICACION", "PEFC");
					ReportUtils2.runReport("invent/InventarioPorCategoriasCert.jasper", form.getParametros());
				}
				
		}
		form.dispose();
		
	}
	
	public static void run(){
		InventarioCertificadoReportForm action=new InventarioCertificadoReportForm();
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
		
		private JCheckBox fsc;
		private JCheckBox sfi;
		private JCheckBox pefc;
		
		//private JComboBox clasificacion;
		

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
			fsc=new JCheckBox("", false);
			sfi=new JCheckBox("", false);
			pefc=new JCheckBox("", false);
		/*	List data=Services.getInstance().getJdbcTemplate().queryForList("SELECT CLASIFICACION FROM SX_PRODUCTOS_CLASIFICACION", String.class);
			clasificacion=new JComboBox(data.toArray());*/
			
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
			builder.append("FSC",fsc);
			builder.append("SFI",sfi);
			builder.append("PEFC",pefc);
			//builder.append("Clasificacion",clasificacion);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		private JComboBox createSucursalControl() {			

			List<Sucursal> sucursales=Services.getInstance().getSucursalesOperativas();
			Sucursal local=Services.getInstance().getConfiguracion().getSucursal();
			sucursales.remove(local);
			sucursales.add(0,local);
			final JComboBox box = new JComboBox(sucursales.toArray(new Sucursal[0]));
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
			Integer mes=(Integer)mesHolder.getValue();
			Integer year=(Integer)yearHolder.getValue();
			parametros.put("AÑO", year);
			parametros.put("MES", mes);
			parametros.put("SUC", getSucursalClave());
			if(fsc.isSelected()){
				parametros.put("CERTIFICACION", "FSC");
			}
			if(sfi.isSelected()){
				parametros.put("CERTIFICACION", "SFI");
			}
			if(pefc.isSelected()){
				parametros.put("CERTIFICACION", "PEFC");
			}
		
			
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
