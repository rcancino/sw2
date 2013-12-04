package com.luxsoft.siipap.model.contabilidad;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;


import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;



/**
 * 
 * @author RUBEN
 *
 */
public class Poliza extends BaseBean{
	
	private Long id=0l;
	private Integer folio=0;
	private String concepto;
	private String tipo;	
	private Date fecha;
	private int mes;
	private int year;
	private String sucursalNombre;
	private int sucursalId;
	private String exportName;
	
	private List<AsientoContable> registros=new ArrayList<AsientoContable>();
	
	
	public Poliza(){
	}
	
	/**
	 * @return the concepto
	 */
	public String getConcepto() {
		return concepto;
	}
	/**
	 * @param concepto the concepto to set
	 */
	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}
	/**
	 * @return the fecha
	 */
	public Date getFecha() {
		return fecha;
	}
	/**
	 * @param fecha the fecha to set
	 */
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	/**
	 * @return the foliio
	 */
	public Integer getFolio() {
		return folio;
	}
	/**
	 * @param foliio the foliio to set
	 */
	public void setFolio(Integer foliio) {
		this.folio = foliio;
	}
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the mes
	 */
	public int getMes() {
		return mes;
	}
	/**
	 * @param mes the mes to set
	 */
	public void setMes(int mes) {
		this.mes = mes;
	}
	/**
	 * @return the tipo
	 */
	public String getTipo() {
		return tipo;
	}
	/**
	 * @param tipo the tipo to set
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}
	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	public List<AsientoContable> getRegistros() {
		return registros;
	}
	
	public void agregarAsiento(final AsientoContable as){
		registros.add(as);
		as.setPoliza(this);
	}
	
	public void removerAsiento(final AsientoContable as){
		registros.remove(as);
		as.setPoliza(null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((folio == null) ? 0 : folio.hashCode());
		result = PRIME * result + mes;
		result = PRIME * result + year;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Poliza other = (Poliza) obj;
		if (folio == null) {
			if (other.folio != null)
				return false;
		} else if (!folio.equals(other.folio))
			return false;
		if (mes != other.mes)
			return false;
		if (year != other.year)
			return false;
		return true;
	}
	
	
	public String toString(){
		String pattern="Año:{0} Mes:{1}  Folio:{2}";
		return MessageFormat.format(pattern,year,mes,folio);
	}	
	
		
	
	public CantidadMonetaria getCuadre(){
		CantidadMonetaria debe=CantidadMonetaria .pesos(0);
		CantidadMonetaria haber=CantidadMonetaria.pesos(0);
		for(AsientoContable a:registros){
			debe=debe.add(a.getDebe());
			haber=haber.add(a.getHaber());
		}
		return debe.subtract(haber);
	}
	
	public CantidadMonetaria getDebe(){
		CantidadMonetaria debe=CantidadMonetaria .pesos(0);
		for(AsientoContable a:registros){
			debe=debe.add(a.getDebe());
		}
		return debe;
		
	}
	
	public CantidadMonetaria getHaber(){
		CantidadMonetaria haber=CantidadMonetaria.pesos(0);
		for(AsientoContable a:registros){
			haber=haber.add(a.getHaber());
		}
		return haber;
	}
	
	public boolean isValida(){
		return getCuadre().amount().doubleValue()==0;
	}
	
	public void sincronizar(){
		for(AsientoContable as:registros){
			as.setPoliza(this);
		}
	}

	public String getSucursalNombre() {
		return sucursalNombre;
	}

	public void setSucursalNombre(String descripcion) {
		this.sucursalNombre = descripcion;
	}
	
	

	public int getSucursalId() {
		return sucursalId;
	}

	public void setSucursalId(int sucursalId) {
		this.sucursalId = sucursalId;
	}

	/**
	 * El nombre para la exportacion a poliza modelo COI
	 * 
	 * @return
	 */
	public String getExportName() {
		return exportName;
	}

	public void setExportName(String exportName) {
		this.exportName = exportName;
	}
	

	public void ordenarPartidas(String property,String... others){
		Comparator<AsientoContable> c=GlazedLists.beanPropertyComparator(AsientoContable.class,property, others);
		Collections.sort(this.registros,c);
	}
	
	public void depurar(){
		CollectionUtils.filter(registros, new Predicate(){

			public boolean evaluate(Object object) {
				AsientoContable a=(AsientoContable)object;
				return a.getDebe().add(a.getHaber()).amount().abs().doubleValue()>0;
			}
			
		});
	}
	
	public void ordenNatural(){
		Collections.sort(this.registros,GlazedLists.beanPropertyComparator(AsientoContable.class, "orden"));
	}

}
