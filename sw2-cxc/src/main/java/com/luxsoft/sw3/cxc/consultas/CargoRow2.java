package com.luxsoft.sw3.cxc.consultas;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * Clase para representar de manera facil un Cargo
 * 
 * @author Ruben Cancino
 *
 */
public class CargoRow2 {
	
	private String id;
	
	private String tipo;
	
	private String origen;
	
	private Long documento;
	
	private Integer numeroFiscal;
	
	private boolean postFechado;
	
	private Date fecha;
	
	private Date vencimiento;
	
	private Date reprogramarPago;
	
	private Long atraso;
	
	private int sucursal;
	
	private String sucursalNombre;
	
	private String clave;
	
	private String nombre;
	
	private BigDecimal total;
	
	private BigDecimal devoluciones;
	
	private BigDecimal bonificaciones;
	
	private BigDecimal descuentos;
	
	private BigDecimal pagos;
	
	private BigDecimal saldo;
	
	private BigDecimal cargoAplicado;
	
	private BigDecimal cargo;
	
	private BigDecimal importeCargo;
	
	private Date impreso;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
	}

	public Integer getNumeroFiscal() {
		return numeroFiscal;
	}

	public void setNumeroFiscal(Integer numeroFiscal) {
		this.numeroFiscal = numeroFiscal;
	}

	public boolean isPostFechado() {
		return postFechado;
	}

	public void setPostFechado(boolean postFechado) {
		this.postFechado = postFechado;
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

	public Date getReprogramarPago() {
		return reprogramarPago;
	}

	public void setReprogramarPago(Date reprogramarPago) {
		this.reprogramarPago = reprogramarPago;
	}

	public Long getAtraso() {
		return atraso;
	}

	public void setAtraso(Long atraso) {
		this.atraso = atraso;
	}

	public int getSucursal() {
		return sucursal;
	}

	public void setSucursal(int sucursal) {
		this.sucursal = sucursal;
	}

	public String getSucursalNombre() {
		return sucursalNombre;
	}

	public void setSucursalNombre(String sucursalNombre) {
		this.sucursalNombre = sucursalNombre;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getDevoluciones() {
		return devoluciones;
	}

	public void setDevoluciones(BigDecimal devoluciones) {
		this.devoluciones = devoluciones;
	}

	public BigDecimal getBonificaciones() {
		return bonificaciones;
	}

	public void setBonificaciones(BigDecimal bonificaciones) {
		this.bonificaciones = bonificaciones;
	}

	public BigDecimal getDescuentos() {
		return descuentos;
	}

	public void setDescuentos(BigDecimal descuentos) {
		this.descuentos = descuentos;
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
	
	public CantidadMonetaria getSaldoMN(){
		return CantidadMonetaria.pesos(getSaldo()); 
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public BigDecimal getCargoAplicado() {
		return cargoAplicado;
	}

	public void setCargoAplicado(BigDecimal cargoAplicado) {
		this.cargoAplicado = cargoAplicado;
	}

	public BigDecimal getCargo() {
		return cargo;
	}

	public void setCargo(BigDecimal cargo) {
		this.cargo = cargo;
	}

	public BigDecimal getImporteCargo() {
		return importeCargo;
	}

	public void setImporteCargo(BigDecimal importeCargo) {
		this.importeCargo = importeCargo;
	}
	
	

	public Date getImpreso() {
		return impreso;
	}

	public void setImpreso(Date impreso) {
		this.impreso = impreso;
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
		CargoRow2 other = (CargoRow2) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	

	private Long cobradorId;
	
	private boolean revisada;
	
	private boolean revision;
	
	private Date fechaRecepcionCXC;
	
	private Date fechaRevisionCxc;
	
	private String comentarioRepPago;
	
	private String comentario2;

	public Long getCobradorId() {
		return cobradorId;
	}

	public void setCobradorId(Long cobradorId) {
		this.cobradorId = cobradorId;
	}

	public boolean isRevisada() {
		return revisada;
	}

	public void setRevisada(boolean revisada) {
		this.revisada = revisada;
	}

	public boolean isRevision() {
		return revision;
	}

	public void setRevision(boolean revision) {
		this.revision = revision;
	}

	public Date getFechaRecepcionCXC() {
		return fechaRecepcionCXC;
	}

	public void setFechaRecepcionCXC(Date fechaRecepcionCXC) {
		this.fechaRecepcionCXC = fechaRecepcionCXC;
	}

	public boolean isRecibidaCXC() {
		return fechaRecepcionCXC!=null;
	}

	public Date getFechaRevisionCxc() {
		return fechaRevisionCxc;
	}

	public void setFechaRevisionCxc(Date fechaRevisionCxc) {
		this.fechaRevisionCxc = fechaRevisionCxc;
	}

	public String getComentarioRepPago() {
		return comentarioRepPago;
	}

	public void setComentarioRepPago(String comentarioRepPago) {
		this.comentarioRepPago = comentarioRepPago;
	}

	public String getComentario2() {
		return comentario2;
	}

	public void setComentario2(String comentario2) {
		this.comentario2 = comentario2;
	}

	

}
