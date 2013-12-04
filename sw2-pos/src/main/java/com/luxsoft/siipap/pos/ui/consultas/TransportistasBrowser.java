package com.luxsoft.siipap.pos.ui.consultas;

import java.util.List;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.embarques.ServicioDeTransporte;
import com.luxsoft.siipap.pos.ui.forms.ServicioDeTransporteForm;
import com.luxsoft.siipap.swing.dialog.AbstractCatalogDialog;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;
import com.luxsoft.sw3.services.Services;

public class TransportistasBrowser extends AbstractCatalogDialog<ServicioDeTransporte>{

	public TransportistasBrowser() {
		super(ServicioDeTransporte.class, new BasicEventList<ServicioDeTransporte>(), "Catálogo de transportes foraneos");
		
	}
	
	@Override
	protected TableFormat<ServicioDeTransporte> getTableFormat() {
		String[] props={"nombre"};
		String[] names={"Nombre"};
		return GlazedLists.tableFormat(ServicioDeTransporte.class,props, names);
	}

	@Override
	protected ServicioDeTransporte doInsert() {
		return ServicioDeTransporteForm.showForm();
	}

	@Override
	protected boolean doDelete(ServicioDeTransporte bean) {
		Services.getInstance().getUniversalDao().remove(ServicioDeTransporte.class,bean.getId());
		return true;
	}
	
	private UniversalDao getDao(){
		return Services.getInstance().getUniversalDao();
	}

	@Override
	protected ServicioDeTransporte save(ServicioDeTransporte bean) {
		return bean;
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
				TransportistasBrowser browser=new TransportistasBrowser();
				browser.open();
				System.exit(0);
			}

		});
	}

	@Override
	protected List<ServicioDeTransporte> getData() {
		return Services.getInstance().getUniversalDao().getAll(ServicioDeTransporte.class);
	}

	
}
