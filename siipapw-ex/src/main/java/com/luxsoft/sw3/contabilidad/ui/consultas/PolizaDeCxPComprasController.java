package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.PolizaContableManager;
import com.luxsoft.utils.LoggerHelper;
import com.mysql.jdbc.Util;

/**
 * Implementacion de {@link PolizaContableManager} para la generación y mantenimiento de la poliza de Compras - Almacen
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeCxPComprasController {
	
	Logger logger=LoggerHelper.getLogger();
	
	private EventList<PagoCXP> pagos;
	private GroupingList<PagoCXP> pagosPorGrupo;
	
	protected List<Poliza> generaPoliza(final Date fecha) {
		final List<Poliza> polizas=new ArrayList<Poliza>(0);
		cargarDatos(fecha);
		for(List<PagoCXP> grupo:pagosPorGrupo){
			Poliza poliza=new Poliza();
			poliza.setFecha(fecha);
			poliza.setTipo(Poliza.Tipo.EGRESO);
			for(Iterator<PagoCXP> it=grupo.iterator();it.hasNext();){
				PagoCXP pago=it.next();
				if(!it.hasNext()){					
					poliza.setClase("CXP COMPRAS");			
					String descripcion="{0} {1} {2} {3}";					
					poliza.setDescripcion(MessageFormat.format(descripcion
							,pago.getBANCO()
							,pago.getFORMAPAGO()
							,pago.getREFERENCIA()
							,pago.getNOMBRE()
							)
							);
				}
			}
			generarPartidas(poliza, grupo);
			poliza.actualizar();
			polizas.add(poliza);
		}
		return polizas;
	}
	
	/**
	 * Carga todos los registros
	 * 
	 * @param fecha
	 */
	private void cargarDatos(final Date fecha){
		String sql="SELECT b.CARGOABONO_ID,B.fecha,B.REFERENCIA,B.CUENTA_ID,(SELECT X.DESCRIPCION FROM SW_CUENTAS X WHERE X.ID=B.CUENTA_ID) AS BANCO"
			+" ,(SELECT X.NUMERO FROM SW_CUENTAS X WHERE X.ID=B.CUENTA_ID) AS CONCEPTO"
			+" ,CASE WHEN T.FORMADEPAGO=0 THEN 'T.' ELSE 'CH' END AS FORMAPAGO,C.CLAVE AS CLAVEPROV,C.NOMBRE,-B.IMPORTE as IMPORTE"
			+" ,C.CXP_ID,C.DOCUMENTO,C.FECHA as FECHA_DOCTO,C.MONEDA,C.TC,c.TOTAL,C.IMPORTE,C.IMPUESTO,C.FLETE,C.FLETE_IVA,C.FLETE_RET"
			+" ,ROUND(C.TOTAL*C.TC,2) AS TOT_MN,D.TOTAL*t.TC AS APAGAR"
			+" FROM SX_CXP C" 
			+" JOIN sw_trequisiciondet D ON(C.CXP_ID=D.CXP_ID)"
			+" JOIN sw_trequisicion T ON(T.REQUISICION_ID=D.REQUISICION_ID)"
			+" JOIN sw_bcargoabono B ON(T.CARGOABONO_ID=B.CARGOABONO_ID)"
			//+" WHERE  T.CONCEPTO_ID=201136 AND B.fecha =?";
			+" WHERE  B.fecha =?";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,fecha)
		};	
		System.out.println(sql);
		pagos=GlazedLists.eventList(ServiceLocator2.getJdbcTemplate().query(sql, params, new BeanPropertyRowMapper(PagoCXP.class)));
		Comparator c=GlazedLists.beanPropertyComparator(PagoCXP.class, "CARGOABONO_ID");
		pagosPorGrupo=new GroupingList<PagoCXP>(pagos,c);
	}
	
	private void generarPartidas(final Poliza poliza,List<PagoCXP> pagos){
		
			BigDecimal flete= new BigDecimal(0);	
			BigDecimal fleteIva= new BigDecimal(0);
			BigDecimal fleteRet= new BigDecimal(0);
			
		
		//Abono a bancos UNA VEZ
		for (PagoCXP p:pagos){
			flete=flete.add(p.getFLETE());
			fleteIva=fleteIva.add(p.getFLETE_IVA());
			fleteRet=fleteRet.add(p.getFLETE_RET());
		}
	
		PagoCXP pag=pagos.get(0);
		
		String conceptoClave=pag.getCONCEPTO();
		PolizaDet abonoBancos=poliza.agregarPartida();
		abonoBancos.setCuenta(getCuenta("102"));
		abonoBancos.setHaber(pag.getIMPORTE());
		abonoBancos.setAsiento("PAGO");
		//abonoBancos.setDescripcion(pag.getBANCO());
		abonoBancos.setConcepto(abonoBancos.getCuenta().getConcepto(conceptoClave));
		abonoBancos.setDescripcion2(MessageFormat.format("{0} {1}"+" Cta.Destino "+"{2}"
				,pag.getFORMAPAGO()
				,pag.getREFERENCIA()
				,pag.getCONCEPTO())
				);
		abonoBancos.setReferencia(pag.getNOMBRE());
		abonoBancos.setReferencia2("");	
		
		
		PolizaDet cargoIvaBancos=poliza.agregarPartida();
		cargoIvaBancos.setCuenta(getCuenta("117"));
		cargoIvaBancos.setDebe(round(MonedasUtils.calcularImpuesto(
						MonedasUtils.calcularImporteDelTotal(pag.getIMPORTE().subtract(flete.add(fleteIva).subtract(fleteRet)))
						.add(flete)).subtract(fleteRet)));
		
		//cargoIvaBancos.setDebe(round(MonedasUtils.calcularImpuestoDelTotal(pag.getIMPORTE())));
		cargoIvaBancos.setAsiento("PAGO");
		cargoIvaBancos.setDescripcion(PolizaContableManager.IVA_EN_COMPRAS);
		cargoIvaBancos.setDescripcion2(MessageFormat.format("{0} {1}"
				,pag.getFORMAPAGO()
				,pag.getREFERENCIA())
				);
		cargoIvaBancos.setReferencia(pag.getNOMBRE());
		cargoIvaBancos.setReferencia2("");	
		
		
		
		for(Iterator<PagoCXP> it=pagos.iterator();it.hasNext();){
			PagoCXP pago=it.next();
			//Cargo a probeedor
			PolizaDet cargoProveedores=poliza.agregarPartida();
			cargoProveedores.setCuenta(getCuenta("200"));
			cargoProveedores.setDebe(pago.getAPAGAR());
			if(pago.getMONEDA().equals("USD")){
				BigDecimal tc=BigDecimal.valueOf(getTipoDeCambioDelMes(pago.getFECHA_DOCTO()));
				BigDecimal totalFactura=pago.getTOTAL();
				BigDecimal cargo=totalFactura.multiply(tc);
				cargoProveedores.setDebe(round(cargo));
			}
			cargoProveedores.setAsiento("PAGO");
			ConceptoContable concepto=cargoProveedores.getCuenta().getConcepto(pago.getCLAVEPROV());
			if(concepto==null){
				Proveedor proveedor=ServiceLocator2.getProveedorManager().buscarPorClave(pago.getCLAVEPROV());
				concepto=new ConceptoContable();
				concepto.setCuenta(cargoProveedores.getCuenta());
				cargoProveedores.getCuenta().getConceptos().add(concepto);
				concepto.setClave(pago.getCLAVEPROV());
				concepto.setDescripcion(proveedor.getNombreRazon());
				concepto=(ConceptoContable)ServiceLocator2.getUniversalDao().save(concepto);
			}
			cargoProveedores.setConcepto(concepto);
			cargoProveedores.setDescripcion("PROVEEDORES");
			String descripcion2=MessageFormat.format("Fac: {0} {1,date,short}"
					, pago.getDOCUMENTO(),pago.getFECHA_DOCTO());
			cargoProveedores.setDescripcion2(descripcion2);
			cargoProveedores.setReferencia(pago.getNOMBRE());
			cargoProveedores.setReferencia2("");
			
			//Abono a IVA 
			PolizaDet abonoIvaPorAcreditar=poliza.agregarPartida();
			abonoIvaPorAcreditar.setCuenta(getCuenta("117"));
			//abonoIvaPorAcreditar.setHaber(round(MonedasUtils.calcularImpuestoDelTotal(round(pago.getAPAGAR()))) );
			
			abonoIvaPorAcreditar.setHaber(round(MonedasUtils.calcularImpuesto(
					MonedasUtils.calcularImporteDelTotal(pago.getAPAGAR().subtract(pago.getFLETE().add(pago.getFLETE_IVA()).subtract(pago.getFLETE_RET())))
					.add(pago.getFLETE())).subtract(pago.getFLETE_RET())));
			
			abonoIvaPorAcreditar.setAsiento("PAGO");
			abonoIvaPorAcreditar.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_COMPRAS);
			abonoIvaPorAcreditar.setDescripcion2(descripcion2);
			abonoIvaPorAcreditar.setReferencia(pago.getNOMBRE());
			abonoIvaPorAcreditar.setReferencia2("");
		}
		
		BigDecimal mpIETU=BigDecimal.ZERO;
		BigDecimal fleteIETU=BigDecimal.ZERO;
		for(PagoCXP pago1:pagos){
			mpIETU=mpIETU.add(round(
					MonedasUtils.calcularImporteDelTotal(pago1.getAPAGAR().subtract(pago1.getFLETE().add(pago1.getFLETE_IVA()).subtract(pago1.getFLETE_RET())))
					));
			fleteIETU=fleteIETU.add(pago1.getFLETE());
			System.out.println("IETU MP " +mpIETU);
			System.out.println("IETU flete "+fleteIETU);
			
		}
		
		PolizaDet cargoIETU=poliza.agregarPartida();
		cargoIETU.setCuenta(getCuenta("900"));
		cargoIETU.setDebe(mpIETU.add(fleteIETU));
		cargoIETU.setAsiento("PAGO");
		cargoIETU.setDescripcion("IETU DEDUCIBLE COMPRAS");
		cargoIETU.setDescripcion2(MessageFormat.format("{0} {1}"
				,pag.getFORMAPAGO()
				,pag.getREFERENCIA())
				);
		cargoIETU.setReferencia(pag.getNOMBRE());
		cargoIETU.setReferencia2("");
		
		PolizaDet abonoIETU=poliza.agregarPartida();
		abonoIETU.setCuenta(getCuenta("901"));
	//	abonoIETU.setHaber(ivaIETU1.divide(BigDecimal.valueOf(0.16)).add(fleteIETU));
		abonoIETU.setHaber(mpIETU.add(fleteIETU));
		abonoIETU.setAsiento("PAGO");
		abonoIETU.setDescripcion("DEDUCIBLE IETU COMPRAS");
		abonoIETU.setDescripcion2(MessageFormat.format("{0} {1}"
				,pag.getFORMAPAGO()
				,pag.getREFERENCIA())
				);
		abonoIETU.setReferencia(pag.getNOMBRE());
		abonoIETU.setReferencia2("");
		generarDiferenciaCambiaria(poliza, pagos);
		generarRetenciones(poliza, pagos);
		//procesarAnticipos(poliza)
	}
	
	private void generarDiferenciaCambiaria(final Poliza poliza, List<PagoCXP> pagos){
		
		if(!pagos.get(0).getMONEDA().equals("USD"))
			return;
		
		BigDecimal diferencia=BigDecimal.ZERO;
		BigDecimal importePagado=pagos.get(0).getIMPORTE();
		BigDecimal importeFacturado=BigDecimal.ZERO;
		BigDecimal diferenciaIva=BigDecimal.ZERO;
		for(PagoCXP pago:pagos){
			BigDecimal importe=BigDecimal.valueOf(pago.getTOT_MN());			
			if(!DateUtil.isSameMonth(pago.getFecha(), pago.getFECHA_DOCTO())){
				BigDecimal tc=BigDecimal.valueOf(getTipoDeCambioDelMes(pago.getFECHA_DOCTO()));
				BigDecimal totalFactura=pago.getTOTAL();
				importe=totalFactura.multiply(tc);
			}
			importeFacturado=importeFacturado.add(importe);
			
			BigDecimal ivaDiferencia=MonedasUtils.calcularImpuestoDelTotal( BigDecimal.valueOf(pago.getTOT_MN()).subtract(pago.getAPAGAR()) );
			diferenciaIva=diferenciaIva.add(ivaDiferencia);
			//Abono a IVA 
			PolizaDet abonoIvaPorAcreditar=poliza.agregarPartida();
			abonoIvaPorAcreditar.setCuenta(getCuenta("117"));
			abonoIvaPorAcreditar.setHaber(round(ivaDiferencia));
			abonoIvaPorAcreditar.setAsiento("PAGO");
			abonoIvaPorAcreditar.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_COMPRAS);
			abonoIvaPorAcreditar.setDescripcion2("");
			abonoIvaPorAcreditar.setReferencia(pago.getNOMBRE());
			abonoIvaPorAcreditar.setReferencia2("");
			
		}
		diferencia=importePagado.subtract(importeFacturado);
		// Aplicamos la diferencia cambiara acumulada
		
		PolizaDet difCambiaria=poliza.agregarPartida();
		if(diferencia.doubleValue()>0){
			difCambiaria.setCuenta(getCuenta("701"));
			difCambiaria.setDebe(round(diferencia.abs()));
		}
		else{
			difCambiaria.setCuenta(getCuenta("705"));
			difCambiaria.setHaber(round(diferencia.abs()));
		}
		difCambiaria.setAsiento("PAGO");
		difCambiaria.setDescripcion("VARIACION CAMBIARIA");
		difCambiaria.setDescripcion2(MessageFormat.format("{0} {1}"
				,pagos.get(0).getFORMAPAGO()
				,pagos.get(0).getREFERENCIA())
				);
		difCambiaria.setReferencia(pagos.get(0).getNOMBRE());
		difCambiaria.setReferencia2("");
		
		PolizaDet ivaDiferenciaCambiaria=poliza.agregarPartida();
		if(diferenciaIva.doubleValue()>0){
			ivaDiferenciaCambiaria.setCuenta(getCuenta("701"));
			ivaDiferenciaCambiaria.setDebe(round(diferenciaIva.abs()));
		}
		else{
			ivaDiferenciaCambiaria.setCuenta(getCuenta("705"));
			ivaDiferenciaCambiaria.setHaber(round(diferenciaIva.abs()));
		}
		ivaDiferenciaCambiaria.setAsiento("PAGO");
		ivaDiferenciaCambiaria.setDescripcion("VARIACION CAMBIARIA IVA");
		ivaDiferenciaCambiaria.setDescripcion2(MessageFormat.format("{0} {1}"
				,pagos.get(0).getFORMAPAGO()
				,pagos.get(0).getREFERENCIA())
				);
		ivaDiferenciaCambiaria.setReferencia(pagos.get(0).getNOMBRE());
		ivaDiferenciaCambiaria.setReferencia2("");
		
	}
	
	private void generarRetenciones(final Poliza poliza,List<PagoCXP> pagos){
		BigDecimal retencion=BigDecimal.ZERO;
		for(PagoCXP pago:pagos){
			retencion=retencion.add(pago.getFLETE_RET());
		}
		
		if(retencion.doubleValue()>0){
			PagoCXP pago=pagos.get(0);
			PolizaDet cargoIvaAcreditableRetenido=poliza.agregarPartida();
			cargoIvaAcreditableRetenido.setCuenta(getCuenta("117"));
			cargoIvaAcreditableRetenido.setDebe(retencion);
			cargoIvaAcreditableRetenido.setAsiento("PAGO");
			cargoIvaAcreditableRetenido.setDescripcion(PolizaContableManager.IVA_ACREDITABLE_RETENIDO);
							
			cargoIvaAcreditableRetenido.setDescripcion2(MessageFormat.format("{0} {1}"
					,pago.getFORMAPAGO()
					,pago.getREFERENCIA())
					);
			cargoIvaAcreditableRetenido.setReferencia(pago.getNOMBRE());
			cargoIvaAcreditableRetenido.setReferencia2("");
			
		}
		
		for(Iterator<PagoCXP> it=pagos.iterator();it.hasNext();){
			PagoCXP pago=it.next();
			
			
			if(pago.getFLETE().doubleValue()>0){
				
				BigDecimal imp=pago.getFLETE_RET();
				
				PolizaDet abonoIvaPorAcreditarRet=poliza.agregarPartida();
				abonoIvaPorAcreditarRet.setCuenta(getCuenta("117"));
				abonoIvaPorAcreditarRet.setHaber(imp);
				abonoIvaPorAcreditarRet.setAsiento("PAGO");
				abonoIvaPorAcreditarRet.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_RETENIDO);
				final String descripcion2=MessageFormat.format("Fac: {0} {1,date,short}", pago.getDOCUMENTO(),pago.getFECHA_DOCTO());				
				abonoIvaPorAcreditarRet.setDescripcion2(descripcion2);
				abonoIvaPorAcreditarRet.setReferencia(pago.getNOMBRE());
				abonoIvaPorAcreditarRet.setReferencia2("");
				
				PolizaDet cargoIvaRetenidoPendiente=poliza.agregarPartida();
				cargoIvaRetenidoPendiente.setCuenta(getCuenta("205"));
				cargoIvaRetenidoPendiente.setDebe(imp);
				cargoIvaRetenidoPendiente.setAsiento("PAGO");
				cargoIvaRetenidoPendiente.setDescripcion(PolizaContableManager.IVA_RETENIDO_PENDIENTE);
				cargoIvaRetenidoPendiente.setDescripcion2(descripcion2);
				cargoIvaRetenidoPendiente.setReferencia(pago.getNOMBRE());
				cargoIvaRetenidoPendiente.setReferencia2("");
				
				PolizaDet abonoIvaRetenido=poliza.agregarPartida();
				abonoIvaRetenido.setCuenta(getCuenta("205"));
				abonoIvaRetenido.setHaber(imp);
				abonoIvaRetenido.setAsiento("PAGO");
				abonoIvaRetenido.setDescripcion(PolizaContableManager.IVA_RETENIDO);
				abonoIvaRetenido.setDescripcion2(descripcion2);
				abonoIvaRetenido.setReferencia(pago.getNOMBRE());
				abonoIvaRetenido.setReferencia2("");
			}
			
		}
	}
	
	
	public CuentaContable getCuenta(String clave){
		return ServiceLocator2.getCuentasContablesManager().buscarPorClave(clave);
	}
	
	public double getTipoDeCambioDelMes(final Date fecha){
		Periodo p=Periodo.getPeriodoEnUnMes(fecha);
		Date fechaX=DateUtils.addDays(p.getFechaFinal(), -1);
		String sql="select factor from sx_tipo_de_cambio where fecha=?";
		Double res=(Double)ServiceLocator2.getJdbcTemplate().queryForObject(sql, new Object[]{fechaX},Double.class);
		Assert.notNull(res,MessageFormat.format("No encontro T.C para la fecha: {0,date,short} Mes: {1} ",fechaX));
		return res.doubleValue();
		
	}
	/*	
	private void procesarAnticipos(final Poliza poliza){
		String hql="from CargoAbono c where " +
				" c.fecha=? and " +
				" c.requisicion.concepto.id=201136 ";
		List<CargoAbono> anticipos=ServiceLocator2.getHibernateTemplate()
					.find(hql, poliza.getFecha());
		for(CargoAbono pag :anticipos){
			PolizaDet abonoBancos=poliza.agregarPartida();
			abonoBancos.setCuenta(getCuenta("102"));
			abonoBancos.setHaber(pag.getImporte());
			abonoBancos.setAsiento("ANTICIPO COMPRAS");
			//abonoBancos.setDescripcion(pag.getBANCO());
			//abonoBancos.setConcepto(abonoBancos.getCuenta().getConcepto(conceptoClave));
			abonoBancos.setDescripcion2("Pendiente");
			abonoBancos.setReferencia("Pendiente");
			abonoBancos.setReferencia2("");
		}
	}*/
			
	public static void main(String[] args) {
		PolizaDeAlmacenController model=new PolizaDeAlmacenController();
		model.generarPoliza(DateUtil.toDate("12/08/2010"));
		
	}

	
	public static class PagoCXP{
		
		private long CARGOABONO_ID;
		private Date fecha;
		private String REFERENCIA;
		private long CUENTA_ID;
		private String BANCO;
		private String FORMAPAGO;
		private String CLAVEPROV;
		private String NOMBRE;
		private BigDecimal IMPORTE;
		private long CXP_ID;
		private String DOCUMENTO;
		private Date FECHA_DOCTO;
		private String MONEDA;
		private double TC;
		private BigDecimal APAGAR;
		private BigDecimal TOTAL;
		private BigDecimal IMPUESTO;
		private BigDecimal FLETE;
		private BigDecimal FLETE_IVA;
		private BigDecimal FLETE_RET;
		private double TOT_MN;
		private String CONCEPTO;
		
		public long getCARGOABONO_ID() {
			return CARGOABONO_ID;
		}
		public void setCARGOABONO_ID(long cargoabono_id) {
			CARGOABONO_ID = cargoabono_id;
		}
		public Date getFecha() {
			return fecha;
		}
		public void setFecha(Date fecha) {
			this.fecha = fecha;
		}
		public String getREFERENCIA() {
			return REFERENCIA;
		}
		public void setREFERENCIA(String referencia) {
			REFERENCIA = referencia;
		}
		public long getCUENTA_ID() {
			return CUENTA_ID;
		}
		public void setCUENTA_ID(long cuenta_id) {
			CUENTA_ID = cuenta_id;
		}
		
		
		public String getBANCO() {
			return BANCO;
		}
		public void setBANCO(String formapago) {
			BANCO = formapago;
		}
		
		
		
		public String getFORMAPAGO() {
			return FORMAPAGO;
		}
		public void setFORMAPAGO(String formapago) {
			FORMAPAGO = formapago;
		}
		
		public String getCLAVEPROV() {
			return CLAVEPROV;
		}
		public void setCLAVEPROV(String cLAVEPROV) {
			CLAVEPROV = cLAVEPROV;
		}
		public String getNOMBRE() {
			return NOMBRE;
		}
		public void setNOMBRE(String nombre) {
			NOMBRE = nombre;
		}
		public BigDecimal getIMPORTE() {
			return IMPORTE;
		}
		
		public void setIMPORTE(BigDecimal importe) {
			IMPORTE = importe;
		}
		public long getCXP_ID() {
			return CXP_ID;
		}
		public void setCXP_ID(long cxp_id) {
			CXP_ID = cxp_id;
		}
		public String getDOCUMENTO() {
			return DOCUMENTO;
		}
		public void setDOCUMENTO(String documento) {
			DOCUMENTO = documento;
		}
		
		public String getCONCEPTO() {
			return CONCEPTO;
		}
		public void setCONCEPTO(String cONCEPTO) {
			CONCEPTO = cONCEPTO;
		}
		public String getMONEDA() {
			return MONEDA;
		}
		public void setMONEDA(String moneda) {
			MONEDA = moneda;
		}
		public double getTC() {
			return TC;
		}
		public void setTC(double tc) {
			TC = tc;
		}
		public BigDecimal getTOTAL() {
			return TOTAL;
		}
		public void setTOTAL(BigDecimal total) {
			TOTAL = total;
		}
		public BigDecimal getIMPUESTO() {
			return IMPUESTO;
		}
		public void setIMPUESTO(BigDecimal impuesto) {
			IMPUESTO = impuesto;
		}
		public BigDecimal getFLETE() {
			return FLETE;
		}
		public void setFLETE(BigDecimal flete) {
			FLETE = flete;
		}
		public BigDecimal getFLETE_IVA() {
			return FLETE_IVA;
		}
		public void setFLETE_IVA(BigDecimal flete_iva) {
			FLETE_IVA = flete_iva;
		}
		public BigDecimal getFLETE_RET() {
			return FLETE_RET;
		}
		public void setFLETE_RET(BigDecimal flete_ret) {
			FLETE_RET = flete_ret;
		}
		public double getTOT_MN() {
			return TOT_MN;
		}
		public void setTOT_MN(double tot_mn) {
			TOT_MN = tot_mn;
		}
		public BigDecimal getAPagarMonedaNacional(){
			return CantidadMonetaria.pesos(getAPAGAR()).amount();
		}
		public Date getFECHA_DOCTO() {
			return FECHA_DOCTO;
		}
		public void setFECHA_DOCTO(Date fecha_docto) {
			FECHA_DOCTO = fecha_docto;
		}
		public BigDecimal getAPAGAR() {
			return APAGAR;
		}
		public void setAPAGAR(BigDecimal apagar) {
			APAGAR = apagar;
		}
		
		
	}
	
	public Poliza salvar(Poliza target){
		boolean existe=ServiceLocator2.getPolizasManager().existe(target);
		/*if(existe){
			MessageUtils.showConfirmationMessage(msg, title)
		}*/
		return ServiceLocator2.getPolizasManager().salvarPoliza(target);
	}
	
	
	public static BigDecimal round(BigDecimal v){
		return CantidadMonetaria.pesos(v).amount();
	}
	
}

/*
BigDecimal ivaPagado=MonedasUtils.calcularImpuestoDelTotal(pago.getIMPORTE());
BigDecimal ivaEstimado=BigDecimal.ZERO;			
ivaEstimado=ivaEstimado.add(MonedasUtils.calcularImpuestoDelTotal(importe));
*/

/*BigDecimal ivadiferenciaCambiaria=ivaPagado.subtract(ivaEstimado);			
*/	
