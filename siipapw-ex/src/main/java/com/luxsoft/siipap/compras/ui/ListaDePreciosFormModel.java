package com.luxsoft.siipap.compras.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.event.ListEvent;

import com.luxsoft.siipap.compras.model.ListaDePrecios;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;

import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;


public class ListaDePreciosFormModel extends MasterDetailFormModel{
	
	
	
	public static Logger logger=Logger.getLogger(ListaDePreciosFormModel.class);

	public ListaDePreciosFormModel() {
		super(ListaDePrecios.class);		
	}

	public ListaDePreciosFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public ListaDePreciosFormModel(Object bean) {
		super(bean);		
	}
	
	public ListaDePrecios getLista(){
		return (ListaDePrecios)getBaseBean();
	}
	
	
	
	@Override
	public boolean manejaTotalesEstandares() {
		return false;
	}

	@SuppressWarnings("unchecked")
	protected void init(){
		super.init();
		if(getLista().getId()!=null ){
			for(ListaDePreciosDet det:getLista().getPrecios()){
				source.add(det);
			}
		}		
		//Handlers
		getModel("proveedor").addValueChangeListener(new ProveedorHandler());
	}
	
	
	@SuppressWarnings("unchecked")
	public Object insertDetalle(final Object obj){
		ListaDePreciosDet det=(ListaDePreciosDet)obj;
		if(getLista().agregarPrecio(det)){
			det.setDescuentoFinanciero(getLista().getDescuentoFinanciero());
			det.setCargo1(getLista().getCargo1());
			source.add(det);
			return det;
		}else
			return null;		
	}  
	
	
	public boolean deleteDetalle(final Object obj){
		ListaDePreciosDet part=(ListaDePreciosDet)obj;
		boolean res=getLista().eliminarPrecio(part);
		if(res){
			return source.remove(part);
		}
		return false;
	}
	
	
	public void limpiarPartidas(){
		if(getLista().getId()==null){
			getLista().eliminarPrecios();
			source.clear();
			logger.debug("Partidas eliminadas");
		}
	}
	
	public void doListUpdated(ListEvent listChanges){
		int index=listChanges.getIndex();
		Object updated=listChanges.getSourceList().get(index);
		System.out.println("Cambio detectado desde el model en :"+updated);
	}
	
	
	/**
	 * Detecta cambios en el proveedor y aplica las reglas adecuadas 
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class ProveedorHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			limpiarPartidas();			
		}		
	}
	
	

}
