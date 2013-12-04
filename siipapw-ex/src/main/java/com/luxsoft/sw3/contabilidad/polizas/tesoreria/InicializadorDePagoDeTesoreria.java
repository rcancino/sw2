package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;


public class InicializadorDePagoDeTesoreria {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	
	public InicializadorDePagoDeTesoreria(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	public void inicializar(ModelMap model){
		pagos(model);
	}
	
	private void pagos(ModelMap model){
		Periodo periodo=(Periodo)model.get("periodo");
		String sql="SELECT B.CARGOABONO_ID" +
				"  FROM sw_bcargoabono B LEFT JOIN SW_TREQUISICION T ON (B.CARGOABONO_ID=T.CARGOABONO_ID) WHERE " +
				"  B.FECHA between ? and ? AND B.ORIGEN=\'TESORERIA\'" +
				" AND (T.CONCEPTO_ID IN(224289,737336,737344) OR T.REQUISICION_ID IS NULL)";
		Object[] args=new Object[]{
			new SqlParameterValue(Types.DATE, periodo.getFechaInicial())	
			,new SqlParameterValue(Types.DATE, periodo.getFechaFinal())
		};
		final List<Long> pagosIds=getJdbcTemplate().queryForList(sql, args,Long.class);
		model.addAttribute("pagosIds", pagosIds);
		
	}
	
		
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


	public static void main(String[] args) {
		DBUtils.whereWeAre();
		InicializadorDePagoDeTesoreria i=new InicializadorDePagoDeTesoreria(ServiceLocator2.getHibernateTemplate(),ServiceLocator2.getJdbcTemplate());
		ModelMap model=new ModelMap();
		model.addAttribute("periodo", Periodo.getPeriodoDelMesActual(DateUtil.toDate("04/01/2012")));
		i.inicializar(model);
			
	}

}
