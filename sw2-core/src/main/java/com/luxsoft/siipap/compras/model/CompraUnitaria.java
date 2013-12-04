package com.luxsoft.siipap.compras.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.util.MonedasUtils;

@Entity
@Table(name = "SX_COMPRAS2_DET")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class CompraUnitaria extends BaseBean{

	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="COMPRADET_ID")
	protected String id;
	
	@Version
	private int version;
	

	@ManyToOne(optional=false)
	@JoinColumn (name="COMPRA_ID",nullable=false,updatable=false)	
	private Compra2 compra;
	
	@ManyToOne (optional=true,fetch=FetchType.LAZY)
	@JoinColumn (name="SUCURSAL_ID", nullable=true, updatable=true)	
	private Sucursal sucursal;
	
	@Column (name="SUC_NAME",nullable=false, length=50)
	private String sucursalNombre;

	@ManyToOne(optional=false,fetch=FetchType.LAZY)
	@JoinColumn (name="PRODUCTO_ID",nullable=false)
	@NotNull
	private Producto producto;
	
	@Column(name="CLAVE",nullable=false)
    private String clave;
    
    @Column(name="DESCRIPCION",nullable=false,length=250)
    private String descripcion;
	
    @Column(name="UNIDAD",length=3,nullable=false)
	private String unidad;
    
    @Column(name="FACTOR",nullable=false)
    private double factor;
	
	@Column(name="SOLICITADO",nullable=false)
	private double solicitado=0;
	
	@Formula("(" +
			"select ifnull(sum(x.cantidad),0) from sx_inventario_com  x " +
			"	where x.COMPRADET_ID=COMPRADET_ID"+
			")")
	private double recibido=0;
	
	@Column(name="RECIBIDO_GLOBAL",nullable=true)
	//@Transient
	private double recibidoGlobal=0;
	
	@Column (name="PRECIO",nullable=false,scale=2,precision=10)
	private BigDecimal precio=BigDecimal.ZERO;
	
	@Column (name="DESC1",scale=4)
	private double desc1=0;
	
	@Column (name="DESC2",scale=4)
	private double desc2=0;
	
	@Column (name="DESC3",scale=4)
	private double desc3=0;
	
	@Column (name="DESC4",scale=4)
	private double desc4=0;
	
	@Column (name="DESC5",scale=4)
	private double desc5=0;
	
	@Column (name="DESC6",scale=4)
	private double desc6=0;
	
	@Column (name="DESCF",scale=4)
	private double descuentof=0;	
	
	@Column (name="COSTO",nullable=false,scale=6,precision=16)
	private BigDecimal costo=BigDecimal.ZERO;
	
	@Column (name="IMPORTE_BRUTO",nullable=false,scale=2,precision=16)
	private BigDecimal importeBruto=BigDecimal.ZERO;
	
	@Column (name="IMPORTE_DESC",nullable=false,scale=2,precision=16)
	private BigDecimal importeDescuento=BigDecimal.ZERO;

	@Column (name="IMPORTE_NETO",nullable=false,scale=6,precision=16)
	private BigDecimal importeNeto=BigDecimal.ZERO;
	
			
	@Column(name="COMENTARIO",length=250)
	private String comentario;	
	
		
	@Column(name="DEPURACION")
	private Date depuracion;
	
	@Column(name="DEPURADO",nullable=false, columnDefinition=" double precision  default 0")
	private double depurado; 
	
	/*
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })*/
	@Transient
	private UserLog log=new UserLog();
	
	/**
	 * Folio origen de las compras consolidadas
	 * 
	 * 
	 */
	@Column(name="FOLIO_ORIGEN")
	public Long folioOrigen;
	
	/**
     * Ultima fecha en que se actualizo informacion aduanal
     * 
     */
    @Column(name="REGISTRO_ADUANA")
    private Date registroAduana;
    
    /**
     * Cantidad de material q se encuentra en el almacen del agente aduanal
     * 
     */
    @Column(name="ADUANA")
    private double aduana;

	public double getFactor() {
		return factor;
	}

	public CompraUnitaria() {
	}
	
	public CompraUnitaria(Producto producto) {
		this.producto = producto;
		Assert.notNull(producto,"El producto es mandatorio");
		setProducto(producto);
	}
	
	public String getId() {
		return id;
	}
	

	public int getVersion() {
		return version;
	}
	
	
	/**
	 * La compra a la que pertenece esta partida
	 * 
	 * @return
	 */
	public Compra2 getCompra() {
		return compra;
	}
	public void setCompra(Compra2 compra) {
		this.compra = compra;
	}
	
	public Producto getProducto() {
		return producto;
	}
	public void setProducto(Producto producto) {
		Object old=this.producto;
		this.producto = producto;
		firePropertyChange("producto", old, producto);
		if(producto!=null){
			this.clave=producto.getClave();
			this.descripcion=producto.getDescripcion();
			this.unidad=producto.getUnidad().getUnidad();
			this.factor=producto.getUnidad().getFactor();
		}else{
			this.clave=null;
			this.descripcion=null;
			this.unidad=null;
			this.factor=0;
		}
	}
	
	public String getClave() {
		return clave;
	}

	public String getDescripcion() {
		return descripcion;
	}
	
	public String getUnidad() {
		return unidad;
	}
	
	

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		Object old=this.sucursal;
		this.sucursal = sucursal;
		firePropertyChange("sucursal", old, sucursal);
		if(sucursal!=null)
			this.sucursalNombre=sucursal.getNombre();
		else
			this.sucursalNombre=null;
		
	}
	

	public String getSucursalNombre() {
		return sucursalNombre;
	}

	public double getSolicitado() {
		return solicitado;
	}

	public void setSolicitado(double solicitado) {
		double old=this.solicitado;
		this.solicitado = solicitado;
		firePropertyChange("solicitado", old, solicitado);
	}	
	
	public BigDecimal getPrecio() {		
		return precio;
	}
	public void setPrecio(BigDecimal precio) {
		Object old=this.precio;
		this.precio = precio;
		firePropertyChange("precio", old, precio);
	}
	
	public Currency getMoneda(){
		Assert.notNull(getCompra(),"La compra no es ha fijado por lo tanto no existe tipo de cambio");
		return getCompra().getMoneda();
	}
	
	public double getTc(){
		Assert.notNull(getCompra(),"La compra no es ha fijado por lo tanto no existe tipo de cambio");
		return getCompra().getTc();
	}
	
	public double getDesc1() {
		return desc1;
	}	
	public void setDesc1(double desc1) {
		double old=this.desc1;
		this.desc1 = desc1;
		firePropertyChange("desc1", old, desc1);
	}

	public double getDesc2() {
		return desc2;
	}

	public void setDesc2(double desc2) {
		double old=this.desc2;
		this.desc2 = desc2;
		firePropertyChange("desc2", old, desc2);
	}

	public double getDesc3() {
		return desc3;
	}

	public void setDesc3(double desc3) {
		double old=this.desc3;
		this.desc3 = desc3;
		firePropertyChange("desc3", old, desc3);
	}

	public double getDesc4() {
		return desc4;
	}

	public void setDesc4(double desc4) {
		double old=this.desc4;
		this.desc4 = desc4;
		firePropertyChange("desc4", old, desc4);
	}
	
	public double getDesc5() {
		return desc5;
	}

	public void setDesc5(double desc5) {
		double old=this.desc5;
		this.desc5 = desc5;
		firePropertyChange("desc5", old, desc5);
	}

	public double getDesc6() {
		return desc6;
	}

	public void setDesc6(double desc6) {
		Object old=this.desc6;
		this.desc6 = desc6;
		firePropertyChange("desc6", old, desc6);
	}

	public double getDescuentof() {
		return descuentof;
	}

	public void setDescuentof(double descuentof) {
		this.descuentof = descuentof;
	}

	public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		Object old=this.costo;
		this.costo = costo;
		firePropertyChange("costo", old, costo);
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

	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}	

	public UserLog getLog() {
		return log;
	}
	public void setLog(UserLog log) {
		this.log = log;
	}
	
	public boolean isDepurada() {
		return depuracion!=null;
	}
	
	public Date getDepuracion() {
		return depuracion;
	}

	public void setDepuracion(Date depuracion) {
		this.depuracion = depuracion;
	}

	public double getDepurado() {
		return depurado;
	}

	public void setDepurado(double depurado) {
		this.depurado = depurado;
	}	
	

	/**
	 * Actualizamos los importes en funcion del precio y los descuentos
	 *  
	 */
	public void actualizar(){
		
		CantidadMonetaria precio=getPrecioEnMoneda();
		CantidadMonetaria impBruto=precio.multiply(solicitado/factor);
		setImporteBruto(impBruto.amount());
		
		CantidadMonetaria importeNeto=aplicarDescuentos(impBruto);
		setImporteNeto(importeNeto.amount());
		
		setImporteDescuento(importeBruto.subtract(importeNeto.amount()));
		
		CantidadMonetaria costo=aplicarDescuentos(precio);
		setCosto(costo.amount());
		setEspecial(producto.isMedidaEspecial());
		
	}

	

	/**
	 * Calcula el costo en moneda nacional
	 * 
	 */
	public CantidadMonetaria getCostoMN(){
		return CantidadMonetaria.pesos(getCosto()).multiply(getTc());
	}
	
	/**
	 * Regresa el precio del producto como {@link CantidadMonetaria}
	 */
	public CantidadMonetaria getPrecioEnMoneda(){
		return new CantidadMonetaria(getPrecio(),getMoneda());
	}
	
	/**
	 * Aplica descuentos en cascada
	 * Funciona como metodo para desvincular los descuentos de las demas propiedades. Si se requieren mas descuentos solo es necesario
	 * modificar este metodo o si bien los descuentos son dinamicos tambien se puede sobre escribir este metodo
	 * 
	 * @param importe
	 * @return
	 */
	private CantidadMonetaria aplicarDescuentos(CantidadMonetaria importe){
		return MonedasUtils.aplicarDescuentosEnCascadaBase100(importe, getDesc1(),getDesc2(),getDesc3(),getDesc4(),getDesc5(),getDesc6());
	}	
	
	
	/**
	 * Determina si esta partida esta pendiente
	 * @return
	 */
	public boolean isPendiente(){
		if(isDepurada())
			return false;
		return getSolicitado()-getRecibido()>0;
	}	
	
	public double getRecibido(){
		return recibido;
	}
	
	/**
	 * Regresa la cantidad e material con posibilidad de ser entregado 
	 * 
	 * @return
	 */
	public double getPendiente(){
		return getSolicitado()-getRecibido()-getDepurado();
	}
	
	
	/**
	 * Porcentaje de participacion de esta partida en el total de la compra
	 * 
	 * @return
	 *//*
	public double getParticipacion(){
		if(getCompra()!=null){
			double total=getCompra().getImporte().amount().doubleValue();
			double neto=getImporte().amount().doubleValue();
			double res=neto/total;
			return res;
			
		}else
			return 0;
	}*/
	
	
	
	/*** Validadores de estado ****/
	
	//@AssertTrue (message="El precio debe ser >0")
	public boolean validarPrecio(){
		return getPrecio().doubleValue()>0;
	}
	
	@AssertTrue (message="La cantidad solicitada no puede ser <=0")
	public boolean validarCantidad(){
		return getSolicitado()>0;
	}
	
	//@AssertTrue (message="El descuento  debe ser <100")
	public boolean validarDescuentos(){
		return (getDesc1()<100d && getDesc2()<100d && getDesc3()<100d && getDesc4()<100d );
	}
	
	/** ecuals,hashCode toString ******/
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof CompraUnitaria))
			return false;
		CompraUnitaria otro = (CompraUnitaria) obj;
		return new EqualsBuilder()
			.append(sucursal, otro.getSucursal())
			.append(producto, otro.getProducto())			
			//.append(id, otro.getId())
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 35)
		.append(sucursal)
		.append(producto)		
		//.append(id)
		.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(getId())
		.append(sucursalNombre)
		.append(producto)
		.append(solicitado)
		.append(precio)
		.toString();
	}

	@Transient
	 private boolean porDepurar=false;

	public boolean isPorDepurar() {
		return porDepurar;
	}

	public void setPorDepurar(boolean porDepurar) {
		boolean old=this.porDepurar;
		this.porDepurar = porDepurar;
		firePropertyChange("porDepurar", old, porDepurar);
	}

	public Long getFolioOrigen() {
		return folioOrigen;
	}

	public void setFolioOrigen(Long folioOrigen) {
		this.folioOrigen = folioOrigen;
	}

	public Date getRegistroAduana() {
		return registroAduana;
	}

	public void setRegistroAduana(Date registroAduana) {
		this.registroAduana = registroAduana;
	}

	public double getAduana() {
		return aduana;
	}

	public void setAduana(double aduana) {
		double old=this.aduana;
		this.aduana = aduana;
		firePropertyChange("aduana", old, aduana);
		setRegistroAduana(new Date());
		
	}

	public double getRecibidoGlobal() {
		return recibidoGlobal;
	}

	public void setRecibidoGlobal(double recibidoGlobal) {
		this.recibidoGlobal = recibidoGlobal;
	}
	
	
	@Column(name="ESPECIAL")	
	private boolean especial=false;
	
	@Column(name = "LARGO", scale = 3)
	private double largo = 0;
	
	
	@Column(name = "ANCHO", scale = 3)
	private double ancho = 0;
	
	@Column(name="PRECIO_KILO")
	private BigDecimal precioPorKilo;

	public boolean isEspecial() {
		return especial;
	}

	public void setEspecial(boolean especial) {
		this.especial = especial;
	}

	public double getLargo() {
		return largo;
	}

	public void setLargo(double largo) {
		double old=this.largo;
		this.largo = largo;
		firePropertyChange("largo", old, largo);
		actualizarDescripcion();
	}

	public double getAncho() {
		return ancho;
	}

	public void setAncho(double ancho) {
		double old=this.ancho;
		this.ancho = ancho;
		firePropertyChange("ancho", old, ancho);
		actualizarDescripcion();
	}
	
	public void actualizarDescripcion(){
		NumberFormat nf=new DecimalFormat("#.#");
		String desc=MessageFormat.format("{0} {1}X{2}"
				, getProducto().getDescripcion()
				,nf.format(getAncho())
				,nf.format(getLargo())
				);
		setDescripcion(desc);
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	

}
