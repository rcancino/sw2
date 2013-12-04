package com.luxsoft.siipap.ventas.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;

@Entity
@Table(name="SX_LP_VENT")
public class ListaDePreciosVenta extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="LISTA_ID")
	private Long id;
	
	@Version
	private int version;
	
	
	@Column(name="AUTORIZADA")
	@Length(max=255)
	private String autorizada;
	
	@Column(name="APLICADA",nullable=true)
	private Date aplicada;
	
	@Column(name="COMENTARIO")
	private String comentario;
	
	 @OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="lista")
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE})
	private Set<ListaDePreciosVentaDet> precios=new HashSet<ListaDePreciosVentaDet>();
	
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
	
	public ListaDePreciosVenta(){		
		
	}
	
	
	
	public Long getId() {
		return id;
	}
	
	public int getVersion() {
		return version;
	}
	

	public String getAutorizada() {
		return autorizada;
	}
	public void setAutorizada(String autorizada) {
		this.autorizada = autorizada;
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

	public Set<ListaDePreciosVentaDet> getPrecios() {
		return precios;
	}
	
	public boolean agregarPrecio(final ListaDePreciosVentaDet det){
		det.setLista(this);
		return precios.add(det);
	}
	public boolean removerPrecio(final ListaDePreciosVentaDet det){
		det.setLista(null);
		return precios.remove(det);
	}	
	
	public Date getAplicada() {
		return aplicada;
	}

	public void setAplicada(Date aplicada) {
		this.aplicada = aplicada;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if(obj==null) 
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListaDePreciosVenta other = (ListaDePreciosVenta) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(id)		
		.append(comentario)
		.append(autorizada)
		.toString();
	}
	
	@Column (name="TC_DOLARES",nullable=false)
	private double tcDolares;
	
	@Column (name="TC_EUROS",nullable=false)
	private double tcEuros;

	public double getTcDolares() {
		return tcDolares;
	}
	public void setTcDolares(double tcDolares) {
		double old=this.tcDolares;
		this.tcDolares = tcDolares;
		firePropertyChange("tcDolares", old, tcDolares);
	}
	public double getTcEuros() {
		return tcEuros;
	}
	
	public void setTcEuros(double tcEuros) {
		double old=this.tcEuros;
		this.tcEuros = tcEuros;
		firePropertyChange("tcEuros", old, tcEuros);
	}

		
	

}
