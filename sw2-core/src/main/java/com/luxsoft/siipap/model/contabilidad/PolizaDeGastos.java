package com.luxsoft.siipap.model.contabilidad;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.AsientoDeGasto;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;

/**
 * 
 * @author RUBEN
 *@deprecated YA NO SE USA (ELIMINAR PROXIMAMENTE)
 */
public class PolizaDeGastos extends BaseBean{
	
	private Long id;
	private Integer folio;
	private String concepto;
	private String tipo;
	private Date fecha;
	private int mes;
	private int year;
	
	private List<AsientoDeGasto> registros=new ArrayList<AsientoDeGasto>();
	
	
	public PolizaDeGastos(final GFacturaPorCompra factura){
		//Assert.notNull(factura.getRequisiciondet(),"La factura no ha sido requisitada");
		//Assert.notNull(factura.getRequisiciondet().getRequisicion().getPago(),"La factura no esta pagada");
		/*final CargoAbono pago=factura.getRequisiciondet().getRequisicion().getPago();
		registrarConcepto(pago);
		registrarPeriodo(pago);
		registrarTipo(pago);
		
		registrarAsientos(factura.getCompra());
		afectarBancos(pago);
		cargoAGastos(pago);
		abonoAGastos(pago);
		cargoAIva(factura.getCompra());
		this.id=pago.getId();
		*/
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

	public List<AsientoDeGasto> getRegistros() {
		return registros;
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
		final PolizaDeGastos other = (PolizaDeGastos) obj;
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
	
	private void registrarConcepto(final CargoAbono pago){
		String forma=pago.getFormaDePago().name();
		forma=StringUtils.substring(forma, 0,2);
		String pattern="{0} {1} {2}";
		String ms=MessageFormat.format(pattern, forma,pago.getReferencia(),pago.getAFavor());
		ms=StringUtils.substring(ms, 0,120);
		setConcepto(ms);
	}
	
	private void registrarPeriodo(final CargoAbono pago){
		mes=Periodo.obtenerMes(pago.getFecha())+1;
		year=Periodo.obtenerYear(pago.getFecha());
		fecha=pago.getFecha();
	}
	
	private void registrarTipo(final CargoAbono pago){
		this.tipo=pago.getCuenta().getClave();
	}
	
	@SuppressWarnings("unchecked")
	private void registrarAsientos(final GCompra compra){
		final EventList<GCompraDet> eventList=GlazedLists.eventList(compra.getPartidas());
		final GroupingList groupList=new GroupingList(eventList,GlazedLists.beanPropertyComparator(GCompraDet.class, "sucursal.clave"));
		for(int index=0;index<groupList.size();index++){
			List<GCompraDet> row=groupList.get(index);
			registrarAsientos(row);			
		}
	}
	
	private void registrarAsientos(final List<GCompraDet> dets){
		GCompraDet det=dets.get(0);
		AsientoDeGasto asiento=new AsientoDeGasto();
		asiento.registrarCuentaContable(det);
		asiento.registrarDescripcion(det);
		if( (det.getRubro()!=null) || (det.getRubro().getRubroCuentaOrigen()!=null) ){
			ConceptoDeGasto concepto=det.getRubro().getRubroCuentaOrigen();
			String cc=concepto!=null?concepto.getDescripcion():"NA";
				//.getDescripcion();
			cc=StringUtils.substring(cc, 0,28);
			asiento.setConcepto(cc);	
		}
		asiento.registrarDescripcion(det);
		asiento.setSucursal(det.getSucursal().getNombre());
		CantidadMonetaria debe=CantidadMonetaria.pesos(0);
		for(GCompraDet part:dets){
			debe=debe.add(part.getImporteMN().abs());
		}
		asiento.setDebe(debe);	
		registros.add(asiento);
	}
	
	private void afectarBancos(final CargoAbono pago){
		AsientoDeGasto a1=new AsientoDeGasto();		
		a1.setCuenta(pago.getCuenta().getCuentaContable());
		String c=MessageFormat.format("{0} {1}", pago.getCuenta().getDescripcion(),pago.getCuenta().getNumero());
		c=StringUtils.substring(c, 0,28);
		a1.setConcepto(c);
		a1.setHaber(pago.getImporteMN().abs());
		a1.setDescripcion(StringUtils.substring(getConcepto(), 0,28));
		registros.add(a1);
	}
	
	private void cargoAGastos(final CargoAbono pago){
		AsientoDeGasto a1=new AsientoDeGasto();
		a1.setConcepto("GASTOS");
		a1.setCuenta("900-0002-000");
		a1.setDescripcion(StringUtils.substring(getConcepto(), 0,28));
		a1.setDebe(pago.getImporteMNSinIva().abs());
		registros.add(a1);
	}
	
	private void abonoAGastos(final CargoAbono pago){
		AsientoDeGasto a1=new AsientoDeGasto();
		a1.setConcepto("GASTOS");
		a1.setCuenta("901-0002-000");
		a1.setDescripcion(StringUtils.substring(getConcepto(), 0,28));
		a1.setHaber(pago.getImporteMNSinIva().abs());
		registros.add(a1);
	}
	
	private void cargoAIva(final GCompra compra){
		CantidadMonetaria importe=CantidadMonetaria.pesos(0);
		for(GCompraDet det:compra.getPartidas()){
			importe=importe.add(det.getImpuestoMN().abs());
		}
		AsientoDeGasto a1=new AsientoDeGasto();
		a1.setConcepto("IVA EN GASTOS");
		a1.setCuenta("117-0001-003");
		a1.setDescripcion(StringUtils.substring(getConcepto(), 0,28));
		a1.setDebe(importe);
		registros.add(a1);
	}
	
	
	public CantidadMonetaria getCuadre(){
		CantidadMonetaria debe=CantidadMonetaria .pesos(0);
		CantidadMonetaria haber=CantidadMonetaria.pesos(0);
		for(AsientoDeGasto a:registros){
			debe=debe.add(a.getDebe());
			haber=haber.add(a.getHaber());
		}
		return debe.subtract(haber);
	}
	
	public CantidadMonetaria getDebe(){
		CantidadMonetaria debe=CantidadMonetaria .pesos(0);
		for(AsientoDeGasto a:registros){
			debe=debe.add(a.getDebe());
		}
		return debe;
		
	}
	
	public CantidadMonetaria getHaber(){
		CantidadMonetaria haber=CantidadMonetaria.pesos(0);
		for(AsientoDeGasto a:registros){
			haber=haber.add(a.getHaber());
		}
		return haber;
	}
	
	

}
