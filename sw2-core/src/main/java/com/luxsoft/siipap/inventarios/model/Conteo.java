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
@Table(name="SX_CONTEO"	
	)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class Conteo extends BaseBean implements Serializable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="CONTEO_ID")
	protected String id;
	
	
	@Version
	private int version;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="SUCURSAL_ID", nullable=false,updatable=false)    
	private Sucursal sucursal;
	
	@Column(name="DOCUMENTO",nullable=false)
	private Long documento;
	
	@Column(name="FECHA",nullable=false)
	@Type(type="date")
	private Date fecha;
	
	@Column(name="SECTOR")
	private int sector;
	
	@Column(name="COMENTARIO",length=255)
	@Length(max=255)
	private String comentario;
	
	@Column(name="CONTADOR1",length=50)
	@Length(max=50)
	private String contador1;
	
	@Column(name="CONTADOR",length=50)
	@Length(max=50)
	private String contador2;
	
	@Column(name="CAPTURISTA",length=50)
	@Length(max=50)
	private String capturista;
	
	
	@Column(name="AUDITOR1",length=50)
	@Length(max=50)
	private String auditor1;
	
	@Column(name="AUDITOR2",length=50)
	@Length(max=50)
	private String auditor2;
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch= FetchType.EAGER		
				)
	@JoinColumn(name="CONTEO_ID",nullable=false)
	@Cascade(value={
			org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE
			})
	@IndexColumn(name="RNGL",base=1)
	private List<ConteoDet> partidas=new ArrayList<ConteoDet>();
	
	
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

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
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

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public String getContador1() {
		return contador1;
	}

	public void setContador1(String contador1) {
		Object old=this.contador1;
		this.contador1 = contador1;
		firePropertyChange("contador1", old, contador1);
	}

	public String getContador2() {
		return contador2;
	}

	public void setContador2(String contador2) {
		Object old=this.contador2;
		this.contador2 = contador2;
		firePropertyChange("contador2", old, contador2);
	}

	public String getCapturista() {
		return capturista;
	}

	public void setCapturista(String capturista) {
		Object old=this.capturista;
		this.capturista = capturista;
		firePropertyChange("capturista", old, capturista);
	}

	public String getAuditor1() {
		return auditor1;
	}

	public void setAuditor1(String auditor1) {
		Object old=this.auditor1;
		this.auditor1 = auditor1;
		firePropertyChange("auditor1", old, auditor1);
	}

	public String getAuditor2() {
		return auditor2;
	}

	public void setAuditor2(String auditor2) {
		Object old=this.auditor2;
		this.auditor2 = auditor2;
		firePropertyChange("auditor2", old, auditor2);
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
	
	

	public List<ConteoDet> getPartidas() {
		return partidas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((documento == null) ? 0 : documento.hashCode());
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
		Conteo other = (Conteo) obj;
		if (documento == null) {
			if (other.documento != null)
				return false;
		} else if (!documento.equals(other.documento))
			return false;
		if (sucursal == null) {
			if (other.sucursal != null)
				return false;
		} else if (!sucursal.equals(other.sucursal))
			return false;
		return true;
	}
	
	public String toString(){
		String pattern="Conteo: {0} ({1,date,long})  {2}";
		return MessageFormat.format(pattern, getDocumento(),getFecha(),getSucursal());
	}

	public boolean agregarPartida(ConteoDet det) {
		det.setConteo(this);
		return partidas.add(det);
	}

}
