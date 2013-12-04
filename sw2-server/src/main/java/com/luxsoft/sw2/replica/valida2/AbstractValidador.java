package com.luxsoft.sw2.replica.valida2;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;


import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw2.replica.valida.ConnectionServices;




/**
 * Plantilla base para los validadores de informacion
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AbstractValidador {
	
	Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	private String tabla;
	
	private String entidad;
	
	private String sucursalSql;
	
	private String centralSql;
	
	private boolean limpiar=true;
	
	//private String beanName;
	
	public AbstractValidador() {
	}

	public AbstractValidador(String entidad, String tabla) {
		super();
		this.entidad = entidad;
		this.tabla = tabla;
	}

	public List<Validacion> validar() {
		return validar(Periodo.hoy());
	}

	public List<Validacion> validar(Periodo periodo) {
		logger.info("Validando periodo: "+periodo);		
		
		List<Validacion> validaciones=new ArrayList<Validacion>();
		
		for(Long sucursalId:getSucursales()){
			try {
				List<Validacion> res=validar(sucursalId, periodo);
				validaciones.addAll(res);
			} catch (Exception e) {
				e.printStackTrace();
				String msg=ExceptionUtils.getRootCauseMessage(e);
				Throwable cause=ExceptionUtils.getRootCause(e);
				logger.error(msg,cause);
			}
		}
		for(Validacion v:validaciones){
			System.out.println(ToStringBuilder.reflectionToString(v,ToStringStyle.SHORT_PREFIX_STYLE));
		}
		return validaciones;
	}
	
	protected List<Validacion> validar(Long sucursalId,Periodo periodo){
		
		//Analisis para la central
		List<Validacion> validacionesTarget=analizarEnCentral(sucursalId, periodo);
		
		//Analisis primero para la sucursal
		List<Validacion> validaciones=analizarEnSucursal(sucursalId, periodo);
		
		
		for(final Validacion target:validaciones){
			Validacion source=(Validacion) CollectionUtils.find(validacionesTarget, new Predicate(){
				public boolean evaluate(Object object) {
					if(object!=null){
						Validacion v=(Validacion)object;
						if(DateUtils.isSameDay(v.getFecha(), target.getFecha())){
							return v.getConcepto().equals(target.getConcepto());
						}
					}
					return false;
				}
			});
			if(source!=null){
				target.setRegistrosEnCentral(source.getRegistrosEnCentral());
				target.setControl_1_central(source.getControl_1_central());
				target.setControl_2_central(source.getControl_2_central());
				target.setControl_3_central(source.getControl_3_central());
			}
		}		
		logger.info("Validaciones de la sucursal :"+sucursalId+" generadas: "+validaciones.size());
		return validaciones;
	}
	
	protected Object[] getParametrosSQL(Long sucursalId,Periodo periodo){
		Object[] parametros={
				new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
				,new SqlParameterValue(Types.INTEGER,sucursalId)
						};
		return parametros;
	}
	
	protected List<Validacion> analizarEnSucursal(Long sucursalId,Periodo periodo){
		
		JdbcTemplate source=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
		List<Map<String, Object>> rows=source.queryForList(getSucursalSql(), getParametrosSQL(sucursalId, periodo));
		List<Validacion> validaciones=new ArrayList<Validacion>(rows.size());
		for(Map<String,Object> row:rows){
			Validacion target=new Validacion();
			BeanWrapperImpl wrapper=new BeanWrapperImpl(target);
			for(Map.Entry<String, Object> entry:row.entrySet()){
				wrapper.setPropertyValue(entry.getKey(), entry.getValue());
			}
			target.setActualizacion(new Date());
			target.setTabla(getTabla());
			target.setEntidad(getEntidad());
			target.setSucursalId(sucursalId);			
			validaciones.add(target);
			
		}
		return validaciones;
	}
	
	protected List<Validacion> analizarEnCentral(Long sucursalId,Periodo periodo){
		List<Map<String, Object>> rows=getJdbcTemplate().queryForList(getCentralSql(), getParametrosSQL(sucursalId, periodo));
		List<Validacion> validaciones=new ArrayList<Validacion>(rows.size());
		for(Map<String,Object> row:rows){
			Validacion target=new Validacion();
			BeanWrapperImpl wrapper=new BeanWrapperImpl(target);
			for(Map.Entry<String, Object> entry:row.entrySet()){
				wrapper.setPropertyValue(entry.getKey(), entry.getValue());
			}
			validaciones.add(target);
			
		}
		return validaciones;
	}
	
	protected void persistir(List<Validacion> res){
		for(int index=0;index<res.size();index++){
			Validacion v=res.get(index);
			//v=getManager().salvar(v);
			res.set(index, v);
		}
	}
	
	public void limpiar(final Periodo periodo){
		String DELETE="DELETE FROM SX_VALIDACIONES_REPLICA WHERE TABLA=? AND ENTIDAD=? AND FECHA BETWEEN ? AND ?";		
		int res=getJdbcTemplate().update(DELETE, new Object[]{
				new SqlParameterValue(Types.VARCHAR,getTabla())
				,new SqlParameterValue(Types.VARCHAR,getEntidad())
				,new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
				}
		);
		logger.info("Validaciones eliminadas: "+res+" Entidad: "+getEntidad()+" Tabla: "+getTabla()+ "  Periodo: "+periodo);
	}

	

	public String getSucursalSql() {
		return sucursalSql;
	}

	public void setSucursalSql(String sucursalSql) {
		this.sucursalSql = sucursalSql;
	}

	public String getCentralSql() {
		return centralSql;
	}

	public void setCentralSql(String centralSql) {
		this.centralSql = centralSql;
	}

	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public void addSucursal(Long... sucs){
		this.sucursales.clear();
		this.sucursales.addAll(Arrays.asList(sucs));
	}	

	public String getEntidad() {
		return entidad;
	}

	public void setEntidad(String entidad) {
		this.entidad = entidad;
	}

	public String getTabla() {
		return tabla;
	}

	public void setTabla(String tabla) {
		this.tabla = tabla;
	}
	

	public boolean isLimpiar() {
		return limpiar;
	}

	public void setLimpiar(boolean limpiar) {
		this.limpiar = limpiar;
	}

	public JdbcTemplate getJdbcTemplate(){
		return ServiceLocator2.getJdbcTemplate();
	}


	public static final String REGISTROS_DE_VALIDACION="REGISTROS DE VALIDACION PERSISTIDOS";
	
	
/*
	public void setBeanName(String name) {
		this.beanName=name;		
	}

	public String getBeanName() {
		return beanName;
	}
	*/
	
}
