package com.luxsoft.siipap.pos.ui.selectores;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.ventas.model.Asociado;
import com.luxsoft.sw3.services.Services;

public class SelectorDeAsociados extends AbstractSelector<Asociado>{

	public SelectorDeAsociados() {
		super(Asociado.class, "Catálogo de socios de la Unión de crédito");
		
	}

	@Override
	protected List<Asociado> getData() {
		String hql="from Asociado a left join fetch a.cliente c left join fetch a.vendedor v";
		return Services.getInstance().getHibernateTemplate().find(hql);
	}

	@Override
	protected TableFormat<Asociado> getTableFormat() {
		return GlazedLists.tableFormat(Asociado.class
				,new String[]{"clave","nombre","cliente.nombre"}
				,new String[]{"Socio","Nombre","Cliente"}
		);
	}
	
	
	public static Asociado seleccionar(){
		final SelectorDeAsociados selector=new SelectorDeAsociados();
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
