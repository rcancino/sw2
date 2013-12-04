package com.luxsoft.siipap.ventas.consultas;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;


import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Panel que sirve de base para las consultas de analisis 
 * 
 * 
 * @author Ruben Cancino
 *
 */
public abstract class AbstractAnalisisPanel extends AbstractControl{
	
	protected EventList sourceList;
	protected JXTable masterGrid;
	protected JTabbedPane tabPanel;

	@Override
	protected JComponent buildContent() {
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT
				,buildMasterPanel()
				,buildDetailPanel());
		sp.setResizeWeight(.3);
		sp.setOneTouchExpandable(true);
		return sp;
	}
	
	protected JComponent buildMasterPanel(){
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildGridPanel(),BorderLayout.CENTER);
		return panel;
	}
	
	protected SortedList sortedList;
	protected EventSelectionModel selectionModel;
	
	protected JComponent buildGridPanel(){
		initGlazedList();
		masterGrid=ComponentUtils.getStandardTable();
		sortedList=buildSortedList();
		TableFormat tableFormat=buildTableFormat();
		final EventTableModel tm=new EventTableModel(sortedList,tableFormat);
		masterGrid.setModel(tm);
		selectionModel =new EventSelectionModel(sortedList);
		masterGrid.setSelectionModel(selectionModel);
		new TableComparatorChooser(masterGrid,sortedList,true);
		JScrollPane sp=new JScrollPane(masterGrid);
		return sp;
	}
	
	 
	
	protected SortedList buildSortedList(){
		sortedList=new SortedList(sourceList,null);
		return sortedList;
	}
	
	public EventList getSourceList(){
		return sortedList;
	}
	
	public SortedList getSortedList(){
		return sortedList;
	}
	
	protected abstract TableFormat buildTableFormat();
	
	protected abstract List loadData();
	
	protected JComponent buildDetailPanel(){
		tabPanel=new JTabbedPane();
		return tabPanel;
	}
	
	public void close(){
		sourceList.clear();
	}

	public JXTable getMasterGrid() {
		return masterGrid;
	}
	
	protected void initGlazedList(){
		sourceList=GlazedLists.threadSafeList(new BasicEventList());
		
	}
	
	
	private EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
	
	protected EventList getFilterList(){
		installMatcherEditors(editors);
		CompositeMatcherEditor editor=new CompositeMatcherEditor(editors);
		FilterList filterList=new FilterList(sourceList,editor);
		return filterList;
	}
	
	/**
	 * Hook para instalar matcher editores especificos
	 * 
	 * @param editors
	 */
	protected void installMatcherEditors(EventList<MatcherEditor> editors){
		
	}
	
	public void load(){
		beforeLoad();
		final SwingWorker<List, String> worker=new SwingWorker<List, String>(){			
			protected List doInBackground() throws Exception {
				return loadData();
			}			
			protected void done() {
				try {
					afterLoad(get());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
		};
		executeWorker(worker);
	}
	
	protected void beforeLoad(){
		sourceList.clear();
	}
	
	protected void afterLoad(final List res){
		sourceList.addAll(res);
		masterGrid.packAll();
	}
	
	protected void executeWorker(SwingWorker worker){
		TaskUtils.executeTask(worker);
	}
	
	protected List<Action> actions;
	
	public List<Action> getActions(){
		if(actions==null){
			actions=new ArrayList<Action>();
			actions.add(getLoadAction());
		}
		return actions;
	}
	
	private Action loadAction;
	
	public Action getLoadAction(){
		if(loadAction==null){
			loadAction=CommandUtils.createLoadAction(this, "load");
		}
		return loadAction;
	}

}
