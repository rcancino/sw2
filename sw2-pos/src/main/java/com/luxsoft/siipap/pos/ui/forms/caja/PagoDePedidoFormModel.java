package com.luxsoft.siipap.pos.ui.forms.caja;

import org.springframework.util.Assert;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.pos.facturacion.FacturacionModel;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;


public class PagoDePedidoFormModel extends PagoFormModel{
	
	private final FacturacionModel facturacionModel;

	public PagoDePedidoFormModel(FacturacionModel facturacionModel) {
		super();
		this.facturacionModel = facturacionModel;
		Assert.notNull(facturacionModel);
		//setFormasDePago(FormaDePago.EFECTIVO,FormaDePago.CHEQUE);
		initPago();
	}
	
	@Override
	public FormaDePago[] getFormasDePago() {
		if(facturacionModel.getPagos().isEmpty()){
			return new FormaDePago[]{facturacionModel.getPedido().getFormaDePago()};
		}else
			return new FormaDePago[]{FormaDePago.EFECTIVO,FormaDePago.CHEQUE};
	}



	private void initPago(){
		Pedido pedido=facturacionModel.getPedido();
		Assert.notNull(pedido);
		
		getPago().setCliente(pedido.getCliente());
		getPago().setFormaDePago(pedido.getFormaDePago());
		//getPago().registrarImporte(pedido.getTotal());
		getPago().registrarImporte(facturacionModel.getPorPagar());
	}
	
	
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		super.addValidation(support);
		if(facturacionModel==null) return;
		if(!facturacionModel.existePagoConTarjeta()){
			if (facturacionModel.getPagos().size()>0){
				if(getPago().getFormaDePago().name().startsWith("TARJETA")){
					support.getResult().addError("El segundo pago no puede ser con tarjeta");
				}
			}
		}
		if(facturacionModel.getPorPagar().doubleValue()<=0){
			support.getResult().addError("El importe del pedido ya esta pagado");
		}		
	}

	/*protected void formaDePagoAsignada(){		
		if(facturacionModel.getPagos().isEmpty()){
			if(!facturacionModel.getPedido().getFormaDePago().equals(getPago().getFormaDePago())){
				System.out.println("Cuidado psobles cambios en forma de pago nueva: "+getPago().getFormaDePago());
				
				Pedido pedido=facturacionModel.getPedido();
				System.out.println("Total: "+pedido.getTotal());
				pedido.setFormaDePago(getPago().getFormaDePago());
				pedido.setComisionTarjeta(2.0);
				pedido.setDescripcionFormaDePago(FormaDePago.TARJETA.name());
				//pedido.actualizarComisiones();
				pedido.actualizarImportes();
				facturacionModel.getFacturas().clear();
				facturacionModel.agregarFacturas(Services.getInstance().getFacturasManager().prepararParaFacturar(pedido));
				facturacionModel.recalcular();
				System.out.println("Nuevo total: "+pedido.getTotal());
				//Services.getInstance().getPedidosManager().actualizarFormaDePago(pedido);
			}
		}
		getPago().registrarImporte(facturacionModel.getPorPagar());
		
	}*/
	
	
}
