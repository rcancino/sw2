package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JComponent;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

public class SelectorDeAnticiposFacturadosPendientes extends AbstractSelector<Venta>{
	
	private final String cliente;
	
	public SelectorDeAnticiposFacturadosPendientes(final String cliente) {
		super(Venta.class, "Selector de Anticipos facturados disponibles");
		this.cliente=cliente;
		
	}	

	@Override
	protected List<Venta> getData() {
		return Services.getInstance()
			.getHibernateTemplate()
			.find("from Venta v where v.anticipo=true and v.clave=?",cliente);
		
	}
	
	

	@Override
	protected void setPreferedDimension(JComponent gridComponent) {
		gridComponent.setPreferredSize(new Dimension(600,400));
		
	}

	@Override
	protected TableFormat<Venta> getTableFormat() {
		String props[]={
				"sucursal.nombre"
				,"origen"
				,"documento"
				,"fecha"
				,"clave"
				,"nombre"
				,"total"
				,"disponibleDeAnticipo"
				};
		String labels[]={
				"Sucursal"
				,"Tipo"
				,"Documento"
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Total"
				,"Disponible"
				};
		return GlazedLists.tableFormat(Venta.class,props,labels);
	}
	
	
	public static Venta seleccionar(String cliente){
		SelectorDeAnticiposFacturadosPendientes selector=new SelectorDeAnticiposFacturadosPendientes(cliente);
		selector.open();
		if(!selector.hasBeenCanceled())
			return selector.getSelected();
		else return null;
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
				seleccionar("I020376");
			}

		});
	}

}
