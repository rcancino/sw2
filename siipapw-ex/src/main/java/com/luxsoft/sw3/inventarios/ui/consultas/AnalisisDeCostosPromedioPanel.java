package com.luxsoft.sw3.inventarios.ui.consultas;

import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DateUtil;

public class AnalisisDeCostosPromedioPanel extends AbstractMasterDatailFilteredBrowserPanel<Producto,CostoPromedio>{
	
	

	public AnalisisDeCostosPromedioPanel() {
		super(Producto.class);
	}
	
	protected void agregarMasterProperties(){
		addProperty("id","clave","descripcion","lineaOrigen","activo","inventariable","servicio","deLinea","nacional","linea.nombre","marca.nombre","clase.nombre","lineaOrigen","kilos","gramos","activoVentas","activoCompras","activoInventario","eliminado");
		addLabels("Id","Clave","Descripcion","Familia","Activo","Inventariable","Servicio","DeLinea","Nacional","Línea","Marca","Clase","Familia","Kilos","Gramos","Ventas","Compras","Inv","B");
		installTextComponentMatcherEditor("Articulo", "clave","descripcion");
		installTextComponentMatcherEditor("Linea", "linea.nombre");
		installTextComponentMatcherEditor("Marca", "marca.nombre");
		installTextComponentMatcherEditor("Familia", "lineaOrigen");
		
	}
	
	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={
				"year"
				,"mes"
				,"costop"
		};
		String[] names={
				"Año"
				,"Mes"
				,"Costo Promedio"
		};	
		return GlazedLists.tableFormat(CostoPromedio.class,props,names);
	}


	@Override
	protected Model<Producto, CostoPromedio> createPartidasModel() {
		return new Model<Producto, CostoPromedio>(){
			public List<CostoPromedio> getChildren(Producto parent) {
				if(parent.getCostos()==null){
					parent.setCostos(ServiceLocator2.getHibernateTemplate().find("from CostoPromedio e where e.producto.id=?",parent.getId()));
				}
				return parent.getCostos();
			}
			
		};
	}
	
	public JComponent buildDetailGridPanel(JXTable detailGrid){
		JScrollPane sp=new JScrollPane(detailGrid);
		JTabbedPane tabPanel=new JTabbedPane();
		tabPanel.addTab("Gráfica", createChartPanel1());
		tabPanel.addTab("Detalle", sp);
		return tabPanel;
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
		
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()				
				};
		return actions;
	}

	private JCheckBox sospechosesBox;
	
	public JComponent[] getOperacionesComponents(){
		if(sospechosesBox==null){
			sospechosesBox=new JCheckBox("Sospechosos",false);
			sospechosesBox.setOpaque(false);
		}
		return new JComponent[]{sospechosesBox};
	}
	
	@Override
	protected List<Producto> findData() {
		return ServiceLocator2.getProductoManager().getAll();
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
	public void open(){
		this.detailSelectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
		load();
	}
	
	public void close(){
		
	}
	
	protected void detailChanged(){
		actualizar();
	}

	private DefaultCategoryDataset dataSet;
	
	private JFreeChart costosChart;
	private JFreeChart res;
	

	private JComponent createChartPanel1(){	
		
		dataSet=new DefaultCategoryDataset();		
	/*costosChart=ChartFactory.createBarChart3D(
				"Costos por Periodo"
				,"Periodo"
				,"Costo"
				,dataSet
				,PlotOrientation.VERTICAL,true,true,false);*/
		
		 //costosChart=ChartFactory.createLineChart3D
		costosChart=ChartFactory.createBarChart(
		//costosChart=ChartFactory.createLineChart(	
		//costosChart=ChartFactory.createLineChart(	
				"Costos por Periodo"
				,"Periodo"
				,"Costo Promedio"
				,dataSet
				,PlotOrientation.VERTICAL
				,true
				,true
				,false
				);
		
		//NumberAxis3D axis1=new NumberAxis3D("Costo");
		//axis1.setStandardTickUnits(NumberAxis3D.createIntegerTickUnits());
		//LineAndShapeRenderer renderer1=new LineAndShapeRenderer()
		
		//dataSet2=new DefaultCategoryDataset();
		//chart2=ChartFactory.createLineChart3D("Existencias","Periodo","Existencia",dataSet2,PlotOrientation.VERTICAL,true,true,false);
		
		CategoryAxis3D axis=new CategoryAxis3D("Periodo");
		axis.setLabelFont(axis.getLabelFont().deriveFont(.8f));
		//axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/5.0));
		axis.setCategoryLabelPositionOffset(8);
		
		CombinedDomainCategoryPlot plot=new CombinedDomainCategoryPlot(axis);
		plot.add(costosChart.getCategoryPlot(),2);
		//plot.set
		//plot.add(chart2.getCategoryPlot(),1);		
		//ChartFactory.create
		res=new JFreeChart("Graficas",plot);
		return new ChartPanel(res);
	}
	
	private void actualizar(){
		dataSet.clear();
		//dataSet2.clear();
		res.setTitle("");
		Producto selected=(Producto)getSelectedObject();
		if(selected!=null){
			for(CostoPromedio cp:selected.getCostos()){			
				dataSet.addValue(cp.getCostop().doubleValue(),String.valueOf(cp.getYear()),DateUtil.getMesAsString(cp.getMes()));				
				res.setTitle(cp.getProducto().getDescripcion()+"   ("+cp.getProducto().getClave()+")");
			}
		}
	}

}
