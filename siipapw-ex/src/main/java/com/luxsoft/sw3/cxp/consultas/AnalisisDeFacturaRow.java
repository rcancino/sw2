package com.luxsoft.sw3.cxp.consultas;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;

public class AnalisisDeFacturaRow {
	
	private Long id;
	private Date fecha;	
	private String nombre;
	private String factura;	
	private Date fechaFactura;
	private String moneda;
	private double tc;
	private BigDecimal totalFactura;
	private BigDecimal totalAnalisis;
	private BigDecimal bonificado;
	private BigDecimal importeDescuentoFinanciero;
	private BigDecimal pagos;
	private BigDecimal saldo;
	private BigDecimal porRequisitar;
	private BigDecimal requisitado;
	private String comentario;
	
	public AnalisisDeFacturaRow() {
		// TODO Auto-generated constructor stub
	}
	
	public AnalisisDeFacturaRow(AnalisisDeFactura a) {
		setId(a.getId());
		setBonificado(a.getFactura().getBonificado());
		setComentario(a.getComentario());
		setFactura(a.getFactura().getDocumento());
		setFecha(a.getFecha());
		setFechaFactura(a.getFactura().getFecha());
		setImporteDescuentoFinanciero(a.getFactura().getImporteDescuentoFinanciero2().amount());
		setMoneda(a.getFactura().getMoneda().getCurrencyCode());
		setNombre(a.getFactura().getNombre());
		setPagos(a.getFactura().getPagos());
		setPorRequisitar(a.getFactura().getPorRequisitar().amount());
		setRequisitado(a.getFactura().getRequisitado());
		setSaldo(a.getFactura().getSaldoCalculado());
		setTc(a.getFactura().getTc());
		setTotalAnalisis(a.getTotal());
		setTotalFactura(a.getFactura().getTotal());
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getFactura() {
		return factura;
	}
	public void setFactura(String factura) {
		this.factura = factura;
	}
	public Date getFechaFactura() {
		return fechaFactura;
	}
	public void setFechaFactura(Date fechaFactura) {
		this.fechaFactura = fechaFactura;
	}
	public String getMoneda() {
		return moneda;
	}
	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}
	public double getTc() {
		return tc;
	}
	public void setTc(double tc) {
		this.tc = tc;
	}
	public BigDecimal getTotalFactura() {
		return totalFactura;
	}
	public void setTotalFactura(BigDecimal totalFactura) {
		this.totalFactura = totalFactura;
	}
	public BigDecimal getTotalAnalisis() {
		return totalAnalisis;
	}
	public void setTotalAnalisis(BigDecimal totalAnalisis) {
		this.totalAnalisis = totalAnalisis;
	}
	public BigDecimal getBonificado() {
		return bonificado;
	}
	public void setBonificado(BigDecimal bonificado) {
		this.bonificado = bonificado;
	}
	public BigDecimal getImporteDescuentoFinanciero() {
		return importeDescuentoFinanciero;
	}
	public void setImporteDescuentoFinanciero(BigDecimal importeDescuentoFinanciero) {
		this.importeDescuentoFinanciero = importeDescuentoFinanciero;
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
	public BigDecimal getPorRequisitar() {
		return porRequisitar;
	}
	public void setPorRequisitar(BigDecimal porRequisitar) {
		this.porRequisitar = porRequisitar;
	}
	public BigDecimal getRequisitado() {
		return requisitado;
	}
	public void setRequisitado(BigDecimal requisitado) {
		this.requisitado = requisitado;
	}
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		AnalisisDeFacturaRow other = (AnalisisDeFacturaRow) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	

}
