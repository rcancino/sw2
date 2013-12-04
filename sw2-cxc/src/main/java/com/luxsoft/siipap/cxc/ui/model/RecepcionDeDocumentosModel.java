package com.luxsoft.siipap.cxc.ui.model;

import java.util.Date;
import java.util.List;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.rules.RecepcionDeDocumentosRules;

/**
 * Presentation layer para la recepcion de documentos
 * al departamenteo de cuentas por cobrar
 * 
 * @author Ruben Cancino
 *
 */
public class RecepcionDeDocumentosModel {
	
	private ValueModel fechaModel;
	private final EventList<Cargo> cuentas;
	
	public RecepcionDeDocumentosModel(final List<Cargo> cuentas) {
		this(GlazedLists.eventList(cuentas));
	}

	public RecepcionDeDocumentosModel(final EventList<Cargo> cuentas) {
		fechaModel=new ValueHolder(new Date());
		this.cuentas=cuentas;
	}
	
	public ValueModel getFechaModel(){
		return fechaModel;
	}
	
	public EventList<Cargo> getCuentas(){
		return cuentas;
	}
	
	public Date getFecha(){
		return (Date)fechaModel.getValue();
	}
	
	
	
	public void validar()throws IllegalArgumentException{
		RecepcionDeDocumentosRules.instance().validar(cuentas, getFecha());
	}
	
	/**
	 * Actualiza las cuentas por cobrar en lo relacionado 
	 * a la recepcion de documentos
	 * 
	 * @return
	 */
	public void  actualizar(){
		for(Cargo c:cuentas){
			RecepcionDeDocumentosRules.instance().recibir(c, getFecha());
		}
		
	}
	
	public TableFormat<Cargo> buildTableFormat(){
		return GlazedLists.tableFormat(Cargo.class
		,new String[]{"fecha","sucursal.nombre","documento","total","saldo"}
		,new String[]{"Fecha","Sucursal","Docto","Total","Saldo"});
	}
 
}
