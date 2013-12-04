package com.luxsoft.siipap.inventarios.procesos;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Permite importar los movimientos TRV desde SX_INVENTARIO_MOV a SX_INVENTARIO_TRS
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ImportadorDeTRV {
	
	private Logger logger=Logger.getLogger(getClass());
	
	public void execute(final Periodo per){
		String hql="from MovimientoDet d where d.fecha between ? and ? " +
		" and d.concepto in(\'TRV\') " +
		" order by d.sucursal.nombre,d.documento,d.renglon";
		List<MovimientoDet> movimientos=ServiceLocator2.getHibernateTemplate().find(hql, new Object[]{per.getFechaInicial(),per.getFechaFinal()});
		final EventList<MovimientoDet> source=GlazedLists.eventList(movimientos);
		logger.info("TRVs a procesar: "+source.size());
		Comparator<MovimientoDet> comparator=GlazedLists.beanPropertyComparator(MovimientoDet.class, "sucursal.id", "documento");
		GroupingList<MovimientoDet> movimientosGroup=new GroupingList<MovimientoDet>(source,comparator);
		for(List<MovimientoDet> list:movimientosGroup){
			logger.info("Documento:"+list.get(0).getDocumento()+" Sucursal:"+list.get(0).getSucursal().getNombre());
			
			//Localizar la entrada
			CantidadMonetaria costoTotalSalidas=CantidadMonetaria.pesos(0);
			MovimientoDet entrada=null;
			for(MovimientoDet det:list){
				String cos=det.getCantidad()>0?" Costo: "+det.getCosto():" Costo P:"+det.getCostoPromedio();
				logger.info("\t  Producto: "+det.getClave()
						+"  Cantidad: "+det.getCantidad()+cos+ "  Renglon: "+det.getRenglon());
				if(det.getCantidad()<0){
					final double salida=det.getCantidad()/det.getFactor();
					BigDecimal cp=det.getCostoPromedio();
					CantidadMonetaria impSalida=CantidadMonetaria.pesos(cp);
					impSalida=impSalida.multiply(salida);
					costoTotalSalidas=costoTotalSalidas.add(impSalida);							
				}else{
					entrada=det;
				}
			}
			if(entrada!=null){
				CantidadMonetaria costoEntrada=costoTotalSalidas.abs().divide(entrada.getCantidad()/entrada.getFactor());
				logger.info("Entrada generada: "+entrada.getCantidad()+ "Costo calculado: "+costoEntrada);
			}
		}
	}

}
