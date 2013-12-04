package com.luxsoft.siipap.tesoreria.movimientos;


import java.util.List;

import javax.swing.Action;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXTable;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.dao.legacy.DepositoDao;
import com.luxsoft.siipap.model.legacy.Deposito;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

public class DepositosPanel extends FilteredBrowserPanel<Deposito> {
	
	private JXDatePicker datePicker1;
	private JXDatePicker datePicker2;
	private JXDatePicker datePicker3;
	

	public DepositosPanel() {
		super(Deposito.class);		
		addProperty("id","sucursal","origen","formaDePago","fecha","importe","banco","cuenta","folio","revisada","fechaCobranza");
		datePicker1=new JXDatePicker();
		datePicker1.setFormats("dd/MM/yyyy");
		datePicker2=new JXDatePicker();
		datePicker2.setFormats("dd/MM/yyyy");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Folio", "folio");
		installTextComponentMatcherEditor("Banco", "banco");
		installTextComponentMatcherEditor("Forma P.", "formaDePago");
		datePicker3=new JXDatePicker();
		datePicker3.setFormats("dd/MM/yyyy");
	}

	@Override
	protected EventList getSourceEventList() {		
		EventList res= new ObservableElementList(super.getSourceEventList(),GlazedLists.beanConnector(Deposito.class));
		return res;
	}
	
	

	@Override
	protected JXTable buildGrid() {
		JXTable res= super.buildGrid();
		getFilteredSource().addListEventListener(new Observer());
		return res;		
	}

	@Override
	protected void installCustomComponentsInFilterPanel(
			DefaultFormBuilder builder) {
		builder.append("Fecha Ini",datePicker1);
		builder.append("Fecha Fin",datePicker2);
		builder.appendSeparator();
		builder.append("Fecha Cob.",datePicker3);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction(),getViewAction()};
		return actions;
	}
	
	
	
	
	@Override
	protected TableFormat buildTableFormat() {
		String[] props={"id","sucursal","origen","formaDePago","fecha","importe","banco","cuenta","folio","revisada","fechaCobranza"};
		boolean[] edits={false,false,false,true,false,false,false,false,false,true,false};
		return GlazedLists.tableFormat(Deposito.class, props, props,edits);
	}

	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected List<Deposito> findData() {
		DepositoDao depositoDao=(DepositoDao)ServiceLocator2.instance().getContext().getBean("depositoDao");
		return depositoDao.buscarDepositos(datePicker1.getDate(),datePicker2.getDate());
	}
	
	private class Observer implements ListEventListener<Deposito>{
		public void listChanged(ListEvent<Deposito> listChanges) {
			
			while(listChanges.next()){				
				if(listChanges.getType()==ListEvent.UPDATE){
					int index=listChanges.getIndex();
					Deposito origien=(Deposito)listChanges.getSourceList().get(index);
					origien.setFechaCobranza(origien.isRevisada()?datePicker3.getDate():null);
					if(!origien.getOrigen().equals("CON")){
						origien.setFechaCobranza(origien.getFecha());
					}
					try {
						Deposito target=new Deposito();
						BeanUtils.copyProperties(origien, target);
						DepositoDao depositoDao=(DepositoDao)ServiceLocator2.instance().getContext().getBean("depositoDao");						
						depositoDao.save(target);
						listChanges.getSourceList().set(index, target);
					} catch (Exception e) {
						MessageUtils.showError("Error salvando", e);
					}
					
				}
			}
		}
		
	}
	
	
}
