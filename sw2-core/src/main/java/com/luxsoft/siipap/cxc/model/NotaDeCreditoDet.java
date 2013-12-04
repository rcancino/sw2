package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

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
import org.hibernate.annotations.Type;
import org.hibernate.validator.AssertTrue;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.ventas.model.Venta;


/**
 * Detalle de una nota de credito
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_NOTA_DET")
public class NotaDeCreditoDet extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="NOTADET_ID")
	private Long id;	
	
    @ManyToOne
	@JoinColumn (name="ABONO_ID",nullable=false,updatable=false)
	private NotaDeCredito nota;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "VENTA_ID", nullable = false)
	private Venta venta;
	
	@Column(name="IMPORTE",nullable=false,scale=6,precision=16)
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Column(name="COMENTARIO",nullable=true)
	private String comentario;
	
    
    @Column(name="DESCUENTO",nullable=true)
	private double descuento=0;
    
    @Column(name="SUCURSAL",length=20)
    private String sucursal;
    
    @Column(name="ORIGEN",length=20)
    private String origen;
    
    @Column(name="DOCUMENTO")
    private Long documento;
    
    @Column(name="FECHA_DOCTO")
    @Type(type="date")
    private Date fechaDocumento;
    
    
	public Long getId() {
		return id;
	}
	
	public NotaDeCredito getNota() {
		return nota;
	}
	public void setNota(NotaDeCredito nota) {
		this.nota = nota;
	}

	public Venta getVenta() {
		return venta;
	}
	public void setVenta(Venta venta) {
		Object old=this.venta;
		this.venta = venta;
		firePropertyChange("venta", old, venta);
		setOrigen(venta!=null?venta.getOrigen().name():null);
		setSucursal(venta!=null?venta.getOrigen().name():null);
		setDocumento(venta!=null?venta.getDocumento():null);
		setFechaDocumento(venta!=null?venta.getFecha():null);
		
	}

	public double getDescuento() {
		return descuento;
	}
	public void setDescuento(double descuento) {
		double old=this.descuento;
		this.descuento = descuento;
		firePropertyChange("descuento", old, descuento);
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

	/**
	 * Calcula el descuento a partir de un importe asignado a esta partida
	 * 
	 * @return
	 */
	public double getDescuentoCalculado(){		
		//BigDecimal total=venta.getImporte();
		//BigDecimal devoluciones=venta.getDevoluciones();
		//BigDecimal valor=total.subtract(devoluciones);
		BigDecimal valor=getVenta().getVentaNeta();
		double res;
		if(valor.doubleValue()>0)
			res=getImporte().doubleValue()/valor.doubleValue();
		else
			res=0;		
		return res;
	}
	
	/**
	 * Actualiza el importe de la partida a partir de un descuento asignado
	 * 
	 */
	public void actualizarImporte(){
		CantidadMonetaria impCalculado=CantidadMonetaria.pesos(0);
		if(validarDescuento()){
			if(venta!=null){
				//imp=getVenta().getTotalCM();
				BigDecimal subTot=getVenta().getImporte().subtract(getVenta().getDevoluciones());
				impCalculado=new CantidadMonetaria(subTot,getVenta().getMoneda());
				impCalculado=impCalculado.multiply(getDescuento()/100);
			}
			setImporte(impCalculado.amount());
		}
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
		if(o==this) return true;
		if(getClass()!=o.getClass()) return false;
		NotaDeCreditoDet other=(NotaDeCreditoDet)o;
		return new EqualsBuilder()
		.append(getNota(), other.getNota())
		.append(venta, other.getVenta())
		.append(importe, other.getImporte())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,25)
		.append(getNota())
		.append(venta)
		.append(importe)
		.toHashCode();
	}

	@Override
	public String toString() {		
		return MessageFormat.format("{0} {1} {2}", comentario,getDescuento(),importe);
	}
	
	@AssertTrue(message="El descuento es incorrecto")
	public boolean validarDescuento(){
		return getDescuento()>=0 && getDescuento()<100;
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

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
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

	public Date getFechaDocumento() {
		return fechaDocumento;
	}

	public void setFechaDocumento(Date fechaDocumento) {
		this.fechaDocumento = fechaDocumento;
	}
	
}
