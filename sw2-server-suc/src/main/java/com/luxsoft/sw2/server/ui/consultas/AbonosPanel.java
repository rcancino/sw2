package com.luxsoft.sw2.server.ui.consultas;

import javax.swing.Action;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.model.Periodo;


/**
 * Consulta para la tareas de replicación de abonos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AbonosPanel extends DefaultSucursalReplicaPanel<Abono>{

	public AbonosPanel() {
		super(Abono.class);
	}

	protected void init(){
		
		addProperty(
				"sucursal.nombre"
				,"tipo"
				,"clave"
				,"nombre"
				,"fecha"
				,"total"
				,"Tipo"
				,"id"
				);
		addLabels(
				"Sucursal"
				,"Tipo"
				,"Cliente"
				,"Nombre"
				,"Fecha"
				,"Total"
				,"Info"
				,"Id"
				);	
		installTextComponentMatcherEditor("Cliente", "clave","nombre");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Info", "info");
		installTextComponentMatcherEditor("Total", "total");
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
		periodo=Periodo.getPeriodo(-2);
	}

}
