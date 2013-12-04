package com.luxsoft.siipap.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
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
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Minimiza el esfuerzo requerido para el mantenimiento de entidades
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public  abstract class AbstractCatalogDialog<T> extends SXAbstractDialog {
	
	protected Logger logger=Logger.getLogger(getClass());
	
	protected final EventList<T> source;
	protected SortedList<T> sortedSource;
	private EventSelectionModel<T> selectionModel; 
	private String headerTitle;
	private String headerDescription;
	protected JXTable grid;
	protected boolean confirmarEliminar=true;
	protected List<Action> toolbarActions;
	private final Class<T> baseClass;
	protected EventList<MatcherEditor<T>> editors=new BasicEventList<MatcherEditor<T>>();

	public AbstractCatalogDialog(Class<T> baseClass,final String title) {
		this(baseClass,new BasicEventList(),title);
	}
	
	public AbstractCatalogDialog(Class<T> baseClass,final EventList<T> source,final String title,final String header,final String headerDesc) {
		this(baseClass,source,title);
		setHeaderTitle(header);
		setHeaderDescription(headerDesc);		
	}
	
	public AbstractCatalogDialog(Class<T> baseClass,final EventList<T> source,final String title) {
		super(title);
		this.source=source;
		this.baseClass=baseClass;
		init();
	}
	
	protected void init(){
		
	}
	
	@Override
	protected JComponent buildContent(){
		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildMainPanel(),BorderLayout.CENTER);
		panel.add(buildToolbar(),BorderLayout.NORTH);
		panel.setPreferredSize(getDefaultSize());
		return panel;
	}
	
	/**
	 * Sobre escribir para ajustar un tamaño 
	 * @return
	 */
	protected Dimension getDefaultSize(){
		return new Dimension(600,400);
	}
	
	
	
	protected JComponent buildToolbar(){
		final ToolBarBuilder builder=new ToolBarBuilder();
		for (Action a:getToolbarActions()){
			builder.add(a);
		}
		for(Map.Entry<String, JComponent> e:componentEditors.entrySet()){
			JLabel l=DefaultComponentFactory.getInstance().createTitle(e.getKey());
			l.setBorder(BorderFactory.createEmptyBorder(0,0,0,7));
			builder.add(l);
			builder.add(e.getValue());
		}
		return builder.getToolBar();
	}
	
	protected List<Action> getToolbarActions(){
		if(toolbarActions==null)
			toolbarActions=CommandUtils.createCommonCURD_Actions(this);
		return toolbarActions;
	}
	
	
	protected JComponent buildMainPanel() {
		Assert.notNull(getTableFormat(),"Debe definir primero el TableFormat");
		sortedSource=new SortedList<T>(getFilteredSource(),null);
		final EventTableModel<T> tm=new EventTableModel<T>(sortedSource,getTableFormat());
		selectionModel=new EventSelectionModel(sortedSource);
		selectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		grid.setSelectionModel(selectionModel);
		final Action select=new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				view();
			}
		};
		ComponentUtils.addEnterAction(grid, select);
		grid.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) view();
			}			
		});
		grid.packAll();
		new TableComparatorChooser<T>(grid,sortedSource,true);
		ComponentUtils.decorateActions(grid);
		return new JScrollPane(grid);
	}
	
	public Class<T> getBaseClass(){
		return baseClass;
	}
	
	protected EventList<T> getFilteredSource(){
		CompositeMatcherEditor<T> editor=new CompositeMatcherEditor<T>(editors);
		FilterList<T> filterList=new FilterList<T>(source,editor);
		return filterList;
	}
	
	protected Map<String, JComponent> componentEditors=new HashMap<String, JComponent>();
	
	public void addTextEditor(final String label,final String... props){
		final JTextField tf=new JTextField(10);
		tf.setName(label);
		TextComponentMatcherEditor<T> ed=new TextComponentMatcherEditor<T>(tf,(TextFilterator<T>) GlazedLists.textFilterator(props));
		componentEditors.put(label, tf);
		addEditor(ed);
	}
	
	public void addEditor(MatcherEditor<T> editor){
		editors.add(editor);
	}
		
	public T getSelected() {
		if(!selectionModel.getSelected().isEmpty()){
			return selectionModel.getSelected().get(0);
		}
		return null;
	}
	
	public void view(){
		T selected=getSelected();
		if(selected!=null){
			doView(selected);
		}
	}
	
	/**
	 * Template Method para personalizar la consulta de beans
	 * 
	 * @param bean
	 */
	protected void doView(T bean){}
	
	/**
	 * Permite editar un objeto de manera adecuada
	 *
	 */
	public void edit(){
		T selected=getSelected();
		if(selected!=null){
			int index=source.indexOf(selected);
			T bean=doEdit(selected);
			if(bean!=null){
				source.set(index, bean);
			}
		}
	}
	
	/**
	 * Template Method para personalizar la edicion de beans
	 * 
	 * @param bean
	 */
	protected T doEdit(T bean){return null;}
	
	
	public void delete(){
		if(isConfirmarEliminar()){
			T bean=getSelected();
			if(bean==null)
				return;
			if(MessageUtils.showConfirmationMessage("Seguro que desa eliminar: \n"+bean, "Eliminar registro")){
				try {
					if(doDelete(bean))				
						source.remove(getSelected());
				} catch (Exception e) {
					MessageUtils.showError("Error eliminando registro",e);
				}
				
			}else
				return;
		}
		
		
	}
	
	protected boolean doDelete(T bean){
		
		return false;
	}
	
	public void insert(){
		Object o=doInsert();
		if(o!=null){
			source.add((T)o);
			grid.packAll();
			int index=sortedSource.indexOf(o);
			selectionModel.setSelectionInterval(index, index);
		}
	}
	
	/**
	 * Template method para facilitar la generacion de nuevos registros en el browser
	 * 
	 * @return
	 */
	protected T doInsert(){
		return null;
	}
	
	/**
	 * Persiste/Actualiza el bean en la base de datos
	 * 
	 * @param bean
	 * @return
	 */
	protected abstract T save(T bean);
	
	public void filter(){
		
	}
	public void print(){
		
	}
	
	public void refresh(){
		SwingWorker<List<T>, String> worker=new SwingWorker<List<T>, String>(){
			@Override
			protected List<T> doInBackground() throws Exception {
				return getData();
			}
			@Override
			protected void done() {
				try {
					source.clear();
					source.addAll(get());
					grid.packAll();
				} catch (Exception e) {
					MessageUtils.showError("Error cargando datos",e);
				}
			}
			
			
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	protected abstract List<T> getData();


	protected abstract TableFormat<T> getTableFormat();
	
	

	@Override
	protected void onWindowOpened() {
		refresh();
	}

	@Override
	protected JComponent buildHeader() {
		if(getHeaderTitle()!=null)
			return new HeaderPanel(getHeaderTitle(),getHeaderDescription());
		return null;
	}

	public String getHeaderTitle() {
		return headerTitle;
	}

	public void setHeaderTitle(String headerTitle) {
		this.headerTitle = headerTitle;
	}

	public String getHeaderDescription() {
		return headerDescription;
	}

	public void setHeaderDescription(String headerDescription) {
		this.headerDescription = headerDescription;
	}

	public boolean isConfirmarEliminar() {
		return confirmarEliminar;
	}

	public void setConfirmarEliminar(boolean confirmarEliminar) {
		this.confirmarEliminar = confirmarEliminar;
	}
	
	

}
