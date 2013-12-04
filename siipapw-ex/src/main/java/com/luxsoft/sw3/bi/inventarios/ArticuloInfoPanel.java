package com.luxsoft.sw3.bi.inventarios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.UIFactory;

@SuppressWarnings("unchecked")
public class ArticuloInfoPanel {
	
	private EventList<CostosPorArticuloRow> costos;
	private SortedList<CostosPorArticuloRow> sortedCostos;
	
	
	public ArticuloInfoPanel(){
		costos=new BasicEventList();
		sortedCostos=new SortedList(costos,new RowsComparator());
		//sortedCostos.addListEventListener(new Handler());
	}
	
	public void loadData(final Collection col){
		costos.getReadWriteLock().writeLock().lock();
		try{
			costos.clear();		
			costos.addAll(col);
		}finally{
			costos.getReadWriteLock().writeLock().unlock();
		}
		//SwingWorker worker=new SwingWorker(){
			
		//}
		actualizar();
	}

	public JComponent getControl() {
		FormLayout layout=new FormLayout(
				"f:p:g(.3),4dlu,f:p:g(.7)",
				"f:p:g"
				);
		PanelBuilder builder=new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc=new CellConstraints();
		builder.add(createTablePanel(),cc.xy(1,1));
		builder.add(createChartPanel1(),cc.xy(3,1));
		//builder.add(UIFactory.createStrippedScrollPane(createChartPanel2()),cc.xy(3,3));
		return builder.getPanel();
	}
	
	@SuppressWarnings("unchecked")
	private JComponent createTablePanel(){
		JPanel panel=new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);
		
		//El grid de los costos
		String[] props={"clave","periodo","existencia","costoPromedio","costoUltimo"};
		String[] names={"Clave","Periodo","Existencia","C. Promedio","C. Ultimo"};
		TableFormat format=GlazedLists.tableFormat(CostosPorArticuloRow.class,props,names);
		
		EventTableModel model=new EventTableModel(sortedCostos,format);
		
		JTable table=new JTable(model);
		panel.add(UIFactory.createTablePanel(table),BorderLayout.CENTER);
		return panel;
		
	}
	
	private DefaultCategoryDataset dataSet;
	private DefaultCategoryDataset dataSet2;
	private JFreeChart pieChart;
	private JFreeChart chart2;
	private JFreeChart res;
    
	private JComponent createChartPanel1(){	
		
		dataSet=new DefaultCategoryDataset();		
		pieChart=ChartFactory.createBarChart3D("Costos por Periodo","Periodo","Costo",dataSet,PlotOrientation.VERTICAL,true,true,false);
		//NumberAxis3D axis1=new NumberAxis3D("Costo");
		//axis1.setStandardTickUnits(NumberAxis3D.createIntegerTickUnits());
		//LineAndShapeRenderer renderer1=new LineAndShapeRenderer()
		
		dataSet2=new DefaultCategoryDataset();
		chart2=ChartFactory.createLineChart3D("Existencias","Periodo","Existencia",dataSet2,PlotOrientation.VERTICAL,true,true,false);
		
		CategoryAxis3D axis=new CategoryAxis3D("Periodo");
		axis.setLabelFont(axis.getLabelFont().deriveFont(.8f));
		axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/5.0));
		axis.setCategoryLabelPositionOffset(8);
		CombinedDomainCategoryPlot plot=new CombinedDomainCategoryPlot(axis);
		plot.add(pieChart.getCategoryPlot(),2);
		plot.add(chart2.getCategoryPlot(),1);		
		//ChartFactory.create
		res=new JFreeChart("Graficas",plot);
		return new ChartPanel(res);
	}
	
	private void actualizar(){
		dataSet.clear();
		dataSet2.clear();
		res.setTitle("");
		for(CostosPorArticuloRow row:sortedCostos){
			
			dataSet.addValue(row.getCostoPromedio().doubleValue(),"Promedio",row.getPeriodo());
			dataSet.addValue(row.getCostoUltimo().doubleValue(),"Ultimo",row.getPeriodo());
			dataSet2.addValue(row.getExistencia().doubleValue(),"Existencia",row.getPeriodo());
			res.setTitle(row.getDescripcion()+"   ("+row.getClave()+")");
		}
	}
	/*
	private class Handler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {			
			for(CostosPorArticuloRow row:sortedCostos){
				dataSet.clear();
				dataSet2.clear();
				dataSet.addValue(row.getCostoPromedio().doubleValue(),"Promedio",row.getPeriodo());
				dataSet.addValue(row.getCostoUltimo().doubleValue(),"Ultimo",row.getPeriodo());
				dataSet2.addValue(row.getExistencia().doubleValue(),"Existencia",row.getPeriodo());
				res	.setTitle(row.getDescripcion()+"   ("+row.getClave()+")");
			}
		}
		
	}
	*/

	
	private class RowsComparator implements Comparator<CostosPorArticuloRow>{
		
		private SimpleDateFormat df=new SimpleDateFormat("MM/yyyy");

		public int compare(CostosPorArticuloRow o1, CostosPorArticuloRow o2) {
			try{
				Date d1=df.parse(o1.getPeriodo());
				Date d2=df.parse(o2.getPeriodo());
				return d1.compareTo(d2);
			}catch(Exception ex){
				ex.printStackTrace();
				return 0;
			}
			
			
		}
		
	}
	
	
	
	

}
