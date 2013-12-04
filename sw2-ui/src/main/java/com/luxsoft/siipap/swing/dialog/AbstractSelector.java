package com.luxsoft.siipap.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.EventHandler;
import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;

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
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * <P>
 * Permite construir selectores rapidamente indicandole el bean a presentar un {@link TableFormat} 
 * y sobre escribiendo el metodo load.
 * </P>
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public  abstract class AbstractSelector<T> extends SXAbstractDialog {
	
	protected EventList<T> source;
	protected SortedList<T> sortedSource;
	protected EventSelectionModel<T> selectionModel;
	protected JXTable grid;
	protected JTextField textFilter=new JTextField(50);	
	protected final Class<T> beanClass;	
	protected HeaderPanel header=new HeaderPanel("","");
	protected Periodo periodo;
	
	private WeakReference<ValueModel> valueHolderRef;
	
	public AbstractSelector(Dialog owner,final Class<T> clazz,final String title) {
		super(owner,title);
		beanClass=clazz;
	}

	public AbstractSelector(final Class<T> clazz,final String title) {
		this(clazz,title,title,"");
	}
	
	public AbstractSelector(final Class<T> clazz,final String title,final String header,final String headerDesc) {
		super(title);
		beanClass=clazz;
		setHeaderTitle(header);
		setHeaderDescription(headerDesc);
	}
	
	public void initGlazedLists(){
		source=GlazedLists.threadSafeList(new BasicEventList<T>());
		sortedSource=new SortedList<T>(buildFilterList(),getComparator());		
	}
	
	protected Comparator<T> getComparator(){
		return null;
	}
	
	/**
	 * Template method para crear una lista filtrada
	 * Normalmente este metodo no requiere ser modificado, es mejor crear e instalar un editor en 
	 * el metodo installEditors 
	 * 
	 * @return
	 */
	protected EventList<T> buildFilterList(){
		final EventList<MatcherEditor<T>> editors=new BasicEventList<MatcherEditor<T>>();
		installEditors(editors);
		final CompositeMatcherEditor<T> matcherEditor=new CompositeMatcherEditor<T>(editors);
		final FilterList<T> filterList=new FilterList<T>(source,new ThreadedMatcherEditor<T>(matcherEditor));
		return filterList;
	}
	
	/**
	 * Template method para instalar filtros personalizados
	 * 
	 * @param editors
	 */
	protected void installEditors(final EventList<MatcherEditor<T>> editors){
		TextComponentMatcherEditor<T> editor1=new TextComponentMatcherEditor<T>(textFilter,getBasicTextFilter());
		editors.add(editor1);
	}
	
	/**
	 * Metodo para construir el grid. Subclaseses recomendable que ejecuten super.buildGrid antes
	 * de iniciar una personalizacion de este metodo
	 * 
	 * @return
	 */
	protected JXTable buildGrid(){
		final EventTableModel<T> tm=new EventTableModel<T>(sortedSource,getTableFormat());
		selectionModel=new EventSelectionModel(sortedSource);
		selectionModel.setSelectionMode(getSelectionMode());
		final JXTable grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		grid.setSelectionModel(selectionModel);
		final Action select=new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				doSelect();
			}
		};
		ComponentUtils.addEnterAction(grid, select);
		grid.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) doSelect();
			}			
		});
		//new TableComparatorChooser(grid,sortedSource,true);
		TableComparatorChooser.install(grid, sortedSource, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		grid.packAll();
		return grid;
	}
	
	private int selectionMode=ListSelection.SINGLE_SELECTION;
	
	public int getSelectionMode(){
		return selectionMode;
	}
	
	
	public void setSelectionMode(int selectionMode) {
		this.selectionMode = selectionMode;
	}


	/*
	 * (non-Javadoc)
	 * @see com.jgoodies.uif.AbstractDialog#buildContent()
	 */
	@Override
	protected JComponent buildContent() {
		initGlazedLists();
		final JPanel panel=new JPanel(new BorderLayout());		
		panel.add(buildToolbar(),BorderLayout.NORTH);
		grid=buildGrid();
		adjustGrid(grid);
		JComponent c=ComponentUtils.createTablePanel(grid);
		setPreferedDimension(c);
		panel.add(c,BorderLayout.CENTER);
		afterContentBuild(panel);
		return panel;
	}
	
	protected void afterContentBuild(JPanel content){
		
	}
	
	public void adjustGrid(final JXTable grid){
		
	}
	
	protected void setPreferedDimension(JComponent gridComponent){
		
	}
	
	/**
	 * Construye el panel superior que se ocupa para filtros
	 * 
	 * @return
	 */
	protected JComponent buildFilterPanel(){
		return ComponentUtils.buildTextFilterPanel(textFilter);
	}
	
	protected boolean manejarPeriodo=false;
	
	public void manejarPeriodo(){
		periodo=Periodo.getPeriodoDelMesActual(new Date());
		manejarPeriodo=true;
	}
	public void cambiarPeriodo(){
		ValueHolder holder=new ValueHolder(periodo);
		AbstractDialog dialog=Binder.createPeriodoSelector(holder);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=(Periodo)holder.getValue();
			periodoLabel.setText("Per:" +periodo.toString());
			load();
		}
	}
	
	/**
	 * Permite crear una barra de herramientas a ser colocada en la
	 * parte izquierda del panel y en posición vertical
	 * Subclases que quieran agregar mas acciones deberan
	 * llamar super.bukldToolbar para obtener el builder pre configurado
	 * 
	 * @return
	 */
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		if(manejarPeriodo){
			//builder.add(getPeriodoLabel());
		}else{
			builder.add(CommandUtils.createLoadAction(this, "load"));
			builder.add(buildFilterPanel());
		}
		addButton(builder);
		return builder.getToolBar();
	}
	
	protected void addButton(ToolBarBuilder builder){
		
	}
	
	protected ActionLabel periodoLabel;
	

	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){
			periodoLabel=new ActionLabel("Periodo: "+periodo.toString());
			periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	
	/**
	 * Template method ejecutado al dar doble clik o presionar la tecla Enter a un registro del grid
	 *  Por default este metodo cierra la ventana.
	 *  Si existe un {@link ValueModel} asociado se asigna el valor seleccionado 
	 */
	protected void doSelect(){
		
		if(valueHolderRef!=null && valueHolderRef.get()!=null)
			valueHolderRef.get().setValue(getSelected());
		doAccept();
	}
	
	/**
	 * Carga los registros en un sub-proceso que ejecuta el metodo getData()
	 * 
	 * @see getData()
	 */
	public void load(){
		final SwingWorker<List<T>, String> worker=new SwingWorker<List<T>, String>(){			
			protected List<T> doInBackground() throws Exception {				
				return getData();
			}
			protected void done() {				
				try {
					List<T> res=get();
					source.clear();
					source.addAll(res);
				} catch (Exception e) {
					MessageUtils.showError("Error al cargar datos", e);
				}finally{
					grid.packAll();
					afterLoad();
				}
			}
			
			
		};
		//TaskUtils.executeSwingWorker(worker,"Catálogo de clientes","Buscando clientes..");
		execut(worker);
	}
	
	protected void execut(SwingWorker worker){
		TaskUtils.executeSwingWorker(worker,"Catálogo de clientes","Buscando clientes..");
	}
	
	protected void afterLoad(){
		
	}
	
	/**
	 * Regresa el primer bean seleccinado por el usuario
	 * 
	 */	
	public T getSelected() {
		if(!selectionModel.getSelected().isEmpty()){
			return selectionModel.getSelected().get(0);
		}
		return null;
	}
	
	public List<T> getSelectedList(){
		if(!selectionModel.getSelected().isEmpty()){
			return selectionModel.getSelected();
		}
		return new ArrayList<T>();
	}

	/**
	 * {@link TableFormat} para presentar los beans en el gread
	 * 
	 * @return
	 */
	protected abstract TableFormat<T> getTableFormat();
	
	/**
	 * Regresa la lista de beans, probablemente de la base de datos
	 * 
	 * @return
	 */
	protected abstract List<T> getData();
	
	/**
	 * El {@link TextFilterator} basico que por default utiliza el metodo
	 * toString del bean como base de filtrado
	 * 
	 * @return
	 */
	protected TextFilterator<T> getBasicTextFilter(){
		return GlazedLists.toStringTextFilterator();
	}

	/**
	 * Permite colocar un titulo en el browser
	 * 
	 * @param headerTitle
	 */
	public void setHeaderTitle(String headerTitle) {
		this.header.setTitle(headerTitle);
	}
	
	/**
	 * Permite colocar una descripción en el browser
	 * 
	 * @param headerDescription
	 */
	public void setHeaderDescription(String headerDescription) {
		this.header.setTitle(headerDescription);
	}	
	
	/**
	 * Permite asiciar la seleccion a un {@link ValueModel} para
	 * generar un binding
	 * 
	 * @param vm
	 */
	public void setValueModel(final ValueModel vm){
		valueHolderRef=new WeakReference<ValueModel>(vm);
		
	}


	@Override
	protected void onWindowOpened() {
		load();
	}
	
	 

}
