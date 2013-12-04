package com.luxsoft.siipap.inventarios.model;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.validator.Length;

import com.luxsoft.siipap.cxp.model.AnalisisDeTransformacion;
import com.luxsoft.siipap.maquila.model.MovimientoConFlete;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;

/**
 * Detalle de transformacion
 * 
 * @author Ruben Cancino
 *
 */
@Entity

@Table(name="SX_INVENTARIO_TRS",uniqueConstraints=@UniqueConstraint(
		columnNames={"DOCUMENTO","TRTIP","SUCURSAL_ID","CLAVE","RENGLON"}))
public class TransformacionDet extends Inventario implements MovimientoConFlete,CostoHojeable{
	
	@ManyToOne(optional=true)
	@JoinColumn (name="TRANSFORMACION_ID",nullable=true)
	private Transformacion transformacion;
	
	@OneToOne(optional=true,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	    @JoinColumn(
	    	name="DESTINO_ID")
	private TransformacionDet destino;
	 
	@OneToOne(optional=true, mappedBy="destino")
	private TransformacionDet origen;
		
	private BigDecimal costoOrigen=BigDecimal.ZERO;
	
	private BigDecimal gastos=BigDecimal.ZERO;
	
	@Column(name="GASTO_COMENTARIO",length=100)
	@Length(max=100)
	private String comentarioGasto;
	
	@Column(name="GASTO_DOCTO",length=20)
	@Length(max=20)
	private String documentoGasto;
	
	@Column(name="TRTIP",length=3,nullable=false)
	private String conceptoOrigen="TRS";
	
	@ManyToOne(optional=true) 
    @JoinColumn(name="ANALISIS_ID")
	private AnalisisDeTransformacion analisis;
	
	@ManyToOne(optional=true) 
    @JoinColumn(name="ANALISIS_FLETE_ID")
	private AnalisisDeFlete analisisFlete;
	
	@ManyToOne(optional=true) 
    @JoinColumn(name="ANALISIS_HOJEO_ID")
	private AnalisisDeHojeo analisisHojeo;
	
	public Transformacion getTransformacion() {
		return transformacion;
	}

	public void setTransformacion(Transformacion transformacion) {
		this.transformacion = transformacion;
	}

	@Override
	public String getTipoDocto() {
		return "TRS";
	}

	public TransformacionDet getOrigen() {
		return origen;
	}

	public void setOrigen(TransformacionDet origen) {
		this.origen = origen;
	}

	public TransformacionDet getDestino() {
		return destino;
	}

	public void setDestino(TransformacionDet destino) {
		this.destino = destino;
	}
	
	public BigDecimal getCostoOrigen() {
		if(costoOrigen==null)
			costoOrigen=BigDecimal.ZERO;
		return costoOrigen;
	}

	public void setCostoOrigen(BigDecimal costoOrigen) {
		this.costoOrigen = costoOrigen;
	}
	
	public BigDecimal getImporteGasto(){
		return CantidadMonetaria.pesos(getGastos()).multiply(getCantidadEnUnidad()).amount();
	}

	public BigDecimal getGastos() {
		return gastos;
	}

	public void setGastos(BigDecimal gastos) {
		this.gastos = gastos;
	}

	public String getComentarioGasto() {
		return comentarioGasto;
	}

	public void setComentarioGasto(String comentarioGasto) {
		this.comentarioGasto = comentarioGasto;
	}

	public String getDocumentoGasto() {
		return documentoGasto;
	}

	public void setDocumentoGasto(String documentoGasto) {
		this.documentoGasto = documentoGasto;
	}

	public String getConceptoOrigen() {
		return conceptoOrigen;
	}

	public void setConceptoOrigen(String conceptoOrigen) {
		this.conceptoOrigen = conceptoOrigen;
	}
	
	public AnalisisDeTransformacion getAnalisis() {
		return analisis;
	}

	public void setAnalisis(AnalisisDeTransformacion analisis) {
		this.analisis = analisis;
	}
	
	public AnalisisDeFlete getAnalisisFlete() {
		return analisisFlete;
	}

	public void setAnalisisFlete(AnalisisDeFlete analisisFlete) {
		this.analisisFlete = analisisFlete;
	}

	@Transient
	private MovimientoDet moviOrigen;

	public MovimientoDet getMoviOrigen() {
		return moviOrigen;
	}

	public void setMoviOrigen(MovimientoDet moviOrigen) {
		this.moviOrigen = moviOrigen;
	}
	
	public void actualizarCosto(){
		setCosto(getCostoOrigen()
				.add(getGastos())
				.add(getCostoCorte())
				.add(getCostoFlete())
				);
	}
	
	/**
	 * Actualiza el costo de la entrada a partir del costo origen de la salida
	 * 
	 * Nota el costo  promedio tambien es copiada de la salida
	 * 
	 */
	public void actualizarCostoOrigen(){
		if(getCantidad()>0){
			if(getOrigen()!=null){
				
				//Costo total de la salida
				TransformacionDet salida=getOrigen();
				CantidadMonetaria costoSalida=CantidadMonetaria.pesos(salida.getCostoOrigen());
				costoSalida=costoSalida.multiply(salida.getCantidadEnUnidad());
				
				
				//Calculamos el costo de la entrada
				BigDecimal costoEntrada=costoSalida.divide(getCantidadEnUnidad()).amount().abs();
				setCostoOrigen(costoEntrada);
				costoEntrada=getCostoOrigen().add(getGastos());
				setCosto(costoEntrada);
				//setCostoPromedio(salida.getCostoPromedio());
			}
		}
	}
	
	public BigDecimal getCostoTotal(){
		return getCostoOrigen().multiply(BigDecimal.valueOf(getCantidadEnUnidad()));
	}
/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime;
		result = prime * result + ((destino == null) ? 0 : destino.hashCode());
		result = prime * result + ((origen == null) ? 0 : origen.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransformacionDet other = (TransformacionDet) obj;
		return new EqualsBuilder()
		.append(lhs, rhs)
	}
	*/
	

	@Column(name="COSTO_FLETE",nullable=false,scale=6,precision=16)
	private BigDecimal costoFlete=BigDecimal.ZERO;
	
	@Column(name="REMISION",length=20)
	public String  remision;

	public BigDecimal getCostoFlete() {
		return costoFlete;
	}

	public void setCostoFlete(BigDecimal costoFlete) {
		Object old=this.costoFlete;
		this.costoFlete = costoFlete;
		firePropertyChange("costoFlete", old, costoFlete);
	}

	public BigDecimal getImporteDelFlete(){
		BigDecimal imp=getCostoFlete().multiply(BigDecimal.valueOf(getCantidadEnUnidad()));
		return CantidadMonetaria.pesos(imp).amount();
	}

	public String getRemision() {
		return remision;
	}

	public void setRemision(String remision) {
		this.remision = remision;
	}
	
	@Column(name="COSTO_CORTE",nullable=false,scale=6,precision=16)
	private BigDecimal costoCorte=BigDecimal.ZERO;
	
	public BigDecimal getCostoCorte() {
		return costoCorte;
	}

	public void setCostoCorte(BigDecimal costoCorte) {
		Object old=this.costoCorte;
		this.costoCorte = costoCorte;
		firePropertyChange("costoCorte", old, costoCorte);
	}
	
	public BigDecimal getCostoMateria() {
		return getCostoOrigen();
	}

	public AnalisisDeHojeo getAnalisisHojeo() {
		return analisisHojeo;
	}

	public void setAnalisisHojeo(AnalisisDeHojeo analisisHojeo) {
		this.analisisHojeo = analisisHojeo;
	}
	
	

}
