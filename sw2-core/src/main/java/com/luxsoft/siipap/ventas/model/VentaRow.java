package com.luxsoft.siipap.ventas.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * Bean para la proyecccion de ventas en GUI. Abstrae los elementos mas importantes
 * 
 * @author RUBEN
 *
 */
public class VentaRow {

	private Serializable id;

	private String sucursal;

	private Long clienteId;

	private String clave;

	private String nombre;

	private Date fecha;

	private Date vencimiento;

	private String factura;

	

	private String origen;

	private String tipo;

	private BigDecimal importe;

	private BigDecimal iva;

	private BigDecimal total;

	private double tc;

	private BigDecimal saldo;

	public VentaRow(final Venta v) {
		this.id=v.getId();
		this.sucursal=v.getSucursal().getNombre();
		this.nombre=v.getNombre();
		this.fecha=v.getFecha();
		this.origen=v.getOrigen().name();		
		this.total=v.getTotal();
		this.saldo=v.getSaldo();
		this.tc=v.getTc();
		this.vencimiento=v.getVencimiento();
	}

	public Object getId() {
		return id;
	}

	public String getSucursal() {
		return sucursal;
	}

	public Long getClienteId() {
		return clienteId;
	}

	public String getClave() {
		return clave;
	}

	public String getNombre() {
		return nombre;
	}

	public Date getFecha() {
		return fecha;
	}

	public Date getVencimiento() {
		return vencimiento;
	}

	public String getFactura() {
		return factura;
	}

	public String getOrigen() {
		return origen;
	}

	public String getTipo() {
		return tipo;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public BigDecimal getIva() {
		return iva;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public double getTc() {
		return tc;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	@Override
	public String toString() {
		return "VentaRow [sucursal=" + sucursal + ", factura=" + factura
				+ ", origen=" + origen + "]";
	}
	
	

	

}
