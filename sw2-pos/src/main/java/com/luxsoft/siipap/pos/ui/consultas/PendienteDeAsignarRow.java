package com.luxsoft.siipap.pos.ui.consultas;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.time.DurationFormatUtils;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.util.SQLUtils;

public class PendienteDeAsignarRow {
	

private String sucursal;
private String nombre;
private String origen;
private Boolean contraEntrega;
private String formaPago;
private Date fecha;
private Date pedidoCreado;
private Double pedidoFolio;
private Double documento;
private Date facturaCreado;
private Date fechaEntrega;
private BigDecimal total;
private BigDecimal saldo;
private BigDecimal importe;
private BigDecimal entregado;
private BigDecimal pendiente;
private Date ultimoPago;
private String instruccionDeEntrega;
private String cargoId;
private BigDecimal devolucionAplicada;




public String getSucursal() {
	return sucursal;
}
public void setSucursal(String sucursal) {
	this.sucursal = sucursal;
}
public String getNombre() {
	return nombre;
}
public void setNombre(String nombre) {
	this.nombre = nombre;
}
public String getOrigen() {
	return origen;
}
public void setOrigen(String origen) {
	this.origen = origen;
}
public Boolean getContraEntrega() {
	return contraEntrega;
}
public void setContraEntrega(Boolean contraEntrega) {
	this.contraEntrega = contraEntrega;
}
public String getFormaPago() {
	return formaPago;
}
public void setFormaPago(String formaPago) {
	this.formaPago = formaPago;
}
public Date getFecha() {
	return fecha;
}
public void setFecha(Date fecha) {
	this.fecha = fecha;
}
public Date getPedidoCreado() {
	return pedidoCreado;
}
public void setPedidoCreado(Date pedidoCreado) {
	this.pedidoCreado = pedidoCreado;
}
public Double getPedidoFolio() {
	return pedidoFolio;
}
public void setPedidoFolio(Double pedidoFolio) {
	this.pedidoFolio = pedidoFolio;
}
public Double getDocumento() {
	return documento;
}
public void setDocumento(Double documento) {
	this.documento = documento;
}
public Date getFacturaCreado() {
	return facturaCreado;
}
public void setFacturaCreado(Date facturaCreado) {
	this.facturaCreado = facturaCreado;
}
public Date getFechaEntrega() {
	return fechaEntrega;
}
public void setFechaEntrega(Date fechaEntrega) {
	this.fechaEntrega = fechaEntrega;
}
public BigDecimal getTotal() {
	return total;
}
public void setTotal(BigDecimal total) {
	this.total = total;
}
public BigDecimal getSaldo() {
	return saldo;
}
public void setSaldo(BigDecimal saldo) {
	this.saldo = saldo;
}
public BigDecimal getImporte() {
	return importe;
}
public void setImporte(BigDecimal importe) {
	this.importe = importe;
}
public BigDecimal getEntregado() {
	return entregado;
}
public void setEntregado(BigDecimal entregado) {
	this.entregado = entregado;
}
public BigDecimal getPendiente() {
	return pendiente;
}
public void setPendiente(BigDecimal pendiente) {
	this.pendiente = pendiente;
}
public Date getUltimoPago() {
	return ultimoPago;
}
public void setUltimoPago(Date ultimoPago) {
	this.ultimoPago = ultimoPago;
}
public String getInstruccionDeEntrega() {
	return instruccionDeEntrega;
}
public void setInstruccionDeEntrega(String instruccionDeEntrega) {
	this.instruccionDeEntrega = instruccionDeEntrega;
}
public String getCargoId() {
	return cargoId;
}
public void setCargoId(String cargoId) {
	this.cargoId = cargoId;
}
public BigDecimal getDevolucionAplicada() {
	return devolucionAplicada;
}
public void setDevolucionAplicada(BigDecimal devolucionAplicada) {
	this.devolucionAplicada = devolucionAplicada;
}



}
