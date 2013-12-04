package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

import com.luxsoft.sw3.services.Services;

/**
 * 
 * @author Ruben Cancino
 *
 */
public class VentasDiarias extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(form.postFechado.isSelected()){
				ReportUtils2.runReport("ventas/ventas_diariasCHE.jasper", form.getParametros());
			}
			else
			ReportUtils2.runReport("ventas/ventas_diarias.jasper", form.getParametros());
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
		private JComboBox origenBox;
		private JCheckBox anticipo;
		private JCheckBox postFechado;

		public ReportForm() {
			super("Ventas Diarias");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			Object[] values={OrigenDeOperacion.MOS,OrigenDeOperacion.CAM,OrigenDeOperacion.CRE};
			origenBox=new JComboBox(values);
			anticipo=new JCheckBox("",false);
			postFechado=new JCheckBox("",false);
			
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			builder.append("Tipo",origenBox);
			builder.append("Anticipo",anticipo);
			builder.append("PostFechado",postFechado);
			
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Relación de ventas diarias","").getHeader();
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
			parametros.put("FECHA_INI", fechaInicial.getDate());
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			parametros.put("SUCURSAL", String.valueOf(suc.getClave()));
			OrigenDeOperacion origen=(OrigenDeOperacion)origenBox.getSelectedItem();
			parametros.put("ORIGEN", origen.name());
			logger.info("Parametros de reporte:"+parametros);
			if (anticipo.isSelected())
			{
				parametros.put("ANTICIPO","  and (anticipo is true or anticipo_aplicado > 0 )");
				
			}
			else
			parametros.put("ANTICIPO","  and (anticipo is false and (anticipo_aplicado = 0 or importe>0))");
		
			 
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void run(){
		VentasDiarias action=new VentasDiarias();
		action.execute();
	}
	
	public static void main(String[] args) {
		run();
	}

}
