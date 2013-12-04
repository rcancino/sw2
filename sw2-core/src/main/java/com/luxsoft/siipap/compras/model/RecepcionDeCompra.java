package com.luxsoft.siipap.compras.model;

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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Mesatro para el control de la recepcion de compra
 * 
 * @author Ruben Cancino Ramos
 * 
 */
@Entity
@Table(name = "SX_ENTRADA_COMPRAS")
@GenericGenerator(name = "hibernate-uuid", strategy = "uuid", parameters = { @Parameter(name = "separator", value = "-") })
public class RecepcionDeCompra extends BaseBean implements Replicable{

	@Id
	@GeneratedValue(generator = "hibernate-uuid")
	@Column(name = "ID")
	protected String id;

	@Version
	private int version;
	
	@Column(name="DOCUMENTO")
	private Long documento;
	
	@ManyToOne (optional=true,fetch=FetchType.EAGER)
	@JoinColumn (name="SUCURSAL_ID", nullable=true, updatable=true)
	@NotNull
	private Sucursal sucursal;	

	@ManyToOne(optional = false)
	@JoinColumn(name = "COMPRA_ID", nullable = false, updatable = false)
	@NotNull
	private Compra2 compra;
	
	@Column(name="REMISION",length=25)
	@Length(max = 25) @NotEmpty(message="La remisión/factura es mandatoria")
	private String remision;

	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	@NotNull
	private Date fecha;

	@Column(name = "COMENTARIO")
	@Length(max = 255)
	private String comentario;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "recepcion")
	@Fetch(FetchMode.SUBSELECT)	
	@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	private Set<EntradaPorCompra> partidas = new HashSet<EntradaPorCompra>();

	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "createUser", column = @Column(name = "CREADO_USR", nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "updateUser", column = @Column(name = "MODIFICADO_USR", nullable = true, insertable = true, updatable = true)),
			@AttributeOverride(name = "creado",		column = @Column(name = "CREADO", nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "modificado", column = @Column(name = "MODIFICADO", nullable = true, insertable = true, updatable = true)) }
	)
	private UserLog log = new UserLog();
	

	

	

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

	public Compra2 getCompra() {
		return compra;
	}

	public void setCompra(Compra2 compra) {
		Object old=this.compra;
		this.compra = compra;
		firePropertyChange("compra", old, compra);
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}
	
	

	public String getRemision() {
		return remision;
	}

	public void setRemision(String remision) {
		Object old=this.remision;
		this.remision = remision;
		firePropertyChange("remision", old, remision);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public Set<EntradaPorCompra> getPartidas() {
		return partidas;
	}

	public void registrarEntrada(EntradaPorCompra e){
		e.setRecepcion(this);
		partidas.add(e);
	}
	
	

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public String getId() {
		return id;
	}

	public int getVersion() {
		return version;
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
		RecepcionDeCompra other = (RecepcionDeCompra) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
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
