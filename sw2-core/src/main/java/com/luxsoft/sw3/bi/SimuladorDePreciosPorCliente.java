package com.luxsoft.sw3.bi;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.sw3.model.AdressLog;

/**
 * 
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_SIMULADOR_PRECIOS")
public class SimuladorDePreciosPorCliente extends BaseBean {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="CLIENTE_ID",nullable=false,updatable=false,unique=true)
	private Cliente cliente;
	
	@Column(name="FECHA_INI",nullable=false)
	private Date fechaInicial;
	
	@Column(name="FECHA_FIN",nullable=false)
	private Date fechaFinal;
	
	
	@Column(name="DESCUENTO_FIJO")
	private double descuentoFijo=0;
	
	@Column(name="DESCUENTO")
	private double descuento=0;
	
	@Column(name="MARGEN_MINIMO")
	private double margenMinimo=8.0;
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO_PRECIO", nullable = false, length = 25)
	private TipoPrecio tipoPrecio=TipoPrecio.CREDITO; 
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO_COSTO", nullable = false, length = 25)
	private TipoCosto tipoCosto=TipoCosto.COSTO_PROMEDIO; 
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.EAGER,mappedBy="lista")	
	private Set<SimuladorDePreciosPorClienteDet> precios=new HashSet<SimuladorDePreciosPorClienteDet>();
	
	public SimuladorDePreciosPorCliente(){
		Periodo p=Periodo.periodoDeloquevaDelYear();
		fechaInicial=p.getFechaInicial();
		fechaFinal=p.getFechaFinal();
	}
	
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
	

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		Object old=this.cliente;
		this.cliente = cliente;
		firePropertyChange("cliente", old, cliente);
	}

	public Date getFechaInicial() {
		return fechaInicial;
	}
	
	public void setFechaInicial(Date fechaInicial) {
		Object old=this.fechaInicial;
		this.fechaInicial = fechaInicial;
		firePropertyChange("fechaInicial", old, fechaInicial);
	}
	
	public Date getFechaFinal() {
		return fechaFinal;
	}
	
	public void setFechaFinal(Date fechaFinal) {
		Object old=this.fechaFinal;
		this.fechaFinal = fechaFinal;
		firePropertyChange("fechaFinal", old, fechaFinal);
	}
	
	
	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		double old=this.descuento;
		this.descuento = descuento;
		firePropertyChange("descuento", old, descuento);
	}
	

	public double getDescuentoFijo() {
		return descuentoFijo;
	}

	public void setDescuentoFijo(double descuentoFijo) {
		double old=this.descuentoFijo;
		this.descuentoFijo = descuentoFijo;
		firePropertyChange("descuentoFijo", old, descuentoFijo);
	}

	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	

	public Set<SimuladorDePreciosPorClienteDet> getPrecios() {
		return precios;
	}
	
	public boolean agregarPartida(SimuladorDePreciosPorClienteDet det){
		det.setLista(this);
		return getPrecios().add(det);
		
	}
	

	public TipoPrecio getTipoPrecio() {
		return tipoPrecio;
	}

	public void setTipoPrecio(TipoPrecio tipoPrecio) {
		Object old=this.tipoPrecio;
		this.tipoPrecio = tipoPrecio;
		firePropertyChange("tipoPrecio", old, tipoPrecio);
	}

	public TipoCosto getTipoCosto() {
		return tipoCosto;
	}

	public void setTipoCosto(TipoCosto tipoCosto) {
		Object old=this.tipoCosto;
		this.tipoCosto = tipoCosto;
		firePropertyChange("tipoCosto", old, tipoCosto);
	}

	public AdressLog getAddresLog() {
		if(addresLog==null){
			addresLog=new AdressLog();
		}
		return addresLog;
	}

	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}

	public UserLog getLog() {
		if(log==null)
			log=new UserLog();
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	

	public double getMargenMinimo() {
		return margenMinimo;
	}

	public void setMargenMinimo(double margenMinimo) {
		this.margenMinimo = margenMinimo;
	}

	public boolean agregarPrecio(final SimuladorDePreciosPorClienteDet det){
		det.setLista(this);
		return precios.add(det);
	}
	public boolean removerPrecio(final SimuladorDePreciosPorClienteDet det){
		det.setLista(null);
		return precios.remove(det);
	}
	public void eliminarPrecios(){
		precios.clear();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,79)
		.append(getId())
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj==null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimuladorDePreciosPorCliente other = (SimuladorDePreciosPorCliente) obj;
		return new EqualsBuilder()
		.append(getId(), other.getId())
		.isEquals();
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(id)		
		.append(cliente)
		.append(fechaInicial)
		.append(fechaInicial)
		.append(comentario)
		.toString();
	}
	
	public enum TipoPrecio{
		CREDITO,CONTADO
	}
	
	public enum TipoCosto{
		COSTO_PROMEDIO
		,COSTO_ULTIMO
		,COSTO_REPO
	}

}
