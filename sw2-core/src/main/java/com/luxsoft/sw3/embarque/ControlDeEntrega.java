package com.luxsoft.sw3.embarque;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ControlDeEntrega {

	private String id;
	
	private String sucursal;
	private String cliente;
	private Long documento;
	private String tipo;
	private Date fechaFactura;
	private Date fechaPedido;
	
	private Date surtido;
	private Date enviado;	
	
	private Date arribo;
	private Date recepcion;
	
	private String incidente;
	
	
	private String comentario;
	
	

	public ControlDeEntrega(Entrega e) {
		this.id=e.getId();
		this.sucursal=e.getEmbarque().getSucursal();
		this.cliente=e.getNombre();
		this.fechaFactura=e.getFechaFactura();
		this.fechaPedido=e.getFechaFactura();
		this.surtido=e.getSurtido();
		this.enviado=e.getEmbarque().getSalida();
		this.arribo=e.getArribo();
		this.recepcion=e.getRecepcion();
		//this.incidente=e.getEmbarque().getIncidentes().toString();
		this.comentario=e.getComentario();
		this.documento=e.getFactura().getDocumento();
		this.tipo=e.getFactura().getOrigen().getShortName();
	}

	

	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
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

	public Date getFechaFactura() {
		return fechaFactura;
	}

	public void setFechaFactura(Date fechaFactura) {
		this.fechaFactura = fechaFactura;
	}

	public Date getFechaPedido() {
		return fechaPedido;
	}

	public void setFechaPedido(Date fechaPedido) {
		this.fechaPedido = fechaPedido;
	}
	
	
	public Date getSurtido() {
		return surtido;
	}

	public void setSurtido(Date surtido) {
		this.surtido = surtido;
	}
	
	public String getSurtidoHora(){
		return toHora(surtido);
	}	
	
	public Date getEnviado() {
		return enviado;
	}

	public void setEnviado(Date enviado) {
		this.enviado = enviado;
	}
	
	public String getEnviadoHora(){
		return toHora(enviado);
	}	

	public Date getArribo() {
		return arribo;
	}

	public void setArribo(Date arribo) {
		this.arribo = arribo;
	}
	
	public String getArriboHora(){
		return toHora(arribo);
	}

	public Date getRecepcion() {
		return recepcion;
	}

	public void setRecepcion(Date recepcion) {
		this.recepcion = recepcion;
	}
	
	public String getRecepcionHora(){
		return toHora(recepcion);
	}
	
	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	public String getIncidente() {
		return incidente;
	}

	public void setIncidente(String incidente) {
		this.incidente = incidente;
	}
	
	
	
	public Long getDocumento() {
		return documento;
	}



	public void setDocumento(Long documento) {
		this.documento = documento;
	}



	public String getTipo() {
		return tipo;
	}



	public void setTipo(String tipo) {
		this.tipo = tipo;
	}



	private DateFormat horaFormat=new SimpleDateFormat("hh:mm");
	
	private String toHora(Date date){
		if(date!=null)
			return horaFormat.format(date);
		return "";
	}
	
	public String getTiempoDeEntregaEnvio(){
		if(getRecepcion()!=null && getEnviado()!=null){
			long dif=getRecepcion().getTime()-getEnviado().getTime();
			Date td=new Date(dif);
			return toHora(td);
		}else
			return toHora(null);
		
	}
	
	public String getTiempoDeEntregaPedido(){
		if((getRecepcion()!=null) && (getFechaPedido()!=null)){
			long dif=getRecepcion().getTime()-getFechaPedido().getTime();
			Date td=new Date(dif);
			return toHora(td);
		}else
			return toHora(null);
		
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
		ControlDeEntrega other = (ControlDeEntrega) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public String toString(){
		return id.toString(); 
	}

}
