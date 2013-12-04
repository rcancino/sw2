package com.luxsoft.siipap.cxp.ui.reportes;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXDatePicker;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Genera el reporte de Estado de cuenta de un cliente
 * 
 * @author Ruben Cancino
 *
 */
public class EstadoDeCuentaReport extends SWXAction{
	
	
	

	@Override
	public void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){			
			if(form.tipo.getSelectedItem().equals("GENERAL"))
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxp/EstadoDeCuentaGeneral.jasper"), form.getParametros());
			else{
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxp/EstadoDeCuentaDetalle.jasper"), form.getParametros());
			}
		}
		form.dispose();
	}


	/**
	 * Forma para el reporte de estado de cuenta
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros=new HashMap<String, Object>();
		
		private JComboBox proveedorControl;
		private JXDatePicker fechaInicial;
		private JXDatePicker fechaFinal;
		private JComboBox moneda;
		private JComboBox tipo;
		
		

		public ReportForm() {
			super("Relación de pagos");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			proveedorControl=buildProveedorControl();
			Object[] monedas={MonedasUtils.PESOS,MonedasUtils.DOLARES,MonedasUtils.EUROS};
			moneda=new JComboBox(monedas);
			tipo=new JComboBox(new String[]{"GENERAL","DETALLE"});
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"60dlu,2dlu,70dlu,3dlu,60dlu,2dlu,70dlu",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Proveedor",proveedorControl,5);
			builder.append("Tipo",tipo);
			builder.append("Moneda",moneda);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Estado de cuenta","").getHeader();
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
			parametros.put("FECHA_FIN", fechaFinal.getDate());
			parametros.put("MONEDA", ((Currency)moneda.getSelectedItem()).toString());
			if(getProveedorSelected()!=null)
				parametros.put("PROVEEDOR", getProveedorSelected().getClave());
			String tip=(String)tipo.getSelectedItem();
			if(StringUtils.equals(tip, "DETALLE")){
				parametros.put("FECHA_INI", fechaInicial.getDate());
				parametros.put("SALDO_INI", getSaldoInicial());
			}
			logger.info("Parametros de reporte:"+parametros);
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		private JComboBox buildProveedorControl(){			
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2.getProveedorManager().getAll());
			final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave", "nombre"});
			AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setStrict(false);
			return box;
		}
		 
		private Proveedor getProveedorSelected(){
			return (Proveedor)proveedorControl.getSelectedItem();
		}
		private Currency getMoneda(){
			return (Currency)moneda.getSelectedItem();
		}
		
		private BigDecimal getSaldoInicial(){
			return CXPServiceLocator.getInstance()
				.getCXPManager()
				.getSaldo(getProveedorSelected(),getMoneda(), fechaInicial.getDate());
		}
		
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){

			public void run() {
				SWExtUIManager.setup();
				EstadoDeCuentaReport action=new EstadoDeCuentaReport();
				action.execute();
				
			}
			
		});
		
	}

}
