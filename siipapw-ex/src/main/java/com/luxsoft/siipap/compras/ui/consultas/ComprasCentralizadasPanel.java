package com.luxsoft.siipap.compras.ui.consultas;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.reportes.ComprasDepuradasReportForm;
import com.luxsoft.siipap.reportes.ComprasPendientesReportForm;
import com.luxsoft.siipap.reportes.ReporteDeAlcancesForm;
import com.luxsoft.siipap.reportes.ReporteDeSugerenciaDeTrasladoForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Consulta para el control y mantenimiento de embarques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ComprasCentralizadasPanel extends AbstractMasterDatailFilteredBrowserPanel<Compra2,CompraUnitaria>{
	
	ComprasCentralizadasController controller;

	public ComprasCentralizadasPanel() {
		super(Compra2.class);
		setTitle("Compras centralizadas");
	}
	
	protected void init(){
		controller=new ComprasCentralizadasController();
		super.init();
	}
	
	CheckBoxMatcher<Compra2> nacionalesMatcher;
	CheckBoxMatcher<Compra2> importadosMatcher;
		
	@Override
	protected void agregarMasterProperties(){
		addProperty("folio","importacion","especial","sucursal.nombre","clave","nombre","fecha","entrega","moneda","tc","total","descuentoEspecial","depuracion","consolidada","comentario");
		addLabels("Folio","Imp","Esp","Sucursal","Prov","Nombre","Fecha","Entrega","Mon","TC","Total","Dscto","Depuracion","Con","Comentario");
		setDetailTitle("Ordenes de Compra");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Proveedor", "clave","nombre");		
		installTextComponentMatcherEditor("Folio", "folio");
		
		importadosMatcher=new CheckBoxMatcher<Compra2>(false) {			
			protected Matcher<Compra2> getSelectMatcher(Object... obj) {				
				return new Matcher<Compra2>() {					
					public boolean matches(Compra2 item) {
						return item.isImportacion();
					}					
				};
			}
		};
		installCustomMatcherEditor("Importados", importadosMatcher.getBox(), importadosMatcher);
		manejarPeriodo();
	}
	
	
	@Override
	protected Model<Compra2, CompraUnitaria> createPartidasModel() {
		return new Model<Compra2, CompraUnitaria>(){
			public List<CompraUnitaria> getChildren(Compra2 parent) {
				return controller.buscarPartidas(parent);
			}
		};
	}	

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"compra.folio","sucursalNombre","clave","descripcion","unidad","solicitado","recibido","depurado","pendiente","aduana","depuracion","folioOrigen","registroAduana","comentario"};
		String[] labels={"Folio","Sucursal","Prod","Descrpción","U","Solicitado","Recibido","Depurado","Pendiente","Aduana","Depuracion","Origen","Aduana(act)","Comentario"};
		return GlazedLists.tableFormat(CompraUnitaria.class, props,labels);
	}
	
	protected void adjustMainGrid(final JXTable grid){
		grid.getColumnExt("Depuracion").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		
	}	
	
	private Action printAction;
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			getInsertAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/basket_add.png"));
			getEditAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/basket_edit.png"));
			printAction=addContextAction(new NotNullSelectionPredicate(), "", "imprimir", "Imprimir");
			printAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/file/printview_tsk.gif"));
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,getInsertAction()
				,getEditAction()
				,printAction
				,addAction("", "insertarImportacion", "Compra de Importación")
				,addAction("", "consolidar", "Consolidar")
				,addAction("", "cancelar","Cancelar")
				,addAction("", "cambiarEntrega","Cambiar entrega")
				,addAction("", "depurar","Depurar")
				};
		}
		return actions;
	}
	
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporteDeAlcances", "Alcances"));
		procesos.add(addAction("","reporteComprasDepuradas", "Compras Depuradas"));
		procesos.add(addAction("", "reporteComprasPendientes", "Compras Pendientes"));
		procesos.add(addAction("", "actualizarPrecios", "Actualizar Precios"));
		procesos.add(addAction("", "reporteDeSugerenciaDeTraslado", "Sugerencia de Traslados"));
		return procesos;
	}
	
	private JCheckBox pendientesBox;
	
	public JComponent[] getOperacionesComponents(){
		if(pendientesBox==null){
			pendientesBox=new JCheckBox("Pendientes",true);
			pendientesBox.setOpaque(false);
		}
		return new JComponent[]{pendientesBox};
	}
	
	/*
	public void cambiarPeriodo(){
		if(!pendientesBox.isSelected())
			super.cambiarPeriodo();
	}
	*/
	
	@Override
	protected List<Compra2> findData() {
		if(pendientesBox.isSelected())
			return controller.buscarPendientes();
		else
			return controller.buscarCompras(periodo);
	}
	
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}
	
	
	/** Implementacion de acciones ***/
	
	@Override
	protected Compra2 doInsert() {
		return controller.generarCompra();
	}
	
	@Override
	protected void afterInsert(Compra2 bean) {
		super.afterInsert(bean);
		imprimir(bean);
	}
	
	@Override
	protected Compra2 doEdit(Compra2 bean) {
		return controller.editar(bean);
	}

	@Override
	protected void doSelect(Object bean) {
		Compra2 selected=(Compra2)bean;
		controller.mostrarCompra(selected.getId());
	}

	public void cancelar(){
		Compra2 res=controller.cancelar(getSelectedObject());
		if(res!=null){
			int index=source.indexOf(res);
			if(index!=-1)
				source.set(index, res);
		}
	}
	
	public void depurar(){
		Compra2 res=controller.depurar(getSelectedObject());
		if(res!=null){
			int index=source.indexOf(res);
			if(index!=-1)
				source.set(index, res);
		}
	}
	
	public void imprimir(){
		executeSigleSelection(new SingleSelectionHandler<Compra2>(){
			public Compra2 execute(Compra2 selected) {
				ComprasCentralizadasController.imprimir(selected);
				return selected;
			}
		});
	}
	
	public void imprimir(Compra2 c){
		ComprasCentralizadasController.imprimir(c);
	}
	
	public void reporteComprasDepuradas() {
		ComprasDepuradasReportForm.run();
	}
	
	public void reporteComprasPendientes() {
		ComprasPendientesReportForm.run();
	}
	
	public void reporteDeAlcances(){
		ReporteDeAlcancesForm.run();
	}
	
	public void reporteDeSugerenciaDeTraslado(){
		ReporteDeSugerenciaDeTrasladoForm.run();
	}

	public void consolidar(){
		if(!selectionModel.getSelected().isEmpty()){
			
			List<String> ids=new ArrayList<String>();
			for(Object o:getSelected()){
				Compra2 c=(Compra2)o;
				ids.add(c.getId());
			}
			Compra2 res=controller.consolidar(ids);
			if(res!=null){
				source.add(res);
				imprimir(res);
			}
			
		}
	}
	
	public void actualizarPrecios(){
		if(!selectionModel.getSelected().isEmpty()){
			
			List<Compra2> compras=new ArrayList<Compra2>();
			for(Object o:getSelected()){
				Compra2 c=(Compra2)o;
				compras.add(c);
			}
			for(Compra2 c:compras){
				Compra2 res=controller.actualizarPrecios(c);
				if(res!=null){
					int index=source.indexOf(res);
					if(index!=-1){
						source.set(index, res);
					}
				}
			}
			
		}
	}
	
	public void insertarImportacion(){
		Compra2 res=controller.generarCompraDeImportacion();
		if(res!=null){
			source.add(res);
			imprimir(res);
		}
	}
	
	public void cambiarEntrega(){
		Object selected=getSelectedObject();
		if(selected!=null){
			int index=source.indexOf(selected);
			Date fecha=SelectorDeFecha.seleccionar();
			Compra2 compra=(Compra2)selected;
			compra=ServiceLocator2.getComprasManager().buscarInicializada(compra.getId());
			compra.setEntrega(fecha);
			compra=(Compra2)ServiceLocator2.getHibernateTemplate().merge(compra);
			if(index!=-1){
				source.set(index, compra);
			}
			
		}
	}
	

}
