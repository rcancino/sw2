package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.AssertTrue;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Detalle de una nota de notaDeCargo
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_NOTADECARGO_DET")
public class NotaDeCargoDet extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="CARGODET_ID")
	private Long id;
	
	
    @ManyToOne
	@JoinColumn (name="CARGO_ID",nullable=false,updatable=false)
	private NotaDeCargo notaDeCargo;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "VENTA_ID", nullable = false)
	private Cargo venta;
	
	@Column(name="IMPORTE",nullable=false)
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Column(name="COMENTARIO",nullable=true)
	private String comentario;
	
	/**
     * Id para ventas importadas del sistema anterior. Nulo para ventas
     * generadas en el nuevo sistema
     * 
     */
    @Column(name="SIIPAPWIN_ID",nullable=true)
    private Long siipapWinId;
    
    @Column(name="CARGO",nullable=true)
	private double cargo=0;
    
    
	public Long getId() {
		return id;
	}	

	public NotaDeCargo getNotaDeCargo() {
		return notaDeCargo;
	}

	public void setNotaDeCargo(NotaDeCargo cargo) {
		this.notaDeCargo = cargo;
	}

	public Cargo getVenta() {
		return venta;
	}

	public void setVenta(Cargo venta) {
		this.venta = venta;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public double getCargo() {
		return cargo;
	}

	public void setCargo(double cargo) {
		double old=this.cargo;
		this.cargo = cargo;
		firePropertyChange("cargo", old, cargo);
	}

	/**
	 * Calcula en forma dinamica el  % del notaDeCargo
	 * real con respecto a la venta
	 * 
	 * @return
	 */
	public double getCargoAplicado(){		
		BigDecimal total=venta.getImporte();
		BigDecimal devoluciones=venta.getDevoluciones();
		BigDecimal valor=total.subtract(devoluciones);
		double res;
		if(valor.doubleValue()>0)
			res=getImporte().doubleValue()/valor.doubleValue();
		else
			res=0;		
		return res;
	}
	
	public void actualizarImporte(){
		CantidadMonetaria imp=new CantidadMonetaria(0d,getNotaDeCargo().getMoneda());
		if(validarCargo()){
			if(venta!=null){
				//imp=getVenta().getTotalCM();
				BigDecimal subTot=getVenta().getImporte().subtract(getVenta().getDevoluciones());
				imp=new CantidadMonetaria(subTot,getVenta().getMoneda());
				imp=imp.multiply(cargo/100);
			}
			setImporte(imp.amount());
		}
	}
	
	public Long getSiipapWinId() {
		return siipapWinId;
	}

	public void setSiipapWinId(Long siipapWinId) {
		this.siipapWinId = siipapWinId;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
		if(o==this) return true;
		if(getClass()!=o.getClass()) return false;
		NotaDeCargoDet other=(NotaDeCargoDet)o;
		return new EqualsBuilder()
		.append(venta, other.getVenta())
		.append(importe, other.getImporte())
		.append(comentario,other.getComentario())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,25)
		.append(venta)
		.append(importe)
		.append(comentario)
		.toHashCode();
	}

	@Override
	public String toString() {		
		return MessageFormat.format("{0} {1}", comentario,importe);
	}
	
	@AssertTrue(message="El cargo es incorrecto")
	public boolean validarCargo(){
		return cargo>=0 && cargo<100;
	}
	
	@Transient
	private BigDecimal saldo=BigDecimal.ZERO;


	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		Object old=this.saldo;
		this.saldo = saldo;
		firePropertyChange("saldo", old,saldo);
	}
	
	

}
