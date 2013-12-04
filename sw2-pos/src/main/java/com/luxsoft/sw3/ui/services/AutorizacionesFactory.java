package com.luxsoft.sw3.ui.services;

import java.util.Date;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.cxc.model.AutorizacionDeAbono;
import com.luxsoft.siipap.cxc.model.AutorizacionParaCargo;
import com.luxsoft.siipap.inventarios.model.AutorizacionDeMovimiento;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.AutorizacionParaFacturarSinExistencia;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.AutorizacionDePedido;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Fabrica para autorizaciones
 * 
 * @author Ruben Cancino Ramos
 * 
 */
public class AutorizacionesFactory {
	
	private static Logger logger=Logger.getLogger(AutorizacionesFactory.class);

	/**
	 * Genera una autorizacion para la cancelacion de abonos
	 * 
	 * @param abono
	 * @param fecha
	 * @return
	 */
	public static AutorizacionDeAbono getCancelacionDeDeposito() {
		
		String rol="CXC_AUTORIZA_N1";
		if(KernellSecurity.instance().hasRole(rol)){
			AutorizacionDeAbono aut = new AutorizacionDeAbono();
			aut.setAutorizo(KernellSecurity.instance().getCurrentUserName());
			aut.setFechaAutorizacion(Services.getInstance().obtenerFechaDelSistema());
			aut.setComentario("CANCELACION DE ABONO: Rol: "+rol);
			aut.setIpAdress(KernellSecurity.getIPAdress());
			aut.setMacAdress(KernellSecurity.getMacAdress());
			
			return aut;
		}else{
			return null;
		}
		
	}
	
	public static AutorizacionDeAbono getAutorizacionParaDeposito(){
		//User user=KernellUtils.buscarUsuario();
		User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if(user!=null&& user.hasRole(POSRoles.AUTORIZADOR_DE_DEPOSITOS.name()) ){
			AutorizacionDeAbono aut=new AutorizacionDeAbono();
			aut.setComentario("AUTORIZACION DE DEPOSITO");
			aut.setAutorizo(user.getLastName());
			aut.setFechaAutorizacion(Services.getInstance().obtenerFechaDelSistema());
			aut.setIpAdress(KernellSecurity.getIPAdress());
			aut.setMacAdress(KernellSecurity.getMacAdress());
			return aut;
		}else{
			MessageUtils.showMessage("No tiene los derechos necesarios \n Role requerido: "
					+POSRoles.AUTORIZADOR_DE_DEPOSITOS.name(), "Autorización de depositos");
			return null;
		}	
		
	}
	
	public static AutorizacionParaFacturarSinExistencia getAutorizacionParaFacturacionSinExistencias(){
		User user=KernellUtils.buscarUsuario();
		if(user!=null&& user.hasRole(POSRoles.FACTURACION_ESPECIAL.name()) ){			
			AutorizacionParaFacturarSinExistencia aut=new AutorizacionParaFacturarSinExistencia();			
			aut.setAutorizo(user.getUsername());
			aut.setFechaAutorizacion(Services.getInstance().obtenerFechaDelSistema());
			aut.setIpAdress(KernellSecurity.getIPAdress());
			aut.setMacAdress(KernellSecurity.getMacAdress());
			return aut;
		}else{
			MessageUtils.showMessage("No tiene los derechos necesarios para facturar sin existencia\n Role requerido: "
					+POSRoles.FACTURACION_ESPECIAL.name(), "Facturación especial");
			return null;
		}		
	}
	
	public static AutorizacionDeMovimiento getAutorizacionParaCancelarMovimiento(){
		User user=KernellUtils.buscarUsuario();
		if(user!=null&& user.hasRole(POSRoles.FACTURACION_ESPECIAL.name()) ){			
			AutorizacionDeMovimiento aut=new AutorizacionDeMovimiento();			
			aut.setAutorizo(user.getUsername());
			aut.setFechaAutorizacion(Services.getInstance().obtenerFechaDelSistema());
			aut.setIpAdress(KernellSecurity.getIPAdress());			
			aut.setMacAdress(KernellSecurity.getMacAdress());
			aut.setComentario("CANCELACION DE MOVIMIENTO DE INVENTARIO");
			return aut;
		}else{
			MessageUtils.showMessage("Derechos insuficientes\n Role requerido: "+POSRoles.ADMINISTRACION_INVENTARIOS.name(), "Autorizaciones especiales");
			return null;
		}
		
	}
	
	
	public static AutorizacionParaCargo getAutorizacionParaCancelarFactura(final Date fecha){
		User user=KernellUtils.buscarUsuario();
		
		if(user!=null&& user.hasRole(POSRoles.GERENTE_DE_VENTAS.name()) ){			
			AutorizacionParaCargo aut=new AutorizacionParaCargo();
			aut.setAutorizo(KernellSecurity.instance().getCurrentUserName());
			aut.setComentario("CANCELACION DE FACTURA");
			aut.setFechaAutorizacion(fecha);
			aut.setIpAdress(KernellSecurity.getIPAdress());
			aut.setMacAdress(KernellSecurity.getMacAdress());
			return aut;
		}else{
			MessageUtils.showMessage("Derechos insuficientes\n Role requerido: "+POSRoles.GERENTE_DE_VENTAS.name()
					, "Autorizaciones especiales");
			return null;
		}
	}
	
	/**
	 * Autorizacion especial para pago contra entrega
	 * 
	 * @param p
	 * @return
	 */
	public static AutorizacionDePedido getAutorizacionParaPagoContraEntrega(final Pedido p){
		
		if(p.getCliente().isContraEntrega()){
			AutorizacionDePedido aut=new AutorizacionDePedido();			
			aut.setAutorizo(KernellSecurity.instance().getCurrentUserName());
			aut.setFechaAutorizacion(Services.getInstance().obtenerFechaDelSistema());
			aut.setIpAdress(KernellSecurity.getIPAdress());			
			aut.setMacAdress(KernellSecurity.getMacAdress());
			aut.setComentario(AutorizacionDePedido.Conceptos.PAGO_CONTRA_ENTREGA.name());
			return aut;
		}
		else {
			User user=KernellUtils.buscarUsuario();
			logger.info("Evaluando usuario: "+user);
			if(user!=null&& user.hasRole(POSRoles.VENDEDOR.name()) ){
				AutorizacionDePedido aut=new AutorizacionDePedido();			
				aut.setAutorizo(user.getUsername());
				aut.setFechaAutorizacion(Services.getInstance().obtenerFechaDelSistema());
				aut.setIpAdress(KernellSecurity.getIPAdress());			
				aut.setMacAdress(KernellSecurity.getMacAdress());				
				aut.setComentario(AutorizacionDePedido.Conceptos.PAGO_CONTRA_ENTREGA.name());
				return aut;
			}else{
				MessageUtils.showMessage("Derechos insuficientes para " +
						" autorizar pago contra entrega a usuario: "+user +
						" \n Role requerido: "+POSRoles.VENDEDOR.name(), "Autorizaciones especiales");
				return null;
			}			
		}
	}
	
	public static AutorizacionDePedido getAutorizacionDePedido(){
		User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate(),"Autorizacion para Precio/Desceunto especial ");
		//User user=KernellUtils.buscarUsuario();
		if(user!=null&& user.hasRole(POSRoles.ADMINISTRADOR_DE_VENTAS.name()) ){
			
			AutorizacionDePedido aut=new AutorizacionDePedido();
			aut.setComentario("DESCUENTO/PRECIO ESPECIAL ");
			aut.setIpAdress(KernellSecurity.getIPAdress());
			aut.setMacAdress(KernellSecurity.getMacAdress());
			aut.setFechaAutorizacion(Services.getInstance().obtenerFechaDelSistema());
			aut.setIpAdress(KernellSecurity.getIPAdress());			
			aut.setMacAdress(KernellSecurity.getMacAdress());			
			aut.setAutorizo(user.getUsername());			
			return aut;
		}else{
			MessageUtils.showMessage("Derechos insuficientes\n Role requerido: "+POSRoles.ADMINISTRADOR_DE_VENTAS.name(), "Autorizaciones especiales");
			return null;
		}
		
	}

}
