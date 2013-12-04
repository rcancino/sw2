package com.luxsoft.siipap.compras.model;


import java.text.MessageFormat;
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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.sw3.replica.Replicable;


/**
 * Maestro para las devoluciones de compras
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_DEVOLUCION_COMPRAS")
@GenericGenerator(name = "hibernate-uuid", strategy = "uuid", parameters = { @Parameter(name = "separator", value = "-") })
public class DevolucionDeCompra extends BaseBean implements Replicable{
			
	
	@Id
	@GeneratedValue(generator = "hibernate-uuid")
	@Column(name = "DEVOLUCION_ID")
	protected String id;

	@Version
	private int version;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	@NotNull
	private Date fecha;
	
	@Column(name="DOCUMENTO")
	private Long documento;
	
	@ManyToOne (optional=false,fetch=FetchType.EAGER)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false, updatable=false)
	@NotNull
	private Proveedor proveedor;
	
	@Column(name="CLAVE",nullable=false,length=4)
	private String clave;
	
	@Column (name="NOMBRE",nullable=false,length=250)
	private String nombre;
	
	@ManyToOne (optional=true,fetch=FetchType.EAGER)
	@JoinColumn (name="SUCURSAL_ID", nullable=true, updatable=true)
	@NotNull
	private Sucursal sucursal;
	
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@Column(name="REFERENCIA")
	@Length(max=20)
	private String referencia;
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REMOVE }, fetch = FetchType.EAGER, mappedBy = "devolucion")
	@Fetch(FetchMode.SUBSELECT)	
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
					,org.hibernate.annotations.CascadeType.REPLICATE
					})
	private Set<DevolucionDeCompraDet> partidas = new HashSet<DevolucionDeCompraDet>();
	
	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "createUser", column = @Column(name = "CREADO_USR", nullable = true, insertable = true, updatable = true)),
			@AttributeOverride(name = "updateUser", column = @Column(name = "MODIFICADO_USR", nullable = true, insertable = true, updatable = true)),
			@AttributeOverride(name = "creado",		column = @Column(name = "CREADO", nullable = true, insertable = true, updatable = true)),
			@AttributeOverride(name = "modificado", column = @Column(name = "MODIFICADO", nullable = true, insertable = true, updatable = true)) }
	)
	private UserLog log = new UserLog();
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(name="CREATED_IP",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="updatedIp",	column=@Column(name="UPDATED_IP",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(name="CREATED_MAC",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="updatedMac",column=@Column(name="UPDATED_MAC",nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();
	
	public DevolucionDeCompra(){
	}

	

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		
		this.fecha = fecha;
	}

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	
	
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		Object old=this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
		if(proveedor!=null){
			this.clave=proveedor.getClave();
			this.nombre=proveedor.getNombre();
		}else{
			this.clave=null;
			this.nombre=null;
		}
		
	}

	public String getClave() {
		return clave;
	}



	public void setClave(String clave) {
		this.clave = clave;
	}



	public String getNombre() {
		return nombre;
	}



	public void setNombre(String nombre) {
		this.nombre = nombre;
	}



	public String getReferencia() {
		return referencia;
	}



	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}



	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
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
	
	public boolean agregarPartida(DevolucionDeCompraDet det){
		det.setDevolucion(this);
		det.setRenglon(partidas.size()+1);
		return partidas.add(det);
	}
	

	public Set<DevolucionDeCompraDet> getPartidas() {
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
		DevolucionDeCompra other = (DevolucionDeCompra) obj;
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
		String pattern=" {0,date,short} {1} {2}";
		return MessageFormat.format(pattern, getFecha(),getDocumento(),getComentario());
	}
	
	
	public String getUsuario() {
		return getLog().getCreateUser();
	}

	public void setUsuario(String usuario) {
		Object old=this.getUsuario();
		getLog().setCreateUser(usuario);
		firePropertyChange("usuario", old, usuario);
	}
	

	@Column(name="TX_IMPORTADO",nullable=true)
	protected Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	protected Date replicado;

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
	
}
