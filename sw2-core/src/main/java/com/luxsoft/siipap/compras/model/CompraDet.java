package com.luxsoft.siipap.compras.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Unidad;
import com.luxsoft.siipap.util.MonedasUtils;

@Entity
@Table(name = "SX_COMPRASDET")
public class CompraDet extends BaseBean{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "COMPRADET_ID")
	private Long id;
	
	@Version
	private int version;

	@ManyToOne(optional=false)
	@JoinColumn (name="COMPRA_ID",nullable=false,updatable=false)	
	private Compra compra;
	
	@ManyToOne (optional=true,fetch=FetchType.LAZY)
	@JoinColumn (name="SUCURSAL_ID", nullable=true, updatable=true)	
	private Sucursal sucursal;

	@ManyToOne(optional=false,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn (name="PRODUCTO_ID",nullable=false)
	@NotNull
	private Producto producto;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="UNIDAD",nullable=false)
	private Unidad unidad;
	
	@Column(name="SOLICITADO",nullable=false)
	private double solicitado=0;
	
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
		
	@Column(name="COMENTARIO",length=150)
	@Length(max=150)
	private String comentario;	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REFRESH
			}
			,fetch=FetchType.LAZY,mappedBy="compraDet")
			@Fetch(value=FetchMode.SUBSELECT)
	private Set<EntradaPorCompra> entradas=new HashSet<EntradaPorCompra>();
	
	@Column(name="DEPURADA",nullable=false)
	private boolean depurada=false;
	
	@Column(name="FOLIO")
	private Integer folio;
	
	@Column(name="RENGLON")
	private int renglon;
	
	@Embedded
	private UserLog log=new UserLog();
	

	public CompraDet() {
	}
	
	public CompraDet(Producto producto) {
		this.producto = producto;
	}
	
	public Long getId() {
		return id;
	}
	protected void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}
	protected void setVersion(int version) {
		this.version = version;
	}
	
	/**
	 * La compra a la que pertenece esta partida
	 * 
	 * @return
	 */
	public Compra getCompra() {
		return compra;
	}
	public void setCompra(Compra compra) {
		this.compra = compra;
	}
	
	public Producto getProducto() {
		return producto;
	}
	public void setProducto(Producto producto) {
		Object old=this.producto;
		this.producto = producto;
		this.unidad=producto.getUnidad();
		firePropertyChange("producto", old, producto);
	}
	
	public Unidad getUnidad() {
		return unidad;
	}

	public void setUnidad(Unidad unidad) {
		this.unidad = unidad;
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
	
	public Currency getMoneda(){
		Assert.notNull(getCompra(),"La compra no es ha fijado por lo tanto no existe tipo de cambio");
		return getCompra().getMoneda();
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
	
	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		Object old=this.sucursal;
		this.sucursal = sucursal;
		firePropertyChange("sucursal", old, sucursal);
	}
	public boolean isDepurada() {
		return depurada;
	}

	public void setDepurada(boolean depurada) {
		this.depurada = depurada;
	}
	
	
	
	public int getRenglon() {
		return renglon;
	}

	public void setRenglon(int renglon) {
		this.renglon = renglon;
	}

	/**** Propiedades dinamicas **/
	

	public Integer getFolio() {
		return folio;
	}

	public void setFolio(Integer folio) {
		this.folio = folio;
	}

	/**
	 * Calcula el costo en moneda nacional
	 * 
	 */
	public CantidadMonetaria getCostoMN(){
		return getCosto().multiply(getCompra().getTc());
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
		return MonedasUtils.aplicarDescuentosEnCascadaBase100(importe, getDesc1(),getDesc2(),getDesc3(),getDesc4());
	}
	
	/**
	 * Precio en {@link CantidadMonetaria} de cada producto despues de descuentos
	 * Este es el costo del producto
	 * 
	 */
	public CantidadMonetaria getCosto(){
		return aplicarDescuentos(getPrecioEnMoneda());
	}
	
	/**
	 * Importe bruto de lo solicitado
	 * 
	 * @return
	 */
	public CantidadMonetaria getImporteBruto(){
		CantidadMonetaria precioMN=new CantidadMonetaria(getPrecio(),getMoneda());
		return precioMN.multiply(getSolicitado());
	}

	/**
	 * Importe de lo solicitado aplicando los descuentos. Los descuentos se aplican al importe bruto
	 * 
	 * @return
	 * @see getImporteBruto
	 */
	public CantidadMonetaria getImporte() {
		CantidadMonetaria bruto=getImporteBruto();
		return aplicarDescuentos(bruto);
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
	
	
	/*@org.hibernate.annotations.Formula(
			"SELECT COUNT(*) FROM SX_RECEPCION_COMPRASDET X WHERE X.COMPRA_ID=id"
			)
	private double recibido=0;
	*/
	public double getRecibido(){
		double r=0;
		for(EntradaPorCompra e:entradas){
			r+=e.getCantidad();
		}
		return r;
		
		//return recibido;
	}
	
	/**
	 * Regresa la cantidad e material con posibilidad de ser entregado 
	 * 
	 * @return
	 */
	public double getPendiente(){
		return getSolicitado()-getRecibido();
	}
	
	/**
	 * Actualiza el costo de las entradas
	 * 
	 */
	public void actualizarEntradas(){
		for(EntradaPorCompra e:entradas){
			e.setCosto(getCostoMN().amount());
		}
		
	}
	
	/*** Collecciones ****/

	public Set<EntradaPorCompra> getEntradas(){
		return Collections.unmodifiableSet(entradas);
	}
	
	public boolean agregarEntrada(EntradaPorCompra e){
		boolean res=entradas.add(e);
		//if(res)
			//e.setCompraDet(this);
		return res;
	}	
	
	public boolean eliminarEntreada(EntradaPorCompra entrega){
		boolean res=entradas.remove(entrega);
		return res;
	}
	
	
	/**
	 * Porcentaje de participacion de esta partida en el total de la compra
	 * 
	 * @return
	 */
	public double getParticipacion(){
		if(getCompra()!=null){
			double total=getCompra().getImporte().amount().doubleValue();
			double neto=getImporte().amount().doubleValue();
			double res=neto/total;
			return res;
			
		}else
			return 0;
	}
	
	
	
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
		if (!(obj instanceof CompraDet))
			return false;
		CompraDet otro = (CompraDet) obj;
		return new EqualsBuilder()
			.append(sucursal, otro.getSucursal())
			.append(producto, otro.getProducto())
			.append(renglon, otro.getRenglon())
			.append(unidad, otro.getUnidad())
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 35)
		.append(sucursal)
		.append(producto)
		.append(renglon)
		.append(unidad)
		.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(getId())
		.append(sucursal)
		.append(producto)
		.append(solicitado)
		.append(precio)
		.toString();
	}
	

}
