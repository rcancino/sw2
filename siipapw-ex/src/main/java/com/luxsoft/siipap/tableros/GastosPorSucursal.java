package com.luxsoft.siipap.tableros;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.math.BigDecimal;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;

import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;



/**
 * Medicion de gastos por sucursal
 * 
 * @author Ruben Cancino
 *
 */
public class GastosPorSucursal {

	private JComponent control;
	
	public JComponent getControl(){
		if(control==null){
			control=buildControl();
		}
		return control;
	}
	
	protected JComponent buildControl(){
		JPanel panel=new JPanel(new BorderLayout());
		DefaultValueDataset dataSet=new DefaultValueDataset(BigDecimal.valueOf(65.00));
		MeterPlot plot=new MeterPlot(dataSet);
		plot.setUnits(" $ (Millones) ");
		plot.setRange(new Range(20.00,140.00));
		MeterInterval normal=new MeterInterval("ALTO",new Range(90.00,140.00));
		plot.addInterval(normal);
		//plot.setNeedlePaint(Color.WHITE);
		//plot.setDialShape(DialShape.CHORD);
		
		final JFreeChart chart=new JFreeChart("Gasto x Sucursal",JFreeChart.DEFAULT_TITLE_FONT,plot,false);
		final ChartPanel chartPanel=new ChartPanel(chart,true);
		panel.add(chartPanel,BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(600,400));
		return panel;		
	}
	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		final GastosPorSucursal p=new GastosPorSucursal();
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){

			@Override
			protected JComponent buildContent() {
				return p.getControl();
			}
			
		};
		dialog.open();
		System.exit(0);
	}

}
