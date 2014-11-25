package com.luxsoft.sw3.embarques.ui.consultas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

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
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reportes.BuscadorDeVentasEnvio;
import com.luxsoft.siipap.reportes.EntregasPorChofer;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.EmbarquesRoles;
import com.luxsoft.sw3.embarque.Entrega;


/**
 * Consulta para el control y mantenimiento de embarques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EmbarquesCentralizadosPanel extends AbstractMasterDatailFilteredBrowserPanel<Embarque,Entrega>{
	
	

	public EmbarquesCentralizadosPanel() {
		super(Embarque.class);
		
	}
	public void init(){
		super.init();
	}
	
	@Override
	protected void agregarMasterProperties(){
		addProperty("sucursal","fecha","documento","chofer","valorCalculado","kilos","comentario","salida","regreso");
		addLabels("Sucursal","Fecha","Embarque","Chofer","Valor","kilos","Comentario","Salida","Regreso");
		setDetailTitle("Entregas (Facturas)");
		installTextComponentMatcherEditor("Docto", "documento");
		installTextComponentMatcherEditor("Chofer", "transporte.chofer.nombre");
		//installTextComponentMatcherEditor("Chofer Id","chofer.id");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		manejarPeriodo();
		periodo=Periodo.getPeriodo(-1);
		
	}	
	
	protected void adjustMainGrid(final JXTable grid){
		grid.getColumnExt("Salida").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		//grid.getColumnExt("Cerrado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Regreso").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				"embarque.documento"
				,"factura.documento"
				,"factura.numeroFiscal"
				,"clave"
				,"nombre"				
				,"parcial"
				,"kilos"
				,"valor"
				,"porCobrar"
				,"arribo"
				,"recepcion"
				,"recibio"
				,"comentario"
				
				};
		String[] labels={
				"Embarque"
				,"Docto"
				,"N.Fiscal"
				,"Clinete"
				,"Nombre"				
				,"Parcial"
				,"Kgr"
				,"Valor"
				,"Por Cobrar"								
				,"Arribo"
				,"Recepción"
				,"Recibió"
				,"Comentario"
				};
		return GlazedLists.tableFormat(Entrega.class, props,labels);
	}		
	
	private Action buscarAction;
	private Action agregarEntrega;
	private Action eliminarEntrega;
	private Action modificarEntrega;
	private Action actualizarEntrega;
	private Action consultarEntrega;
	
	protected void initActions(){
		buscarAction=addAction(null,"buscar", "Buscar");
		buscarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/page_find.png"));
		
		agregarEntrega=addAction(EmbarquesRoles.ContralorDeEmbarques.name(), "agregarEntrega", "Agregar");		
		agregarEntrega.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/table_add.png"));
	
		eliminarEntrega=addAction(EmbarquesRoles.ContralorDeEmbarques.name(), "eliminarEntrega", "Eliminar");		
		eliminarEntrega.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/table_delete.png"));
		
		modificarEntrega=addAction( EmbarquesRoles.ContralorDeEmbarques.name(), "modificarEntrega", "Modificar");		
		modificarEntrega.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/table_edit.png"));
		
		actualizarEntrega=addAction( EmbarquesRoles.ContralorDeEmbarques.name(), "actualizarEntrega", "Actualizar");		
		actualizarEntrega.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/book_edit.png"));
		
		consultarEntrega=addAction( EmbarquesRoles.ContralorDeEmbarques.name(), "consultarEntrega", "Consultar");		
		consultarEntrega.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/book_open.png"));
		
		getInsertAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_add.png"));
		getDeleteAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lorry_delete.png"));
	}
	
	private JTextField entregaField=new JTextField(5);
	private JTextField documentField=new JTextField(5);
	private JTextField clienteField=new JTextField(5);
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Embarque",entregaField);
		builder.append("Factura",documentField);
		builder.append("Cliente",clienteField);
	}
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		TextFilterator entregaFilterator=GlazedLists.textFilterator("embarque.documento");
		TextComponentMatcherEditor entregaEditor=new TextComponentMatcherEditor(entregaField,entregaFilterator);
		editors.add(entregaEditor);
		
		TextFilterator docFilterator=GlazedLists.textFilterator("factura.documento");
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
				//actions.add(CommandUtils.createPrintAction(this, "imprimir"));
				//,getInsertAction()
				//,getDeleteAction()
				//,getEditAction()
				//,CommandUtils.createPrintAction(this, "imprimir")
				};
		}
		return actions;
	}
	
	
	
	@Override
	protected List<Action> createProccessActions() {
		
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporteDeAsignacion", "Asignación de choferes"));
		procesos.add(addAction("", "reporteDeEntregas", "Entregas por chofer"));
		procesos.add(addAction("", "reporteVentasDeEnvio", "Facturas de Envio"));
	
		return procesos;
	}
	@Override
	protected List<Embarque> findData() {
		String hql="from Embarque e where e.regreso is null";
		List<Embarque> data=ServiceLocator2.getHibernateTemplate().find(hql);
		hql="from Embarque e where date(e.regreso) between ? and ?";
		data.addAll(ServiceLocator2.getHibernateTemplate()
			.find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()}));
		
		UniqueList<Embarque> unidata=new UniqueList<Embarque>(GlazedLists.eventList(data),GlazedLists.beanPropertyComparator(Embarque.class, "id"));
		return unidata;
		
	}
	@Override
	protected Model<Embarque, Entrega> createPartidasModel() {		
		return new CollectionList.Model<Embarque, Entrega>(){
			public List<Entrega> getChildren(Embarque parent) {
				String hql="from Entrega e where e.embarque.id=?";
				return ServiceLocator2.getHibernateTemplate().find(hql,parent.getId());
			}
		};
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}
	
	
	/**
	 * Necesario para evitar NPE por la etiqueta de periodo
	
	protected void updatePeriodoLabel(){
		periodoLabel.setText("Periodo:" +periodo.toString());
	} */
	
	public void buscar(){
		if(periodo==null)
			periodo=Periodo.getPeriodoDelMesActual();
		cambiarPeriodo();
	}
	

	/** Implementacion de acciones ***/
	
	public void insert(){
		throw new UnsupportedOperationException("EN DESARROLLO");
	}
	
	@Override
	protected String getDeleteMessage(Embarque bean) {		
		return "Seguro que desea eliminar el embarque :"+bean.getId()+ "\n con todo y sus entregas";
	}
	
	@Override
	public boolean doDelete(Embarque bean) {
		throw new UnsupportedOperationException("EN DESARROLLO");
	}
	
	
	
	@Override
	protected Embarque doEdit(Embarque bean) {
		throw new UnsupportedOperationException("EN DESARROLLO");
	}
	
	public void registrarSalida(){
		throw new UnsupportedOperationException("EN DESARROLLO");
	}
	
	public void agregarEntrega(){
		throw new UnsupportedOperationException("EN DESARROLLO");
	}
	
	public void eliminarEntrega(){
		throw new UnsupportedOperationException("EN DESARROLLO");
	}
	
	public void consultarEntrega(){
		if(detailSelectionModel.isSelectionEmpty())
			return;
		throw new UnsupportedOperationException("EN DESARROLLO");
	}
	
	public void modificarEntrega(){
		if(detailSelectionModel.isSelectionEmpty())
			return;
		throw new UnsupportedOperationException("EN DESARROLLO");
	}
	
	public void actualizarEntrega(){
		if(detailSelectionModel.isSelectionEmpty())
			return;
		throw new UnsupportedOperationException("EN DESARROLLO");
	}
	
	
	
	public void reporteDeAsignacion(){
		Embarque target=(Embarque)getSelectedObject();
		if(target!=null){
			final Map map=new HashMap();
			map.put("EMBARQUE_ID", target.getId());
			map.put("SUCURSAL", target.getSucursal());
			ReportUtils.viewReport(ReportUtils.toReportesPath("embarques/AsignacionDeEnvio.jasper"), map);
		}
	}
	
	

	public void reporteVentasDeEnvio() {
		new BuscadorDeVentasEnvio().actionPerformed(null);
		
	}
	
	
	
	public void reporteDeEntregas(){
		EntregasPorChofer.run();
	}
	
	public void refreshSelection(){
		
	}	

}
