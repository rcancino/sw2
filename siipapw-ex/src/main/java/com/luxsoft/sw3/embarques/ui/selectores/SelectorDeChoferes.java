package com.luxsoft.sw3.embarques.ui.selectores;

import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.embarque.ChoferFacturista;


public class SelectorDeChoferes extends AbstractSelector<Chofer>{
	
	private ChoferFacturista facturista;

	public SelectorDeChoferes() {
		super(Chofer.class, "Catálogo de choferes");
	}

	@Override
	protected List<Chofer> getData() {
		if(getFacturista()!=null){
			String hql="from Chofer c where c.facturista is  null  and c.suspendido=false";
			List r1=ServiceLocator2.getHibernateTemplate().find(hql);
			String hql1="from Chofer c where c.facturista.id!=? and c.suspendido=false";
			List r2=ServiceLocator2.getHibernateTemplate().find(hql1,getFacturista().getId());
			r1.addAll(r2);
			return r1;
		}else{
			String hql="from Chofer c where c.suspendido=false";
			return ServiceLocator2.getHibernateTemplate().find(hql);
		}
		
	}

	@Override
	protected TableFormat<Chofer> getTableFormat() {
		String props[]={"facturista.id","id","nombre","radio","rfc"};
		String names[]={"Facturista","Id","Nombre","Radio","RFC"};
		return GlazedLists.tableFormat(Chofer.class, props,names);
	}

	@Override
	protected TextFilterator<Chofer> getBasicTextFilter() {
		return GlazedLists.textFilterator("id","nombre");
	}	
	
	
	
	public ChoferFacturista getFacturista() {
		return facturista;
	}

	public void setFacturista(ChoferFacturista facturista) {
		this.facturista = facturista;
	}
	
	public static Chofer buscar(){
		return buscar(null);
	}

	public static Chofer buscar(final ChoferFacturista facturista){
		SelectorDeChoferes selector=new SelectorDeChoferes();		
		selector.setFacturista(facturista);
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			Chofer selected=selector.getSelected();
			return selected;
		}
		return null;
	}
	
	public static List<Chofer> seleccionar(final ChoferFacturista facturista){
		SelectorDeChoferes selector=new SelectorDeChoferes();		
		selector.setFacturista(facturista);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			return selector.getSelectedList();
		}
		return ListUtils.EMPTY_LIST;
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
				Chofer res=buscar();
				System.out.println(ToStringBuilder.reflectionToString(res));
				System.exit(0);
			}
		});
	}

}
