package com.luxsoft.siipap.inventarios.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.impl.GenericManagerImpl;
import com.luxsoft.siipap.util.DBUtils;

/**
 * Implementacion de {@link TransformacionesManager}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class TransformacionesManagerImpl extends GenericManagerImpl<TransformacionDet, String> implements TransformacionesManager{

	private HibernateTemplate hibernateTemplate;
	
	private Logger logger=Logger.getLogger(getClass());
	
	public TransformacionesManagerImpl(GenericDao<TransformacionDet, String> genericDao) {
		super(genericDao);
	}

	
	
	/**
	 * Persiste una  transformaciones que fue importada desde un bean {@link MovimientoDet}
	 * 
	 * Opera en una sola transaccion
	 * 	 Al persistir cada transformacion elimina el movimiento origen
	 * 
	 * @param data
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public TransformacionDet persistirImportacion(TransformacionDet data){
		if(data.getCantidad()<0){ //Solo persistimos las salidas, ya que las entradas se persisten de forma transitiva
			//Borramos las entradas y salidas origen
			getHibernateTemplate().delete(data.getMoviOrigen());
			getHibernateTemplate().delete(data.getDestino().getMoviOrigen());
			TransformacionDet res= save(data);			
			return res;
		}
		return null;
	}
	
	/**
	 * Actualiza el costo de las transformaciones
	 * 
	 * @param year
	 * @param mes
	 * @param clave
	 */
	public void actualizarCostos(int year, int mes,String clave){
		actualizarCostos(Periodo.getPeriodoEnUnMes(mes-1, year), clave);
	}
	
	/**
	 * Actualiza el costo de las transformaciones
	 * 
	 * @param periodo
	 * @param clave
	 */
	//@Transactional(propagation=Propagation.REQUIRED)
	public void actualizarCostos(final Periodo periodo,final String clave){		
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from TransformacionDet d where d.fecha between ? and ? and d.clave=? "; 
				ScrollableResults rs=session.createQuery(hql)
				.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, periodo.getFechaInicial(),Hibernate.DATE)
				.setString(2, clave)
				.scroll();
				while(rs.next()){
					TransformacionDet tr=(TransformacionDet)rs.get()[0];
					tr.actualizarCostoOrigen();
				}
				return null;
			}			
		});
	}
	
	/**
	 * Actualiza el costo de las transformaciones
	 * 
	 * @param year
	 * @param mes
	 */
	public void actualizarCostos(int year, int mes){
		final Periodo periodo=Periodo.getPeriodoEnUnMes(mes-1, year);
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from TransformacionDet d where d.fecha between ? and ? "; 
				ScrollableResults rs=session.createQuery(hql)
				.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, periodo.getFechaInicial(),Hibernate.DATE)
				.scroll();
				while(rs.next()){
					TransformacionDet tr=(TransformacionDet)rs.get()[0];
					tr.actualizarCostoOrigen();
				}
				return null;
			}			
		});
	}
	
		
	
	/**
	 * Importa transformacioens pendientes desde SX_INVENTARIO_MOV
	 * 
	 * Nota: Por compatibilidad con SIIPAP DBF
	 * 
	 * @param periodo
	 */
	//@Transactional(propagation=Propagation.NEVER)
	public void importarPendientes(final Periodo periodo){
		logger.info("Importando transformaciones para el periodo: "+periodo);
		String hql="from MovimientoDet d where d.fecha between ? and ? " +
		" and d.concepto in(\'TRS\',\'REC\',\'REF\',\'RAU\') " +
		" order by d.sucursal.nombre,d.documento,d.renglon";
		List<MovimientoDet> movimientos=getHibernateTemplate().find(hql, new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
		
		final EventList<MovimientoDet> source=GlazedLists.eventList(movimientos);
		logger.info("Movimientos a procesar: "+source.size());
		Comparator<MovimientoDet> comparator=GlazedLists.beanPropertyComparator(MovimientoDet.class, "sucursal.id", "documento","concepto");
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
	private List<TransformacionDet> procesarDocumento(List<MovimientoDet> list){
		logger.info("Documento:"+list.get(0).getDocumento()+" Sucursal:"+list.get(0).getSucursal().getNombre()+ "Concepto: "+list.get(0).getConcepto());		
		List<TransformacionDet> transformaciones=convertir(list);
		for(TransformacionDet det:transformaciones){
			String cos=det.getCantidad()>0?" Costo: "+det.getCosto():" Costo P:"+det.getCostoPromedio();
			logger.info("\t  Producto: "+det.getClave()
					+"  Cantidad: "+det.getCantidad()+cos+ "  Renglon: "+det.getRenglon());
			try {
				persistirImportacion(det);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return transformaciones;
	}
	
	/**
	 * Convierte una lista de {@link MovimientoDet} a {@link TransformacionDet}
	 * @param movs
	 * @return
	 */
	private   List<TransformacionDet> convertir(final List<MovimientoDet> movs){
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


	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	
	
	public static void main(String[] args) {
		//Periodo p=Periodo.getPeriodoEnUnMes(6, 2009);
		DBUtils.whereWeAre();
		Periodo p=new Periodo("01/08/2009","12/08/2009");
		ServiceLocator2.getTransformacionesManager().importarPendientes(p);
		
	}

}
