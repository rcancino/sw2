package com.luxsoft.siipap.service.gastos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.gastos.GCompraDao;
import com.luxsoft.siipap.dao.tesoreria.CargoAbonoDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraRow;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GeneradorDePolizaDeGastos;
import com.luxsoft.siipap.model.gastos.GeneradorDePolizaDePagos;
import com.luxsoft.siipap.model.gastos.GeneradorDePolizaDePagos2;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.impl.GenericManagerImpl;
import com.luxsoft.siipap.util.DateUtil;

public class ComprasDeGastosManagerImpl extends GenericManagerImpl<GCompra, Long> implements ComprasDeGastosManager{
	
	private CargoAbonoDao cargoAbonoDao;

	public ComprasDeGastosManagerImpl(GCompraDao compraDao) {
		super(compraDao);
	}
	
	@Override
	public GCompra save(GCompra object) {
		//object.actualizar();
		object.actualizarSaldoDeFacturas();
		if(!object.getFacturas().isEmpty()){
			GFacturaPorCompra fac=object.getFacturas().iterator().next();
			fac.setImporte(object.getImpuestoEnCantidadMonetaria());
			fac.setImpuesto(object.getImpuestoEnCantidadMonetaria());
			fac.actualizarImportes();
			fac.actualizarSaldo();
			/*
			if(!fac.getRequisiciones().isEmpty()){
				for(RequisicionDe det:fac.getRequisiciones()){
					det.setImporte(importe)
					det.setImpuesto(impuesto);
				}
			}
			*/
		}
		return super.save(object);
	}



	public List<GCompraRow> buscarComprasRow() {
		return getCompraDao().buscarComprasRow();
	}
	
	public List<GCompraRow> buscarComprasRow(Periodo p) {
		return getCompraDao().buscarComprasRow(p);
	}

	private GCompraDao getCompraDao(){
		return ((GCompraDao)genericDao);
	}

	
	@Override
	public GCompra get(Long id) {		
		return super.get(id);
	}


	@Transactional(propagation=Propagation.REQUIRED)
	public Poliza generarPolizaDeProvision(final Periodo periodo){
		List<GFacturaPorCompra> facs=getCompraDao().buscarFacturasConstaldo(periodo);
		if(facs.isEmpty())
			return null;
		GeneradorDePolizaDeGastos g=new GeneradorDePolizaDeGastos();
		return g.generar(facs);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public List<Poliza> generarPolizaDePagos(Date fecha) {
		List<CargoAbono> pagos=getCargoAbonoDao().buscarPagos(fecha);
		final List<Poliza> polizas=new ArrayList<Poliza>();
		GeneradorDePolizaDePagos g=new GeneradorDePolizaDePagos();
		for(CargoAbono ca:pagos){
			System.out.println("Procesando: "+ca);
			Poliza p=g.generaPolizaDePagos(ca);
			polizas.add(p);
		}
		return polizas;
	}

	
	@Transactional(propagation=Propagation.REQUIRED)
	public List<Poliza> generarPolizaDePagos2(Date fecha) {
		List<CargoAbono> pagos=getCargoAbonoDao().buscarPagos(fecha);
		final List<Poliza> polizas=new ArrayList<Poliza>();
		GeneradorDePolizaDePagos2 g=new GeneradorDePolizaDePagos2();
		for(CargoAbono ca:pagos){
			Poliza p=g.generaPolizaDePagos(ca);
			polizas.add(p);
		}
		return polizas;
	}

	public CargoAbonoDao getCargoAbonoDao() {
		return cargoAbonoDao;
	}

	public void setCargoAbonoDao(CargoAbonoDao cargoAbonoDao) {
		this.cargoAbonoDao = cargoAbonoDao;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public GFacturaPorCompra salvarFactura(GFacturaPorCompra fac) {
		fac.actualizarImportes();
		fac.actualizarSaldo();
		return getCompraDao().salvarFactura(fac);
		
	}
	
	public static void main(String[] args) {
		/*
		Periodo p=Periodo.periodoDeloquevaDelMes();
		for(Date d:p.getListaDeDias()){
			ServiceLocator2.getComprasDeGastosManager().generarPolizaDeGastos(d);
		}
		*/
		ServiceLocator2.getComprasDeGastosManager().generarPolizaDePagos(DateUtil.toDate("02/06/2008"));
		
	}

	
	

}
