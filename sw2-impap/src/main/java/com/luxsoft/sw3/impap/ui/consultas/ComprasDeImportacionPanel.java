package com.luxsoft.sw3.impap.ui.consultas;

import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.cxp.ui.selectores.SelectorDeProveedores;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.reportes.ComprasDepuradasReportForm;
import com.luxsoft.siipap.reportes.ComprasPendientesReportForm;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
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
public class ComprasDeImportacionPanel extends AbstractMasterDatailFilteredBrowserPanel<Compra2,CompraUnitaria>{
	
	ComprasDeImportacionController controller;

	public ComprasDeImportacionPanel() {
		super(Compra2.class);
		setTitle("Compras");
	}
	
	protected void init(){
		controller=new ComprasDeImportacionController();
		super.init();
	}
	
	CheckBoxMatcher<Compra2> nacionalesMatcher;
	CheckBoxMatcher<Compra2> importadosMatcher;
		
	@Override
	protected void agregarMasterProperties(){
		addProperty("folio","especial","clave","nombre","fecha","moneda","tc","total","descuentoEspecial","depuracion","consolidada","comentario");
		addLabels("Folio","Esp","Prov","Nombre","Fecha","Mon","TC","Total","Dscto","Depuracion","Con","Comentario");
		setDetailTitle("Ordenes de Compra");
		//installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Proveedor", "clave","nombre");		
		installTextComponentMatcherEditor("Folio", "folio");		
		manejarPeriodo();
	}
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodoConAnteriroridad(6);
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
		String[] props={"compra.folio","sucursalNombre","clave","descripcion","unidad","solicitado","recibido","depurado","pendiente","depuracion","folioOrigen","comentario"};
		String[] labels={"Folio","Sucursal","Prod","Descrpción","U","Solicitado","Recibido","Depurado","Pendiente","Depuracion","Origen","Comentario"};
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
				,addAction("", "cancelar","Cancelar")
				,addAction("", "depurar","Depurar")
				,addAction("", "importar","Importar")
				,addAction("", "cambiarProveedor","Cambiar proveedor")
				};
		}
		return actions;
	}
	
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();		
		procesos.add(addAction("","reporteComprasDepuradas", "Compras Depuradas"));
		procesos.add(addAction("", "reporteComprasPendientes", "Compras Pendientes"));
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
		return controller.generarCompraDeImportacion();
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
	
	public void importar(){
		String val=JOptionPane.showInputDialog("Folio: ");
		if(StringUtils.isNotBlank(val)){
			if(NumberUtils.isNumber(val)){
				Long folio=new Long(val);
				Compra2 compra=controller.importarCompra(folio);
				source.add(compra);
			}
		}
	}
	
	public void cambiarProveedor(){
		Compra2 selected=(Compra2)getSelectedObject();
		if(selected!=null){
			int index=source.indexOf(selected);
			if(index!=-1){
				Compra2 res=controller.cambiarProveedor(selected);
				if(res!=null){
					source.set(index, res);
				}
			}
				
		}
		
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
				ComprasDeImportacionController.imprimir(selected);
				return selected;
			}
		});
	}
	
	public void imprimir(Compra2 c){
		ComprasDeImportacionController.imprimir(c);
	}
	
	public void reporteComprasDepuradas() {
		ComprasDepuradasReportForm.run();
	}
	
	public void reporteComprasPendientes() {
		ComprasPendientesReportForm.run();
	}

}
