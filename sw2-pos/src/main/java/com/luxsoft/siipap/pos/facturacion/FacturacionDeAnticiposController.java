package com.luxsoft.siipap.pos.facturacion;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.consultas.caja.CajaController;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeAnticiposFacturadosPendientes;
import com.luxsoft.siipap.security.AutorizacionesController;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.CFDPrintServices;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.services.FacturasManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.AutorizacionDePedido;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.Pedido.FormaDeEntrega;
import com.luxsoft.sw3.ventas.PedidoDet;

/**
 * Controlador para la facturacion de anticipos 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Service("facturacionDeAnticiposController")
public class FacturacionDeAnticiposController {
	
	@Autowired
	private CajaController cajaController;
	
	public void facturarPedidoConAnticipo( Pedido pedido){
		Assert.isTrue(!pedido.isFacturado(),"Este pedido ya ha sido facturado");
		Assert.isTrue(!pedido.isDeCredito(),"El pedido debe ser de contado");
		User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if(!Services.getInstance().getEmpresa().getTipoDeComprobante().equals(Empresa.TipoComprobante.CFDI)
				&& (!pedido.getCliente().getClave().equals("U050008"))){
			MessageUtils.showMessage("Solo se pueden generar comprobantes tipo CFDI", "Comprobantes fiscales");
			return;
		}
		if(user==null)
			return;
		//User user=KernellUtils.buscarUsuario();
		if(!user.hasRole(POSRoles.CONTROLADOR_DE_ANTICIPOS.name())){
			MessageUtils.showMessage("Derechos requeridos ROL:\n"+POSRoles.CONTROLADOR_DE_ANTICIPOS, "Control de anticipos");
			return;
		}
		Venta anticipo=SelectorDeAnticiposFacturadosPendientes.seleccionar(pedido.getClave());
		String msg=MessageFormat.format("Anticipo: {0} - Fac: {1} Fecha:{2,date,short} Disponible: {3}   " +
				"\n Pedido: {4} Total: {5} "
				,anticipo.getSucursal().getNombre()
				,anticipo.getDocumento()
				,anticipo.getFecha()
				,anticipo.getDisponibleDeAnticipo()
				,pedido.getFolio()
				,pedido.getTotal()
				
				);
		if(MessageUtils.showConfirmationMessage(msg,"Aplicación de anticipo")){
			for(PedidoDet det:pedido.getPartidas()){
				double cantidad=det.getCantidad()/det.getFactor();
				CantidadMonetaria neto=CantidadMonetaria.pesos(det.getImporteNeto());
				CantidadMonetaria precio=neto.divide(cantidad);
				det.setPrecio(precio.amount());
				det.setDescuento(0);
				det.actualizar();
			}
			pedido.actualizarImportes();
			
			PedidoDet partidaDeAnticipo=PedidoDet.getPedidoDet();
			partidaDeAnticipo.setProducto(Services.getInstance().getProductosManager().buscarPorClave("ANTICIPO"));
			partidaDeAnticipo.setCantidad(-1);
			BigDecimal disponible=MonedasUtils.calcularImporteDelTotal(anticipo.getDisponibleDeAnticipo());
			BigDecimal requerido=pedido.getSubTotal1();
			if(disponible.doubleValue()>=requerido.doubleValue()){
				partidaDeAnticipo.setPrecio(requerido);
			}else{
				partidaDeAnticipo.setPrecio(disponible);
			}
			partidaDeAnticipo.actualizar();
			pedido.agregarPartida(partidaDeAnticipo);
			pedido.actualizarImportes();
			pedido.setEntrega(FormaDeEntrega.ENVIO);
			//pedido.setMismaDireccion(true);
			
			Date fecha=Services.getInstance().obtenerFechaDelSistema();
			AutorizacionDePedido aut=new AutorizacionDePedido();			
			aut.setAutorizo(user.getUsername());
			aut.setFechaAutorizacion(fecha);
			aut.setIpAdress(KernellSecurity.getIPAdress());			
			aut.setMacAdress(KernellSecurity.getMacAdress());
			aut.setComentario(AutorizacionDePedido.Conceptos.PAGO_CONTRA_ENTREGA.name());
			pedido.setPagoContraEntrega(aut);
			
			
			List<Venta> facturas=getManager().prepararParaFacturar(pedido,fecha);
			Date fechaSistema=fecha;
			for(Venta v:facturas){
				v.setFecha(fechaSistema);
				registrarLog(v, pedido, fecha);
			}
			facturas=getManager().facturar(facturas,anticipo,MonedasUtils.calcularTotal(partidaDeAnticipo.getPrecio()));
			if(facturas!=null){
				for(Venta fac:facturas){					
					//generarCompbobanteEImprimir(fac);
					fac=getManager().buscarVentaInicializada(fac.getId());
					MessageUtils.showMessage("Factura generada: "+fac.getDocumento(), "Facturación");
				}
			}
		}
		
	}
	/*
	public void generarCompbobanteEImprimir(Venta venta){
		venta=getManager().buscarVentaInicializada(venta.getId());
		ComprobanteFiscal cf=Services.getInstance().getComprobantesDigitalManager().generarComprobante(venta);
		//CFDPrintServices.imprimirFacturaEnMostrador(venta, cf);
		//ReportUtils2.imprimirFactura(fac);
	}*/
	
		
	
	
	private void registrarLog(final Venta v,final Pedido pedido,final Date creado){
		try {
			v.setContraEntrega(pedido.isContraEntrega());
			v.getLog().setCreado(creado);
			v.getLog().setModificado(creado);
			v.getLog().setCreateUser(pedido.getLog().getCreateUser());
			v.getLog().setUpdateUser(pedido.getLog().getUpdateUser());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public FacturasManager getManager(){
		return Services.getInstance().getFacturasManager();
	}
	
	public CajaController getCajaController() {
		return cajaController;
	}

	public void setCajaController(CajaController cajaController) {
		this.cajaController = cajaController;
	}

	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				//System.out.println(Services.getInstance().getConfiguracion().getSucursal());
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				Pedido pedido=Services.getInstance().getPedidosManager().get(2L);
				System.out.println("Pedido: "+pedido);
				CajaController cajaController=new CajaController();
				FacturacionController controller=new FacturacionController();
				controller.setCajaController(cajaController);
				controller.facturarPedido(pedido);
				System.exit(0);
			}

		});
	}
*/
}
