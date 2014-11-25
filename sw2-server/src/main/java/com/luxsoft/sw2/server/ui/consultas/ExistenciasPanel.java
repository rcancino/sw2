package com.luxsoft.sw2.server.ui.consultas;

import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingWorker;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.utils.TaskUtils;


/**
 * Consulta base para el mantenimiento de inventarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ExistenciasPanel extends DefaultCentralReplicaPanel<Existencia>{

	public ExistenciasPanel() {
		super(Existencia.class);
	}

	protected void init(){
		
		addProperty(
				"sucursal.nombre"
				,"year"
				,"mes"
				,"clave"
				,"descripcion"
				,"unidad"
				,"nacional"
				,"kilos"
				,"cantidad"
				,"recorte"
				,"costoPromedio"
				,"costoAPromedio"
				,"producto.linea.nombre"
				,"producto.clase.nombre"
				,"producto.marca.nombre"
				);
		addLabels(
				"Sucursal"
				,"Año","Mes"
				,"Producto"
				,"Desc"
				,"U"
				,"Nac"
				,"Kilos"
				,"Cantidad"
				,"Recorte"
				,"Promedio"
				,"Costo P"				
				,"L�nea"
				,"Clase"
				,"Marca"
				);	
		installTextComponentMatcherEditor("Producto", "producto.clave","producto.descripcion");		
		installTextComponentMatcherEditor("Linea", "producto.linea.nombre");
		installTextComponentMatcherEditor("Clase", "producto.clase.nombre");
		installTextComponentMatcherEditor("Marca", "producto.marca.nombre");
		installTextComponentMatcherEditor("Existencia", "existencia");		
		manejarPeriodo();
		
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addAction(null, "recalcular", "Recalcular")
				,addAction(null,"replicar","Replicar")
				,addAction(null,"replicaBatch","Replica batch")
				};
		return actions;
	}
	
	@Override
	protected void manejarPeriodo() {
		periodo=Periodo.getPeriodoDelMesActual(new Date());
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
	
	final DateFormat df=new SimpleDateFormat("MMMM - yyyy");
	
	protected void updatePeriodoLabel(){
		periodoLabel.setText(df.format(periodo.getFechaFinal()));
	}
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){
			manejarPeriodo();
			periodoLabel=new ActionLabel(df.format(periodo.getFechaFinal()).toUpperCase());
			periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	
	@Override
	public void open() {
		load();
	}
	
	public void recalcular(){
		final SwingWorker worker=new SwingWorker(){				
			protected Object doInBackground() throws Exception {
				ServiceLocator2.getExistenciaDao().actualizarExistencias(getSecurityId(), getYear(), getMes());
				return "OK";
			}
			@Override
			protected void done() {
				try{
					get();
					load();					
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		TaskUtils.executeSwingWorker(worker,"Recalculando existencias","Existencias");
	}
	
	
	
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected List<Existencia> findData() {
		String hql="from Existencia e where e.year=? and e.mes=?  ";
		return ServiceLocator2.getHibernateTemplate().find(hql, new Object[]{getYear(),getMes()});
	}
	
	
	public Integer getMes(){
		return Periodo.obtenerMes(periodo.getFechaFinal())+1;
	}
	public Integer getYear(){
		return Periodo.obtenerYear(periodo.getFechaFinal());
	}
	
}	

