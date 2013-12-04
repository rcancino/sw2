package com.luxsoft.siipap.pos.ui.consultas;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.reports.ComprasDepuradasReportForm;
import com.luxsoft.siipap.pos.ui.reports.ComprasPendientesReportForm;
import com.luxsoft.siipap.pos.ui.reports.ReporteDeAlcancesForm;
import com.luxsoft.siipap.pos.ui.reports.ReporteMesesDeInventario;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.ui.forms.CompraController2;
import com.luxsoft.sw3.ui.forms.RecepcionDeCompraController;
import com.luxsoft.sw3.ui.forms.RecepcionDeCompraForm;

/**
 * Consulta para el control y mantenimiento de embarques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ComprasPanel extends AbstractMasterDatailFilteredBrowserPanel<Compra2,CompraUnitaria>{
	
	private CompraController2 controller;

	public ComprasPanel() {
		super(Compra2.class);
		
	}
	public void init(){
		super.init();
		controller=new CompraController2();
	}
	
	@Override
	protected void agregarMasterProperties(){
		addProperty("folio","sucursal.nombre","clave","nombre","fecha","moneda","tc","cierre","depuracion","importacion","consolidada","comentario");
		addLabels("Folio","Sucursal","Prov","Nombre","Fecha","Mon","TC","Cierre","Depuracion","Imp","Cons","Comentario");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Proveedor", "clave","nombre");		
		installTextComponentMatcherEditor("Folio", "folio");
		manejarPeriodo();
		setDetailTitle("Ordenes de Compra");
	}
	
	private JPanel buildMainPanel(){
		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildGridPanel(buildGrid()),BorderLayout.CENTER);
		JComponent header=buildHeader();
		if(header!=null)
			panel.add(header,BorderLayout.NORTH);
		return panel;
	}
	
	protected void adjustMainGrid(final JXTable grid){
		grid.getColumnExt("Depuracion").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Cierre").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
	}
	
	
	@Override
	protected JComponent buildContent() {
		
		JPanel topPanel=new JPanel(new BorderLayout());
		JComponent parent=buildMainPanel();
		topPanel.add(parent,BorderLayout.CENTER);
		topPanel.add(buildButtonPanel(),BorderLayout.SOUTH);
				
		
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.4);
		sp.setTopComponent(topPanel);
		sp.setBottomComponent(buildDeailPanel());
		
		return sp;
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"compra.folio","sucursalNombre","clave","descripcion","unidad","solicitado","recibido","depurado","pendiente","depuracion","comentario"};
		String[] labels={"Folio","Sucursal","Prod","Descrpción","U","Solicitado","Recibido","Depurado","Pendiente","Depuracion","Comentario"};
		return GlazedLists.tableFormat(CompraUnitaria.class, props,labels);
	}		
	
	private Action cancelarAction;
	private Action buscarAction;
	private Action cerrarAction;	
	private Action cancelarCierreAction;
	private Action depurarAction;
	private Action printAction;
	private Action mailAction;
	
	
	
	protected void initActions(){
		buscarAction=addAction("buscar.id","buscar", "Buscar");
		buscarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/page_find.png"));
		
		cerrarAction=addContextAction(new SinCerrarPredicate(), POSRoles.ADMINISTRADOR_DE_COMRAS.name(), "cerrar", "Cerrar");
		cerrarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lock.png"));
		
		cancelarCierreAction=addContextAction(new CerradoPredicate(), POSRoles.ADMINISTRADOR_DE_COMRAS.name(), "cancelarCierre", "Abrir");		
		cancelarCierreAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_error.png"));		
		
		depurarAction=addContextAction(new NoDepuradoPredicate(), POSRoles.ADMINISTRADOR_DE_COMRAS.name(), "depurar", "Depurar");
		
		cancelarAction=addContextAction(new SinCerrarPredicate(), POSRoles.ADMINISTRADOR_DE_COMRAS.name(), "cancelar", "Cancelar");
		
		printAction=addContextAction(new NotNullSelectionPredicate(), "", "imprimir", "Imprimir");
		printAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/file/printview_tsk.gif"));
		
		mailAction=addContextAction(new NotNullSelectionPredicate(), "", "mandarPorEmail", "Imprimir");
		mailAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/email_go.png"));
		
		getInsertAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/basket_add.png"));
		getDeleteAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/basket_delete.png"));
		//getEditAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/basket_edit.png"));
		
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			initActions();
			actions=new Action[]{
				getLoadAction()
				,buscarAction
				,getViewAction()
				,getInsertAction()
				,getEditAction()				
				//,getDeleteAction()				
				//,cancelarAction	
				,addRoleBasedContextAction(null,POSRoles.ADMINISTRADOR_DE_COMRAS.name(), this, "recepcion", "Recepción de Comrpas")
				};
		}
		return actions;
	}
	
	
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporteDeAlcances", "Alcances"));
		procesos.add(addAction("", "reporteMesesDeInventario", "Meses de Inv."));
		procesos.add(addAction("","reporteComprasDepuradas", "Compras Depuradas"));
		procesos.add(addAction("", "reporteComprasPendientes", "Compras Pendientes"));
		return procesos;
 		
	}
	
	public void reporteComprasDepuradas() {
		ComprasDepuradasReportForm.run();
	}
	
	public void reporteComprasPendientes() {
		ComprasPendientesReportForm.run();
	}
	
	private JPanel buildButtonPanel(){
		if(this.actions==null)
			initActions();
		JButton buttons[]=new JButton[]{				
				new JButton(cerrarAction)
				,new JButton(cancelarCierreAction)
				,new JButton(depurarAction)	
				,new JButton(printAction)
				,new JButton(addAction("", "copiar", "Copiar"))
				,new JButton(addAction("", "consolidar", "Consolidar"))
				,new JButton(addAction("", "automática", "Automática"))
				,new JButton(addAction("", "sobrePedido", "Con Pedido"))
				,new JButton(addAction("", "plantilla", "Pantilla"))
				,new JButton(mailAction)
				,new JButton(printAction)
		};
		return ButtonBarFactory.buildRightAlignedBar(buttons);
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
	@Override
	protected List<Compra2> findData() {
		if(periodo==null){
			return controller.buscarComprasPendientes();
		}else{
			return controller.buscarCompras(periodo);
		}
	}*/
	
	@Override
	protected List<Compra2> findData() {
		if(pendientesBox.isSelected())
			return controller.buscarComprasPendientes();
		else
			return controller.buscarCompras(periodo);
	}
	
	
	
	@Override
	protected void afterLoad() {		
		super.afterLoad();
		updatePeriodoLabel();
	}
	
	@Override
	protected Model<Compra2, CompraUnitaria> createPartidasModel() {		
		return new CollectionList.Model<Compra2, CompraUnitaria>(){
			public List<CompraUnitaria> getChildren(Compra2 compra) {
				return controller.buscarPartidas(compra);
			}
		};
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}
	/**
	 * Necesario para evitar NPE por la etiqueta de periodo
	 */
	//protected void updatePeriodoLabel(){}
	
	public void buscar(){
		if(periodo==null)
			periodo=Periodo.getPeriodoDelMesActual();
		cambiarPeriodo();
	}
	
	/** Implementacion de acciones ***/
	
	public void insert(){
		Compra2 res=controller.generarCompra();
		if(res!=null){
			source.add(res);
			int index=source.indexOf(res);
			selectionModel.setSelectionInterval(index, index);
			controller.imprimirCompra(res);
		}		
	}
	
	@Override
	public void edit() {
		executeSigleSelection(new SingleSelectionHandler<Compra2>(){
			public Compra2 execute(Compra2 selected) {
				return controller.modificarCompra(selected);
			}
		});
		
	}
	
	@Override
	public boolean doDelete(Compra2 bean) {
		controller.eleiminarCompra(bean);
		return true;
	}
	
	/*
	public void cancelar(){
		executeSigleSelection(new SingleSelectionHandler<Compra2>(){
			public Compra2 execute(Compra2 selected) {
				return controller.cancelarCompra(selected);
			}
		});
	}
	*/
	
	public void cerrar(){
		executeSigleSelection(new SingleSelectionHandler<Compra2>(){
			public Compra2 execute(Compra2 selected) {
				return controller.cerrarCompra(selected);
			}
		});
	}
	
	public void cancelarCierre(){
		executeSigleSelection(new SingleSelectionHandler<Compra2>(){
			public Compra2 execute(Compra2 selected) {
				return controller.cancelarCierreDeCompra(selected);
			}
		});
	}
	
	public void depurar(){
		executeSigleSelection(new SingleSelectionHandler<Compra2>(){
			public Compra2 execute(Compra2 selected) {
				return controller.depurarCompra(selected);
			}
		});
	}
	
	public void imprimir(){
		executeSigleSelection(new SingleSelectionHandler<Compra2>(){
			public Compra2 execute(Compra2 selected) {
				controller.imprimirCompra(selected);
				return selected;
			}
		});
	}
	
	public void recepcion(){
		RecepcionDeCompraController controller=new RecepcionDeCompraController();
		RecepcionDeCompraForm form=new RecepcionDeCompraForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			//Services.getInstance().getComprasManager().registrarRecepcion(controller.getRecepcion());
			controller.persist();
		}
	}
	
	public void reporteDeAlcances(){
		ReporteDeAlcancesForm.run();
	}
	
	public void reporteMesesDeInventario(){
		ReporteMesesDeInventario.run();
	}
	
	private class SinCerrarPredicate implements Predicate{
		public boolean evaluate(Object bean) {
			if(bean==null) return false;
			Compra2 e=(Compra2)bean;
			return e.getCierre()==null;
		}
	}
	
	private class CerradoPredicate implements Predicate{
		public boolean evaluate(Object bean) {
			if(bean==null) return false;
			Compra2 e=(Compra2)bean;
			return e.getCierre()!=null;
		}
	}
	
	private class NoDepuradoPredicate implements Predicate{
		public boolean evaluate(Object bean) {
			if(bean==null) return false;
			Compra2 e=(Compra2)bean;
			return e.getDepuracion()==null;
		}
	}
	
	
	
	

}
