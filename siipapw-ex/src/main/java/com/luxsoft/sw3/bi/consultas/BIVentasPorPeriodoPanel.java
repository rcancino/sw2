package com.luxsoft.sw3.bi.consultas;

import java.util.Iterator;
import java.util.List;

import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.sw3.bi.AnalisisDeVenta;
import com.luxsoft.sw3.bi.BIVenta;

public class BIVentasPorPeriodoPanel extends FilteredBrowserPanel<AnalisisDeVenta>{

	private  GroupingList<BIVenta> ventasPorPeriodo;
	
	public BIVentasPorPeriodoPanel(GroupingList<BIVenta> source) {
		super(AnalisisDeVenta.class);
		this.ventasPorPeriodo=source;
		this.ventasPorPeriodo.addListEventListener(new SourceHandler());
	}
	
	protected void init(){
		addProperty(
				"entidad"
				,"year"
				,"mes"
				,"ventasBrutas"
				,"ventasNetas"
				,"costo"
				,"utilidad"
				,"utitlidadPorcentual"
				);
		addLabels(
				"Tipo"
				,"Año"
				,"Mes"
				,"Venta Bruta"
				,"Venta Neta"
				,"Costo"
				,"Utilidad"
				,"Util (%)"
				);
	}

	
	protected void cargar(){
		getSource().clear();
		for(List<BIVenta> vp:ventasPorPeriodo){
			AnalisisDeVenta a=new AnalisisDeVenta();
			for(Iterator<BIVenta> it=vp.iterator();it.hasNext();){
				BIVenta v=it.next();
				a.setVentasBrutas(a.getVentasBrutas().add(v.getIMPORTE_BRUTO()));
				a.setImporte(a.getImporte().add(v.getImporte()));
				a.setDevoluciones(a.getDevoluciones().add(v.getDEVOLUCION2()));
				a.setBonificaciones(a.getBonificaciones().add(v.getBONIFICACION()));
				if(!it.hasNext()){
					a.setYear(v.getYear());
					a.setMes(v.getMes());
					getSource().add(a);
				}
			}
		}
	}
	
	private class SourceHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			while(listChanges.hasNext()){
				
			}
			cargar();
		}
	}

}
