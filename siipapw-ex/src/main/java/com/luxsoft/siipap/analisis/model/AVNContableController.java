package com.luxsoft.siipap.analisis.model;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.SQLUtils;

import freemarker.template.SimpleDate;

public class AVNContableController {
	
	private JdbcTemplate jdbcTemplate;
	
	
	
	public List<ImportesMensuales> buscarImportesDeVentas(final int year,String tipo){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/analisis/resumenVentas.sql");
		String origen="";
		if(!tipo.startsWith("TOD")){
			if("CRE".equalsIgnoreCase(tipo)){
				origen=" AND ORIGEN=\'"+tipo+"\'";
			}else{
				origen=" AND ORIGEN IN(\'MOS\',\'CAM\')";
			}
		}
		sql=sql.replaceAll("@ORIGEN", origen);
		BeanPropertyRowMapper mapper=new BeanPropertyRowMapper(ImportesMensuales.class);
		SqlParameterValue p1=new SqlParameterValue(Types.INTEGER,year);
		return getJdbcTemplate().query(sql, new Object[]{p1},mapper);
	}
	
	public List<ImportesMensuales> buscarImportesDescuentos(final int year,String tipo){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/analisis/resumenNotas.sql");
		sql=sql.replaceAll("@TIPOS", "\'L\',\'U\',\'V\',\'F\',\'C\'");
		String origen="";
		if(!tipo.startsWith("TOD")){
			if("CRE".equalsIgnoreCase(tipo)){
				origen=" AND SERIEDOCUMENTO=\'E\'";
			}else if("CON".equalsIgnoreCase(tipo)){
				origen=" AND SERIEDOCUMENTO<>\'E\'";
			}
		}
		sql=sql.replaceAll("@ORIGEN", origen);		
		BeanPropertyRowMapper mapper=new BeanPropertyRowMapper(ImportesMensuales.class);
		SqlParameterValue p1=new SqlParameterValue(Types.INTEGER,year);
		return getJdbcTemplate().query(sql, new Object[]{p1},mapper);
		
	}
	
	public List<ImportesMensuales> buscarImportesDeDevos(final int year,String tipo){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/analisis/resumenNotas.sql");
		sql=sql.replaceAll("@TIPOS", "\'H\',\'I\',\'J\'");
		String origen="";
		if(!tipo.startsWith("TOD")){
			if("CRE".equalsIgnoreCase(tipo)){
				origen=" AND SERIEDOCUMENTO=\'E\'";
			}else if("CON".equalsIgnoreCase(tipo)){
				origen=" AND SERIEDOCUMENTO<>\'E\'";
			}
		}
		sql=sql.replaceAll("@ORIGEN", origen);
		BeanPropertyRowMapper mapper=new BeanPropertyRowMapper(ImportesMensuales.class);
		SqlParameterValue p1=new SqlParameterValue(Types.INTEGER,year);
		return getJdbcTemplate().query(sql, new Object[]{p1},mapper);
	}
	
	public List<ImportesMensuales> buscarCosto(final int year,String tipo){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/analisis/resumenCosto.sql");		
		String origen="";
		if(!tipo.startsWith("TOD")){
			if("CRE".equalsIgnoreCase(tipo)){
				origen=" AND SERIE=\'E\'";
			}else if("CON".equalsIgnoreCase(tipo)){
				origen=" AND SERIE<>\'E\'";
			}
		}
		sql=sql.replaceAll("@ORIGEN", origen);
		BeanPropertyRowMapper mapper=new BeanPropertyRowMapper(ImportesMensuales.class);
		SqlParameterValue p1=new SqlParameterValue(Types.INTEGER,year);
		return getJdbcTemplate().query(sql, new Object[]{p1},mapper);
	}
	
	public List<ImportesMensuales> buscarImportesDeProvision(final int year){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/analisis/resumenProvision.sql");
		DateFormat df=new SimpleDateFormat("dd/MM/yyyy");
		sql=sql.replaceAll("@FECHA",df.format(new Date()));
		SqlParameterValue p1=new SqlParameterValue(Types.INTEGER,year);
		List<Map<String,Object>> rows=getJdbcTemplate().queryForList(sql, new Object[]{p1});
		//System.out.println(rows);
		List<ImportesMensuales> importes=new ArrayList<ImportesMensuales>();
		
		for(Map<String,Object> row:rows){
			ImportesMensuales bean=new ImportesMensuales();
			bean.setYear(year);
			Number mes=(Number)row.get("MES");
			BigDecimal val=(BigDecimal)row.get("IMP_PROV");
			bean.setMes(mes.intValue());
			bean.setTotal(val);
			importes.add(bean);
		}
		
		return importes;
	}
	
	public List<ImportesMensuales> buscarGastos(final int year){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/analisis/resumenGastos.sql");
		sql=sql.replaceAll("@YEAR", String.valueOf(year));
		
		List<ImportesMensuales> res=getJdbcTemplate().query(sql, new BeanPropertyRowMapper(ImportesMensuales.class));
		System.out.println(res);
		
		return res;
	}
	
	public List<AVNContablePorConcepto> cargarAnalisis(int year,String tipo){
		String ttipo=StringUtils.substring(tipo, 0, 3);
		List<AVNContablePorConcepto> res=new ArrayList<AVNContablePorConcepto>();

		//Ventas
		AVNContablePorConcepto ven=new AVNContablePorConcepto("Ventas");
		ven.setYear(year);
		ven.getImportes().addAll(buscarImportesDeVentas(year,ttipo));
		res.add(0,ven);
		
		// Descuentos
		AVNContablePorConcepto desc=new AVNContablePorConcepto("Descuentos");
		desc.setYear(year);
		desc.getImportes().addAll(buscarImportesDescuentos(year,ttipo));		
		res.add(1,desc);
		
		// Devoluciones
		AVNContablePorConcepto devos=new AVNContablePorConcepto("Devoluciones");
		devos.setYear(year);
		devos.getImportes().addAll(buscarImportesDeDevos(year,ttipo));		
		res.add(2,devos);
		
		// Provision
		
		AVNContablePorConcepto prov=new AVNContablePorConcepto("Provision");
		prov.setYear(year);
		if(tipo.startsWith("TOD") || tipo.startsWith("CRE")){
			prov.getImportes().addAll(buscarImportesDeProvision(year));
		}		
		res.add(3,prov);
		
		//Venta Neta
		AVNVentaNeta vn=new AVNVentaNeta();
		vn.setYear(year);
		vn.getImportes().addAll(ven.getImportes());
		vn.getImportes().addAll(desc.getImportes());
		vn.getImportes().addAll(devos.getImportes());
		vn.getImportes().addAll(prov.getImportes());
		res.add(vn);
		
		
		// Costo
		AVNContablePorConcepto costos=new AVNContablePorConcepto("Costo");
		costos.setYear(year);
		costos.getImportes().addAll(buscarCosto(year,ttipo));		
		res.add(costos);
		
		//Utilidad
		AVNVentaNeta util=new AVNVentaNeta("Utilidad");
		util.setYear(year);
		util.getImportes().addAll(vn.getImportes());
		util.getImportes().addAll(costos.getImportes());
		res.add(util);
		
		//Gastos}
		AVNContablePorConcepto gastos=new AVNContablePorConcepto("Gastos");
		gastos.setYear(year);
		gastos.getImportes().addAll(buscarGastos(year));		
		res.add(gastos);
		
		//% Costo
		CostoEnProporcion cosAsProp=new CostoEnProporcion("Costo %");
		cosAsProp.setVentaNeta(vn);
		cosAsProp.setCostos(costos);
		res.add(cosAsProp);
		
		//% Utilidad
		UtilidadEnProporcion utilAsProp=new UtilidadEnProporcion("Utilidad %");
		utilAsProp.setVentaNeta(vn);
		utilAsProp.setUtilidad(util);
		res.add(utilAsProp);
		
		return res;
	}
	
	/**
	 * Importe de la provision historica
	 * 
	 * @param year
	 * @param mes El mes de calculo, 0 based
	 * @return
	 */
	public List<ImportesMensuales> buscarImportesDeProvisionHistorica(final int year,int mes){		
		System.out.println("Procesando provision historica....para :"+year+ "/ "+mes);
		String sql=SQLUtils.loadSQLQueryFromResource("sql/analisis/resumenProvisionHistorica.sql");
		Periodo p=Periodo.getPeriodoEnUnMes(mes, year);
		DateFormat df=new SimpleDateFormat("dd/MM/yyyy");
		sql=sql.replaceAll("@FECHA", "\'"+df.format(p.getFechaFinal())+"\'");
		List<Map<String,Object>> rows=getJdbcTemplate().queryForList(sql);
		//System.out.println(rows);
		List<ImportesMensuales> importes=new ArrayList<ImportesMensuales>();
		
		for(Map<String,Object> row:rows){
			ImportesMensuales bean=new ImportesMensuales();
			Number yy=(Number)row.get("YEAR");
			bean.setYear(yy.intValue());
			Number currMes=(Number)row.get("MES");
			BigDecimal val=(BigDecimal)row.get("IMP_PROV");
			bean.setMes(currMes.intValue());
			bean.setImportePorMes(val, mes+1);
			bean.setTotal(val);
			importes.add(bean);
		}
		
		return importes;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public static void printRows(List rows){
		for(Object row:rows){
			System.out.println(row);
		}
	}
	
	public static void main(String[] args) {
		AVNContableController controller=new AVNContableController();
		controller.setJdbcTemplate(ServiceLocator2.getAnalisisJdbcTemplate());
		printRows(controller.buscarImportesDeProvisionHistorica(2008,0));
	}
	

}
