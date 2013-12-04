package com.luxsoft.siipap.pos.ui.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.CFDPrintServices;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.reports.ReportsManager;
import com.luxsoft.sw3.reports.ReportsManagerFactory;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Facade para la impresion de  los reportes mas usados en POS
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReportUtils2 {
	
	private static ReportsManager rm=ReportsManagerFactory.getInstance();
	
	private static Logger logger=Logger.getLogger(ReportUtils2.class);
	
	public static void runReport(String path,Map params){
		rm.runReport(path, params);
	}
	
	
	/**
	 * Imprime un pedido. Si es la primera vez que se imprime el pedido se salva la fecha
	 * de impresion
	 * 
	 * @param pedido
	 */
	public static void imprimirPedido(final Pedido pedido){
		String total=ImporteALetra.aLetra(pedido.getTotalMN());
		final Map parameters=new HashMap();
		parameters.put("PEDIDO", String.valueOf(pedido.getId()));
		parameters.put("IMP_CON_LETRA", total);
		rm.runReport("ventas/Pedido.jasper", parameters);
		/*if(pedido.getImpreso()==null){			
			Services.getInstance().getPedidosManager()
			.actualizarFechaDeImpresion(pedido, fechaActual());
		}*/
	}
	
	public static void imprimirFactura(final String id ){
		Venta factura=Services.getInstance().getFacturasManager().getFactura(id);
		imprimirFactura(factura);
	}
	
	public static void imprimirFactura(final Venta factura){
		final Map parameters=new HashMap();
		String total=ImporteALetra.aLetra(factura.getTotalCM());
		parameters.put("CARGO_ID", String.valueOf(factura.getId()));
		parameters.put("IMP_CON_LETRA", total);
		
		//Definimos el fomrato de impresion
		
		if(factura.getCliente().getClave().equalsIgnoreCase("1")) 			
			rm.printReport("ventas/FacturaMostrador.jasper", parameters,false); //Factura a cliente MOSTRADOR
		else if(factura.getOrigen().equals(OrigenDeOperacion.CRE)) 
			rm.printReport("ventas/FacturaNew.jasper", parameters,false);  //Factura CRE
		else  
			rm.printReport("ventas/Factura.jasper", parameters,false);  //Factura CAM y MOS
		
		final Date impresion=fechaActual();
		if(factura.getImpreso()==null){
			logger.info("Actualizando fecha de impresion para factura: "+factura.getId());
			
			try {
				Venta target=Services.getInstance().getFacturasManager().getFactura(factura.getId());
				target.setImpreso(impresion);
				Services.getInstance().getUniversalDao().save(target);
			} catch (Exception ex) {
				logger.error(ExceptionUtils.getRootCauseMessage(ex),ex);
			}
			
		}
	}
	
	public static void imprimirFactura_bak(final Venta factura){
		final Map parameters=new HashMap();
		String total=ImporteALetra.aLetra(factura.getTotalCM());
		parameters.put("CARGO_ID", String.valueOf(factura.getId()));
		parameters.put("IMP_CON_LETRA", total);
		if(factura.getCliente().getClave().equalsIgnoreCase("1"))
			//rm.runReport("ventas/FacturaMostrador.jasper", parameters);
			rm.printReport("ventas/FacturaMostrador.jasper", parameters,false);
		else
			rm.printReport("ventas/Factura.jasper", parameters,false);
		final Date impresion=fechaActual();
		if(factura.getImpreso()==null){
			logger.info("Actualizando fecha de impresion para factura: "+factura.getId());
			
			try {
				Venta target=Services.getInstance().getFacturasManager().getFactura(factura.getId());
				target.setImpreso(impresion);
				Services.getInstance().getUniversalDao().save(target);
				//Services.getInstance().getHibernateTemplate().save(factura);
			} catch (Exception ex) {
				logger.error(ExceptionUtils.getRootCauseMessage(ex),ex);
			}
			
		}
	}
	
	public static void imprimirFacturaCopia(final String id ){
		//Venta factura=Services.getInstance().getFacturasManager().getFactura(id);
		Venta factura=Services.getInstance().getFacturasManager().buscarVentaInicializada(id);
		ComprobanteFiscal cf=Services.getInstance().getComprobantesDigitalManager().cargarComprobante(factura);
		if(cf==null)
			imprimirFacturaCopia(factura);
		else
			CFDPrintServices.impripirComprobante(factura, cf,null, true);
	}
	
	public static void imprimirFacturaCopia(final Venta factura){
		final Map parameters=new HashMap();
		String total=ImporteALetra.aLetra(factura.getTotalCM());
		parameters.put("CARGO_ID", String.valueOf(factura.getId()));
		parameters.put("IMP_CON_LETRA", total);
		rm.runReport("ventas/FacturaCopia.jasper", parameters);
	}
	
	/**
	 * Imprime una orden de compra
	 * @param compra
	 */
	public static void imprimirCompra(Compra2 compra){
		
		final Map map=new HashMap();
		map.put("COMPRA_ID", compra.getId());
		map.put("CLAVEPROV", "NO");
		if(MessageUtils.showConfirmationMessage("Con claves del proveedor", "Impresión de Orden de Compra")){
			map.put("CLAVEPROV", "SI");
		}
		rm.runReport("compras/OrdenDeCompraSuc.jasper", map);
	}
	
	
	
	/**
	 * Regresa la fecha actual de la base de datos
	 * 
	 * @return
	 */
	private static Date fechaActual(){
		return Services.getInstance().obtenerFechaDelSistema();
	}

	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				//final Pedido pedido=new Pedido();
				//imprimirPedido(pedido);
				//System.exit(0);
				//imprimirFacturaCopia("8a8a81c7-256ff552-0125-6ff5d87e-0001");
				
			}

		});
	}

}
