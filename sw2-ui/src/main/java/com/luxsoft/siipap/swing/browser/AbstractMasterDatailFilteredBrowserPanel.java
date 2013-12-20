package com.luxsoft.siipap.swing.browser;

import java.util.Comparator;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel.ContextAction;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel.Predicate;
import com.luxsoft.siipap.swing.utils.ComponentUtils;

/**
 * Implementacion abstracta de {@link FilteredBrowserPanel} para facilitar la generacion
 * de paneles con maestro detalle
 * 
 * @author Ruben Cancino
 *
 */
public abstract class AbstractMasterDatailFilteredBrowserPanel<E,D> extends FilteredBrowserPanel<E>{
	
	private int detailSelectionMode=ListSelection.MULTIPLE_INTERVAL_SELECTION;
		
	public AbstractMasterDatailFilteredBrowserPanel(Class<E> beanClazz) {
		super(beanClazz);
		
	}
	
	
	protected void init(){
		setDefaultComparator(createIdComparator());
		agregarMasterProperties();
	}
	
	protected void agregarMasterProperties(){
	}
	
	@Override
	protected JComponent buildContent() {
		JComponent parent=super.buildContent();
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.4);
		sp.setTopComponent(parent);
		sp.setBottomComponent(buildDeailPanel());
		return sp;
	}
	
	protected JXTable detailGrid;
	protected EventSelectionModel detailSelectionModel;
	
	
	
	protected JComponent buildDeailPanel(){
		
		EventList eventLst=selectionModel.getSelected();
		//eventLst=decorateDetailList(eventLst);
		EventList partidasList=new CollectionList(eventLst,createPartidasModel());
		partidasList=decorateDetailList(partidasList);
		SortedList sortedDetail=new SortedList(partidasList,getDefaultDetailComparator());
		EventTableModel tm=new EventTableModel(sortedDetail,createDetailTableFormat());
		
		detailGrid=ComponentUtils.getStandardTable();
		detailGrid.setModel(tm);
		
		detailSelectionModel=new EventSelectionModel(sortedDetail);
		detailSelectionModel.setSelectionMode(getDetailSelectionMode());
		selectionModel.addListSelectionListener(new DetailSelectionHandler());
		detailGrid.setSelectionModel(detailSelectionModel);
		adjustDetailGrid(detailGrid);
		//new TableComparatorChooser(detailGrid,sortedDetail,true);
		this.detailSortedList=sortedDetail;
		TableComparatorChooser.install(detailGrid, sortedDetail, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE_WITH_UNDO);
		return buildDetailGridPanel(detailGrid);
	}
	
	protected SortedList<D> detailSortedList;
	
	/**
	 * Template method para personalizar las propiedades y caracteristicas del JXTable de
	 * detalle
	 * 
	 * @param grid
	 */
	protected void adjustDetailGrid(final JXTable grid){
		
	}
	




	public int getDetailSelectionMode() {
		return detailSelectionMode;
	}


	public void setDetailSelectionMode(int detailSelectionMode) {
		this.detailSelectionMode = detailSelectionMode;
	}


	protected Comparator getDefaultDetailComparator(){
		return null;
	}
	
	public JComponent buildDetailGridPanel(JXTable detailGrid){
		JScrollPane sp=new JScrollPane(detailGrid);
		SimpleInternalFrame frame=new SimpleInternalFrame(detailTitle);
		frame.setContent(sp);
		return frame;
	}
	
	
	protected EventList decorateDetailList( EventList data){
		data.addListEventListener(new DetailGridHandler());
		return data;
	}
	
	private String detailTitle="ND";
	
	
	
	public String getDetailTitle() {
		return detailTitle;
	}


	public void setDetailTitle(String detailTitle) {
		this.detailTitle = detailTitle;
	}


	protected abstract TableFormat createDetailTableFormat();
	
	
	
	protected abstract Model<E,D> createPartidasModel();
	
	/**
	 * Genera un panel de filtros diferente al
	 * default
	 */	
	
	public JPanel getFilterPanel() {
		if(filterPanel==null){
			filterPanel=getFilterPanelBuilder().getPanel();
			installFilters(filterPanelBuilder);			
			installDetailFilterComponents(filterPanelBuilder);
		}
		return filterPanel;
	}
	
	protected void installFilters(final DefaultFormBuilder builder){
		builder.appendSeparator("Maestro");
		//Instalamos los filtros basados en textComponents
		for(Map.Entry<String, JComponent> entry:textEditors.entrySet()){
			builder.append(entry.getKey(),entry.getValue());
		}
		installCustomComponentsInFilterPanel(builder);
	}
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
	}
	
	public Action addDetailContextAction(Predicate p,String actionId,String method,String label){
		ContextAction a=new ContextAction(p,this,method);
		configAction(a, actionId,label);
		this.detailSelectionModel.addListSelectionListener(a);
		return a;
	}
	
	public class DetailGridHandler implements ListEventListener{

		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				switch (listChanges.getType()) {
				case ListEvent.INSERT:
					detailGridInserted();
				case ListEvent.DELETE:
				case ListEvent.UPDATE:
					
					break;
				default:
					break;
				}				
			}
			detailChanged();
		}
		
	}
	
	/**
	 * Template method para detectar cambios en el grid de detalle 
	 */
	protected void detailChanged(){
		
	}

	public void detailGridInserted() {
		this.detailGrid.packAll();
		
	}
	/*
	protected HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}*/
	
	private class DetailSelectionHandler implements ListSelectionListener{

		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()){
				detailGrid.packAll();
			}
			
		}
		
	}
		
}


