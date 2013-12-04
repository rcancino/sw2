package com.luxsoft.sw3.maquila.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.inventarios.model.CostoHojeable;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.cxp.selectores.SelectorDeTrsParaAnalisis;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeComsParaAnalisisDeGasto;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeMaqsParaAnalisis;





/**
 * Controlador y PresentationModel para la fomra y mantenimiento al analisis de hojeo
 * {@link AnalisisDeHojeo}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeHojeoFormModel extends DefaultFormModel {
	
	private EventList<CostoHojeable> partidasSource;
	private EventList<Producto> productos;
	private EventList<Proveedor> proveedores;
	
	protected Logger logger=Logger.getLogger(getClass());

	public AnalisisDeHojeoFormModel() {
		super(new AnalisisDeHojeo());
	}
	
	public AnalisisDeHojeoFormModel(AnalisisDeHojeo analisis) {
		super(analisis);
	}
	
	protected void init(){		
		partidasSource=new BasicEventList<CostoHojeable>(0);
		partidasSource.addAll(getAnalisis().getEntradas());
		partidasSource.addAll(getAnalisis().getTransformaciones());
		partidasSource.addAll(getAnalisis().getEntradasCompras());
				if(!isReadOnly()){
			getModel("importe").addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					getAnalisis().actualizarCostos();
				}			
			});
		}
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(isReadOnly()){
			return;
		}/*
		if(getAnalisis().getEntradas().size()==0){
			support.addError("", "Debe registrar por lo menos una partida");
		}*/
		if(getAnalisis().getTotal().doubleValue()<0){
			support.addError("", "Debe registrar el importe del análisis");
		}
		super.addValidation(support);
	}
	
	public EventList getPartidasSource() {
		return partidasSource;
	}	
	
	public AnalisisDeHojeo getAnalisis(){
		return (AnalisisDeHojeo)getBaseBean();
	}
	
	protected EventList<Producto> getProductos(){
		if(productos==null){
			productos=new BasicEventList<Producto>();
		}
		return productos;
	}
	
	public void insertarMaquila(){
		List<EntradaDeMaquila> entradas=SelectorDeMaqsParaAnalisis.seleccionar(false);
		for(EntradaDeMaquila det:entradas){
			boolean ok=getAnalisis().agregarEntrada(det);
			if(ok){
				getAnalisis().actualizarCostos();
				if(StringUtils.isBlank(det.getRemision())){
					det.setRemision(det.getRecepcion().getRemision());
				}
				partidasSource.add(det);
				validate();
				
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
			}
		}
	}
	
	public void insertarTrs(){
		List<TransformacionDet> entradas=SelectorDeTrsParaAnalisis.seleccionarEntradasParaAnalisisHojeo();
		for(TransformacionDet det:entradas){
			boolean ok=getAnalisis().agregarTransformacion(det);
			if(ok){
				getAnalisis().actualizarCostos();
				//afterInserPartida(det);
				partidasSource.add(det);
				
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
			}
		}
	}
	
	public void insertarCom(){
		List<EntradaPorCompra> entradas=SelectorDeComsParaAnalisisDeGasto.seleccionar();
		for(EntradaPorCompra det:entradas){
			boolean ok=getAnalisis().agregarEntradaCompras(det);
			if(ok){
				getAnalisis().actualizarCostos();
				//afterInserPartida(det);
				partidasSource.add( det);
				
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
			}
		}
	}
	
	
	public void elminarPartida(int index){
		CostoHojeable det=(CostoHojeable)partidasSource.get(index);
		if(det!=null && (index!=-1)){
			boolean ok=false;
			if(det instanceof EntradaDeMaquila){
				EntradaDeMaquila maq=(EntradaDeMaquila)det;
				ok=getAnalisis().eliminarEntrada(maq);
			}else if(det instanceof TransformacionDet){
				TransformacionDet trs=(TransformacionDet)det;
				ok=getAnalisis().eliminarTransformacion(trs);
			}
			if(ok){
				partidasSource.remove(index);
				getAnalisis().actualizarCostos();
				validate();
			}
			
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	
	public EventList<Proveedor> getProveedores(){
		if(proveedores==null){
			proveedores=GlazedLists.eventList(ServiceLocator2.getHibernateTemplate().find("from Proveedor p where p.maquilador=true"));
		}
		return proveedores;
	}
	
	
	/**
	 * Actualiza el impuesto y el total de lo maquilado a partir del importe
	 * 
	 */
	public void actualizarTotalConImporte() {
		
		BigDecimal imp=getAnalisis().getImporte();		
		getAnalisis().setImpuesto(MonedasUtils.calcularImpuesto(imp));
		getAnalisis().setTotal(MonedasUtils.calcularTotal(getAnalisis().getImporte()));
	}
	
	public void actualizarTotalConTotal() {
		BigDecimal imp=getAnalisis().getTotal();
		getAnalisis().setImporte(MonedasUtils.calcularImporteDelTotal(imp));
		getAnalisis().setImpuesto(MonedasUtils.calcularImpuesto(getAnalisis().getImporte()));
	}
	
}
