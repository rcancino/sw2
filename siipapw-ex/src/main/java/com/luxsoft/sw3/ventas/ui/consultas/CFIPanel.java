package com.luxsoft.sw3.ventas.ui.consultas;

import java.sql.Types;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingWorker;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reports.VentasDiariasBI;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Panel de comprobantes fiscales digitales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CFIPanel extends FilteredBrowserPanel<ReporteMensualCFD>{

	public CFIPanel() {
		super(ReporteMensualCFD.class);
	}
	
	protected void init(){
		String[] props=new String[]{
				"rfc"
				,"receptor"
				,"serie"
				,"folio"
				,"no_aprobacion"
				,"fecha"
				,"total"
				,"impuesto"
				,"estado"
				,"tipo_cfd"
				,"pedimento"
				,"fecha_ped"
				,"aduana"
				};
		String[] names=new String[]{	
				
				"RFC"
				,"Receptor"
				,"Serie"
				,"Folio"
				,"No_Aprobacion"
				,"Fecha"
				,"Total"
				,"Impuesto"
				,"Estado"
				,"Tipo_CFD"
				,"Pedimento"
				,"Fecha_ped"
				,"Aduana"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("RFC", "rfc");
		installTextComponentMatcherEditor("Receptor", "Receptor");
		installTextComponentMatcherEditor("Serie", "Serie");
		installTextComponentMatcherEditor("Folio", "Folio");
		installTextComponentMatcherEditor("Total", "Total");
		installTextComponentMatcherEditor("Estado", "Estado");
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.periodoDeloquevaDelMes();
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				//,getViewAction()
				//,addAction(null,"imprimir", "Imprimir")
				};
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("","reporteDeVentasDiarias", "Ventas Diarias"));
		return procesos;
	}
	
	public void reporteDeVentasDiarias(){
		VentasDiariasBI.run();
	}

	@Override
	protected List<ReporteMensualCFD> findData() {
		String sql= 
					"SELECT CARGO_ID,TIPO,TIPO_CFD,SERIE,FOLIO,NO_APROBACION,RECEPTOR,EMISOR,CREADO,RFC,TOTAL,IMPUESTO,'1' AS ESTADO FROM SX_COMPROBANTES_IMPRESOS C WHERE  DATE(C.CREADO) BETWEEN ? AND ? " +
					"UNION " +
					"SELECT RFC,RECEPTOR,SERIE,FOLIO,NO_APROBACION,C.CREADO AS FECHA,TOTAL,IMPUESTO,'0' AS ESTADO,TIPO_CFD,PEDIMENTO,PEDIMENTO_FECHA AS FECHA_PED,ADUANA FROM SX_CFD C JOIN SX_CXC_CARGOS_CANCELADOS X ON(X.CARGO_ID=C.ORIGEN_ID) WHERE DATE(C.CREADO) BETWEEN ? AND ? " +
					"UNION " +
					"SELECT RFC,RECEPTOR,SERIE,C.FOLIO,NO_APROBACION,C.CREADO AS FECHA,C.TOTAL,C.IMPUESTO,'0' AS ESTADO,TIPO_CFD,PEDIMENTO,PEDIMENTO_FECHA AS FECHA_PED,ADUANA FROM SX_CFD C JOIN SX_CXC_ABONOS X ON(X.ABONO_ID=C.ORIGEN_ID) WHERE DATE(C.CREADO) BETWEEN ? AND ? AND X.TOTAL=0"
			;
		
		
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
				,new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
				,new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
		};
		return ServiceLocator2
			.getJdbcTemplate()
			.query(sql, args, new BeanPropertyRowMapper(ReporteMensualCFD.class));
		
	}	
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}


}
