package com.luxsoft.siipap.pos.ui.selectores;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.CheckPlusOpcion;

public class SelectorDeCheckplus extends AbstractSelector<CheckPlusOpcion>{

	public SelectorDeCheckplus() {
		super(CheckPlusOpcion.class, "Opciones de pago con Check Plus");
		
	}

	@Override
	protected List<CheckPlusOpcion> getData() {
		String hql="from CheckPlusOpcion order by plazo asc";
		return Services.getInstance().getHibernateTemplate().find(hql);
	}

	@Override
	protected TableFormat<CheckPlusOpcion> getTableFormat() {
		return GlazedLists.tableFormat(CheckPlusOpcion.class
				,new String[]{"plazo","cargo","comentario"}
				,new String[]{"Plazo","Cargo","Comentario"}
		);
	}
	
	
	public static CheckPlusOpcion seleccionar(){
		final SelectorDeCheckplus selector=new SelectorDeCheckplus();
		selector.open();
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				System.out.println(ToStringBuilder.reflectionToString(seleccionar()));
				System.exit(0);
			}

		});
	}

}
