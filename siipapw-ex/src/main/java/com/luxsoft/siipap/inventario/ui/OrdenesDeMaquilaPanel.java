package com.luxsoft.siipap.inventario.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.sw3.services.MaquilaManager;

/**
 * Panel para el mantenimiento de ordenes de maquila
 * y entradas por este concepto
 * 
 * @author Ruben Cancino
 *
 */
public class OrdenesDeMaquilaPanel extends AbstractMasterDatailFilteredBrowserPanel<RecepcionDeMaquila, EntradaDeMaquila>{

	public OrdenesDeMaquilaPanel() {
		super(RecepcionDeMaquila.class);
	}
	
	protected void init(){
		addProperty("id","fecha","sucursal","depurada","comentario");
		addLabels("Id","Fecha","Sucursal","Depurada","Comentario");
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"orden.id","producto.clave","producto.descripcion","cantidad","fecha"};
		String[] labels={"Orden","Producto","Descripcion","Recibido","Fecha"};
		return GlazedLists.tableFormat(EntradaDeMaquila.class, props, labels);
	}

	@Override
	protected Model<RecepcionDeMaquila, EntradaDeMaquila> createPartidasModel() {
		return new Model<RecepcionDeMaquila, EntradaDeMaquila>(){
			public List<EntradaDeMaquila> getChildren(RecepcionDeMaquila parent) {
				return new ArrayList<EntradaDeMaquila>(parent.getPartidas());
			}			
		};
	}
	
	private MaquilaManager getManager(){
		return ServiceLocator2.getMaquilaManager();
	}
	

	@Override
	protected List<RecepcionDeMaquila> findData() {
		//return ServiceLocator2.getMaquilaManager().getAll();
		return ListUtils.EMPTY_LIST;
	}
	
	
	/**** Altas/Bajas/Cambios ***/

	@Override
	protected RecepcionDeMaquila doInsert() {
		/*RecepcionDeMaquila m=new RecepcionDeMaquila();
		m=OrdenDeMaquilaForm.showForm(m);
		if(m!=null){
			m=ServiceLocator2.getMaquilaManager().save(m);
			return m;
		}*/
		return null;
	}

	@Override
	protected void doSelect(Object bean) {
		RecepcionDeMaquila m=(RecepcionDeMaquila)bean;
		m=OrdenDeMaquilaForm.showForm(m,true);
	}



	@Override
	public boolean doDelete(RecepcionDeMaquila bean) {
		//ServiceLocator2.getMaquilaManager().remove(bean.getId());
		return true;
		 
	}
	
	
	@Override
	protected RecepcionDeMaquila doEdit(RecepcionDeMaquila bean) {
//		RecepcionDeMaquila m=getManager().get(bean.getId());
//		m=OrdenDeMaquilaForm.showForm(m, false);
//		if(m!=null){
//			System.out.println("Actualizando Orden de Maq: "+m.getId()+"Comentario: "+m.getComentario());
//			RecepcionDeMaquila res=getManager().save(m);
//			return res;
//		}
		return bean;
	}


	
	/**** END Altas/Bajas/Cambios ***/	


}
