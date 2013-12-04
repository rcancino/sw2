package com.luxsoft.sw3.cfd.model;


import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Entidad para administrar los folios de los comprobantes fiscales 
 * digitales (CFD)
 * 
 * @author Ruben Cancino Ramos
 * 
 */
@Entity
@Table(name="SX_CFD_FOLIOS"
	,uniqueConstraints=@UniqueConstraint(columnNames={"SUCURSAL_ID","SERIE"})
)
	
public class FolioFiscal extends BaseBean {
	
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private FolioFiscalId id;
	
	@Version
	private int version;
	
	@Column(name="FOLIO",nullable=false)
	private Long folio;
	
	@Column(name="FOLIO_INI",nullable=false)
	private Long folioInicial;
	
	@Column(name="FOLIO_FIN",nullable=false)
	private Long folioFinal;
	
	@Column(name="ASIGNACION",nullable=false)
	@Type(type="date")
	private Date asignacion;
	
	@Column(name="NO_APROBACION",nullable=false)
	private Integer noAprobacion;
	
	@Column(name="ANO_APROBACION",nullable=false)
	private Integer anoAprobacion;	
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();

	public FolioFiscalId getId() {
		return id;
	}

	public void setId(FolioFiscalId id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public Long getFolio() {
		if(folio==null)
			folio=0L;
		return folio;
	}

	public void setFolio(Long folio) {		
		this.folio = folio;
	}
	
	public Long getFolioInicial() {
		return folioInicial;
	}

	public void setFolioInicial(Long folioInicial) {
		Object old=this.folioInicial;
		this.folioInicial = folioInicial;
		firePropertyChange("folioInicial", old, folioInicial);
	}

	public Long getFolioFinal() {
		return folioFinal;
	}

	public void setFolioFinal(Long folioFinal) {
		Object old=this.folioFinal;
		this.folioFinal = folioFinal;
		firePropertyChange("folioFinal", old, folioFinal);
	}

	public Date getAsignacion() {
		return asignacion;
	}

	public void setAsignacion(Date asignacion) {
		Object old=this.asignacion;
		this.asignacion = asignacion;
		firePropertyChange("asignacion", old, asignacion);
	}
	
	public Integer getNoAprobacion() {
		return noAprobacion;
	}

	public void setNoAprobacion(Integer noAprobacion) {
		Object old=this.noAprobacion;
		this.noAprobacion = noAprobacion;
		firePropertyChange("noAprobacion", old, noAprobacion);
	}

	public Integer getAnoAprobacion() {
		return anoAprobacion;
	}

	public void setAnoAprobacion(Integer anoAprobacion) {
		Object old=this.anoAprobacion;
		this.anoAprobacion = anoAprobacion;
		firePropertyChange("anoAprobacion", old, anoAprobacion);
	}
	
	/**
	 * Incrementa el folio en uno y lo regresa
	 * 
	 * @return
	 */
	public Long next(){
		Assert.isTrue(getFolio().longValue()<getFolioFinal().longValue(),"Se llego al límite de folios");
		Long next=getFolio()+1;
		setFolio(next);
		return next;
		
	}

	public Date getImportado() {
		return importado;
	}

	public void setImportado(Date importado) {
		this.importado = importado;
	}

	public Date getReplicado() {
		return replicado;
	}

	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
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
		FolioFiscal other = (FolioFiscal) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(getAnoAprobacion())
		.append(getNoAprobacion())
		.append(getFolio())
		.toString();
	}

}
