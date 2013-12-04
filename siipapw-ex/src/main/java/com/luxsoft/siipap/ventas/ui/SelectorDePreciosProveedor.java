package com.luxsoft.siipap.ventas.ui;

import java.awt.Dimension;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;

public class SelectorDePreciosProveedor extends AbstractSelector<ListaDePreciosDet>{
	
	private String prodClave;

	public SelectorDePreciosProveedor() {
		super(ListaDePreciosDet.class, "Precios disponibles por proveedor");
		
	}

	@Override
	protected List<ListaDePreciosDet> getData() {
		if(StringUtils.isNotBlank(getProdClave()))
			return ServiceLocator2.getListaDePreciosDao().buscarPreciosVigentes(getProdClave(), new Date());
		else
			return ListUtils.EMPTY_LIST;
	}

	@Override
	protected TableFormat<ListaDePreciosDet> getTableFormat() {
		String[] props={
				"clave"
				,"descripcion"
				,"precio"
				,"precio.currency"
				,"costo"
				,"lista.proveedor.clave"
				,"lista.proveedor.nombre"
				,"lista.id"
				,"lista.descripcion"
				,"lista.vigente"
				,"lista.fechaInicial"
				,"lista.fechaFinal"};
		String[] names={
				"Producto"
				,"Descripción"
				,"Precio"
				,"Moneda"
				,"Costo"
				,"Proveedor"
				,"Nombre"
				,"Lista"
				,"Comentario"
				,"Vigente"
				,"F.Inicial"
				,"F.Final"
				};
		return GlazedLists.tableFormat(ListaDePreciosDet.class, props,names);
	}
	
	@Override
	public void adjustGrid(JXTable grid) {
		String[] hide={"Nombre","F.Inicial","F.Final"};
		for(String col:hide){
			grid.getColumnExt(col).setVisible(false);
		}		
	}	

	@Override
	protected void setPreferedDimension(JComponent gridComponent) {		
		gridComponent.setPreferredSize(new Dimension(790,330));
	}	

	public String getProdClave() {
		return prodClave;
	}

	public void setProdClave(String prodClave) {
		this.prodClave = prodClave;
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
				SelectorDePreciosProveedor selector=new SelectorDePreciosProveedor();
				selector.setProdClave("POL74");
				selector.open();
				System.out.println(selector.getSelected());
				System.exit(0);
			}

		});
	}

}
