package com.luxsoft.sw3.contabilidad.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.MonedasUtils;

import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;

public abstract class  AbstractPolizaManager implements PolizaContableManager{

	protected Poliza poliza;
	
	public Poliza generarPoliza(Date fecha) {
		inicializarPoliza(fecha);
		inicializarDatos();
		procesarPoliza();
		poliza.actualizar();
		return poliza;
	}
	
	protected void inicializarPoliza(final Date fecha){
		poliza=new Poliza();
		poliza.setFecha(fecha);
		poliza.setTipo(Poliza.Tipo.INGRESO);
		poliza.setDescripcion(MessageFormat.format("Poliza de ventas del {0,date,short}",fecha));
	}
	
	protected void inicializarDatos(){
		
	}
	
	protected abstract void procesarPoliza();

	public Poliza salvarPoliza(Poliza poliza) {
		poliza=getPolizaManager().salvarPoliza(poliza);
		return poliza;
	}
	
	public boolean eliminarPoliza(Poliza poliza) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	/**
	 * Rgresa una lista de partidas det tipo {@link PolizaDet} utilizando un RowMapper
	 * 
	 * @param sql La sentencia SQL a ejecutar para traer los registros
	 * @param mapper Implementacion de RowMapper para convertir cada registro en PolizaDet
	 * @param parametros Parametros para el query
	 * @return La lista de beans PolizaDet
	 */
	public List<PolizaDet> getPolizaDetRows(String sql,RowMapper mapper,Object...parametros){
		List<PolizaDet> data=getJdbcTemplate().query(sql, parametros, mapper);
		return data;
	}
	/**
	 * Regresa la lista de registros PolizaDet asumiendo que cada columna de la 
	 * sentencia SQL representa una propiedad de PolizaDet 
	 * 
	 * @param sql La sentencia SQL apropiada para regresa registros que representen por si solos 
	 *        	  entidades PolizaDet
	 * @param parametros  Parametros del query SQL
	 * @return La lista de beans PolizaDet
	 */
	public List<PolizaDet> getPolizaDetBeans(String sql,Object... parametros){
		return getData(sql,PolizaDet.class,parametros);
	}
	
	public final BigDecimal calcularImpuesto(BigDecimal importe){
		return importe.multiply(MonedasUtils.IVA);
	}
	public  final BigDecimal calcularTotal(BigDecimal importe){
		return importe.add(calcularImpuesto(importe));
	}
	public final BigDecimal calcularImporteDelTotal(BigDecimal total){
		return calcularImporteDelTotal(total, 4);
	}
	
	public final BigDecimal calcularImporteDelTotal(BigDecimal total,int decimales){
		BigDecimal val=BigDecimal.valueOf(1).add(MonedasUtils.IVA);
		BigDecimal importe=total.divide(val,decimales,RoundingMode.HALF_EVEN);
		return importe;
	}
	
	public final BigDecimal redondear(BigDecimal valor){
		return CantidadMonetaria.pesos(valor).amount();
	}
	
	/**
	 * 
	 * @param sql
	 * @param mapClass
	 * @param parametros
	 * @return
	 */
	public List getData(String sql,Class mapClass,Object... parametros){
		return getData(sql, new BeanPropertyRowMapper(mapClass),parametros);
	}
	
	public List<Map<String, Object>> getData(String sql,Object...parametros){
		List<Map<String, Object>> rows=getJdbcTemplate().queryForList(sql,parametros);
		return rows;
	}
	
	public List getHibernateData(String hql,Object...parametros){
		return getHibernateTemplate().find(hql, parametros);
	}
	
	public JdbcTemplate getJdbcTemplate(){
		return ServiceLocator2.getJdbcTemplate();
	}
	
	public HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}
	
	public PolizasManager getPolizaManager(){
		return ServiceLocator2.getPolizasManager();
	}
	
	public CuentaContable getCuenta(String clave){
		return ServiceLocator2.getCuentasContablesManager().buscarPorClave(clave);
	}
	
	public List<Sucursal> sucursales;
	
	public Sucursal getSucursal(final Long id){
		if(sucursales==null)
			sucursales=ServiceLocator2.getLookupManager().getSucursales();
		return (Sucursal)CollectionUtils.find(sucursales, new Predicate() {
			public boolean evaluate(Object object) {
				Sucursal s=(Sucursal)object;
				return s.getId().equals(id);
			}
		});
	}
	

}
