package com.luxsoft.siipap.gastos.operaciones;

import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.DatePickerCellEditor;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.gastos.GasActions;
import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.TaskUtils;

public class ChequesPanel extends FilteredBrowserPanel<CargoAbono>{
	
	
	private JXDatePicker fechaInicialPicker;
	private JXDatePicker fechaFinalPicker;
	private Action salvarAction;
	private ObservableElementList<CargoAbono> olist;

	@SuppressWarnings("unchecked")
	public ChequesPanel() {
		super(CargoAbono.class);
		Periodo periodo=Periodo.periodoDeloquevaDelMes();
		addProperty("id"
				,"cuenta.banco.clave"
				,"requisicion.origen"
				,"fecha"
				,"aFavor"
				,"importe"
				,"referencia"
				,"liberado"
				,"entregado"
				,"entregadoFecha"
				,"comentario2"
				,"requisicion.comentario");
		addLabels("Id"				
				,"Banco"
				,"Origen"
				,"Fecha"
				,"Beneficiario"
				,"Importe"
				,"Cheque"
				,"Liberado"
				,"Entregado"
				,"Entregado F."
				,"Comentario"
				,"Coment (Req)");
		
		installTextComponentMatcherEditor("A Favor", "aFavor");		
		//installTextComponentMatcherEditor("Cuenta", "cuenta.numero");
		installTextComponentMatcherEditor("Banco", "cuenta.banco.clave","cuenta.banco.nombre");
		installTextComponentMatcherEditor("Coment (Req)", "requisicion.comentario");
		installTextComponentMatcherEditor("Origen", "requisicion.origen");
		fechaFinalPicker=new JXDatePicker();
		fechaFinalPicker.setDate(periodo.getFechaFinal());
		fechaInicialPicker=new JXDatePicker();
		fechaInicialPicker.setDate(periodo.getFechaInicial());
		fechaFinalPicker.setFormats("dd/MM/yyyy");
		fechaInicialPicker.setFormats("dd/MM/yyyy");
	}
	
	
	@Override
	protected TableFormat buildTableFormat() {
		boolean[] edits=getEdiciones();
		return GlazedLists.tableFormat(beanClazz,getProperties(), getLabels(),edits);
	}
	
	public boolean[] getEdiciones(){
		boolean r=true;
		if(KernellSecurity.instance().isSecurityEnabled())
			r=KernellSecurity.instance().isResourceGranted(GasActions.LiberarCheques.getId(),Modulos.GASTOS);
		boolean[] edits={false,false,false,false,false,false,false
				,r
				,false
				,r
				,r
				,false};
		return edits;
	}
	
	@Override
	protected EventList getSourceEventList() {
		EventList<CargoAbono> list=new BasicEventList<CargoAbono>();
		Connector<CargoAbono> conn=GlazedLists.beanConnector(CargoAbono.class);
		olist=new ObservableElementList<CargoAbono>(list,conn);
		olist.addListEventListener(new ListHandler());
		return olist;
	}
	
	protected void adjustMainGrid(final JXTable grid){
		DatePickerCellEditor editor=new DatePickerCellEditor(new SimpleDateFormat("dd/MM/yyyy"));
		grid.setDefaultEditor(Date.class, editor);
		grid.getColumnExt("Id").setVisible(false);
	}
	
	public void close(){
		source.clear();
	}


	protected void installCustomComponentsInFilterPanel(DefaultFormBuilder builder){
		builder.append("Fecha Ini",fechaInicialPicker);
		builder.append("Fecha Fin",fechaFinalPicker);
	}

	@Override
	protected List<CargoAbono> findData() {
		Periodo periodo=new Periodo(fechaInicialPicker.getDate(),fechaFinalPicker.getDate());
		return ServiceLocator2.getCargoAbonoDao().buscarEgresos(periodo);
	}
	
	
	@SuppressWarnings("unchecked")
	public Action[] getActions(){
		if(actions==null)
			actions=new Action[]{
				getLoadAction(),getViewAction()};
		return actions;
	}
	
	public Action getSalvarAction(){
		if(salvarAction==null){
			salvarAction=new AbstractAction("Salvar"){
				public void actionPerformed(ActionEvent e) {
					
				}				
			};
		}
		return salvarAction;
	}
	
	@SuppressWarnings("unchecked")
	public void salvar(final CargoAbono bean){
		bean.setEntregado(bean.getEntregadoFecha()!=null);
		if(bean.isEntregado())
			bean.setLiberado(true);
		try {
			CargoAbono next=ServiceLocator2.getCargoAbonoDao().save(bean);
			int index=source.indexOf(bean);
			source.set(index, next);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
		
	protected void executeLoadWorker(final SwingWorker worker){
		//worker.execute();
		TaskUtils.executeSwingWorker(worker);
	}
	
	private class ListHandler implements ListEventListener<CargoAbono>{

		public void listChanged(ListEvent<CargoAbono> listChanges) {
			if(listChanges.next()){
				if(listChanges.getType()==ListEvent.UPDATE){
					CargoAbono bean=listChanges.getSourceList().get(listChanges.getIndex());
					salvar(bean);
				}
			}
			
		}
		
	}

}
