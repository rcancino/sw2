package com.luxsoft.sw3.cxp.consultas;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.SQLUtils;

public class CargoRow {
	
	private String nombre;
	private long requisicion;
	private long cxp;
	private String documento;
	private Date fecha;
	private Date vencimiento;
	private String moneda;
	private BigDecimal total;
	private BigDecimal pagos;
	private BigDecimal saldo;
	private String tipo;
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public long getRequisicion() {
		return requisicion;
	}
	public void setRequisicion(long requisicion) {
		this.requisicion = requisicion;
	}
	public long getCxp() {
		return cxp;
	}
	public void setCxp(long cxp) {
		this.cxp = cxp;
	}
	public String getDocumento() {
		return documento;
	}
	public void setDocumento(String documento) {
		this.documento = documento;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public Date getVencimiento() {
		return vencimiento;
	}
	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}
	public String getMoneda() {
		return moneda;
	}
	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public BigDecimal getPagos() {
		return pagos;
	}
	public void setPagos(BigDecimal pagos) {
		this.pagos = pagos;
	}
	public BigDecimal getSaldo() {
		return saldo;
	}
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public static String sql="SELECT c.nombre," +
			"(SELECT R.REQUISICION_ID FROM sw_trequisiciondet R WHERE R.CXP_ID=C.CXP_ID) AS requisicion,"
			+" C.cxp_id as cxp,C.documento,C.fecha,C.VTO AS vencimiento,C.moneda,C.total"
			+" ,IFNULL((SELECT SUM(A.IMPORTE) FROM sx_cxp_aplicaciones A WHERE A.CARGO_ID=C.CXP_ID),0) AS pagos"
			+" ,C.TOTAL-IFNULL((SELECT SUM(A.IMPORTE) FROM sx_cxp_aplicaciones A WHERE A.CARGO_ID=C.CXP_ID),0) AS saldo"			
			+" FROM sx_cxp c where C.TIPO=\'FACTURA\' "
			+" AND c.PROVEEDOR_ID=? and c.MONEDA=? " +
			"  AND C.TOTAL-IFNULL((SELECT SUM(A.IMPORTE) FROM sx_cxp_aplicaciones A WHERE A.CARGO_ID=C.CXP_ID),0)>0";
	
	public static List<CargoRow> buscarPendientes(Long proveedorId,String moneda){
		return ServiceLocator2.getJdbcTemplate().query(sql
				, new Object[]{proveedorId,moneda}
				, new BeanPropertyRowMapper(CargoRow.class));
	}
	
	public static void main(String[] args) {
		SQLUtils.printBeanClasFromSQL(sql, true);
	}
	
	

}
