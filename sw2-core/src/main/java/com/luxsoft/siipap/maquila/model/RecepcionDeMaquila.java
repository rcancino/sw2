package com.luxsoft.siipap.maquila.model;

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.sw3.replica.Replicable;


/**
 * Recepcion de material proveniente de maquiladores
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_RECEPCION_MAQUILA")
@GenericGenerator(name = "hibernate-uuid", strategy = "uuid", parameters = { @Parameter(name = "separator", value = "-") })
public class RecepcionDeMaquila extends BaseBean implements Replicable{
	
	@Id
	@GeneratedValue(generator = "hibernate-uuid")
	@Column(name = "ID")
	protected String id;

	@Version
	private int version;
		
	@Column(name="FECHA",nullable=false)
	@Type(type="date")
	@NotNull
	private Date fecha=new Date();
	
	@ManyToOne(optional=false)
	@JoinColumn(name="SUCURSAL_ID",nullable=false,updatable=false)
	@NotNull
	private Sucursal sucursal;
	
	@ManyToOne (optional=false
			,cascade={CascadeType.PERSIST,CascadeType.MERGE}
			,fetch=FetchType.EAGER)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false, updatable=false)
	@NotNull
	private Proveedor proveedor;
	
	@Column(name="DOCUMENTO")
	private Long documento;
	
	@Column(name="REMISION",length=25)
	@Length(max = 25) @NotEmpty(message="La remisión/factura es mandatoria")
	private String remision;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.EAGER,mappedBy="recepcion")
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
				,org.hibernate.annotations.CascadeType.REPLICATE}
	)
	@org.hibernate.annotations.OrderBy(clause = "RENGLON asc")
	private Set<EntradaDeMaquila> partidas=new HashSet<EntradaDeMaquila>();
	
	
	@Length(max=100)
	private String comentario;
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "createUser", column = @Column(name = "CREADO_USR", nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "updateUser", column = @Column(name = "MODIFICADO_USR", nullable = true, insertable = true, updatable = true)),
			@AttributeOverride(name = "creado",		column = @Column(name = "CREADO", nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "modificado", column = @Column(name = "MODIFICADO", nullable = true, insertable = true, updatable = true)) }
	)
	private UserLog log = new UserLog();

	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();
	
	
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		Object old=this.proveedor;		
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
	}

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
	}

	public String getRemision() {
		return remision;
	}

	public void setRemision(String remision) {
		Object old=this.remision;
		this.remision = remision;
		firePropertyChange("remision", old, remision);
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

	public void setVersion(int version) {
		this.version = version;
	}
	
	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
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
		String old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}
	
	

	public Set<EntradaDeMaquila> getPartidas() {
		return partidas;
	}
	
	public boolean eleiminarPartida(EntradaDeMaquila det){
		det.setRecepcion(null);
		return partidas.remove(det);
	}
	public boolean agregarPartida(EntradaDeMaquila det){
		det.setRecepcion(this);
		return partidas.add(det);
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

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(documento)
		.append(sucursal)
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj==null) return false;
		if (getClass() != obj.getClass())
			return false;
		RecepcionDeMaquila other = (RecepcionDeMaquila) obj;
		return new EqualsBuilder()
		.append(this.sucursal, other.getSucursal())
		.append(this.documento, other.getDocumento())
		.isEquals();
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(sucursal)
		.append(documento)
		.append(fecha)
		.append(comentario)
		.toString();
	}	

}
