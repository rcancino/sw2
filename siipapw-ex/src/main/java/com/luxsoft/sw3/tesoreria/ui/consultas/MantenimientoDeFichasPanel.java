package com.luxsoft.sw3.tesoreria.ui.consultas;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingWorker;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;


public class MantenimientoDeFichasPanel extends FilteredBrowserPanel<Ficha>{
	

	@SuppressWarnings("unchecked")
	public MantenimientoDeFichasPanel() {
		super(Ficha.class);
		
		addProperty("origen","fecha","ingreso.fecha","ingreso.fechaDeposito","sucursal.nombre","folio","total"
				,"cuenta","tipoDeFicha"
				,"corte","ingreso.id","comentario","cancelada");
		addLabels("Origen","Fecha","Fecha Tes","Fecha Dep","Suc","Folio","Total"
				,"Cuenta","Tipo(Ficha)"
				,"Corte","Ingreso","Comentario","Cancelada");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Folio", "folio");
		
		
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Total", "total");
		installTextComponentMatcherEditor("Tipo Ficha", "tipoDeFicha");
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
	protected List<Ficha> findData() {
		String hql="from Ficha f where f.fecha " +
				" between ? and ?  and f.ingreso!=null";
		Object[] values={periodo.getFechaInicial(),periodo.getFechaFinal()};
		return ServiceLocator2.getHibernateTemplate().find(hql, values);
		
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
				,addAction(null, "cambiarFechaDeDeposito", "Cambiar fecha de deposito")
				};
		return actions;
	}
	
	public void cambiarFechaDeDeposito(){
		Ficha ca=(Ficha)getSelectedObject();
		if(ca!=null){
			String pattern="Cambiar la fecha deposito " +
					"de la Ficha {0} ";
			String msg=MessageFormat.format(pattern, ca.getFolio());
			if(MessageUtils.showConfirmationMessage(msg, "Cambio de fecha")){
				Date fecha=SelectorDeFecha.seleccionar();
				if(fecha!=null){
					CargoAbono pago =ca.getIngreso();
					pago.setFechaDeposito(fecha);
					int index=source.indexOf(ca);
					ServiceLocator2.getUniversalDao().save(pago);
					ca=(Ficha)ServiceLocator2.getUniversalDao().get(Ficha.class, ca.getId());
					source.set(index, ca);
				}
			}
		}
	}

}
