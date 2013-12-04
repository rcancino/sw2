package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.text.MessageFormat;
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
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.BaseBean;

/**
 * Entidad para registrar las cuentas por cobrar
 * que se procesan en el area juridica de la empresa
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_JURIDICO")
public class Juridico extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="JURIDICO_ID")
	private Long id;
	
	@Version @Column(name="VERSION")
	private int version;
	
	/*@ManyToOne(optional = false, cascade = { CascadeType.MERGE,
			CascadeType.PERSIST })			
	@JoinColumn(name = "CARGO_ID", nullable = false)*/
	
	@OneToOne(optional=false)
	@JoinColumn(name="CARGO_ID", unique=true, nullable=false, updatable=false)
	private Cargo cargo;
	
	@Column(name="ABOGADO")
	private String abogado;
	
	@Column(name="TRASPASO")
	@Type(type="date")
	private Date traspaso=new Date();
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@Column(name="SALDO_DOCTO")
	private BigDecimal saldoDocumento=BigDecimal.ZERO;
	
	@Column(name = "NOMBRE", nullable = false)
	private String nombre;
	
	@Column(name = "CLAVE", length = 7, nullable=false)
	private String clave;
	
	@Column(name = "FECHA_DOCTO", nullable = false)
	@Type(type = "date")
	private Date fechaDocto = new Date();
	
	@Column(name="VTO",nullable=false)
    @Type(type="date")
    private Date vencimiento;
	
	@Column(name = "MONEDA", nullable = false)
	private Currency moneda ;
	
	@Column(name = "TC", nullable = false)
	private double tc ;
	
	@Column(name = "DOCTO",nullable=false)
	private Long documento;
	
	@Column(name = "FISCAL",nullable=false)
	private Long fiscal;
	
	@Column(name = "IMPORTE", nullable = false)
	private BigDecimal importe ;
	
	@Column(name = "IMPUESTO", nullable = false)
	private BigDecimal impuesto;
	
	@Column(name = "TOTAL", nullable = false)
	private BigDecimal total ;
	
	@Column(name = "ATRASO", nullable = false)
	private int atraso;
		
	@Column(name = "ENTREGADO")
	@Type(type="date")
	private Date entregado;
	 

	public Long getId() {
		return id;
	}
	
	
	public int getVersion() {
		return version;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		Object old=this.cargo;
		this.cargo = cargo;
		firePropertyChange("cargo", old, cargo);
		if(cargo!=null){
			setSaldoDocumento(cargo.getSaldoCalculado());
			cargo.setJuridico(this);
			setMoneda(cargo.getMoneda());
			setImporte(cargo.getImporte());
			setImpuesto(cargo.getImpuesto());
			setTotal(cargo.getTotal());
			setClave(cargo.getClave());
			setNombre(cargo.getNombre());
			setDocumento(cargo.getDocumento());
			setFiscal(cargo.getNumeroFiscal().longValue());
			setAtraso(cargo.getAtraso());
			setFechaDocto(cargo.getFecha());
			setVencimiento(cargo.getVencimiento());
			
		}
	}


	public String getAbogado() {
		return abogado;
	}


	public void setAbogado(String abogado) {
		Object old=this.abogado;
		this.abogado = abogado;
		firePropertyChange("abogado", old, abogado);
	}


	public Date getTraspaso() {
		return traspaso;
	}


	public void setTraspaso(Date traspaso) {
		Object old=this.traspaso;
		this.traspaso = traspaso;
		firePropertyChange("traspaso", old, traspaso);
	}


	public String getComentario() {
		return comentario;
	}


	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}


	public BigDecimal getSaldoDocumento() {
		return saldoDocumento;
	}

	public void setSaldoDocumento(BigDecimal saldoDocumento) {
		Object old=this.saldoDocumento;
		this.saldoDocumento = saldoDocumento;
		firePropertyChange("saldoDocumento", old, saldoDocumento);
	}
	
	


	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public String getClave() {
		return clave;
	}


	public void setClave(String clave) {
		this.clave = clave;
	}


	public Date getFechaDocto() {
		return fechaDocto;
	}


	public void setFechaDocto(Date fechaDocto) {
		this.fechaDocto = fechaDocto;
	}


	public Date getVencimiento() {
		return vencimiento;
	}


	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}


	public Currency getMoneda() {
		return moneda;
	}


	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}


	public double getTc() {
		return tc;
	}


	public void setTc(double tc) {
		this.tc = tc;
	}


	public Long getDocumento() {
		return documento;
	}


	public void setDocumento(Long documento) {
		this.documento = documento;
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


	public int getAtraso() {
		return atraso;
	}


	public void setAtraso(int atraso) {
		this.atraso = atraso;
	}

	
	

	public Long getFiscal() {
		return fiscal;
	}


	public void setFiscal(Long fiscal) {
		this.fiscal = fiscal;
	}

	

	public Date getEntregado() {
		return entregado;
	}


	public void setEntregado(Date entregado) {
		this.entregado = entregado;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cargo == null) ? 0 : cargo.hashCode());
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
		Juridico other = (Juridico) obj;
		if (cargo == null) {
			if (other.cargo != null)
				return false;
		} else if (!cargo.equals(other.cargo))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return MessageFormat.format("{0} {1} {2} {3}"
				, cargo.getDocumento(),cargo.getFecha(),abogado,saldoDocumento);
	}
	
	
	
	

}
