package com.luxsoft.siipap.pos.ui.venta.forms;

import org.springframework.beans.BeanUtils;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.ventas.CheckPlusVenta;
import com.luxsoft.sw3.ventas.Pedido;

public class CheckplusVentaFormModel extends DefaultFormModel{
	
	private final  Pedido pedido;

	public CheckplusVentaFormModel(Pedido pedido) {
		super(Bean.proxy(CheckPlusVenta.class));
		this.pedido=pedido;
		getCheckPlus().setRazonSocial(pedido.getCliente().getNombre());
		getCheckPlus().setDireccion(pedido.getCliente().getDireccionFiscal());
	}
	
	
	public CheckPlusVenta getCheckPlus(){
		return (CheckPlusVenta)getBaseBean();
	}
	
	public CheckPlusVenta commit(){
		
		getCheckPlus().setCargo(pedido.getCheckplusOpcion().getCargo());
		getCheckPlus().setPlazo(pedido.getCheckplusOpcion().getPlazo());
		CheckPlusVenta target=new CheckPlusVenta();
		
		BeanUtils.copyProperties(getBaseBean(), target);
		KernellSecurity.instance().registrarAddressLog(target, "addresLog");
		KernellSecurity.instance().registrarUserLog(target, "log");
		return target;
	}
	
	
	

}
