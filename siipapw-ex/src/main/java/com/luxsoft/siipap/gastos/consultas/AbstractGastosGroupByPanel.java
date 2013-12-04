package com.luxsoft.siipap.gastos.consultas;

import java.awt.BorderLayout;
import java.util.Comparator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.FunctionList.Function;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.Renderers;

/**
 * Clase base para la creacion de paneles que representan gastos agrupados de alguna forma
 * 
 * @author Ruben Cancino
 *
 */
public abstract class AbstractGastosGroupByPanel extends JPanel {
	
	
	private final EventList source;
	protected GroupingList groupList;
	protected FunctionList groupedByList;
	
	protected String[] properties={"key","value","participacion"};
	protected String[] labels={"Llave","Valor","Par(%)"};
	private JXTable grid;
	
	
	

	public AbstractGastosGroupByPanel(final EventList source){
		this.source=source;			
	}
	
	public void init(){		
		initGlazedList();
		setLayout(new BorderLayout());		
		add(buildListPanel(),BorderLayout.CENTER);
	}
	
	/*** GlazedList magic implementation ****/
	
	private void initGlazedList(){
		groupList=new GroupingList(source,getGroupComparator());
		groupList.addListEventListener(new TotalHandler());
		groupedByList=new FunctionList(groupList,createGroupByFunction());		
	}
	
	/**
	 * {@link FunctionList} encargada de generar la lista final de elementos totalizados
	 * por una llave 
	 *  
	 * 
	 * @return
	 */
	protected abstract Function createGroupByFunction();
	
	/**
	 * Comparator que permite crear una instacia de {@link GroupingList} para agrupar en una
	 * primera etapa los elementos analizados
	 * 
	 * @return
	 */
	protected abstract Comparator getGroupComparator();
	
	/**
	 * Inicializa los componentes del panel
	 * 
	 */
	@Override
	public void addNotify() {		
		super.addNotify();
		init();
	}
	
	
	protected JComponent buildListPanel(){
		grid=ComponentUtils.getStandardTable();
		grid.setColumnControlVisible(false);
		final SortedList sortedList=new SortedList(groupedByList,createInitialComparator());
		EventTableModel tm=new EventTableModel(sortedList,getGridTableFormat());
		grid.setModel(tm);
		new TableComparatorChooser(grid,sortedList,true);
		decorateGrid(grid);
		JScrollPane sp=new JScrollPane(grid);		
		return sp;
	}
	
	protected void decorateGrid(final JXTable grid){
		grid.getColumnExt(2).setCellRenderer(Renderers.getPorcentageRenderer(100));
		grid.getColumnExt(1).setCellRenderer(Renderers.getCantidadNormalTableCellRenderer());
	}
	
	/**
	 * Sencible default para la sortedList del grid
	 * 
	 * @return
	 */
	protected Comparator createInitialComparator(){
		Comparator c= GlazedLists.beanPropertyComparator(GroupByBean.class,"value");
		return GlazedLists.reverseComparator(c);
	}
	
	protected TableFormat getGridTableFormat(){
		return GlazedLists.tableFormat(GroupByBean.class
				,getProperties()
				,getLabels()
		);
	}
	
	/**
	 * Total del campo medible de la lista agrupadora
	 * Se utliza para calcular la participacion
	 *  
	 */
	private double total;
	
	/**
	 * Actualiza la lista fuente de la grafica
	 * 
	 */
	protected void updateGraphList(ListEvent listChanges){		
		double res=0;
		for(Object gb:source){
			AnalisisDeGasto bb=(AnalisisDeGasto)gb;
			res+=bb.getImporte().doubleValue();
		}
		total=res;
		System.out.println("Nuevo total: "+total);		
	}
	
	
	public String[] getProperties() {
		return properties;
	}

	public void setProperties(String... properties) {
		this.properties = properties;
	}

	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String...labels) {
		this.labels = labels;
	}	
	
	
	/**
	 * Bean para representar agrupaciones
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public  class GroupByBean {
		
		private String key;
		private double value;
		
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public double getValue() {
			return value;
		}
		public void setValue(double value) {
			this.value = value;
		}	
		
		public double getParticipacion(){
			if(total==0)
				return 0;
			double res= value/total;
			return res;
		}
		
	}
	
	
	private class TotalHandler implements ListEventListener<GroupByBean>{
		public void listChanged(ListEvent<GroupByBean> listChanges) {
			updateGraphList(listChanges);
		}
	}

}
