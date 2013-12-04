package com.luxsoft.siipap.pos.ui.consultas;

import java.util.Date;

import javax.swing.JOptionPane;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
import com.luxsoft.siipap.pos.ui.selectores.BuscadorSelectivoVentas;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;

public class RefacturadorController {
	
	
	public static Pedido refacturar(){
		BuscadorSelectivoVentas form=new BuscadorSelectivoVentas();
		form.setTitle("Seleccionar factura");
		form.setSucursales(Services.getInstance().getConfiguracion().getSucursal());
		form.open();
		if(!form.hasBeenCanceled()){
			//Generar una copia del Pedido
			Sucursal suc=form.getSucursal();
			Long docto=form.getDocumento();
			OrigenDeOperacion origen=form.getOrigen();
			Venta v=Services.getInstance().getFacturasManager().getVentaDao()
				.buscarVenta(suc.getId(), docto, origen);			
			if(v==null){
				JOptionPane.showMessageDialog(form, "Factura inexistente");
				form.open();
			}else{				
				return refacturar(v.getId());
			}
		}
		return null;
	}
	
	public static Pedido refacturar(final String facturaId){
		
		final Venta factura=Services.getInstance().getFacturasManager().buscarVentaInicializada(facturaId);
		Venta target=(Venta)Bean.proxy(Venta.class);
		Bean.normalizar(factura, target, new String[]{"partidas"});
		//target.registrarId(v.getId());
		target.getPartidas().addAll(factura.getPartidas());
		FacturaForm form=new FacturaForm();
		//form.saldo.setText(NumberFormat.getCurrencyInstance().format(v.getSaldoCalculado().doubleValue()));
		form.setFactura(target);
		form.cargarAplicaciones(Services.getInstance().getFacturasManager()
				.buscarAplicaciones(factura.getId()));
		form.setFacturaId(factura.getId());
		form.open();
		if(!form.hasBeenCanceled()){
			User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
			Date time=Services.getInstance().obtenerFechaDelSistema();
			int res=JOptionPane.showConfirmDialog(form, "Generar pedido nuevo?","Generación de nuevo pedido",JOptionPane.OK_CANCEL_OPTION);
			if(res==JOptionPane.OK_OPTION){
				Pedido pedido=Services.getInstance().getPedidosManager()
				.generarCopia(factura.getPedido().getId(),time,user.getUsername());
				return pedido;
			}
					}
		return null;
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
				refacturar();
				System.exit(0);
			}

		});
	}
	

}
