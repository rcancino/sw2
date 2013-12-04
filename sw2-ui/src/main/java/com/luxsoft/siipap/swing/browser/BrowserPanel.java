package com.luxsoft.siipap.swing.browser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.ThreadedMatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.uifextras.util.UIFactory;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel.Predicate;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * 
 * Este control es basicamente un panel con un {@link JXTable} respaldado por un {@link EventTableModel}
 * de GlazedList para presentar instancias de beans.
 * 
 * @author Ruben Cancino
 *
 */
public abstract class BrowserPanel<E> extends AbstractControl{
	
	protected EventList<E> source;
	protected SortedList<E> sortedSource;
	protected EventSelectionModel<E> selectionModel;
	protected EventList<MatcherEditor<E>> matcherEditors=new BasicEventList<MatcherEditor<E>>();
	
	protected JXTable grid;
	
	protected JPanel filterPanel;
	protected JTextField filterField1;
	protected Document filterDocument1=new PlainDocument();
	private Comparator<E> defaultComparator;
	 
	
	/**
	 * Inicializacion de GlazedList
	 * 
	 */
	protected void initGlazedList(){
		if(source==null)
			source=GlazedLists.threadSafeList(getSourceEventList());
		sortedSource=new SortedList<E>(getFilteredList(source),getDefaultComparator());
		selectionModel=new EventSelectionModel<E>(sortedSource);
	}
	
	protected EventList<E> getSourceEventList(){
		return new BasicEventList<E>();
	}
	
	/**
	 * Regresa una lista filtrada mediante un {@link CompositeMatcherEditor}
	 * 
	 * @param list
	 * @return
	 */
	protected EventList<E> getFilteredList(final EventList<E> list){
		
		final CompositeMatcherEditor<E> editor=new CompositeMatcherEditor<E>(matcherEditors);
		installEditors(matcherEditors);
		final FilterList<E> filterList=new FilterList<E>(list,new ThreadedMatcherEditor<E>(editor));
		return filterList;
	}
	
	/**
	 * Template method para instalar {@link MatcherEditor}'s para la lista filtrada
	 * Por default el unico editor instalado es uno que permite buscar con el metodo toString
	 * del bean
	 * 
	 * @param editors
	 */
	@SuppressWarnings("unchecked")
	protected void installEditors(final EventList<MatcherEditor<E>> editors){		
		final TextFilterator<E> filterator=GlazedLists.toStringTextFilterator();
		TextComponentMatcherEditor<E> editor=new TextComponentMatcherEditor<E>(filterDocument1,filterator);
		editors.add(editor);
	}
	
	@SuppressWarnings("unchecked")
	protected JXTable buildGrid(){
		initGlazedList();
		grid=ComponentUtils.getStandardTable();
		EventTableModel<E> tm=new EventTableModel<E>(sortedSource,buildTableFormat());
		grid.setModel(tm);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addEnterAction(grid, getSelectAction());
		//ComponentUtils.addF2Action(grid, getLoadAction());
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					select();
			}			
		});
		ComponentUtils.addAction(grid, new DispatchingAction(this,"clearSelection"), KeyStroke.getKeyStroke("alt  C"));
		//chooser=new TableComparatorChooser(grid,sortedSource,true);
		installChooser(grid, sortedSource);
		adjustMainGrid(grid);
		return grid;		
	}
	
	protected void afterGridCreated(){
		
	}
	
	public void clearSelection(){
		selectionModel.clearSelection();
	}
	
	protected TableComparatorChooser chooser;
	
	protected void installChooser(JXTable grid,SortedList sortedSource){
		chooser=new TableComparatorChooser(grid,sortedSource,true);
	}
	
	/**
	 * 
	 */
	public void open(){
		
	}
	
	protected JScrollPane buildGridPanel(final JXTable grid){
		
		//final JScrollPane sp=new JScrollPane(grid);
		//return sp;
		return UIFactory.createStrippedScrollPane(grid);
	}
	
	/**
	 * Template method para ajustar las columnas del grid
	 * 
	 * @param grid
	 */
	protected void adjustMainGrid(final JXTable grid){		
	}	
	
	@Override
	protected JComponent buildContent() {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildGridPanel(buildGrid()),BorderLayout.CENTER);
		afterGridCreated();
		JComponent header=buildHeader();
		if(header!=null)
			panel.add(header,BorderLayout.NORTH);
		return panel;
	}
	
	protected JComponent buildHeader(){
		return null;
	}

	/**
	 * Metodo detonado cuando se selecciona un registro en el grid
	 * @see doSelect(E bean)
	 */
	public void select(){
		E selected=getSelectedObject();
		if(selected!=null)
			doSelect(selected);
	}
	
	/**
	 * Template method para facilitar el comportamiento cuando el usuario da doble click o enter en algun
	 * registro del grid. Este metodo es adecuado para ser implementado por las subclases 
	 * 
	 * @param bean
	 */
	protected void doSelect(E bean){
		if(logger.isDebugEnabled()){
			logger.debug("Seleccion; "+bean);
		}
	}
	
	public boolean isSelectionEmpty(){
		return selectionModel.isSelectionEmpty();
	}
	
	protected EventList<E> getSelected(){
		return selectionModel.getSelected();
	}
	
	public E getSelectedObject(){
		if(!selectionModel.isSelectionEmpty()){
			return getSelected().get(0);
		}
		return null;
	}	
	
	public EventList<E> getSource() {
		return source;
	}
	
	public EventList<E> getFilteredSource(){
		return sortedSource;
	}

	public void setSource(EventList<E> source) {
		Assert.isNull(this.source,"La Lista fuente ya fue asignada, es inmutable");
		this.source = source;
	}

	/*
	protected JPanel getFilterPanel(){		
		if(filterPanel==null){
			filterPanel=buildFilterPanel();
		}
		return filterPanel;
	}
	*/
	/**
	 * La implementacion por default regresa un input field con un filterato
	 * que utiliza el metodo toString de los beans en el grid
	 * para realizar la busqueda
	 *  
	 * @return
	 */
	protected JPanel buildFilterPanel(){
		filterField1=new JTextField();
		filterField1.setDocument(filterDocument1);
		return (JPanel)ComponentUtils.buildTextFilterPanel(filterField1);
		
	}
	
	protected abstract TableFormat<E> buildTableFormat();
	
	
	protected Action selectAction;
	protected Action loadAction;
	
	//** Acciones **/
	
	public Action getSelectAction(){
		return selectAction;
	}
	public Action getLoadAction(){
		if(loadAction==null)
			loadAction=CommandUtils.createLoadAction(this, "load");
		return loadAction;
	}
	
	protected Action[] actions;
	
	public Action[] getActions(){
		if(actions==null)
			actions=new Action[]{getLoadAction()};
		return actions;
	}
	
	/**
	 * Componentes a ser colocados en el task panel de operaciones
	 * @return
	 */
	public JComponent[] getOperacionesComponents(){
		return new JComponent[0];
	}

	public void setActions(Action[] actions) {
		this.actions = actions;
	}
	
	/**
	 * Otra forma de asignar acciones
	 * 
	 * @param action
	 */
	public void addActions(Action...action){
		this.actions=action;
	}
	
	protected int registrosBeforeLoad=0;
	
	protected void beforeLoad(){
		registrosBeforeLoad=source.size();
		getLoadAction().setEnabled(false);
	}
	
	public void load(){
		beforeLoad();
		SwingWorker<List<E>, String> worker=new SwingWorker<List<E>, String>(){
			
			protected List<E> doInBackground() throws Exception {
				publish("Cargando datos....");
				return findData();
			}
			protected void done(){
				
				List<E> data;
				try {
					data = get();
					dataLoaded(data);
				} catch (InterruptedException e) {
					e.printStackTrace();
					MessageUtils.showError("Error", e);
				} catch (ExecutionException e) {
					e.printStackTrace();
					MessageUtils.showError("Error", e);
					
				}finally{
					getLoadAction().setEnabled(true);
					sureAfterLoad();
				}
			}
			
			
		};
		executeLoadWorker(worker);
	}
	
	/**
	 * Template method para ejecutar despues de un load sin importar si hay errores
	 */
	protected void sureAfterLoad(){
		
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		worker.execute();
		//TaskUtils.executeSwingWorker(worker);
	}
	
	/**
	 * Called safetly from the Load's method SwingWorker
	 * It is executed in the EDT
	 * 
	 * @param data
	 */
	protected void dataLoaded(final List<E> data){		
		source.clear();
		source.addAll(data);
		if(grid!=null)
			grid.packAll();
		afterLoad();
	}
	
	protected int registrosAfterLoad;
	
	/**
	 * Template method para hacer algo en el EDT despues de cargar los datos
	 * 
	 */
	protected void afterLoad(){
		registrosAfterLoad=source.size();
	}
	
	protected List<E> findData(){
		return new ArrayList<E>();
	}
	
	public JXTable getGrid(){
		return grid;
	}

	public Comparator<E> getDefaultComparator() {
		return defaultComparator;
	}

	public void setDefaultComparator(Comparator<E> defaultComparator) {
		this.defaultComparator = defaultComparator;
	}
	

	protected void configAction(final Action a,String id,String label){
		CommandUtils.configAction(a, id, null);
		a.putValue(Action.NAME, label);
		a.putValue("ID", id);
		
	}
	
	protected void configAction(final Action a,String id){
		CommandUtils.configAction(a, id, null);
	}
	
	
	/**
	 * Prototipo para facilitar las acciones de sible selection
	 * 
	 * TODO Falla al tratar de ejecutar el metodo pasando parametros
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	public static class SingleSelectioAction extends AbstractAction implements ListSelectionListener{
		
		private final Object delegate;
		private final String methodName;
		private Predicate predicate;
		private WeakReference<EventSelectionModel> selectionRef;
		private Logger logger=Logger.getLogger(getClass());
		

		public SingleSelectioAction(Object delegate, String methodName,final EventSelectionModel sm) {
			this.delegate=delegate;
			this.methodName=methodName;
			selectionRef=new WeakReference<EventSelectionModel>(sm);
		}
		
		public void actionPerformed(ActionEvent e) {
			dispatchAction(methodName);
		}
		
		protected EventSelectionModel getSelectionModel(){
			if(selectionRef.get()!=null)
				return selectionRef.get();
			return null;
		}
		
		protected boolean isSelectionEmpty(){
			if(getSelectionModel()!=null)
				return getSelectionModel().isSelectionEmpty();
			return false;
		}
		
		protected Object getSelectedObject(){
			if(getSelectionModel()!=null)
				return getSelectionModel().getSelected().get(0);
			return null;
		}
		
		protected void dispatchAction(String name){
			if(isSelectionEmpty()) 
				return;		
			
			int index=getSelectionModel().getMinSelectionIndex();
			try {					
				Method m=delegate.getClass().getMethod(name);
				Object res= m.invoke(delegate);
				getSelectionModel().getSelected().add(index, res);
			} catch (NoSuchMethodException me){			
				String msg="El Delegado no soporta la accion : " + name
				+"\n[Modelo|Plugin]: "+delegate.getClass().getName()
				+"\nEs posible que esta proceso este en producción";
				logger.error(msg,me);
				JOptionPane.showMessageDialog(
						null,
						msg,
						"[Model|Plugin]: "+delegate.toString(),
						JOptionPane.WARNING_MESSAGE);
			}catch(Exception ex){
				MessageUtils.showMessage(ExceptionUtils.getRootCauseMessage(ex), "Error de procesamiento");
				logger.error(ex);
			}			
		}
		
		
		
		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()){
				if(predicate!=null)
					setEnabled(predicate.evaluate(getSelectedObject()));
				else{
					setEnabled(isSelectionEmpty());
				}
			}
		}

		public Predicate getPredicate() {
			return predicate;
		}

		public void setPredicate(Predicate predicate) {
			this.predicate = predicate;
		}
		
		
		
	}

}
