package com.luxsoft.siipap.inventarios.model;

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
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Traslado de material entre sucursales
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_TRASLADOS"
	//,uniqueConstraints=@UniqueConstraint(columnNames={"TIPO","DOCUMENTO","SOL_ID"})
	)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class Traslado extends BaseBean implements Replicable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="TRASLADO_ID")
	protected String id;
	
	@Version
	private int version;
	
	@Column(name="TIPO",length=3,nullable=false)
	private String tipo;
	
	@Column(name="DOCUMENTO",nullable=false)
	private Long documento;
	
	@Column(name="FECHA",nullable=false)
	@Type(type="date")
	private Date fecha;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="SUCURSAL_ID", nullable=false,updatable=false)    
	private Sucursal sucursal;
	
	@ManyToOne (optional=false,cascade={CascadeType.MERGE,CascadeType.PERSIST})
    @JoinColumn (name="SOL_ID", nullable=false,updatable=false)
    @NotNull
	private SolicitudDeTraslado solicitud;
	
	@Column(name="IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="REPLICADO",nullable=true)
	private Date replicado;
		
	@Column(name="CHOFER")
	private String chofer;
	
	@ManyToOne (optional=true)
    @JoinColumn (name="CHOFER_ID")    
	private Chofer choferId;
	
	
	@Column(name="COMENTARIO",length=255)
	@Length(max=255)
	private String comentario;
	
	@Formula("(select IFNULL(sum( (-a.CANTIDAD/a.FACTORU)* b.KILOS ),0) " +
			"FROM SX_INVENTARIO_TRD a join SX_PRODUCTOS b on (a.CLAVE=b.CLAVE)" +
			" where a.TRASLADO_ID=TRASLADO_ID and a.TIPO=\'TPS\')")
	private double kilos=0;
	
	@Column(name="COMISION_CHOFER",nullable=false)
	private double comisionChofer;
	
	@Column(name="COMISION_FECHA",nullable=true)
	private Date fechaComision;
	
	@Column(name="COMISION_COMENTARIO",nullable=true)
	private String comentarioComision;
	
	@Column(name="PORINVENTARIO")
	private Boolean porInventario;
	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch= FetchType.LAZY			
				)
	@JoinColumn(name="TRASLADO_ID",nullable=false)
	@Cascade(value={
			org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE
			})
	@IndexColumn(name="RNGL",base=1)
	private List<TrasladoDet> partidas=new ArrayList<TrasladoDet>();
	
	
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
	
	@Column(name="CORTADOR",nullable=true,length=150)
	private String cortador;
	
	@Column(name="SURTIDOR",nullable=true,length=150)
	private String surtidor;
	
	@Column(name="SUPERVISO",nullable=true,length=150)
	private String superviso;
	
	@Formula("(select X.CFD_ID FROM SX_CFDI X where X.ORIGEN_ID=TRASLADO_ID )")
	private String cfdi;
	
	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
	}

	public SolicitudDeTraslado getSolicitud() {
		return solicitud;
	}

	public void setSolicitud(SolicitudDeTraslado solicitud) {
		Object old=this.solicitud;
		this.solicitud = solicitud;
		firePropertyChange("solicitud", old, solicitud);
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

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal origen) {
		Object old=this.sucursal;
		this.sucursal = origen;
		firePropertyChange("sucursal", old, origen);
	}
	
	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}
	
	

	public String getChofer() {
		return chofer;
	}

	public void setChofer(String chofer) {
		Object old=this.chofer;
		this.chofer = chofer;
		firePropertyChange("chofer", old, chofer);
	}
	
	public Chofer getChoferId() {
		return choferId;
	}

	public void setChoferId(Chofer choferId) {
		Object old=this.choferId;
		this.choferId = choferId;
		firePropertyChange("choferId", old, choferId);
		if(choferId!=null){
			setChofer(choferId.getNombre());
		}else
			setChofer(null);
	}

	public List<TrasladoDet> getPartidas() {
		return partidas;
	}

	public UserLog getLog() {
		return log;
	}

	public AdressLog getAddresLog() {
		return addresLog;
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

	public boolean agregarPartida(final TrasladoDet det){
		Assert.notNull(det,"La partida a agregar no debe ser nula");
		det.setTraslado(this);
		det.setRenglon(partidas.size()+1);
		return partidas.add(det);
	}

	
	public boolean eliminarPartida(final TrasladoDet det){
		Assert.notNull(det,"La partida no debe ser nula");	
		det.setTraslado(null);
		return partidas.remove(det);
	}
	
	
	
	public double getKilos() {
		return kilos;
	}
	

	public double getComisionChofer() {
		return comisionChofer;
	}

	public void setComisionChofer(double comisionChofer) {
		double old=this.comisionChofer;
		this.comisionChofer = comisionChofer;
		firePropertyChange("comisionChofer", old, comisionChofer);
	}

	public Date getFechaComision() {
		return fechaComision;
	}

	public void setFechaComision(Date fechaComision) {
		Object old=this.fechaComision;
		this.fechaComision = fechaComision;
		firePropertyChange("fechaComision", old, fechaComision);
	}

	public String getComentarioComision() {
		return comentarioComision;
	}

	public void setComentarioComision(String comentarioComision) {
		Object old=this.comentarioComision;
		this.comentarioComision = comentarioComision;
		firePropertyChange("comentarioComision", old, comentarioComision);
	}
	
	@Transient
	private double precioComisionTonelada;
	

	public double getPrecioComisionTonelada() {
		return precioComisionTonelada;
	}

	public void setPrecioComisionTonelada(double precio) {
		double old=this.precioComisionTonelada;
		this.precioComisionTonelada = precio;
		firePropertyChange("precioComisionTonelada", old, precio);
		if(precio>=0){
			double toneladas=getKilos()/1000;
			double importe=toneladas*precio;
			setComisionChofer(importe);
			setFechaComision(new Date());
		}
	}
	
	public Boolean getPorInventario() {
		return porInventario;
	}

	public void setPorInventario(Boolean porInventario) {
		Object old=this.porInventario;
		this.porInventario = porInventario;
		firePropertyChange("porInventario", old, porInventario);
	}
	
	

	public String getCortador() {
		return cortador;
	}

	public void setCortador(String cortador) {
		Object old=this.cortador;
		this.cortador = cortador;
		firePropertyChange("cortador", old, "cortador");
	}

	public String getSurtidor() {
		return surtidor;
	}

	public void setSurtidor(String surtidor) {
		Object old=this.surtidor;
		this.surtidor = surtidor;
		firePropertyChange("surtidor", old, "surtidor");
	}

	public String getSuperviso() {
		return superviso;
	}

	public void setSuperviso(String superviso) {
		Object old=this.superviso;
		this.superviso = superviso;
		firePropertyChange("superviso", old, "superviso");
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
		final Traslado other = (Traslado) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String toString(){
		String pattern="{0} {1} {2}";
		return MessageFormat.format(pattern, id,fecha,comentario);
	}

	public String getCfdi() {
		return cfdi;
	}
	
	

}
