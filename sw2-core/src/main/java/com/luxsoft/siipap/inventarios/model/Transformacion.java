package com.luxsoft.siipap.inventarios.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Transformacion de material
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_TRANSFORMACIONES")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class Transformacion extends BaseBean {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="TRANSFORMACION_ID")
	protected String id;
	
	@Version
	private int version;
	
	@Enumerated(EnumType.STRING)
	@Column (name="CLASE",nullable=false, length=20)
	@NotNull
	private Clase clase=Clase.Reclasificacion;
	
	@Column(name="FECHA",nullable=false)
	@NotNull
	private Date fecha=new Date();
	
	@ManyToOne (optional=false)
    @JoinColumn (name="SUCURSAL_ID", nullable=false,updatable=false)
    @NotNull
	private Sucursal sucursal;
	
	@Column(name="COMENTARIO",length=255)
	private String comentario;
	
	@Column(name="GASTO")
	private BigDecimal gastos=BigDecimal.ZERO;
	
	@Column(name="DOCUMENTO")
	private Long documento;
	
	
	
	@Column(name="PORINVENTARIO")
	private Boolean porInventario;
	
	@Embedded
	private UserLog log=new UserLog();
	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="transformacion")
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE
			}
	)
	@Fetch(value=FetchMode.SUBSELECT)
	private Set<TransformacionDet> partidas=new HashSet<TransformacionDet>();

	

	public String getId() {
		return id;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
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
	

	public BigDecimal getGastos() {
		return gastos;
	}

	public void setGastos(BigDecimal gastos) {
		this.gastos = gastos;
	}

	

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		Object old=this.documento;
		this.documento = documento;
		firePropertyChange("documento", old, documento);
	}

	public Clase getClase() {
		return clase;
	}

	public void setClase(Clase clase) {
		Object old=this.clase;
		this.clase = clase;
		firePropertyChange("clase", old, clase);
	}

	public Boolean getPorInventario() {
		return porInventario;
	}

	public void setPorInventario(Boolean porInventario) {
		Object old=this.porInventario;
		this.porInventario = porInventario;
		firePropertyChange("porInventario", old, porInventario);
	}

	public int getVersion() {
		return version;
	}

	
	

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public Set<TransformacionDet> getPartidas() {
		return partidas;
	}
	
	public void agregarTransformacion(TransformacionDet trs){
		trs.setTransformacion(this);
		partidas.add(trs);
	}

	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
		if(o==this) return true;
		if(getClass()!=o.getClass()) return false;
		Transformacion othre=(Transformacion)o;
		return new EqualsBuilder()
		.append(getId(), othre.getId())
		.isEquals();
	}

	

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getId())
		.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(id)
		.append(fecha)
		.append(comentario)
		.toString();
	}	

	
	
	
	public static enum Clase{
		
		/**
		 * Son lo que antes eran RAU,REC,REF 
		 * La raglas fundamentales son:
		 * 
		 * 	- El producto origen y destino deben ser de la misma linea
		 *  - La cantidad de origen debe ser igual a la cantidad destino
		 *  - No existen gastos asiciados
		 * 
		 */
		Reclasificacion 
		
		/**
		 * Son lo que antes eran TRS
		 * Las reglas fundamentales son:
		 * 
		 * 	- Tiene un gasto asociado
		 */
		,Transformacion
		
		/**
		 * Son lo que antes eran TRS
		 * Las reglas fundamentales son:
		 * 
		 * 	- Tiene un gasto asociado
		 *  - El destino debe pertenecer al a linea de Metalizado
		 * 
		 */
		,Metalizado
	}
}
