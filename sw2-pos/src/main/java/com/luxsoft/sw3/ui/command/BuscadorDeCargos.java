package com.luxsoft.sw3.ui.command;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.selectores.SelectorDeCargos;

public class BuscadorDeCargos extends AbstractAction{

	public void actionPerformed(ActionEvent e) {
		String res=JOptionPane.showInputDialog(Application.isLoaded()?Application.instance().getMainFrame():null,"Factura");
		if(StringUtils.isNotBlank(res)){
			if(NumberUtils.isNumber(res)){
				Long folio=Long.valueOf(res);
				Venta found=SelectorDeCargos.buscar(folio,Services.getInstance().getConfiguracion().getSucursal().getId());
				if(found!=null){
					FacturaForm.show(found.getId());
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		new BuscadorDeCargos().actionPerformed(null);
	}

}
