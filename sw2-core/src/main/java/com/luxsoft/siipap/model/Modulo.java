package com.luxsoft.siipap.model;

import java.util.Properties;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;





/**
 * Encapsula información relacionada con un modulo del sistema
 * 
 * TODO Considerar si esta clase puede tener metodos para generar
 * estructura incial de los modulos como access rights, configuracion
 * de estructura de base de datos etc. Pensar en posibles implementaciones
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table (name="SW_MODULOS")
public class Modulo {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Column(name="CLAVE",length=20,nullable=false,unique=true)
	private String clave;
	
	@Column(name="DESCRIPCION",nullable=true)
	private String descripcion;
	
	@Column(name="PACKAGE",nullable=true)
	private String packageName;
	
	@Embedded
	private UserLog userLog=new UserLog();
	
	/**
	 * Lista de propiedades especificas para el modulo,No se cargan
	 * de la base de datos, sino de un archivo de propiedades generalmente
	 * procesado por maven par instalar los datos mas recientes del proceso de build
	 */
	@Transient
	private Properties propiedades;
	
	@Transient
	protected ModuloPropertiesReader reader;
	
	/**
	 * Public constructor solo para Hibernate/Spring
	 *
	 */
	public Modulo(){	}

	public Modulo(String clave, String descripcion) {		
		this.clave = clave;
		this.descripcion = descripcion;
	}
	
	public Long getId() {
		return id;
	}

	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}

	/**
	 * Descripcion del modulo
	 * 
	 * @return
	 */
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * Propiedades particulares de un modulo. Relacionadas con
	 * La version,nombre del jar etc. En general toda esta informacion
	 * es registrada por maven y es instalada en un archivo de solo lectura
	 * dentro del jar del modulo. 
	 * 
	 * @return
	 */
	public Properties getPropiedades() {
		if(propiedades==null){
			propiedades=new Properties();
		}
		return propiedades;
	}
	

	public UserLog getUserLog() {
		return userLog;
	}
	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
	}

	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	
	

}
