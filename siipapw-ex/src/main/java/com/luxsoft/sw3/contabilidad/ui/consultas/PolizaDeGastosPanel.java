package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.util.Date;
import java.util.List;

import javax.swing.Action;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.ui.consultas2.PolizaDeGastosController2;

public class PolizaDeGastosPanel extends PanelGenericoDePolizasMultiples{
	
	private PolizaDeGastosController2 controller;
	
	public PolizaDeGastosPanel() {
		super();
		setClase("Gastos");
		controller=new PolizaDeGastosController2();
	}

	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			CuentaContable cuenta =det.getCuenta(); 
			String desc=det.getDescripcion2();
			String asiento=det.getAsiento();
		}
	}

	@Override
	public List<Poliza> generarPolizas(Date fecha) {
		return controller.generaPoliza(fecha);
	}
	
	@Override
	public Poliza salvar(Poliza poliza){
		Poliza existente=controller.existente(poliza);
		if(existente!=null){
			if(MessageUtils.showConfirmationMessage("Poliza para el pago: "+existente.getReferencia()+ "Ya existe, desea actualizarla", "Acutalizar poliza")){
				controller.actualizar(existente);
				return ServiceLocator2.getPolizasManager().salvarPoliza(existente);
			}
			return poliza;
		}else{
			return ServiceLocator2.getPolizasManager().salvarPoliza(poliza);
		}
	}	
	
	public void actualizar(){
		Poliza pol=(Poliza)getSelectedObject();
		if(pol!=null && (pol.getId()!=null) ){
			int index=source.indexOf(pol);
			if(index!=-1){				
				Poliza res=controller.actualizar(pol);
				res=ServiceLocator2.getPolizasManager().salvarPoliza(res);
				source.set(index, res);
				setSelected(res);
			}			
		}
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()				
				,getInsertAction()
				,getDeleteAction()
				,CommandUtils.createPrintAction(this, "imprimirPoliza")
				,addAction(null, "salvar", "Salvar póliza")
				,addAction(null,"actualizar","Actualiza póliza")
												};
		return actions;
	}

}
