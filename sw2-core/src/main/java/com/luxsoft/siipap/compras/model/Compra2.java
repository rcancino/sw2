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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
import com.luxsoft.sw3.replica.Replicable;


/**
 *
 * @author Ruben Cancino Ramos <rcancino@luxsoftnet.com>
 */
@Entity
@Table(name="SX_COMPRAS2"
	//,uniqueConstraints=@UniqueConstraint(columnNames={"SUCURSAL_ID","TIPO","DOCTO","ORIGEN"})
	)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TIPO",discriminatorType=DiscriminatorType.STRING,length=10)
@DiscriminatorValue("NACIONAL")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class Compra2 extends BaseBean implements Replicable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="COMPRA_ID")
	protected String id;
	
	@Version
	private int version;
	
	@ManyToOne (optional=false,fetch=FetchType.LAZY)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false, updatable=false)
	@NotNull
	private Proveedor proveedor;
	
	@Column(name="CLAVE",nullable=false,length=4)
	private String clave;
	
	@Column (name="NOMBRE",nullable=false,length=250)
	private String nombre;
	
	@ManyToOne (optional=true,fetch=FetchType.EAGER)
	@JoinColumn (name="SUCURSAL_ID", nullable=true, updatable=true)
	@NotNull
	private Sucursal sucursal;	
	
	@Column (name="FECHA",nullable=false)
	@Type (type="date")
	@NotNull
	private Date fecha;	
	
	@Column (name="ENTREGA",nullable=false)	
	@Type (type="date")
	private Date entrega;	
	
	@Column (name="MONEDA",length=3)
	private Currency moneda=MonedasUtils.PESOS;	
	
	@Column (name="TC")
	private double tc=1d; 
	
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@Column(name="DOCTO",length=15)
	@Length(max=15)
	private String documento;	
	
    @Column(name="COMENTARIO_REC")
	private String comentarioRecepcion;
    
    @Column(name="FOLIO",nullable=false,columnDefinition=" BIGINT(20) default 0")
	private Long folio=-1L;
    
    @Column(name="CIERRE",nullable=true)
    private Date cierre;
    
    @Column(name="DEPURACION",nullable=true)
    private Date depuracion;
    
    @Column(name="PENDIENTE",nullable=false)
    private double pendiente=0;
    
    @Column (name="IMPORTE_BRUTO",nullable=false,scale=2,precision=16)
	private BigDecimal importeBruto=BigDecimal.ZERO;
	
	@Column (name="IMPORTE_DESC",nullable=false,scale=2,precision=16)
	private BigDecimal importeDescuento=BigDecimal.ZERO;

	@Column (name="IMPORTE_NETO",nullable=false,scale=6,precision=16)
	private BigDecimal importeNeto=BigDecimal.ZERO;
	
	@Column (name="SUBTOTAL",nullable=false,scale=6,precision=16)
	private BigDecimal subTotal=BigDecimal.ZERO;
	
	@Column (name="IMPUESTOS",nullable=false,scale=6,precision=16)
	private BigDecimal impuestos=BigDecimal.ZERO;
	
	@Column (name="TOTAL",nullable=false,scale=6,precision=16)
	private BigDecimal total=BigDecimal.ZERO;
    
    @OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			,CascadeType.REFRESH
			}
			,fetch=FetchType.LAZY,mappedBy="compra")
	//@Fetch(FetchMode.SUBSELECT)
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE
			,org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	private Set<CompraUnitaria> partidas=new HashSet<CompraUnitaria>();
    
   @Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
    
    @Column(name="CONSOLIDADA")
    private Boolean consolidada=Boolean.FALSE;
	
    @Column(name="IMPORTACION")
    private boolean importacion=false;
		
    @Column(name="ESPECIAL")
    private boolean especial=false;
    
    @Column(name="DSCTO_ESPECIAL")
    private double descuentoEspecial=0;
    
    
    
	public Compra2(){
	}
	
	public String getId() {
		return id;
	}
	
	public int getVersion() {
		return version;
	}
	
	
	public Proveedor getProveedor() {
		return proveedor;
	}
	public void setProveedor(Proveedor proveedor) {
		Object old=this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
		if(proveedor!=null){
			this.clave=proveedor.getClave();
			this.nombre=proveedor.getNombre();
		}else{
			this.clave=null;
			this.nombre=null;
		}
		
	}
	
	public String getClave() {
		return clave;
	}

	public String getNombre() {
		return nombre;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		Object old=this.sucursal;
		this.sucursal = sucursal;
		firePropertyChange("sucursal", old, sucursal);
	}

	public Long getFolio() {
		return folio;
	}

	public void setFolio(Long folio) {
		this.folio = folio;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getCierre() {
		return cierre;
	}
	

	public Date getEntrega() {
		return entrega;
	}

	public void setEntrega(Date entrega) {
		this.entrega = entrega;
	}

	public void setCierre(Date cierre) {
		this.cierre = cierre;
	}	
	
	public Date getDepuracion() {
		return depuracion;
	}

	public void setDepuracion(Date depuracion) {
		this.depuracion = depuracion;
	}

	public Currency getMoneda() {
		return moneda;
	}
	public void setMoneda(Currency moneda) {
		Object old=this.moneda;
		this.moneda = moneda;
		firePropertyChange("moneda", old, moneda);
	}
	
	public double getTc() {
		return tc;
	}
	public void setTc(double tc) {
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
	

	public String getComentarioRecepcion() {
		return comentarioRecepcion;
	}

	public void setComentarioRecepcion(String comentarioRecepcion) {
		this.comentarioRecepcion = comentarioRecepcion;
	}	
	
	
	public BigDecimal getImporteBruto() {
		return importeBruto;
	}

	public void setImporteBruto(BigDecimal importeBruto) {
		this.importeBruto = importeBruto;
	}

	public BigDecimal getImporteDescuento() {
		return importeDescuento;
	}

	public void setImporteDescuento(BigDecimal importeDescuento) {
		this.importeDescuento = importeDescuento;
	}

	public BigDecimal getImporteNeto() {
		return importeNeto;
	}

	public void setImporteNeto(BigDecimal importeNeto) {
		this.importeNeto = importeNeto;
	}

	public BigDecimal getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(BigDecimal subTotal) {
		this.subTotal = subTotal;
	}

	public BigDecimal getImpuestos() {
		return impuestos;
	}

	public void setImpuestos(BigDecimal impuestos) {
		this.impuestos = impuestos;
	}

	public BigDecimal getTotal() {
		return total;
	}
	public CantidadMonetaria getTotalCM(){
		return new CantidadMonetaria(getTotal(),getMoneda());
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public boolean isDepurada(){
		return depuracion!=null;
	}
	
	
	
	public double getPendiente() {
		return pendiente;
	}

	public void setPendiente(double pendiente) {
		this.pendiente = pendiente;
	}

	public UserLog getLog() {
		return log;
	}
	public void setLog(UserLog log) {
		this.log = log;
	}
	
	/** Manejo de partidas **/
	
	public Set<CompraUnitaria> getPartidas() {
		return partidas;
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
	public boolean agregarPartida(final CompraUnitaria det){
		boolean res= partidas.add(det);
		det.setCompra(this);
		return res;
	}
	
	/**
	 * Elimina una partida de requisicion de la coleccion de partidas
	 * 
	 * @param det
	 * @return
	 */
	public boolean eleiminarPartida(final CompraUnitaria det){
		det.setCompra(null);
		return partidas.remove(det);
	}
	
	
	
	
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
	

	public int getMes(){
		return Periodo.obtenerMes(fecha)+1;
	}
	public int getYear(){
		return Periodo.obtenerYear(fecha);
	}

	public void actualizar(){
		
		BigDecimal importeBruto=BigDecimal.ZERO;
		BigDecimal importeNeto=BigDecimal.ZERO;		
		BigDecimal descuento=BigDecimal.ZERO;
		
		for(CompraUnitaria det:partidas){
			importeBruto=importeBruto.add(det.getImporteBruto());
			descuento=descuento.add(det.getImporteDescuento());
			importeNeto=importeNeto.add(det.getImporteNeto());
			setImporteBruto(importeBruto);
			setImporteDescuento(descuento);
			setImporteNeto(importeNeto);			
		}
		setImpuestos(MonedasUtils.calcularImpuesto(getImporteNeto()));
		setTotal(MonedasUtils.calcularTotal(getImporteNeto()));
	}
	
	
	/****  equals,hashCode toString implementation ****/
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(obj==this) return true;
		if(getClass()!=obj.getClass()) return false;
		Compra2 otro=(Compra2)obj;
		return new EqualsBuilder()		
		.append(getId(),otro.getId())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)		
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
		.toString();
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
	
	public boolean isCancelable(){
		for(CompraUnitaria cu:partidas){
			if(cu.getRecibido()>0)
				return false;
		}
		return true;
	}

	public Boolean getConsolidada() {
		return consolidada;
	}

	public void setConsolidada(Boolean consolidada) {
		this.consolidada = consolidada;
	}

	public boolean isImportacion() {
		return importacion;
	}

	public void setImportacion(boolean importacion) {
		this.importacion = importacion;
	}

	public boolean isEspecial() {
		return especial;
	}

	public void setEspecial(boolean especial) {
		boolean old=this.especial;
		this.especial = especial;
		firePropertyChange("especial", old, especial);
	}

	public double getDescuentoEspecial() {
		return descuentoEspecial;
	}

	public void setDescuentoEspecial(double descuentoEspecial) {
		double old=this.descuentoEspecial;
		this.descuentoEspecial = descuentoEspecial;
		firePropertyChange("descuentoEspecial", old, descuentoEspecial);
	}
	
	
	
	 

}
