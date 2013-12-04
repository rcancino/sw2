package com.luxsoft.siipap.inventario;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;

public class MovimientoFormModel extends MasterDetailFormModel{

	public MovimientoFormModel() {
		super(Movimiento.class);
	}
	
	public MovimientoFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public Movimiento getMovimiento(){
		return (Movimiento)getBaseBean();
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getMovimiento().getPartidas().isEmpty()){
			support.addError("partidas", "No se puede salvar el movimiento sin partidas");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object insertDetalle(Object obj) {
		MovimientoDet det=(MovimientoDet)obj;
		boolean res=getMovimiento().agregarPartida(det);
		if(res){
			source.add(det);
			return true;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected void init(){
		super.init();
		if(getMovimiento().getId()!=null){
			System.out.println("Insertando Partidas");
			for (MovimientoDet det:getMovimiento().getPartidas()){
				source.add(det);
			}
		}
		
	}


	@Override
	public boolean deleteDetalle(Object obj) {
		if(!isReadOnly()){
			MovimientoDet det=(MovimientoDet)obj;
			boolean res= getMovimiento().eliminarPartida(det);
			if(res)
				source.remove(det);
			return res;
		}
		return false;
	}
	

}
