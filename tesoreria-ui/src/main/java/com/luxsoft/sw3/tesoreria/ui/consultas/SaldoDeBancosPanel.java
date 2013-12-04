package com.luxsoft.sw3.tesoreria.ui.consultas;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingWorker;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.AbstractDialog;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils.TaskDialog;
import com.luxsoft.sw3.tesoreria.model.SaldoDeCuentaBancaria;


public class SaldoDeBancosPanel extends AbstractMasterDatailFilteredBrowserPanel<SaldoDeCuentaBancaria,CargoAbono>{

	public SaldoDeBancosPanel() {
		super(SaldoDeCuentaBancaria.class);		
	}
	protected void agregarMasterProperties(){
		addProperty(
				"id"
				,"cuenta.banco.nombre"
				,"cuenta.numero"				
				,"year"
				,"mes"
				,"saldoInicial"
				,"depositos"
				,"retiros"
				,"saldoFinal"
				,"cuenta.moneda"
				,"cierre"
				);
		addLabels(
				"Folio"
				,"Banco"
				,"Cuenta"				
				,"year"
				,"mes"
				,"Saldo Inicial"
				,"Depositos"
				,"Retiros"
				,"Saldo Final"
				,"Moneda"
				,"Cierre"
				);
		setDefaultComparator(GlazedLists.beanPropertyComparator(SaldoDeCuentaBancaria.class, "cuenta.banco.nombre"));
		installTextComponentMatcherEditor("Banco", "cuenta.banco.nombre");
		installTextComponentMatcherEditor("Cuenta", "cuenta.numero");
		manejarPeriodo();		
	}
	
	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={"id","cuenta.cuentaDesc","fecha","clasificacion","importe","referencia","comentario"};
		String names[]={"Folio","Cuenta","Fecha","Clase","Importe","Ref","Comentario"};
		return GlazedLists.tableFormat(CargoAbono.class, props, names);
	}
	
	@Override
	protected Model<SaldoDeCuentaBancaria, CargoAbono> createPartidasModel() {
		return new Model<SaldoDeCuentaBancaria, CargoAbono>(){
			public List<CargoAbono> getChildren(SaldoDeCuentaBancaria parent) {
				Object[] params=new Object[]{parent.getCuenta().getId(),periodo.getFechaInicial(),periodo.getFechaFinal()};
				//return getHibernateTemplate().find("from CargoAbono c where c.cuenta.id=? and c.fecha between ? and ?",params);
				return ListUtils.EMPTY_LIST;
			}
		};
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				,addAction(null,"actualizar","Actualizar saldos")
				,addAction(null,"cierre","Cierre de ejercicio")
				//,getInsertAction()
				//,getDeleteAction()
				};
		}
		return actions;
	}
	
	public void actualizar(){
		if(getSelected().isEmpty()){
			if(MessageUtils.showConfirmationMessage("Actualizar los saldos de todas las cuentas ?", "Cuentas bancarias")){
				doActualizar();
			}else{
				doActualizar();
			}
		}
	}
	
	private void doActualizar(){
		
		SwingWorker<List<String>, SaldoDeCuentaBancaria> worker=new SwingWorker<List<String>, SaldoDeCuentaBancaria>(){
			@Override
			protected List<String> doInBackground() throws Exception {
				if(!getSelected().isEmpty()){
					
				}else{
					List<Cuenta> cuentas=ServiceLocator2.getTesoreriaManager().buscarCuentas();
					logger.info("Cuentas a actualizar: "+cuentas.size());
					for(Cuenta c:cuentas){
						try {
							SaldoDeCuentaBancaria saldo=ServiceLocator2.getTesoreriaManager().actualizarSaldo(c, getYear(), getMes());
							logger.info("Saldo actualizado: "+saldo);
							publish(saldo);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				return null;
			}
			@Override
			protected void process(List<SaldoDeCuentaBancaria> chunks) {
				for(SaldoDeCuentaBancaria s:chunks){
					getWorkerDialog().setHeaderDesc("Saldo actualizado para : "+s.getCuenta()+ " Nvo saldo: "+s.getSaldoFinal());
				}
			}
			@Override
			protected void done() {
				detailSelectionModel.clearSelection();
				load();
			}
		};
		worker.addPropertyChangeListener(workerHandler);
		TaskUtils.executeSwingWorker(worker);
	}
	
	public void cierre(){
		if(MessageUtils.showConfirmationMessage("El cierre de operaciones para una cuenta o periodo es irreversible" , "Saldos de cuentas")){
			
		}
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {		
		TaskUtils.executeSwingWorker(worker);
	}
	
	public int getYear(){
		return Periodo.obtenerYear(periodo.getFechaFinal());
	}
	public int getMes(){
		return Periodo.obtenerMes(periodo.getFechaFinal())+1;
	}

	@Override
	protected List<SaldoDeCuentaBancaria> findData() {
		String hql="from SaldoDeCuentaBancaria c where c.year=? and c.mes=?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					getYear()
					,getMes()
					}
		);
	}
	
	public void open(){
		load();
	}
	@Override
	public void cambiarPeriodo() {
		final ValueHolder yearModel=new ValueHolder(getYear());
		final ValueHolder mesModel=new ValueHolder(getMes());
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=Periodo.getPeriodoEnUnMes(mesModel.intValue()-1, yearModel.intValue());
			nuevoPeriodo(periodo);
			updatePeriodoLabel();
		}
	}
	
	public TaskDialog workerDialog;
	
	public TaskDialog getWorkerDialog(){
		if(workerDialog==null){
			workerDialog=new TaskUtils.TaskDialog();
		}
		return workerDialog;
	}
	
	private WorkerHandler workerHandler=new WorkerHandler();
	
	private class WorkerHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.PENDING.equals(evt.getNewValue())){
				getWorkerDialog().open();
			}else if("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.STARTED.equals(evt.getNewValue())){
				getWorkerDialog().setHeaderTitle("Cargando información");
			}else if("state".equals(evt.getPropertyName()) &&SwingWorker.StateValue.STARTED.equals(evt.getNewValue())){
				getWorkerDialog().close();
			}
			
		}
	}
	
	
}
