package com.luxsoft.sw3.contabilidad.model;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;

@Entity
@Table (name="SX_CUENTAS_CONTABLES")
public class CuentaContable {
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="CUENTA_ID")
	private Long id;
	
	@Version
	private int version;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO", nullable = false,length=20)
	@NotNull
	private Tipo tipo;
	
	@Column(name = "SUB_TIPO", nullable = false,length=20)
	@NotNull
	private String subTipo;
	
	@Column(name="CLAVE",nullable=false,length=20,unique=true)
	@NotNull
	@Length(max=4,min=1)
	private String clave;
	
	@Column(name="DESCRIPCION",nullable=false)
	@NotNull
	@Length(min=1,max=255)
	private String descripcion;
	
	@Column(name="DESCRIPCION2",nullable=true)
	@Length(max=255)
	private String descripcion2;	
	
	
	@Column (name="DETALLE",nullable=false)
	@NotNull
	private boolean detalle=true;
	
	@Column (name="DE_RESULTADO",nullable=false)
	@NotNull
	private boolean deResultado=false;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "NATURALEZA", nullable = false,length=20)
	@NotNull
	private Naturaleza naturaleza;
	
	@ManyToOne(optional=true,fetch=FetchType.LAZY)
	@JoinColumn (name="PADRE_ID")
	private CuentaContable padre;
		
	@OneToMany (mappedBy="padre"
		, cascade={CascadeType.PERSIST,CascadeType.MERGE}
		,fetch=FetchType.LAZY)
	private Set<CuentaContable> subCuentas=new HashSet<CuentaContable>();
	
	@OneToMany (mappedBy="cuenta"
			, cascade={CascadeType.PERSIST,CascadeType.MERGE}
			,fetch=FetchType.LAZY)
	private Set<ConceptoContable> conceptos=new HashSet<ConceptoContable>();
	
	@Column (name="PRES_CONTABLE",nullable=false)
	@NotNull
	private boolean presentacionContable=false;
	
	@Column (name="PRES_FISCAL",nullable=false)
	@NotNull
	private boolean presentacionFiscal=false;
	
	@Column (name="PRES_FINANCIERA",nullable=false)
	@NotNull
	private boolean presentacionFinanciera=false;
	
	@Column (name="PRES_PRESUPUESTAL",nullable=false)
	@NotNull
	private boolean presentacionPresupuestal=false;
	
	
	
	
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
	       @AttributeOverride(name="createdIp",	column=@Column(name="CREADO_IP" ,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(name="MODIFICADO_IP",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(name="CREADO_MAC",nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(name="MODIFICADO_MAC",nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();
	
	public CuentaContable() {
	}

	public CuentaContable(String clave, String descripcion) {
		this.clave = clave;
		this.descripcion = descripcion;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDescripcion2() {
		return descripcion2;
	}

	public void setDescripcion2(String descripcion2) {
		this.descripcion2 = descripcion2;
	}

	
	public boolean isMayor() {
		return getPadre()==null;
	}
	
	public boolean isDetalle() {
		return detalle;
	}
	
	public boolean isAcumulativa(){
		return !isDetalle();
	}

	public void setDetalle(boolean detalle) {
		this.detalle = detalle;
	}

	public CuentaContable getPadre() {
		return padre;
	}

	public void setPadre(CuentaContable padre) {
		this.padre = padre;
	}

	public Set<CuentaContable> getSubCuentas() {
		return subCuentas;
	}
	
	public boolean agregarCuenta(CuentaContable subCuenta){
		Assert.isTrue(isAcumulativa(),"Las cuentas de movimientos no pueden tener sub cuentas");
		subCuenta.setPadre(this);
		subCuenta.setTipo(getTipo());
		return subCuentas.add(subCuenta);
	}
	
	public ConceptoContable agregarCuenta(String clave){
		ConceptoContable concepto=new ConceptoContable();
		concepto.setCuenta(this);
		return concepto;
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

	public int getVersion() {
		return version;
	}
	

	public String getSubTipo() {
		return subTipo;
	}

	public void setSubTipo(String subTipo) {		
		this.subTipo = subTipo;		
	}

	public boolean isDeResultado() {
		return deResultado;
	}

	public void setDeResultado(boolean deResultado) {
		this.deResultado = deResultado;
	}

	public void setSubCuentas(Set<CuentaContable> subCuentas) {
		this.subCuentas = subCuentas;
	}
	

	public Naturaleza getNaturaleza() {
		return naturaleza;
	}

	public void setNaturaleza(Naturaleza naturaleza) {
		this.naturaleza = naturaleza;
	}
	

	public boolean isPresentacionContable() {
		return presentacionContable;
	}

	public void setPresentacionContable(boolean presentacionContable) {
		this.presentacionContable = presentacionContable;
	}

	public boolean isPresentacionFiscal() {
		return presentacionFiscal;
	}

	public void setPresentacionFiscal(boolean presentacionFiscal) {
		this.presentacionFiscal = presentacionFiscal;
	}

	public boolean isPresentacionFinanciera() {
		return presentacionFinanciera;
	}

	public void setPresentacionFinanciera(boolean presentacionFinanciera) {
		this.presentacionFinanciera = presentacionFinanciera;
	}

	public boolean isPresentacionPresupuestal() {
		return presentacionPresupuestal;
	}

	public void setPresentacionPresupuestal(boolean presentacionPresupuestal) {
		this.presentacionPresupuestal = presentacionPresupuestal;
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clave == null) ? 0 : clave.hashCode());
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
		CuentaContable other = (CuentaContable) obj;
		if (clave == null) {
			if (other.clave != null)
				return false;
		} else if (!clave.equals(other.clave))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String toString(){
		return MessageFormat.format("{0} ({1})", getDescripcion(),getClave());
		/*
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(getId())
		.append(getClave())
		.append(getDescripcion())
		.toString();*/
	}
	


	/*
	 * Valida  la clave asignada
	 */
	@AssertTrue(message="La clave debe ser  alfanumerica ")
	public boolean validarClave(){
		boolean a=StringUtils.isAlphanumeric(clave);
		boolean b=StringUtils.isNotBlank(clave);
		return (a && b);
	}

	public ConceptoContable getConcepto(String conceptoClave) {
		for(ConceptoContable c:conceptos){
			if(c.getClave().equals(conceptoClave))
				return c;
		}
		return null;
	}
	
	public ConceptoContable getConceptoPorDescripcion(String conceptoClave) {
		for(ConceptoContable c:conceptos){
			if(c.getDescripcion().equals(conceptoClave))
				return c;
		}
		return null;
	}

	public Set<ConceptoContable> getConceptos() {
		return conceptos;
	}

	public void setConceptos(Set<ConceptoContable> conceptos) {
		this.conceptos = conceptos;
	}
	

}
