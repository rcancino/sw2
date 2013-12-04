package com.luxsoft.sw3.contabilidad.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Entidad que representa las polizas de Contablidad
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table (name="SX_POLIZAS")
public class Poliza extends BaseBean{

	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="POLIZA_ID")
	private Long id;
	
	@Version
	private int version;
	
	@Column(name="FOLIO",nullable=false)
	private Long folio;
	
	@Column(name="CLASE",nullable=false,length=100)
	@NotNull
	private String clase;
	
	@Column(name="REFERENCIA",nullable=false,length=50)
	@NotNull
	@Length(max=50)
	private String referencia;
	
	@Column(name="DESCRIPCION",nullable=false)
	@NotNull
	@Length(max=255)
	private String descripcion;
	
	@Column(name="FECHA",nullable=false)
	@NotNull
	private Date fecha=new Date();
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch= FetchType.LAZY			
				)
	@JoinColumn(name="POLIZA_ID",nullable=false)
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	@IndexColumn(name="RENGLON",base=1)
	private List<PolizaDet> partidas=new ArrayList<PolizaDet>(); 
	
	@Column (name="DEBE",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal debe=BigDecimal.ZERO;
	
	@Column (name="HABER",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal haber=BigDecimal.ZERO;
	
    
    @Enumerated(EnumType.STRING)
	@Column(name = "TIPO_ID", nullable = false,length=20)
	@NotNull
	private Tipo tipo;
	
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
	       @AttributeOverride(name="createdIp",	column=@Column(name="CREADO_IP" ,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(name="MODIFICADO_IP",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(name="CREADO_MAC",nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(name="MODIFICADO_MAC",nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getFolio() {
		return folio;
	}

	public void setFolio(Long folio) {
		this.folio = folio;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		Object old=this.descripcion;
		this.descripcion = descripcion;
		firePropertyChange("descripcion", old, descripcion);
	}

	public Date getFecha() {
		return fecha;
	}
	
	public String getFechaAsString(){
		return DateUtil.convertDateToString(getFecha());
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	public BigDecimal getDebe() {
		return debe;
	}

	public void setDebe(BigDecimal debe) {
		Object old=this.debe;
		this.debe = debe;
		firePropertyChange("debe", old, debe);
		firePropertyChange("cuadre",null,getCuadre());
	}

	public BigDecimal getHaber() {
		return haber;
	}

	public void setHaber(BigDecimal haber) {
		Object old=this.haber;
		this.haber = haber;
		firePropertyChange("haber", old, haber);
		firePropertyChange("cuadre",null,getCuadre());
		
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		Object old=this.tipo;
		this.tipo = tipo;
		firePropertyChange("tipo", old, tipo);
	}

	public int getVersion() {
		return version;
	}

	public List<PolizaDet> getPartidas() {
		return partidas;
	}
	
	public PolizaDet agregarPartida(){
		PolizaDet det=new PolizaDet();
		det.setPoliza(this);
		partidas.add(det);
		return det;
	}
	
	public PolizaDet agregarPartida(PolizaDet det){
		det.setPoliza(this);
		partidas.add(det);
		return det;
	}
	
	public int getYear(){
		return Periodo.obtenerYear(getFecha());
	}
	public int getMes(){
		return Periodo.obtenerMes(getFecha())+1;
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
	
	public void actualizar(){
		clean();
		CantidadMonetaria debe=CantidadMonetaria.pesos(0);
		CantidadMonetaria haber=CantidadMonetaria.pesos(0);
		for(PolizaDet det:getPartidas()){
			debe=debe.add(det.getDebeCM());
			haber=haber.add(det.getHaberCM());
		}
		setDebe(debe.amount());
		setHaber(haber.amount());		
	}
	
	public void clean(){
		Iterator<PolizaDet> it=getPartidas().iterator();
		while(it.hasNext()){
			PolizaDet det=it.next();
			//System.out.println("Debe: "+det.getDebe().doubleValue()+"  "+"Haber: "+det.getHaber().doubleValue()+ "Zero: "+BigDecimal.ZERO+ " a2: "+new BigDecimal(0d));
			//BigDecimal zero=new BigDecimal(0.0).setScale(getDe)
			if(det.getDebe().doubleValue()==0 && det.getHaber().doubleValue()==0)
				it.remove();
		}
	}
	
	public BigDecimal getCuadre(){
		return getDebe().subtract(getHaber(),MCTX);
	}
	
	public static MathContext MCTX=new MathContext(4,RoundingMode.HALF_EVEN);

	
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append("Tipo ",getTipo())
		.append("Clase: "+getClase())
		.append("Referencia: "+getReferencia())
		.append("Fecha: "+getFechaAsString())
		.append("Folio: ",getFolio())
		.append("Desc ", getDescripcion())
		.toString();
	}

	public static enum Tipo{
		INGRESO,EGRESO,DIARIO;
		
		public String getNombre(){
			return name();
		}
	}

	public String getClase() {
		return clase;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)		
		.append(getClase())
		.append(getReferencia())
		//.append(getFolio())
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(obj==this) return true;
		if(getClass()!=obj.getClass()) return false;
		Poliza other = (Poliza) obj;
		return new EqualsBuilder()		
		.append(getClase(),other.getClase())
		.append(getReferencia(), other.getReferencia())
		//.append(getFolio(),other.getFolio())
		.isEquals();
	}

	public void setClase(String clase) {
		this.clase = clase;
	}
	
	

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}
	

	@Transient
	public int ultimoRenglon=0;

}
