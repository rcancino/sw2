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
package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.MonedasUtils;


/**
 * Entidad para el estado de las partidas de una venta
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name = "SX_VENTASDET")
public class VentaDet extends Inventario{
	
	
    @ManyToOne(optional = false)
    @JoinColumn(name = "VENTA_ID"
    	, nullable = false
    	, updatable = false,insertable=false
    	)
    private Venta venta;
    
    
    /**
     * Precio del producto en la lista general de la empresa al momento de la venta
     * En el DBF es el ALMPRECI
     */
    @Column(name = "PRECIO_L", scale=4,precision=19,nullable=false)
    private BigDecimal precioLista;
    
    /**
     * Precio con el que se factura el producto
     * En el DBF es el ALMPREFA
     * 	
     *     CONTADO(MOS,CAM)       Es igual al precio de lista            			 
     *     CREDITO PRECIO BRUTO   Es igual al precio de lista
     *     CREDITO (Tipo='G' casi contado pero cheque post-fechado) Es el precio de lista
     *     CREDITO a Precio Neto    Es el precio despues de descuentos sobre el VentaDet.precioLista
     */
    @Column(name = "PRECIO", scale=4,precision=19,nullable=false)
    private BigDecimal precio; 
    
    
    /**
     * ES EL IMPORTE BRUTO DE LA OPERACION DE VENTA UNITARIA
     * 
     * El importe de la partida
     * 
     * (precio*cantidad)/factor
     *     
     * 
     */
    @Column(name = "IMPORTE", scale=4,precision=19,nullable=false)
    private BigDecimal importe=BigDecimal.ZERO;
    
    /**
     * Descuento real ocupado para calcular los importes de la partida
     *  
     *     
     *     
     *    CONTADO(MOS,CAM)       Se genera con el maestro ej: CreditoDBFMapper            			 
     *    CREDITO PRECIO BRUTO   Es cero
     *    CREDITO (Tipo='G')     Igual que contado
     *    CREDITO a Precio Neto  Es cero
     * 
     */
    @Column(name = "DSCTO",scale=6,precision=8,nullable=false)
    private double descuento = 0;
    
    /**
     *  (precio*cantidad)/factor + cortes
     *  
     *  Actualmente es igual al importe hasta producir ventas en SW2
     *  TODO ELIMINAR
     *  @deprecated
     */
    @Column(name = "SUBTOTAL", scale=4,precision=19,nullable=false)
    private BigDecimal subTotal=BigDecimal.ZERO;
    
    /**
     *  Descuento  para ser aplicado en nota de credito posterior a la
     *  venta y normalmente al momento de cobrar
     *    CONTADO(MOS,CAM)       Es Cero            			 
     *    CREDITO PRECIO BRUTO   Calculado por las reglas de negocio DescuentosManager
     *    CREDITO (Tipo='G')     Es Cero
     *    CREDITO a Precio Neto  Es Cero  
     * 
     */
    @Column(name = "DSCTO_NOTA",scale=6,precision=8,nullable=false)
    private double descuentoNota=0;
    
    
    @Column(name = "IMPORTE_NETO", scale=4,precision=19,nullable=false)
    private BigDecimal importeNeto=BigDecimal.ZERO;
   
    
    @Column(name = "CORTES", nullable=false)
    private int cortes=0;
    
    @Column(name = "PRECIO_CORTES", scale=4,precision=19,nullable=false)
    private BigDecimal precioCorte=BigDecimal.ZERO;
    
    /**
     * Descuento originalmente pactado para la venta ES INMUTABLE
     * 
     *    CONTADO(MOS,CAM)       Es igual a VentaDet.descuento            			 
     *    CREDITO PRECIO BRUTO   Calculado por las reglas de negocio DescuentosManager
     *    CREDITO (Tipo='G')     Es igual a VentaDet.descuento
     *    CREDITO a Precio Neto  Calculado por las reglas de negocio DescuentosManager     
     */
    @Column(name = "DSCTO_ORIG",scale=6,precision=8,nullable=false)
    private double descuentoOriginal=0;
    
    @Formula("(select ifnull(sum(X.CANTIDAD),0) " +
    		"FROM SX_INVENTARIO_DEV X where X.VENTADET_ID=INVENTARIO_ID)")
    private double devueltas=0d;
    
   
    /** Propiedades por compatibilidad con replica de SIIPAP DBF **/    
    
    
    @Column(name="SERIE",length=1)
    private String serie;
    
    @Column(name="TIPO",length=1)
    private String tipo;   
    
    
    @Column(name="SIIPAPWIN_ID",nullable=true)
    private Long siipapWinId;
    
    @Column(name = "CORTE_LARGO")
	private double corteLargo;
	
	@Column(name = "CORTE_ANCHO")
	private double corteAncho;
	
	@Column(name="CORTES_INSTRUCCION")
	@Length(max=20)
	private String instruccionesDecorte;
    
	
	@Type (type="timestamp")
	@Column(name="ORDENP",updatable=false)
	private Date ordenp;

    public VentaDet() {
    }

    
	public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }	

	

	public BigDecimal getPrecioLista() {
		return precioLista;
	}

	public void setPrecioLista(BigDecimal precioLista) {
		this.precioLista = precioLista;
	}

	public BigDecimal getPrecio() {
		return precio;
	}
	
	public BigDecimal getPrecioConIva(){
		CantidadMonetaria factor=CantidadMonetaria.pesos(1).add(CantidadMonetaria.pesos(MonedasUtils.IVA));
		return factor.multiply(getPrecio()).amount();
	}
	
	

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}
	
	public BigDecimal getImporteConIva(){
		CantidadMonetaria factor=CantidadMonetaria.pesos(1).add(CantidadMonetaria.pesos(MonedasUtils.IVA));
		return factor.multiply(getImporte()).amount();
	}
	
	

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}
	
	public BigDecimal getImporteDescuento(){
		return getImporte().multiply(BigDecimal.valueOf(getDescuento()/100));
	}

	/**
	 * @deprecated NO USAR PROXIMANENTE SE ELIMINARA
	 * @return
	 */
	public BigDecimal getSubTotal() {
		return subTotal;
	}

	/**
	 * @deprecated NO USAR PROXIMANENTE SE ELIMINARA
	 * @param subTotal
	 */
	public void setSubTotal(BigDecimal subTotal) {
		this.subTotal = subTotal;
	}

	public double getDescuentoNota() {
		return descuentoNota;
	}

	public void setDescuentoNota(double descuentoNota) {
		this.descuentoNota = descuentoNota;
	}

	public BigDecimal getImporteNeto() {
		return importeNeto;
	}	

	public double getDescuentoOriginal() {
		return descuentoOriginal;
	}

	public void setDescuentoOriginal(double descuentoOriginal) {
		this.descuentoOriginal = descuentoOriginal;
	}

	public void setImporteNeto(BigDecimal importeNeto) {
		this.importeNeto = importeNeto;
	}

	public int getCortes() {
		return cortes;
	}

	public void setCortes(int cortes) {
		this.cortes = cortes;
	}
	
	

	public double getCorteLargo() {
		return corteLargo;
	}


	public void setCorteLargo(double corteLargo) {
		this.corteLargo = corteLargo;
	}


	public double getCorteAncho() {
		return corteAncho;
	}


	public void setCorteAncho(double corteAncho) {
		this.corteAncho = corteAncho;
	}


	public BigDecimal getPrecioCorte() {
		return precioCorte;
	}

	public void setPrecioCorte(BigDecimal precioCorte) {
		this.precioCorte = precioCorte;
	}
	
	public BigDecimal getImporteCortes(){
		return getPrecioCorte().multiply(BigDecimal.valueOf(getCortes()));
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}	
	
	

	public double getDevueltas() {
		return devueltas;
	}


	public Long getSiipapWinId() {
		return siipapWinId;
	}

	public void setSiipapWinId(Long siipapWinId) {
		this.siipapWinId = siipapWinId;
	}
	
	/**
	 * Regresa el importe de la provision historica
	 * de la partida en el caso de las ventas de credito a precio 
	 * @return
	 */
	public BigDecimal getProvision(){
		BigDecimal provision=BigDecimal.ZERO;
		if(descuentoNota>0){
			BigDecimal descNota=BigDecimal.valueOf(descuentoNota/100);
			provision=importe.multiply(descNota);
		}
		return provision.abs();
	}

	public boolean equals(Object other){
		if(other==null) return false;
		if(other==this) return true;
		if(getClass()!=other.getClass()) return false;
		VentaDet det=(VentaDet)other;
		return new EqualsBuilder()
		.appendSuper(super.equals(det))
		/*.append(getTipoDocto(),det.getTipoDocto())
		.append(getSucursal(),det.getSucursal())
        .append(getFecha(),det.getFecha())
        .append(getProducto(),det.getProducto())
        .append(getUnidad(),det.getUnidad())
        .append(getCantidad(),det.getCantidad())*/
		.append(getRenglon(),det.getRenglon())
		.isEquals();
		
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.appendSuper(super.hashCode())
        /*.append(getSucursal())
        .append(getFecha())
        .append(getProducto())
        .append(getUnidad())
        .append(getCantidad())*/
        .append(getRenglon())
		.toHashCode();
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append("Suc",getSucursal())
		.append("Tipo",getTipoDocto())
		.append("Docto",getDocumento())
		.append("Serie",getSerie())
		.append("Clave",getClave())
		.append("Desc",getDescripcion())
		.append("Cant",getCantidad())
		.append("Uni",getUnidad())
		.append("Precio",getPrecio())
		.append("Importe",getImporte())
		.append("Descto",getDescuento())
		.toString();
	}


	@Override
	public String getTipoDocto() {
		return "FAC";
	}
	
	@Transient
	public Long siipapWinVentaId;


	public String getInstruccionesDecorte() {
		return instruccionesDecorte;
	}


	public void setInstruccionesDecorte(String instruccionesDecorte) {
		this.instruccionesDecorte = instruccionesDecorte;
	}


	public Date getOrdenp() {
		return ordenp;
	}


	public void setOrdenp(Date ordenp) {
		this.ordenp = ordenp;
	}


	public void setDevueltas(double devueltas) {
		this.devueltas = devueltas;
	}
	

	/**
	 * Actualiza los importes de la partida en funcion del precio,cantida,descuento,cortes y precioPorCorte
	 * 
	 */
	public void actualizar(){
		
		if(getProducto()==null)
			return;
		
		//Actualizar el importe bruto
		
		CantidadMonetaria precio=new CantidadMonetaria(getPrecio(),getVenta().getMoneda());
		double cantidad=getCantidad()/getProducto().getUnidad().getFactor();
		CantidadMonetaria importeBruto=precio.multiply(cantidad);
		setImporte(importeBruto.amount());
		actualizarKilosDelMovimiento();
	}
	
	
	//@Column(name="ESPECIAL")
	@Transient
	private boolean especial=false;
	
	//@Column(name = "LARGO", scale = 3)
	@Transient
	private double largo = 0;
	
	
	//@Column(name = "ANCHO", scale = 3)
	@Transient
	private double ancho = 0;
	
	//@Column(name="PRECIO_KILO")
	@Transient
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
	}

	public double getAncho() {
		return ancho;
	}

	public void setAncho(double ancho) {
		double old=this.ancho;
		this.ancho = ancho;
		firePropertyChange("ancho", old, ancho);
	}


	public BigDecimal getPrecioPorKilo() {
		return precioPorKilo;
	}


	public void setPrecioPorKilo(BigDecimal precioPorKilo) {
		this.precioPorKilo = precioPorKilo;
	}
	
	
	public double getKilosMillar() {
		if(getProducto().isMedidaEspecial()){
			double kk=super.getKilos();
			return Math.abs(kk/getCantidadEnUnidad());
		}else{
			return getProducto().getKilos();
		}
	}
	
	
}
