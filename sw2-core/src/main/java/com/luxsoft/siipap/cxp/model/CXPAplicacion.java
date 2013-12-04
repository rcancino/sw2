package com.luxsoft.siipap.cxp.model;

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.CantidadMonetaria;


@Entity
@Table(name="SX_CXP_APLICACIONES")
public class CXPAplicacion {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="APLICACION_ID")
	private Long id;
	
	@Column(name="FECHA",nullable=false)
	private Date fecha=new Date();
	
	@ManyToOne(optional=false)
	@JoinColumn (name="ABONO_ID",nullable=false)
	@NotNull
	private CXPAbono abono;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="CARGO_ID",nullable=false)
	@NotNull
	private CXPCargo cargo;
	
	@Column(name="IMPORTE",nullable=false)
	@NotNull
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@Column(name="TIPO_ABONO",nullable=false,length=30)
	private String tipoAbono;
	
	@Column(name="TIPO_CARGO",nullable=false,length=15)
	private String tipoCargo;
	
	@Column(name="CREADO_USERID",updatable=false,length=50)
	private String createUser;
	
    @Column(name="MODIFICADO_USERID",length=50)
    private String updateUser;
	
    @Type (type="time")
	@Column(name="CREADO",updatable=false,nullable=true)
	private Date creado=new Date();
	
    @Type (type="time")
	@Column(name="MODIFICADO",updatable=false,insertable=false)
	private Date modificado;

	public Long getId() {
		return id;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public CXPAbono getAbono() {
		return abono;
	}

	public void setAbono(CXPAbono abono) {
		this.abono = abono;
		setTipoAbono(abono.getTipoId());
	}

	public CXPCargo getCargo() {
		return cargo;
	}

	public void setCargo(CXPCargo cargo) {
		this.cargo = cargo;
		setTipoCargo((cargo instanceof CXPFactura)?"FAC":"CAR");
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}
	
	public BigDecimal getImporteMN(){
		BigDecimal tc=BigDecimal.valueOf(getAbono().getTc());
		return getImporte().multiply(tc);
	}
	public CantidadMonetaria getImporteCM(){
		return new CantidadMonetaria(getImporte(),getCargo().getMoneda());
	}

	public String getTipoAbono() {
		return tipoAbono;
	}

	public void setTipoAbono(String tipo) {
		this.tipoAbono = tipo;
	}
	
	

	public String getTipoCargo() {
		return tipoCargo;
	}

	public void setTipoCargo(String tipoCargo) {
		this.tipoCargo = tipoCargo;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getCreado() {
		return creado;
	}

	public void setCreado(Date creado) {
		this.creado = creado;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public Date getModificado() {
		return modificado;
	}
	
	public String toString(){
		String pattern="Cargo: {0} Abono:{1} Fecha: {2} Monto aplicado: {3}";
		return MessageFormat.format(pattern, cargo.getId(),abono.getId(),fecha,importe);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(39,93)
		//.append(getAbono().getId())
		.append(getCargo().getId())
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CXPAplicacion other = (CXPAplicacion) obj;
		return new EqualsBuilder()
		//.append(getAbono().getId(), other.getAbono().getId())
		.append(getCargo().getId(), other.getCargo().getId())
		.isEquals();
	}
    

	public CXPFactura getFactura(){
		return (CXPFactura)getCargo();
	}

}
