package com.luxsoft.sw3.ventas;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;

/**
 * JavaBean / Entidad que es la abstraccion basica de lo que es un pedido de 
 * venta
 * 
 * @author Ruben Cancino Ramos
 *
 */

public class PedidoRow {
	
	
	protected String id;
	private long folio;
	private Date fecha;	
	private String clave;
	private String nombre;
	private BigDecimal total = BigDecimal.ZERO;
	private BigDecimal totalFacturado=BigDecimal.ZERO;
	private boolean puesto=false;
	private String modificado;
	private String creado;
	private String formaDePago;
	private String entrega;
	private String comentario;    
	private String comentario2;    
	private String comentarioAutorizacion;
	private String tipo;	
	
	//private boolean contraEntrega;
	
	

	private String contraEntregaId;
	private boolean facturable;
	private String pendiente;
	private Long pendienteId;
	private boolean especial=false;
	//private Currency moneda;
	private String moneda;
	private String operador;
	private String pendienteDesc;
	private boolean deCredito;
	private String origen;
	
	
	public PedidoRow(){}
	
	public PedidoRow(Pedido p){
		setClave(p.getClave());
		setComentario(p.getComentario());
		setComentario2(p.getComentario2());
		setComentarioAutorizacion(p.getComentarioAutorizacion());
		setContraEntregaId(p.getPagoContraEntrega()!=null?p.getPagoContraEntrega().getId():null);
		setCreado(p.getLog().getCreateUser());
		setEntrega(p.getEntrega().name());
		setEspecial(p.isEspecial());
		setFacturable(p.isFacturable());
		setFecha(p.getFecha());
		setFolio(p.getFolio());
		setFormaDePago(p.getFormaDePago().name());
		setId(p.getId());
		setModificado(p.getLog().getUpdateUser());
		setNombre(p.getNombre());
		setPendiente(p.getPendiente()!=null?p.getPendienteDesc():null);
		setPendienteId(p.getPendiente()!=null?p.getPendiente().getId():null);
		setPuesto(p.isPuesto());
		setTipo(p.getOrigen());
		setTotal(p.getTotal());
		setTotalFacturado(p.getTotalFacturado());
		setMoneda(p.getMoneda().getCurrencyCode());
		setOperador(p.getOperador());
		setPendienteDesc(p.getPendienteDesc());
		setOrigen(p.getOrigen());
		//setContraEntrega(p.isContraEntrega());
		
	
	}
	
	/*public boolean isContraEntrega() {
		return contraEntrega;
	}

	public void setContraEntrega(boolean contraEntrega) {
		this.contraEntrega = contraEntrega;
	}
	
	public boolean getContraEntrega() {
		return contraEntrega;
	}
	*/

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getFolio() {
		return folio;
	}

	public void setFolio(long folio) {
		this.folio = folio;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
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

	public boolean isPuesto() {
		return puesto;
	}

	public void setPuesto(boolean puesto) {
		this.puesto = puesto;
	}

	public String getModificado() {
		return modificado;
	}

	public void setModificado(String modificado) {
		this.modificado = modificado;
	}

	public String getFormaDePago() {
		return formaDePago;
	}

	public void setFormaDePago(String formaDePago) {
		this.formaDePago = formaDePago;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getComentarioAutorizacion() {
		return comentarioAutorizacion;
	}

	public void setComentarioAutorizacion(String comentarioAutorizacion) {
		this.comentarioAutorizacion = comentarioAutorizacion;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public boolean isContraEntrega() {
		return getContraEntregaId()!=null;
	}

	

	public String getContraEntregaId() {
		return contraEntregaId;
	}

	public void setContraEntregaId(String contraEntregaId) {
		this.contraEntregaId = contraEntregaId;
	}

	public boolean isFacturable() {
		return facturable;
	}

	public void setFacturable(boolean facturable) {
		this.facturable = facturable;
	}
	

	public BigDecimal getTotalFacturado() {
		return totalFacturado;
	}

	public void setTotalFacturado(BigDecimal totalFacturado) {
		this.totalFacturado = totalFacturado;
	}

	
	public String getPendiente() {
		return pendiente;
	}

	public void setPendiente(String pendiente) {
		this.pendiente = pendiente;
	}

	public String getEstado(){
		if(getTotalFacturado().doubleValue()>0)
			return "FACTURADO";		
		if(isFacturable())
			return "FACTURABLE";
		if(getPendienteId()!=null && getPendienteId()>0)
			return "POR AUTORIZAR";
		if(StringUtils.containsIgnoreCase(comentario2, "CANCELADO"))
			return "CANCELADO";
		else
			return "PENDIENTE";
		
	}

	public String getCreado() {
		return creado;
	}

	public void setCreado(String creado) {
		this.creado = creado;
	}

	public String getComentario2() {
		return comentario2;
	}

	public void setComentario2(String comentario2) {
		this.comentario2 = comentario2;
	}
	
	public boolean isEspecial() {
		return especial;
	}

	public void setEspecial(boolean especial) {
		this.especial = especial;
	}
	

	public String getEntrega() {
		return entrega;
	}

	public void setEntrega(String entrega) {
		this.entrega = entrega;
	}
	

	public Long getPendienteId() {
		return pendienteId;
	}

	public void setPendienteId(Long pendienteId) {
		this.pendienteId = pendienteId;
	}
	

	



	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
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
		PedidoRow other = (PedidoRow) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getOperador() {
		return operador;
	}

	public void setOperador(String operador) {
		this.operador = operador;
	}

	public String getPendienteDesc() {
		return pendienteDesc;
	}

	public void setPendienteDesc(String pendienteDesc) {
		this.pendienteDesc = pendienteDesc;
	}

	public boolean isDeCredito() {
		return deCredito;
	}

	public void setDeCredito(boolean deCredito) {
		this.deCredito = deCredito;
	}
	public String getOrigen() {
		return origen;
	}
	public void setOrigen(String origen) {
		this.origen = origen;
	}
	
	
	
}
