package com.luxsoft.sw3.impap.ui.controllers;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.impap.ui.form.PedidoController;
import com.luxsoft.sw3.impap.ui.form.PedidoForm;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.utils.LoggerHelper;

public class FacturacionController {
	
	private static FacturacionController INSTACE;
	
	private Logger logger=LoggerHelper.getLogger();
	
	private FacturacionController(){}
	
	public Venta facturar(){
		PedidoController model=new PedidoController();		
		PedidoForm form=new PedidoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			Pedido pedido=model.persist();
			logger.info("Pedido generado: "+pedido.getFolio());
			facturar(pedido);
		}
		return null;
	}
	
	public List<Venta> facturar(final Pedido pedido){
		Date fecha=ServiceLocator2.obtenerFechaDelSistema();
		List<Venta> ventas=ServiceLocator2.getFacturasManager().prepararParaFacturar(pedido, fecha);
		List<Venta> facturas=ServiceLocator2.getFacturasManager().facturar(ventas);
		return facturas;
	}
	 
	
	public static FacturacionController getInstance(){
		if(INSTACE==null){
			INSTACE=new FacturacionController();
		}
		return INSTACE;
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
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				//getInstance().facturar();
				try {
					Pedido pedido=ServiceLocator2.getPedidosManager().get("8a8a81c7-2c7f53ed-012c-7f54ae74-0001");
					getInstance().facturar(pedido);
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					System.exit(0);
				}
				
				
			}

		});
	}

}
