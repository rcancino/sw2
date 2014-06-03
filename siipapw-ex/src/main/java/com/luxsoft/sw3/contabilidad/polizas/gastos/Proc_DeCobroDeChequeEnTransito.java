package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Origen;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

/**
 * Controlador para la generacion de polizas de cobro de cheques en transito
 * de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class Proc_DeCobroDeChequeEnTransito {
	
	
	
	public Proc_DeCobroDeChequeEnTransito(HibernateTemplate hibernateTemplate) {
		
		this.hibernateTemplate = hibernateTemplate;
	}

	public List<Poliza> generaPoliza(final Date fecha) {
		String sql="SELECT B.CARGOABONO_ID " +
				"  FROM sw_bcargoabono B WHERE " +
				"  B.FECHA_COBRO >=?  AND" +
				"  B.ORIGEN in (\'GASTOS\','COMPRAS')" +
				" AND COMENTARIO NOT LIKE '%CANCELA%'" +
				"  AND MONTH(B.FECHA)<>MONTH(B.FECHA_COBRO) ";
				//"  AND ( B.FECHA_COBRO IS NULL OR MONTH(B.FECHA)<>MONTH(B.FECHA_COBRO) )";
		Object[] args=new Object[]{
			new SqlParameterValue(Types.DATE, fecha)	
		};
		List<Long> pagos=ServiceLocator2
				.getJdbcTemplate().queryForList(sql, args,Long.class);
		//System.out.println("Pagos a procesar: "+pagos.size() +"Este es el procesador para cheques en transito");
		
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
		poliza.setClase("COBRO CHEQUE TRANSITO");
		String forma=pago.getFormaDePago().name();
		forma=StringUtils.substring(forma, 0,2);
		String pattern="{0} {1} {2}";
		String ms=MessageFormat.format(pattern, forma,pago.getReferencia(),pago.getAFavor());
		ms=StringUtils.substring(ms, 0,255);
		poliza.setDescripcion(ms+ "(Cobro Transito)");
		
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
		
		String asiento="PAGO";
		
		BigDecimal ietu2=MonedasUtils.calcularImporteDelTotal(pago.getRequisicion().getPorPagar().abs().amount());
			
		BigDecimal ietu=MonedasUtils.calcularImporteDelTotal(pago.getImporteMN().abs().amount());
		
		System.out.println("fecha pago: "+fechaPago+" Descrip: "+pago.getAFavor()+"ietu1: "+ietu+"ietu2: "+ietu2);
		
		BigDecimal iva=MonedasUtils.calcularImpuesto(ietu);
		if(requisicion!=null && pago.getOrigen().equals(Origen.GASTOS)){
			BigDecimal ivaAcumulado=BigDecimal.ZERO;
			BigDecimal ietuAcumulado=BigDecimal.ZERO;
			for(RequisicionDe det:requisicion.getPartidas()){
				ivaAcumulado=ivaAcumulado.add(det.getImpuestoParaPolizaMN().amount());
				ivaAcumulado=ivaAcumulado.subtract(det.getRetencion1().amount());
				ietuAcumulado=ietuAcumulado.add(det.getIetu().amount());
			}
			//ietu=ietuAcumulado.doubleValue()>0?ietuAcumulado:ietu;
			//iva=ivaAcumulado.doubleValue()>0?ivaAcumulado:iva;
			ietu=ietuAcumulado;
			iva=ivaAcumulado;
		}
	
		if(pago.getOrigen().equals(Origen.GASTOS)){
			/*registrarIvaPorAcreditarGastos(poliza, pago, iva, true);
			registrarIvaEnGastos(poliza, pago, iva, false);
			registrarIETUEnGastos(poliza, pago, importe);*/
			//IVA
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", false, iva, "IVA POR ACRED EN GASTOS", pago.getAFavor(), "OFICINAS", asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01", true, iva, "IVA EN GASTOS", pago.getAFavor(), "OFICINAS", asiento);
			
			//IETU
			//PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD02", true, ietu, "IETU DEDUCIBLE GASTOS", pago.getAFavor(), "OFICINAS", asiento);
			//PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU02", false, ietu, "DEDUCIBLE IETU GASTOS", pago.getAFavor(), "OFICINAS", asiento);
			Proc_PagoNormal.registrarIetuAcumuladoPorConcepto(poliza, pago, asiento);
			registrarRetenciones(pago, poliza, asiento);
		
		}else if(pago.getOrigen().equals(Origen.COMPRAS)){
			asiento="PAGO";

			BigDecimal retencion= BigDecimal.ZERO;
			BigDecimal ietuCompra= BigDecimal.ZERO;
			
			if(requisicion.getMoneda().equals(MonedasUtils.PESOS))
			
			for(RequisicionDe det:requisicion.getPartidas()){				
				//final CantidadMonetaria importeFacturado=det.getFacturaDeCompras().getImporteMN();				
				//CantidadMonetaria ivaFac=MonedasUtils.calcularImpuesto(importeFacturado);
				
				//Retencion de flete
				retencion=det.getFacturaDeCompras().getRetencionflete();
				
				
				
				BigDecimal ivaSinRetencion=MonedasUtils.calcularImpuestoDelTotal(det.getTotalMN().amount());	
		//		BigDecimal ivaSinRetencion=ivaFac.amount().subtract(retencion);			
				
				
				String desc=MessageFormat.format("Fac {0} ({1,date,short}) ({2})", det.getDocumento(),det.getFechaDocumento(),det.getRequisicion().getMoneda());

				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02", false, ivaSinRetencion, desc, pago.getAFavor(), "OFICINAS", asiento);
							
				PolizaDetFactory.generarPolizaDet(poliza, "117","IVAR01",true,retencion,desc,pago.getAFavor(),"TODAS", asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "117","IVAR02",false,retencion,desc,pago.getAFavor(),"TODAS", asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "205","IMPR02",false,retencion,desc,pago.getAFavor(),"TODAS", asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "205","IMPR03",true,retencion,desc,pago.getAFavor(),"TODAS", asiento);

				//IVA en compras
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC01", true, ivaSinRetencion, desc, pago.getAFavor(), "OFICINAS", asiento);
			
				//IETU en Compras			
				
				System.out.println("fecha pago: "+fechaPago+" Descrip: "+pago.getAFavor()+" ietu1: "+ietu+" ietu2: "+ietu2+" iva2222: "+ivaSinRetencion);
				
	//			ietuCompra=ietuCompra.add(det.getFacturaDeCompras().getImporteMN().amount());
				ietuCompra=ietu2;
				
			}			
			//IETU
			PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD01", true, ietuCompra, "IETU DEDUCIBLE COMPRAS", pago.getAFavor(), "OFICINAS", asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU01", false, ietuCompra, "DEDUCIBLE IETU COMPRAS", pago.getAFavor(), "OFICINAS", asiento);	
					
			if(!requisicion.getMoneda().equals(MonedasUtils.PESOS))
				generarDiferenciaCambiaria(poliza, pago);			
		}
		return poliza;
	}
	
	private void registrarRetenciones(final CargoAbono pago,Poliza p,String asiento){
		
		
		if(pago.getRequisicion()!=null){
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){	
				String pattern="PROV F:{0} {1,date,short}";				
				String desc2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				CantidadMonetaria monto=det.getRetencion1();
				
				String ref2="";
				if(monto.amount().doubleValue()!=0){
					if(pago.getSucursal()!=null)
						ref2=pago.getSucursal().getNombre();
					
				}
				PolizaDetFactory.generarPolizaDet(p, "117","IVAR01", true, monto.amount().abs(), desc2, pago.getAFavor(), ref2, asiento);
				PolizaDetFactory.generarPolizaDet(p, "117","IVAR02", false, monto.amount().abs(), desc2, pago.getAFavor(), ref2, asiento);
				
			}
			
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				String pattern="PROV F:{0} {1,date,short}";						
				String desc2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				CantidadMonetaria monto=det.getRetencion1();
				boolean cargo=true;
				String ref2="";
				if(monto.amount().doubleValue()!=0){
					cargo=false;
				}
				PolizaDetFactory.generarPolizaDet(p, "205","IMPR02", cargo, monto.amount().abs()
						, desc2, pago.getAFavor()
						, det.getFacturaDeGasto().getCompra().getSucursal().getNombre()
						, asiento);
				PolizaDetFactory.generarPolizaDet(p, "205","IMPR03", !cargo, monto.amount().abs()
						, desc2, pago.getAFavor()
						, det.getFacturaDeGasto().getCompra().getSucursal().getNombre()
						, asiento);
				
			}			
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){	
				
				String pattern="PROV F:{0} {1,date,short}";						
				String desc2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				CantidadMonetaria monto=det.getRetencion2();
				boolean cargo=true;
				if(monto.amount().doubleValue()!=0){
					cargo=false;
				}
				PolizaDetFactory.generarPolizaDet(p, "205","IMPR01", cargo, monto.amount().abs(), desc2, pago.getAFavor()
						, det.getFacturaDeGasto().getCompra().getSucursal().getNombre(), asiento);
				PolizaDetFactory.generarPolizaDet(p, "205","IMPR04", !cargo, monto.amount().abs(), desc2, pago.getAFavor()
						, det.getFacturaDeGasto().getCompra().getSucursal().getNombre(), asiento);
			}
		}
	}
	
	private void generarDiferenciaCambiaria(final Poliza poliza,CargoAbono pago){
		String asiento="PAGO";
		Requisicion requisicion=pago.getRequisicion();
		
		BigDecimal ietuCompra= BigDecimal.ZERO;
		
		for(RequisicionDe det:requisicion.getPartidas()){
			
			final CantidadMonetaria totalFacturado=det.getFacturaDeCompras().getTotalMN();
			//double tc=ServiceLocator2.buscarTipoDeCambio(pago.getFecha());
			double tc=det.getRequisicion().getTipoDeCambio().doubleValue();
			CantidadMonetaria totalPagado=CantidadMonetaria.pesos(det.getFacturaDeCompras().getTotal());
			totalPagado=totalPagado.multiply(tc);
			
			System.out.println("  Total  Pagado  "+totalPagado+ " Tc:"+tc);
			
			CantidadMonetaria diferenciaCambiaria=totalFacturado.subtract(totalPagado);
			BigDecimal iva=MonedasUtils.calcularImpuestoDelTotal(diferenciaCambiaria.amount());
			iva=PolizaUtils.redondear(iva);
			//ivaAcumulado=ivaAcumulado.add(iva);
			String desc=MessageFormat.format("Fac {0} ({1,date,short}) ({2}) Tc:{3} Tc-Fac: {4}", det.getDocumento(),det.getFechaDocumento(),det.getRequisicion().getMoneda(),tc,det.getRequisicion().getTipoDeCambio());
			
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC01", true
					, PolizaUtils.redondear(MonedasUtils.calcularImpuestoDelTotal(totalPagado.amount()))
					, desc, pago.getAFavor(), "OFICINAS", asiento);
			
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02", false
					, PolizaUtils.redondear(MonedasUtils.calcularImpuestoDelTotal(totalPagado.amount()))
					, desc, pago.getAFavor(), "OFICINAS", asiento);
			
			if(iva.doubleValue()>0){
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02", false, iva.abs()
						, desc, pago.getAFavor(), "OFICINAS", asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "705", "GSTF01", true, iva.abs()
						, desc, pago.getAFavor(), "OFICINAS", asiento);
			}else if(iva.doubleValue()<0){
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02", true, iva.abs(), desc, pago.getAFavor(), "OFICINAS", asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "701", "PRFN04", false, iva.abs(), desc, pago.getAFavor(), "OFICINAS", asiento);
				
			}
			
			//IETU en Compras
		//	ietuCompra=ietuCompra.add(det.getFacturaDeCompras().getImporteMN().amount());
			 
			ietuCompra=ietuCompra.add(PolizaUtils.redondear(MonedasUtils.calcularImpuestoDelTotal(totalPagado.amount())).divide(new BigDecimal(0.16),2,RoundingMode.HALF_EVEN));
	
		}
		
		//IETU
		PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD01", true, ietuCompra, "IETU DEDUCIBLE COMPRAS", pago.getAFavor(), "OFICINAS", asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU01", false, ietuCompra, "DEDUCIBLE IETU COMPRAS", pago.getAFavor(), "OFICINAS", asiento);	
		
	
	}
	
	private double getTipoDeCambioDelMes(final Date fecha){
		try {
			Periodo p=Periodo.getPeriodoEnUnMes(fecha);
			Date fechaX=DateUtils.addDays(p.getFechaFinal(), -1);
			String sql="select max(factor) from sx_tipo_de_cambio where fecha=?";
			Double res=(Double)ServiceLocator2.getJdbcTemplate().queryForObject(sql, new Object[]{fechaX},Double.class);
			Assert.notNull(res,MessageFormat.format("No encontro T.C para la fecha: {0,date,short} Mes: {1} ",fechaX));
			return res.doubleValue();
			
		} catch (EmptyResultDataAccessException de) {
			de.printStackTrace();
			throw new RuntimeException("No existe tipo de cambio para la fecha: "+fecha);
			
		}
		
		
		
	}
	
	
	private final HibernateTemplate hibernateTemplate;
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}
	
	public static void main(String[] args) {
		Proc_DeCobroDeChequeEnTransito p=new Proc_DeCobroDeChequeEnTransito(ServiceLocator2.getHibernateTemplate());
		//double tc=p.getTipoDeCambioDelMes(DateUtil.toDate("29/11/2011"));
		//System.out.println(tc);
	}
	
		
}


