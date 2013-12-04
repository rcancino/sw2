package com.luxsoft.sw3.caja;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;

@Entity
@Table(name = "SX_GASTOS")
@GenericGenerator(name = "hibernate-uuid", strategy = "uuid", parameters = { @Parameter(name = "separator", value = "-") })
public class Gasto {

	@Id
	@GeneratedValue(generator = "hibernate-uuid")
	@Column(name = "ID")
	protected String id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "SUCURSAL_ID", nullable = false, updatable = false)
	private Sucursal sucursal;
	
	@Transient
	private ProductoServicio productoServicio;
	
	private Long folio;
	
	@Column(name="PROD_ID",nullable=false)
	private Long productoId=0L;

	@Column(name="DESCRIPCION",nullable=false)
	private String descripcion;
	
	@Column(name="DOCUMENTO")
	private String documento;

	@Column(name="COMENTARIO")
	private String comentario;

	@Column(name = "IMPORTE")
	private BigDecimal importe = BigDecimal.ZERO;

	@Column(name = "REMBOLSO", nullable = true)
	@Type(type = "date")
	private Date rembolso;

	@Column(name = "SOLICITUD", nullable = true)
	@Type(type = "date")
	private Date solicitud;

	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "createUser", column = @Column(name = "CREADO_USR", nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "updateUser", column = @Column(name = "MODIFICADO_USR", nullable = true, insertable = true, updatable = true)),
			@AttributeOverride(name = "creado", column = @Column(name = "CREADO", nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "modificado", column = @Column(name = "MODIFICADO", nullable = true, insertable = true, updatable = true)) })
	private UserLog log = new UserLog();

	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "createdIp", column = @Column(nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "updatedIp", column = @Column(nullable = true, insertable = true, updatable = true)),
			@AttributeOverride(name = "createdMac", column = @Column(nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "updatedMac", column = @Column(nullable = true, insertable = true, updatable = true)) })
	private AdressLog addresLog = new AdressLog();

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Long getFolio() {
		return folio;
	}

	public void setFolio(Long folio) {
		this.folio = folio;
	}

	public ProductoServicio getProductoServicio() {
		if(productoServicio!=null){
			productoServicio=new ProductoServicio(this.productoId,this.descripcion);
		}
		return productoServicio;
	}

	public void setProductoServicio(ProductoServicio productoServicio) {
		this.productoServicio = productoServicio;
		if(productoServicio!=null){
			setProductoId(productoServicio.getId());
			setDescripcion(productoServicio.getDescripcion());
		}
	}

	public Long getProductoId() {
		return productoId;
	}

	public void setProductoId(Long productoId) {
		this.productoId = productoId;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public Date getRembolso() {
		return rembolso;
	}

	public void setRembolso(Date rembolso) {
		this.rembolso = rembolso;
	}

	public Date getSolicitud() {
		return solicitud;
	}

	public void setSolicitud(Date solicitud) {
		this.solicitud = solicitud;
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
		Gasto other = (Gasto) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append(
				getDescripcion()).append(getDocumento()).append(getImporte())
				.append(
						String.format(" Rembolso: %1$te/%1$tm/%1$tY: %1$tr",
								getRembolso())).toString();
	}

}
