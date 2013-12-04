package com.luxsoft.sw3.ui.command;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;

import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.pedidos.forms.ExistenciasConsultaForm;
import com.luxsoft.sw3.pedidos.forms.PedidoDetFormModel2;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.selectores.SelectorDeCargos;
import com.luxsoft.sw3.ventas.PedidoDet;

public class ExistenciaFinder extends AbstractAction{

	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				final PedidoDet det=PedidoDet.getPedidoDet();
				
				PedidoDetFormModel2 model=new PedidoDetFormModel2(det);
				model.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
				ExistenciasConsultaForm form=new ExistenciasConsultaForm(model);
				form.setProductos(Services.getInstance().getProductosManager().getActivosAsRows());
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println("Ped: "+ToStringBuilder.reflectionToString(model.getBaseBean()));
				}
				
				
			}
		});
		
	}
	
	public static void main(String[] args) {
		new ExistenciaFinder().actionPerformed(null);
	}

}
