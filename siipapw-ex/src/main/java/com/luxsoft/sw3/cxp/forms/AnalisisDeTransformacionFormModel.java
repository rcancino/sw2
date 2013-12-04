package com.luxsoft.sw3.cxp.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxp.model.AnalisisDeTransformacion;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.cxp.selectores.SelectorDeTrsParaAnalisis;






/**
 * Controlador y PresentationModel para la fomra y mantenimiento al analisis de flete
 * {@link AnalisisDeFlete}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeTransformacionFormModel extends DefaultFormModel {
	
	private EventList<TransformacionDet> partidasSource;
	private EventList<Producto> productos;
	private EventList<Proveedor> proveedores;
	
	protected Logger logger=Logger.getLogger(getClass());

	public AnalisisDeTransformacionFormModel() {
		super(new AnalisisDeTransformacion());
	}
	
	public AnalisisDeTransformacionFormModel(AnalisisDeTransformacion analisis) {
		super(analisis);
	}
	
	protected void init(){
		partidasSource=GlazedLists.eventList(new ArrayList<TransformacionDet>());
		partidasSource.addAll(getAnalisis().getPartidas());
		addBeanPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				actualizarTotalConImporte();
				prorratear();
			}			
		});
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getAnalisis().getPartidas().size()==0){
			support.addError("", "Debe registrar por lo menos una partida");
		}
		if(getAnalisis().getTotal().doubleValue()<0){
			support.addError("", "Debe registrar el importe del análisis");
		}
		super.addValidation(support);
	}
	
	
	public EventList<TransformacionDet> getPartidasSource() {
		return partidasSource;
	}	
	
	public AnalisisDeTransformacion getAnalisis(){
		return (AnalisisDeTransformacion)getBaseBean();
	}
	
	protected EventList<Producto> getProductos(){
		if(productos==null){
			productos=new BasicEventList<Producto>();
		}
		return productos;
	}
	
	
	public void insertarTransformaciones(){
		List<TransformacionDet> entradas=SelectorDeTrsParaAnalisis.pendientesDeAnalizar();
		for(TransformacionDet det:entradas){
			boolean ok=getAnalisis().agregarTransformacion(det);
			if(ok){
				prorratear();
				validate();
				partidasSource.add(det);
				
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
			}
		}
	}
	
	public void afterInserPartida(TransformacionDet det){
		validate();
	}
	
	public void elminarPartida(int index){
		TransformacionDet det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getAnalisis().eliminarTransformacion(det);
			if(ok){
				partidasSource.remove(index);
				prorratear();
				validate();
				return;
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
		BigDecimal fleteComision=BigDecimal.valueOf(0.0);
		BigDecimal retencion=getAnalisis().getImporte().multiply(fleteComision);
		getAnalisis().setRetencion(retencion);
		
		BigDecimal total=getAnalisis().getImporte()
			.add(getAnalisis().getImpuesto())
			.subtract(getAnalisis().getRetencion());
		getAnalisis().setTotal(total);
	}
	
	private void prorratear() {
		getAnalisis().actualizarCotsos();
		//actualizarTotalConImporte();
		/*
		
		for(int index=0;index<partidasSource.size();index++){
			MovimientoConFlete element=partidasSource.get(index);
			partidasSource.set(index, element);
		}*/
		
	}

	
	
}
