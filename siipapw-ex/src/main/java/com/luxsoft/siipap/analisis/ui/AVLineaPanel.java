package com.luxsoft.siipap.analisis.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.PieDataset;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.jfreechart.EventListPieDataset;

import com.luxsoft.siipap.analisis.model.VentaPorLinea;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.SQLUtils;

public class AVLineaPanel extends FilteredBrowserPanel<VentaPorLinea>{

	public AVLineaPanel() {
		super(VentaPorLinea.class);
		addProperty("linea","toneladasCre","toneladasCon","ventaNetaCre","ventaNetaCon","costoCre","costoCon");		
	}	
	
	@Override
	protected List<VentaPorLinea> findData() {
		String  sql=SQLUtils.loadSQLQueryFromResource("sql/analisis/avPorLineaGeneral2.sql");		
		List res=ServiceLocator2.getAnalisisJdbcTemplate().query(sql,new BeanPropertyRowMapper(VentaPorLinea.class));
		GroupingList<VentaPorLinea> glist=new GroupingList(GlazedLists.eventList(res),new Comparator<VentaPorLinea>(){
			public int compare(VentaPorLinea o1,VentaPorLinea o2) {
				return o1.getLinea().compareTo(o2.getLinea());
			}
		});
		final List<VentaPorLinea> ventas=new ArrayList<VentaPorLinea>();
		
		for(int i=0;i<glist.size();i++){
			List<VentaPorLinea> l=glist.get(i);
			System.out.println(l);
			
			VentaPorLinea a=new VentaPorLinea();
			a.setYear(l.get(0).getYear());
			a.getChilren().addAll(l);
			a.setLinea(l.get(0).getLinea());
			a.consolidar();
			ventas.add(a);
		
		}
		
		return ventas;
	}	

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}	
	
	
	@Override
	protected JComponent buildContent() {
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setTopComponent(super.buildContent());
		sp.setBottomComponent(buildGrafica());
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.4);
		return sp;
	}
	
	private JComponent buildGrafica(){
		final FunctionList.Function keyFunction=new FunctionList.Function(){
			public Object evaluate(Object sourceValue) {
				VentaPorLinea bean=(VentaPorLinea)sourceValue;
				return bean.getLinea();
			}
		};
		final FunctionList.Function valueFunction=new FunctionList.Function(){
			public Object evaluate(Object sourceValue) {
				VentaPorLinea bean=(VentaPorLinea)sourceValue;
				return bean.getToneladasCre();
			}
			
		};
		final PieDataset dataSet=new EventListPieDataset(source,keyFunction,valueFunction);
		final JFreeChart chart=ChartFactory.createPieChart3D("General", dataSet, true, true, false);
		
		final ChartPanel panel=new ChartPanel(chart,true);
		panel.setPreferredSize(new Dimension(300,300));
		return panel;
	}


	public static void main(String[] args) {
		final AVLineaPanel panel=new AVLineaPanel();
		
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){

			@Override
			protected JComponent buildContent() {
				return panel.getControl();
			}

			@Override
			protected void onWindowOpened() {
				panel.load();
			}
			
			
		};
		dialog.open();
		System.exit(0);
	}

}
