package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.sw3.services.Services;

/**
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class ArqueoCaja extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils2.runReport("ventas/ArqueoCaja.jasper", form.getParametros());
		}
		form.dispose();
	}
	
	/**
	 * 
	 * 
	 * @author RUBEN
	 *
	 */
	public  class ReportForm extends SXAbstractDialog{
		
		private final Map<String, Object> parametros=new HashMap<String, Object>();
		
		
		private JXDatePicker fechaInicial;
		private JFormattedTextField saldoInicialCaja;
		private JFormattedTextField saldoInicialDeposito;
		
		

		public ReportForm() {
			super("Arqueo ");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			NumberFormat nf=NumberFormat.getNumberInstance();
			NumberFormatter formatter=new NumberFormatter(nf);
			formatter.setValueClass(BigDecimal.class);
			saldoInicialCaja=new JFormattedTextField(formatter);
			saldoInicialDeposito=new JFormattedTextField(formatter);
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Saldo Inicial Caja",saldoInicialCaja);
			builder.append("Saldo Inicial Deposito",saldoInicialDeposito);
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Arqueo de caja","").getHeader();
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
			parametros.put("FECHA", fechaInicial.getDate());			
			Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			parametros.put("SUCURSAL", suc.getClave());
			parametros.put("SALDO_INI_CAJA", getSaldoInicialCaja());
			parametros.put("SALDO_INI_DEP", getSaldoInicialDepositos());
			
			System.out.println(parametros);
			logger.info("Parametros de reporte:"+parametros);
			
		}
		
		private BigDecimal getSaldoInicialCaja(){
			BigDecimal val=(BigDecimal)saldoInicialCaja.getValue();
			if(val==null)
				val=BigDecimal.ZERO;
			return val;
		}
		
		private BigDecimal getSaldoInicialDepositos(){
			BigDecimal val=(BigDecimal)saldoInicialDeposito.getValue();
			if(val==null)
				val=BigDecimal.ZERO;
			return val;
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		 
		
	}
	
	public static void run(){
		ArqueoCaja action=new ArqueoCaja();
		action.execute();
	}
	
	public static void main(String[] args) {
		run();
	}

}
