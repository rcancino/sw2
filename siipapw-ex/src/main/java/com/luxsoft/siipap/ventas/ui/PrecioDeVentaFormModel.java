package com.luxsoft.siipap.ventas.ui;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVenta;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVentaDet;
import com.luxsoft.siipap.ventas.ui.PaginaForm.PaginaBean;

public class PrecioDeVentaFormModel extends MasterDetailFormModel{
	
	
	public PrecioDeVentaFormModel() {
		super(new ListaDePreciosVenta(),false);
	}

	public PrecioDeVentaFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public PrecioDeVentaFormModel(Object bean) {
		super(bean);
	}
	
	public ListaDePreciosVenta getLista(){
		return (ListaDePreciosVenta)getBaseBean();
	}
	
	public ListaDePreciosVenta commit(){
		ListaDePreciosVenta lista=getLista();
		logger.info("Lista completada: "+lista.getId()+ "Precios: "+lista.getPrecios().size()+ "Precios en grid: "+source.size()+" Precios en partidasSource:"+partidasSource.size());
		lista=ServiceLocator2.getListaDePreciosVentaManager().salvar(lista);
		return lista;
	}

	@Override
	public boolean manejaTotalesEstandares() {
		return false;
	}

	protected void init(){
		super.init();		
		if(!getLista().getPrecios().isEmpty()){
			source.addAll(getLista().getPrecios());
		}
	}
	
	protected EventList createPartidasSource(){
		return source;	
	}	
	

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getLista().getTcDolares()<=0){
			support.getResult().addError("Registre el T.C. en Dolares");
		}
		if(getLista().getTcDolares()<=0){
			support.getResult().addError("Registre el T.C en Euros");
		}if(getLista().getPrecios().isEmpty()){
			support.getResult().addError("Debe registrar al menos un precio");
		}
	}

	
	
	@Override
	public boolean deleteDetalle(Object obj) {
		ListaDePreciosVentaDet det=(ListaDePreciosVentaDet)obj;		
		boolean res=getLista().removerPrecio(det);
		if(res)
			source.remove(det);
		return res;
	}
	
	public void agregarPrecio(final Producto p){
		ListaDePreciosVentaDet det=new ListaDePreciosVentaDet();
		det.setProducto(p);
		det.setPrecioAnterior(BigDecimal.valueOf(p.getPrecioContado()));
		det.setPrecio(det.getPrecioAnterior());
		det.setPrecioAnteriorCredito(BigDecimal.valueOf(p.getPrecioCredito()));
		det.setPrecioCredito(det.getPrecioAnteriorCredito());
		det.setPresentacion("NA");
				
		if(p.getProveedor()!=null){
			det.setProveedorClave(p.getProveedor().getClave());
			det.setProveedorNombre(p.getProveedor().getNombreRazon());
			ListaDePreciosDet precio=ServiceLocator2.getListaDePreciosDao().buscarPrecioVigente(p,p.getProveedor(), new Date());
			if(precio!=null){
				BigDecimal costo=getCostoEnMN(precio);
				det.setCosto(costo);
				det.setCostoUltimo(getCostoUltimoEnMN(precio));
			}
		}
		det.actualizarFactor(true);
		det.actualizarFactor(false);
		boolean res=getLista().agregarPrecio(det);
		if(res)
			source.add(det);
	}
	

	public void congiurarPagina(SortedList<ListaDePreciosVentaDet> sortedPartidas) {
		if(sortedPartidas.isEmpty())
			return;
		PaginaBean config=PaginaForm.showForm();
		if(config==null)
			return;
		for(ListaDePreciosVentaDet det:sortedPartidas){
			det.setPagina(config.getPagina());
			det.setColumna(config.getColumna());
			det.setGrupo(config.getGrupo());
			det.setPresentacion(config.getPresentacion());
			partidasSource.set(partidasSource.indexOf(det), det);
		}
		
	}
	
	/**
	 * Reasigna el costo desde otro proveedor
	 * 
	 * @param selected
	 */
	public void reasignarCosto(EventList<ListaDePreciosVentaDet> selected){
		final SelectorDePreciosProveedor selector=new SelectorDePreciosProveedor();
		for(ListaDePreciosVentaDet det:selected){
			selector.setProdClave(det.getProducto().getClave());
			selector.open();
			if(!selector.hasBeenCanceled()){
				ListaDePreciosDet precio=selector.getSelected();
				BigDecimal costo=getCostoEnMN(precio);
				det.setCosto(costo);
				det.setProveedorClave(precio.getLista().getProveedor().getClave());
				det.setProveedorNombre(precio.getLista().getProveedor().getNombre());
				det.actualizarFactor(true);
				det.actualizarFactor(false);
				
				partidasSource.set(partidasSource.indexOf(det),det);
			}
			
		}
	}
	
	private BigDecimal getCostoEnMN(ListaDePreciosDet precio){
		CantidadMonetaria costo=precio.getCosto();
		Currency moneda=precio.getCosto().getCurrency();
		if(!moneda.equals(MonedasUtils.PESOS)){
			double tc=0;
			if(moneda.equals(MonedasUtils.DOLARES))
				tc=getLista().getTcDolares();
			else if(moneda.equals(MonedasUtils.EUROS))
				tc=getLista().getTcEuros();
			else
				tc=0;
			costo=costo.multiply(tc);
		}	
		return costo.amount();
	}
	
	private BigDecimal getCostoUltimoEnMN(ListaDePreciosDet precio){
		CantidadMonetaria costo=precio.getCostoUltimo();
		Currency moneda=precio.getCosto().getCurrency();
		if(!moneda.equals(MonedasUtils.PESOS)){
			double tc=0;
			if(moneda.equals(MonedasUtils.DOLARES))
				tc=getLista().getTcDolares();
			else if(moneda.equals(MonedasUtils.EUROS))
				tc=getLista().getTcEuros();
			else
				tc=0;
			costo=costo.multiply(tc);
		}	
		return costo.amount();
	}

	/**
	 * Actualiza los costos de las partidas
	 */
	public void actualizarCostos() {
		System.out.println("PArtidas: "+source.size());
		for(int index=0;index<source.size();index++){
			Object obj = source.get(index);
			ListaDePreciosVentaDet det=(ListaDePreciosVentaDet)obj;
			Producto p=det.getProducto();
			if(p.getProveedor()!=null){
				det.setProveedorClave(p.getProveedor().getClave());
				det.setProveedorNombre(p.getProveedor().getNombreRazon());
				ListaDePreciosDet precio=ServiceLocator2.getListaDePreciosDao().buscarPrecioVigente(p,p.getProveedor(), new Date());
				if(precio!=null){
					BigDecimal costo=getCostoEnMN(precio);
					det.setCosto(costo);
					det.setCostoUltimo(getCostoUltimoEnMN(precio));
				}
			}
			det.actualizarFactor(true);
			det.actualizarFactor(false);
			source.set(index, det);
		}		
	}
	 

}
