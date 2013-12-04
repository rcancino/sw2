package com.luxsoft.siipap.compras.ui;


import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.model.core.Marca;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.replica.ReplicaExporter.Tipo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.swx.catalogos.ProductoForm;
import com.luxsoft.siipap.swx.catalogos.ProductoFormModel.Familia;

/**
 * Panel para el mantenimientod e articulos
 * 
 * @author Ruben Cancino
 *
 */
public class ProductosPanel extends FilteredBrowserPanel<Producto>{
	
	private CheckBoxMatcher<Producto> activosMatcher;

	public ProductosPanel() {
		super(Producto.class);
		installTextComponentMatcherEditor("Articulo", "clave","descripcion");
		installTextComponentMatcherEditor("Linea", "linea.nombre");
		installTextComponentMatcherEditor("Marca", "marca.nombre");
		installTextComponentMatcherEditor("Clase", "clase.nombre");
		//installTextComponentMatcherEditor("Familia", "lineaOrigen");
		
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
				"id"
				,"clave"
				,"descripcion"
				,"activo"
				,"inventariable"
				,"servicio"
				,"deLinea"
				,"nacional"
				,"linea.nombre"
				,"marca.nombre"
				,"clase.nombre"
				,"kilos"
				,"gramos"
				,"activoVentas"
				,"activoCompras"
				,"activoInventario"
				,"eliminado"
				,"precioContado"
				,"precioCredito"
				,"activoVentasObs"
				);
		addLabels(
				"Id"
				,"Clave"
				,"Descripcion"
				,"Activo"
				,"Inventariable"
				,"Servicio"
				,"DeLinea"
				,"Nacional"
				,"Línea"
				,"Marca"
				,"Clase"
				,"Kilos"
				,"Gramos"
				,"Ventas"
				,"Compras"
				,"Inv"
				,"B"
				,"$Contado"
				,"$Credito"
				,"Obs.Venta"
				);
		boolean[] edits={false,false,false,true,false,true,true,true,false,false,false,false,false,false,true,true,true,true,false,false,false};
		return GlazedLists.tableFormat(beanClazz,getProperties(), getLabels(),edits);
	}
	
	@Override
	protected List<Producto> findData() {
		return ServiceLocator2.getProductoManager().getAll();
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
	protected Producto doInsert() {
		Producto res=ProductoForm.showForm(new Producto());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Producto doEdit(Producto bean) {
		if(!bean.isEliminado()){
			Producto res=ServiceLocator2.getProductoManager().get(bean.getId());
			res=ProductoForm.showForm(res);
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
		ProductoForm.showForm(selected,true);
	}
	
	
	private Producto save(final Producto p){
		return ServiceLocator2.getProductoManager().save(p);
		
	}
	
	public void copiar(){
		if(getSelectedObject()!=null){
			Producto source=(Producto)getSelectedObject();
			Producto target=new Producto();
			BeanUtils.copyProperties(source, target, new String[]{"clave","id"});
			target.setClave(source.getClave()+"_COPIA");
			Producto res=ProductoForm.showForm(target);
			if(res!=null){
				Producto p=save(res);
				this.source.add(p);
			}
		}
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null){
			
			Action copiarAction=new DispatchingAction(this,"copiar");
			copiarAction.putValue(Action.NAME, "Copira");
			
			Action activarSiipap=new DispatchingAction(this,"activarEnSiipap");
			activarSiipap.putValue(Action.NAME, "Activar en Siipap");
			
			Action cancelarSiipap=new DispatchingAction(this,"cancelarEnSiipap");
			cancelarSiipap.putValue(Action.NAME, "Cancelar en Siipap");
			
			Action asignarLinea=new DispatchingAction(this,"asignarLinea");
			asignarLinea.putValue(Action.NAME, "Asignar Línea");
			
			Action asignarMarca=new DispatchingAction(this,"asignarMarca");
			asignarMarca.putValue(Action.NAME, "Asignar Marca");
			
			Action asignarClase=new DispatchingAction(this,"asignarClase");
			asignarClase.putValue(Action.NAME, "Asignar Clase");
			
			Action asignarFamilia=new DispatchingAction(this,"asignarFamilia");
			asignarFamilia.putValue(Action.NAME, "Asignar Familia");
			
			actions=new Action[]{getLoadAction(),getInsertAction(),getDeleteAction()
					,getEditAction(),getViewAction(),copiarAction,activarSiipap
					,cancelarSiipap
					,asignarLinea
					,asignarMarca
					,asignarClase
					,asignarFamilia
					};
		}
		return actions;
	}
	
	public void activarEnSiipap(){
		if(getSelectedObject()!=null){
			Producto sel=(Producto)getSelectedObject();
			if(sel.isEliminado()){				
				sel=ServiceLocator2.getProductoManager().get(sel.getId());
				ServiceLocator2.getExportadorManager().exportarProducto(sel, Tipo.A);
				//Una pausa entre la generacion del archivo
				try {
		            Thread.sleep(1000);
		        } catch (InterruptedException e) {}
				int index=source.indexOf(sel);
				sel.setEliminado(false);
				sel=save(sel);
				source.set(index, sel);
			}
		}
	}

	public void cancelarEnSiipap(){
		if(getSelectedObject()!=null){
			Producto sel=(Producto)getSelectedObject();
			if(!sel.isEliminado()){				
				sel=ServiceLocator2.getProductoManager().get(sel.getId());
				ServiceLocator2.getExportadorManager().exportarProducto(sel, Tipo.B);
				//Una pausa entre la generacion del archivo
				try {
		            Thread.sleep(1000);
		        } catch (InterruptedException e) {}
				int index=source.indexOf(sel);
				sel.setEliminado(true);
				sel=save(sel);
				source.set(index, sel);
			}
		}
	}
	
	public void asignarLinea(){
		if(!getSelected().isEmpty()){
			Linea lin=SelectorDeLineas.seleccionar();
			if(lin!=null){
				ArrayList<Producto> selected=new ArrayList<Producto>();
				selected.addAll(getSelected());
				for(Producto p:selected){
					p=ServiceLocator2.getProductoManager().get(p.getId());
					p.setLinea(lin);
					p.setReplicar(false);
					//p=ServiceLocator2.getProductoManager().save(p);
					int index=source.indexOf(p);
					source.set(index,p);
				}
			}			
		}
	}
	
	public void asignarMarca(){
		if(!getSelected().isEmpty()){
			Marca mar=SelectorDeMarcas.seleccionar();
			if(mar!=null){
				ArrayList<Producto> selected=new ArrayList<Producto>();
				selected.addAll(getSelected());
				for(Producto p:selected){
					p=ServiceLocator2.getProductoManager().get(p.getId());
					p.setMarca(mar);
					p.setReplicar(false);
					int index=source.indexOf(p);
					source.set(index,p);
				}
			}			
		}
	}
	
	public void asignarClase(){
		if(!getSelected().isEmpty()){
			Clase mar=SelectorDeClases.seleccionar();
			if(mar!=null){
				ArrayList<Producto> selected=new ArrayList<Producto>();
				selected.addAll(getSelected());
				for(Producto p:selected){
					p=ServiceLocator2.getProductoManager().get(p.getId());
					p.setReplicar(false);
					p.setClase(mar);
					int index=source.indexOf(p);
					source.set(index,p);
				}
			}			
		}
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
			
			private final ProductosPanel panel=new ProductosPanel();
			
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
