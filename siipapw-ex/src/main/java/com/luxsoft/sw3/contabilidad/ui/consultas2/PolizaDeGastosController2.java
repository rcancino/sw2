package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.AbstractPolizaMultipleManager;
import com.luxsoft.sw3.contabilidad.services.PolizaContableManager;

/**
 * Controlador para el mantenimiento de polizas de pago de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeGastosController2 extends AbstractPolizaMultipleManager{
	
	
	public Poliza existente(final Poliza poliza){
		String hql="from Poliza p where p.clase=? and p.referencia=?";
		Object values[]={"Gastos",poliza.getReferencia()};
		List<Poliza> res=ServiceLocator2.getHibernateTemplate().find(hql, values);
		return res.isEmpty()?null:res.get(0);
	}
	
	public Poliza actualizar(Poliza poliza){
		poliza=ServiceLocator2.getPolizasManager().getPolizaDao().get(poliza.getId());
		Long id=Long.valueOf(poliza.getReferencia());
		List<CargoAbono> cargos=getHibernateTemplate().find("from CargoAbono c left join fetch c.pago p where c.id=?",id);
		Assert.notEmpty(cargos,"No existe al pago de la poliza: "+poliza.getId()+ " Ref: "+poliza.getReferencia());
		CargoAbono pago=cargos.get(0);
		poliza.getPartidas().clear();
		
		Poliza res=generarPoliza(pago.getId(),poliza.getFecha());
		
		for(PolizaDet det:res.getPartidas()){
			det.setPoliza(poliza);
			poliza.getPartidas().add(det);
		}
		poliza.actualizar();
		poliza.clean();
		return poliza;
	}

	
	public List<Poliza> generaPoliza(final Date fecha) {
		String sql="SELECT B.CARGOABONO_ID " +
				"  FROM sw_bcargoabono B WHERE " +
				"  B.FECHA =? " +
				"  AND B.ORIGEN=\'GASTOS\'";
		Object[] args=new Object[]{
			new SqlParameterValue(Types.DATE, fecha)	
		};
		List<Long> pagos=ServiceLocator2
				.getJdbcTemplate().queryForList(sql, args,Long.class);
		System.out.println("Pagos a procesar: "+pagos.size());
		
		final List<Poliza> polizas=new ArrayList<Poliza>();
		
		for(final Long id:pagos){
			Poliza res=generarPoliza(id,fecha);
			if(res!=null){
				res.actualizar();
				polizas.add(res);
			}
			
		}
		return polizas;
	}
	
	/**
	 * Genera la poliza para el cargo abono abiendo un a transaccion de Hibernate
	 * 
	 * @param id
	 * @return
	 */
	private Poliza generarPoliza(final Long id,final Date fecha){		
		return (Poliza)getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				CargoAbono pago=(CargoAbono)session.load(CargoAbono.class, id);
				return generarPoliza(pago,fecha);
			}
		});
	}
	
	private Poliza generarPoliza(CargoAbono pago,final Date fecha){
		final Poliza poliza=new Poliza();
		poliza.setFecha(fecha);
		poliza.setReferencia(pago.getId().toString());
		poliza.setTipo(Poliza.Tipo.DIARIO);
		poliza.setClase("Gastos");
		String forma=pago.getFormaDePago().name();
		forma=StringUtils.substring(forma, 0,2);
		String pattern="{0} {1} {2}";
		String ms=MessageFormat.format(pattern, forma,pago.getReferencia(),pago.getAFavor());
		ms=StringUtils.substring(ms, 0,255);
		poliza.setDescripcion(ms);
		
		final BigDecimal importePago=pago.getImporte().abs();
		if(importePago.doubleValue()==0){
			//poliza.setDescripcion(ms+" (CANCELADOI)");
			return poliza;
		}
		
		Requisicion requisicion=pago.getRequisicion();
		if(requisicion==null){
			System.out.println("PAgo sin requisicion: "+pago.getId());
			return null;
		}
		if(requisicion.getPartidas().size()==0){
			System.out.println("No existe partidas para la requisicion: "+requisicion.getId());
			return null;
		}
		
		final Date fechaPago=pago.getFecha();
		Date fechaCobro=pago.getFechaCobro();
		if(fechaCobro==null)
			fechaCobro=fechaPago;
		
		GFacturaPorCompra factura=requisicion
								.getPartidas().iterator().next()
								.getFacturaDeGasto();
		Date fechaCorte=DateUtils.addDays(poliza.getFecha(), -1);
		BigDecimal saldo=factura==null?BigDecimal.ZERO:factura.getSaldoCalculadoAlCorte(fechaCorte).amount();
		
		Long concepto=pago.getRequisicion().getConcepto()!=null?pago.getRequisicion().getConcepto().getId():0L;
		
		if(concepto==201136L){
			System.out.println("Pago de anticipo.....");
			if(saldo.subtract(importePago).doubleValue()>0 || (factura==null)){
				registrarAnticipoParcial(poliza,pago);
				return poliza;
			}else{
				registrarUltimoAnticipo(poliza,pago,factura);
				return poliza;
			}
		}
		final Date fechaFactura=factura.getFecha();		
		final BigDecimal totalFactura=factura.getTotalMN().amount();
		
		if(concepto==737332L){
			if(saldo.doubleValue()==totalFactura.doubleValue()){
				registrarPrimeraParcialidad(poliza,pago,factura);						
				return poliza;
			}else{
				registrarPagoParcial(poliza,pago,factura);
				return poliza;
			}
		}
		
		
		if(!DateUtil.isSameMonth(fechaPago, fechaCobro)){
			
			if(!DateUtil.isSameMonth(fechaPago, fechaFactura)){
				poliza.setDescripcion(poliza.getDescripcion()+ " (TRANSITO PROVISIONADO)");
				canclearProvision(poliza, pago,factura);
				return poliza;
			}else{
				poliza.setDescripcion(poliza.getDescripcion()+ " (TRANSITO)");
				registrarGastoPorChequeEnTransito(poliza,pago,factura);
				return poliza;
			}
			
		}else{
			if(!DateUtil.isSameMonth(fechaPago, fechaFactura)){
				BigDecimal iva=MonedasUtils.calcularImpuestoDelTotal(pago.getImporteMN().amount().abs());
				BigDecimal importe=MonedasUtils.calcularImporteDelTotal(pago.getImporteMN().abs().amount());
				poliza.setDescripcion(poliza.getDescripcion()+" (CANCELA PROV)");
				
				canclearProvision(poliza,pago,factura);
				registrarIvaEnGastos(poliza, pago, iva, false);
				registrarIvaPorAcreditarGastos(poliza, pago, iva, true);
				registrarIETUEnGastos(poliza, pago, importe);
				return poliza;
			}else{
				registrarPagoNormal(poliza,pago,factura);
			}
		}
		
		return poliza;
	}
	
	private void canclearProvision(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura) {
		
		//Cancelar la provision (C)
		PolizaDet cargoProvision=poliza.agregarPartida();
		cargoProvision.setCuenta(getCuenta("212"));
		cargoProvision.setDebe(factura.getTotalMN().amount());
		cargoProvision.setConcepto(cargoProvision.getCuenta().getConcepto("PRVG03"));
		String pattern="PROVISION F:{0}  {1,date,short}";				
		final String descripcion2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
		cargoProvision.setDescripcion2(descripcion2);
		cargoProvision.setReferencia(factura.getCompra().getProveedor().getNombreRazon());
		cargoProvision.setReferencia2(factura.getCompra().getSucursal().getNombre());
		
		//Bancos (A)
		registrarAbonoABancos(poliza, pago);
	}

	private void registrarGastoPorChequeEnTransito(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura) {
		registrarGasto(poliza, pago);
		registrarAbonoABancos(poliza, pago);
		BigDecimal iva=MonedasUtils.calcularImpuestoDelTotal(pago.getImporteMN().abs().amount());
		registrarIvaPorAcreditarGastos(poliza, pago, iva,false);
	}

	private void registrarPagoNormal(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura) {
		poliza.setDescripcion(poliza.getDescripcion()+" (Normal) ");
		registrarGasto(poliza, pago);
		registrarAbonoABancos(poliza, pago);
		BigDecimal iva=factura.getImpuesto().abs().amount();
		iva=iva.subtract(factura.getCompra().getRet1MN().amount());
		registrarIvaEnGastos(poliza, pago, iva, false);
		registrarIETUEnGastos(poliza, pago, factura.getCompra().getIetu().amount());
		registrarRetenciones(pago, poliza,factura);
	}

	private void registrarPagoParcial(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura) {
		
		poliza.setDescripcion(poliza.getDescripcion()+" (Pago parcial) ");
		registrarAbonoABancos(poliza, pago);
		
		//Cancelar la provision (C)		
		PolizaDet cargoProvision=poliza.agregarPartida();
		cargoProvision.setCuenta(getCuenta("212"));
		cargoProvision.setDebe(MonedasUtils.calcularImporteDelTotal(pago.getImporte().abs()));
		cargoProvision.setConcepto(cargoProvision.getCuenta().getConcepto("PRVG03"));
		String pattern="PROVISION F:{0}  {1,date,short}";			
		String descripcion2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
		cargoProvision.setDescripcion2(descripcion2);
		cargoProvision.setReferencia(factura.getCompra().getProveedor().getNombreRazon());
		cargoProvision.setReferencia2(factura.getCompra().getSucursal().getNombre());
		BigDecimal ivaPago=MonedasUtils.calcularImpuestoDelTotal(pago.getImporte().abs());
		registrarIvaPorAcreditarGastos(poliza, pago, ivaPago, false);
		
		registrarIvaEnGastos(poliza, pago, ivaPago, false);
		registrarIvaPorAcreditarGastos(poliza, pago, ivaPago, true);
		registrarIETUEnGastos(poliza, pago, MonedasUtils.calcularImporteDelTotal(pago.getImporteMN().abs().amount()));
		
	}

	private void registrarPrimeraParcialidad(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura) {
		
		poliza.setDescripcion(poliza.getDescripcion()+" (Primera parcialidad) ");
		registrarGasto(poliza, pago);
		// Provision
		PolizaDet abonoProvision=poliza.agregarPartida();
		abonoProvision.setCuenta(getCuenta("212"));
		abonoProvision.setHaber(factura.getTotalMN().amount());
		abonoProvision.setConcepto(abonoProvision.getCuenta().getConcepto("PRVG03"));
		String pattern="PROVISION F:{0}  {1,date,short}";				
		String descripcion2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
		abonoProvision.setDescripcion2(descripcion2);
		abonoProvision.setReferencia(factura.getCompra().getProveedor().getNombreRazon());
		abonoProvision.setReferencia2(factura.getCompra().getSucursal().getNombre());
		
		BigDecimal iva=MonedasUtils.calcularImpuestoDelTotal(factura.getTotalMN().amount());
		
		//BigDecimal importe=factura.getImporte().amount();
		
		registrarIvaPorAcreditarGastos(poliza, pago, iva, false);
		registrarAbonoABancos(poliza, pago);
		
		//Cancelar la provision (C)
		
		PolizaDet cargoProvision=poliza.agregarPartida();
		cargoProvision.setCuenta(getCuenta("212"));
		cargoProvision.setDebe(MonedasUtils.calcularImporteDelTotal(pago.getImporte().abs()));
		cargoProvision.setConcepto(cargoProvision.getCuenta().getConcepto("PRVG03"));
						
		descripcion2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
		cargoProvision.setDescripcion2(descripcion2);
		cargoProvision.setReferencia(factura.getCompra().getProveedor().getNombreRazon());
		cargoProvision.setReferencia2(factura.getCompra().getSucursal().getNombre());
		BigDecimal ivaPago=MonedasUtils.calcularImpuestoDelTotal(pago.getImporte().abs());
		
		registrarIvaPorAcreditarGastos(poliza, pago, ivaPago, false);		
		registrarIvaEnGastos(poliza, pago, ivaPago, false);
		registrarIvaPorAcreditarGastos(poliza, pago, ivaPago, true);
		registrarIETUEnGastos(poliza, pago, MonedasUtils.calcularImporteDelTotal(pago.getImporteMN().abs().amount()));
		
	}

	/**
	 * Pago Final de un anticipo 
	 * 
	 * @param poliza
	 * @param pago
	 */
	private void registrarUltimoAnticipo(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura) {
		poliza.setDescripcion(poliza.getDescripcion()+" (Anticipo final) ");
		registrarGasto(poliza, pago);
		
		//Anticipo
		PolizaDet cargoAnticipo=poliza.agregarPartida();
		cargoAnticipo.setCuenta(getCuenta("111"));
		cargoAnticipo.setHaber(factura.getTotalMN().subtract(pago.getImporteMN().abs()).amount());
		cargoAnticipo.setConcepto(cargoAnticipo.getCuenta().getConcepto("ANTP01"));
		String pattern="ANTICIPO F:{0} {1,date,short}";			
		String descripcion2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
		cargoAnticipo.setDescripcion2(descripcion2);
		cargoAnticipo.setReferencia(pago.getAFavor());
		cargoAnticipo.setReferencia2("OFICINAS");
		
		registrarAbonoABancos(poliza, pago);
		
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			
			CantidadMonetaria aplicado=CantidadMonetaria.pesos(0);
			if(factura!=null){
				CantidadMonetaria saldo=factura.getSaldoCalculadoAlCorte(DateUtils.addDays(poliza.getFecha(), -1));
				aplicado=factura.getTotalMN().subtract(saldo);
			}
			
			CantidadMonetaria importeAplicado=MonedasUtils.calcularImporteDelTotal(aplicado).abs();
			CantidadMonetaria importeFactura=MonedasUtils.calcularImporteDelTotal(factura.getTotalMN());
			CantidadMonetaria ivaDeFactura=MonedasUtils.calcularImpuesto(importeFactura);
			
			
			// Cargo a IVA por acreditar en gastos 
			PolizaDet cargoIvaPorAcreditarDelGasto=poliza.agregarPartida();
			cargoIvaPorAcreditarDelGasto.setCuenta(getCuenta("117"));
			cargoIvaPorAcreditarDelGasto.setDebe(ivaDeFactura.amount());
			cargoIvaPorAcreditarDelGasto.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_GASTOS);
			cargoIvaPorAcreditarDelGasto.setDescripcion2("PENDIENTE");
			cargoIvaPorAcreditarDelGasto.setReferencia(pago.getAFavor());
			cargoIvaPorAcreditarDelGasto.setReferencia2("OFICINAS");
			
			// Cargo en IVA en GAsto
			PolizaDet cargoIvaEnGasto=poliza.agregarPartida();
			cargoIvaEnGasto.setCuenta(getCuenta("117"));
			cargoIvaEnGasto.setDebe(ivaDeFactura.amount());
			cargoIvaEnGasto.setDescripcion(PolizaContableManager.IVA_EN_GASTOS);
			cargoIvaEnGasto.setDescripcion2("PENDIENTE");
			cargoIvaEnGasto.setReferencia(pago.getAFavor());
			cargoIvaEnGasto.setReferencia2("OFICINAS");
			
			// Abono a IVa por acreditar en gasto
			PolizaDet abonoIvaPorAcreditarEnGasto=poliza.agregarPartida();
			abonoIvaPorAcreditarEnGasto.setCuenta(getCuenta("117"));
			abonoIvaPorAcreditarEnGasto.setHaber(ivaDeFactura.amount());
			abonoIvaPorAcreditarEnGasto.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_GASTOS);
			abonoIvaPorAcreditarEnGasto.setDescripcion2("PENDIENTE");
			abonoIvaPorAcreditarEnGasto.setReferencia(pago.getAFavor());
			abonoIvaPorAcreditarEnGasto.setReferencia2("OFICINAS");
			
			// Abono a 900 IETU de lo aplicado
			PolizaDet abonoIEUTAplicado=poliza.agregarPartida();
			abonoIEUTAplicado.setCuenta(getCuenta("900"));
			abonoIEUTAplicado.setHaber(importeAplicado.amount());
			abonoIEUTAplicado.setDescripcion("IETU DEDUCIBLE ANTICIPOS");
			abonoIEUTAplicado.setDescripcion2("PENDIENTE");
			abonoIEUTAplicado.setReferencia(pago.getAFavor());
			abonoIEUTAplicado.setReferencia2("OFICINAS");
			
			// Cargo a 901 IETU de lo aplicado
			PolizaDet cargoIETUAplicado=poliza.agregarPartida();
			cargoIETUAplicado.setCuenta(getCuenta("901"));
			cargoIETUAplicado.setDebe(importeAplicado.amount());
			cargoIETUAplicado.setDescripcion("IETU DEDUCIBLE ANTICIPOS");
			cargoIETUAplicado.setDescripcion2("PENDIENTE");
			cargoIETUAplicado.setReferencia(pago.getAFavor());
			cargoIETUAplicado.setReferencia2("OFICINAS");
			
			//IETU en Gasto
			registrarIETUEnGastos(poliza, pago, importeFactura.amount());
		}
	
	}

	private void registrarAnticipoParcial(Poliza p,CargoAbono pago) {
		p.setDescripcion(p.getDescripcion()+" (Anticipo) ");
		//Bancos (A)
		registrarAbonoABancos(p, pago);
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){		
			
			GFacturaPorCompra factura=det.getFacturaDeGasto();
			CantidadMonetaria pendienteDePagar=det.getTotalMN();
			if(factura!=null){
				CantidadMonetaria saldo=factura.getSaldoCalculadoAlCorte(DateUtils.addDays(p.getFecha(), -1));
				pendienteDePagar=saldo.subtract(det.getTotalMN());
			}			
			if((factura==null) ||(pendienteDePagar.amount().abs().doubleValue()>0)){
				
				BigDecimal importe=MonedasUtils.calcularImporteDelTotal(det.getTotal().abs().amount());
				BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
				
				//Anticipo
				PolizaDet cargo=p.agregarPartida();
				cargo.setCuenta(getCuenta("111"));
				cargo.setDebe(det.getTotal().amount());
				cargo.setConcepto(cargo.getCuenta().getConcepto("ANTP01"));
				String pattern="ANTICIPO F:{0} {1,date,short}";			
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				cargo.setDescripcion2(descripcion2);
				cargo.setReferencia(pago.getAFavor());
				cargo.setReferencia2("OFICINAS");
				
				// IVA en Gastos
				registrarIvaEnGastos(p, pago, iva, false);
				
				//IVA por Acreditar en Gastos
				registrarIvaPorAcreditarGastos(p, pago, iva,true);
				
				
				PolizaDet cargoIetu=p.agregarPartida();
				cargoIetu.setDebe(importe);
				cargoIetu.setCuenta(getCuenta("900"));
				cargoIetu.setConcepto(cargoIetu.getCuenta().getConcepto("IETUD04"));
				pattern="IETU Deducible de Anticipos";			
				descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				cargoIetu.setDescripcion2(descripcion2);
				cargoIetu.setReferencia(pago.getAFavor());
				cargoIetu.setReferencia2("OFICINAS");
				
				PolizaDet abonoIetu=p.agregarPartida();
				abonoIetu.setHaber(importe);
				abonoIetu.setCuenta(getCuenta("901"));				
				abonoIetu.setConcepto(abonoIetu.getCuenta().getConcepto("DIETU04"));
				pattern="IETU Deducible de Anticipos";			
				descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				abonoIetu.setDescripcion2(descripcion2);
				abonoIetu.setReferencia(pago.getAFavor());
				abonoIetu.setReferencia2("OFICINAS");
				
			}
			
		}
		
	}
	
	private void registrarAbonoABancos(Poliza poliza,CargoAbono pago){
		//Bancos (A)
		PolizaDet abonoBancos=poliza.agregarPartida();
		abonoBancos.setCuenta(getCuenta("102"));
		abonoBancos.setHaber(pago.getImporteMN().abs().amount());	
		String numeroDeCuenta=pago.getCuenta().getNumero().toString();
		abonoBancos.setConcepto(abonoBancos.getCuenta().getConcepto(numeroDeCuenta));
		String c=MessageFormat.format("{0} {1}", pago.getCuenta().getDescripcion(),pago.getCuenta().getNumero());
		c=StringUtils.substring(c, 0,50);
		abonoBancos.setDescripcion2(c);
		abonoBancos.setReferencia(pago.getAFavor());
		abonoBancos.setReferencia2("OFICINAS");
	}
	
	private void registrarGasto(final Poliza poliza,final CargoAbono pago){		
		List<GFacturaPorCompra> facturas=new ArrayList<GFacturaPorCompra>();		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			if(det.getFacturaDeGasto()!=null)
				facturas.add(det.getFacturaDeGasto());
		}
		for(GFacturaPorCompra fac:facturas){			
			generarCargoAGastosAcumulado(fac, poliza,pago);
		}		
	}
	
	private void generarCargoAGastosAcumulado(final GFacturaPorCompra factura,final Poliza poliza,final CargoAbono pago){
		
		final EventList<GCompraDet> eventList=GlazedLists.eventList(factura.getCompra().getPartidas());
		final Comparator<GCompraDet> c1=GlazedLists.beanPropertyComparator(GCompraDet.class, "rubro.id");
		final Comparator<GCompraDet> c2=GlazedLists.beanPropertyComparator(GCompraDet.class, "sucursal.clave");
		Comparator<GCompraDet>[] comps=new Comparator[]{c1,c2};
		final GroupingList groupList=new GroupingList(eventList,GlazedLists.chainComparators(Arrays.asList(comps)));
		
		CantidadMonetaria ivaAcumulado=CantidadMonetaria.pesos(0);
		CantidadMonetaria importeAcumulado=CantidadMonetaria.pesos(0);
		
		String proveedor=null;
		String sucursal=null;
		for(int index=0;index<groupList.size();index++){
			List<GCompraDet> dets=groupList.get(index);
			String asiento="GASTO";
			GCompraDet gasto=dets.get(0);
			PolizaDet cargoAGastos=poliza.agregarPartida();
			//cargoAGastos.setCuenta(getCuenta("600"));
			
			ConceptoDeGasto concepto=gasto.getRubro();
			
			if(concepto!=null){	
				if(pago.getId()==835098L){
					System.out.println("DEBUG.....");
				}				
				concepto=concepto.getRubroSegundoNivel(concepto);
				
				ConceptoContable conceptoContable=buscarConceptoContable(concepto.getId().toString());
				if(conceptoContable!=null){
					cargoAGastos.setConcepto(conceptoContable);
					cargoAGastos.setCuenta(conceptoContable.getCuenta());
				}
				//cargoAGastos.setConcepto(cargoAGastos.getCuenta().getConcepto(concepto.getId().toString()));
				if(cargoAGastos.getConcepto()==null){
					cargoAGastos.setDescripcion("SIN CONCEPTO: "+concepto.getId().toString());					
				}				
			}
			
			String pattern="FAC: {0}  ({1,date,short}), {2}";
			String descripcion2=MessageFormat.format(pattern
					, factura.getDocumento()
					,factura.getFecha()
					,gasto.getRubro()!=null?gasto.getRubro().getDescripcion():"SIN CONCEPTO DE GASTO"
					);
			descripcion2=StringUtils.substring(descripcion2, 0,255);
			cargoAGastos.setDescripcion2(descripcion2);
			if(proveedor==null)
				proveedor=dets.get(0).getCompra().getProveedor().getNombreRazon();
			cargoAGastos.setReferencia(proveedor);
			if(sucursal==null)
				sucursal=new String(gasto.getSucursal().getNombre());
			cargoAGastos.setReferencia2(sucursal);
			
			CantidadMonetaria importe=CantidadMonetaria.pesos(0);
			
			for(GCompraDet part:dets){
				importe=importe.add(part.getImporteMN());
				ivaAcumulado=ivaAcumulado.add(part.getImpuestoMN());
			}
			importeAcumulado=importeAcumulado.add(gasto.getCompra().getTotalMN());
			cargoAGastos.setDebe(importe.amount());
			cargoAGastos.setAsiento(asiento);				
		}
		
	}
	
	private void registrarIvaPorAcreditarGastos(Poliza poliza,CargoAbono pago,BigDecimal iva,boolean abono){
		// Abono a IVa por acreditar en gasto
		PolizaDet abonoIvaPorAcreditarEnGasto=poliza.agregarPartida();
		abonoIvaPorAcreditarEnGasto.setCuenta(getCuenta("117"));
		if(abono){
			abonoIvaPorAcreditarEnGasto.setHaber(iva);
			
		}else{
			abonoIvaPorAcreditarEnGasto.setDebe(iva);
		}
		abonoIvaPorAcreditarEnGasto.setConcepto(abonoIvaPorAcreditarEnGasto.getCuenta().getConcepto("IVAG02"));
		abonoIvaPorAcreditarEnGasto.setDescripcion2("PENDIENTE");
		abonoIvaPorAcreditarEnGasto.setReferencia(pago.getAFavor());
		abonoIvaPorAcreditarEnGasto.setReferencia2("OFICINAS");
	}
	
	private void registrarIvaEnGastos(Poliza poliza,CargoAbono pago,BigDecimal iva,boolean abono){
		PolizaDet cargoIvaEnGasto=poliza.agregarPartida();
		cargoIvaEnGasto.setCuenta(getCuenta("117"));
		if(abono){
			cargoIvaEnGasto.setHaber(iva);
			
		}else{
			cargoIvaEnGasto.setDebe(iva);
		}
		cargoIvaEnGasto.setConcepto(cargoIvaEnGasto.getCuenta().getConcepto("IVAG01"));
		cargoIvaEnGasto.setDescripcion2("PENDIENTE");
		cargoIvaEnGasto.setReferencia(pago.getAFavor());
		cargoIvaEnGasto.setReferencia2("OFICINAS");
	}
	
	private void registrarIETUEnGastos(Poliza poliza,CargoAbono pago,BigDecimal importe){
		
		// Cargo a 900 IETU en Gasto
		PolizaDet cargo=poliza.agregarPartida();
		cargo.setCuenta(getCuenta("900"));
		cargo.setDebe(importe);
		cargo.setConcepto(cargo.getCuenta().getConcepto("IETUD02"));
		cargo.setDescripcion2("PENDIENTE");
		cargo.setReferencia(pago.getAFavor());
		cargo.setReferencia2("OFICINAS");
					
		// Abono a 901 IETU en Gasto
		PolizaDet abono=poliza.agregarPartida();
		abono.setCuenta(getCuenta("901"));
		abono.setHaber(importe);
		abono.setConcepto(abono.getCuenta().getConcepto("DIETU02"));
		abono.setDescripcion2("PENDIENTE");
		abono.setReferencia(pago.getAFavor());
		abono.setReferencia2("OFICINAS");
	}

	private void registrarRetenciones(final CargoAbono pago,final Poliza p,GFacturaPorCompra factura){
		
		BigDecimal retencion=factura.getCompra().getRetencionesMN().abs().amount();
		if(retencion.doubleValue()==0){
			return;
		}
		
		if(pago.getRequisicion()!=null){
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				PolizaDet cargoIvaAcreditableRet=p.agregarPartida();				
				//cargoIvaAcreditableRet.setDescripcion(PolizaContableManager.IVA_ACREDITABLE_RETENIDO);
				cargoIvaAcreditableRet.setCuenta(getCuenta("117"));
				cargoIvaAcreditableRet.setConcepto(cargoIvaAcreditableRet.getCuenta().getConcepto("IVAR01"));
				String pattern="PROV F:{0} {1,date,short}";				
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				cargoIvaAcreditableRet.setDescripcion2(descripcion2);				
				
				CantidadMonetaria monto=det.getRetencion1();
				if(monto.amount().doubleValue()!=0){
					cargoIvaAcreditableRet.setDebe(monto.amount());
					if(pago.getSucursal()!=null)
						cargoIvaAcreditableRet.setReferencia2(pago.getSucursal().getNombre());
					else
						cargoIvaAcreditableRet.setReferencia2("NA");
					
				}
				cargoIvaAcreditableRet.setReferencia(pago.getAFavor());
				cargoIvaAcreditableRet.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
				
			}
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				PolizaDet ablnoIvaRetenido=p.agregarPartida();
				ablnoIvaRetenido.setCuenta(getCuenta("205"));
				//ablnoIvaRetenido.setDescripcion(PolizaContableManager.IVA_RETENIDO);
				ablnoIvaRetenido.setConcepto(ablnoIvaRetenido.getCuenta().getConcepto("IMPR02"));
				String pattern="PROV F:{0} {1,date,short}";			
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				ablnoIvaRetenido.setDescripcion2(descripcion2);
				
				CantidadMonetaria monto=det.getRetencion1();
				if(monto.amount().doubleValue()!=0){
					ablnoIvaRetenido.setHaber(monto.amount());
					if(pago.getSucursal()!=null)
						ablnoIvaRetenido.setReferencia2(pago.getSucursal().getNombre());
					else
						ablnoIvaRetenido.setReferencia2("NA");					
					//String pat="{0} {1}";
					//ablnoIvaRetenido.setReferencia(MessageFormat.format(pat, pago.getOrigen().name(),pago.getComentario()));
					//ablnoIvaRetenido.setReferencia(det.getDocumento());
				}
				ablnoIvaRetenido.setReferencia(pago.getAFavor());
				ablnoIvaRetenido.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
			}
			/*********************************************************************/ 
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				/*PolizaDet cargoIvaRetenidoPendiente=p.agregarPartida();
				cargoIvaRetenidoPendiente.setDescripcion(PolizaContableManager.IVA_RETENIDO_PENDIENTE);
				String pattern="PROV F:{0} {1,date,short}";			
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				cargoIvaRetenidoPendiente.setDescripcion2(descripcion2);
				cargoIvaRetenidoPendiente.setCuenta(getCuenta("117"));
				CantidadMonetaria monto=det.getRetencion2();
				if(monto.amount().doubleValue()!=0){
					cargoIvaRetenidoPendiente.setDebe(monto.amount());
					if(pago.getSucursal()!=null)
						cargoIvaRetenidoPendiente.setReferencia2(pago.getSucursal().getNombre());
					else
						cargoIvaRetenidoPendiente.setReferencia2("NA");
				}
				cargoIvaRetenidoPendiente.setReferencia(pago.getAFavor());
				cargoIvaRetenidoPendiente.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());*/
			}
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				PolizaDet a=p.agregarPartida();				
				a.setCuenta(getCuenta("205"));
				//a.setDescripcion("ISR RETENIDO");
				a.setConcepto(a.getCuenta().getConcepto("IMPR01"));
				String pattern="PROV F:{0} {1,date,short}";			
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				a.setDescripcion2(descripcion2);
				CantidadMonetaria monto=det.getRetencion2();
				if(monto.amount().doubleValue()!=0){
					a.setHaber(monto.amount());
					if(pago.getSucursal()!=null)
						a.setReferencia2(pago.getSucursal().getNombre());
					else
						a.setReferencia2("NA");
				}
				a.setReferencia(pago.getAFavor());
				a.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
			}
		}
	}
	
	private ConceptoContable buscarConceptoContable(String clave){
		String hql="from ConceptoContable c where c.clave=?";
		List<ConceptoContable> res=getHibernateTemplate().find(hql,clave);
		return res.isEmpty()?null:res.get(0);
		
	}

	public static void main(String[] args) {
		DBUtils.whereWeAre();
		PolizaDeGastosController2 controller=new PolizaDeGastosController2();
		controller.generaPoliza(DateUtil.toDate("14/10/2011"));
		//controller.
	}

		
}


