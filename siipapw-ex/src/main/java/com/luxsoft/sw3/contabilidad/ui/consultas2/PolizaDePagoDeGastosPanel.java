package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.gastos.operaciones.OCompraForm;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeAnticiposPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeCortesDeTarjetaPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeFichasDeDepositoPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeIngresosPorDepositosAutorizadosPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeNotasDeCreditoPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeOtrosGastosPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeOtrosProductosPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisDeVentasPanel;
import com.luxsoft.sw3.contabilidad.ui.consultas.AnalisisSaldosAFavorPanel;


public class PolizaDePagoDeGastosPanel extends PolizaDinamicaPanel{

	public PolizaDePagoDeGastosPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	/*
	@Override
	protected void agregarMasterProperties() {
		addProperty("folio","tipo","descripcion","fecha","referencia","debe","haber","cuadre","clase","id");
		addLabels("Folio","Tipo","Descripción","Fecha","Referencia","Debe","Haber","Cuadre","Clase","Id");
		installTextComponentMatcherEditor("Descripción", "descripcion");
		manejarPeriodo();
	}*/
	@Override
	protected void afterGridCreated() {
		
		super.afterGridCreated();
		grid.getColumnExt("Id").setVisible(false);
		grid.getColumnExt("Clase").setVisible(false);
	}

	public void generar(){
		Date fecha=SelectorDeFecha.seleccionar();
		if(fecha!=null){
			Periodo periodo=Periodo.getPeriodoDelMesActual(fecha);
			System.out.println("Periodo: "+fecha);
			List<Poliza> res=controller.generar(periodo);
			super.insertarPolizas(res);
		}
	}
	@Override
	protected void doSelect(Object bean) {		
		Poliza poliza=(Poliza)getSelectedObject();
		if(poliza!=null){
			final Long pagoId=new Long(poliza.getReferencia());
			GCompra compra=findCompra(pagoId);
			if(compra!=null){
				OCompraForm.showForm(compra, true);
			}
			
		}
	}
	
	@Override
	public void drill(PolizaDet det) {
		if(det!=null){
			
			CuentaContable cuenta =det.getCuenta();
			if(cuenta!=null && cuenta.getClave().equals("600")  ){
				Poliza poliza=(Poliza)getSelectedObject();
				if(poliza!=null){
					final Long pagoId=new Long(poliza.getReferencia());
					GCompra compra=findCompra(pagoId);
					if(compra!=null){
						OCompraForm.showForm(compra, true);
					}
					
				}
			}if(cuenta!=null && cuenta.getClave().equals("212")  ){
				Poliza poliza=(Poliza)getSelectedObject();
				if(poliza!=null){
					final Long pagoId=new Long(poliza.getReferencia());
					GCompra compra=findCompra(pagoId);
					if(compra!=null){
						OCompraForm.showForm(compra, true);
					}
					
				}
			}else if(det.getDescripcion2().startsWith("FAC:")){
				Poliza poliza=det.getPoliza();
				final Long pagoId=new Long(poliza.getReferencia());
				GCompra compra=findCompra(pagoId);
				if(compra!=null){
					OCompraForm.showForm(compra, true);
				}
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
	
	@Override
	protected List<Poliza> findData() {
		String hql="from Poliza p " +
				" where p.clase=? " +
				"  and p.fecha between ? and ?" +
				"  and p.descripcion not like ?";
		Object[] params={getClase(),periodo.getFechaInicial(),periodo.getFechaFinal(),"COMPRAS:%"};
		return ServiceLocator2
			.getHibernateTemplate()
			.find(hql,params);
	}
	
	
}
