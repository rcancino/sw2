package com.luxsoft.siipap.swx.binding;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.jdesktop.swingx.JXTable;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
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

import com.jgoodies.binding.value.ValueHolder;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * 
 * Este control es basicamente un panel con un {@link JXTable} respaldado por un {@link EventTableModel}
 * de GlazedList para presentar instancias de beans.
 * 
 * @author Ruben Cancino
 *
 */
public class BrowserLookup<E> extends AbstractControl{
	
	private final Class<E> beanClass;
	protected EventList<E> source;
	protected SortedList<E> sortedSource;
	protected EventSelectionModel<E> selectionModel;
	protected EventList<MatcherEditor<E>> matcherEditors=new BasicEventList<MatcherEditor<E>>();
	
	protected JXTable grid;
	
	protected JPanel filterPanel;
	protected JTextField filterField1;
	protected Document filterDocument1=new PlainDocument();
	protected final WeakReference<ValueHolder> holderRef;
	 
	private String[] properties;
	private String[] labels;
	
	
	private Action selectAction;
	private Action loadAction;
	private boolean instalarDefaultEditor=false;
	
	public BrowserLookup(final Class<E> clazz) {
		this(clazz,new ValueHolder(null,true));
	}
	
	public BrowserLookup(final Class<E> clazz,final ValueHolder holder) {
		holderRef=new WeakReference<ValueHolder>(holder);
		this.beanClass=clazz;
	}

	/**
	 * Inicializacion de GlazedList
	 * 
	 */
	protected void initGlazedList(){
		if(source==null)
			source=GlazedLists.threadSafeList(new BasicEventList<E>());
		sortedSource=new SortedList<E>(getFilteredList(source),null);
		selectionModel=new EventSelectionModel<E>(sortedSource);
		selectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
	}
	
	/**
	 * Regresa una lista filtrada mediante un {@link CompositeMatcherEditor}
	 * 
	 * @param list
	 * @return
	 */
	protected EventList<E> getFilteredList(final EventList<E> list){		
		final CompositeMatcherEditor<E> editor=new CompositeMatcherEditor<E>(matcherEditors);		
		if(isInstalarDefaultEditor())
			installDefaultEditor(matcherEditors);
		installEditors(matcherEditors);		
		final FilterList<E> filterList=new FilterList<E>(list,new ThreadedMatcherEditor<E>(editor));
		return filterList;
	}
	
	/**
	 * Template method para instalar {@link MatcherEditor}'s para la lista filtrada
	 * 
	 * 
	 * @param editors
	 */
	@SuppressWarnings("unchecked")
	protected void installEditors(final EventList<MatcherEditor<E>> editors){		
		
	}
	
	/**
	 * Template method para instalar el editor por default
	 * 
	 * @param editors
	 */
	protected void installDefaultEditor(final EventList<MatcherEditor<E>> editors){
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
		ComponentUtils.addF2Action(grid, getLoadAction());
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					select();
			}			
		});
		new TableComparatorChooser(grid,sortedSource,true);
		return grid;		
	}
	
	
	
	@Override
	protected final JComponent buildContent() {
		final JPanel panel=new JPanel(new BorderLayout());
		final JScrollPane sp=new JScrollPane(buildGrid());
		panel.add(sp,BorderLayout.CENTER);
		return panel;
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
	
	protected EventList<E> getSelected(){
		return selectionModel.getSelected();
	}
	
	protected E getSelectedObject(){
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
	
	protected TableFormat<E> buildTableFormat(){
		return GlazedLists.tableFormat(beanClass, getProperties(), getLabels());
	}
	
	
	
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

	public void setActions(Action[] actions) {
		this.actions = actions;
	}
	
	public void load(){
		SwingWorker<List<E>, String> worker=new SwingWorker<List<E>, String>(){
			
			protected List<E> doInBackground() throws Exception {
				return findData();
			}
			protected void done(){
				try {					
					List<E> data=get();
					dataLoaded(data);
				} catch (Exception e) {
					MessageUtils.showError("Error al cargar datos", e);
				}
			}
			
		};
		//worker.execute();
		TaskUtils.executeSwingWorker(worker);
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
		grid.packAll();
	}
	
	protected List<E> findData(){
		return new ArrayList<E>();
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
	
	/**
	 * Comoditi metodo para agregar propiedades
	 * 
	 * @param prop
	 */
	public void addProperty(String...prop){
		setProperties(prop);
	}
	
	/**
	 * Comoditi metodo para agregar etiquetas
	 * 
	 * @param lab
	 */
	public void addLables(String...lab){
		setLabels(lab);
	}

	/**
	 * Determina si se debe isntalar el defual editor que normalmente es un editor
	 * que busca con el metodo toString del bean. Esta porpiedad se debe establecer antes
	 * de presentar el browser
	 * 
	 * @return
	 */
	public boolean isInstalarDefaultEditor() {
		return instalarDefaultEditor;
	}

	public void setInstalarDefaultEditor(boolean instalarDefaultEditor) {
		this.instalarDefaultEditor = instalarDefaultEditor;
	}
	
	

}
