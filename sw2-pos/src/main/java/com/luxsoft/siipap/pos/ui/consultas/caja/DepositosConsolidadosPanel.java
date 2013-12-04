package com.luxsoft.siipap.pos.ui.consultas.caja;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Panel de consulta para depositos generados en ambos sistemas
 * 
 * @author ruben
 *
 */
public class DepositosConsolidadosPanel extends FilteredBrowserPanel<DepositoUniversal>{
	
	
	public DepositosConsolidadosPanel() {
		super(DepositoUniversal.class);
		
	}
	
	protected void init(){
		addProperty("ORIGEN","SUCURSAL","FOLIO","SOLICITO","FECHA","CLAVE","NOMBRE","TIPO_VTA","FECHA_DEPOSITO","REFERENCIA","FPAGO","BANCO"
				,"TOTAL","BANCO_DEST","STATUS","COMENTARIO");
		installTextComponentMatcherEditor("Sucursal", "SUCURSAL");
		installTextComponentMatcherEditor("Cliente", "CLAVE","NOMBRE");
		installTextComponentMatcherEditor("Fecha Dep", "FECHA_DEPOSITO");
		installTextComponentMatcherEditor("Banco Orig", "BANCO");
		installTextComponentMatcherEditor("Banco Dest", "BANCO_DEST");
		installTextComponentMatcherEditor("Importe", "TOTAL");
		installTextComponentMatcherEditor("Solicito", "SOLICITO");
	}
	
	

	@Override
	protected List<DepositoUniversal> findData() {
		String sql="SELECT 'DBF' AS ORIGEN,DEPNOMBSUC AS SUCURSAL,DEPNUMERO AS FOLIO,DEPNOMBSOL AS SOLICITO,DEPFECSOL AS FECHA " +
		" ,DEPCVECLI AS CLAVE,DEPNOMBCLI AS NOMBRE,DEPTIPVTA AS TIPO_VTA,0 AS SIIPAP_ID" +
		",DEPFECDEP AS FECHA_DEPOSITO,DEPNUMREF AS REFERENCIA,DEPFORMDEP AS FPAGO" +
		",DEPNOMBREF AS BANCO,DEPIMPDEP AS TOTAL,DEPNOMBBAN AS BANCO_DEST" +
		",DEPSTATUS AS STATUS,DEPOBSSTAT AS COMENTARIO " +
		" FROM AUTDEP12 WHERE DEPFECDEP BETWEEN #12/01/2009# AND #12/31/2009# " +
		" AND DEPSTATUS NOT IN('CAN')";
			JdbcTemplate t1=new JdbcTemplate(POSDBUtils.createSiipapDataSource());
			List<DepositoUniversal> rows=t1.query(sql, new BeanPropertyRowMapper(DepositoUniversal.class));
			
			
			String sql2=SQLUtils.loadSQLQueryFromResource("sql/DepositosDuplicadosMySQL.sql");
			List<DepositoUniversal> rows2=Services.getInstance().getJdbcTemplate().query(sql2,new BeanPropertyRowMapper(DepositoUniversal.class));
			rows.addAll(rows2);
		return rows;
	}
	
	

}
