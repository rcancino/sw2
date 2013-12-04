package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;


import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;


import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.services.Services;



/**
 * 
 * @author Ruben Cancino
 *
 */
public class DiferenciasConteoReportForm extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils2.runReport("INVENT/DiferenciasDeInventario.jasper", form.getParametros());
			
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
		private JFormattedTextField sectorInicial;
		private JFormattedTextField sectorFinal;
		private JCheckBox conDiferencia;
		
		
		

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
			
			//NumberFormatter formatter1=new NumberFormatter(NumberFormat.getNumberInstance());
			//formatter.setValueClass(Long.class);
			sectorInicial=new JFormattedTextField(formatter);
			sectorFinal=new JFormattedTextField(formatter);
			conDiferencia=new JCheckBox("",true);
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
			builder.append("Todas" ,conDiferencia);
			builder.append("Sucursal",sucursalControl);
			builder.append("Sector Inicial",sectorInicial);
			builder.append("Sector Final" ,sectorFinal);
			
			return builder.getPanel();
		}
		
		private JComboBox createSucursalControl() {			
			final JComboBox box = new JComboBox(Services.getInstance().getSucursalesOperativas().toArray());
			Sucursal local=Services.getInstance().getConfiguracion().getSucursal();
			for(int index=0;index<box.getModel().getSize();index++){
				Sucursal s=(Sucursal)box.getModel().getElementAt(index);
				if(s.equals(local)){
					box.setSelectedIndex(index);
					break;
				}
			}
			return box;
		}
		
		private Double getRangoInferior(){
			return (Double)rangoInferior.getValue();
		}
		
		private Double getRangoSuperior(){
			return (Double)rangoSuperior.getValue();
		}
		
		private  Double getSectorInicial(){
			return (Double)sectorInicial.getValue();
		}
		
		private Double getSectorFinal(){
			return (Double)sectorFinal.getValue();
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
			parametros.put("FECHA", fechaInventario.getDate());
			parametros.put("SUC", getSucursal());
            parametros.put("DIF_DE", getRangoInferior());
            parametros.put("DIF_A", getRangoSuperior());
			parametros.put("SECTOR_INICIAL",getSectorInicial());
			parametros.put("SECTOR_FINAL",getSectorFinal());
			if (!conDiferencia.isSelected()){
			  parametros.put("CON_DIFERENCIA"," AND E.DIFERENCIA BETWEEN " +getRangoInferior()+ " AND " +getRangoSuperior());
				}
			else
				parametros.put("CON_DIFERENCIA"," ");
			
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void run(){
		DiferenciasConteoReportForm action=new DiferenciasConteoReportForm();
		action.execute();
	}
	
	public static void main(String[] args) {
		run();
	}

}
