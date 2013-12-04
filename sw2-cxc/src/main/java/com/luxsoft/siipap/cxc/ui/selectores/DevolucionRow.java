package com.luxsoft.siipap.cxc.ui.selectores;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.util.SQLUtils;

public class DevolucionRow {
	

	
	private String devo_id;
	private Long numero;
	private String sucursal;
	private String cliente;
	private Date fecha;
	private Long documento;
	private String origen;
	private BigDecimal total;
	
	//private OrigenDeOperacion origenOp;
	
	
	
	
	

	public String getDevo_id() {
		return devo_id;
	}
	public void setDevo_id(String devo_id) {
		this.devo_id = devo_id;
	}
	public Long getNumero() {
		return numero;
	}
	public void setNumero(Long numero) {
		this.numero = numero;
	}
	public String getSucursal() {
		return sucursal;
	}
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	public String getCliente() {
		return cliente;
	}
	public void setCliente(String cliente) {
		this.cliente = cliente;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public Long getDocumento() {
		return documento;
	}
	public void setDocumento(Long documento) {
		this.documento = documento;
	}
	public String getOrigen() {
		return origen;
	}
	public void setOrigen(String origen) {
		this.origen = origen;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
	/*public OrigenDeOperacion getOrigenOp() {
		return origenOp;
	}

	public void setOrigenOp(OrigenDeOperacion origenOp) {
		this.origenOp = origenOp;
	}*/
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((devo_id == null) ? 0 : devo_id.hashCode());
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
		DevolucionRow other = (DevolucionRow) obj;
		if (devo_id == null) {
			if (other.devo_id != null)
				return false;
		} else if (!devo_id.equals(other.devo_id))
			return false;
		return true;
	}
	
/*	public static void main(String[] args) {
		String sql="SELECT D.DEVO_ID,D.numero,S.nombre as sucursal,v.nombre as cliente,d.fecha,v.docto as documento,v.origen,d.total " +
				" FROM sx_devoluciones D USE INDEX(FECHA) JOIN sx_ventas V ON (D.VENTA_ID=V.CARGO_ID)"+  
		" LEFT JOIN sx_cxc_abonos A ON(A.DEVOLUCION_ID=D.DEVO_ID) JOIN SW_SUCURSALES S ON (S.SUCURSAL_ID=V.SUCURSAL_ID)"+
		" WHERE D.FECHA>='2011/01/01' AND V.ORIGEN=\'"+get+"\' AND A.DEVOLUCION_ID IS NULL" ;
		SQLUtils.printBeanClasFromSQL(sql);
	}*/
	

	
	

}
