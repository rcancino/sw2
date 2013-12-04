package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Formula;
import org.hibernate.validator.Length;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Abstraccion de una factura de proveedor (Cuenta por Pagar)
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@DiscriminatorValue("FACTURA")
public class CXPFactura extends CXPCargo{
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="factura")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)	
	private Set<CXPAnalisisDet> partidas=new HashSet<CXPAnalisisDet>();
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			}
			,fetch=FetchType.LAZY,mappedBy="factura")
	private Set<AnalisisDeFactura> analisis=new HashSet<AnalisisDeFactura>();
	
	@Formula("(select sum(X.IMPORTE) FROM SX_ANALISIS X where X.CXP_ID=CXP_ID)")
	private BigDecimal analizado;
	
	@Formula("(select sum(a.IMPORTE) FROM SX_ANALISIS X join SX_ANALISISDET a on (x.ANALISIS_ID=a.ANALISIS_ID) where X.CXP_ID=CXP_ID)")
	private BigDecimal analizadoCosto;
	
	@Formula("(select sum(X.TOTAL) FROM SX_MAQ_ANALISIS_FLETE X where X.CXP_ID=CXP_ID)")
	private BigDecimal analizadoComoFlete;
	
	
	@Formula("(select sum(X.TOTAL) FROM SX_MAQ_ANALISIS_HOJEO X where X.CXP_ID=CXP_ID)")
	private BigDecimal analizadoComoHojeo;
	
	/**
	 * Util solo para registros importados
	 */
	@Column(name="SIIPAPWIN_ID")
	private Long siipapAnalisis;
	
	@Column(name="IMPRESO",nullable=true)
	private boolean impreso;
	
	//@Column(name="FRACCIONADA")
	@Transient
	private boolean fraccionada;
	
	@ManyToOne(optional=true)
	@JoinColumn (name="ANTICIPO_ID")
	private AnticipoDeCompra anticipo;
	
	
	
	@Column(name = "ANTICIPO", nullable = false)
	private boolean anticipof=false;
	
	
	public boolean isAnticipo() {
		return anticipof;
	}
	
	public void setAnticipo(boolean anticipo) {
		this.anticipof = anticipo;
	}
	
	public CXPFactura() {
	}
	
	
	public boolean isImpreso() {
		return impreso;
	}

	public void setImpreso(boolean impreso) {
		this.impreso = impreso;
	}
	

	public Long getSiipapAnalisis() {
		return siipapAnalisis;
	}

	public void setSiipapAnalisis(Long analisisId) {
		this.siipapAnalisis = analisisId;
	}
	
	

	public BigDecimal getAnalizado() {
		if(analizado==null)
			analizado=BigDecimal.ZERO;
		return analizado;
	}
	
	public BigDecimal getAnalizadoImpuesto(){
		return MonedasUtils.calcularImpuesto(getAnalizado());
	}
	public BigDecimal getAnalizadoTotal(){
		return MonedasUtils.calcularTotal(getAnalizado());
	}

	public BigDecimal getAnalizadoTotalCosto(){
		return MonedasUtils.calcularTotal(getAnalizadoCosto());
	}
	


/********** Coleccion de partidas ******************************/

	public Set<CXPAnalisisDet> getPartidas() {
		return partidas;
	}
	
	public boolean agregarPartida(final CXPAnalisisDet det){
		Assert.notNull(det);
		det.setFactura(this);
		return partidas.add(det);
	}
	
	public boolean eliminarPartida(final CXPAnalisisDet det){
		det.setFactura(null);
		return partidas.remove(det);
	}
	
	public Set<AnalisisDeFactura> getAnalisis() {
		return analisis;
	}

	public void setAnalisis(Set<AnalisisDeFactura> analisis) {
		this.analisis = analisis;
	}
	public boolean agregarAnalisis(AnalisisDeFactura a){
		a.setFactura(this);
		return this.analisis.add(a);
	}
	public boolean eliminarAnalisis(AnalisisDeFactura a){
		a.setFactura(null);
		return this.analisis.remove(a);
	}


	/**
	 * Obtiene el total del analisis
	 */
	public CantidadMonetaria getTotalAnalisis(){
		CantidadMonetaria analizado=new CantidadMonetaria(getAnalizado(),getMoneda());
		analizado=analizado.add(MonedasUtils.calcularImpuesto(analizado));
		return analizado;
	}
	
	/**
	 * Obtiene el importe del analisis en MN
	 */
	public CantidadMonetaria getImporteAnalisisMN(){
		BigDecimal tota=getAnalizado().multiply(BigDecimal.valueOf(getTc()));
		CantidadMonetaria analizado=CantidadMonetaria.pesos(tota.doubleValue());//new CantidadMonetaria(getAnalizado(),getMoneda());
		//analizado=analizado.add(MonedasUtils.calcularImpuesto(analizado));
		return analizado;
	}
	
	/**
	 * Obtiene el total del analisis en MN
	 */
	public CantidadMonetaria getTotalAnalisisMN(){
		BigDecimal tota=getAnalizado().multiply(BigDecimal.valueOf(getTc()));
		CantidadMonetaria analizado=CantidadMonetaria.pesos(tota.doubleValue());//new CantidadMonetaria(getAnalizado(),getMoneda());
		analizado=analizado.add(MonedasUtils.calcularImpuesto(analizado));
		return analizado;
	}
	
	public CantidadMonetaria getTotalFlete(){
		CantidadMonetaria flete=new CantidadMonetaria(getFlete(),getMoneda());
		CantidadMonetaria fleteIva=new CantidadMonetaria(getImpuestoflete(),getMoneda());
		CantidadMonetaria fleteRet=new CantidadMonetaria(getRetencionflete(),getMoneda());
		flete=flete.add(fleteIva).subtract(fleteRet);
		return flete;
	}
	
	/**
	 * @deprecated
	 */
	public CantidadMonetaria getImporteDescuentoFinanciero(){
		if(getDescuentoFinanciero()==0)
			return new CantidadMonetaria(0,getMoneda());
		CantidadMonetaria totalBase=getTotalAnalisis();
		CantidadMonetaria cargos=getTotalCargos();
		totalBase=totalBase.add(getTotalFlete()).add(cargos);
		
		return totalBase.multiply(getDescuentoFinanciero()/100);
	}
	
	public CantidadMonetaria getImporteDescuentoFinanciero2(){
		if(getDescuentoFinanciero()==0)
			return new CantidadMonetaria(0,getMoneda());
		CantidadMonetaria totalBase=new CantidadMonetaria(getAnalizadoTotalCosto(),getMoneda());
		CantidadMonetaria cargos=getTotalCargos();
		totalBase=totalBase.add(getTotalFlete()).add(cargos);		
		return totalBase.multiply(getDescuentoFinanciero()/100);
	}
	
	public CantidadMonetaria getPorRequisitar(){
		return getPorRequisitar(true);
	}
	
	public CantidadMonetaria getPorRequisitar(boolean conDescuentoFinanciero){
		
		//CantidadMonetaria pendiente=new CantidadMonetaria(0d,getMoneda());
		CantidadMonetaria porRequisitar=new CantidadMonetaria(0d,getMoneda());
		if(getSaldoCalculado().doubleValue()<=0)
			return porRequisitar;
		if(getTotalAnalisis().amount().doubleValue()==0){
			porRequisitar=getTotalCM();
		}else{
			porRequisitar=getTotalAnalisis();
			porRequisitar=porRequisitar.add(getTotalFlete());
			porRequisitar=porRequisitar.subtract(getBonificadoCM());
		}
		
		CantidadMonetaria cargos=getTotalCargos();
		
		porRequisitar=porRequisitar.add(cargos);
		
		if(conDescuentoFinanciero){
			CantidadMonetaria df=getImporteDescuentoFinanciero();
			porRequisitar=porRequisitar.subtract(df);
		}
		
		CantidadMonetaria requisitado=new CantidadMonetaria(getRequisitado(),getMoneda());
		porRequisitar=porRequisitar.subtract(requisitado);
		
		if(porRequisitar.amount().doubleValue()<0)
			porRequisitar=new CantidadMonetaria(0,getMoneda());
		
		return porRequisitar;
		
	}
	
	public CantidadMonetaria getPorRequisitarSimple(boolean conDescuentoFinanciero){
		
		//CantidadMonetaria pendiente=new CantidadMonetaria(0d,getMoneda());
		CantidadMonetaria porRequisitar=new CantidadMonetaria(0d,getMoneda());
		if(getSaldoCalculado().doubleValue()<=0)
			return porRequisitar;
		if(getTotalAnalisis().amount().doubleValue()==0){
			porRequisitar=getTotalCM();
		}else{
			porRequisitar=getTotalAnalisis();
			porRequisitar=porRequisitar.add(getTotalFlete());
			porRequisitar=porRequisitar.subtract(getBonificadoCM());
		}
		
		if(conDescuentoFinanciero){
			CantidadMonetaria df=getImporteDescuentoFinanciero();
			porRequisitar=porRequisitar.subtract(df);
		}
		
		CantidadMonetaria cargos=new CantidadMonetaria(getCargos(),getMoneda());
		porRequisitar=porRequisitar.add(cargos);
		
		//CantidadMonetaria requisitado=new CantidadMonetaria(getRequisitado(),getMoneda());
		//porRequisitar=porRequisitar.subtract(requisitado);
		
		if(porRequisitar.amount().doubleValue()<0)
			porRequisitar=new CantidadMonetaria(0,getMoneda());
		
		return porRequisitar;
		
	}
	
	
	/*** Metodos solo utiles para la UI ***/
	/** Relacionado con el analisis ***/
	
	@Transient
	private BigDecimal importeAnalizado;
	@Transient
	private BigDecimal impuestoAnalizado;
	@Transient
	private BigDecimal totalAnalizado;
	
	@Transient
	@Length(max=255)
	private String comentarioAnalisis;

	public BigDecimal getImporteAnalizado() {
		return importeAnalizado;
	}
	public void setImporteAnalizado(BigDecimal importeAnalizado) {
		Object old=this.importeAnalizado;
		this.importeAnalizado = importeAnalizado;
		firePropertyChange("importeAnalizado", old, importeAnalizado);
	}
	
	public BigDecimal getImpuestoAnalizado() {
		return impuestoAnalizado;
	}
	public void setImpuestoAnalizado(BigDecimal impuestoAnalizado) {
		Object old=this.impuestoAnalizado;
		this.impuestoAnalizado = impuestoAnalizado;
		firePropertyChange("impuestoAnalizado", old, impuestoAnalizado);
	}
	
	public BigDecimal getTotalAnalizado() {
		if(totalAnalizado==null)
			totalAnalizado=BigDecimal.ZERO;
		return totalAnalizado;
	}
	public void setTotalAnalizado(BigDecimal totalAnalizado) {
		Object old=this.totalAnalizado;
		this.totalAnalizado = totalAnalizado;
		firePropertyChange("totalAnalizado", old, totalAnalizado);
	}
	
	public void actualizarTotalesAnalizado(){
		BigDecimal importe=BigDecimal.ZERO;
		/*for(CXPAnalisisDet det:getPartidas()){
			importe=importe.add(det.getImporte());
		}*/
		for(AnalisisDeFactura det:getAnalisis()){
			importe=importe.add(det.getImporte());
		}
		CantidadMonetaria imp=CantidadMonetaria.pesos(importe.doubleValue());
		setImporteAnalizado(imp.amount());
		setImpuestoAnalizado(MonedasUtils.calcularImpuesto(imp).amount());
		setTotalAnalizado(MonedasUtils.calcularTotal(imp).amount());
	}


	public BigDecimal getAnalizadoComoFlete() {
		if(analizadoComoFlete==null)
			analizadoComoFlete=BigDecimal.ZERO;
		return analizadoComoFlete;
	}


	public void setAnalizadoComoFlete(BigDecimal analizadoComoFlete) {
		this.analizadoComoFlete = analizadoComoFlete;
	}
	
	public BigDecimal getAnalizadoComoHojeo() {
		if(analizadoComoHojeo==null)
			analizadoComoHojeo=BigDecimal.ZERO;
		return analizadoComoHojeo;
	}


	public void setAnalizadoComoHojeo(BigDecimal analizadoComoHojeo) {
		this.analizadoComoHojeo = analizadoComoHojeo;
	}


	public String getTipoDeFactura(){
		if(getAnalizadoComoFlete().doubleValue()>0){
			return "FLETE";
		}else if(getAnalizadoComoHojeo().doubleValue()>0){
			return "HOJEO";
		}else			
			return "NORMAL";
	}


	public boolean isFraccionada() {
		return fraccionada;
	}


	public void setFraccionada(boolean fraccionada) {
		boolean old=this.fraccionada;
		this.fraccionada = fraccionada;
		firePropertyChange("fraccionada", old, fraccionada);
	}


	public String getComentarioAnalisis() {
		return comentarioAnalisis;
	}


	public void setComentarioAnalisis(String comentarioAnalisis) {
		Object old=this.comentarioAnalisis;
		this.comentarioAnalisis = comentarioAnalisis;
		firePropertyChange("comentarioAnalisis", old, comentarioAnalisis);
	}


	public AnticipoDeCompra getAnticipo() {
		return anticipo;
	}


	public void setAnticipo(AnticipoDeCompra anticipo) {
		this.anticipo = anticipo;
	}
	
	
	@Transient
	private BigDecimal diferenciaEnAnalisis;
	@Transient
	private BigDecimal analizadoAcumulado;
	@Transient
	private BigDecimal pendienteDeAnalizar;

	public BigDecimal getDiferenciaEnAnalisis() {
		return diferenciaEnAnalisis;
	}


	public void setDiferenciaEnAnalisis(BigDecimal diferenciaEnAnalisis) {
		Object old=this.diferenciaEnAnalisis;
		this.diferenciaEnAnalisis = diferenciaEnAnalisis;
		firePropertyChange("diferenciaEnAnalisis", old, diferenciaEnAnalisis);
	}


	public BigDecimal getPendienteDeAnalizar() {
		return pendienteDeAnalizar;
	}


	public void setPendienteDeAnalizar(BigDecimal pendienteDeAnalizar) {
		Object old=this.pendienteDeAnalizar;
		this.pendienteDeAnalizar = pendienteDeAnalizar;
		firePropertyChange("pendienteDeAnalizar", old, pendienteDeAnalizar);
	}


	public BigDecimal getAnalizadoAcumulado() {
		if(analizadoAcumulado==null)
			analizadoAcumulado=BigDecimal.ZERO;
		return analizadoAcumulado;
	}


	public void setAnalizadoAcumulado(BigDecimal analizadoAcumulado) {
		Object old=this.analizadoAcumulado;
		this.analizadoAcumulado = analizadoAcumulado;
		firePropertyChange("analizadoAcumulado", old, analizadoAcumulado);
	}
	
	


	public BigDecimal getAnalizadoCosto() {
		if(analizadoCosto==null)
			analizadoCosto=BigDecimal.ZERO;
		return analizadoCosto;
	}

	public void setAnalizadoCosto(BigDecimal analizadoCosto) {
		if(analizadoCosto==null)
			analizadoCosto=BigDecimal.ZERO;
		this.analizadoCosto = analizadoCosto;
	}

	/**
	 * Total usado en algunas formas
	 * 
	 * @return
	 */
	public BigDecimal getTotalAnalizadoConFlete(){
		BigDecimal ta=getAnalizadoTotalCosto();
		ta=ta.add(getFlete())
				.add(getImpuestoflete())
				.subtract(getRetencionflete());
		return ta;
		/*
		CantidadMonetaria ta=new CantidadMonetaria(getAnalizadoTotalCosto(),getMoneda());
		
		ta=ta.add(getFlete()
				.add(getImpuestoFlete())
				.subtract(getRetencionFlete())
				);
		return ta.amount();	*/	
	}
	
	public BigDecimal getTotalAnalizadoConFleteMN(){
		//getT
		CantidadMonetaria ta=CantidadMonetaria.pesos(getAnalizadoTotalCosto()).multiply(getTc());
		
		ta=ta.add(getFleteMN()
				.add(getImpuestoFleteMN())
				.subtract(getRetencionFleteMN())
				);
		return ta.amount();		
	}
	
}
