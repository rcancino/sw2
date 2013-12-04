package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.swing.Action;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.gastos.operaciones.OCompraForm;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.ui.consultas.PanelGenericoDePolizasMultiples;


public class PolizaDeCobroChequeTransitoPanel extends PanelGenericoDePolizasMultiples{
	
	private PolizaDeCobroDeChequeEnTransitoController controller;
	
	public PolizaDeCobroChequeTransitoPanel() {
		super();
		setClase("COBRO CHEQUE TRANSITO");
		controller=new PolizaDeCobroDeChequeEnTransitoController();
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
	
	@Override
	public void drill(PolizaDet det) {
		Poliza poliza=(Poliza)getSelectedObject();
		if(poliza!=null){
			final Long pagoId=new Long(poliza.getReferencia());
			System.out.println("Buscando comprra de gastos para cargoabono: "+pagoId);
			GCompra compra=findCompra(pagoId);
			if(compra!=null){
				OCompraForm.showForm(compra, true);
			}
		}
	}
	
	private GCompra findCompra(final Long pagoId){
		return (GCompra)getHibernateTemplate().execute(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				CargoAbono c=(CargoAbono)session.get(CargoAbono.class, pagoId);
				Requisicion requisicion=c.getRequisicion();
				GFacturaPorCompra factura=requisicion
								.getPartidas().iterator().next()
								.getFacturaDeGasto();
				GCompra compra=factura.getCompra();
				compra.getPartidas().iterator().next();				
				return compra;
			}
		});
	}

}
