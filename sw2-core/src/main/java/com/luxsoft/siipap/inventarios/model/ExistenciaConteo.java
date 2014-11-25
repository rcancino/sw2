package com.luxsoft.siipap.inventarios.model;

import java.io.Serializable;
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
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Entidad para el conteo de inventario
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_EXISTENCIA_CONTEO")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
,parameters={
		@Parameter(name="separator",value="-")
	}
)
public class ExistenciaConteo implements Serializable{
	
	@Id  @GeneratedValue(generator="hibernate-uuid")
	@Column(name="EXISTENCIA_ID")
	private String id;
    
    @Version
    private int version;    
    
    @ManyToOne (optional=false)
    @JoinColumn (name="SUCURSAL_ID",updatable=false)
    private Sucursal sucursal;
    
    @Column (name="FECHA",nullable=false)
    @Type(type="date")
    private Date fecha;
    
    @ManyToOne (optional=false)
    @JoinColumn (name="PRODUCTO_ID", nullable=false,updatable=false) 
    @NotNull
    private Producto producto;    
    
    @Column(name="CLAVE",nullable=false)
    @NotNull  @Length(max=10)
    private String clave;
    
    @Column(name="DESCRIPCION",nullable=false)
    private String descripcion;
    
    @Column(name="UNIDAD",length=3,nullable=false)
	@Length(max=3,min=2)
	private String unidad;
    
    @Column(name="FACTORU",nullable=false)
	private double factor;
    
    @Column(name="CANTIDAD",nullable=false)
    private double existencia=0;
    
    @Column(name="CONTEO",nullable=false)
    private double conteo=0;
    
    @Formula("(select round(ifnull(sum(X.CANTIDAD),0),3) " +
    		" FROM SX_CONTEODET X JOIN SX_CONTEO C ON(C.CONTEO_ID=X.CONTEO_ID) " +
    		" where X.CLAVE=CLAVE " +
    		" AND C.SUCURSAL_ID=SUCURSAL_ID" +
    		" AND DATE(C.FECHA)=DATE(NOW())" +
    		")")	
    private double conteoEnLinea=0;
       
    @Column(name="DIFERENCIA",nullable=false)
    private double diferencia=0;
    
    @Column(name="AJUSTE",nullable=false)
    private double ajuste=0;
    
    @Column(name="EXISTENCIA_FINAL",nullable=false)
    private double existenciaFinal=0;
    
    @Column(name="SECTORES")
    private String sectores;
    
    @Formula("(select ifnull(max(C.SECTOR),0) " +
    		" FROM SX_CONTEODET X JOIN SX_CONTEO C ON(C.CONTEO_ID=X.CONTEO_ID) " +
    		" where X.CLAVE=CLAVE " +
    		" AND C.SUCURSAL_ID=SUCURSAL_ID" +
    		" AND DATE(C.FECHA)=DATE(NOW())" +
    		")")	
    private double sectorEnLinea=0;
    
    @Column (name="FIJADO")
    private Date fijado;
    
    @Column(name="EXISTENCIA_ORIGEN")
    private String existenciaOrigen;
    
    @Column(name="CONTEO_PARCIAL")
    private Boolean conteoParcial=false;
    
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

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
	    this.producto = producto;
	    if(producto!=null){
	    	this.clave=producto.getClave();
		    this.descripcion=producto.getDescripcion();
		    this.factor=producto.getUnidad().getFactor();
		    this.unidad=producto.getUnidad().getUnidad();
	    }else{
	    	this.clave=null;
		    this.descripcion=null;
		    this.factor=0;
	    }
	    
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	

	public String getUnidad() {
		return unidad;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public double getExistencia() {
		return existencia;
	}

	public void setExistencia(double existencia) {
		this.existencia = existencia;
	}

	public double getConteo() {
		return conteo;
	}
	
	

	public double getConteoEnLinea() {
		return conteoEnLinea;
	}

	public void setConteoEnLinea(double conteoEnLinea) {
		this.conteoEnLinea = conteoEnLinea;
	}

	public void setConteo(double conteo) {
		this.conteo = conteo;
	}

	public double getDiferencia() {
		return diferencia;
	}

	public void setDiferencia(double diferencia) {
		this.diferencia = diferencia;
	}

	public double getAjuste() {
		return ajuste;
	}

	public void setAjuste(double ajuste) {
		this.ajuste = ajuste;
	}

	public double getExistenciaFinal() {
		return existenciaFinal;
	}

	public void setExistenciaFinal(double existenciaFinal) {
		this.existenciaFinal = existenciaFinal;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public String getSectores() {
		return sectores;
	}

	public void setSectores(String sectores) {
		this.sectores = sectores;
	}	

	public Date getFijado() {
		return fijado;
	}

	public void setFijado(Date fijado) {
		this.fijado = fijado;
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
	

	public String getExistenciaOrigen() {
		return existenciaOrigen;
	}

	public void setExistenciaOrigen(String existenciaOrigen) {
		this.existenciaOrigen = existenciaOrigen;
	}
	
	

	public Boolean getConteoParcial() {
		return conteoParcial;
	}

	public void setConteoParcial(Boolean conteoParcial) {
		this.conteoParcial = conteoParcial;
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
		ExistenciaConteo other = (ExistenciaConteo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	} 


	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SIMPLE_STYLE);
	}
	
	public void actualizar(){
		if(getFijado()==null){
			setDiferencia(getConteo()-getExistencia());
			if(getProducto().getUnidad().getUnidad().equals("MIL")){
				double aju=Math.abs(getDiferencia());
				if(-30.00d<aju && aju<30.00d){
					setAjuste(getDiferencia());
				}else
					setAjuste(0.0d);
			}
			setExistenciaFinal(getExistencia()+getAjuste());
		}
	}

	public double getSectorEnLinea() {
		return sectorEnLinea;
	}

	public void setSectorEnLinea(double sectorEnLinea) {
		this.sectorEnLinea = sectorEnLinea;
	}
	
	
    
}
