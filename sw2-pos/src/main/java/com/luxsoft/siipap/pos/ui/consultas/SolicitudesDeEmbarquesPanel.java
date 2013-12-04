package com.luxsoft.siipap.pos.ui.consultas;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.exception.ExceptionUtils;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.forms.EmbarqueController;
import com.luxsoft.siipap.pos.ui.forms.SolicitudDeEmbarqueForm;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeFacturas;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.ventas.model.Venta;

import com.luxsoft.sw3.embarque.SolicitudDeEmbarque;
import com.luxsoft.sw3.services.Services;

/**
 * Consulta para el control y mantenimiento de solicitudes de embarques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SolicitudesDeEmbarquesPanel extends FilteredBrowserPanel<SolicitudDeEmbarque>{
	

	public SolicitudesDeEmbarquesPanel() {
		super(SolicitudDeEmbarque.class);
		
	}
	public void init(){
		addProperty("id","fecha","factura.documento","factura.numeroFiscal","factura.origen","factura.fecha","factura.sucursal","factura.nombre"
				,"factura.total","direccion.cp","direccion.municipio","direccion.colonia","direccion.calle");
		addLabels("Id","Fecha","Docto","Fiscal","Tipo","Fecha F","Sucursal","Cliente"
				,"Total","CP","Del/Mpo","Col","Calle");
		installTextComponentMatcherEditor("Cliente", "factura.clave","factura.nombre");
		installTextComponentMatcherEditor("Mpo/Del", "direccion.municipio");
		installTextComponentMatcherEditor("CP", "direccion.cp");
	}

		
	
	/*private Action buscarAction;
	private Action cerrarAction;
	private Action cancelarCierreAction;
	private Action salidaAction;
	private Action registrarRetorno;
	private Action registrarIncidente;
	private Action agregarEntrega;
	private Action eliminarEntrega;
	private Action modificarEntrega;*/
	
	
	protected void initActions(){
		/*buscarAction=addAction("buscar.id","buscar", "Buscar");
		buscarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/page_find.png"));
		
		cerrarAction=addContextAction(new SinCerrarPredicate(), POSRoles.EMBARQUES.name(), "cerrarEmbarque", "Cerrar");
		cerrarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_link.png"));
		
		cancelarCierreAction=addContextAction(new CerradoPredicate(), POSRoles.EMBARQUES.name(), "cancelarCierre", "Abrir");		
		cancelarCierreAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_error.png"));
		
		salidaAction=addContextAction(new CerradoPredicate(), POSRoles.EMBARQUES.name(), "registrarSalida", "Salida");		
		salidaAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_go.png"));
		
		registrarRetorno=addContextAction(new EnviadoPredicate(), POSRoles.EMBARQUES.name(), "registrarRetorno", "Retorno");		
		registrarRetorno.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_flatbed.png"));
		
		registrarIncidente=addContextAction(new CerradoPredicate(), POSRoles.EMBARQUES.name(), "registrarIncidente", "Incidente");		
		registrarIncidente.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_error.png"));
		
		agregarEntrega=addContextAction(new SinCerrarPredicate(), POSRoles.EMBARQUES.name(), "agregarEntrega", "Agregar");		
		agregarEntrega.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/table_add.png"));
	
		eliminarEntrega=addContextAction(new SinCerrarPredicate(), POSRoles.EMBARQUES.name(), "eliminarEntrega", "Eliminar");		
		eliminarEntrega.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/table_delete.png"));
		
		modificarEntrega=addContextAction(new SinCerrarPredicate(), POSRoles.EMBARQUES.name(), "modificarEntrega", "Modificar");		
		modificarEntrega.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/table_edit.png"));
		
		getInsertAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_add.png"));
		getDeleteAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_delete.png"));*/
		
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			initActions();
			actions=new Action[]{
				getLoadAction()
				//,buscarAction
				//,getViewAction()
				,getInsertAction()
				,getEditAction()
				,getDeleteAction()
				};
		}
		return actions;
	}
	
	
	
	@Override
	protected List<SolicitudDeEmbarque> findData() {
		String hql="from SolicitudDeEmbarque e ";
		return Services.getInstance().getHibernateTemplate().find(hql);			
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}
	
	/** Implementacion de acciones ***/
	
	public void insert(){
		List<Venta> facturas=SelectorDeFacturas.seleccionar();
		if(facturas.isEmpty())
			return;
		Venta factura=facturas.get(0);
		boolean valid=validarFactura(factura);
		if(valid){
			SolicitudDeEmbarque target=new SolicitudDeEmbarque();
			target.setFactura(factura);
			DefaultFormModel model=new DefaultFormModel(target);
			SolicitudDeEmbarqueForm form=new SolicitudDeEmbarqueForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				target=(SolicitudDeEmbarque)Services.getInstance().getUniversalDao().save(model.getBaseBean());
				if(target!=null)
					source.add(target);
			}
		}else{
			MessageUtils.showMessage("La factura "+factura.getDocumento()+ "ya está registrada", "Embarque de facturas");
		}
		
		
	}
	
	private boolean validarFactura(Venta fac){
		return true;
	}
	
	
	
	/*public void cerrarEmbarque(){
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.cerrarEmbarque(selected, grid);
			}
		});
	}*/
	
	
	
	@Override
	public boolean doDelete(SolicitudDeEmbarque bean) {
		Services.getInstance().getUniversalDao().remove(SolicitudDeEmbarque.class, bean.getId());
		return true;
	}
	
	public void refreshSelection(){
		
	}
	
	
	

}
