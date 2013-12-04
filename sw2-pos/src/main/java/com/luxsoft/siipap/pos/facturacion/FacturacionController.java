package com.luxsoft.siipap.pos.facturacion;

import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.ui.consultas.caja.CajaController;
import com.luxsoft.siipap.pos.ui.venta.forms.CheckplusVentaForm;
import com.luxsoft.siipap.pos.ui.venta.forms.FacturacionDeCreditoForm;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.CFDPrintServices;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.services.FacturasManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.CheckPlusVenta;
import com.luxsoft.sw3.ventas.EstadoDeVenta;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * UI Controller para adminsitrar el proceso de facturacion 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Service("facturacionController")
public class FacturacionController {
	
	@Autowired
	private CajaController cajaController;
	
	public void facturarPedidoSinExistencia(final Pedido pedido){
		throw new UnsupportedOperationException("Por implementar");
	}
	
	public void facturarPedidoConAnticipo( Pedido pedido){
		Assert.isTrue(!pedido.isFacturado(),"Este pedido ya ha sido facturado");
		
		List<Venta> facturas;
		facturas=facturarPedidoDeCredito(pedido);
		if(facturas!=null){
			for(Venta fac:facturas){
				MessageUtils.showMessage("Factura generada: "+fac.getDocumento(), "Facturación");
				generarCompbobanteEImprimir(fac);
				
			}
		}
	}
	
	public void facturarPedido( Pedido pedido){	
		
		Assert.isTrue(!pedido.isFacturado(),"Este pedido ya ha sido facturado");
		List<Venta> facturas;
		if(pedido.isDeCredito())
			facturas=facturarPedidoDeCredito(pedido);
		else
			facturas=facturarPedidoDeContado(pedido);
		if(facturas!=null){
			for(Venta fac:facturas){
				MessageUtils.showMessage("Factura generada: "+fac.getDocumento(), "Facturación");
				generarCompbobanteEImprimir(fac);
				generarRegsitroRastreo(fac);
				//fac=getManager().buscarVentaInicializada(fac.getId());
//				ReportUtils2.imprimirFactura(fac);
				
			}
		}
	}
	
	public void generarRegsitroRastreo(Venta fac){
		try {
			EstadoDeVenta e=new EstadoDeVenta(fac);
			Services.getInstance().getHibernateTemplate().merge(e);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("No fue posible generar registro de rastreo para venta: "+fac.getId()+  "Err: "+ExceptionUtils.getRootCauseMessage(e));
			
		}
		
		
	}
	
	public void generarCompbobanteEImprimir(Venta venta){
		venta=getManager().buscarVentaInicializada(venta.getId());
		ComprobanteFiscal cf=Services.getInstance().getComprobantesDigitalManager().generarComprobante(venta);
		CFDPrintServices.imprimirFacturaEnMostrador(venta, cf);
		
	}
	
	private List<Venta> facturarPedidoDeCredito(final Pedido pedido){
		User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if(user!=null){
			FacturacionDeCreditoForm form=new FacturacionDeCreditoForm(pedido,null);
			form.setUser(user);
			final EventList<Venta> facturas=new BasicEventList<Venta>(0);
			Date fecha=Services.getInstance().obtenerFechaDelSistema();
			facturas.addAll(getManager().prepararParaFacturar(pedido,fecha));
			form.setFacturas(facturas);
			form.open();
			if(!form.hasBeenCanceled()){
				Date creado=Services.getInstance().obtenerFechaDelSistema();
				String cuenta="";
				if(!pedido.getFormaDePago().equals(FormaDePago.EFECTIVO)){
					cuenta=pedido.getCliente().getCuentaDePago();
					if(StringUtils.isBlank(cuenta)){
						boolean valido=false;
						while(!valido){
							cuenta=JOptionPane.showInputDialog("Ultimos 4 digitos de la cuenta/tarjeta ");
							cuenta=StringUtils.left(cuenta, 4);
							if(StringUtils.isBlank(cuenta))
								valido=true;
							else
								valido=NumberUtils.isDigits(cuenta);
						}
					}
					else{
						if(!pedido.getCliente().getFormaDePago().equals(pedido.getFormaDePago())){
							cuenta="";
						}
						boolean valido=false;
						while(!valido){
							cuenta=JOptionPane.showInputDialog("Ultimos 4 digitos de la cuenta/tarjeta \n"+"Cte. Metodo: "+pedido.getCliente().getFormaDePago()+"  Pedido F.Pago: " + pedido.getFormaDePago(),cuenta );
							cuenta=StringUtils.left(cuenta, 4);
							if(StringUtils.isBlank(cuenta))
								valido=true;
							else
								valido=NumberUtils.isDigits(cuenta);
						}
					}
				}
				if(pedido.getFormaDePago().equals(FormaDePago.CHECKPLUS)){
					CheckPlusVenta checkplus=CheckplusVentaForm.showForm(pedido);
					if(checkplus!=null){
						for(Venta v:facturas){
							v.setComentarioCancelacionDBF(cuenta);
							v.setContraEntrega(pedido.isContraEntrega());
							v.getLog().setCreado(creado);
							v.getLog().setModificado(creado);
							v.getLog().setCreateUser(user.getUsername());
							v.getLog().setUpdateUser(user.getUsername());
							
						}
						return getManager().facturar(facturas,checkplus);
					}
					
				}else{
					for(Venta v:facturas){
						v.setComentarioCancelacionDBF(cuenta);
						v.setContraEntrega(pedido.isContraEntrega());
						v.getLog().setCreado(creado);
						v.getLog().setModificado(creado);
						v.getLog().setCreateUser(user.getUsername());
						v.getLog().setUpdateUser(user.getUsername());
						
					}
					if(form.getAnticipo()!=null){
						return getManager().facturar(facturas,form.getAnticipo().getAnticipo(),form.getAnticipo().getImporte());
					}
					
					return getManager().facturar(facturas);
				}
				
				
			}
		}
		return null;
	}
	
	private List<Venta> facturarPedidoDeContado(final Pedido pedido){
		Date fecha=Services.getInstance().obtenerFechaDelSistema();
		List<Venta> facturas=getManager().prepararParaFacturar(pedido,fecha);
		Date fechaSistema=fecha;
		for(Venta v:facturas){
			v.setFecha(fechaSistema);
			
			if(pedido.getAutorizacionSinExistencia()!=null)
				v.setAutorizacionSinExistencia(pedido.getAutorizacionSinExistencia());
		}
		FacturacionModel facturacionModel=FacturacionModel.getModel(pedido);
		facturacionModel.agregarFacturas(facturas);
		final DefaultFormModel model=new DefaultFormModel(facturacionModel);
		
		final FacturacionDePedidoForm form=new FacturacionDePedidoForm(model);
		form.setCajaController(getCajaController());
		form.open();
		
		
		if(!form.hasBeenCanceled()){
			
			String cuenta="";
			
			if(!(pedido.getFormaDePago().equals(FormaDePago.EFECTIVO) || pedido.getCliente().getRfc().equals("XAXX010101000")) ){
				cuenta=pedido.getCliente().getCuentaDePago();
				if(StringUtils.isBlank(cuenta)){
					boolean valido=false;
					while(!valido){
						cuenta=JOptionPane.showInputDialog("Ultimos 4 digitos de la cuenta/tarjeta ");
						cuenta=StringUtils.left(cuenta, 4);
						if(StringUtils.isBlank(cuenta))
							valido=true;
						else
							valido=NumberUtils.isDigits(cuenta);
					}
				}
				else{
					if(!pedido.getCliente().getFormaDePago().equals(pedido.getFormaDePago())){
						cuenta="";
					}
					boolean valido=false;
					while(!valido){
						cuenta=JOptionPane.showInputDialog("Ultimos 4 digitos de la cuenta/tarjeta \n"+"Cte. Metodo: "+pedido.getCliente().getFormaDePago()+"  Pedido F.Pago: " + pedido.getFormaDePago(),cuenta );
						cuenta=StringUtils.left(cuenta, 4);
						if(StringUtils.isBlank(cuenta))
							valido=true;
						else
							valido=NumberUtils.isDigits(cuenta);
					}
				}
			}
			
			if (StringUtils.isBlank(cuenta))
				cuenta="";
			Date creado=Services.getInstance().obtenerFechaDelSistema();
			for(Venta v:facturacionModel.getFacturas()){
				registrarLog(v, pedido, creado);
				v.setComentarioCancelacionDBF(cuenta);
			}
			List<Venta> res= getManager().facturarYAplicar(facturacionModel.getPagos(), facturacionModel.getFacturas());
			getManager().generarAbonoAutmatico(res);
			return res;
		}
		return null;
	}
	
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
