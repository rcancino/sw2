package com.luxsoft.siipap.reports;


import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.JXDatePicker;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.model.CantidadMonetaria;
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
public class CobranzaCamionetaPOS extends SWXAction{
	
	

	@Override
	protected void execute() {
		
		final ReportForm form=new ReportForm();
		form.open();
		if(!form.hasBeenCanceled()){
			
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/CobranzaCamioneta.jasper"), form.getParametros());
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
		
		

		public ReportForm() {
			super("Cobranza");
		}
		
		private void initComponents(){
			fechaInicial=new JXDatePicker();
			fechaInicial.setFormats("dd/MM/yyyy");
			fechaFinal=new JXDatePicker();
			fechaFinal.setFormats("dd/MM/yyyy");
			sucursalControl=createSucursalControl();
			
			
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
		
		private String getSucursal(){
			Sucursal selected=(Sucursal)sucursalControl.getSelectedItem();
			return selected.getNombre();
		}
		
		
		private JComponent buildForm(){
			initComponents();
			final FormLayout layout=new FormLayout(
					"p,3dlu,f:60dlu:g",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Fecha Inicial",fechaInicial);
			//builder.append("Fecha Final",fechaFinal);
			builder.append("Sucursal",sucursalControl);
			
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
			//Sucursal suc=Services.getInstance().getConfiguracion().getSucursal();
			parametros.put("SUCURSAL", getSucursal());
			registrarSaldosAFavor();
			
			logger.info("Parametros de reporte:"+parametros);
			
		}
		
		public void registrarSaldosAFavor(){
			Date fecha=fechaInicial.getDate();
			String suc=getSucursal();
			
			//String SQL="select count(*) as SALDO_AFAVOR from SX_EMBARQUES where FECHA=?";
			
			String SQL="SELECT IFNULL(SUM(A.TOTAL-IFNULL((SELECT IFNULL(SUM(B.IMPORTE),0)" +
					   " FROM SX_CXC_APLICACIONES B WHERE A.ABONO_ID=B.ABONO_ID " +
					   "AND B.FECHA=A.SAF ),0)),0) AS SALDOAFAVOR " +
					   "FROM sx_cxc_abonos A " +
					   " JOIN SW_SUCURSALES S ON (A.SUCURSAL_ID=S.SUCURSAL_ID) WHERE A.SAF =?" +
					   " AND S.NOMBRE=? AND  A.ANTICIPO IS FALSE	" +
					   "AND A.ABONO_ID IN(SELECT B.ABONO_ID FROM SX_CXC_APLICACIONES B WHERE A.ABONO_ID=B.ABONO_ID AND B.CAR_ORIGEN='CAM')" +
					   " AND (A.TOTAL-IFNULL((SELECT SUM(B.IMPORTE) FROM SX_CXC_APLICACIONES B WHERE A.ABONO_ID=B.ABONO_ID AND B.FECHA=A.SAF),0))>10"; 
			
			
			SqlParameterValue pv=new SqlParameterValue(Types.DATE,fecha);
			SqlParameterValue pv1=new SqlParameterValue(Types.VARCHAR,suc);
			BigDecimal saldo=(BigDecimal)getJdbcTemplate().queryForObject(
					SQL
					, new Object[]{pv,pv1}
					, BigDecimal.class);
			
			parametros.put("SALDOAFAVOR", saldo);
			
			
		}

		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		
		
		
		
	}
	private JdbcTemplate jdbcTemplate;



	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public static void run(JdbcTemplate template){
		CobranzaCamionetaPOS action=new CobranzaCamionetaPOS();
		action.setJdbcTemplate(template);
		action.execute();
	}
	
	public static void main(String[] args) {
		run(ServiceLocator2.getJdbcTemplate());
	}

}
