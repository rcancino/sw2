package com.luxsoft.siipap.inventarios.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Solicitud de material 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_SOLICITUD_TRASLADOS",uniqueConstraints=@UniqueConstraint(
		columnNames={"SUCURSAL_ID","DOCUMENTO"})
	)
	@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class SolicitudDeTraslado extends BaseBean implements Replicable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="SOL_ID")
	private String id;
	
	@Version
	private int version;
	
	@Column(name="DOCUMENTO",nullable=false)
	private Long documento;
	
	@Column(name="FECHA",nullable=false)
	@Type(type="date")
	private Date fecha;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="SUCURSAL_ID", nullable=false,updatable=false)    
	private Sucursal sucursal;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="ORIGEN_ID", nullable=false,updatable=false)
    @NotNull
	private Sucursal origen;
	
	@Column(name="COMENTARIO",length=255)
	@Length(max=255)
	private String comentario;
	
	@Column(name="CLASIFICACION",length=255)
	@Length(max=255)
	private String clasificacion;
	
	
	@Column(name="REFERENCIA",length=30)
	@Length(max=30)
	private String referencia;
	
	@Transient
	@Length(max=255)
	private String comentarioTps;
	
	@CollectionOfElements(fetch=FetchType.EAGER)
	@JoinTable(
			name = "SX_SOLICITUD_TRASLADOSDET",
			joinColumns = @JoinColumn(name = "SOL_ID")
			)
	@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE ,
			org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	@Fetch(value=FetchMode.SUBSELECT)
	private List<SolicitudDeTrasladoDet> partidas=new ArrayList<SolicitudDeTrasladoDet>();
	
	@Column(name="PEDIDO_ID",length=255)
	@Length(max=255)
	private String pedido;
	
	
	public String getPedido() {
		return pedido;
	}

	public void setPedido(String pedido) {
		Object old=this.comentario;
		this.pedido = pedido;
		firePropertyChange("pedido", old, pedido);
	}
	
	
	
	
	/**
	 * Numero del documento q atendio la solicitud
	 */
	//@Column(name="ATENDIDO",nullable=true)
	@Formula( " (SELECT X.DOCUMENTO FROM  SX_TRASLADOS  X WHERE X.SOL_ID=SOL_ID AND X.TIPO=\'TPS\')")
	private Long atendido;
	
	
	
	
	@Column(name="REPLICADO",nullable=true)
	private Date replicado;
	
	@Column(name="IMPORTADO",nullable=true)
	private Date importado;
	
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
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}	

	public Sucursal getOrigen() {
		return origen;
	}

	public void setOrigen(Sucursal origen) {
		Object old=this.origen;
		this.origen = origen;
		firePropertyChange("origen", old, origen);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}
	
	
	public String getClasificacion() {
		return clasificacion;
	}

	public void setClasificacion(String clasificacion) {
		Object old=this.clasificacion;
		this.clasificacion = clasificacion;
		firePropertyChange("clasificacion", old, clasificacion);
	}
	

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		Object old=this.referencia;
		this.referencia = referencia;
		firePropertyChange("referencia", old, referencia);
	}

	public Long getAtendido() {
		return atendido;
	}

	public void setAtendido(Long atendido) {
		this.atendido = atendido;
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
	
	public String getComentarioTps() {
		return comentarioTps;
	}

	public void setComentarioTps(String comentarioTps) {
		Object old=this.comentarioTps;
		this.comentarioTps = comentarioTps;
		firePropertyChange("comentarioTps", old, comentarioTps);
	}

	public List<SolicitudDeTrasladoDet> getPartidas() {
		return partidas;
	}
	
	public void agregarPartida(final Producto producto,double cantidad){
		SolicitudDeTrasladoDet det=new SolicitudDeTrasladoDet();
		det.setSucursal(getSucursal().getId());
		det.setOrigen(getOrigen().getId());
		det.setProducto(producto);
		det.setSolicitado(cantidad);
		partidas.add(det);
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(getSucursal().getNombre())
		.append(getDocumento())
		.append(getOrigen())
		.toString();
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
		final SolicitudDeTraslado other = (SolicitudDeTraslado) obj;
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
	
	@Transient
	private Boolean porInventario;
	
	public Boolean getPorInventario() {
		return porInventario;
	}

	public void setPorInventario(Boolean porInventario) {
		Object old=this.porInventario;
		this.porInventario = porInventario;
		firePropertyChange("porInventario", old, porInventario);
	}

}
