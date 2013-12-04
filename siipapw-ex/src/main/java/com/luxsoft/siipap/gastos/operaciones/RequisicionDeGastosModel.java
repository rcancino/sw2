package com.luxsoft.siipap.gastos.operaciones;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;

import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.util.MonedasUtils;

public class RequisicionDeGastosModel extends MasterDetailFormModel{
	
	

	public RequisicionDeGastosModel() {
		super(Requisicion.class);		
	}

	public RequisicionDeGastosModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public RequisicionDeGastosModel(Object bean) {
		super(bean);		
	}
	
	public Requisicion getMasterBean(){
		return (Requisicion)getBaseBean();
	}
	
	@SuppressWarnings("unchecked")
	protected void init(){
		super.init();		
		if(getMasterBean().getId()!=null ){
			//System.out.println("Insertando partidas");
			//inicializarPartidas();
			for(Object  det:getMasterBean().getPartidas()){
				source.add(det);
			}
		}
		
		//Handlers		
		final MonedaHandler monedaHandler=new MonedaHandler();		
		getModel("moneda").addValueChangeListener(monedaHandler);
		definirTipoDeCambio();
	}
	
	
	@SuppressWarnings("unchecked")
	public Object insertDetalle(final Object obj){
		if(obj!=null){
			RequisicionDe det=(RequisicionDe)obj;
			boolean resModel=getMasterBean().agregarPartida(det);
			if(resModel){
				source.add(det);
				return det;
			}
			return null;			
		}
		return null;
		
	}
	
	@Override
	protected void afeterPartidaInserted(Object partida) {		
		//getMasterBean().actualizarTotal();
	}

	@Override
	protected void doListChange(){
		//System.out.println("Contenido cambio...");
		getMasterBean().actualizarTotal();
	}
	
	
	public boolean deleteDetalle(final Object obj){
		RequisicionDe part=(RequisicionDe)obj;
		boolean res=getMasterBean().eleiminarPartida(part);
		if(res){
			return source.remove(part);
		}
		return false;
	}
	
	protected void definirTipoDeCambio(){
		if(getValue("moneda").equals(MonedasUtils.PESOS)){
			setValue("tipoDeCambio", BigDecimal.ONE);
			getComponentModel("tipoDeCambio").setEnabled(false);
		}else{
			setValue("tipoDeCambio", BigDecimal.ONE);
			getComponentModel("tipoDeCambio").setEnabled(true);
		}
	}
	
	
	/**
	 * Listener que detecta cambios en la moneda  para aplicar las reglas
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class MonedaHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {			
			if("moneda".equals(evt.getPropertyName())){
				if(logger.isDebugEnabled()){
					logger.debug("Cambio de moneda detectado");
				}
				definirTipoDeCambio();
			}
			
		}
	}


	

}
