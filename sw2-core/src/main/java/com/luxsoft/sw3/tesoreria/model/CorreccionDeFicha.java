package com.luxsoft.sw3.tesoreria.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.model.AdressLog;

@Entity
@Table(name="SX_FICHAS_CORRECIONES")
public class CorreccionDeFicha extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="ID")
	private Long id;
	
	@Column(name="fecha" , nullable=false)
	@Type(type="date")
	private Date fecha;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "SUCURSAL_ID", nullable = false)
	@NotNull(message="La sucursal es mandatoria")
	private Sucursal sucursal;
	
	@ManyToOne(optional = false)			
	@JoinColumn(name = "FICHA_ID")
	private Ficha ficha;
	
	@Column(name="IMPORTE_ORIGINAL",nullable=false)
	private BigDecimal importeOriginal=BigDecimal.ZERO;
	
	@Column(name="IMPORTE_REAL",nullable=false)
	private BigDecimal importeReal=BigDecimal.ZERO;
	
	@Column(name="DIFERENCIA",nullable=false)
	private BigDecimal diferencia=BigDecimal.ZERO;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="CONCEPTO_ID", nullable=false) 
	@NotNull(message="Registre el empleado/cajero")
	private ConceptoContable concepto;
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@Enumerated(EnumType.STRING)
	@Column(name="TIPO",nullable=true)
	@NotNull
	private Tipo tipo=Tipo.FALTANTE_CORRECCION_FICHA;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();

	
	public CorreccionDeFicha(Ficha ficha) {
		setFicha(ficha);
	}
	
	public CorreccionDeFicha(){}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Ficha getFicha() {
		return ficha;
	}

	public void setFicha(Ficha ficha) {
		Assert.notNull(ficha,"La ficha no puede ser nula");
		this.ficha = ficha;
		setImporteOriginal(ficha.getTotal());
		setFecha(ficha.getFecha());
		setSucursal(ficha.getSucursal());
	}

	public BigDecimal getImporteOriginal() {
		return importeOriginal;
	}

	public void setImporteOriginal(BigDecimal importeOriginal) {
		this.importeOriginal = importeOriginal;
	}
	
	public BigDecimal getImporteReal() {
		return importeReal;
	}

	public void setImporteReal(BigDecimal importeReal) {
		Object old=this.importeReal;
		this.importeReal = importeReal;
		firePropertyChange("importeReal", old, importeReal);
		actualizar();
	}
	
	public BigDecimal getDiferencia() {
		return diferencia;
	}

	public void setDiferencia(BigDecimal diferencia) {
		this.diferencia = diferencia;
	}

	public ConceptoContable getConcepto() {
		return concepto;
	}

	public void setConcepto(ConceptoContable concepto) {
		Object old=this.concepto;
		this.concepto = concepto;
		firePropertyChange("concepto", old, concepto);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public AdressLog getAddresLog() {
		return addresLog;
	}

	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ficha == null) ? 0 : ficha.hashCode());
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
		CorreccionDeFicha other = (CorreccionDeFicha) obj;
		if (ficha == null) {
			if (other.ficha != null)
				return false;
		} else if (!ficha.equals(other.ficha))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append("Fecha",getFecha())
		.append("Corrección: "+getDiferencia())
		.toString();
	}

	public void actualizar(){
		switch (getTipo()) {
		case FALTANTE_CORRECCION_FICHA:
			setDiferencia(getImporteReal().subtract(getImporteOriginal()));
			break;
		case FALTANTE_POR_OPERACION:			
		case SOBRANTE_NO_IDENTIFICADO:
		case SOBRANTE_POR_COBRANZA:
		case FALTANTE_EN_VALORES:
		case SOBRANTE_EN_VALORES:
			setDiferencia(getImporteReal());
			break;
		default:
			break;
		}
			
		
	}
	
	public Tipo getTipo() {
		return tipo;
	}
	public void setTipo(Tipo tipo) {
		Object old=this.tipo;
		this.tipo = tipo;
		firePropertyChange("tipo", old, tipo);
	}
	
	public  static enum Tipo{
		FALTANTE_CORRECCION_FICHA
		,FALTANTE_POR_OPERACION
		,SOBRANTE_POR_COBRANZA
		,SOBRANTE_NO_IDENTIFICADO
		,FALTANTE_EN_VALORES
		,SOBRANTE_EN_VALORES
	}
}
