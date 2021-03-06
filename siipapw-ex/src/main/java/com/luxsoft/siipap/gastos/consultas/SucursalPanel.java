package com.luxsoft.siipap.gastos.consultas;

import java.util.Comparator;
import java.util.List;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.FunctionList.Function;

public class SucursalPanel extends AbstractGastosGroupByPanel {
	

	public SucursalPanel(final EventList source){
		super(source);
	}
	
	
	/**
	 * Comparador para agrupar por rubro
	 * 
	 */
	protected Comparator getGroupComparator(){
		return GlazedLists.beanPropertyComparator(AnalisisDeGasto.class, "sucursal");
	}

	/**
	 * Implementacion para totalizar por rubro
	 * 
	 */
	@Override
	protected Function<List<AnalisisDeGasto>, com.luxsoft.siipap.gastos.consultas.AbstractGastosGroupByPanel.GroupByBean> createGroupByFunction() {
		
		return new GroupByFunction();
	}
	
	
	/**
	 * Agrupa por Rubro
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class GroupByFunction implements Function<List<AnalisisDeGasto>, GroupByBean>{
		public GroupByBean evaluate(List<AnalisisDeGasto> sourceValue) {
			GroupByBean bean=new GroupByBean();			
			bean.setKey(sourceValue.get(0).getSucursal());
			double val=0;
			for(AnalisisDeGasto a:sourceValue){
				val+=a.getImporte().doubleValue();
			}
			bean.setValue(val);
			return bean;
		}
		
	}



	
	
}
