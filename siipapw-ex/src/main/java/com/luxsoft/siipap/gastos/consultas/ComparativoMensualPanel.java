package com.luxsoft.siipap.gastos.consultas;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.luxsoft.siipap.swing.utils.ComponentUtils;

/**
 * Panel comparativo mensual para gastos
 * 
 * @author Ruben Cancino
 *
 */
public class ComparativoMensualPanel extends JPanel {
	
	
	private final EventList source;
	protected GroupingList groupList;
	protected FunctionList groupedByList;
	
	protected String[] properties={"mes","rubro","sucursal","importe","2007","2008"};
	protected String[] labels=properties;
	private JXTable grid;
	
	
	

	public ComparativoMensualPanel(final EventList source){
		this.source=source;			
	}
	
	public void init(){		
		initGlazedList();
		setLayout(new BorderLayout());		
		add(buildListPanel(),BorderLayout.CENTER);
	}
	
	/*** GlazedList magic implementation ****/
	
	private void initGlazedList(){
		
		List<Comparator<AnalisisDeGasto>> list=new ArrayList<Comparator<AnalisisDeGasto>>();
		list.add(GlazedLists.beanPropertyComparator(AnalisisDeGasto.class, "mes"));
		list.add(GlazedLists.beanPropertyComparator(AnalisisDeGasto.class, "rubro"));
		list.add(GlazedLists.beanPropertyComparator(AnalisisDeGasto.class, "sucursal"));
		//list.add(GlazedLists.beanPropertyComparator(AnalisisDeGasto.class, "proveedor"));
		//list.add(GlazedLists.beanPropertyComparator(AnalisisDeGasto.class, "descripcion"));
		
		Comparator comparator=GlazedLists.chainComparators(list);
		
		groupList=new GroupingList(source,comparator);
		
		groupList.addListEventListener(new TotalHandler());
		
		groupedByList=new FunctionList(groupList,new GroupFunction());		
	}
	
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
		final SortedList sortedList=new SortedList(groupedByList);
		EventTableModel tm=new EventTableModel(sortedList,getGridTableFormat());
		grid.setModel(tm);
		new TableComparatorChooser(grid,sortedList,true);
		decorateGrid(grid);
		JScrollPane sp=new JScrollPane(grid);		
		return sp;
	}
	
	protected void decorateGrid(final JXTable grid){
		//grid.getColumnExt(2).setCellRenderer(Renderers.getPorcentageRenderer(100));
		//grid.getColumnExt(1).setCellRenderer(Renderers.getCantidadNormalTableCellRenderer());
	}
	
	protected TableFormat getGridTableFormat(){		
		return GlazedLists.tableFormat(AcumuladoBean.class
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
	
	private class GroupFunction implements FunctionList.Function<List<AnalisisDeGasto>,AcumuladoBean>{

		public AcumuladoBean evaluate(List<AnalisisDeGasto> sourceValue) {
			AcumuladoBean bean=new AcumuladoBean();
			AnalisisDeGasto a=sourceValue.get(0);
			bean.setMes(a.getMes());
			bean.setProveedor(a.getProveedor());
			bean.setRubro(a.getRubro());
			bean.setDescripcion(a.getDescripcion());
			bean.setSucursal(a.getSucursal());
			for(AnalisisDeGasto ana:sourceValue){
				bean.getPeriodos().put(ana.getYear(), ana.getImporte().doubleValue());
			}
			return bean;
		}
		
		
	}
	
	
	private class TotalHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			updateGraphList(listChanges);
		}
	}
	
	public class AcumuladoBean implements Comparable<AcumuladoBean>{
		
		private Integer mes;
		private String  rubro;
		private String  sucursal;
		private String  proveedor;
		private String  descripcion;
		
		private Map<Integer,Double> periodos=new TreeMap<Integer, Double>();		
		
		public AcumuladoBean() {
		}
		
		public Integer getMes() {
			return mes;
		}
		public String getRubro() {
			return rubro;
		}
		public String getSucursal() {
			return sucursal;
		}
		public String getProveedor() {
			return proveedor;
		}
		public String getDescripcion() {
			return descripcion;
		}
		
		public double getImporte() {
			double imp=0;
			for(Map.Entry<Integer, Double> entry:periodos.entrySet()){
				imp+=entry.getValue();
			}
			return imp;
		}
		
		public Map<Integer, Double> getPeriodos() {
			return periodos;
		}

		public void setMes(Integer mes) {
			this.mes = mes;
		}

		public void setRubro(String rubro) {
			this.rubro = rubro;
		}

		public void setSucursal(String sucursal) {
			this.sucursal = sucursal;
		}

		public void setProveedor(String proveedor) {
			this.proveedor = proveedor;
		}

		public void setDescripcion(String descripcion) {
			this.descripcion = descripcion;
		}

		public void setPeriodos(Map<Integer, Double> periodos) {
			this.periodos = periodos;
		}
		
		public int compareTo(AcumuladoBean o) {
			return mes.compareTo(o.getMes());
		}
		
		public double get2007(){
			if(periodos.containsKey(2007)){
				return periodos.get(2007);
			}
			return 0;
		}
		
		public double get2008(){
			if(periodos.containsKey(2008)){
				return periodos.get(2008);
			}
			return 0;
		}
		
	}

}
