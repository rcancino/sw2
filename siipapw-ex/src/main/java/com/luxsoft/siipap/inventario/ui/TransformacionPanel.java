package com.luxsoft.siipap.inventario.ui;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.compras.ui.ProductosPanel;
import com.luxsoft.siipap.inventario.TransformacionForm;
import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.swx.catalogos.ProductoForm;

public class TransformacionPanel extends FilteredBrowserPanel<Transformacion>{

	public TransformacionPanel() {
		super(Transformacion.class);
		
	}
	
	@Override
	protected TableFormat buildTableFormat() {
		addProperty("id","clave","descripcion","lineaOrigen","activo","inventariable","servicio","deLinea","nacional","linea.nombre","clase.nombre","marca.nombre","lineaOrigen","kilos","gramos","eliminado");
		addLabels("Id","Clave","Descripcion","Familia","Activo","Inventariable","Servicio","DeLinea","Nacional","Línea","Clase","Marca","Familia","Kilos","Gramos","B");
		return GlazedLists.tableFormat(getProperties(), getLabels());
	}
	
	@Override
	protected List<Transformacion> findData() {
		return null;//ServiceLocator2.getProductoManager().getAll();
	}
	
	@Override
	protected EventList getSourceEventList() {
		EventList<Transformacion>res=new ObservableElementList<Transformacion>(super.getSourceEventList()
				,GlazedLists.beanConnector(Transformacion.class));
		return super.getSourceEventList();
	}
	
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
	@Override
	public void open() {
		load();
	}
	@Override
	public void close() {
		
	}
	
	@Override
	protected Transformacion doInsert() {
		Transformacion res=TransformacionForm.showForm(new Transformacion());
		if(res!=null)
			return save(res);
		return null;
	}
	
	
	@Override
	protected Transformacion doEdit(Transformacion bean) {
	/*	if(!bean.isEliminado()){
			Transformacion res=ServiceLocator2.getInventarioManager().salvarTransformacion(bean);
			res=TransformacionForm.showForm(res);
			if(res!=null)
				return save(res);			
		}else{
			MessageUtils.showMessage("Este producto esta eliminado en siipap, debe activarlo  primero", "Productos");
			
		}*/
		return null;	
	}
	
	@Override
	protected void doSelect(Object bean) {
//		Transformacion selected=ServiceLocator2.getProductoManager().get(((Transformacion)bean).getId());
//		ProductoForm.showForm(selected,true);
	}
	
	
	private Transformacion save(final Transformacion p){
		return ServiceLocator2.getInventarioManager().salvarTransformacion(p);
		
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
