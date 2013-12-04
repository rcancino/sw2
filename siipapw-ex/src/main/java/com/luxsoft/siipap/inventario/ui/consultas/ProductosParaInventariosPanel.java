package com.luxsoft.siipap.inventario.ui.consultas;


import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.compras.ui.SelectorDeFamilias;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

import com.luxsoft.siipap.swx.catalogos.ProductoFormModel.Familia;

/**
 * Panel para el mantenimientod e articulos
 * 
 * @author Ruben Cancino
 *
 */
public class ProductosParaInventariosPanel extends FilteredBrowserPanel<Producto>{
	
	private CheckBoxMatcher<Producto> activosMatcher;

	public ProductosParaInventariosPanel() {
		super(Producto.class);
		installTextComponentMatcherEditor("Articulo", "clave","descripcion");
		installTextComponentMatcherEditor("Linea", "linea.nombre");
		installTextComponentMatcherEditor("Marca", "marca.nombre");
		installTextComponentMatcherEditor("Clase", "clase.nombre");
		installTextComponentMatcherEditor("Familia", "lineaOrigen");
		activosMatcher=new CheckBoxMatcher<Producto>(){			
			protected Matcher<Producto> getSelectMatcher(Object... obj) {
				return Matchers.beanPropertyMatcher(Producto.class, "activo", Boolean.TRUE);
			}
		};
		activosMatcher.getBox().setEnabled(true);
		installCustomMatcherEditor("Activos ", activosMatcher.getBox(), activosMatcher);
	}
	
	@Override
	protected TableFormat buildTableFormat() {
		addProperty(
				"clave"
				,"descripcion"
				,"activo"
				,"inventariable"
				,"deLinea"
				,"nacional"
				,"linea.nombre"
				,"marca.nombre"
				,"clase.nombre"
				,"kilos"
				,"gramos"
				);
		addLabels(
				"Clave"
				,"Descripcion"
				,"Activo"
				,"Inventariable"
				,"DeLinea"
				,"Nacional"
				,"Línea"
				,"Marca"
				,"Clase"
				,"Kilos"
				,"Gramos"
				);
		return GlazedLists.tableFormat(beanClazz,getProperties(), getLabels());
	}
	
	@Override
	protected List<Producto> findData() {
		return ServiceLocator2.getProductoManager().buscarProductosActivosYDeLinea();
	}	
	
	protected EventList<Producto> getSourceEventList(){
		EventList<Producto>  res=new ObservableElementList<Producto>(super.getSourceEventList(),GlazedLists.beanConnector(Producto.class));
		res.addListEventListener(new ListHandler());
		return res;
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
	public void open(){
		load();
	}
	
	public void close(){
		
	}
	
	
	
	@Override
	protected Producto doEdit(Producto bean) {
		if(!bean.isEliminado()){
			Producto res=ServiceLocator2.getProductoManager().get(bean.getId());
			res=ProductoParaInventarioForm.showForm(res);
			if(res!=null)
				return save(res);			
		}else{
			MessageUtils.showMessage("Este producto esta eliminado en siipap, debe activarlo  primero", "Productos");
			
		}
		return null;	
	}
	
	@Override
	protected void doSelect(Object bean) {
		Producto selected=ServiceLocator2.getProductoManager().get(((Producto)bean).getId());
		ProductoParaInventarioForm.showForm(selected,true);
	}
	
	
	private Producto save(final Producto p){
		KernellSecurity.instance().registrarUserLog(p, "userLog");
		return ServiceLocator2.getProductoManager().save(p);
		
	}
	

	@Override
	public Action[] getActions() {
		if(actions==null){
			Action asignarFamilia=new DispatchingAction(this,"asignarFamilia");
			asignarFamilia.putValue(Action.NAME, "Asignar Familia");
			
			actions=new Action[]{
					getLoadAction()
					,getEditAction()
					,getViewAction()
					//,asignarFamilia
					};
		}
		return actions;
	}
	
	public void asignarFamilia(){
		if(!getSelected().isEmpty()){
			Familia fam=SelectorDeFamilias.seleccionar();
			if(fam!=null){
				ArrayList<Producto> selected=new ArrayList<Producto>();
				selected.addAll(getSelected());
				for(Producto p:selected){
					p=ServiceLocator2.getProductoManager().get(p.getId());
					p.setLineaOrigen(fam.getClave());
					p.setReplicar(false);
					//p=ServiceLocator2.getProductoManager().save(p);
					int index=source.indexOf(p);
					source.set(index,p);
				}
			}			
		}
	}
	
	/**
	 * Detectea cualquier cambio en la lista de beans para 
	 * persistir los mismos a la base de datos
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class ListHandler implements ListEventListener<Producto>{

		public void listChanged(ListEvent<Producto> listChanges) {
			while(listChanges.next()){
				if(listChanges.getType()==ListEvent.UPDATE){
					int index=listChanges.getIndex();
					Producto p=listChanges.getSourceList().get(index);
					p=save(p);
					listChanges.getSourceList().set(index,p);
				}
			}			
		}		
	}
	

	public static void main(String[] args) {
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){
			
			private final ProductosParaInventariosPanel panel=new ProductosParaInventariosPanel();
			
			@Override
			protected JComponent buildContent() {
				return panel.getControl();
			}

			@Override
			protected void onWindowOpened() {
				panel.open();
			}
			
			
		};
		dialog.open();
		System.exit(0);
	}


}
