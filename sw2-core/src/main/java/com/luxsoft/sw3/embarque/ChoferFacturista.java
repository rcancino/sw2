package com.luxsoft.sw3.embarque;

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
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Cascade;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.replica.Replicable;

@Entity
@Table(name="SX_CHOFER_FACTURISTA")
public class ChoferFacturista extends BaseBean implements Replicable{
	
	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="FACTURISTA_ID", 
            initialValue=100,
            allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
	@Column (name="ID",nullable=false,unique=true)
	private Long id;
	
	@Column(name="NOMBRE",nullable=false,unique=true)
	private String nombre;
	
	@Embedded
	private Direccion direccion=new Direccion();
	
	@Column(name="TEL1",length=25)
	private String telefono1;
	
	@Column(name="TEL2",length=25)
	private String telefono2;
	
	@Column(name="FAX",length=25)
	private String fax;
	
	@Column(name="RFC",length=25)
	private String rfc;
	
	@Email
	@Length (max=100)
	private String email1;
	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE			
			}
			,fetch=FetchType.LAZY,mappedBy="facturista")
	@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	private Set<Chofer> choferes = new HashSet<Chofer>();
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Direccion getDireccion() {
		return direccion;
	}

	public void setDireccion(Direccion direccion) {
		this.direccion = direccion;
	}

	public String getTelefono1() {
		return telefono1;
	}

	public void setTelefono1(String telefono1) {
		this.telefono1 = telefono1;
	}

	public String getTelefono2() {
		return telefono2;
	}

	public void setTelefono2(String telefono2) {
		this.telefono2 = telefono2;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}
	
	

	public Set<Chofer> getChoferes() {
		return choferes;
	}
	
	public boolean agregarChofer(Chofer chofer){
		chofer.setFacturista(this);
		return choferes.add(chofer);
	}
	
	public boolean eliminarChofer(Chofer chofer){
		chofer.setFacturista(null);
		return choferes.remove(chofer);
	}

	

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	

	public UserLog getLog() {
		if(log==null){
			log=new UserLog();
		}
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
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
	
	public String getEmail1() {
		return email1;
	}

	public void setEmail1(String email1) {
		Object old=this.email1;
		this.email1 = email1;
		firePropertyChange("email1", old, email1);
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
		ChoferFacturista other = (ChoferFacturista) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
	@Override
	public String toString() {
		return this.nombre;
	}
	

}
