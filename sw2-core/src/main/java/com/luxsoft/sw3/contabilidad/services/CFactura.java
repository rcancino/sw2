package com.luxsoft.sw3.contabilidad.services;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;

/**
 *  Entidad para representar una venta facturada en papel y trabajarla desde contabilidad usando
 *  SQL puro
 *  
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class CFactura {
	
	//select V.TIPO,S.NOMBRE AS SUCURSAL,V.CARGO_ID,V.ORIGEN,V.TOTAL,V.IMPORTE,V.IMPUESTO from SX_VENTAS V JOIN SW_SUCURSALES S ON(S.SUCURSAL_ID=V.SUCURSAL_ID) WHERE V.FECHA=? AND V.TIPO=?"
	
	String TIPO;
	String SUCURSAL;
	String CARGO_ID;
	String ORIGEN;
	BigDecimal TOTAL;
	BigDecimal IMPORTE;
	BigDecimal IMPUESTO;
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
	
	public String getCARGO_ID() {
		return CARGO_ID;
	}
	public void setCARGO_ID(String cargo_id) {
		CARGO_ID = cargo_id;
	}
	public String getORIGEN() {
		return ORIGEN;
	}
	public void setORIGEN(String origen) {
		ORIGEN = origen;
	}
	public BigDecimal getTOTAL() {
		return TOTAL;
	}
	public void setTOTAL(BigDecimal total) {
		TOTAL = total;
	}
	public BigDecimal getIMPORTE() {
		return IMPORTE;
	}
	public void setIMPORTE(BigDecimal importe) {
		IMPORTE = importe;
	}
	public BigDecimal getIMPUESTO() {
		return IMPUESTO;
	}
	public void setIMPUESTO(BigDecimal impuesto) {
		IMPUESTO = impuesto;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((CARGO_ID == null) ? 0 : CARGO_ID.hashCode());
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
		CFactura other = (CFactura) obj;
		if (CARGO_ID == null) {
			if (other.CARGO_ID != null)
				return false;
		} else if (!CARGO_ID.equals(other.CARGO_ID))
			return false;
		return true;
	}
	
	public static String SQL="select V.TIPO,S.NOMBRE AS SUCURSAL,V.CARGO_ID,V.ORIGEN,V.TOTAL,V.IMPORTE,V.IMPUESTO from SX_VENTAS V JOIN SW_SUCURSALES S ON(S.SUCURSAL_ID=V.SUCURSAL_ID) WHERE V.FECHA=? AND V.TIPO=?";
	
	public static List<CFactura> buscarFacturas(Date fecha){
		Object[] params=new Object[]{
				 new SqlParameterValue(Types.DATE,fecha)
				,new SqlParameterValue(Types.VARCHAR,"FAC")
		};
		return ServiceLocator2.getJdbcTemplate().query(SQL,params,new BeanPropertyRowMapper(CFactura.class));
	}
	
	public static void main(String[] args) {
		List<CFactura> res=buscarFacturas(DateUtil.toDate("02/05/2011"));
		System.out.println("Ventas: "+res.size());
	}

}
