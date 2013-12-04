package com.luxsoft.siipap.pos.ui.selectores;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.caja.Gasto;
import com.luxsoft.sw3.services.Services;

public class SelectorDeGastos extends AbstractSelector<Gasto>{
	
	private Long sucursal;

	public SelectorDeGastos() {
		super(Gasto.class, "Gastos registrados");
	}

	@Override
	protected List<Gasto> getData() {
		String hql="from Gasto g where g.sucursal.id=? and g.rembolso is null";
		return Services.getInstance().getHibernateTemplate().find(hql, getSucursal());
	}

	@Override
	protected TableFormat<Gasto> getTableFormat() {
		String[] props={"sucursal.nombre","descripcion","importe","documento","comentario","rembolso"};
		String[] names={"Sucursal","Descripción","Importe","Docto","Comentario","Rembolso"};
		return GlazedLists.tableFormat(Gasto.class, props,names);
	}

	
	
	public Long getSucursal() {
		return sucursal;
	}

	public void setSucursal(Long sucursal) {
		this.sucursal = sucursal;
	}

	public static List<Gasto> select(){
		return select(Configuracion.getSucursalLocalId());
	}
	
	public static List<Gasto> select(final Long suc){
		SelectorDeGastos selector=new SelectorDeGastos();
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.setSucursal(suc);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return new ArrayList<Gasto>(selector.getSelectedList());
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				System.out.println("Gastos: "+select(3L));
				System.exit(0);
			}

		});
	}

}
