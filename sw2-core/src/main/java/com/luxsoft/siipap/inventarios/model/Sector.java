package com.luxsoft.siipap.inventarios.model;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Entidad para el conteo de inventario
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_SECTOR"	
	)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class Sector extends BaseBean implements Serializable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="SECTOR_ID")
	protected String id;
	
	
	@Version
	private int version;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="SUCURSAL_ID", nullable=false,updatable=false)    
	private Sucursal sucursal;
	
	@Column(name="FECHA",nullable=false)
	@Type(type="date")
	private Date fecha;
	
	@Column(name="SECTOR")
	private int sector;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch= FetchType.EAGER		
				)
	@JoinColumn(name="SECTOR_ID",nullable=false)
	@Cascade(value={
			org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE
			})
	@IndexColumn(name="RNGL",base=1)
	private List<SectorDet> partidas=new ArrayList<SectorDet>();
	
	@Column(name="COMENTARIO",length=255)
	@Length(max=255)
	private String comentario;
	
	@Column(name="RESPONSABLE1",length=255)
	@Length(max=255)
	private String responsable1;
	
	@Column(name="RESPONSABLE2",length=255)
	@Length(max=255)
	private String responsable2;
	

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
	
	

	public int getSector() {
		return sector;
	}

	public void setSector(int sector) {
		int old=this.sector;
		this.sector = sector;
		firePropertyChange("sector", old, sector);
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
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}
	
	
	public String getResponsable1() {
		return responsable1;
	}

	public void setResponsable1(String responsable1) {
		Object old=this.responsable1;
		this.responsable1 = responsable1;
		firePropertyChange("responsable1", old, responsable1);
	}
	
	public String getResponsable2() {
		return responsable2;
	}

	public void setResponsable2(String responsable2) {
		Object old=this.responsable2;
		this.responsable2 = responsable2;
		firePropertyChange("responsable2", old, responsable2);
	}
	
	
	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
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

	public String getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}
	
	public List<SectorDet> getPartidas() {
		return partidas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result;
		result = prime * result
				+ ((sucursal == null) ? 0 : sucursal.hashCode());
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
		Sector other = (Sector) obj;	
		if (sucursal == null) {
			if (other.sucursal != null)
				return false;
		} else if (!sucursal.equals(other.sucursal))
			return false;
		return false;
		
	}
	
	public String toString(){
		String pattern="Sector: {0} ({1,date,long})  {2}";
		return MessageFormat.format(pattern, getSector(),getFecha(),getSucursal());
		
		
	}

	public boolean agregarPartida(SectorDet det) {
		det.setSector(this);
		return partidas.add(det);
		
	}
	
	
	


}
