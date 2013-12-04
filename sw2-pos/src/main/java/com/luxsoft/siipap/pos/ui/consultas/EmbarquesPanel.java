package com.luxsoft.siipap.pos.ui.consultas;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.pos.POSActions;
import com.luxsoft.siipap.pos.POSRoles;

import com.luxsoft.siipap.pos.ui.forms.EmbarqueController;
import com.luxsoft.siipap.pos.ui.reports.EntregasPorChofer;
import com.luxsoft.siipap.pos.ui.reports.EntregasPorCobrar;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.services.Services;

/**
 * Consulta para el control y mantenimiento de embarques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EmbarquesPanel extends AbstractMasterDatailFilteredBrowserPanel<Embarque,Entrega>{
	
	private EmbarqueController controller;

	public EmbarquesPanel() {
		super(Embarque.class);
		
	}
	public void init(){
		super.init();
		controller=new EmbarqueController();
		
	}
	
	@Override
	protected void agregarMasterProperties(){
		addProperty("documento","chofer","log.creado","comentario","salida","regreso","sucursal");
		addLabels("Embarque","Chofer","Registrado","Comentario","Salida","Regreso","Sucursal");
		setDetailTitle("Entregas (Facturas)");
		installTextComponentMatcherEditor("Embarque", "documento");
		installTextComponentMatcherEditor("Chofer", "transporte.chofer.nombre");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		//manejarPeriodo();
		
	}
	/*
	@Override
	protected void manejarPeriodo() {
		periodo=Periodo.getPeriodo(-1);
		
	}*/
	
	private JPanel buildMainPanel(){
		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildGridPanel(buildGrid()),BorderLayout.CENTER);
		JComponent header=buildHeader();
		if(header!=null)
			panel.add(header,BorderLayout.NORTH);
		return panel;
	}
	
	protected void adjustMainGrid(final JXTable grid){
		grid.getColumnExt("Registrado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Salida").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		//grid.getColumnExt("Cerrado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Regreso").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
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
	protected void adjustDetailGrid(JXTable grid) {
		grid.getColumnExt("Retorno").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Salida").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Asignación").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Recepción").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Arribo").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		
		grid.getColumnExt("Surtido").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Facturado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		
	}
	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				"embarque.transporte.chofer.id"
				,"embarque.documento"
				,"factura.documento"
				,"nombre"	
				,"factura.log.creado"
				,"surtido"
				,"log.creado"
				,"embarque.salida"				
				,"arribo"
				,"recepcion"
				,"embarque.regreso"
				,"parcial"
				,"kilos"
				,"valor"
				,"porCobrar"
				,"recibio"
				,"surtidor"
				
				};
		String[] labels={
				"Chofer"
				,"Embarque"
				,"Docto"
				,"Cliente"
				,"Facturado"    // 1
				,"Surtido"		// 2
				,"Asignación"	// 3
				,"Salida"		// 4
				,"Arribo"		// 5
				,"Recepción"	// 6
				,"Retorno"		// 7
				,"Parcial"
				,"Kgr"
				,"Valor"
				,"Por Cobrar"
				,"Recibió"
				,"Surtidor"
				};
		return GlazedLists.tableFormat(Entrega.class, props,labels);
	}		
	
	private Action buscarAction;
	private Action cerrarAction;
	private Action cancelarCierreAction;
	private Action salidaAction;
	private Action registrarRetorno;
	private Action registrarIncidente;
	private Action agregarEntrega;
	private Action eliminarEntrega;
	private Action modificarEntrega;
	private Action actualizarEntrega;
	private Action registrarLlegadaCliente;
	private Action consultarEntrega;
	
	protected void initActions(){
		buscarAction=addAction("buscar.id","buscar", "Buscar");
		buscarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/page_find.png"));
		
		cerrarAction=addContextAction(new SinCerrarPredicate(), POSRoles.EMBARQUES.name(), "cerrarEmbarque", "Cerrar");
		cerrarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_link.png"));
		
		cancelarCierreAction=addContextAction(new CerradoPredicate(), POSRoles.EMBARQUES.name(), "cancelarCierre", "Abrir");		
		cancelarCierreAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_error.png"));
		
		salidaAction=addContextAction(new PorSalirPredicate(), POSRoles.EMBARQUES.name(), "registrarSalida", "Salida");		
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
		
		actualizarEntrega=addContextAction(new SinCerrarPredicate(), POSRoles.EMBARQUES.name(), "actualizarEntrega", "Actualizar");		
		actualizarEntrega.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/book_edit.png"));
		
		registrarLlegadaCliente=addContextAction(new SinCerrarPredicate(), POSRoles.EMBARQUES.name(), "registrarLlegadaCliente", "Llegada cliente");
		
		consultarEntrega=addContextAction(new SinCerrarPredicate(), POSRoles.EMBARQUES.name(), "consultarEntrega", "Consultar");		
		consultarEntrega.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/book_open.png"));
		
		getInsertAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_add.png"));
		getDeleteAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_delete.png"));
	}
	
	private JTextField entregaField=new JTextField(5);
	private JTextField documentField=new JTextField(5);
	private JTextField clienteField=new JTextField(5);
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Id",entregaField);
		builder.append("Docto",documentField);
		builder.append("Cliente",clienteField);
	}
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		TextFilterator entregaFilterator=GlazedLists.textFilterator("embarque.id");
		TextComponentMatcherEditor entregaEditor=new TextComponentMatcherEditor(entregaField,entregaFilterator);
		editors.add(entregaEditor);
		
		TextFilterator docFilterator=GlazedLists.textFilterator("documento");
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(documentField,docFilterator);
		editors.add(docEditor);
		
		
		TextFilterator clienteFilterator=GlazedLists.textFilterator("nombre");
		TextComponentMatcherEditor clienteEditor=new TextComponentMatcherEditor(clienteField,clienteFilterator);
		editors.add(clienteEditor);
		
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}
	
	@Override
	public Action[] getActions() {
		
		if(actions==null){
			initActions();
			actions=new Action[]{
				getLoadAction()
				,buscarAction
				,getInsertAction()
				,getDeleteAction()
				,getEditAction()
				,CommandUtils.createPrintAction(this, "reporteDeAsignacion")
				};
		}
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporteEntregasPorChofer", "Entregas por chofer"));
		procesos.add(addAction("", "reporteEntregasPorCobrar", "Embarques por cobrar (COD)"));
		return procesos;
	}
	
	private JPanel buildButtonPanel(){
		if(this.actions==null)
			initActions();
		JButton buttons[]=new JButton[]{
				new JButton(consultarEntrega)
				,new JButton(agregarEntrega)
				,new JButton(eliminarEntrega)
				,new JButton(modificarEntrega)
				//,new JButton(cerrarAction)
				//,new JButton(cancelarCierreAction)
				,new JButton(registrarLlegadaCliente)
				,new JButton(actualizarEntrega)
				
				,new JButton(salidaAction)
				,new JButton(registrarRetorno)
				,new JButton(registrarIncidente)
		};
		return ButtonBarFactory.buildRightAlignedBar(buttons);
	}
	
	@Override
	protected List<Embarque> findData() {
		Periodo periodo=Periodo.getPeriodo(-10);
		String hql="from Embarque e where " +
				//" e.regreso is null" +
				" e.fecha between ? and ? " 
				//+" and e.sucursal.id=?"
				;
		Object[] params={periodo.getFechaInicial(),periodo.getFechaFinal()
				//,Configuracion.getSucursalLocalId()
				};
		List<Embarque> data=Services.getInstance().getHibernateTemplate().find(hql,params);
		//UniqueList<Embarque> unidata=new UniqueList<Embarque>(GlazedLists.eventList(data),GlazedLists.beanPropertyComparator(Embarque.class, "id"));
		return data;
	}
	@Override
	protected Model<Embarque, Entrega> createPartidasModel() {		
		return new CollectionList.Model<Embarque, Entrega>(){
			public List<Entrega> getChildren(Embarque parent) {
				String hql="from Entrega e where e.embarque.id=?";
				return Services.getInstance().getHibernateTemplate().find(hql,parent.getId());
			}
		};
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}
	/**
	 * Necesario para evitar NPE por la etiqueta de periodo
	 */
	protected void updatePeriodoLabel(){
		//periodoLabel.setText("Periodo:" +periodo.toString());
	}
	
	public void buscar(){
		if(periodo==null)
			periodo=Periodo.getPeriodoDelMesActual();
		cambiarPeriodo();
	}
	
	/** Implementacion de acciones ***/
	
	public void insert(){
		Embarque target=controller.generarEmbarque();
		if(target!=null)
			source.add(target);
	}
	
	@Override
	protected String getDeleteMessage(Embarque bean) {		
		return "Seguro que desea eliminar el embarque :"+bean.getId()+ "\n con todo y sus entregas";
	}
	
	@Override
	public boolean doDelete(Embarque bean) {
		try {
			controller.eliminarEmbarque(bean);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(getControl()
					, ExceptionUtils.getRootCause(e)
					,"Error",JOptionPane.ERROR_MESSAGE);
			logger.error(e);
		}
		
		return true;
	}
	
	
	
	@Override
	protected Embarque doEdit(Embarque bean) {
		return controller.editarEmbarque(bean);
	}
	public void cerrarEmbarque(){
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.cerrarEmbarque(selected, grid);
			}
		});
	}
	
	public void cancelarCierre(){
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.cancelarCierreDeEmbarque(selected, grid);
			}
		});
	}
	
	public void registrarSalida(){
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.registrarSalida(selected);
			}
		});
	}
	
	public void registrarRetorno(){
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.registrarRetorno(selected);
			}
		});
		
	}
	
	public void registrarIncidente(){
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.registrarIncidente(selected);
			}
		});
	}
	
	public void agregarEntrega(){
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.agregarEntrega(selected);
			}
		});
	}
	
	public void eliminarEntrega(){
		if(detailSelectionModel.isSelectionEmpty())
			return;
		if(MessageUtils.showConfirmationMessage("Eliminar "+detailSelectionModel.getSelected().size()+" entregas", "Eliminación de entregas")){
			executeSigleSelection(new SingleSelectionHandler<Embarque>(){
				public Embarque execute(Embarque selected) {				
					return controller.elminarEntrega(selected,(Entrega[])detailSelectionModel.getSelected().toArray(new Entrega[0]));
				}
			});
		}
	}
	
	public void consultarEntrega(){
		if(detailSelectionModel.isSelectionEmpty())
			return;
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.consultarEntrega(selected,(Entrega)detailSelectionModel.getSelected().get(0));
			}
		});
	}
	
	public void modificarEntrega(){
		if(detailSelectionModel.isSelectionEmpty())
			return;
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.modificarEntrega(selected,(Entrega)detailSelectionModel.getSelected().get(0));
			}
		});
	}
	
	public void actualizarEntrega(){
		if(detailSelectionModel.isSelectionEmpty())
			return;
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.actualizarEntrega(selected,(Entrega)detailSelectionModel.getSelected().get(0));
			}
		});
	}
	
	public void registrarLlegadaCliente(){
		if(detailSelectionModel.isSelectionEmpty())
			return;
		executeSigleSelection(new SingleSelectionHandler<Embarque>(){
			public Embarque execute(Embarque selected) {				
				return controller.registrarLlegadaCliente(selected,(Entrega)detailSelectionModel.getSelected().get(0));
			}
		});
	}
	
	@Override
	protected void afterInsert(Embarque bean) {
		super.afterInsert(bean);
		this.detailGrid.packAll();
	}
	@Override
	protected void afterEdit(Embarque bean) {
		super.afterEdit(bean);
		this.detailGrid.packAll();
	}
	public void reporteDeAsignacion(){
		Embarque target=(Embarque)getSelectedObject();
		if(target!=null){
			final Map map=new HashMap();
			map.put("EMBARQUE_ID", target.getId());
			map.put("SUCURSAL", target.getSucursal());
			ReportUtils2.runReport("embarques/AsignacionDeEnvio.jasper", map);
		}
		
		
	}
	
	private class SinCerrarPredicate implements Predicate{
		public boolean evaluate(Object bean) {
			Embarque e=(Embarque)bean;
			if(e!=null)
				return e.getCerrado()==null;
			return false;
		}
	}
	
	private class CerradoPredicate implements Predicate{
		public boolean evaluate(Object bean) {
			if(bean==null) return false;
			Embarque e=(Embarque)bean;
			return e.getCerrado()!=null;
		}
	}
	
	private class EnviadoPredicate implements Predicate{
		public boolean evaluate(Object bean) {
			if(bean==null) return false;
			Embarque e=(Embarque)bean;
			if(e.getSalida()!=null)
				return e.getRegreso()==null;
			return false;
		}
	}
	
	private class PorSalirPredicate implements Predicate{
		public boolean evaluate(Object bean) {
			if(bean==null) return false;
			Embarque e=(Embarque)bean;
			return e.getSalida()==null;
		}
	}
	
	public void refreshSelection(){
		
	}
	
	protected void executeSigleSelection(SingleSelectionHandler template){
		if(getSelectedObject()!=null){
			Object selected=getSelectedObject();
			int index=source.indexOf(selected);
			if(index!=-1){				
				try {
					Object target=template.execute(selected);
					if(target!=null){
						source.set(index,target);
						selectionModel.clearSelection();
						//detailSortedList.clear();
					}
				} catch (Exception e) {
					MessageUtils.showMessage(ExceptionUtils.getRootCauseMessage(e), "Error de procesamiento");
					logger.error(e);
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private interface SingleSelectionHandler<T>{
		public T execute(T selected);
	}
	

	public void reporteEntregasPorChofer(){
		EntregasPorChofer.run();
	}
	public void reporteEntregasPorCobrar(){
		EntregasPorCobrar.run();
		
	}

}
