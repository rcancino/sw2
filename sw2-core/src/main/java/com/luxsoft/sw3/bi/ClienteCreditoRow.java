package com.luxsoft.sw3.bi;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.model.core.Cliente;

public class ClienteCreditoRow {
	
	private Long cliente_id;
	private String clave;
	private String nombre;
	private String rfc;
	private BigDecimal linea;
	private int plazo;
	private BigDecimal saldo;
	private int atrasoMaximo;
	private Boolean permitirCheque;
	private Boolean postfechado;
	private Boolean suspendido;
	private Date modificado;
	private String usuario;
	private Boolean checkplus;
	private Boolean vencimientoFechaFactura;
	
	public ClienteCreditoRow(){
		
	}
	
	public ClienteCreditoRow(Cliente c){
		setNombre(c.getNombre());
		setClave(c.getClave());
		setRfc(c.getRfc());
		setCliente_id(c.getId());
		setModificado(c.getLog().getModificado());
		setPlazo(c.getPlazo());
		setPermitirCheque(c.isPermitirCheque());
		if(c.getCredito()!=null){
			setPostfechado(c.getCredito().isChequePostfechado());
			setSaldo(c.getCredito().getSaldo());
			setSuspendido(c.getCredito().isSuspendido());
			setLinea(c.getCredito().getLinea().amount());
			
		}
		setUsuario(c.getLog().getUpdateUser());
		setVencimientoFechaFactura(c.getCredito().isVencimientoFactura());
	}
	
	public Long getCliente_id() {
		return cliente_id;
	}
	public void setCliente_id(Long cliente_id) {
		this.cliente_id = cliente_id;
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
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}
	public BigDecimal getLinea() {
		return linea;
	}
	public void setLinea(BigDecimal linea) {
		this.linea = linea;
	}
	public int getPlazo() {
		return plazo;
	}
	public void setPlazo(int plazo) {
		this.plazo = plazo;
	}
	public BigDecimal getSaldo() {
		return saldo;
	}
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
	
	public int getAtrasoMaximo() {
		return atrasoMaximo;
	}
	public void setAtrasoMaximo(int atrasoMaximo) {
		this.atrasoMaximo = atrasoMaximo;
	}
	
	public Boolean getPostfechado() {
		return postfechado;
	}
	public void setPostfechado(Boolean postfechado) {
		this.postfechado = postfechado;
	}
	
	public Boolean getPermitirCheque() {
		return permitirCheque;
	}
	public void setPermitirCheque(Boolean permitirCheque) {
		this.permitirCheque = permitirCheque;
	}
	public Boolean getSuspendido() {
		return suspendido;
	}
	public void setSuspendido(Boolean suspendido) {
		this.suspendido = suspendido;
	}
	public Date getModificado() {
		return modificado;
	}
	public void setModificado(Date modificado) {
		this.modificado = modificado;
	}
	
	
	public Boolean getCheckplus() {
		return checkplus;
	}

	public void setCheckplus(Boolean checkplus) {
		this.checkplus = checkplus;
	}

	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	
	
	public Boolean getVencimientoFechaFactura() {
		return vencimientoFechaFactura;
	}

	public void setVencimientoFechaFactura(Boolean vencimientoFechaFactura) {
		this.vencimientoFechaFactura = vencimientoFechaFactura;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cliente_id == null) ? 0 : cliente_id.hashCode());
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
		ClienteCreditoRow other = (ClienteCreditoRow) obj;
		if (cliente_id == null) {
			if (other.cliente_id != null)
				return false;
		} else if (!cliente_id.equals(other.cliente_id))
			return false;
		return true;
	}

	


}
