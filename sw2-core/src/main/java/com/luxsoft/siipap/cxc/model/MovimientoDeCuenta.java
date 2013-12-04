package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.Date;

public class MovimientoDeCuenta {
	
	/**
	 * 
	
	
	
	 */
	
	private String nombre;
	private String tipo;
	private Long documento;
	private String sucursal;
	private Currency moneda;
	private BigDecimal total;
	private Date fecha;
	private Date revision;
	private Date vencimiento;
	private String referencia;
	
	
	protected double descuento;
	protected boolean aplicable;
	
	private BigDecimal saldoCargo=BigDecimal.ZERO;	
	private BigDecimal saldoAcumulado=BigDecimal.ZERO;	
	private BigDecimal saldoAFavor=BigDecimal.ZERO;	
	private BigDecimal notaAFavor=BigDecimal.ZERO;	
	private BigDecimal aplicacionesAnteriores=BigDecimal.ZERO;
	
	public MovimientoDeCuenta(){}
	
	public MovimientoDeCuenta(Cargo c){		
		this.nombre=MessageFormat.format("{0} ({1})", c.getNombre(),c.getClave());
		this.tipo=c.getTipoDocto();
		this.sucursal=c.getSucursal().getNombre();
		this.documento=c.getDocumento();
		this.moneda=c.getMoneda();
		this.total=c.getTotal();
		this.fecha=c.getFecha();
		this.revision=c.getFechaRevisionCxc();
		this.vencimiento=c.getVencimiento();
		
	}
	
	public MovimientoDeCuenta(Abono c){		
		this.nombre=MessageFormat.format("{0} ({1})", c.getNombre(),c.getClave());
		this.tipo=c.getTipo();
		this.sucursal=c.getSucursal().getNombre();
		if(c instanceof NotaDeCredito)
			this.documento=(long)((NotaDeCredito)c).getFolio();
		this.moneda=c.getMoneda();
		this.total=c.getTotal();
		this.fecha=c.getFecha();
		
		this.referencia=c.getInfo();
	}
	
	public MovimientoDeCuenta(Aplicacion c){		
		this.nombre=MessageFormat.format("{0} ({1})", c.getAbono().getNombre(),c.getAbono().getClave());
		this.tipo=c.getTipo();
		this.sucursal=c.getCargo().getSucursal().getNombre();
		
		this.documento=c.getCargo().getDocumento();
		this.moneda=c.getAbono().getMoneda();
		this.total=c.getImporte();
		this.fecha=c.getFecha();
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public Long getDocumento() {
		return documento;
	}
	public void setDocumento(Long documento) {
		this.documento = documento;
	}
	public String getSucursal() {
		return sucursal;
	}
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	public Currency getMoneda() {
		return moneda;
	}

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	

	public String getReferencia() {
		return referencia;
	}

	public Date getRevision() {
		return revision;
	}

	public void setRevision(Date revision) {
		this.revision = revision;
	}

	public Date getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}
	

	
	public double getDescuento() {
		return descuento;
	}
	
	public boolean isAplicable() {
		return aplicable;
	}
	
	

	public BigDecimal getSaldoCargo() {
		return saldoCargo;
	}

	public BigDecimal getSaldoAcumulado() {
		return saldoAcumulado;
	}

	public BigDecimal getSaldoAFavor() {
		return saldoAFavor;
	}

	public BigDecimal getNotaAFavor() {
		return notaAFavor;
	}

	public BigDecimal getAplicacionesAnteriores() {
		return aplicacionesAnteriores;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((documento == null) ? 0 : documento.hashCode());
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
		result = prime * result
				+ ((sucursal == null) ? 0 : sucursal.hashCode());
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
		result = prime * result + ((total == null) ? 0 : total.hashCode());
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
		MovimientoDeCuenta other = (MovimientoDeCuenta) obj;
		if (documento == null) {
			if (other.documento != null)
				return false;
		} else if (!documento.equals(other.documento))
			return false;
		if (fecha == null) {
			if (other.fecha != null)
				return false;
		} else if (!fecha.equals(other.fecha))
			return false;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		if (sucursal == null) {
			if (other.sucursal != null)
				return false;
		} else if (!sucursal.equals(other.sucursal))
			return false;
		if (tipo == null) {
			if (other.tipo != null)
				return false;
		} else if (!tipo.equals(other.tipo))
			return false;
		if (total == null) {
			if (other.total != null)
				return false;
		} else if (!total.equals(other.total))
			return false;
		return true;
	}
	
	
	 

}
