package com.luxsoft.sw3.cfd;

import net.sf.jasperreports.engine.data.JRTableModelDataSource;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;


public class CFDTableModelUtils {
	
	
	
	public static JRTableModelDataSource getTableModel(NotaDeCreditoDevolucion notaDevo){
		Devolucion devo=notaDevo.getDevolucion();
		final EventList<DevolucionDeVenta> conceptos=GlazedLists.eventList(devo.getPartidas());
		String[] columnas= {
				"cantidadEnUnidad"
				,"clave"
				,"descripcion"
				,"producto.kilos"
				,"producto.gramos"
				,"precio"
				,"ventaDet.importe"
				,"instruccionesDecorte"
				,"producto.modoDeVenta"
				,"clave"
				,"producto.modoDeVenta"
				,"producto.unidad.unidad"
				,"ordenp"
				,"precioConIva"
				,"importeConIva"
				};
		String[] etiquetas={
				"CANTIDAD"
				,"CLAVE"
				,"DESCRIPCION"
				,"KXM"
				,"GRAMOS"
				,"PRECIO"
				,"IMPORTE"
				,"CORTES_INSTRUCCION"
				,"MDV"
				,"GRUPO"
				,"MDV"
				,"UNIDAD"
				,"ORDENP"
				,"PRECIO_IVA"
				,"IMPORTE_IVA"
				};
		final TableFormat tf=GlazedLists.tableFormat(DevolucionDeVenta.class,columnas, etiquetas);			
		final EventTableModel tableModel=new EventTableModel(conceptos,tf);
		final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
		return tmDataSource;		
	}

}
