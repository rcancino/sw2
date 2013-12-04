package com.luxsoft.siipap.swing.browser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;
import org.springframework.util.Assert;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.util.ActionLabel;

import com.luxsoft.siipap.model.Periodo;

import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * Extiend BrowserPanel para facilitar la generacion
 * del panel de filtrado. Adicionalmente aumenta las acciones
 * para generando template methods para Altas/Bajas/Cambios 
 * 
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class FilteredBrowserPanel<E> extends BrowserPanel{
	
	protected String[] properties;
	protected String[] labels;
	protected final Class<E> beanClazz;
	
	private String title;
	private boolean defaultPanel=false;
	private String securityId;
	
	public static String CLIENTE_PROPERTY_ID="FILTER_BROWSER_PANEL";

	public FilteredBrowserPanel(Class<E> beanClazz) {
		this.beanClazz = beanClazz;
		init();
	}
	
	protected void init(){
		
	}
	
	
	
	
	public boolean isDefaultPanel() {
		return defaultPanel;
	}

	public void setDefaultPanel(boolean defaultPanel) {
		this.defaultPanel = defaultPanel;
	}

	@Override
	protected TableFormat buildTableFormat() {
		Assert.notEmpty(properties);
		if(labels==null)
			labels=this.properties;
		return GlazedLists.tableFormat(beanClazz,getProperties(), getLabels());
	}
	
	/**
	 * Comodity method para asignar las propiedades 
	 * a despelgar en el grid
	 * 
	 * @param props
	 * @see buildTableFormat
	 */
	public void addProperty(String...props){
		properties=props;
	}
	
	/**
	 * Comodity method para asignar las etiquetas de las propiedades
	 * a despelgar en el grid
	 * 
	 * @param labels
	 * @see buildTableFormat
	 */
	public void addLabels(String...labels){
		this.labels=labels;
	}

	public String[] getProperties() {
		return properties;
	}
	public void setProperties(String[] properties) {
		this.properties = properties;
	}

	public String[] getLabels() {
		return labels;
	}
	public void setLabels(String[] labels) {
		this.labels = labels;
	}
	
	protected DefaultFormBuilder filterPanelBuilder;
	protected Map<String, JComponent> textEditors=new LinkedHashMap<String, JComponent>();
	
	/**
	 * Genera un panel de filtros diferente al
	 * default
	 */	
	
	public JPanel getFilterPanel() {
		if(filterPanel==null){
			filterPanel=getFilterPanelBuilder().getPanel();
			installFilters(filterPanelBuilder);
		}
		return filterPanel;
	}
	
	protected void installFilters(final DefaultFormBuilder builder){
		//Instalamos los filtros basados en textComponents
		for(Map.Entry<String, JComponent> entry:textEditors.entrySet()){
			builder.append(entry.getKey(),entry.getValue());
		}
		installCustomComponentsInFilterPanel(builder);
	}
	
	protected void installCustomComponentsInFilterPanel(DefaultFormBuilder builder){
		
	}
	
	public TextComponentMatcherEditor installTextComponentMatcherEditor(final String label,String...propertyNames){
		Assert.notEmpty(propertyNames,"Debe indicar por lo menos una propiedad de filtrado");
		final JTextField tf=new JTextField(5);
		tf.setName(label);
		final TextFilterator<E> filterator=GlazedLists.textFilterator(propertyNames);
		final TextComponentMatcherEditor<E> editor=new TextComponentMatcherEditor<E>(tf,filterator);
		matcherEditors.add(editor);
		textEditors.put(label, tf);
		return editor;
	}
	
	public TextComponentMatcherEditor installTextComponentMatcherEditor(final String label,final JTextField tf,String...propertyNames){
		Assert.notEmpty(propertyNames,"Debe indicar por lo menos una propiedad de filtrado");
		tf.setName(label);
		final TextFilterator<E> filterator=GlazedLists.textFilterator(propertyNames);
		final TextComponentMatcherEditor<E> editor=new TextComponentMatcherEditor<E>(tf,filterator);
		matcherEditors.add(editor);
		textEditors.put(label, tf);
		return editor;
	}
	
	/**
	 * Instala un text component usando un filterator personalizado
	 * 
	 * @param label
	 * @param filterator
	 */
	public TextComponentMatcherEditor<E> installTextComponentMatcherEditor(final String label,TextFilterator filterator,final JTextField tf){		
		final TextComponentMatcherEditor<E> editor=new TextComponentMatcherEditor<E>(tf,filterator);		
		matcherEditors.add(editor);
		textEditors.put(label, tf);
		return editor;
	}
	
	/**
	 * Instala un matcher editor personalizado 
	 * 
	 * @param label
	 * @param filterator
	 */
	public MatcherEditor<E> installCustomMatcherEditor(final String label,final JComponent component,MatcherEditor<E> editor){
		matcherEditors.add(editor);
		textEditors.put(label, component);
		return editor;
	}


	protected DefaultFormBuilder getFilterPanelBuilder(){
		if(filterPanelBuilder==null){
			FormLayout layout=new FormLayout("p,2dlu,f:max(60dlu;p)","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.getPanel().setOpaque(false);
			filterPanelBuilder=builder;
		}
		return filterPanelBuilder;
	}
	
	protected Action insertAction;
	protected Action deleteAction;
	protected Action editAction;
	protected Action viewAction;


	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction(),getInsertAction(),getDeleteAction(),getEditAction(),getViewAction()};
		return actions;
	}

	

	public Action getInsertAction() {
		if(insertAction==null){
			insertAction=CommandUtils.createInsertAction(this, "insert");
		}
		return insertAction;
	}
	
	public Action getSecuredInsertAction(String actionId) {
		if(insertAction==null){
			insertAction=CommandUtils.createInsertAction(this, "insert",actionId);
			if(!KernellSecurity.instance().isActionGranted(insertAction))
				insertAction=null;
		}
		return insertAction;
	}
	
	public Action getRoleBasedInsertAction(String rol) {
		if(insertAction==null){
			insertAction=CommandUtils.createInsertAction(this, "insert",rol);
			if(!KernellSecurity.instance().hasRole(rol))
				insertAction=null;
		}
		return insertAction;
	}
	
	public void setInsertAction(Action insertAction) {
		this.insertAction = insertAction;
	}

	public Action getSecuredDeleteAction(String actionId) {
		if(deleteAction==null){
			deleteAction=CommandUtils.createDeleteAction(this, "delete",actionId);
			if(!KernellSecurity.instance().isActionGranted(deleteAction))
				deleteAction=null;
		}	
		return deleteAction;
	}

	public Action getDeleteAction() {
		if(deleteAction==null)
			deleteAction=CommandUtils.createDeleteAction(this, "delete");
		return deleteAction;
	}
	public void setDeleteAction(Action deleteAction) {
		this.deleteAction = deleteAction;
	}

	public Action getSecuredEditAction(String actionId) {
		if(editAction==null){
			editAction=CommandUtils.createEditAction(this, "edit",actionId);
			if(!KernellSecurity.instance().isActionGranted(editAction))
				editAction=null;
		}
		return editAction;
	}
	
	public Action getRoleBasedEditAction(String rol) {
		if(editAction==null){
			editAction=CommandUtils.createEditAction(this, "edit",rol);
			if(!KernellSecurity.instance().hasRole(rol))
				editAction=null;
		}
		return editAction;
	}
	
	public Action getRoleBasedDeleteAction(String rol) {
		if(deleteAction==null){
			deleteAction=CommandUtils.createDeleteAction(this, "delete",rol);
			if(!KernellSecurity.instance().hasRole(rol))
				deleteAction=null;
		}
		return deleteAction;
	}
	
	public Action getEditAction() {
		if(editAction==null)
			editAction=CommandUtils.createEditAction(this, "edit");
		return editAction;
	}
	public void setEditAction(Action editAction) {
		this.editAction = editAction;
	}


	public Action getViewAction() {
		if(viewAction==null){
			viewAction=CommandUtils.createViewAction(this, "view");
		}
		return viewAction;
	}
	public void setViewAction(Action viewAction) {
		this.viewAction = viewAction;
	}
	
	public void insert(){
		E bean=doInsert();
		if(bean!=null){
			source.add(bean);
			afterInsert(bean);
		}
	}
	
	protected void afterInsert(E bean){
		grid.packAll();
	}
	
	protected E doInsert(){
		if(logger.isDebugEnabled()){
			logger.debug("Inserting new bean..."+beanClazz.getName());
		}
		return null;
	}
	
	public void delete(){
		E bean=(E)getSelectedObject();
		if(bean!=null){
			if(MessageUtils.showConfirmationMessage(getDeleteMessage(bean),"Borrar"))
				try {
					if(doDelete(bean)){
						source.remove(bean);
					}
				} catch (Exception e) {
					MessageUtils.showError("Error al eliminar registro", e);
				}
		}
	}
	
	protected String getDeleteMessage(E bean){
		return "Seguro que desea eliminar el registro:\n "+bean;
	}
	
	public boolean doDelete(E bean){
		return false;
	}
	
	public void edit(){
		E origen=(E)super.getSelectedObject();
		if(origen!=null){
			E bean=doEdit(origen);
			if(bean!=null){
				int index=source.indexOf(origen);
				if(index!=-1){
					//source.remove(origen);
					source.set(index,bean);
					setSelected(bean);
					afterEdit(bean);
				}else
					logger.info("No se localizo en el eventList el bean: "+origen);
					
			}
		}
	}
	
	protected void afterEdit(final E bean){
		grid.packAll();
	}
	
	public void setSelected(E bean){
		int index=sortedSource.indexOf(bean);
		if(index!=-1){
			selectionModel.clearSelection();
			selectionModel.setSelectionInterval(index, index);
		}
	}
	
	protected E doEdit(final E bean){
		if(logger.isDebugEnabled()){
			logger.debug("Editing bean: "+bean);
		}
		return null;
	}
	
	public void view(){
		select();
	}
	
	protected List<E> findData(){
		return ServiceLocator2.getUniversalDao().getAll(beanClazz);
	}
	
	public void close(){
		String id=getTitle()!=null?getTitle():getClass().getName();
		logger.info("Cerrando browser: "+id+" (lipiando glazedlist...)");
		source.clear();
	}
	
	protected List<Action> proccessActions;
	
	/**
	 * Regresa una lista de las acciones catalogadas como procesos
	 * 
	 * @return
	 */
	public List<Action> getProccessActions(){
		if(proccessActions==null){
			proccessActions=createProccessActions();
		}
		return proccessActions; 
	}
	
	/**
	 * Template method para instalar las acciones catalogadas como procesos
	 *  
	 * @return
	 */
	protected List<Action> createProccessActions(){
		return new ArrayList<Action>();
	}
	
	
	
	/**
	 * 
	 */
	public void open(){
		
	}
	
	/**
	 * Utiliti comparator para crear un comparador basado en el campo Id del
	 *  bean base.
	 * 
	 * @return
	 */
	protected Comparator createIdComparator(){
		return GlazedLists.reverseComparator(
				GlazedLists.beanPropertyComparator(this.beanClazz, "id")
				);
	}
	
	protected Periodo periodo;
	protected ActionLabel periodoLabel;
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodoDelMesActual();
	}
	
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){
			manejarPeriodo();
			periodoLabel=new ActionLabel("Periodo: "+periodo.toString());
			periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	public Periodo getPeriodo(){
		return periodo;
	}
	public void cambiarPeriodo(){
		ValueHolder holder=new ValueHolder(periodo);
		AbstractDialog dialog=Binder.createPeriodoSelector(holder);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=(Periodo)holder.getValue();
			
			nuevoPeriodo(periodo);
			updatePeriodoLabel();
		}
	}
	
	protected void updatePeriodoLabel(){
		periodoLabel.setText("Per:" +periodo.toString());
	}
	
	protected void nuevoPeriodo(Periodo p){
		load();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Action addAction(String actionId,String method,String label){
		Action a=new DispatchingAction(this,method);
		configAction(a, actionId,label);
		if(StringUtils.isBlank(actionId))
			return a;  //No requiere seguridad
		if(KernellSecurity.instance().isActionGranted(a))
			return a;
		return null;
	}
	
	
	public Action addRoleBasedAction(final String role,String method,String label){
		Assert.hasLength(role, "Debe especificar el rol de la accion");
		Action a=new DispatchingAction(this,method){
			public void actionPerformed(ActionEvent e){
				if(KernellSecurity.instance().hasRole(role)){
					super.actionPerformed(e);
				}else{
					MessageUtils.showMessage("No tiene acceso a esta operación", "Mantenimiento de clientes de credito");
				}
			}
		};
		configAction(a, role,label);
		return a;
	}
	
	
	/** Implementacion de ejecuciones usando Template pattern similar a Spring JdbcTemplate***/
	
	protected void execute(final ExecutionSelectionTemplate<E> template){
		if(!getSelected().isEmpty()){
			try {
				doExecute(template);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(getControl(), e.getMessage(),"Error en tarea",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}
	
	
	
	protected void doExecute(final ExecutionSelectionTemplate<E> template){
		
	}
	
	/**
	 * Comodity para confirmar una accion
	 * 
	 * @param msg
	 * @return
	 */
	public boolean confirmar(String msg){
		int res=JOptionPane.showConfirmDialog(getControl(), msg, "Ejecutar?",JOptionPane.OK_CANCEL_OPTION);
		return res==JOptionPane.OK_OPTION;
	}

	/**
	 * Template para ejeccuion de tareas sobre una seleccion
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	public static interface ExecutionSelectionTemplate<E>{
		
		/**
		 * Ejecuta una tarea sobre la seleccion
		 * 
		 * @param selected
		 * @return
		 */
		public List<E> execute(final List<E> selected);
		
	}
	
	public Action addContextAction(Predicate p,String actionId,String method,String label){
		ContextAction a=new ContextAction(p,this,method);		
		configAction(a, actionId,label);
		selectionModel.addListSelectionListener(a);
		if(StringUtils.isBlank(actionId))
			return a;
		if(KernellSecurity.instance().isActionGranted(a))
			return a;
		return null;
	}
	
	/**
	 * Genera una accion
	 * 
	 * @param p Predicate para disponibilidad de la accion
	 * @param role Role de disponibilidad
	 * @param target Proxy encargado de la ejecuccion de la accion
	 * @param method Metodo del proxy a ejecutar
	 * @param label Etiqueta por default
	 * @return
	 */
	public Action addRoleBasedContextAction(Predicate p,String role,Object target,String method,String label){
		return addRoleBasedContextAction(p, role, target, method, label, selectionModel);
	}
	
	/**
	 * 
	 * @param p
	 * @param role
	 * @param target
	 * @param method
	 * @param label
	 * @param selection  El selection model del que depende la activacion de la accion
	 * @return
	 */
	public Action addRoleBasedContextAction(Predicate p,String role,Object target,String method,String label,EventSelectionModel selection){
		if(target==null)
			target=this;
		Action a;
		if(p!=null){
			ContextAction cxa=new ContextAction(p,target,method);
			selection.addListSelectionListener(cxa);
			a=cxa;
		}
		else
			a=new DispatchingAction(target,method);
		configAction(a, role,label);
		if(KernellSecurity.instance().hasRole(role))
			return a;
		return null;
	}

	/**
	 * Extension de {@link DispatchingAction} para detectar la seleccion 
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	public class ContextAction extends DispatchingAction implements ListSelectionListener{
		
		private final Predicate predicate;

		public ContextAction(Predicate predicate,Object delegate, String methodName) {
			super( delegate, methodName);	
			this.predicate=predicate;
			setEnabled(false);
		}

		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()){
				setEnabled(predicate.evaluate(getSelectedObject()));
			}
		}
		
		
		
	}
	
	public static interface Predicate{
		public boolean evaluate(Object bean);
	}
	
	
	public Action addMultipleContextAction(MultiplePredicate p,String actionId,String method,String label){
		MultipleSelectionAction a=new MultipleSelectionAction(p,this,method);
		configAction(a, actionId,label);
		selectionModel.addListSelectionListener(a);
		if(KernellSecurity.instance().isActionGranted(a))
			return a;
		return null;
	}
	
	
	/**
	 * Extension de {@link DispatchingAction} para detectar la seleccion 
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	public class MultipleSelectionAction extends DispatchingAction implements ListSelectionListener{		
		
		private final MultiplePredicate predicate;

		public MultipleSelectionAction(MultiplePredicate predicate,Object delegate, String methodName) {
			super( delegate, methodName);
			this.predicate=predicate;
			setEnabled(false);
		}

		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()){
				setEnabled(predicate.evaluate(getSelected()));
			}
		}
	}
	
	public static interface MultiplePredicate{
		public boolean evaluate(List data);
	}
	

	public static class NotNullSelectionPredicate implements Predicate{

		public boolean evaluate(Object bean) {
			return bean!=null;
		}
		
	}
	
	public static class SelectionPredicate implements Predicate{

		public boolean evaluate(Object bean) {
			return bean!=null;
		}		
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}
	
	
	public void executeSigleSelection(SingleSelectionHandler template){
		if(getSelectedObject()!=null){
			Object selected=getSelectedObject();
			int index=source.indexOf(selected);
			if(index!=-1){				
				try {
					Object target=template.execute(selected);
					if(target!=null){
						source.set(index,target);
						setSelected((E) target);
					}
					afterSingleSelectionTask();
				} catch (Exception e) {
					MessageUtils.showMessage(ExceptionUtils.getRootCauseMessage(e), "Error de procesamiento");
					logger.error(e);
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Post single selection task 
	 */
	public void afterSingleSelectionTask(){
		getGrid().packAll();
	}
	
	
	public  static interface SingleSelectionHandler<T>{
		public T execute(T selected);
	}
	
	
	private View dockingView;
	
	public View getView(){
		if(dockingView==null){
			/*
			View[] views={
					new View("", null, getControl())
					,new View("Filtros", null, getFilterPanel())
					,new View("Acciones", null, getActionsToolBar())
			};
			
			ViewMap vm=new ViewMap(views);
			RootWindow window=new RootWindow(vm);
			
			window.setWindow(new SplitWindow(false, 0.16425121f, 
				    views[2],
				    new SplitWindow(true, 0.21284756f,views[1],views[0])));
			*/
			/*JPanel panel=new JPanel();
			panel.add(getActionsToolBar(),BorderLayout.NORTH);
			panel.add(getControl(),BorderLayout.CENTER);
			panel.add(getFilterDockPanel(),BorderLayout.SOUTH);
			*/
			
			dockingView=new View(getTitle(),null,getControl());
			
			
		}
		return dockingView;
	}
	
	
	
	
	
	
}
