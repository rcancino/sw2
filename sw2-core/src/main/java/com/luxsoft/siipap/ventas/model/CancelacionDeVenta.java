package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Entidad que funciona como bitacora de para las ventas canceladas
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_VENTAS_CANCELADAS")
public class CancelacionDeVenta {
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@OneToOne(optional=false)
    @JoinColumn(name="VENTA_ID")
	private Venta venta;
	
	@Column(name="MONEDA",nullable=false)
	private Currency moneda;
        
    @Column(name="IMPORTE",nullable=false)
    private BigDecimal importe;
    
    @Column(name="IMPUESTO",nullable=false)  
    private BigDecimal impuesto;
    
    @Column(name="TOTAL",nullable=false)  
    private BigDecimal total;
	
	@Column(name="USUARIO",nullable=false)
	private String usuario;
	
	@Column(name="FECHA",nullable=false)
	private Date fecha;
	
	@Column(name="COMENTARIO",nullable=false)
	private String comentario;
	

	public Long getId() {
		return id;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Venta getVenta() {
		return venta;
	}

	public void setVenta(Venta venta) {
		this.venta = venta;
	}

	public Currency getMoneda() {
		return moneda;
	}

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(BigDecimal impuesto) {
		this.impuesto = impuesto;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
	

}
