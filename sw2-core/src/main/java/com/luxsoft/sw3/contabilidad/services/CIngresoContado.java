package com.luxsoft.sw3.contabilidad.services;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.ClassUtils;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.tesoreria.model.TipoDeAplicacion;

/**
 *  Entidad para representar una venta facturada en papel y trabajarla desde contabilidad usando
 *  SQL puro
 *  
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class CIngresoContado {
	
	//select V.TIPO,S.NOMBRE AS SUCURSAL,V.CARGO_ID,V.ORIGEN,V.TOTAL,V.IMPORTE,V.IMPUESTO from SX_VENTAS V JOIN SW_SUCURSALES S ON(S.SUCURSAL_ID=V.SUCURSAL_ID) WHERE V.FECHA=? AND V.TIPO=?"
	
	String TIPO;
	String SUCURSAL;
	String ORIGEN_ID;
	Long  CARGOABONO_ID;
	String ORIGEN;
	String DESCRIPCION;
	String BANCO;
	BigDecimal IMPORTE;
	String CONCEPTO;
	
	public String getTIPO() {
		return TIPO;
	}
	public void setTIPO(String tipo) {
		TIPO = tipo;
	}
	
	public String getSUCURSAL() {
		return SUCURSAL;
	}
	public void setSUCURSAL(String sucursal) {
		SUCURSAL = sucursal;
	}	
	public String getORIGEN() {
		return ORIGEN;
	}
	public void setORIGEN(String origen) {
		ORIGEN = origen;
	}
	public BigDecimal getIMPORTE() {
		return IMPORTE;
	}
	public void setIMPORTE(BigDecimal importe) {
		IMPORTE = importe;
	}
	
	
	public String getORIGEN_ID() {
		return ORIGEN_ID;
	}
	public void setORIGEN_ID(String origen_id) {
		ORIGEN_ID = origen_id;
	}
	public Long getCARGOABONO_ID() {
		return CARGOABONO_ID;
	}
	public void setCARGOABONO_ID(Long cargoabono_id) {
		CARGOABONO_ID = cargoabono_id;
	}
	public String getDESCRIPCION() {
		return DESCRIPCION;
	}
	public void setDESCRIPCION(String descripcion) {
		DESCRIPCION = descripcion;
	}
	public String getBANCO() {
		return BANCO;
	}
	public void setBANCO(String banco) {
		BANCO = banco;
	}	
	public String getCONCEPTO() {
		return CONCEPTO;
	}
	public void setCONCEPTO(String concepto) {
		CONCEPTO = concepto;
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((CARGOABONO_ID == null) ? 0 : CARGOABONO_ID.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CIngresoContado other = (CIngresoContado) obj;
		if (CARGOABONO_ID == null) {
			if (other.CARGOABONO_ID != null)
				return false;
		} else if (!CARGOABONO_ID.equals(other.CARGOABONO_ID))
			return false;
		return true;
	}	
	
	public static List<CIngresoContado> buscarIngresos(Date fecha){
		String path=ClassUtils.addResourcePathToPackagePath(CIngresoContado.class, "IngresosContado.sql");
		String SQL=SQLUtils.loadSQLQueryFromResource(path);
		Object[] params=new Object[]{
				 new SqlParameterValue(Types.DATE,fecha)
				,new SqlParameterValue(Types.DATE,fecha)
				 ,new SqlParameterValue(Types.DATE,fecha)
		};
		return ServiceLocator2.getJdbcTemplate().query(SQL,params,new BeanPropertyRowMapper(CIngresoContado.class));
	}
	
	
	public static List<CIngresoContado> buscarIngresosCredito(Date fecha){
		String path=ClassUtils.addResourcePathToPackagePath(CIngresoContado.class, "IngresosCredito.sql");
		String SQL=SQLUtils.loadSQLQueryFromResource(path);
		Object[] params=new Object[]{
				 new SqlParameterValue(Types.DATE,fecha)
				,new SqlParameterValue(Types.DATE,fecha)
		};
		return ServiceLocator2.getJdbcTemplate().query(SQL,params,new BeanPropertyRowMapper(CIngresoContado.class));
	}
	
	public static void main(String[] args) {
		List<CIngresoContado> res=buscarIngresos(DateUtil.toDate("01/06/2011"));
		System.out.println("Ingresos: "+res.size());
	}

}
