package com.luxsoft.siipap.reports;

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
public class VentasDiariasBI extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/ventas_diarias.jasper"), form.getParametros());
			//ReportUtils.viewReport(ReportUtils.toReportesPath("BI/VentasPorVendedor.jasper"), form.getParametros());
			
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
		private JXDatePicker fechaInicial;
		private JXDatePicker fechaFinal;
		private JComboBox origenBox;
		private JCheckBox anticipo;
		
		

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
			sucursalControl=createSucursalControl();
			anticipo=new JCheckBox("",false);
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
			builder.append("Sucursal",sucursalControl);
			builder.append("Anticipo",anticipo);
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
		
		private int getSucursal(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			return selected.getClave();
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
			//Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			//parametros.put("SUCURSAL", String.valueOf(suc.getClave()));
			parametros.put("SUCURSAL", String.valueOf(getSucursal()));
			OrigenDeOperacion origen=(OrigenDeOperacion)origenBox.getSelectedItem();
			parametros.put("ORIGEN", origen.name());
			if (anticipo.isSelected())
			{
				parametros.put("ANTICIPO","  and (anticipo is true or anticipo_aplicado > 0 )");
				
			}
			else
			parametros.put("ANTICIPO","  and (anticipo is false and (anticipo_aplicado = 0 or importe>0))");
			
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void run(){
		VentasDiariasBI action=new VentasDiariasBI();
		action.execute();
	}
	

	
	public static void main(String[] args) {
		run();
	}

}
