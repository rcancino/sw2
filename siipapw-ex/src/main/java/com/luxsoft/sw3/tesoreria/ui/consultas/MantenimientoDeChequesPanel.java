package com.luxsoft.sw3.tesoreria.ui.consultas;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingWorker;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.tesoreria.TESORERIA_ROLES;

public class MantenimientoDeChequesPanel extends FilteredBrowserPanel<CargoAbono>{
	

	@SuppressWarnings("unchecked")
	public MantenimientoDeChequesPanel() {
		super(CargoAbono.class);
	
		
		addProperty(
				"id"
				,"aFavor"
				,"cuenta.banco.clave"
				,"cuenta.numero"
				,"fecha"
				,"fechaCobro"
				,"concep"	
				//,"descripcion"				
				,"importe"
				,"origen.name"
				,"referencia"
				,"requisicion.id"
				,"formaDePago"
				);
		addLabels(
				"id"
				,"A Favor"
				,"Banco"
				,"Cuenta"
				,"Fecha"
				,"Fecha Cobro"
				,"Concepto"	
				//,"Descripcion"				
				,"Importe"
				,"Origen"
				,"Referencia"
				,"Req"
				,"F.P"
				);
		
		installTextComponentMatcherEditor("Id", "id");
		installTextComponentMatcherEditor("Sucursal", "aFavor");
		installTextComponentMatcherEditor("Importe", "importe");
		installTextComponentMatcherEditor("Folio", "referencia");
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.periodoDeloquevaDelMes();
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {		
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected List<CargoAbono> findData() {
		String hql="select a from CargoAbono a left join a.requisicion r" +
				" where a.fecha between ? and ? " +
				" and r.id >0 "+
				//" and a.formaDePago=\'CHEQUE\' " +
				" and a.importe<0" 
				;
				
		return ServiceLocator2.getHibernateTemplate()
		.find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	public void open(){
		load();
	}
	
	@SuppressWarnings("unchecked")
	public Action[] getActions(){
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addAction(TESORERIA_ROLES.CONTROL_DE_INGRESOS.getId(), "cambiarFechaDeCobroDeCheque", "Cambiar fecha de cobro")
				,addAction(TESORERIA_ROLES.CONTROL_DE_INGRESOS.getId(), "cancelarFechaDeCobroDeCheque", "Cancelar fecha de cobro")
				};
		return actions;
	}
	
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		return procesos;
	}
	
	@SuppressWarnings("unchecked")
	public void salvar(final CargoAbono bean){
		//System.out.println("Salvando el abono: "+bean);
		//bean.setEntregado(bean.getEntregadoFecha()!=null);
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
	
	
	public void cambiarFechaDeCobroDeCheque(){
		CargoAbono ca=(CargoAbono)getSelectedObject();
		if(ca!=null){
			String pattern="Cambiar la fecha cobro " +
					"del Cargo/Abono {0} ";
			String msg=MessageFormat.format(pattern, ca.getId());
			if(MessageUtils.showConfirmationMessage(msg, "Cambio de fecha")){
				Date fecha=SelectorDeFecha.seleccionar();
				if(fecha!=null){
					int index=source.indexOf(ca);
					ServiceLocator2.getIngresosManager().correccionDeFechaCobro(ca,fecha);
					ca=ServiceLocator2.getCargoAbonoDao().get(ca.getId());
					source.set(index, ca);
					//setSelected(ca);
				}
			}
		}
	}
	
	public void cancelarFechaDeCobroDeCheque(){
		CargoAbono ca=(CargoAbono)getSelectedObject();
		if(ca!=null){
			String pattern="Cancelar fecha cobro " +
					"del Cargo/Abono {0} ";
			String msg=MessageFormat.format(pattern, ca.getId());
			if(MessageUtils.showConfirmationMessage(msg, "Cambio de fecha")){
				int index=source.indexOf(ca);
				ServiceLocator2.getIngresosManager().correccionDeFechaCobro(ca,null);
				ca=ServiceLocator2.getCargoAbonoDao().get(ca.getId());
				source.set(index, ca);
			}
		}
	}

}
