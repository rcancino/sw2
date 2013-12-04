package com.luxsoft.sw3.maquila.model;

import java.math.BigDecimal;
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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;



/**
 * 
 * Analisis de gastos de maquilador. 
 * 
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MAQ_ANALISIS_GASTOS")
public class AnalisisDeGastos extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ANALISIS_ID")
	private Long id;
	
	@Version
	private int version;
	

	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	@NotNull
	private Date fecha=new Date();
	
	@Column(name = "FACTURA_FLETE", length = 20, nullable = true)
	@Length(max=20)
	private String facturaFlete;
	
	@Column(name = "FACTURA_MAQUILA", length = 20, nullable = true)
	@Length(max=20)
	private String facturaMaquilador;
	
	
	@Column(name = "FECHA_MAQUILA")
	@Type(type = "date")
	private Date fechaDoctoMaquila=new Date();
	
	@Column(name = "FECHA_FLETE" )
	@Type(type = "date")
	private Date fechaDoctoFlete=new Date();
	
	@Column(name="COMENTARIO")
	@Length(max=250)
	private String comentario;
	
	
	@Column (name="FLETE_IMPORTE",nullable=false)
	private BigDecimal importeFlete=BigDecimal.ZERO;
	
	@Column (name="FLETE_IMPUESTO",nullable=false)
	private BigDecimal impuestoFlete=BigDecimal.ZERO;
	
	@Column (name="FLETE_TOTAL",nullable=false)
	protected BigDecimal totalFlete=BigDecimal.ZERO;
	
	@Column (name="MAQUILA_IMPORTE",nullable=false)
	private BigDecimal importeMaquilador=BigDecimal.ZERO;
	
	@Column (name="MAQUILA_IMPUESTO",nullable=false)
	private BigDecimal impuestoMaquilador=BigDecimal.ZERO;
	
	@Column (name="MAQUILA_TOTAL",nullable=false)
	protected BigDecimal totalMaquilador=BigDecimal.ZERO;
	
	@OneToMany(mappedBy="analisis",fetch=FetchType.LAZY
			,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<EntradaDeMaquila> entradas=new HashSet<EntradaDeMaquila>();
	
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
	
	public Long getId() {
		return id;
	}
	
	public int getVersion() {
		return version;
	}	

	public String getFacturaFlete() {
		return facturaFlete;
	}

	public void setFacturaFlete(String facturaFlete) {
		Object old=this.facturaFlete;
		this.facturaFlete = facturaFlete;
		firePropertyChange("facturaFlete", old, facturaFlete);
	}

	public String getFacturaMaquilador() {
		return facturaMaquilador;
	}

	public void setFacturaMaquilador(String facturaMaquilador) {
		Object old=this.facturaMaquilador;
		this.facturaMaquilador = facturaMaquilador;
		firePropertyChange("facturaMaquilador", old, facturaMaquilador);
	}

	public Date getFechaDoctoMaquila() {
		return fechaDoctoMaquila;
	}

	public void setFechaDoctoMaquila(Date fechaDoctoMaquila) {
		Object old=this.fechaDoctoMaquila;
		this.fechaDoctoMaquila = fechaDoctoMaquila;
		firePropertyChange("fechaDoctoMaquila", old, fechaDoctoMaquila);
	}

	public Date getFechaDoctoFlete() {
		return fechaDoctoFlete;
	}

	public void setFechaDoctoFlete(Date fechaDoctoFlete) {
		Object old=this.fechaDoctoFlete;
		this.fechaDoctoFlete = fechaDoctoFlete;
		firePropertyChange("fechaDoctoFlete", old, fechaDoctoFlete);
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}	
	
	public BigDecimal getImporteMaquilador() {
		return importeMaquilador;
	}

	public void setImporteMaquilador(BigDecimal importeMaquilador) {
		Object old=this.importeMaquilador;
		this.importeMaquilador = importeMaquilador;
		firePropertyChange("importeMaquilador", old, importeMaquilador);
	}

	public BigDecimal getImpuestoMaquilador() {
		return impuestoMaquilador;
	}

	public void setImpuestoMaquilador(BigDecimal impuestoMaquilador) {
		Object old=this.impuestoMaquilador;
		this.impuestoMaquilador = impuestoMaquilador;
		firePropertyChange("impuestoMaquilador", old, impuestoMaquilador);
	}

	public BigDecimal getTotalMaquilador() {
		return totalMaquilador;
	}

	public void setTotalMaquilador(BigDecimal totalMaquilador) {
		Object old=this.totalMaquilador;
		this.totalMaquilador = totalMaquilador;
		firePropertyChange("totalMaquilador", old, totalMaquilador);
	}
	
	public BigDecimal getImporteFlete() {
		return importeFlete;
	}

	public void setImporteFlete(BigDecimal importeFlete) {
		Object old=this.importeFlete;
		this.importeFlete = importeFlete;
		firePropertyChange("importeFlete", old, importeFlete);
	}

	public BigDecimal getImpuestoFlete() {
		return impuestoFlete;
	}

	public void setImpuestoFlete(BigDecimal impuestoFlete) {
		Object old=this.impuestoFlete;
		this.impuestoFlete = impuestoFlete;
		firePropertyChange("impuestoFlete", old, impuestoFlete);
	}

	public BigDecimal getTotalFlete() {
		return totalFlete;
	}

	public void setTotalFlete(BigDecimal totalFlete) {
		Object old=this.totalFlete;
		this.totalFlete = totalFlete;
		firePropertyChange("totalFlete", old, totalFlete);
	}

	public Set<EntradaDeMaquila> getEntradas() {
		return entradas;
	}
	
	public boolean agregarEntrada(EntradaDeMaquila e){
		//e.setAnalisis(this);
		return entradas.add(e);
	}
	public boolean eliminarEntrada(EntradaDeMaquila e){
		//e.setAnalisis(null);
		return entradas.remove(e);
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
	
	/**
	 * Actualiza los costos de las entradas
	 * 
	 */
	public void actualizarCostos(){
		double totalKilos=0;
		
		for(EntradaDeMaquila e:entradas){
			totalKilos+=(e.getCantidad()/e.getFactor())*e.getProducto().getKilos();
		}
		
		double flete=getImporteFlete().doubleValue();
		double hojeo=getImporteMaquilador().doubleValue();
		
		//Asignando costo de proporcionales
		for(EntradaDeMaquila e:entradas){
			//Calculando participacion
			double kilos=e.getKilosCalculados();
			double participacion=kilos/totalKilos;
			
			double millares=e.getCantidad()/e.getFactor();
		
			//Flete
			double importeFlete=flete*participacion;			
			double costoFlete=importeFlete/millares;
			e.setCostoFlete(CantidadMonetaria.pesos(costoFlete).amount());
			
			//Hojeo
			double importeHojeado=hojeo*participacion;
			double costoHojeo=importeHojeado/millares;
			e.setCostoCorte(BigDecimal.valueOf(costoHojeo));
			
			//Total
			e.setCosto(e.getCostoMateria().add(e.getCostoFlete().add(e.getCostoCorte())));
			
		}
		
	}

	public boolean equals(Object obj){
    	if(obj==null) return false;
    	if(obj==this) return true;
    	if(getClass()!=obj.getClass()) return false;
    	AnalisisDeGastos other=(AnalisisDeGastos)obj;
    	return new EqualsBuilder()
    	.append(id, other.getId())
    	.isEquals();
    }
	
    public int hashCode(){
    	return new HashCodeBuilder(27,93)
    	.append(id)
    	.toHashCode();
    }
    
    public String toString(){
    	return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
    	.append(id)
    	.append(fecha)
    	.toString();
    }
	

}
