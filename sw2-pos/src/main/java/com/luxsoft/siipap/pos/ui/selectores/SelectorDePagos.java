package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JComponent;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;

public class SelectorDePagos extends AbstractSelector<Pago>{

	
	public SelectorDePagos() {
		super(Pago.class, "Selector de pagos");		
	}

	@Override
	protected TableFormat<Pago> getTableFormat() {
		String[] props={
				"sucursal.nombre"
				,"origenAplicacion"
				,"info"
				,"clave"
				,"nombre"
				,"fecha"
				,"primeraAplicacion"
				,"banco"
				,"anticipo"
				,"total"
				,"diferencia"
				,"disponible"
				,"deposito"
				};
		String[] names={
				"Sucursal"
				,"Origen"
				,"Info"
				,"Cliente"
				,"Nombre"
				,"Fecha"
				,"Primera Ap"
				,"Banco"
				,"Anticipo"
				,"Total"
				,"Dif"
				,"Disponible"
				,"Deposito"
				};
		return GlazedLists.tableFormat(Pago.class,props,names);
	}
	
	@Override
	protected List<Pago> getData() {
		return null;
	}

	@Override
	protected void setPreferedDimension(JComponent gridComponent) {
		gridComponent.setPreferredSize(new Dimension(650,450));
	}
	
	


}
