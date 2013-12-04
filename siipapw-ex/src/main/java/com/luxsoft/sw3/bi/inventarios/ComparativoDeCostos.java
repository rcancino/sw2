package com.luxsoft.sw3.bi.inventarios;

import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.TableColumnModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.luxsoft.siipap.swing.utils.ComponentUtils;


public class ComparativoDeCostos  {
	
	private EventList articulosList;
	private SortedList sortedList;
	
	
	private EventTableModel tableModel;
	private EventSelectionModel selectionModel;
	
	private JTextField inputField=new JTextField();
	private JXTable grid;
	private JToolBar toolbar;
	private ArticuloInfoPanel infoPanel;
	private ComparativoModel model=new ComparativoModel();
	
	@SuppressWarnings("unchecked")
	private void initGlazedList(){
		//Source list
		articulosList=new BasicEventList();
		
		//Lista de unicos
		UniqueList uniqueList=new UniqueList(articulosList,GlazedLists.beanPropertyComparator(CostosPorArticuloRow.class,"clave"));
		//FilterList
		TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		TextComponentMatcherEditor textEditor=new TextComponentMatcherEditor(inputField,filterator);
		FilterList filterList=new FilterList(uniqueList,textEditor);
		
		//Sorted List
		sortedList=new SortedList(filterList,null);
		//TableModel
		tableModel=new EventTableModel(sortedList,createTableFormat());
		selectionModel=new EventSelectionModel(sortedList);
		selectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
		
		
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void select(){
		//System.out.println("Seleccion: "+index);	
		try{
			final CostosPorArticuloRow row=(CostosPorArticuloRow)selectionModel.getSelected().get(0);
			System.out.println("Seleccion: "+ row);
			Collection<CostosPorArticuloRow> col=CollectionUtils.select(articulosList,new Predicate(){

				public boolean evaluate(Object object) {
					CostosPorArticuloRow rr=(CostosPorArticuloRow)object;
					return row.getClave().equalsIgnoreCase(rr.getClave());
				}
				
			});
			System.out.println("Seleccionados: "+col);
			infoPanel.loadData(col);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//
		
	}
	
						
	
	private TableFormat createTableFormat(){
		String[] props=new String[]{"clave","descripcion","familia","familiaDesc","unidad","kilos","gramos"};
		String[] names=new String[]{"Clave","Descripción","Familia","Familia Desc","Unidad","kilos","gramos"};
		return GlazedLists.tableFormat(CostosPorArticuloRow.class,props,names);
	}
	

	@SuppressWarnings("unchecked")	
	protected JComponent buildContent() {
		
		initGlazedList();
		
		//Panel principal
		FormLayout layout=new FormLayout(
				"p:g",
				"f:p:g(0.35),3dlu,f:p:g(0.65)"
				);
		PanelBuilder builder=new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc=new CellConstraints();
		
		grid=ComponentUtils.getStandardTable();
		
		grid.setSortable(false);
		grid.setModel(tableModel);
		grid.setSelectionModel(selectionModel);
		
		new TableComparatorChooser(grid,sortedList,true);
		columnSize(grid.getColumnModel());
		
		toolbar=createToolbar();
		JComponent c=new SimpleInternalFrame("",toolbar,grid);
		//GuiStandardUtils.attachDialogBorder(c);
		builder.add(c,cc.xy(1,1));
		
		//Info panel
		infoPanel=new ArticuloInfoPanel();
		builder.add(infoPanel.getControl(),cc.xy(1,3));
		return builder.getPanel();
	}
	
	private void columnSize(TableColumnModel cm){
		cm.getColumn(0).setPreferredWidth(150);
		cm.getColumn(1).setPreferredWidth(350);
		cm.getColumn(2).setPreferredWidth(100);
		cm.getColumn(3).setPreferredWidth(250);
	}
	
	private JToolBar createToolbar(){
		ToolBarBuilder builder=new ToolBarBuilder();
		return builder.getToolBar();
	}
	
	SwingWorker worker;
	//ProgressWatcher watcher=new ProgressWatcher();
	
	
	public void doLoad(){		
		worker=new SwingWorker<List,String>(){			
			@SuppressWarnings("unchecked")
			protected List doInBackground() throws Exception {
				/*
				articulosList.getReadWriteLock().writeLock().lock();
				try{
					
					model.cargarCostosPorArticulo(articulosList);
				}finally{
					articulosList.getReadWriteLock().writeLock().unlock();
				}
				*/
				model.cargarCostosPorArticulo(articulosList);
				return articulosList;
			}
			@Override
			protected void done() {
				selectionModel.setSelectionInterval(0,0);
			}		
						
		};		
		
		worker.execute();
	}

	
	@SuppressWarnings("unchecked")
	
	public void componentOpened() {
		doLoad();
		
	}
	

	
	protected void disposeCollaborators() {
		articulosList.clear();
		if(worker!=null){
			worker.cancel(true);
			//worker.removePropertyChangeListener(watcher);
		}
	}
	
	

}
