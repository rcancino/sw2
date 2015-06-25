package com.luxsoft.sw3.replica;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="AUDIT_LOG")
/*@GenericGenerator(name="hibernate-uuid",strategy="uuid"
,parameters={
		@Parameter(name="separator",value="-")
	}
)*/
public class AuditLog implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//@Id @GeneratedValue(generator="hibernate-uuid")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ID")
	private Long id;
	
	@Version
	private int version;
	
	@Column(nullable=false,length = 40)
	private String entityName;
	
	@Column(nullable=false,length = 255)
	private String entityId;
	
	@Column(nullable=false,length=20)
	private String action;
	
	@Lob
	@Column
	private String message;
	
	@Column(nullable=false,length=50)
	private String tableName;
	
	//@Column(nullable=true)
	//private Long sucursalId;
	
	@Column(nullable=false,length=50,name="SUCURSAL_ORIGEN")
	private String sucursalOrigen;
	
	@Column(nullable=true,length=50,name="SUCURSAL_DESTINO")
	private String sucursalDestino;
	
	@Column(nullable=true,length=50)
	private String ip;
	
	@Column(nullable=false)
	private Date	dateCreated=new Date();

	private Date	lastUpdated;
	
	private Date replicado;
	

	public AuditLog() {
		
	}

	public AuditLog(Object entity, Serializable id,String action,String ip,String sucursalOrigen,String sucursalDestino) {
		
		this.entityName = ClassUtils.getShortClassName(entity.getClass());
		this.entityId = id.toString();
		this.tableName=AuditUtils.getTableName(entityName);
		this.action=action;
		this.lastUpdated=new Date();
		this.ip=ip;
		this.sucursalOrigen=sucursalOrigen;
		this.sucursalDestino=sucursalDestino;
		
	}

	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	public String getSucursalOrigen() {
		return sucursalOrigen;
	}

	public void setSucursalOrigen(String sucursalOrigen) {
		this.sucursalOrigen = sucursalOrigen;
	}

	public String getSucursalDestino() {
		return sucursalDestino;
	}

	public void setSucursalDestino(String sucursalDestino) {
		this.sucursalDestino = sucursalDestino;
	}

	/*
	public Long getSucursalId() {
		return sucursalId;
	}

	public void setSucursalId(Long sucursalId) {
		this.sucursalId = sucursalId;
	}
*/
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	
	
	public Date getReplicado() {
		return replicado;
	}



	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}



	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.MULTI_LINE_STYLE);
		/*return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(id)
		.append(entityName)
		.append(this.action)
		.append(this.tableName)
		.toString();
		*/
		
	}
	
	@Override
	public boolean equals(Object o) {
		 if(o==null) return false;
	     if(o==this) return true;
	     if(this.getClass()!=o.getClass()) return false;
	    AuditLog otro=(AuditLog)o;
	    return new EqualsBuilder()
	    .append(this.id, otro.getId())
	    .append(this.entityId, otro.getEntityId())
	    .append(this.entityName, otro.getEntityName())
	    .append(this.sucursalDestino, otro.sucursalDestino)
	    .isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(this.id)
		.append(this.entityId)
		.append(this.entityName)
		.append(this.dateCreated)
		.toHashCode();
	}

}
