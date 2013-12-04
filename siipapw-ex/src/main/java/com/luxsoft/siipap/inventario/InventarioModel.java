package com.luxsoft.siipap.inventario;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.service.InventarioManager;
import com.luxsoft.siipap.service.ServiceLocator2;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

/**
 * Estado y comportamiento relacionados con movimientos de inventarios
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class InventarioModel {
	
	
	private EventList<Inventario> inventario;
	private EventList<Movimiento> movimientos;
	
	public InventarioModel(){
		init();
	}
	
	public void init(){
		inventario=GlazedLists.threadSafeList(new BasicEventList<Inventario>());
		movimientos=GlazedLists.threadSafeList(new BasicEventList<Movimiento>());
	}
	
	public void loadInventario(){
		inventario.clear();
		//inventario.addAll(getInventarioManager().getAllMovimientos());
	}
	public void loadMovimientos(){
		movimientos.clear();
		movimientos.addAll(getInventarioManager().getAll());
	}
	
	public EventList<Inventario> getInventario() {
		return inventario;
	}

	public EventList<Movimiento> getMovimientos() {
		return new UniqueList<Movimiento>(movimientos,GlazedLists.beanPropertyComparator(Movimiento.class, "id"));
	}

	public InventarioManager getInventarioManager(){
		return ServiceLocator2.getInventarioManager();
	}

}
