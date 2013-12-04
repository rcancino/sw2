package com.luxsoft.siipap.gastos.operaciones;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.binding.PresentationModel;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.TipoDeCompra;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.util.MonedasUtils;

public class OCompraModel extends MasterDetailFormModel{

	public OCompraModel() {
		super(GCompra.class);		
	}

	public OCompraModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public OCompraModel(Object bean) {
		super(bean);		
	}
	
	public GCompra getCompra(){
		return (GCompra)getBaseBean();
	}
	
	@SuppressWarnings("unchecked")
	protected void init(){
		super.init();		
		if(getCompra().getId()!=null ){
			System.out.println("Insertando partidas");
			//inicializarPartidas();
			for(GCompraDet det:getCompra().getPartidas()){
				source.add(det);
			}
		}
		
		//Handlers
		getModel("proveedor").addValueChangeListener(new ProveedorHandler());
		getModel("tipo").addValueChangeListener(new TipoDeCompraHandler());
		final MonedaHandler monedaHandler=new MonedaHandler();
		addBeanPropertyChangeListener(monedaHandler);
		addPropertyChangeListener(PresentationModel.PROPERTYNAME_BEAN,monedaHandler);
		definirTipoDeCambio();
	}
	
	
	@SuppressWarnings("unchecked")
	public Object insertDetalle(final Object obj){
		if(obj!=null){
			GCompraDet det=(GCompraDet)obj;
			boolean resModel=getCompra().agregarPartida(det);
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
		getCompra().actualizarTotal();
	}

	@Override
	protected void doListChange(){
		getCompra().actualizarTotal();
	}
	
	
	public boolean deleteDetalle(final Object obj){
		GCompraDet part=(GCompraDet)obj;
		boolean res=getCompra().removerPartida(part);
		if(res){
			return source.remove(part);
		}
		return false;
	}
	
	protected void definirTipoDeCambio(){
		
		if(getValue("moneda").equals(MonedasUtils.PESOS)){
			setValue("tc", BigDecimal.ONE);
			getComponentModel("tc").setEnabled(false);
		}else {
			if(getValue("tc")==null){
				setValue("tc", BigDecimal.ONE);
				getComponentModel("tc").setEnabled(true);
			}
		}
	}
	
	protected EventList createPartidasSource(){
		return source;
	}
	/*
	public void inicializarPartidas(){
		GCompraDao dao=(GCompraDao)ServiceLocator2.instance().getContext().getBean("compraDao");
		dao.inicializarPartidas(getCompra());
	}*/
	
	/**
	 * Detecta cambios en el proveedor y aplica las reglas adecuadas 
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class ProveedorHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if(logger.isDebugEnabled()){
				logger.debug("Cambio de proveedor detectado: "+evt.getNewValue());
			}
			getCompra().actualizar();
		}		
	}
	
	private class TipoDeCompraHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if(logger.isDebugEnabled()){
				logger.debug("Cambio el tipo de compra:"+evt.getNewValue());
			}
			TipoDeCompra tipo=(TipoDeCompra)evt.getNewValue();
			switch (tipo) {
			case CARGAINICIAL:
				setValue("comentario", "GASTO REGISTRADO COMO PARTE DE LA CARGA INICIAL");
			default:
				break;
			}
		}
	}
	
	/**
	 * Listener que detecta cambios en el tipo de cambio para aplicar las reglas
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
