package com.luxsoft.siipap.pos.ui.selectores;

import java.math.BigDecimal;
import java.util.Date;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

public class DepositosRow {
	

	
	private String sol_id;
	private String nombre;
	private String tipo;
	private Long documento;
	private Date fecha;
	private Date fechaDeposito;
	private BigDecimal total;
    private String solicita;
    private String comentario;
    private Boolean salvoBuenCobro;
    private String pagoInfo;
    private Date liberado;
    
    
    public DepositosRow(SolicitudDeDeposito sol){
    	setSol_id(sol.getId());
    	setNombre(sol.getNombre());
    	setTipo(sol.getTipo());
    	setDocumento(sol.getDocumento());
    	setFecha(sol.getFecha());
    	setFechaDeposito(sol.getFechaDeposito());
    	setTotal(sol.getTotal());
    	setSolicita(sol.getSolicita());
    	setComentario(sol.getComentario());
    	setSalvoBuenCobro(sol.getSalvoBuenCobro());
    	setPagoInfo(sol.getPagoInfo());
    	setLiberado(sol.getPago().getFecha());
    }
    
    public DepositosRow(){}
    
	public String getSol_id() {
		return sol_id;
	}
	public void setSol_id(String sol_id) {
		this.sol_id = sol_id;
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
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public Date getFechaDeposito() {
		return fechaDeposito;
	}
	public void setFechaDeposito(Date fechaDeposito) {
		this.fechaDeposito = fechaDeposito;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public String getSolicita() {
		return solicita;
	}
	public void setSolicita(String solicita) {
		this.solicita = solicita;
	}
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	public Boolean getSalvoBuenCobro() {
		return salvoBuenCobro;
	}
	public void setSalvoBuenCobro(Boolean salvoBuenCobro) {
		this.salvoBuenCobro = salvoBuenCobro;
	}
	public String getPagoInfo() {
		return pagoInfo;
	}
	public void setPagoInfo(String pagoInfo) {
		this.pagoInfo = pagoInfo;
	}
	public Date getLiberado() {
		return liberado;
	}
	public void setLiberado(Date liberado) {
		this.liberado = liberado;
	}
	
	
	
	
	
	

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sol_id == null) ? 0 : sol_id.hashCode());
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
		DepositosRow other = (DepositosRow) obj;
		if (sol_id == null) {
			if (other.sol_id != null)
				return false;
		} else if (!sol_id.equals(other.sol_id))
			return false;
		return true;
	}

	

	
	

}
