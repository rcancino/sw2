package com.luxsoft.siipap.model.core;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.luxsoft.sw3.replica.AuditUtils;

/**
 * Registro de auditoria para las modificaciones de clientes
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CLIENTES_AUDITLOG")
public class ClienteAuditLog {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ID")
	private Long id;
	
	@Version
	private int version;
	
	@Column(nullable=false,length = 40)
	private String entityName;
	
	@Column(nullable=false,length = 255)
	private String entityId;
	
	@Column(nullable=false,length=40)
	private String action;
	
	@Lob
	@Column
	private String message;
	
	@Column(nullable=false,length=50)
	private String tableName;
	
	@Column(nullable=false,length=50,name="SUCURSAL_ORIGEN")
	private String sucursalOrigen;
	
	@Column(nullable=true,length=50,name="SUCURSAL_DESTINO")
	private String sucursalDestino;
	
	@Column(nullable=false,length=100,name="USUARIO")
	private String usuario;
	
	
	@Column(nullable=false,length=100,name="ORIGEN")
	private String origen;
	
	@Column(nullable=true,length=50)
	private String ip;
	
	@Column(nullable=false)
	private Date	dateCreated=new Date();

	private Date	lastUpdated;
	
	private Date replicado;
	
	

	public ClienteAuditLog() {
		
	}
	
	public ClienteAuditLog(Object entity, Serializable id,String action,String ip,String sucursalOrigen) {
		this.entityName = ClassUtils.getShortClassName(entity.getClass());
		this.entityId = id.toString();
		this.tableName=AuditUtils.getTableName(entityName);
		this.action=action;
		this.lastUpdated=new Date();
		this.ip=ip;
		this.sucursalOrigen=sucursalOrigen;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
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

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
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

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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

	public Date getReplicado() {
		return replicado;
	}

	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}
	
	
	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	
	

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.MULTI_LINE_STYLE);
		
	}

}
