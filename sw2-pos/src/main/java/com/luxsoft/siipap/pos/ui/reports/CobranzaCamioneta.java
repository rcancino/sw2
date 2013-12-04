package com.luxsoft.siipap.pos.ui.reports;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;
import org.springframework.jdbc.core.SqlParameterValue;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.services.Services;

/**
 * 
 * @author Ruben Cancino
 *
 */
public class CobranzaCamioneta extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			ReportUtils2.runReport("ventas/CobranzaCamioneta.jasper", form.getParametros());
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
		
		

		public ReportForm() {
			super("Cobranza");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			
			
		}
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			builder.append("Fecha Final",fechaFinal);
			
			return builder.getPanel();
		}
		
		protected JComponent buildHeader(){
			return new Header("Relación de cobranza camioneta","").getHeader();
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
			//parametros.put("FECHA_FIN", fechaFinal.getDate());
			Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			parametros.put("SUCURSAL", suc.getNombre());
			parametros.put("SUCURSAL_ID", suc.getId());
			registrarSaldosAFavor();
			
			logger.info("Parametros de reporte:"+parametros);
			
		}
		
		public void registrarSaldosAFavor(){
			Date fecha=fechaInicial.getDate();
			Sucursal suc1=Services.getInstance().getConfiguracion().getSucursal();
			
			String SQL="SELECT IFNULL(SUM(A.TOTAL-IFNULL((SELECT IFNULL(SUM(B.IMPORTE),0)" +
					   " FROM SX_CXC_APLICACIONES B WHERE A.ABONO_ID=B.ABONO_ID " +
					   "AND B.FECHA=A.SAF ),0)),0) AS SALDOAFAVOR " +
					   "FROM sx_cxc_abonos A " +
					   " JOIN SW_SUCURSALES S ON (A.SUCURSAL_ID=S.SUCURSAL_ID) WHERE A.SAF =?" +
					   " AND S.NOMBRE=? AND  A.ANTICIPO IS FALSE	" +
					   "AND A.ABONO_ID IN(SELECT B.ABONO_ID FROM SX_CXC_APLICACIONES B WHERE A.ABONO_ID=B.ABONO_ID AND B.CAR_ORIGEN='CAM')" +
					   " AND (A.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM SX_CXC_APLICACIONES B WHERE A.ABONO_ID=B.ABONO_ID AND B.FECHA=A.SAF),0))>1"; 
			
			
			SqlParameterValue pv=new SqlParameterValue(Types.DATE,fecha);
			SqlParameterValue pv1=new SqlParameterValue(Types.VARCHAR,suc1.getNombre());
			BigDecimal saldo=(BigDecimal)Services.getInstance().getJdbcTemplate().queryForObject(
					SQL
					, new Object[]{pv,pv1}
					, BigDecimal.class);
			parametros.put("SALDOAFAVOR", saldo);
		}
			
		
		public Map<String, Object> getParametros() {
			return parametros;
		}
		
	}

	
		
	
	
	public static void run(){
		CobranzaCamioneta action=new CobranzaCamioneta();
		action.execute();
	}
	
	public static void main(String[] args) {
		run();
	}

}
