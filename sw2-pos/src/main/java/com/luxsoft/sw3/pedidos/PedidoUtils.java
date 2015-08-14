package com.luxsoft.sw3.pedidos;

import java.sql.Types;
import java.util.List;

import org.apache.log4j.Logger;

import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ProductoRow;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoRow;
import com.luxsoft.utils.LoggerHelper;

public class PedidoUtils {
	
	static Logger logger=LoggerHelper.getLogger();
	
	public static List<PedidoRow> find(Periodo periodo){
		String sql="SELECT X.PEDIDO_ID AS ID,X.PEDIDO_ID,X.CLAVE,X.NOMBRE,X.FECHA,X.FOLIO,X.FACTURAR AS FACTURABLE,X.TOTAL,X.PUESTO,X.CREADO_USR AS CREADO ,X.MODIFICADO_USR AS MODIFICADO" +
				",X.FPAGO AS FormaDePago ,x.FENTREGA as entrega" +
				",X.COMENTARIO,X.COMENTARIO2,X.TIPO,X.PUESTO" +
				",Y.AUT_ID AS contraEntregaId " +
				",Z.ID as pendienteId " +
				",Z.COMENTARIO  AS PENDIENTE" +
				",S.AUT_COMMENTARIO AS comentarioAutorizacion, X.VALE, X.CLASIFICACION_VALE AS clasificacionVale, U.NOMBRE AS sucursalVale" +
				" FROM SX_PEDIDOS X " +
				" LEFT JOIN SX_PEDIDOS_PAGOCE Y ON(X.PEDIDO_ID=Y.PEDIDO_ID)" +
				" LEFT JOIN SX_PEDIDOS_PENDIENTES Z ON(X.PEDIDO_ID=Z.PEDIDO_ID)" +
				" LEFT JOIN SX_AUTORIZACIONES2 S ON(X.AUTORIZACION_ID=S.AUT_ID)" +
				" LEFT JOIN SW_SUCURSALES U ON (U.SUCURSAL_ID=X.SUCURSAL_VALE)" +
				" WHERE X.FECHA BETWEEN ? AND ? AND X.ESPECIAL=false";
		//System.out.println(sql);
		Object[] params=new Object[]{
			new SqlParameterValue(Types.DATE, periodo.getFechaInicial())
			,new SqlParameterValue(Types.DATE, periodo.getFechaFinal())
		};
		return Services.getInstance().getJdbcTemplate().query(sql, params, new BeanPropertyRowMapper(PedidoRow.class));
	}
	
	
	public static List<PedidoRow> findPendientesPorFacturar(final Periodo periodo){
		String sql="SELECT X.PEDIDO_ID AS ID,X.PEDIDO_ID,X.CLAVE,X.NOMBRE,X.FECHA,X.FOLIO,X.FACTURAR AS FACTURABLE,X.TOTAL,X.PUESTO,X.CREADO_USR AS CREADO ,X.MODIFICADO_USR AS MODIFICADO" +
				",X.FPAGO AS FormaDePago ,x.FENTREGA as entrega" +
				",X.COMENTARIO,X.COMENTARIO2,X.TIPO,X.PUESTO" +
				",Y.AUT_ID AS contraEntregaId " +
				",Z.ID as pendienteId " +
				",Z.COMENTARIO  AS PENDIENTE" +
				",S.AUT_COMMENTARIO AS comentarioAutorizacion" +
				" ,X.MODIFICADO_USR AS operador " +
				",X.MONEDA as moneda"+
				" FROM SX_PEDIDOS X " +
				" LEFT JOIN SX_PEDIDOS_PAGOCE Y ON(X.PEDIDO_ID=Y.PEDIDO_ID)" +
				" LEFT JOIN SX_PEDIDOS_PENDIENTES Z ON(X.PEDIDO_ID=Z.PEDIDO_ID)" +
				" LEFT JOIN SX_AUTORIZACIONES2 S ON(X.AUTORIZACION_ID=S.AUT_ID)" +
				" WHERE X.FECHA BETWEEN ? AND ? AND X.FACTURAR=true and " +
				" (select ifnull(sum(V.total),0)+ifnull(sum(V.ANTICIPO_APLICADO),0) from sx_ventas  V where V.PEDIDO_ID=X.PEDIDO_ID)=0 " +
				" AND x.tipo='CONTADO'"
			;
		//System.out.println(sql);
		Object[] params=new Object[]{
			new SqlParameterValue(Types.DATE, periodo.getFechaInicial())
			,new SqlParameterValue(Types.DATE, periodo.getFechaFinal())
		};
		return Services.getInstance().getJdbcTemplate().query(sql, params, new BeanPropertyRowMapper(PedidoRow.class));
	}
	
	private static EventList<ProductoRow> productos;
	
	public static void initProductos(){
		
		
		
		Runnable task=new Runnable() {
			public void run() {
				logger.info("Cargando productos para formas");
				getProductos().getReadWriteLock().writeLock().lock();
				getProductos().clear();
				getProductos().addAll(Services.getInstance().getProductosManager().getActivosAsRows());
				getProductos().getReadWriteLock().writeLock().unlock();
			}
		};
		TaskExecutor executor=(TaskExecutor)Services.getInstance().getContext().getBean("taskExecutor");
		executor.execute(task);
		
	}
	
	public static EventList<ProductoRow> getProductos(){
		if(productos==null){
			productos=GlazedLists.eventList(new BasicEventList<ProductoRow>(0));
		}
		return productos;
	}
	
	
	
	/**
	 * Valida las condiciones primarias para poder mandar facturar
	 * 
	 * @param pedido
	 * @return
	 */
	public static boolean validarParaFacturacion(final Pedido pedido){
		if(pedido.isFacturable()){
			MessageUtils.showMessage("Pedido ya es facturable", "Pedidos");
			return false;
		}
		// A - Cheques devueltos y / o Jurídico
		Cliente c=pedido.getCliente();
		if(c.isSuspendido()){
			if(c.isJuridico()){
				MessageUtils.showMessage("El cliente: "+c.getNombre()
						+ " se encuentra en trámite jurídico por lo que no se le puede facturar.\n Pedir autorización al departamento de crédito"
						, "Autorizaciones");
				return false;
			}
			if(c.getChequesDevueltos().doubleValue()>0){
				MessageUtils.showMessage("El cliente: "+c.getNombre()
						+"\n Tiene cheque(s) devueltos por un monto de: "+c.getChequesDevueltos()+" NO SE LE PUEDE FACTURAR." 
						+"\n Pedir autorización al departamento de crédito" 
						, "Autorizaciones");
				return false;
			}
			MessageUtils.showMessage("El cliente: "+c.getNombre()
					+"\n Esta suspendido NO SE LE PUEDE FACTURAR." 
					+"\n Pedir autorización al departamento de crédito" 
					, "Autorizaciones");
			return false;
		}else
			return true;
		
	}
	
	
	
	public static void main(String[] args) {
		//PedidoUtils.initProductos();
		System.out.println(find(Periodo.getPeriodoDelMesActual()));
	}

}
