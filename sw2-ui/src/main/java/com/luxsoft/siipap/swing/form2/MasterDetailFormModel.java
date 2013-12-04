package com.luxsoft.siipap.swing.form2;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;


public class MasterDetailFormModel extends DefaultFormModel {
	
	protected EventList source;
	protected EventList partidasSource;
	
	
	protected Logger logger=Logger.getLogger(getClass());
	

	public MasterDetailFormModel(Class clazz) {
		super(clazz);
	}	
	
	public MasterDetailFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public MasterDetailFormModel(Object bean) {
		super(bean);
	}


	@SuppressWarnings("unchecked")
	protected void init(){
		source=createPartidasList();
		source.addListEventListener(new PartidasHandler());
		partidasSource=createPartidasSource();
		initEventHandling();
	}
	
	protected void initEventHandling(){
		
	}
	
	@SuppressWarnings("unchecked")
	protected EventList createPartidasList(){
		return GlazedLists.threadSafeList(new BasicEventList());
	}
	
	protected EventList createPartidasSource(){
		return GlazedLists.readOnlyList(source);
	}
	
	
	public EventList getPartidas(){		
		return partidasSource;
	}
	
	public Object insertDetalle(final Object obj){
		return null;
	}
	
	/**
	 * Template method para personalizar el comportamiento despues
	 * de agregar una partida 
	 * 
	 * @param partida
	 */
	protected void afeterPartidaInserted(final Object partida){
		
	}
	
	public boolean deleteDetalle(final Object obj){
		return false;
	}
	
	protected void doListChange(){
		validate();
	}
	
	public boolean manejaTotalesEstandares(){
		return true;
	}
	
	public void doListUpdated(ListEvent listChanges){
		
	}
	
	/**
	 * Listener para detectar cambios en el EventList
	 * de la vista de partidas
	 * 
	 * @author Ruben Cancino
	 *
	*/ 
	protected class PartidasHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				switch (listChanges.getType()) {
				case ListEvent.INSERT:
				case ListEvent.DELETE:
					doListChange();
					break;
				case ListEvent.UPDATE:
					doListUpdated(listChanges);
					break;
				default:
					break;
				}				
			}
		}		
	}

}
