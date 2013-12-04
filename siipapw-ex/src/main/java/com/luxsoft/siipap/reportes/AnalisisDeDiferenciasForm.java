package com.luxsoft.siipap.reportes;

import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Sucursal;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;



/**
 * 
 * @author Ruben Cancino
 *
 */
public class AnalisisDeDiferenciasForm extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils.viewReport(ReportUtils.toReportesPath("INVENT/AnalisisAclaracionDeDiferencias.jasper"), form.getParametros());
			
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
		
		private JComboBox sucursalControl;
		private JXDatePicker fechaInventario;
		private JFormattedTextField rangoInferior;
		private JFormattedTextField rangoSuperior;
		
		
		

		public ReportForm() {
			super("Inventarios");
		}
		
		private void initComponents(){
			fechaInventario=new JXDatePicker();
			fechaInventario.setFormats("dd/MM/yyyy");
			NumberFormatter formatter=new NumberFormatter(NumberFormat.getNumberInstance());
			formatter.setValueClass(Double.class);
			rangoInferior=new JFormattedTextField(formatter);
			rangoInferior.setValue(new Double(0));
			rangoSuperior=new JFormattedTextField(formatter);
			rangoSuperior.setValue(new Double(0));
			sucursalControl=createSucursalControl();
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInventario);
			builder.append("Rango Inicial",rangoInferior);
			builder.append("Rango Final",rangoSuperior);
			builder.append("Sucursal",sucursalControl);
			return builder.getPanel();
		}
		
		private JComboBox createSucursalControl() {			
			final JComboBox box = new JComboBox(ServiceLocator2.getLookupManager().getSucursalesOperativas().toArray());
			Sucursal local=ServiceLocator2.getConfiguracion().getSucursal();
			for(int index=0;index<box.getModel().getSize();index++){
				Sucursal s=(Sucursal)box.getModel().getElementAt(index);
				if(s.equals(local)){
					box.setSelectedIndex(index);
					break;
				}
			}
			return box;
		}
		
		private double getRangoInferior(){
			return (Double)rangoInferior.getValue();
		}
		
		private double getRangoSuperior(){
			return (Double)rangoSuperior.getValue();
		}
		
		
		private long getSucursal(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			return selected.getId();
		}
		
		protected JComponent buildHeader(){
			return new Header("Analisis de Diferencias","").getHeader();
		}

		@Override
		protected JComponent buildContent() {			
			JPanel panel=new JPanel(new BorderLayout());			
			panel.add(buildForm(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}

		@Override
		public void doApply() {			
			super.doApply();
			parametros.put("FECHA_INV", fechaInventario.getDate());
			parametros.put("SUCURSAL_ID", getSucursal());
			parametros.put("RANGO_INF",getRangoInferior());
			parametros.put("RANGO_SUP",getRangoSuperior());
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void run(){
		AnalisisDeDiferenciasForm action=new AnalisisDeDiferenciasForm();
		action.execute();
	}
	
	public static void main(String[] args) {
		run();
	}

}
