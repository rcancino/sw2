/*
 *  Copyright 2008 Ruben Cancino Ramos.
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

package com.luxsoft.siipap.model.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Entidad básica del sistema, representa un producto o servicio
 * 
 * @author Ruben Cancino
 */
@Entity
@Table(name="SX_PRODUCTOS")
public class Producto extends BaseBean implements Replicable{
    
    //@Id @GeneratedValue(strategy= GenerationType.AUTO)
	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="PRODUCTO_ID", 
            allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
    @Column(name="PRODUCTO_ID")
    private Long id;
    
    @Version
    private int version;
    
    @Column(name="CLAVE",nullable=false,updatable=false,unique=true)
    @NotNull  @Length(max=10)
    private String clave;
    
    @Column(name="DESCRIPCION",nullable=false)
    @NotNull  @Length(max=250)
    private String descripcion;
    
    @Column(nullable=false)
    private boolean activo=true;
    
    @Column(nullable=false)
    @NotNull
    private boolean inventariable=true;
    
    @Column(nullable=false,updatable=false)
    @NotNull
    private boolean servicio=false;
    
    /**********   Clasificadores para la industria del papel *****************/
    
    @ManyToOne (optional=true
    		,cascade={CascadeType.MERGE,CascadeType.PERSIST}
    		,fetch=FetchType.EAGER)
    @JoinColumn (name="LINEA_ID", nullable=true)
    @Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
    @NotNull
    private Linea linea;
    
    @ManyToOne (optional=true
    		,cascade={CascadeType.MERGE,CascadeType.PERSIST}
    		,fetch=FetchType.EAGER)
    @JoinColumn (name="MARCA_ID", nullable=true)
    @Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
    private Marca marca;
    
    @ManyToOne (optional=true
    		,cascade={CascadeType.MERGE,CascadeType.PERSIST}
    		,fetch=FetchType.EAGER)
    @JoinColumn (name="CLASE_ID", nullable=true)
    @Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
    @NotNull
    private Clase clase;
    
    @OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			}
			,fetch=FetchType.LAZY,mappedBy="producto")
			@Fetch(value=FetchMode.SUBSELECT)
    private Set<Existencia> existencias=new HashSet<Existencia>();
    
    /**********   Atributos particulares de papel *****************/
    
    @NotNull
    private boolean deLinea=true;
    
    @Enumerated(EnumType.STRING)
	@Column (name="PRESENTACION",nullable=true,length=15)
    @NotNull
    private Presentacion presentacion=Presentacion.EXTENDIDO;
    
    @Column(name="KILOS",nullable=false)
    @NotNull
    private double kilos=0;
    
    @Column(name="GRAMOS",scale=3,nullable=false)
    @NotNull
    private int gramos=0;
    
    @Column(nullable=false)
    @NotNull
    private double largo=0;
    
    @Column(nullable=false)
    @NotNull
    private double ancho=0;
    
    @Column(nullable=false)
    @NotNull
    private int calibre=0;   
    
    @Column(nullable=false)
    @NotNull
    private int caras=1;
    
    @Column(name="ACABADO",length=20)    
    private String acabado;
    
    @Column(name="COLOR",length=25)
    private String color;
    
    @NotNull
    private boolean nacional=true;
    
    @ManyToOne (optional=false
    	,cascade={CascadeType.MERGE,CascadeType.PERSIST}
    	,fetch=FetchType.EAGER)
    @JoinColumn (name="UNIDAD", nullable=false)
    @NotNull
    private Unidad unidad;
    
    @CollectionOfElements(fetch= FetchType.LAZY)
    @JoinTable(name="SX_PRODUCTOS_UNIDADES",joinColumns=@JoinColumn( name="PRODUCTO_ID"))
    private Set<Unidad> unidades=new HashSet<Unidad>();
    
    @CollectionOfElements(fetch= FetchType.LAZY)
    @JoinTable(name="SX_PRODUCTOS_CODIGOS",joinColumns=@JoinColumn( name="PRODUCTO_ID"))
    private Set<Codigo> codigos=new HashSet<Codigo>();
    
    @CollectionOfElements
    @JoinTable(name="SX_PRODUCTOS_COMENT",joinColumns=@JoinColumn( name="PRODUCTO_ID"))
    private Set<Comentario> comentarios=new HashSet<Comentario>();
    
    @CollectionOfElements
    @JoinTable(name="SX_PRODUCTOS_DESCUENTOS" ,joinColumns=@JoinColumn(name="PRODUCTO_ID"))
    @Sort(type= SortType.NATURAL)
    private SortedSet<Descuento> descuentos=new TreeSet<Descuento>();
    
    /*** Propiedades por compatibildad replica a SIIPAP DBF***/
    
    private String modoDeVenta="B";
    
    @Column(name="LIN_ORIG",length=10)
    @Length(min=10,max=10)
    private String lineaOrigen;
    
    @NotNull
    private double precioContado=0;
    
    @NotNull
    private double precioCredito=0;
    
    @Column(name="ACTIVO_VEN")
    private boolean activoVentas=true;
    
    @Column(name="ACTIVO_VEN_OBS",length=100)
    @Length(max=40)
    private String activoVentasObs;
    
    @Column(name="ACTIVO_COM")
    private boolean activoCompras=true;
    
    @Column(name="ACTIVO_COM_OBS",length=100)
    @Length(max=40)
    private String activoComprasObs;
    
    @Column(name="ACTIVO_INV")
    private boolean activoInventario=true;
    
    @Column(name="ACTIVO_INV_OBS",length=100)
    @Length(max=40)
    private String activoInventarioObs;
    
    @Column(name="ELIMINADO")
    private boolean eliminado=false;
    
    @Column(name="TRS")
    private Boolean transformable=false;
    
    @Column(name="M2MILLAR")
	private double metros2PorMillar;
    
    @Column(name="AJUSTE")
	private double ajuste;
    
    /** fin de propiedades especificas de papel **/
    
    @Embedded
    private UserLog userLog=new UserLog();
    
    @ManyToOne (optional=true,fetch=FetchType.EAGER)
	@JoinColumn (name="PROVEEDOR_ID")
    private Proveedor proveedor;
    

    public Producto() {
    }

    public Producto(String clave) {
        this.clave = clave;
        this.descripcion=clave;
    }

    public Producto(String clave, String descripcion) {
        this.clave = clave;
        this.descripcion = descripcion;
    }

    
    
    /**
     * Identificador para la base de datos para JPA/Hibernate
     * 
     * @return
     */
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Control de transacciones en JPA/Hibernate
     * @return
     */
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }
    
    /**
     * Clave identificadora del producto. Es mandatoria y unica
     * 
     * @return
     */
    public String getClave() {
        return clave;
    }
    
    /**
     * Fija la clave del articulo. Este propiedad es no actualizable
     * 
     * @param clave
     */
    public void setClave(String clave) {
    	Object old=this.clave;
        this.clave = clave;
        firePropertyChange("clave", old, clave);
    }
    
    /**
     * Descripción del producto, es mandatoria
     * 
     * @return
     */
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
    	Object old=this.descripcion;
        this.descripcion = descripcion;
        firePropertyChange("descripcion", old, descripcion);
    }
    
    public Unidad getUnidad() {
		return unidad;
	}

    public double getAjuste() {
        return ajuste;
    }
    public void setAjuste(double ajuste) {
    	Object old=this.ajuste;
        this.ajuste = ajuste;
        firePropertyChange("ajuste", old, ajuste);
    }
    
    
	public void setUnidad(Unidad unidad) {
		Object old=this.unidad;
		this.unidad = unidad;
		firePropertyChange("unidad", old, unidad);
	}

    /**
     * Determina si el articulo esta activo, Su uso esta delimitado por las reglas
     * de negocio. Por default solo indica si el producto se puede comprar
     * y/o vender
     * 
     * @return
     */
    public boolean isActivo() {
        return activo;
    }
    public void setActivo(boolean activo) {
    	boolean old=this.activo;
        this.activo = activo;
        firePropertyChange("activo", old, activo);
    }

    /**
     * Indica si el producto es de linea o es un producto de fabricación especial
     * 
     * @return
     */
    public boolean isDeLinea() {
        return deLinea;
    }
    public void setDeLinea(boolean deLinea) {
        this.deLinea = deLinea;
    }

    /**
     * Determina si este producto es inventariable.
     * 
     * @return verdadero si el producto es inventariable
     */
    public boolean isInventariable() {
        return inventariable;
    }
    public void setInventariable(boolean inventariable) {
        this.inventariable = inventariable;
    }

    /**
     * Clasificacion principal del producto. Es mandatorio
     * 
     * @return
     */
    public Linea getLinea() {
        return linea;
    }
    public void setLinea(Linea linea) {
    	Object old=this.linea;
        this.linea = linea;
        firePropertyChange("linea", old, linea);
    }

    /**
     * Opcional para definir la marca del producto
     * 
     * @return
     */
    public Marca getMarca() {
        return marca;
    }
    public void setMarca(Marca marca) {
    	Object old=this.marca;
        this.marca = marca;
        firePropertyChange("marca", old, marca);
    }

    /**
     * Determina  la naturaleza Producto o Servicio
     * 
     * @return verdadero si se trata de un servicio
     */
    public boolean isServicio() {
        return servicio;
    }
    public void setServicio(boolean servicio) {
        this.servicio = servicio;
    }   

    /**
     * Bitacora de modificaciones
     * 
     * @return
    */
    public UserLog getUserLog() {
        return userLog;
    }
    public void setUserLog(UserLog userLog) {
        this.userLog = userLog;
    }
    
    
    /** Propiedades especificas de la industria del papel **/
    
    public double getKilos() {
        return kilos;
    }
    public void setKilos(double kilos) {
        this.kilos = kilos;
    }
    
    public int getGramos() {
        return gramos;
    }
    public void setGramos(int gramos) {
        this.gramos = gramos;
    }
    
    public double getLargo() {
        return largo;
    }
    public void setLargo(double largo) {
        this.largo = largo;
    }
    
    public double getAncho() {
        return ancho;
    }
    public void setAncho(double ancho) {
        this.ancho = ancho;
    }

    public int getCalibre() {
        return calibre;
    }
    public void setCalibre(int calibre) {
        this.calibre = calibre;
    }

    public int getCaras() {
        return caras;
    }
    public void setCaras(int caras) {
        this.caras = caras;
    }

    public Clase getClase() {
        return clase;
    }
    public void setClase(Clase clase) {
    	Object old=this.clase;
        this.clase = clase;
        firePropertyChange("clase", old, clase);
    }
    
    public boolean isNacional() {
        return nacional;
    }
    public void setNacional(boolean nacional) {
        this.nacional = nacional;
    }

    public Presentacion getPresentacion() {
        return presentacion;
    }
    public void setPresentacion(Presentacion presentacion) {
        this.presentacion = presentacion;
    }
    
    public String getAcabado() {
        return acabado;
    }
    public void setAcabado(String acabado) {
    	Object old=this.acabado;
        this.acabado = acabado;
        firePropertyChange("acabado", old, acabado);
    }
    
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getLineaOrigen() {
		return lineaOrigen;
	}

    
	public void setLineaOrigen(String lineaOrigen) {
		Object old=this.lineaOrigen;
		this.lineaOrigen = lineaOrigen;
		firePropertyChange("lineaOrigen", old, lineaOrigen);
	}
	
	
	
    /** FIN DE Propiedades especificas del papel ***/
    
    public Boolean getTransformable() {
    	if(transformable==null)
    		return false;
		return transformable;
	}

	public void setTransformable(Boolean transformable) {
		Object old=this.transformable;
		this.transformable = transformable;
		firePropertyChange("transformable", old, transformable);
	}

	/************************************************ TRATAMIENTO DE COLLECTIONES ***********************/
    
    public Set<Unidad> getUnidades() {
        return Collections.unmodifiableSet(unidades);
    }
    
    public boolean agregarUnidad(final Unidad unidad){
        return unidades.add(unidad);
    }
    
    public boolean eliminarUnidad(final Unidad unidad){
        return unidades.remove(unidad);
    }
    

    public Set<Codigo> getCodigos() {
        return Collections.unmodifiableSet(codigos);
    }
    
    public boolean agregarCodigo(final String clave,Integer numero){
    	Codigo c=new Codigo(clave,String.valueOf(numero),"");
    	return codigos.add(c);
    }
    
    public boolean agregarCodigo(final Codigo c){
        return codigos.add(c);
    }
    
    public boolean eliminarCodigo(final Codigo c){
        return codigos.remove(c);
    }
    
    public boolean eliminarCodigo(final String clave){
    	Object o=CollectionUtils.find(codigos, new Predicate(){
			public boolean evaluate(Object object) {
				Codigo c=(Codigo)object;
				return c.getClave().equals(clave);
			}
    	});
    	if(o!=null){
    		Codigo c=(Codigo)o;
    		return eliminarCodigo(c);
    	}
    	return false;
    }
    
    /**
     * Comentarios relacionados con el producto
     * 
     * @return
     */
    public Set<Comentario> getComentarios() {
        return Collections.unmodifiableSet(comentarios);
    }
    
    public boolean agregarComentario(Comentario c){    	
    	return comentarios.add(c);
    	
    }
    
    public boolean eliminarComentario(final Comentario c){
        return comentarios.remove(c);
    }

    /**
     * Regresa un Set  de solo lectura con los descuentos
     * para este producto
     * 
     * @return
     */
    public SortedSet<Descuento> getDescuentos() {
        return Collections.unmodifiableSortedSet(descuentos);
    }
    
    /**
     * Agrega un descuento al final de la lista de descuentos
     * 
     * @param valor
     */
    public void agregarDescuento(final double valor){        
        int orden=descuentos.size()==0?1:descuentos.size()+1;
        agregarDescuento(valor,orden,"NORMAL");
    }
    
    /**
     * 
     * Agrega un descuento al final de la lista de descuentos
     * con la descripcion definida
     * 
     * @param valor
     * @param desc
     */
    public Descuento agregarDescuento(double valor, String desc) {
        int orden=descuentos.size()==0?1:descuentos.size()+1;
        return agregarDescuento(valor,orden,desc);
    }
   
    
    /**
     * Forma ordenada de asignar descuentos con una descripción asociada
     * 
     * @param valor
     * @param orden
     * @param descripcion
     * @return El Descuento generado si este no existia, nulo de lo contrario
     */
    public Descuento agregarDescuento(final double valor,int orden,String descripcion){
        
        Descuento d=new Descuento(valor,descripcion.toUpperCase());
        d.setOrden(orden);
        boolean res=agregarDescuento(d);
        return res?d:null;
    }
    
    /**
     * Agrega un descuento para este producto. 
     * 
     * @param d
     * @return verdadero si el descuento no existia, nulo de lo contrario
     * 
     */
    public boolean agregarDescuento(final Descuento d){
        return descuentos.add(d);
    }
    
    /**
     * Elimina el descuento de este producto
     * 
     * @param desc
     * @return verdadero si el descuento fue eliminado con exito, nulo de lo contrario
     */
    public boolean eliminarDescuento(Descuento desc){
        return descuentos.remove(desc);
    }
    
    /**
     * Elimina un descuento asignado
     * 
     * @param orden
     * @return
     */
    public boolean eliminarDescuento(final int orden){
        Descuento d=(Descuento)CollectionUtils.find(descuentos, new Predicate() {

            public boolean evaluate(Object object) {
                Descuento dd=(Descuento)object;
                return dd.getOrden()==orden;
            }
        });
        return eliminarDescuento(d);
    }

    /************************************************ TRATAMIENTO DE COLLECTIONES ***********************/
    
    /** equals,hashCode y toString*******/
    
    @Override
    public String toString() {
    	return StringUtils
    		.rightPad(getClave(),10,' ')+"  "+getDescripcion()+"   "+getGramos()+"g.";
    }

    @Override
    public boolean equals(Object o) {
        if(o==null) return false;
        if(o==this) return true;
        if(getClass()!=o.getClass()) return false;
        Producto otro=(Producto)o;
        return new EqualsBuilder()
                .append(getClave(), otro.getClave())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,35)
                .append(getClave())
                .toHashCode();
    }
    

    /** Por compatibildad con SIIPAP DBF **/

	public double getPrecioContado() {
		
		return precioContado;
	}

	public void setPrecioContado(double precioContado) {
		this.precioContado = precioContado;
	}

	public double getPrecioCredito() {
		return precioCredito;
	}

	public void setPrecioCredito(double precioCredito) {
		this.precioCredito = precioCredito;
	}
	
	
	
	public String getModoDeVenta() {
		if(modoDeVenta==null)
			modoDeVenta="B";
		return modoDeVenta;
	}

	public void setModoDeVenta(String modoDeVenta) {
		Object old=this.modoDeVenta;
		this.modoDeVenta = modoDeVenta;
		firePropertyChange("modoDeVenta", old, modoDeVenta);
	}

	public boolean isPrecioBruto(){
		return getModoDeVenta().equals("B");
	}

	public boolean isActivoVentas() {
		return activoVentas;
	}

	public void setActivoVentas(boolean activoVentas) {
		boolean old=this.activoVentas;
		this.activoVentas = activoVentas;
		firePropertyChange("activoVentas", old, activoVentas);
	}

	public String getActivoVentasObs() {
		return activoVentasObs;
	}

	public void setActivoVentasObs(String activoVentasObs) {
		Object old=this.activoVentasObs;
		this.activoVentasObs = activoVentasObs;
		firePropertyChange("activoVentasObs", old, activoVentasObs);
	}

	public boolean isActivoCompras() {
		return activoCompras;
	}

	public void setActivoCompras(boolean activoCompras) {
		boolean old=this.activoCompras;
		this.activoCompras = activoCompras;
		firePropertyChange("activoCompras", old, activoCompras);
	}

	public String getActivoComprasObs() {
		return activoComprasObs;
	}

	public void setActivoComprasObs(String activoComprasObs) {
		Object old=this.activoComprasObs;
		this.activoComprasObs = activoComprasObs;
		firePropertyChange("activoComprasObs", old, activoComprasObs);
	}

	public boolean isActivoInventario() {
		return activoInventario;
	}

	public void setActivoInventario(boolean activoInventario) {
		boolean old=this.activoInventario;
		this.activoInventario = activoInventario;
		firePropertyChange("activoInventario", old, activoInventario);
		
	}

	public String getActivoInventarioObs() {
		return activoInventarioObs;
	}

	public void setActivoInventarioObs(String activoInventarioObs) {
		Object old=this.activoInventarioObs;
		this.activoInventarioObs = activoInventarioObs;
		firePropertyChange("activoInventarioObs", old, activoInventarioObs);
	}

	public boolean isEliminado() {
		return eliminado;
	}

	public void setEliminado(boolean eliminado) {
		boolean old=this.eliminado;
		this.eliminado = eliminado;
		firePropertyChange("eliminado", old, eliminado);
	}

	public Set<Existencia> getExistencias() {
		return existencias;
	}
	
	@Transient
	private List<CostoPromedio> costos;
	
	
	
	

	public List<CostoPromedio> getCostos() {
		return costos;
	}

	public void setCostos(List<CostoPromedio> costos) {
		this.costos = costos;
	}

	@Transient
	private Boolean replicar=true;		
	

	/**
	 * Hint para el sistema de replica para poder evitar si se quiere
	 * en tiempo de ejeucion la replica
	 * 
	 * Siempre que se quiera salvar un articulo sin que esto implique
	 * generar un archivo de replica este parametro se debe establecer
	 * en false
	 * 
	 * @return
	 */
	public boolean isReplicar() {
		if(replicar==null)
			return false;
		return replicar;
	}
	public void setReplicar(Boolean replicar) {
		this.replicar = replicar;
	}

	@Column(name="ARTFACNECR")
	private boolean ARTFACNECR=false;
	
	

	public boolean isARTFACNECR() {
		return ARTFACNECR;
	}

	public void setARTFACNECR(boolean artfacnecr) {
		ARTFACNECR = artfacnecr;
	}

	public static enum Presentacion{
    	BOBINA
    	,CORTADO
    	,EXTENDIDO
    	,ND
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

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		Object old=this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
	}

	public double getMetros2PorMillar() {
		return metros2PorMillar;
	}

	public void setMetros2PorMillar(double metros2PorMillar) {
		double old=this.metros2PorMillar;
		this.metros2PorMillar = metros2PorMillar;
		firePropertyChange("metros2PorMillar", old, metros2PorMillar);
	}

	@Column(name="PAQUETE",nullable=false)
	@NotNull
	//@Transient
	private int paquete=1;
	
	@Column(name="FECHA_LP")
	private Date fechaDeAplicacionListaDePrecios;


	public int getPaquete() {
		return paquete;
	}

	public void setPaquete(int paquete) {
		int old=this.paquete;
		this.paquete = paquete;
		firePropertyChange("paquete", old, paquete);
	}
	
	
	@Column(name="ESPECIAL")	
	private boolean medidaEspecial=false;
	
	@Column(name="PRECIO_KILO_CRE")
	private BigDecimal precioPorKiloCredito;
	
	@Column(name="PRECIO_KILO_CON")
	private BigDecimal precioPorKiloContado;
	
	
	@Column(name = "CLASIFICACION", nullable = true, length = 50)
	private String clasificacion;


	public boolean isMedidaEspecial() {
		return medidaEspecial;
	}

	public void setMedidaEspecial(boolean medidaEspecial) {
		boolean old=this.medidaEspecial;
		this.medidaEspecial = medidaEspecial;
		firePropertyChange("medidaEspecial", old, medidaEspecial);
	}

	public BigDecimal getPrecioPorKiloContado() {
		return precioPorKiloContado;
	}

	public void setPrecioPorKiloContado(BigDecimal precioPorKiloContado) {
		Object old=this.precioPorKiloContado;
		this.precioPorKiloContado = precioPorKiloContado;
		firePropertyChange("precioPorKiloContado", old, precioPorKiloContado);
	}

	public void setPrecioPorKiloCredito(BigDecimal precioPorKiloCredito) {
		Object old=this.precioPorKiloCredito;
		this.precioPorKiloCredito = precioPorKiloCredito;
		firePropertyChange("precioPorKiloCredito", old, precioPorKiloCredito);
	}
	
	public BigDecimal getPrecioPorKiloCredito(){
		return this.precioPorKiloCredito;
		/*
		if(getKilos()!=0)
			return getPrecioCredito()/getKilos();
		else
			return 0.0;
			*/
	}

	public Date getFechaDeAplicacionListaDePrecios() {
		return fechaDeAplicacionListaDePrecios;
	}

	public void setFechaDeAplicacionListaDePrecios(
			Date fechaDeAplicacionListaDePrecios) {
		this.fechaDeAplicacionListaDePrecios = fechaDeAplicacionListaDePrecios;
	}
	
	


	public String getClasificacion() {
		return clasificacion;
	}

	public void setClasificacion(String clasificacion) {
		Object old=this.clasificacion;
		this.clasificacion = clasificacion;
		firePropertyChange("clasificacion", old, clasificacion);
	}

	
	

}
