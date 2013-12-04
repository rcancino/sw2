package com.luxsoft.sw2.replica.valida;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.utils.LoggerHelper;

public class AbonosValidator {
	
	Logger logger=LoggerHelper.getLogger();
	private DateFormat df=new SimpleDateFormat("dd/MM/yyyy");
	
	public AbonosValidator validar(String fecha,Long sucursalId){
		try {
			Date dfecha=df.parse(fecha);
			faltantes(dfecha,sucursalId);
			faltantesAplicaciones(dfecha, sucursalId);
		} catch (ParseException e) {}
		return this;
	}
	
	public void faltantes(Date fecha,Long sucursalId){
		String sql="select ABONO_ID,NOMBRE from SX_CXC_ABONOS where date(fecha)=?";
		Object[] args=new Object[]{
				ValUtils.getPamaeter(fecha)
		};
		List<Map<String,Object>> rows=getJdbcTemplate(sucursalId).queryForList(sql, args);
		List<String> faltantes=new ArrayList<String>();
		for(Map<String, Object> row:rows){
			String id=(String)row.get("ABONO_ID");
			String nombre=(String)row.get("NOMBRE");
			Abono a=(Abono)ServiceLocator2.getHibernateTemplate().get(Abono.class, id);
			if(a==null){
				logger.info("Faltante localizado: "+id+ " Cliente: "+nombre);
				faltantes.add(id);
			}
		}
		logger.info("Total de faltantes : "+faltantes.size());	
		
	}
	
	public void faltantesAplicaciones(Date fecha,Long sucursalId){
		String sql="select a.APLICACION_ID,a.NOMBRE,b.fecha,b.total from sx_cxc_aplicaciones a join sx_cxc_abonos b on (a.abono_id=b.abono_id) where date(a.fecha)=?";
		Object[] args=new Object[]{
				ValUtils.getPamaeter(fecha)
		};
		List<Map<String,Object>> rows=getJdbcTemplate(sucursalId).queryForList(sql, args);
		List<String> faltantes=new ArrayList<String>();
		for(Map<String, Object> row:rows){
			String id=(String)row.get("APLICACION_ID");
			String nombre=(String)row.get("NOMBRE");
			Date abonoFecha=(Date)row.get("fecha");
			BigDecimal total=(BigDecimal)row.get("total");
			Aplicacion a=(Aplicacion)ServiceLocator2.getHibernateTemplate().get(Aplicacion.class, id);
			if(a==null){
				logger.info("Aplicación faltante : "+id+ " Cliente: "+nombre+ " Fecha abono: "+abonoFecha+ " Por: "+total);
				faltantes.add(id);
			}
		}
		logger.info("Total de faltantes : "+faltantes.size());	
		
	}
	
	public JdbcTemplate getJdbcTemplate(Long sucursalId){
		return ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
	}
	
	public static void main(String[] args) {
		new AbonosValidator()
			.validar("30/05/2012", 3L)
			;
	}

}
