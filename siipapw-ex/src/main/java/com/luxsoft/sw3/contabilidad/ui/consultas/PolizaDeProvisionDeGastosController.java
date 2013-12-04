package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.contabilidad.GeneradorDePoliza;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.Poliza.Tipo;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.AbstractPolizaManager;
import com.luxsoft.sw3.contabilidad.services.PolizaContableManager;

/**
 * Implementacion de {@link GeneradorDePoliza} para crear la o las polizas
 * de gastos a partir de Facturas de gastos
 * 
 * @author Ruben Cancino 
 *
 */
public class PolizaDeProvisionDeGastosController extends AbstractPolizaManager{
	

	@Override
	protected void procesarPoliza() {
	}

	@SuppressWarnings("unchecked")
	public Poliza generar(final Periodo periodo) {
		
		Poliza p=(Poliza)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				final List<GFacturaPorCompra> facs=session.createQuery(
						"from GFacturaPorCompra fac where fac.fecha between ? and ? "
						//" and fac.requisiciones.requisicion.conceptoid !=737332"
						
						)
						.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
						.setParameter(1, periodo.getFechaFinal(),Hibernate.DATE)
						.list();
				ListIterator<GFacturaPorCompra> iter=facs.listIterator();
				
				List<GFacturaPorCompra> facturas=new ArrayList<GFacturaPorCompra>();
				
				while(iter.hasNext()){
					GFacturaPorCompra ff=iter.next();
					
					// si esta saldado no se provisiona
					double saldo=ff.getSaldoCalculadoAlCorte(periodo.getFechaFinal()).amount().doubleValue();
					if(saldo<=0){
						//iter.remove();
						continue;
					}else{
						// Descriminar las facturas de compras correspondientes a pagos parciales
						for(RequisicionDe det:ff.getRequisiciones()){
							if(det.getRequisicion().getConcepto()!=null && det.getRequisicion().getConcepto().getId()==737332L){
								System.out.println("Requisicion de pagos parciales no se agrega la factura: "+ff.getDocumento()+ "GFactura.id="+ff.getId()+  " Fecha: "+ff.getFecha()+ " Fecha Contable:"+ff.getFechaContable() );
								continue;
							}
							facturas.add(ff);
						}						
					}
				}
				Poliza p=generaPolizaDeGastos(facturas);
				//Poliza p=generaPolizaDeGastos(facs);
				p.actualizar();
				return p;
			}
		});
		return p;
	}
	
	
	private Poliza generaPolizaDeGastos(final List<GFacturaPorCompra> facturas){
		Assert.notEmpty(facturas,"No se puede genera la poliza sin facturas de gastos");
		final Date fecha=facturas.get(0).getFechaContable();
		Poliza p=new Poliza();
		p.setTipo(Tipo.DIARIO);
		p.setDescripcion(MessageFormat.format("Provision de facturas", DateUtil.convertDateToString(fecha)));
		p.setFecha(fecha);
		for(GFacturaPorCompra fac:facturas){
			registrarGastosAgrupados(fac.getCompra(),p,fac.getDocumento());
		}
		registrarProvision(p, facturas);
		registrarIva(p, facturas);
		registrarRetenciones(p, facturas);
		return p;
	}
	
	/**
	 * Registra los gastos agrupandolos por rubro y sucursal
	 * 
	 * @param compra
	 * @param registros
	 */
	@SuppressWarnings("unchecked")
	public void registrarGastosAgrupados(final GCompra compra,final Poliza poliza,final String factura){		
		final EventList<GCompraDet> eventList=GlazedLists.eventList(compra.getPartidas());
		final Comparator<GCompraDet> c1=GlazedLists.beanPropertyComparator(GCompraDet.class, "rubro.id");
		final Comparator<GCompraDet> c2=GlazedLists.beanPropertyComparator(GCompraDet.class, "sucursal.clave");
		Comparator<GCompraDet>[] comps=new Comparator[]{c1,c2};
		final GroupingList groupList=new GroupingList(eventList,GlazedLists.chainComparators(Arrays.asList(comps)));
		for(int index=0;index<groupList.size();index++){
			List<GCompraDet> row=groupList.get(index);
			registrarGasto(row,poliza,factura);			
		}
	}
	
	/**
	 * 
	 * @param dets
	 * @param registros
	 */
	public void registrarGasto(final List<GCompraDet> dets,final Poliza poliza,final String factura){
		GCompraDet det=dets.get(0);
		PolizaDet asiento=poliza.agregarPartida();
		asiento.setCuenta(getCuenta("600"));
		asiento.setDescripcion("");
		
		String pattern="F-{0} {1}";
		String descripcion2=MessageFormat.format(pattern, det.getFactura(),det.getProducto().getDescripcion());
		descripcion2=StringUtils.substring(descripcion2, 0,50);
		asiento.setDescripcion2(descripcion2);
		
		if( (det.getRubro()!=null) || (det.getRubro().getRubroCuentaOrigen()!=null) ){
			ConceptoDeGasto concepto=det.getRubro().getRubroCuentaOrigen();
			String cc=concepto!=null?concepto.getDescripcion():"NA";
			asiento.setDescripcion2(cc);	
		}
		
		
		CantidadMonetaria debe=CantidadMonetaria.pesos(0);
		for(GCompraDet part:dets){
			debe=debe.add(part.getImporteMN());
		}
		
		asiento.setDebe(debe.amount());
		asiento.setReferencia(dets.get(0).getCompra().getProveedor().getNombreRazon());
		asiento.setReferencia2(det.getSucursal().getNombre());
		
	}
	
	
	private void registrarProvision(Poliza p,List<GFacturaPorCompra> facs){
		for(GFacturaPorCompra fac:facs){
			generarPartidaDeProvision(p, fac);
			/*
			PolizaDet a=p.agregarPartida();
			a.setCuenta(getCuenta("212"));
			a.setDescripcion("");
			String pattern="PROV F:{0} {1}";
			String descripcion=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
			a.setDescripcion2(descripcion);
			a.setHaber(fac.getTotatMN().amount());
			a.setReferencia(fac.getCompra().getProveedor().getNombreRazon());
			a.setReferencia2(fac.getCompra().getSucursal().getNombre());*/
		}
	}
	private PolizaDet generarPartidaDeProvision(Poliza p,GFacturaPorCompra fac){
		if(fac.getDocumento().equalsIgnoreCase("21")){
			System.out.println("DEBUG..");
		}
		if(!fac.getRequisiciones().isEmpty()){
			Requisicion r=fac.getRequisiciones().iterator().next().getRequisicion();
			if(r.getConcepto()!=null){
				if(r.getConcepto().getId()==737332L){
					return null;
				}
			}
		}
		PolizaDet a=p.agregarPartida();
		a.setCuenta(getCuenta("212"));
		a.setDescripcion("");
		String pattern="PROV F:{0} {1}";
		String descripcion=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
		a.setDescripcion2(descripcion);
		a.setHaber(fac.getTotatMN().amount());
		a.setReferencia(fac.getCompra().getProveedor().getNombreRazon());
		a.setReferencia2(fac.getCompra().getSucursal().getNombre());
		return a;
	}
	
	private void registrarIva(Poliza p,List<GFacturaPorCompra> facs){
		//Impuesto de gastos
		for(GFacturaPorCompra fac:facs){
			PolizaDet a=p.agregarPartida();
			a.setCuenta(getCuenta("117"));
			a.setDescripcion("Iva x acreditar");
			String pattern="IVA F:{0} {1}";
			String descripcion2=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
			a.setDescripcion2(descripcion2);
			CantidadMonetaria impuestoGastos=CantidadMonetaria.pesos(0);
			for(GCompraDet det:fac.getCompra().getPartidas()){
				if(!det.getProducto().getInversion()){
					CantidadMonetaria imp=det.getImpuestoMN();
					impuestoGastos=impuestoGastos.add(imp);
				}
			}
			a.setDebe(impuestoGastos.amount());
			if( (fac.getCompra().getRet1MN()!=null) &&( fac.getCompra().getRet1MN().amount().doubleValue()>0)){
				a.setDebe(a.getDebe().subtract(fac.getCompra().getRet1MN().amount()));
			}
			a.setReferencia(fac.getCompra().getProveedor().getNombreRazon());
			a.setReferencia2(fac.getCompra().getSucursal().getNombre());
			
		}
		/**Impuesto de gastos**/
		for(GFacturaPorCompra fac:facs){
			PolizaDet a=p.agregarPartida();
			a.setCuenta(getCuenta("117"));
			a.setDescripcion("Iva x acreditar");
			String pattern="IVA F:{0} {1}";
			String descripcion2=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
			a.setDescripcion2(descripcion2);
			
			CantidadMonetaria impuestoGastos=CantidadMonetaria.pesos(0);
			for(GCompraDet det:fac.getCompra().getPartidas()){
				if(det.getProducto().getInversion()){
					CantidadMonetaria imp=det.getImpuestoMN();
					impuestoGastos=impuestoGastos.add(imp);
				}
			}
			a.setDebe(impuestoGastos.amount());
			a.setReferencia(fac.getCompra().getProveedor().getNombreRazon());
			a.setReferencia2(fac.getCompra().getSucursal().getNombre());
		}
	}
	
	public static final double RETENCION_TRANSPORTE=4.0;
	public static final double RETENCION_HONORARIOS=10.0;
	
	public static final String IVA_RET_PEND_POR_ACRED="117-0006-005";	
	
	public static final String IVA_RET_PEND_POR_PAGAR="208-0001-000";
	
	public static final String ISR_RET_PEND_POR_PAGAR="208-0002-000";
	
	public static final String CUENTA_ISR_RETENIDO="205-0003-000";
	
	private void registrarRetenciones(Poliza p,List<GFacturaPorCompra> facs){
		
		for(GFacturaPorCompra fac:facs){			
			
			for(GCompraDet det:fac.getCompra().getPartidas()){				
				if(det.getRetencion1()==RETENCION_HONORARIOS){
					//honorarios=honorarios.add(det.getRetencion1Imp());
					//honorarios=det.getRetencion1Imp();
					PolizaDet a=p.agregarPartida();
					a.setCuenta(getCuenta("205"));
					a.setDescripcion(PolizaContableManager.IVA_RETENIDO_PENDIENTE);
					String pattern="F:{0} {1}";
					String descripcion2=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon()+"aqui");
					a.setDescripcion2(descripcion2);
					
					a.setHaber(CantidadMonetaria.pesos(det.getRetencion2Imp().doubleValue()).amount());
					a.setReferencia(fac.getCompra().getProveedor().getNombreRazon());
					a.setReferencia2(fac.getCompra().getSucursal().getNombre());				
					
					PolizaDet a2=p.agregarPartida();
					a2.setCuenta(getCuenta("117"));
					a2.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_RETENIDO);					
					a2.setDebe(CantidadMonetaria.pesos(det.getRetencion1Imp().doubleValue()).amount());
					a2.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
					a2.setReferencia(fac.getCompra().getProveedor().getNombreRazon());
					a2.setReferencia2(fac.getCompra().getSucursal().getNombre());
					
					
					PolizaDet a3=p.agregarPartida();
					a3.setCuenta(getCuenta("118"));
					a3.setDescripcion("ISR RETENIDO PENDIENTE X PAGAR");
					a3.setHaber(CantidadMonetaria.pesos(det.getRetencion2Imp().doubleValue()).amount());
					a3.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
					a3.setReferencia(fac.getCompra().getProveedor().getNombreRazon());
					a3.setReferencia2(fac.getCompra().getSucursal().getNombre());
					
					continue;
				}else if(det.getRetencion1()==RETENCION_TRANSPORTE){
					//Cargo a Iva Acreditable Retenido
					PolizaDet a=p.agregarPartida();
					a.setCuenta(getCuenta("205"));
					a.setDescripcion(PolizaContableManager.IVA_RETENIDO_PENDIENTE);
					String pattern="F:{0} {1}";
					String descripcion=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
					a.setDescripcion2(descripcion);
					a.setHaber(CantidadMonetaria.pesos(det.getRetencion1Imp().doubleValue()).amount());
					a.setReferencia(fac.getCompra().getProveedor().getNombreRazon());
					a.setReferencia2(fac.getCompra().getSucursal().getNombre());
					
					//Abono a IVA RETENIDO
					PolizaDet a2=p.agregarPartida();				
					a2.setDescripcion(descripcion);
					a2.setCuenta(getCuenta("117"));
					a2.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_RETENIDO);
					a2.setDebe(CantidadMonetaria.pesos(det.getRetencion1Imp().doubleValue()).amount());
					a2.setDescripcion2(descripcion);
					a2.setReferencia(fac.getCompra().getProveedor().getNombreRazon());
					a2.setReferencia2(fac.getCompra().getSucursal().getNombre());
				}
			}
		}		
			
	}


	
	

}
