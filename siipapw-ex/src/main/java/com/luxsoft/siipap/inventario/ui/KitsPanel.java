package com.luxsoft.siipap.inventario.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.inventarios.model.Kit;
import com.luxsoft.siipap.inventarios.model.KitDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Panel para el mantenimiento de ordenes de movimientos
 * de productos Kit
 * 
 * @author Ruben Cancino
 *
 */
public class KitsPanel extends AbstractMasterDatailFilteredBrowserPanel<Kit, KitDet>{

	public KitsPanel() {
		super(Kit.class);
	}
	
	protected void init(){
		manejarPeriodo();
		addProperty("id","fecha","sucursal.nombre","comentario","comentario","entrada.producto.clave","entrada.producto.descripcion");
		addLabels("Id","Fecha","Sucursal","Comentario","Producto Kit","Descripción");
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"id","producto.clave","producto.descripcion","cantidad","fecha"};
		String[] labels={"Id","Producto","Descripcion","Cantidad","Fecha"};
		return GlazedLists.tableFormat(KitDet.class, props, labels);
	}

	@Override
	protected Model<Kit, KitDet> createPartidasModel() {
		return new Model<Kit, KitDet>(){
			public List<KitDet> getChildren(Kit parent) {
				return new ArrayList<KitDet>(parent.getSalidas());
			}			
		};
	}

	@Override
	protected List<Kit> findData() {
		return ServiceLocator2.getInventarioManager().buscarMovimientsKit(periodo);
	}
	
	
	/**** Altas/Bajas/Cambios ***/

	@Override
	protected Kit doInsert() {
		return null;
	}

	@Override
	protected void doSelect(Object bean) {		
	}

	@Override
	public boolean doDelete(Kit bean) {
		ServiceLocator2.getInventarioManager().eliminarMovimientoKit(bean);
		return true;		 
	}
	
	@Override
	protected Kit doEdit(Kit bean) {
		return bean;
	}
	
	public static void main(String[] args) {
		
		final KitsPanel panel=new KitsPanel();
		
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){
			
			@Override
			protected JComponent buildContent() {				
				return panel.getControl();
			}
			
		};
		dialog.setSize(500, 400);
		dialog.open();
	}

}
