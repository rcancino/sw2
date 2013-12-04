/*
 *  Copyright 2008 Ruben Cancino Ramos <rcancino@luxsoftnet.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package com.luxsoft.siipap.compras.model;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.MonedasUtils;


/**
 *
 * @author Ruben Cancino Ramos <rcancino@luxsoftnet.com>
 */
@Entity
@Table(name="SX_COMPRAS"
	//,uniqueConstraints=@UniqueConstraint(columnNames={"SUCURSAL_ID","TIPO","DOCTO","ORIGEN"})
	)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TIPO",discriminatorType=DiscriminatorType.STRING,length=10)
@DiscriminatorValue("NACIONAL")
public class Compra extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="COMPRA_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne (optional=false
			,cascade={CascadeType.PERSIST,CascadeType.MERGE}
			,fetch=FetchType.LAZY)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false, updatable=false)
	@NotNull
	private Proveedor proveedor;
	
	@Column(name="CLAVE",nullable=false,length=4)
	private String clave;
	
	@ManyToOne (optional=true,fetch=FetchType.LAZY)
	@JoinColumn (name="SUCURSAL_ID", nullable=true, updatable=true)
	@NotNull
	private Sucursal sucursal;	
	
	@Column (name="FECHA",nullable=false)
	@Type (type="date")
	@NotNull
	private Date fecha=new Date();	
	
	@Column (name="MONEDA",length=3)
	private Currency moneda=MonedasUtils.PESOS;	
	
	@Column (name="TC",scale=4,precision=12)
	private BigDecimal tc=BigDecimal.ONE; 
	
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.EAGER,mappedBy="compra")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<CompraDet> partidas=new HashSet<CompraDet>();
	
	//@Column(name="DOCTO",length=15)	@Length(max=15)
	@Transient
	private String documento;
	
	//@Enumerated(EnumType.STRING)
	//@Column(name = "TIPOPED", nullable = false, length = 15)
	@Transient
    private TipoPedido tipo=TipoPedido.NORMAL;
    
    @Column(name="COMENTARIO_REC")
    @Transient
	private String comentarioRecepcion;
    
    /** Propiedades para la compatibilidad  con SIIPAP DBF**/
    
    private Integer folio;
    
    
    /*@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })*/
    @Transient
	private UserLog log=new UserLog();
	
		
	public Compra(){
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
	
	public Proveedor getProveedor() {
		return proveedor;
	}
	public void setProveedor(Proveedor proveedor) {
		Object old=this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
		this.clave=proveedor.getClave();
	}
	
	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	
	
	public Integer getFolio() {
		return folio;
	}

	public void setFolio(Integer folio) {
		this.folio = folio;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Currency getMoneda() {
		return moneda;
	}
	public void setMoneda(Currency moneda) {
		Object old=this.moneda;
		this.moneda = moneda;
		firePropertyChange("moneda", old, moneda);
	}
	
	public BigDecimal getTc() {
		return tc;
	}
	public void setTc(BigDecimal tc) {
		Object old=this.tc;
		this.tc = tc;
		firePropertyChange("tc", old, tc);
	}
	
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}	

	public TipoPedido getTipo() {
		return tipo;
	}

	public void setTipo(TipoPedido tipo) {
		this.tipo = tipo;
	}

	public String getComentarioRecepcion() {
		return comentarioRecepcion;
	}

	public void setComentarioRecepcion(String comentarioRecepcion) {
		this.comentarioRecepcion = comentarioRecepcion;
	}
	
	

	/**
	 * TODO Probablemente este deba ser un campo dinamico tomado con 
	 * las partidas de la compra. Para que la compra este depurada
	 * todas sus partidas deben estar depuradas
	 * 
	 * @return
	
	public boolean isDepurada() {
		return depurada;
	}

	public void setDepurada(boolean depurada) {		
		this.depurada = depurada;
		this.pendiente=!depurada;
	}
 */
	
	public UserLog getLog() {
		return log;
	}
	public void setLog(UserLog log) {
		this.log = log;
	}
	
	/**
	 * Depura todas las partidas de la compra
	 * 
	 */
	public void depurar(){
		for(CompraDet det:partidas){
			det.setDepurada(true);
		}
	}
	
	/**
	 * Actualiza el estado importante de la compra
	 * y sus partidas, particularmente actualiza las
	 * entradas
	 * 
	 */
	public void actualizar(){
		for(CompraDet det:partidas){
			det.actualizarEntradas();
		}
	}
	
	public CompraDet getPartida(int renglon){
		for(CompraDet det:partidas){
			if(det.getRenglon()==renglon)
				return det;
		}
		return null;
	}
	
	/** Propiedades dinamicas***/
	
	/**
	 * Determina si el total de la compra esta depurada
	 * 
	 * Con una partida que no este depurada toda la
	 * compra no esta depurada
	 * 
	 */
	public boolean isDepurada(){
		for(CompraDet det:partidas){
			if(!det.isDepurada())
				return false;
		}
		return true;
	}
	
	
	
	/**
	 * Determina si por lo menos una partida de la compra
	 * esta pendiente de entrga
	 * 
	 * @return
	 */
	public boolean isPendiente(){
		for(CompraDet det:partidas){
			if(det.isPendiente())
				return true;
		}
		return false;
	}
	
	/**
	 * Encapsula la logica para decidir si por su estado y vinculacion
	 * con otras entidades esta compra es modificable
	 * 
	 * @return
	 */
	public boolean isModificable(){
		if(getId()==null)
			return true;
		else{
			return isDepurada()?false:true;
		}
	}
	
	
	public CantidadMonetaria getImporte() {
		CantidadMonetaria val=new CantidadMonetaria(0,getMoneda() );
		for(CompraDet det:partidas){
			val=val.add(det.getImporte());
		}
		return val;
	}	
	
	public CantidadMonetaria getImpuesto() {
		return MonedasUtils.calcularImpuesto(getImporte());
	}
	
	public CantidadMonetaria getTotal() {
		CantidadMonetaria subtotal=getImporte()
							.add(getImpuesto());							
		return subtotal;
	}
	
	/** Manejo de partidas **/
	
	public Set<CompraDet> getPartidas() {
		return Collections.unmodifiableSet(partidas);
	}
	
	public void eliminarPartidas(){
		partidas.clear();
	}
	
	/**
	 * Agrega una Detalle de requisicion a la colleccion de partidas
	 * 
	 * @param det
	 * @return
	 */
	public boolean agregarPartida(final CompraDet det){		
		det.setCompra(this);
		return partidas.add(det);
	}
	
	/**
	 * Elimina una partida de requisicion de la coleccion de partidas
	 * 
	 * @param det
	 * @return
	 */
	public boolean eleiminarPartida(final CompraDet det){
		det.setCompra(null);
		return partidas.remove(det);
	}
	
	/** Utility methos **/
	
	
	/**
	 * Regresa la fecha probable de entrega en funcion de los dias
	 * indicados
	 *  
	 */
	public Date estimarFechaDeEntrega(){
		if(getProveedor()!=null){
			int dias=getProveedor().getTiempoDeEntrega();
			final Calendar c=Calendar.getInstance();
			c.setTime(getFecha());
			c.add(Calendar.DATE, dias);
			return c.getTime();
		}
		return null;
	}
	
	
	/*********/
	/**Un extraño bug impide que esta validacion opere correctamente
	 * ya que para cada llamada del metod save siempre la coleccion esta
	 * vacia la primera vez. Como si hibernate validara el estado antes de 
	 * que tenga datos y una con datos
	 * @AssertTrue(message="No se permiten compras sin partidas")
	public boolean validarPartidas(){
		System.out.println("PArtidas: "+getPartidas().size());
		return true;
		//return partidas.size()>0;
	}**/
	
	

	public int getMes(){
		return Periodo.obtenerMes(fecha)+1;
	}
	public int getYear(){
		return Periodo.obtenerYear(fecha);
	}

	
	
	
	/****  equals,hashCode toString implementation ****/
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(obj==this) return true;
		Compra otro=(Compra)obj;
		return new EqualsBuilder()		
		.append(getFecha(),otro.getFecha())
		.append(getProveedor(),otro.getProveedor())
		.append(getMoneda(),otro.getMoneda())
		.append(getTotal(),otro.getTotal())
		.append(getId(),otro.getId())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getFecha())
		.append(getProveedor())
		.append(getMoneda())
		.append(getTotal())
		.append(getId())
		.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(getId())
		.append(getClave())
		.append(getFolio())
		.append(getFecha())		
		.append(getTotal())
		.toString();
	}
	
	public static enum TipoPedido{
		NORMAL,ESPECIAL
	}

}
