package com.luxsoft.siipap.ventas.consultas;

import java.util.Date;
import java.util.List;


import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.AnalisisDeVenta;
import com.luxsoft.siipap.ventas.model.AnalisisDeVentasManager;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

/**
 * Analisis de ventas por sucursal
 * 
 * @author Ruben Cancino
 *
 */
public class AnalisisPorSucursalPanel extends AbstractAnalisisPanel{
	
	private Integer year;
	
	public AnalisisPorSucursalPanel(){
		year=DateUtil.toYear(new Date());
	}

	/**
	 * private String linea;
	private String sucursal;
	private String tipo;	
	private double ventaBruta;
	private double descuento;
	private double utilidad;
	private Integer mes;
	 */
	@Override
	protected TableFormat buildTableFormat() {
		return GlazedLists.tableFormat(
				AnalisisDeVenta.class
				,new String[]{"sucursal","linea","tipo","ventaBruta"}
				,new String[]{"Sucursal","Línea","Tipo","Venta B"}
				);
	}

	@Override
	protected List loadData() {
		return AnalisisDeVentasManager.buscarVentasPorSucursal(year); 
		
	}
	
}
