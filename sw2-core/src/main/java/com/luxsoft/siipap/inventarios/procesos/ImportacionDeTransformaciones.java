package com.luxsoft.siipap.inventarios.procesos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.inventarios.service.TransformacionesManager;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;


/**
 * Permite importar transformaciones desde SX_INVENTARIO_MOV a SX_INVENTARIO_TRS
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Deprecated
public class ImportacionDeTransformaciones {
	
	private TransformacionesManager transformacionesManager;
	
	private Logger logger=Logger.getLogger(getClass());
	
	public ImportacionDeTransformaciones() {
		
	}
	
	
	public void execute(final Periodo per){
		logger.info("Importando transformaciones para el periodo: "+per);
		String hql="from MovimientoDet d where d.fecha between ? and ? " +
		" and d.concepto in(\'TRS\',\'REC\',\'REF\',\'RAU\') " +
		" order by d.sucursal.nombre,d.documento,d.renglon";
		List<MovimientoDet> movimientos=ServiceLocator2.getHibernateTemplate().find(hql, new Object[]{per.getFechaInicial(),per.getFechaFinal()});
		

		final EventList<MovimientoDet> source=GlazedLists.eventList(movimientos);
		logger.info("Movimientos a procesar: "+source.size());
		Comparator<MovimientoDet> comparator=GlazedLists.beanPropertyComparator(MovimientoDet.class, "sucursal.id", "documento");
		GroupingList<MovimientoDet> movimientosGroup=new GroupingList<MovimientoDet>(source,comparator);
		for(List<MovimientoDet> list:movimientosGroup){
			try {
				procesarDocumento(list);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
			
		}
	}
	
	
	/**
	 * Template method para procesar los documentos
	 * 
	 * @param list
	 */
	protected List<TransformacionDet> procesarDocumento(List<MovimientoDet> list){
		logger.info("Documento:"+list.get(0).getDocumento()+" Sucursal:"+list.get(0).getSucursal().getNombre());
		if(list.get(0).getDocumento()==1080L){
			logger.debug("DEBUG");
		}
		
		List<TransformacionDet> transformaciones=convertir(list);
		for(TransformacionDet det:transformaciones){
			String cos=det.getCantidad()>0?" Costo: "+det.getCosto():" Costo P:"+det.getCostoPromedio();
			logger.info("\t  Producto: "+det.getClave()	+"  Cantidad: "+det.getCantidad()+cos+ "  Renglon: "+det.getRenglon());
			try {
				getTransformacionesManager().persistirImportacion(det);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		return transformaciones;
	}
	
	public  List<TransformacionDet> convertir(final List<MovimientoDet> movs){
		//validarMismoDocumento(movs);
		int size=movs.size();
		List<TransformacionDet> res=new ArrayList<TransformacionDet>(size);
		int buff=0;
		while(buff%2==0){
			if(buff>=size) break;
			
			int salidaIndex=0;
			int entradaIndex=0;
			
			if(movs.get(buff).getCantidad()<0){
				salidaIndex=buff;
				entradaIndex=buff+1;
			}else{
				salidaIndex=buff+1;
				entradaIndex=buff;
			}
			
			final MovimientoDet salida=movs.get(salidaIndex);			
			final TransformacionDet salidaTarget=new TransformacionDet();
			BeanUtils.copyProperties(salida, salidaTarget,Inventario.class);
			salidaTarget.setId(null);			
			salidaTarget.setVersion(0);
			salidaTarget.setConceptoOrigen(salida.getConcepto());
			salidaTarget.setMoviOrigen(salida);
			salidaTarget.setCostoOrigen(salida.getCostoPromedio());
			
			final MovimientoDet entrada=movs.get(entradaIndex);
			final TransformacionDet entradaTarget=new TransformacionDet();			
			BeanUtils.copyProperties(entrada, entradaTarget,Inventario.class);
			entradaTarget.setId(null);
			entradaTarget.setVersion(0);
			entradaTarget.setConceptoOrigen(entrada.getConcepto());
			entradaTarget.setMoviOrigen(entrada);
			
			//Costo total de la salida
			CantidadMonetaria costoSalida=CantidadMonetaria.pesos(salidaTarget.getCostoPromedio());
			costoSalida=costoSalida.multiply(salidaTarget.getCantidad());
			
			//Calculamos el costo de la entrada
			BigDecimal costoEntrada=costoSalida.divide(entradaTarget.getCantidad()).amount().abs();
			entradaTarget.setCostoOrigen(costoEntrada);
			entradaTarget.setCosto(costoEntrada);
			entradaTarget.actualizarCosto();
			
			//Vinculo de Salida - Entrada
			salidaTarget.setDestino(entradaTarget);
			entradaTarget.setOrigen(salidaTarget);
			
			res.add(salidaTarget);
			res.add(entradaTarget);
			
			buff=buff+2;
		}
		return res;
	}
	
	
	
	public TransformacionesManager getTransformacionesManager() {
		//return transformacionesManager;
		return ServiceLocator2.getTransformacionesManager();
	}


	public void setTransformacionesManager(
			TransformacionesManager transformacionesManager) {
		this.transformacionesManager = transformacionesManager;
	}


	public static void main(String[] args) {
		//DBUtils.whereWeAre();
		Periodo per=Periodo.getPeriodoEnUnMes(6, 2009);
		ImportacionDeTransformaciones task=new ImportacionDeTransformaciones();
		task.execute(per);
		
		//ServiceLocator2.getHibernateTemplate().execute(new ImportacionDeTransformaciones(per));
	}

}
