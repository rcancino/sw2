package com.luxsoft.sw3.maquila.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.inventarios.model.TrasladoDet;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.maquila.model.MovimientoConFlete;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.cxp.selectores.SelectorDeTpeParaAnalisis;
import com.luxsoft.sw3.cxp.selectores.SelectorDeTrsParaAnalisis;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeComsParaAnalisisDeFlete;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeMaqsParaAnalisis;





/**
 * Controlador y PresentationModel para la fomra y mantenimiento al analisis de flete
 * {@link AnalisisDeFlete}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeFleteFormModel extends DefaultFormModel {
	
	private EventList<MovimientoConFlete> partidasSource;
	private EventList<Producto> productos;
	private EventList<Proveedor> proveedores;
	
	protected Logger logger=Logger.getLogger(getClass());

	public AnalisisDeFleteFormModel() {
		super(new AnalisisDeFlete());
	}
	
	public AnalisisDeFleteFormModel(AnalisisDeFlete analisis) {
		super(analisis);
	}
	
	protected void init(){
		partidasSource=GlazedLists.eventList(new ArrayList<MovimientoConFlete>());
		partidasSource.addAll(getAnalisis().getEntradas());
		partidasSource.addAll(getAnalisis().getComs());
		partidasSource.addAll(getAnalisis().getTransformaciones());
		partidasSource.addAll(getAnalisis().getTraslados());
		
		addBeanPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				actualizarTotalConImporte();
				prorratearFlete();
			}			
		});
	}
	
	
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getAnalisis().getEntradas().size()==0){
			//support.addError("", "Debe registrar por lo menos una partida");
		}
		if(getAnalisis().getTotal().doubleValue()<0){
			support.addError("", "Debe registrar el importe del análisis");
		}
		//System.out.println("Partidas en UI "+);
		super.addValidation(support);
	}
	
	
	public EventList<MovimientoConFlete> getPartidasSource() {
		return partidasSource;
	}	
	
	public AnalisisDeFlete getAnalisis(){
		return (AnalisisDeFlete)getBaseBean();
	}
	
	protected EventList<Producto> getProductos(){
		if(productos==null){
			productos=new BasicEventList<Producto>();
		}
		return productos;
	}
	
	public void insertarMaquila(){
		List<EntradaDeMaquila> entradas=SelectorDeMaqsParaAnalisis.seleccionar(true);
		for(EntradaDeMaquila det:entradas){
			boolean ok=getAnalisis().agregarEntrada(det);
			if(ok){
				if(StringUtils.isBlank(det.getRemision())){
					det.setRemision(det.getRecepcion().getRemision());
				}
				prorratearFlete();
				validate();
				partidasSource.add(det);
				
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
			}
		}
	}
	public void insertarCom(){
		List<EntradaPorCompra> entradas=SelectorDeComsParaAnalisisDeFlete.seleccionar();
		for(EntradaPorCompra det:entradas){
			det.setCostoGasto(BigDecimal.ZERO);
			boolean ok=getAnalisis().agregarEntrada(det);
			
			if(ok){
				prorratearFlete();
				validate();
				partidasSource.add(det);
				
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
			}
		}
	}
	
	public void insertarTrs(){
		List<TransformacionDet> entradas=SelectorDeTrsParaAnalisis.seleccionarEntradasParaFlete();
		for(TransformacionDet det:entradas){
			boolean ok=getAnalisis().agregarTrs(det);
			if(ok){
				prorratearFlete();
				validate();
				partidasSource.add(det);
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
			}
		}
	}
	
	public void insertarTpe(){
		List<TrasladoDet> entradas=SelectorDeTpeParaAnalisis.seleccionarEntradasParaFlete();
		for(TrasladoDet det:entradas){
			boolean ok=getAnalisis().agregarTraslado(det);
			if(ok){
				prorratearFlete();
				validate();
				partidasSource.add(det);
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
			}
		}
	}
	
	
	public void elminarPartida(int index){
		MovimientoConFlete det=partidasSource.get(index);
		if(det!=null){
			if(det instanceof EntradaDeMaquila){
				EntradaDeMaquila e=(EntradaDeMaquila)det;
				boolean ok=getAnalisis().eliminarEntrada(e);
				if(ok){
					partidasSource.remove(index);
					prorratearFlete();
					validate();
					return;
				}
				
			}else if(det instanceof TransformacionDet){
				TransformacionDet trs=(TransformacionDet)det;
				boolean ok=getAnalisis().eliminarTrs(trs);
				if(ok){
					partidasSource.remove(index);
					prorratearFlete();
					validate();
					return;
				}
			}else if(det instanceof TrasladoDet){
				TrasladoDet tpe=(TrasladoDet)det;
				boolean ok=getAnalisis().eliminarTraslado(tpe);
				if(ok){
					partidasSource.remove(index);
					prorratearFlete();
					validate();
					return;
				}
			}
		}
	}
	
	
	public EventList<Proveedor> getProveedores(){
		if(proveedores==null){
			proveedores=GlazedLists.eventList(ServiceLocator2.getProveedorManager().buscarActivos());
		}
		return proveedores;
	}
	
	
	/**
	 * Actualiza el impuesto y el total de lo maquilado a partir del importe
	 * 
	 */
	private void actualizarTotalConImporte() {
		
		BigDecimal imp=getAnalisis().getImporte();		
		getAnalisis().setImpuesto(MonedasUtils.calcularImpuesto(imp));
		
		BigDecimal fleteComision=BigDecimal.valueOf(.04);
		if(AnalisisDeFleteForm.sinRetencion.isSelected())
		 fleteComision=BigDecimal.ZERO;
		BigDecimal retencion=getAnalisis().getImporte().multiply(fleteComision);
		getAnalisis().setRetencion(retencion);
		
		BigDecimal total=getAnalisis().getImporte()
			.add(getAnalisis().getImpuesto())
			.subtract(getAnalisis().getRetencion());
		getAnalisis().setTotal(total);
	}
	
	
	
	private void prorratearFlete() {
		//actualizarTotalConImporte();
		getAnalisis().prorratearFlete();
		for(int index=0;index<partidasSource.size();index++){
			MovimientoConFlete element=partidasSource.get(index);
			partidasSource.set(index, element);
			
		}
		
	}

	
	
}
