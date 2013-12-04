package com.luxsoft.siipap.swing.utils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.ComponentUtils;
import com.jgoodies.uif.util.Resizer;
import com.jgoodies.uif.util.ScreenUtils;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.UIFactory;
import com.luxsoft.siipap.swing.Application;


/**
 * Permite seleccionar una y solo una requisición pendiente de aplicar pagos
 * 
 * @author Ruben Cancino
 * @deprecated usar {@link com.luxsoft.siipap.swing.dialog.AbstractSelector}
 *
 */
public abstract class AbstractSelector<T> extends AbstractDialog{
	

	private ValueModel valueHolder;
	
	private final Class<T> entityClass; 
	
	private EventList<T> eventList=new BasicEventList<T>();
	
	private int selectionMode=ListSelection.SINGLE_SELECTION;
	
	protected EventTableModel<T> tableModel;
	
	private EventSelectionModel<T> selectionModel;
	
	
	private JXTable table;
	
	private JTextField inputField;
	
	protected Logger logger=Logger.getLogger(getClass());
	
	public AbstractSelector(ValueModel valueHolder,String title) {
		this(title);
		setValueHolder(valueHolder);
	}
	
	public AbstractSelector(String title) {
		this(Application.isLoaded()?Application.instance().getMainFrame():new JFrame(),title);
	}

	public AbstractSelector(JFrame owner,String title) {
		super(owner,title);		
		this.entityClass=(Class<T>)((ParameterizedType)getClass()
				.getGenericSuperclass())
				.getActualTypeArguments()[0];
	}
	
	protected JTextField getInputField(){
		if(inputField==null){
			inputField=new JTextField(50);
		}
		return inputField;
	}	
	
	
	public EventList<T> getEventList() {
		if(eventList==null){
			return new BasicEventList<T>();
		}
		return eventList;
	}

	public JXTable getTable() {
		return table;
	}

	
	
	protected TableFormat<T> getTableFormat(){
		return GlazedLists.tableFormat(this.entityClass,getProperties(),getPropertyNames());
	}
	
	protected TextFilterator<T> getTextFilterator(){
		return GlazedLists.textFilterator(getFilterProperties());
	}
	
	public abstract String[] getProperties();
	
	public abstract String[] getPropertyNames();
	
	public abstract String[] getFilterProperties();
	
	public abstract List<T> getData();
	
	public void open() {
		load();
		super.open();
	}
	
	public  void load(){
		
		SwingWorker<List<T> ,String> worker=new SwingWorker<List<T>, String>(){

			@Override
			protected List<T> doInBackground() throws Exception {
				List<T> data=getData();
				return data!=null?data:new ArrayList<T>();
			}

			@Override
			protected void done() {
				try {
					getEventList().getReadWriteLock().writeLock().lock();
					getEventList().clear();
					getEventList().addAll(get());
					getInputField().transferFocus();
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					getEventList().getReadWriteLock().writeLock().unlock();
				}
			}			
		};
		
		JDialog dialog=new JDialog();
		dialog.getContentPane().add(new HeaderPanel("Trabajando ...","Cargando información de la base de datos"));
		dialog.setModal(true);
		dialog.pack();
		ScreenUtils.locateOnScreenCenter(dialog);
		SwingWorkerWaiter2 waiter=new SwingWorkerWaiter2(dialog);
		worker.addPropertyChangeListener(waiter);
		
		worker.execute();
		dialog.setVisible(true);		
	}


	@Override
	protected JComponent buildContent() {		
		JPanel p=new JPanel(new BorderLayout(2,5));
		p.add(BrowserUtils.buildTextFilterPanel(getInputField()),BorderLayout.NORTH);
		p.add(createLookupGrid(),BorderLayout.CENTER);	
		
		return p;
	}
	
	@SuppressWarnings("unchecked")
	protected JComponent createLookupGrid(){
		
		//eventList=new BasicEventList<T>();
		EventList<T> source=eventList;
		//TextComponentMatcherEditor<T> editor=new TextComponentMatcherEditor<T>(getInputField(),getTextFilterator());		
		//source=new FilterList<T>(eventList,editor);
		final SortedList<T> sortedList=new SortedList<T>(source,null);
		
		TextComponentMatcherEditor<T> editor=new TextComponentMatcherEditor<T>(getInputField(),getTextFilterator());
		final FilterList<T> textFilterList=new FilterList<T>(sortedList,editor);
		
		selectionModel=new EventSelectionModel<T>(textFilterList);
			
		selectionModel.setSelectionMode(getSelectionMode());
				
		tableModel=new EventTableModel<T>(sortedList,getTableFormat());
		
		table=new JXTable(tableModel);//BrowserUtils.buildBrowserGrid();
		table.setModel(tableModel);
		table.setSelectionModel(selectionModel);
		table.packAll();
		//new TableComparatorChooser(table,sortedList,true);
		
		table.getActionMap().put("select",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				doAccept();
			}
		});
		table.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),"select");
		table.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					doAccept();
				}
			}
		});
		ComponentUtils.addAction(table, new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				getInputField().requestFocus();				
			}			
		}, KeyStroke.getKeyStroke("F2"));
		adjustTable(table);
		final JPanel panel=new JPanel(new BorderLayout(0,5));		
		panel.add(UIFactory.createTablePanel(table),BorderLayout.CENTER);
		panel.setPreferredSize(Resizer.FIVE2FOUR.fromWidth(700));
		return panel;
	}
	
	/**
	 * Template method para ajustar la configuracion del grid
	 * 
	 * @param table
	 */
	protected void adjustTable(final JXTable table){
		
	}
	
	public void doApply() {
		if(getSelection().isEmpty()){
			getValueHolder().setValue(null);
		}else{
			//			value=getSelection().get(0);
			getValueHolder().setValue(getSelection().get(0));
		}
		
		super.doApply();
	}
	/*
	@Override
	public void close() {		
		disposeGlazedLists();
		super.close();
	}
	*/

	public void disposeGlazedLists(){		
		try {
			//get
			//getEventList().clear();//eventList.clear();
			//logger.info("Limpiando la lista de");
			getEventList().getReadWriteLock().writeLock().lock();
			getEventList().clear();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			getEventList().getReadWriteLock().writeLock().unlock();
		}
	}
	
	@Override
	protected String getString(String key, String defaultText) {
		return key;
	}
	
	
	@SuppressWarnings("unchecked")
	public T getSelectedBean(){
		return (T)getValueHolder().getValue();
	}
	
	public EventList<T> getSelection(){
		return selectionModel.getSelected();
	}

	public ValueModel getValueHolder() {
		if(valueHolder==null)
			valueHolder=new ValueHolder();
		return valueHolder;
	}

	public void setValueHolder(ValueModel valueHolder) {
		this.valueHolder = valueHolder;
	}

	

	public int getSelectionMode() {		
		return selectionMode;
	}

	public void setSelectionMode(int selectionMode) {
		this.selectionMode = selectionMode;
	}

	
	
	

}
