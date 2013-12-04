package com.luxsoft.siipap.inventario.ui.reports;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Tarea para ejecutar el el reporte de analisis de movimientos costeados
 * 
 * @author Ruben Cancino
 *
 */
public class AnalisisMovimientosCosteadosReportForm extends SWXAction{

	@Override
	protected void execute() {
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Parametros enviados: "+form.getParametros());
			}
			ReportUtils.viewReport(ReportUtils.toReportesPath("invent/MovInvCost_det.jasper"), form.getParametros());			
		}
		form.dispose();
		
	}
	
	public static void run(){
		AnalisisMovimientosCosteadosReportForm action=new AnalisisMovimientosCosteadosReportForm();
		action.execute();
	}
	
	/**
	 * Forma para el reporte de cobranza
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros;
		
		private JXDatePicker fechaInicial; 
		
		private JXDatePicker fechaFinal;
		
		private JComboBox tipoBox;
		
		private JComboBox grupoBox;
		
		private JComboBox sucursalControl;
		

		public ReportForm() {
			super("Análisis de Movimientos");
			parametros=new HashMap<String, Object>();
		}
		
		private void initComponents(){	
			
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			
			grupoBox=new JComboBox(new String[]{"COMPRAS","VENTAS","MOVIMIENTOS","TRANSFORMACIONES","TRASLADOS"});
			tipoBox=new JComboBox(new String[]{
					"TODOS","INI","COM","MQH","MQB","FAC","DEV","AJU","CIM","CIS","DEC","MER","OIM","RMC"
					,"TPE","TPS","TRV","VIR","TRS","RAU","REC","REF","SNA"
					}
			);
			sucursalControl=createSucursalControl();
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());			
			final FormLayout layout=new FormLayout(
					"p,2dlu,70dlu,3dlu,p,2dlu,70dlu:g,2dlu,p",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal,true);
			
			builder.append("Tipo",tipoBox);
			builder.append("Grupo",grupoBox,true);
			builder.append("Sucursal",sucursalControl);
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
		
		@Override
		public void doApply() {
			parametros.put("GRUPO",(String)grupoBox.getSelectedItem());
			parametros.put("FECHA_INICIAL",fechaInicial.getDate());
			parametros.put("FECHA_FINAL",fechaFinal.getDate());
			if("TODOS".equals(tipoBox.getSelectedItem())){
				parametros.put("TIPO","%");
			}else
				parametros.put("TIPO",(String)tipoBox.getSelectedItem());
			parametros.put("SUCURSAL", getSucursalClave());
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		private String getSucursalClave(){
			Object selected=sucursalControl.getSelectedItem();
			if(selected instanceof String){
				return "%";
			}else
				return String.valueOf(((Sucursal)selected).getNombre());	
			
		}
		
		
	}
	
	public static void main(String[] args) {
		try {
			SwingUtilities.invokeAndWait(new Runnable(){

				public void run() {
					AnalisisMovimientosCosteadosReportForm.run();
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
