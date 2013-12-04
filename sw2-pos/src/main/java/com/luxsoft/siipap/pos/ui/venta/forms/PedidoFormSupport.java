package com.luxsoft.siipap.pos.ui.venta.forms;

import java.math.BigDecimal;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;

/**
 * Clase de soporte para controlar algunos aspectos del manejo de pedidos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidoFormSupport {
	
	public static void actualizarCortes(final Pedido pedido,final EventList<PedidoDet> partidasUIList){
		
		//Verificar si se requiere partida de cortes
		BigDecimal impCortes=BigDecimal.valueOf(0);		
		for(PedidoDet det:pedido.getPartidas()){
			impCortes=impCortes.add(det.getImporteCorte());			
		}
		//Registramos si existe el corte existente 
		PedidoDet cortes=(PedidoDet)CollectionUtils.find(pedido.getPartidas(), new Predicate(){
			public boolean evaluate(Object object) {
				PedidoDet row=(PedidoDet)object;
				return row.getProducto().getClave().equals("CORTE");
			}
		});
		
		if(impCortes.abs().doubleValue()>0){			
			System.out.println("Corte requerido.....");
			if(cortes==null){
				
				cortes=PedidoDet.getPedidoDet(22);
				cortes.setProducto(Services.getInstance().getProductosManager().buscarPorClave("CORTE"));
				cortes.setCantidad(1.0d);
				cortes.setPrecio(impCortes);
				cortes.setDescuento(0);
				cortes.actualizar();
				cortes.actualizarImporteBruto();
				boolean res=pedido.agregarPartida(cortes);
				if(res && partidasUIList!=null)
					partidasUIList.add(cortes);
			}else{
				cortes.setPrecio(impCortes);
				cortes.setDescuento(0);
				cortes.actualizar();
				cortes.actualizarImporteBruto();
			}
		}else{			
			System.out.println("Corte NO requerido.....");
			if(cortes!=null){
				boolean ok=pedido.getPartidas().remove(cortes);
				if(ok){
					System.out.println("Corte NO requerido existente Por eliminar ");
					partidasUIList.remove(cortes);
				}else{
					System.out.println("PELIGRO NO SE ELIMINO EL CORTE....");
				}
			}
		}
		
		
	}
	
	public static void actualizarManiobras(final Pedido pedido,final EventList<PedidoDet> partidasUIList){
		
		//Verificar si se requiere partida de cortes
		BigDecimal importeManiobras=BigDecimal.ZERO;
		for(PedidoDet det:pedido.getPartidas()){
			if(det.getClave().equals("MANIOBRA"))
				continue;
			if(det.getProducto().getModoDeVenta().equals("N") ){
				importeManiobras=importeManiobras.add(det.getImporteBruto());
			}
		}
		
		double comision=0;
		FormaDePago fp=pedido.getFormaDePago();
		switch (fp) {
		case CHEQUE_POSTFECHADO:
			comision=.04;
			break;
		case TARJETA_CREDITO:
			comision=.02;
			break;
		case TARJETA_DEBITO:
			comision=.01;
			break;
		default:
			break;
		}
		
		importeManiobras=importeManiobras.multiply(BigDecimal.valueOf(comision));
		importeManiobras=importeManiobras.add(pedido.getFlete());	
		//Registramos si existe el corte existente 
		PedidoDet maniobra=(PedidoDet)CollectionUtils.find(pedido.getPartidas(), new Predicate(){
			public boolean evaluate(Object object) {
				PedidoDet row=(PedidoDet)object;
				return row.getProducto().getClave().equals("MANIOBRA");
			}
		});
		
		if(importeManiobras.abs().doubleValue()>0){			
			System.out.println("Maniobra requerida.....");
			if(maniobra==null){
				//SystemUtils.sleep(1000);
				maniobra=PedidoDet.getPedidoDet(23);
				
				
				maniobra.setProducto(Services.getInstance().getProductosManager().buscarPorClave("MANIOBRA"));
				maniobra.setCantidad(1.0d);
				maniobra.setPrecio(importeManiobras);
				maniobra.setDescuento(0);
				maniobra.actualizar();
				maniobra.actualizarImporteBruto();
				boolean res=pedido.agregarPartida(maniobra);
				if(res && partidasUIList!=null)
					partidasUIList.add(maniobra);
			}else{
				maniobra.setPrecio(importeManiobras);
				maniobra.setDescuento(0);
				maniobra.actualizar();
				maniobra.actualizarImporteBruto();
			}
			
		}else{
			//System.out.println("Maniobra  NO requerida.....");
			if(maniobra!=null){
				boolean ok=pedido.getPartidas().remove(maniobra);
				if(ok && (partidasUIList!=null)){
					partidasUIList.remove(maniobra);
				}else
					System.out.println("PELIGRO MANIOBRA NO REQUERIDA NO SE ELIMINO DEL BEAN...");
			}
		}
		
		pedido.setComisionTarjetaImporte(importeManiobras.subtract(pedido.getFlete()));
		pedido.setComisionTarjeta(comision);
	}

}
