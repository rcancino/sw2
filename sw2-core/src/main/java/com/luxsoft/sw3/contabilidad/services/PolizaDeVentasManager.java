package com.luxsoft.sw3.contabilidad.services;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.cxp.CxP2.Origen;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.tesoreria.model.CorreccionDeFicha;
import com.luxsoft.sw3.tesoreria.model.TipoDeAplicacion;

/**
 * Implementacion de {@link PolizaContableManager} para la generación y administracion
 * de polizas de ventas segun las reglas de negocios vigentes
 *  
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeVentasManager  implements PolizaContableManager{
	
	
	
	private EventList<CFactura> ventas;
	
	private Map<String, List<CFactura>> ventasPorTipo;
	
	private EventList<CIngresoContado> ingresos;
	
	private Map<String, List<CIngresoContado>> ingresosPorOrigen;
	
	private Map<String, List<CCobranza>> cobranzaPorOrigen;
	
	
	
	private Poliza poliza;
	
	public PolizaDeVentasManager (){}
	
	private void inicializar(final Date fecha){
		poliza=new Poliza();
		poliza.setFecha(fecha);
		poliza.setTipo(Poliza.Tipo.INGRESO);
		poliza.setClase("VENTAS");
		poliza.setDescripcion(MessageFormat.format("Poliza de ventas del {0,date,short}",fecha));
		
	}
	
	private void inicializarDatos(){
		inicializarVentas();
		cargarIngresosDeTesoreria();
		cargarCobranza();
	}
	
	/**
	 * Cargar las ventas del dia en memeoria para su procesamiento
	 * 
	 * @param poliza
	 */
	public void inicializarVentas(){		
		ventas=GlazedLists.eventList(CFactura.buscarFacturas(poliza.getFecha()));		
		FunctionList.Function<CFactura, String> function=new FunctionList.Function<CFactura, String>(){
			public String evaluate(CFactura sourceValue) {
				return sourceValue.getORIGEN();
			}			
		};
		ventasPorTipo=GlazedLists.syncEventListToMultiMap(ventas, function);
		/*
		System.out.println("Ventas: "+ventas.size());
		for(Map.Entry<String, List<CFactura>> entry:ventasPorTipo.entrySet()){
			System.out.println("\tTipo: "+entry.getKey()+ " Registros: "+entry.getValue().size());
		}*/
	}
	
	public void cargarIngresosDeTesoreria(){		
		ingresos=GlazedLists.eventList(CIngresoContado.buscarIngresos(poliza.getFecha()));
		FunctionList.Function<CIngresoContado, String> function=new FunctionList.Function<CIngresoContado, String>(){
			public String evaluate(CIngresoContado sourceValue) {
				return sourceValue.getORIGEN();
			}						
		};
		ingresosPorOrigen=GlazedLists.syncEventListToMultiMap(ingresos, function);
	}
	
	private void cargarCobranza(){
		cobranzaPorOrigen=ContabilidadSqlSupport.getInstance().buscarCobranzaPorOrigen(poliza.getFecha());
	}
	
	public  Poliza generarPoliza(final Date fecha){
		
		inicializar(fecha);		
		// Carga de informacion;
		
		inicializarDatos();
		
		registrarVentasCreditoCamioneta();		
		registrarVentasMostrador();
		registrarComisionesBancarias();
		registrarSaldosAFavor("MOS","Ventas MOS");
		registrarSaldosAFavorDeOtrasCarteras();
		registrarCobranzaCamioneta();
		registrarSaldosAFavor("CAM","Cobranza CAM");
		registrarAnticipos("Cobranza CAM");
		registrarIETU();
		registrarDevoluciones();
		//registrarDepositosEnTransito();
		registrarFaltantes();
		poliza.actualizar();
		return poliza;
	}
	
	/**
	 * Contabiliza las ventas CRE y CAM del día  mediante el siguiente asiento contable
	 * 
	 *  Abono a Ventas CRE(401) e IVA(206) con cargo a clientes CRE
	 *  Abono a Ventas CAM(401) e IVA(206) con cargo a clientes CAM
	 *   
	 *  
	 * 
	 * @param poliza
	 */
	private  void registrarVentasCreditoCamioneta(){		
		
		for(Map.Entry<String, List<CFactura>> entry:ventasPorTipo.entrySet()){
			String origen=entry.getKey();
			if(origen.equals("CRE")|| origen.equals("CAM")){
				List<CFactura> ventas=entry.getValue();
				GroupingList<CFactura> ventasPorSucursal=new GroupingList<CFactura>(GlazedLists.eventList(ventas),GlazedLists.beanPropertyComparator(CFactura.class, "SUCURSAL"));
				final String asiento="Ventas CXC";
				for(List<CFactura> facs:ventasPorSucursal){
					BigDecimal importe=BigDecimal.ZERO;
					BigDecimal impuesto=BigDecimal.ZERO;
					BigDecimal total=BigDecimal.ZERO;
					
					for(CFactura fac:facs){
						importe=importe.add(fac.getIMPORTE());
						impuesto=impuesto.add(fac.getIMPUESTO());
						total=total.add(fac.getTOTAL());
					}
					//Abono a ventas
					PolizaDet abonoAVentas=poliza.agregarPartida();
					abonoAVentas.setCuenta(getCuenta("401"));
					abonoAVentas.setDescripcion("VENTAS");
					abonoAVentas.setDescripcion2("Ventas ".concat(origen.equals("CAM")?"CAMIONETA":"CREDITO"));
					abonoAVentas.setHaber(importe);
					abonoAVentas.setReferencia(origen);
					abonoAVentas.setReferencia2(facs.get(0).getSUCURSAL());
					abonoAVentas.setAsiento(asiento);
					//Abono a iva
					PolizaDet abonoaIva=poliza.agregarPartida();
					abonoaIva.setCuenta(getCuenta("206"));
					abonoaIva.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
					abonoaIva.setDescripcion2("IVA por Trasladar en Ventas ".concat(origen.equals("CAM")?"CAMIONETA":"CREDITO"));
					abonoaIva.setHaber(impuesto);
					abonoaIva.setReferencia(origen);
					abonoaIva.setReferencia2(facs.get(0).getSUCURSAL());
					abonoaIva.setAsiento(asiento);
					
					//Cargo a clientes
					PolizaDet cargoAClientes=poliza.agregarPartida();
					CuentaContable clientes=origen.equals("CAM")?getCuenta("105"):getCuenta("106");
						
					cargoAClientes.setCuenta(clientes);
					cargoAClientes.setDebe(total);
					cargoAClientes.setDescripcion("CLIENTES ".concat(origen.equals("CAM")?"CAMIONETA":"CREDITO"));
					cargoAClientes.setDescripcion2("Clientes ".concat(origen));
					cargoAClientes.setReferencia(origen);
					cargoAClientes.setReferencia2(facs.get(0).getSUCURSAL());
					cargoAClientes.setAsiento(asiento);
				}
			}
			
		}
	}	
	
	/**
	 * Contabiliza las ventas MOS día  mediante el siguiente asiento contable
	 * 
	 *  Abono a Ventas MOS(401) e IVA(206) con cargo a Bancos(102)  
	 *  
	 *   
	 *  
	 * 
	 * @param poliza
	 */
	private  void registrarVentasMostrador(){
		
		String sql="SELECT 'DEPOSITO' AS TIPO,a.ABONO_ID AS ORIGEN_ID,B.FECHA,S.NOMBRE AS SUCURSAL,b.CARGOABONO_ID "+
		",case when B.ORIGEN='VENTA_CAMIONETA' THEN 'CAM' WHEN B.ORIGEN='VENTA_MOSTRADOR' THEN 'MOS' ELSE 'N/D' END AS ORIGEN "+
		",CASE WHEN A.TRANSFERENCIA<>0 THEN CONCAT('TRANSFERENCIA','-',(SELECT X.DESCRIPCION FROM sw_cuentas X  WHERE X.id=B.CUENTA_ID),' REF:',IFNULL(A.REFERENCIA,'')) ELSE CONCAT('DEPOSITO','-',(SELECT X.DESCRIPCION FROM sw_cuentas X  WHERE X.id=B.CUENTA_ID),' REF:',IFNULL(A.REFERENCIA,'')) END AS CONCEPTO "+
		",B.IMPORTE,(SELECT X.DESCRIPCION FROM sw_cuentas X  WHERE X.id=B.CUENTA_ID) AS BANCO "+ 
		" ,CASE WHEN A.TRANSFERENCIA<>0 THEN 'TRANSFERENCIA' ELSE 'DEPOSITO' END DESCRIPCION "+  
		" FROM sw_bcargoabono b join sx_cxc_abonos a on(a.ABONO_ID=b.PAGO_ID) JOIN sw_sucursales S ON(S.SUCURSAL_ID=B.SUCURSAL_ID) " +
		" where b.fecha=? and b.ORIGEN IN('VENTA_MOSTRADOR') and b.conciliado=true AND A.ABONO_ID NOT IN(SELECT X.ABONO_ID FROM SX_CXC_APLICACIONES X JOIN SX_VENTAS V ON(V.CARGO_ID=X.CARGO_ID) WHERE X.ABONO_ID=A.ABONO_ID AND V.TIPO='TES')";
		EventList<CIngresoContado> depositosConciliados=GlazedLists.eventList(ServiceLocator2.getJdbcTemplate().query(sql
				,new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
				,new BeanPropertyRowMapper(CIngresoContado.class)
				));
		String asiento="Ventas MOS (Conciliados)";
		
		for(CIngresoContado c:depositosConciliados){
			BigDecimal total=c.getIMPORTE();
			//BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
			//BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			
			PolizaDet abonoCliente=poliza.agregarPartida();
			abonoCliente.setCuenta(getCuenta("401"));
			abonoCliente.setHaber(total);
			abonoCliente.setDescripcion("VENTAS MOS (Conciliados)");
			abonoCliente.setDescripcion2(c.getCONCEPTO());
			abonoCliente.setReferencia(c.getORIGEN());
			abonoCliente.setReferencia2(c.getSUCURSAL());
			abonoCliente.setAsiento(asiento);
			
		}
		Map<String,BigDecimal> totalConciliadoPorSucursal=new HashMap<String, BigDecimal>();
		Comparator<CIngresoContado> comparator=GlazedLists.beanPropertyComparator(CIngresoContado.class, "SUCURSAL");
		GroupingList<CIngresoContado> depositosConciladosPorSucursal=new GroupingList<CIngresoContado>(depositosConciliados,comparator);
		for(List<CIngresoContado> depositos:depositosConciladosPorSucursal){
			
			BigDecimal totalConciliado=BigDecimal.valueOf(0);
			BigDecimal importe=BigDecimal.valueOf(0);
			BigDecimal iva=BigDecimal.valueOf(0);
			
			for(CIngresoContado c:depositos){
				totalConciliado=totalConciliado.add(c.getIMPORTE());								
			}
			importe=MonedasUtils.calcularImporteDelTotal(totalConciliado,6);
			iva=MonedasUtils.calcularImpuesto(importe);
			
			importe=CantidadMonetaria.pesos(importe).amount();
			iva=CantidadMonetaria.pesos(iva).amount();
			
			if(totalConciliado.doubleValue()>0){
				CIngresoContado c=depositos.get(0);
				totalConciliadoPorSucursal.put(c.getSUCURSAL(), totalConciliado);
				PolizaDet cargoAcredores=poliza.agregarPartida();
				cargoAcredores.setCuenta(getCuenta("203"));
				cargoAcredores.setDebe(importe);
				cargoAcredores.setDescripcion("ACREEDORES DIVERSOS");
				cargoAcredores.setDescripcion2("Dep. Conciliados");
				cargoAcredores.setReferencia(c.getORIGEN());
				cargoAcredores.setReferencia2(c.getSUCURSAL());
				cargoAcredores.setAsiento(asiento);
			
				PolizaDet cargoIva=poliza.agregarPartida();
				cargoIva.setCuenta(getCuenta("206"));
				cargoIva.setDebe(iva);
				cargoIva.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
				cargoIva.setDescripcion2("Iva por Trasladado Dep. Conciliados");
				cargoIva.setReferencia(c.getORIGEN());
				cargoIva.setReferencia2(c.getSUCURSAL());
				cargoIva.setAsiento(asiento);

				//Cancelacion del IETU de Depositos no Identificados
				PolizaDet abonoIETU=poliza.agregarPartida();
				abonoIETU.setCuenta(getCuenta("902"));
				abonoIETU.setHaber(importe);
				abonoIETU.setDescripcion("IETU ACUMULABLE DEPOSITO NO IDENTIFICADO");
				abonoIETU.setDescripcion2("Cancelacion IETU Acumulable Dep. Conciliados");
				abonoIETU.setReferencia(c.getORIGEN());
				abonoIETU.setReferencia2(c.getSUCURSAL());
				abonoIETU.setAsiento(asiento);
				
				PolizaDet cargoIETU=poliza.agregarPartida();
				cargoIETU.setCuenta(getCuenta("903"));
				cargoIETU.setDebe(importe);
				cargoIETU.setDescripcion("ACUMULABLE IETU DEPOSITO NO IDENTIFICADO");
				cargoIETU.setDescripcion2("Cancelacion IETU Acumulable Dep. Conciliados");
				cargoIETU.setReferencia(c.getORIGEN());
				cargoIETU.setReferencia2(c.getSUCURSAL());
				cargoIETU.setAsiento(asiento);
				
			}
			
		}
		
		asiento="Ventas MOS";
		
		for(Map.Entry<String, List<CFactura>> entry:ventasPorTipo.entrySet()){
			String origen=entry.getKey();
			
			if(origen.equals("MOS")){
				List<CFactura> ventas=entry.getValue();
				GroupingList<CFactura> ventasPorSucursal=new GroupingList<CFactura>(GlazedLists.eventList(ventas),GlazedLists.beanPropertyComparator(CFactura.class, "SUCURSAL"));
				
				for(List<CFactura> facs:ventasPorSucursal){
					BigDecimal importe=BigDecimal.ZERO;
					BigDecimal impuesto=BigDecimal.ZERO;
					BigDecimal total=BigDecimal.ZERO;
					
					for(CFactura fac:facs){
						importe=importe.add(fac.getIMPORTE());
						impuesto=impuesto.add(fac.getIMPUESTO());
						total=total.add(fac.getTOTAL());
					}
					
					//Abono a ventas
					PolizaDet abonoAVentas=poliza.agregarPartida();
					abonoAVentas.setCuenta(getCuenta("401"));
					abonoAVentas.setDescripcion("VENTAS ".concat(origen.equals("MOS")?"MOSTRADOR":"N/A"));
					abonoAVentas.setDescripcion2("Ventas ".concat(origen));
					
					String sucursal=facs.get(0).getSUCURSAL();
					BigDecimal totalConciliado=totalConciliadoPorSucursal.get(sucursal);
					if(totalConciliado==null)
						totalConciliado=BigDecimal.ZERO;
					abonoAVentas.setHaber(importe.subtract(totalConciliado));
					abonoAVentas.setReferencia(origen);
					abonoAVentas.setReferencia2(facs.get(0).getSUCURSAL());
					abonoAVentas.setAsiento(asiento);
					
					//Abono  a IVA 
					PolizaDet abonoaIva=poliza.agregarPartida();
					abonoaIva.setCuenta(getCuenta("206"));
					abonoaIva.setDescripcion(IVA_EN_VENTAS);
					abonoaIva.setDescripcion2("IVA Trasladado en Ventas ".concat(origen));
					abonoaIva.setHaber(impuesto);
					abonoaIva.setReferencia(origen);
					abonoaIva.setReferencia2(facs.get(0).getSUCURSAL());
					abonoaIva.setAsiento(asiento);
					
				}
			}
		}
		//Cargo a Bancos
		for(CIngresoContado ingreso:ingresos){
			if(ingreso.getORIGEN().equals("MOS")){
				String concepto=ingreso.getCONCEPTO();
				if( concepto.startsWith(TipoDeAplicacion.COMISION_AMEX.name())
						 ||concepto.startsWith(TipoDeAplicacion.COMISION_CREDITO.name())
						 ||concepto.startsWith(TipoDeAplicacion.COMISION_DEBITO.name()) 
						 ||concepto.startsWith(TipoDeAplicacion.IMPUESTO.name())
						 )
						continue;
				PolizaDet cargoABancos=poliza.agregarPartida();
				cargoABancos.setCuenta(getCuenta("102"));
				cargoABancos.setDescripcion(ingreso.getBANCO());	
				cargoABancos.setDescripcion2(ingreso.getCONCEPTO());
				cargoABancos.setDebe(ingreso.getIMPORTE().abs());
				cargoABancos.setReferencia(ingreso.getORIGEN());
				cargoABancos.setReferencia2(ingreso.getSUCURSAL());
				cargoABancos.setAsiento(asiento);
			}
		}		
		registrarAbonosTarjetaCamioneta();
		registrarOtrosGastos("MOS","Ventas MOS");
		registrarOtrosProductos("MOS","Ventas MOS");
	}
	
	/**
	 * Complemento al asiento de Ventas Mos para procesar las ventas de CRE y CAM pagadas con Tarjeta
	 * 
	 * Abono a Clientes CRE,CAM en pagos con Tarjeta
	 * 
	 * @param poliza
	 */
	private void registrarAbonosTarjetaCamioneta(){
		// Abono a Clientes CRE y CAM  En Pagos con Tarjeta 
		final String asiento="Ventas MOS";
		List<Aplicacion> aplicacionesTarjeta=ServiceLocator2
		.getHibernateTemplate().find("from Aplicacion a where a.fecha=?  and a.cargo.origen in(\'CAM\') and a.abono.primeraAplicacion=a.fecha"
				,new Object[]{poliza.getFecha()});
		EventList<PagoConTarjeta> pgTar=new UniqueList<PagoConTarjeta>(new BasicEventList<PagoConTarjeta>(),GlazedLists.beanPropertyComparator(PagoConTarjeta.class, "id"));
		for(Aplicacion ap:aplicacionesTarjeta){
			if(ap.getAbono() instanceof PagoConTarjeta){
				PagoConTarjeta ppt=(PagoConTarjeta)ServiceLocator2.getCXCManager().getAbono(ap.getAbono().getId());
				pgTar.add(ppt);
			}
		}
		final Comparator<PagoConTarjeta> c1=GlazedLists.beanPropertyComparator(PagoConTarjeta.class, "sucursal.id");
		GroupingList<PagoConTarjeta> pagosPorSucursal=new GroupingList<PagoConTarjeta>(pgTar,c1);
		
		for(List<PagoConTarjeta> list:pagosPorSucursal){
			BigDecimal total=BigDecimal.ZERO;
			BigDecimal totalAplicado=BigDecimal.ZERO;
			
			for(PagoConTarjeta pago:list){
				if(pago.isAnticipo())
					continue;
				
				total=total.add(pago.getTotal());
				totalAplicado=totalAplicado.add(pago.getAplicado(poliza.getFecha()));
			}
			
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total,4);
			BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			importe=CantidadMonetaria.pesos(importe).amount();
			iva=CantidadMonetaria.pesos(iva).amount();
			
			PolizaDet cargoABancos=poliza.agregarPartida();
			cargoABancos.setCuenta(getCuenta("105"));
			cargoABancos.setHaber(totalAplicado);
			cargoABancos.setDescripcion("CLIENTES CAMIONETA");
			cargoABancos.setDescripcion2("Pago Tarjeta Ctes. Camioneta");
			cargoABancos.setReferencia("CAM");
			cargoABancos.setReferencia2(list.get(0).getSucursal().getNombre());		
			cargoABancos.setAsiento(asiento);
			
			//Cargo  a IETU de Tarjeta Camioneta
			PolizaDet cargoIETU=poliza.agregarPartida();
			cargoIETU.setCuenta(getCuenta("902"));
		//	cargoIETU.setDebe(importe);
			cargoIETU.setDebe(MonedasUtils.calcularImporteDelTotal(totalAplicado));
			cargoIETU.setDescripcion("ACUMULABLE IETU CAMIONETA");
			cargoIETU.setDescripcion2("IETU Acumulable en Tarjeta CAM");
			cargoIETU.setReferencia("CAM");
			cargoIETU.setReferencia2(list.get(0).getSucursal().getNombre());
			cargoIETU.setAsiento(asiento);
			
			//Abono  a IETU de Tarjeta Camioneta
			PolizaDet abonoIETU=poliza.agregarPartida();
			abonoIETU.setCuenta(getCuenta("903"));
		//	abonoIETU.setHaber(importe);
			abonoIETU.setHaber(MonedasUtils.calcularImporteDelTotal(totalAplicado));
			abonoIETU.setDescripcion("IETU ACUMULABLE CAMIONETA");
			abonoIETU.setDescripcion2("IETU Acumulable en Tarjeta CAM");
			abonoIETU.setReferencia("CAM");
			abonoIETU.setReferencia2(list.get(0).getSucursal().getNombre());
			abonoIETU.setAsiento(asiento);
			
			//Cargo  a IVA de Tarjeta Camioneta
			PolizaDet cargoaIvaAnticipo=poliza.agregarPartida();
			cargoaIvaAnticipo.setCuenta(getCuenta("206"));
			cargoaIvaAnticipo.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
			cargoaIvaAnticipo.setDescripcion2("IVA por Trasladar en Tarjeta CAM");
		//	cargoaIvaAnticipo.setDebe(iva);
			cargoaIvaAnticipo.setDebe(MonedasUtils.calcularImpuestoDelTotal(totalAplicado));
			cargoaIvaAnticipo.setReferencia("CAM");
			cargoaIvaAnticipo.setReferencia2(list.get(0).getSucursal().getNombre());
			cargoaIvaAnticipo.setAsiento(asiento);
			
			//Abono  a IVA de Tarjeta Camioneta
			PolizaDet abonoaIvaAnticipo=poliza.agregarPartida();
			abonoaIvaAnticipo.setCuenta(getCuenta("206"));
			abonoaIvaAnticipo.setDescripcion(IVA_EN_VENTAS);
			abonoaIvaAnticipo.setDescripcion2("IVA Trasladado en Tarjeta CAM");
		//	abonoaIvaAnticipo.setHaber(iva);
			abonoaIvaAnticipo.setHaber(MonedasUtils.calcularImpuestoDelTotal(totalAplicado));
			abonoaIvaAnticipo.setReferencia("CAM");
			abonoaIvaAnticipo.setReferencia2(list.get(0).getSucursal().getNombre());
			abonoaIvaAnticipo.setAsiento(asiento);
			
			
		}
		
		List<PagoConTarjeta> pagosTarjetaCre=ServiceLocator2
			.getHibernateTemplate().find("from PagoConTarjeta p where p.fecha=? and p.origen in(\'CRE\',\'CHE\',\'JUR\')",new Object[]{poliza.getFecha()});
		pgTar.clear();
		pgTar.addAll(pagosTarjetaCre);
		for(List<PagoConTarjeta> list:pagosPorSucursal){
			
			BigDecimal total=BigDecimal.ZERO;
			
			for(PagoConTarjeta pago:list){
				total=total.add(pago.getTotal());
			}
			
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total,4);
			BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			importe=CantidadMonetaria.pesos(importe).amount();
			iva=CantidadMonetaria.pesos(iva).amount();
			
			PolizaDet cargoABancos=poliza.agregarPartida();
			cargoABancos.setCuenta(getCuenta("203"));
			cargoABancos.setHaber(total);
			cargoABancos.setDescripcion("ACREEDORES DIVERSOS");
			cargoABancos.setDescripcion2("Pago Tarjeta Ctes. Credito");
			cargoABancos.setReferencia(list.get(0).getOrigenAplicacion());
			cargoABancos.setReferencia2(list.get(0).getSucursal().getNombre());
			cargoABancos.setAsiento(asiento);
			
			//Cargo  a IETU de Tarjeta Camioneta
			PolizaDet cargoIETU=poliza.agregarPartida();
			cargoIETU.setCuenta(getCuenta("902"));
			cargoIETU.setDebe(importe);
			cargoIETU.setDescripcion("ACUMULABLE IETU "+list.get(0).getOrigenAplicacion());
			cargoIETU.setDescripcion2("IETU Acumulable en Tarjeta");
			cargoIETU.setReferencia(list.get(0).getOrigenAplicacion());
			cargoIETU.setReferencia2(list.get(0).getSucursal().getNombre());
			cargoIETU.setAsiento(asiento);
			
			//Abono  a IETU de Tarjeta Camioneta
			PolizaDet abonoIETU=poliza.agregarPartida();
			abonoIETU.setCuenta(getCuenta("903"));
			abonoIETU.setHaber(importe);
			abonoIETU.setDescripcion("IETU ACUMULABLE "+list.get(0).getOrigenAplicacion());
			abonoIETU.setDescripcion2("IETU Acumulable en Tarjeta");
			abonoIETU.setReferencia(list.get(0).getOrigenAplicacion());
			abonoIETU.setReferencia2(list.get(0).getSucursal().getNombre());
			abonoIETU.setAsiento(asiento);
			
			//Cargo  a IVA de Tarjeta Camioneta
			PolizaDet cargoaIvaAnticipo=poliza.agregarPartida();
			cargoaIvaAnticipo.setCuenta(getCuenta("206"));
			cargoaIvaAnticipo.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
			cargoaIvaAnticipo.setDescripcion2("IVA por Trasladar en Tarjeta ");
			cargoaIvaAnticipo.setDebe(iva);
			cargoaIvaAnticipo.setReferencia(list.get(0).getOrigenAplicacion());
			cargoaIvaAnticipo.setReferencia2(list.get(0).getSucursal().getNombre());
			cargoaIvaAnticipo.setAsiento(asiento);
			
			//Abono  a IVA de Tarjeta Camioneta
			PolizaDet abonoaIvaAnticipo=poliza.agregarPartida();
			abonoaIvaAnticipo.setCuenta(getCuenta("206"));
			abonoaIvaAnticipo.setDescripcion(IVA_EN_VENTAS);
			abonoaIvaAnticipo.setDescripcion2("IVA Trasladado en Tarjeta");
			abonoaIvaAnticipo.setHaber(iva);
			abonoaIvaAnticipo.setReferencia(list.get(0).getOrigenAplicacion());
			abonoaIvaAnticipo.setReferencia2(list.get(0).getSucursal().getNombre());
			abonoaIvaAnticipo.setAsiento(asiento);
			
		}
		
		//Abono a Anticipos para los pagos con tarjeta registrados como anticipos
		List<PagoConTarjeta> anticiposConTarjeta=ServiceLocator2.getHibernateTemplate()
		.find("from PagoConTarjeta p where p.fecha=? and p.origen=\'CAM\' and p.anticipo=true"
				,new Object[]{poliza.getFecha()});
		for(PagoConTarjeta pago:anticiposConTarjeta){
			/*
			 * Inicio Modificacion de Anticipo "Total" = Importe antes de IVA y su cuenta de IVA CPG
			 */
			BigDecimal total=pago.getTotal();
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
			BigDecimal impuesto=MonedasUtils.calcularImpuesto(importe);
			
			PolizaDet abonoAAnticiposDeClientes=poliza.agregarPartida();
			abonoAAnticiposDeClientes.setCuenta(getCuenta("204"));
			abonoAAnticiposDeClientes.setHaber(MonedasUtils.calcularImporteDelTotal(total));
			abonoAAnticiposDeClientes.setDescripcion("ANTICIPOS DE CLIENTES");
			abonoAAnticiposDeClientes.setDescripcion2("Anticipo (Tarjeta) de:"+pago.getNombre());
			abonoAAnticiposDeClientes.setReferencia("CAM");
			abonoAAnticiposDeClientes.setReferencia2(pago.getSucursal().getNombre());
			abonoAAnticiposDeClientes.setAsiento("Ventas MOS");
			
			//Cargo  a IVA de Anticipos
		/*	PolizaDet cargoaIvaAnticipo=poliza.agregarPartida();
			cargoaIvaAnticipo.setCuenta(getCuenta("206"));
			cargoaIvaAnticipo.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
			cargoaIvaAnticipo.setDescripcion2("IVA por Trasladar en Anticipos (Tar.)");
			cargoaIvaAnticipo.setDebe(impuesto);
			cargoaIvaAnticipo.setReferencia("CAM");
			cargoaIvaAnticipo.setReferencia2(pago.getSucursal().getNombre());
			cargoaIvaAnticipo.setAsiento("Ventas MOS");*/
			
			//Abono  a IVA de Anticipos
			PolizaDet abonoaIvaAnticipo=poliza.agregarPartida();
			abonoaIvaAnticipo.setCuenta(getCuenta("206"));
			abonoaIvaAnticipo.setDescripcion(IVA_EN_ANTICIPO);
			abonoaIvaAnticipo.setDescripcion2("IVA Trasladado en Anticipos (Tar.)");
			abonoaIvaAnticipo.setHaber(impuesto);
			abonoaIvaAnticipo.setReferencia("CAM");
			abonoaIvaAnticipo.setReferencia2(pago.getSucursal().getNombre());
			abonoaIvaAnticipo.setAsiento("Ventas MOS");
			
			//IETU ANTICIPO EN TARJETA
			PolizaDet cargoIETUaAnticipos=poliza.agregarPartida();
			cargoIETUaAnticipos.setCuenta(getCuenta("902"));
			cargoIETUaAnticipos.setDebe(importe);
			cargoIETUaAnticipos.setDescripcion("ACUMULABLE IETU ANTICIPO DE CLIENTES");
			cargoIETUaAnticipos.setDescripcion2("IETU Acumulable Anticipo Cliente (Tar)");
			cargoIETUaAnticipos.setReferencia("CAM");
			cargoIETUaAnticipos.setReferencia2(pago.getSucursal().getNombre());
			cargoIETUaAnticipos.setAsiento(asiento);
			
			PolizaDet abonoIETUaAnticipos=poliza.agregarPartida();
			abonoIETUaAnticipos.setCuenta(getCuenta("903"));
			abonoIETUaAnticipos.setHaber(importe);
			abonoIETUaAnticipos.setDescripcion("IETU ACUMULABLE ANTICIPO DE CLIENTES");
			abonoIETUaAnticipos.setDescripcion2("IETU Acumulable Anticipo Cliente (Tar)");
			abonoIETUaAnticipos.setReferencia("CAM");
			abonoIETUaAnticipos.setReferencia2(pago.getSucursal().getNombre());
			abonoIETUaAnticipos.setAsiento(asiento);
			
			
			
			/*
			 * Fin Modificacion de Anticipo "Total" = Importe antes de IVA y su cuenta de IVA CPG
			 */
		}
	
	}
	
	private  void registrarOtrosGastos(String origen,String asiento){
		String sql="SELECT (SELECT S.NOMBRE FROM sw_sucursales S WHERE A.SUCURSAL_ID=S.SUCURSAL_ID) AS SUCURSAL" +
				" ,SUM(TOTAL) AS TOTAL,(SELECT X.CAR_ORIGEN FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) AS ORIGEN " +
				" FROM sx_cxc_abonos a where fecha=?  AND TIPO_ID=\'PAGO_DIF\' " +
				" AND (SELECT X.CAR_ORIGEN FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) =? " +
				" GROUP BY SUCURSAL_ID,(SELECT X.CAR_ORIGEN FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID)";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
				,new SqlParameterValue(Types.VARCHAR,origen)
		};
		
		//final String asiento="VENTAS MOS";
		
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		for(Map<String,Object> row:rows){
			
			if(origen.equals("MOS")){
				asiento="MOS Otros Gastos";
			}else{
				asiento="CAM Otros Gastos";
			}
			
			PolizaDet cargoAGastos=poliza.agregarPartida();
			cargoAGastos.setCuenta(getCuenta("704"));
			cargoAGastos.setDebe((BigDecimal)row.get("TOTAL"));
			cargoAGastos.setDescripcion("OTROS GASTOS");
			cargoAGastos.setDescripcion2("OG Ajustes automaticos menores a $1");
			cargoAGastos.setReferencia((String)row.get("ORIGEN"));
			cargoAGastos.setReferencia2((String)row.get("SUCURSAL"));
			cargoAGastos.setAsiento(asiento);
			if("CAM".equals(origen)){
				PolizaDet abonoaClientes=poliza.agregarPartida();
				abonoaClientes.setCuenta(getCuenta("105"));
				abonoaClientes.setHaber((BigDecimal)row.get("TOTAL"));
				abonoaClientes.setDescripcion("CLIENTES CAMIONETA");
				abonoaClientes.setDescripcion2("Clientes CAM otros gastos");
				abonoaClientes.setReferencia((String)row.get("ORIGEN"));
				abonoaClientes.setReferencia2((String)row.get("SUCURSAL"));
				abonoaClientes.setAsiento(asiento);
			}
		}
	}
	
	private  void registrarOtrosProductos(String origen,String asiento){
		String sql="SELECT (SELECT s.nombre FROM sw_sucursales s where s.SUCURSAL_ID=a.SUCURSAL_ID) as SUCURSAL" +
				" ,SUM(a.DIFERENCIA) as TOTAL"
			+",(SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) as ORIGEN"
			+" FROM sx_cxc_abonos a " +
			" where A.diferencia_fecha=? AND A.SAF=A.DIFERENCIA_FECHA" +
			"   AND A.diferencia<>0 " +
			"   AND A.TIPO_ID<>'PAGO_HXE' " +
			"   AND (SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID)=?"
			+" GROUP BY SUCURSAL_ID,(SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID)";

		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
				,new SqlParameterValue(Types.VARCHAR,origen)
		};
		//final String asiento="VENTAS MOS";
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		for(Map<String,Object> row:rows){
			String origenRow=(String)row.get("ORIGEN");
			if(origenRow.equals(origen)){
				
				if(origen.equals("MOS")){
					asiento="MOS Otros Ingresos";
				}else{
					asiento="CAM Otros Ingresos";
				}
				
				PolizaDet abonoAProductos=poliza.agregarPartida();
				abonoAProductos.setCuenta(getCuenta("702"));
				abonoAProductos.setHaber((BigDecimal)row.get("TOTAL"));
				abonoAProductos.setDescripcion("OTROS INGRESOS");
				abonoAProductos.setDescripcion2("OI Ajustes automaticos menores a $10 PA");
				abonoAProductos.setReferencia(origen);
				abonoAProductos.setReferencia2((String)row.get("SUCURSAL"));
				abonoAProductos.setAsiento(asiento);
				
				BigDecimal importe=CantidadMonetaria.pesos(MonedasUtils.calcularImporteDelTotal((BigDecimal)row.get("TOTAL"))).amount();
				
				PolizaDet cargoIETU=poliza.agregarPartida();
				cargoIETU.setCuenta(getCuenta("902"));
				cargoIETU.setDebe(importe.multiply(BigDecimal.valueOf(1)));
				cargoIETU.setDescripcion("IETU ACUMULABLE OTROS INGRESOS");
				cargoIETU.setDescripcion2("IETU Acumulable OI Aju. PA");
				cargoIETU.setReferencia(origen);
				cargoIETU.setReferencia2((String)row.get("SUCURSAL"));
				cargoIETU.setAsiento(asiento);
				
				PolizaDet abonoIETU=poliza.agregarPartida();
				abonoIETU.setCuenta(getCuenta("903"));
				abonoIETU.setHaber(importe.multiply(BigDecimal.valueOf(1)));
				abonoIETU.setDescripcion("ACUMULABLE IETU OTROS INGRESOS");
				abonoIETU.setDescripcion2("IETU Acumulable OI Aju. PA");
				abonoIETU.setReferencia(origen);
				abonoIETU.setReferencia2((String)row.get("SUCURSAL"));
				abonoIETU.setAsiento(asiento);
				
			}
		}
		
		sql="SELECT (SELECT s.nombre FROM sw_sucursales s where s.SUCURSAL_ID=a.SUCURSAL_ID) as SUCURSAL" +
		" ,SUM(a.DIFERENCIA) as TOTAL"
		+",(SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) as ORIGEN"
		+" FROM sx_cxc_abonos a " +
		" where A.diferencia_fecha=? AND A.SAF<>A.DIFERENCIA_FECHA" +
		"   AND A.diferencia<>0 " +
		"   AND A.TIPO_ID<>'PAGO_HXE' " +
		"   AND (SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID)=?"
		+" GROUP BY SUCURSAL_ID,(SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID)";

		params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
				,new SqlParameterValue(Types.VARCHAR,origen)
		};
		
		rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		for(Map<String,Object> row:rows){
			String origenRow=(String)row.get("ORIGEN");
			if(origenRow.equals(origen)){
				PolizaDet abonoAProductos=poliza.agregarPartida();
				abonoAProductos.setCuenta(getCuenta("702"));
				abonoAProductos.setHaber((BigDecimal)row.get("TOTAL"));
				abonoAProductos.setDescripcion("OTROS INGRESOS");
				abonoAProductos.setDescripcion2("OI Ajustes automaticos menores a $10 SAF");
				abonoAProductos.setReferencia(origen);
				abonoAProductos.setReferencia2((String)row.get("SUCURSAL"));
				abonoAProductos.setAsiento(asiento);
				
				PolizaDet cargoASAF=poliza.agregarPartida();
				cargoASAF.setCuenta(getCuenta("203"));
				cargoASAF.setDebe((BigDecimal)row.get("TOTAL"));
				cargoASAF.setDescripcion("ACREEDORES DIVERSOS");
				cargoASAF.setDescripcion2("Ajustes Otros Prod. SAF");
				cargoASAF.setReferencia(origen);
				cargoASAF.setReferencia2((String)row.get("SUCURSAL"));
				cargoASAF.setAsiento(asiento);
			}
		}
	}
	
	private  void registrarAnticipos(String asiento){
		String sql="select 'ANTICIPO' AS TIPO,A.ABONO_ID AS ORIGEN_ID" +
				",A.FECHA" +
				",(SELECT S.NOMBRE FROM sw_sucursales S WHERE A.SUCURSAL_ID=S.SUCURSAL_ID) AS SUCURSAL " +
				",A.ABONO_ID" +
				",A.ORIGEN" +
				",concat('anticipo (',substr(a.TIPO_ID,6,3),') de cte.:',a.nombre) as CONCEPTO" +
				",(TOTAL) AS IMPORTE" +
				",(SELECT C.DESCRIPCION FROM sw_cuentas C WHERE C.ID=A.CUENTA_ID) AS BANCO" +
				",substr(a.TIPO_ID,6,3) AS DESCRIPCION " +
				"from sx_cxc_abonos a where A.ORIGEN=\'CAM\' AND A.TIPO_ID<>\'PAGO_TAR\' " +
				"AND a.FECHA=? AND A.TIPO_ID LIKE 'PAGO%' AND A.ANTICIPO IS TRUE";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
		};	
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		for(Map<String,Object> row:rows){
			
			/*
			 * Inicio Modificacion de Anticipo "Total" = Importe antes de IVA y su cuenta de IVA CPG
			 */
			BigDecimal total= (BigDecimal)row.get("IMPORTE");
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
			BigDecimal impuesto=MonedasUtils.calcularImpuesto(importe);
			impuesto=CantidadMonetaria.pesos(impuesto).amount();
			
			PolizaDet abonoAAnticiposDeClientes=poliza.agregarPartida();
			abonoAAnticiposDeClientes.setCuenta(getCuenta("204"));
			abonoAAnticiposDeClientes.setHaber(importe);
			abonoAAnticiposDeClientes.setDescripcion("ANTICIPOS DE CLIENTES");
			abonoAAnticiposDeClientes.setDescripcion2((String)row.get("CONCEPTO"));
			abonoAAnticiposDeClientes.setReferencia((String)row.get("ORIGEN"));
			abonoAAnticiposDeClientes.setReferencia2((String)row.get("SUCURSAL"));
			abonoAAnticiposDeClientes.setAsiento(asiento);
			
			//Cargo  a IVA de Anticipos
			PolizaDet cargoaIvaAnticipo=poliza.agregarPartida();
			cargoaIvaAnticipo.setCuenta(getCuenta("206"));
			cargoaIvaAnticipo.setDescripcion(IVA_EN_ANTICIPO);
			cargoaIvaAnticipo.setDescripcion2("IVA Trasladado en Anticipos");
			cargoaIvaAnticipo.setHaber(impuesto);
			cargoaIvaAnticipo.setReferencia((String)row.get("ORIGEN"));
			cargoaIvaAnticipo.setReferencia2((String)row.get("SUCURSAL"));
			cargoaIvaAnticipo.setAsiento(asiento);
			
			//IETU ANTICIPO
			PolizaDet cargoIETUaAnticipos=poliza.agregarPartida();
			cargoIETUaAnticipos.setCuenta(getCuenta("902"));
			cargoIETUaAnticipos.setDebe(importe);
			cargoIETUaAnticipos.setDescripcion("ACUMULABLE IETU ANTICIPO DE CLIENTES");
			cargoIETUaAnticipos.setDescripcion2("IETU Acumulable Anticipo Cliente");
			cargoIETUaAnticipos.setReferencia((String)row.get("ORIGEN"));
			cargoIETUaAnticipos.setReferencia2((String)row.get("SUCURSAL"));
			cargoIETUaAnticipos.setAsiento(asiento);
			
			PolizaDet abonoIETUaAnticipos=poliza.agregarPartida();
			abonoIETUaAnticipos.setCuenta(getCuenta("903"));
			abonoIETUaAnticipos.setHaber(importe);
			abonoIETUaAnticipos.setDescripcion("IETU ACUMULABLE ANTICIPO DE CLIENTES");
			abonoIETUaAnticipos.setDescripcion2("IETU Acumulable Anticipo Cliente");
			abonoIETUaAnticipos.setReferencia((String)row.get("ORIGEN"));
			abonoIETUaAnticipos.setReferencia2((String)row.get("SUCURSAL"));
			abonoIETUaAnticipos.setAsiento(asiento);
				
		}
	}
	
	/**
	 * Porcesa todas las comisiones generando un asiento de la siguiente manera
	 *  
	 *  Paraq el importe de la comision
	 *   Abono a Bancos con cargo a Gastos  
	 *  Para el impuesto
	 *   Abono a Bancos con cargo a IVA
	 */
	private void registrarComisionesBancarias(){
		
		String asiento="MOS COMISIONES";
		
		for(CIngresoContado ingreso:ingresos){
			
			String concepto=ingreso.getCONCEPTO();
			if( concepto.startsWith(TipoDeAplicacion.COMISION_AMEX.name())
			 ||concepto.startsWith(TipoDeAplicacion.COMISION_CREDITO.name())
			 ||concepto.startsWith(TipoDeAplicacion.COMISION_DEBITO.name()) )
			{
				PolizaDet abonoABancos=poliza.agregarPartida();
				abonoABancos.setCuenta(getCuenta("102"));
				abonoABancos.setHaber(ingreso.getIMPORTE().abs());
				abonoABancos.setDescripcion(ingreso.getBANCO());
				abonoABancos.setDescripcion2(ingreso.getCONCEPTO());
				abonoABancos.setReferencia(ingreso.getORIGEN());
				abonoABancos.setReferencia2(ingreso.getSUCURSAL());
				abonoABancos.setAsiento(asiento);
				
				PolizaDet cargoAGastos=poliza.agregarPartida();
				cargoAGastos.setCuenta(getCuenta("600"));
				cargoAGastos.setDebe(ingreso.getIMPORTE().abs());
				cargoAGastos.setDescripcion("GASTOS COMISIONES BANCARIAS");
				cargoAGastos.setDescripcion2(ingreso.getCONCEPTO());
				cargoAGastos.setReferencia(ingreso.getORIGEN());
				cargoAGastos.setReferencia2(ingreso.getSUCURSAL());
				cargoAGastos.setAsiento(asiento);
				
			}else if(concepto.startsWith(TipoDeAplicacion.COMISION_AMEX.name())){
				// Pendiente de definir
				
			}else if(concepto.startsWith(TipoDeAplicacion.IMPUESTO.name())){
				PolizaDet abonoABancos=poliza.agregarPartida();
				abonoABancos.setCuenta(getCuenta("102"));
				abonoABancos.setHaber(ingreso.getIMPORTE().abs());
				abonoABancos.setDescripcion(ingreso.getBANCO());
				abonoABancos.setDescripcion2(ingreso.getCONCEPTO());
				abonoABancos.setReferencia(ingreso.getORIGEN());
				abonoABancos.setReferencia2(ingreso.getSUCURSAL());
				abonoABancos.setAsiento(asiento);
				
				PolizaDet cargoAIVA=poliza.agregarPartida();
				cargoAIVA.setCuenta(getCuenta("117"));
				cargoAIVA.setDebe(ingreso.getIMPORTE().abs());
				cargoAIVA.setDescripcion("IVA EN GASTOS");
				cargoAIVA.setDescripcion2(ingreso.getCONCEPTO());
				cargoAIVA.setReferencia(ingreso.getORIGEN());
				cargoAIVA.setReferencia2(ingreso.getSUCURSAL());
				cargoAIVA.setAsiento(asiento);
			}
		}
	}
	
	
	private  void registrarIETU(){		
		
		String asiento="CAM IETU";
		for(final String origen:cobranzaPorOrigen.keySet()){
			
			if(origen.equals("MOS")){
				asiento="MOS IETU";
			}
			
			EventList<CCobranza> source=GlazedLists.eventList(cobranzaPorOrigen.get(origen));
			GroupingList<CCobranza> cobranzaPorSucursal=new GroupingList<CCobranza>(source,GlazedLists.beanPropertyComparator(CCobranza.class, "sucursal"));
			for(List<CCobranza> porSucursal:cobranzaPorSucursal){
				
				BigDecimal importe=BigDecimal.ZERO;
				BigDecimal tar=BigDecimal.ZERO;
				BigDecimal tarCre=BigDecimal.ZERO;
				//Sumarizamos por sucursal
				for(CCobranza cc:porSucursal){
					if(cc.getDescripcion().equalsIgnoreCase("EFE") 
							|| cc.getDescripcion().equalsIgnoreCase("CHE")
							|| cc.getDescripcion().equalsIgnoreCase("TAR")
							|| cc.getDescripcion().equalsIgnoreCase("Dep")
							|| cc.getDescripcion().equalsIgnoreCase("TRA")
						//	|| cc.getDescripcion().equalsIgnoreCase("SAF")
							){
						importe=importe.add(cc.getImporte());
						if(cc.getDescripcion().equalsIgnoreCase("TAR") && cc.getOrigen().equals("CAM")){
							tar=tar.add(cc.getImporte());
						}
						if(cc.getDescripcion().equalsIgnoreCase("TAR") && cc.getOrigen().equals("CRE")){
							tarCre=tarCre.add(cc.getImporte());
						}
					}
				}
				
				String sucursal=porSucursal.get(0).getSucursal();
				String descripcion="IETU Acumulable de cobranza ";
				
				
				
				PolizaDet cargoIETU=poliza.agregarPartida();
				cargoIETU.setCuenta(getCuenta("902"));
				cargoIETU.setDebe(MonedasUtils.calcularImporteDelTotal(importe.subtract(tar).subtract(tarCre)));
				cargoIETU.setDescripcion("ACUMULABLE IETU");
				cargoIETU.setDescripcion2(descripcion);
				cargoIETU.setReferencia(origen);
				cargoIETU.setReferencia2(sucursal);
				cargoIETU.setAsiento(asiento);
				
				PolizaDet abonoIETU=poliza.agregarPartida();
				abonoIETU.setCuenta(getCuenta("903"));
				abonoIETU.setHaber(MonedasUtils.calcularImporteDelTotal(importe.subtract(tar).subtract(tarCre)));
				abonoIETU.setDescripcion("IETU ACUMULABLE");
				abonoIETU.setDescripcion2(descripcion);
				abonoIETU.setReferencia(origen);
				abonoIETU.setReferencia2(sucursal);
				abonoIETU.setAsiento(asiento);
			}
		}
		
		for(CIngresoContado ingreso:ingresos){
			
			String concepto=ingreso.getCONCEPTO();
			if( concepto.startsWith(TipoDeAplicacion.COMISION_AMEX.name())
			 ||concepto.startsWith(TipoDeAplicacion.COMISION_CREDITO.name())
			 ||concepto.startsWith(TipoDeAplicacion.COMISION_DEBITO.name()) )
			{
				
				if(concepto.startsWith("COMISION")){
					asiento="MOS COMISION IETU";
				}
				
				
				PolizaDet abonoIETU=poliza.agregarPartida();
				abonoIETU.setCuenta(getCuenta("901"));
				abonoIETU.setHaber(ingreso.getIMPORTE().abs());
				abonoIETU.setDescripcion("DEDUCIBLE IETU GASTOS");
				abonoIETU.setDescripcion2("IETU Deducible por Comisiones Bancarias");
				abonoIETU.setReferencia(ingreso.getORIGEN());
				abonoIETU.setReferencia2(ingreso.getSUCURSAL());
				abonoIETU.setAsiento(asiento);
				
				PolizaDet cargoIETU=poliza.agregarPartida();
				cargoIETU.setCuenta(getCuenta("900"));
				cargoIETU.setDebe(ingreso.getIMPORTE().abs());
				cargoIETU.setDescripcion("IETU DEDUCIBLE GASTOS");
				cargoIETU.setDescripcion2("IETU Deducible por Comisiones Bancarias");
				cargoIETU.setReferencia(ingreso.getORIGEN());
				cargoIETU.setReferencia2(ingreso.getSUCURSAL());
				cargoIETU.setAsiento(asiento);
				
			}
		}
	}
	
	public BigDecimal calcularSAFDisp(String sucursal){
		
		String sql="select 'SALDO_A_FAVOR_DISP' AS TIPO,(SELECT S.NOMBRE FROM sw_sucursales S WHERE A.SUCURSAL_ID=S.SUCURSAL_ID) AS SUCURSAL"
			+",SUM((A.TOTAL-IFNULL((SELECT (X.DIFERENCIA) FROM sx_cxc_abonos X WHERE X.ABONO_ID=A.ABONO_ID AND X.DIFERENCIA_FECHA=A.SAF),0)-IFNULL((SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.fecha=A.SAF),0) ))AS IMPORTE"
			+" from sx_cxc_abonos a where a.SAF=? AND A.TIPO_ID LIKE 'PAGO%' AND a.ANTICIPO IS false  AND (SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.FECHA=A.SAF) ='CAM'"
			+" GROUP BY (SELECT S.NOMBRE FROM sw_sucursales S WHERE A.SUCURSAL_ID=S.SUCURSAL_ID)"
			+" HAVING SUM((A.TOTAL-IFNULL((SELECT (X.DIFERENCIA) FROM sx_cxc_abonos X WHERE X.ABONO_ID=A.ABONO_ID AND X.DIFERENCIA_FECHA=A.SAF),0)-IFNULL((SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.fecha=A.SAF),0) ))>0";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
						};
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		BigDecimal ivaSAF= new BigDecimal(0);
		
		for(Map<String,Object> row:rows){
			if (sucursal.equals(row.get("SUCURSAL"))){
				ivaSAF=(BigDecimal)row.get("IMPORTE");
			}
			System.out.println("Calc.SAF :" + ivaSAF+ " Suc.:"+sucursal);
		}
		return ivaSAF;
		
	}
	
	private void registrarSaldosAFavor(String origen,String asiento){
		//final String asiento="VENTAS MOS";
		String sql="select 'SALDO_A_FAVOR' AS TIPO,A.TIPO_ID as TIPO_PAGO,A.ABONO_ID AS ORIGEN_ID,A.SAF AS FECHA" +
				",(SELECT S.NOMBRE FROM sw_sucursales S WHERE A.SUCURSAL_ID=S.SUCURSAL_ID) AS SUCURSAL,A.ABONO_ID"
			+",(SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID) AS ORIGEN" 
			+ ",concat('Saldo a Favor del cte.:',a.nombre,' en',substr(a.TIPO_ID,6,3)) as CONCEPTO"
			+",(A.TOTAL-IFNULL((SELECT (X.DIFERENCIA) FROM sx_cxc_abonos X WHERE X.ABONO_ID=A.ABONO_ID AND X.DIFERENCIA_FECHA=A.SAF),0)-IFNULL((SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.fecha=A.SAF),0) )AS IMPORTE"
			+",(SELECT C.DESCRIPCION FROM sw_cuentas C WHERE C.ID=A.CUENTA_ID) AS BANCO,substr(a.TIPO_ID,6,3) AS DESCRIPCION"
			+" from sx_cxc_abonos a where a.SAF=? AND A.TIPO_ID LIKE 'PAGO%' AND a.ANTICIPO IS false  AND (SELECT MAX(X.CAR_ORIGEN) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.FECHA=A.SAF) =? AND "
			+" (A.TOTAL-IFNULL((SELECT (X.DIFERENCIA) FROM sx_cxc_abonos X WHERE X.ABONO_ID=A.ABONO_ID AND X.DIFERENCIA_FECHA=A.SAF),0)-IFNULL((SELECT SUM(X.IMPORTE) FROM sx_cxc_aplicaciones X WHERE X.ABONO_ID=A.ABONO_ID AND X.fecha=A.SAF),0) )>0";
		Object[] params=new Object[]{
				new SqlParameterValue(Types.DATE,poliza.getFecha())
				,new SqlParameterValue(Types.VARCHAR,origen)
		};
	
		List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,params);
		
	    BigDecimal total=new BigDecimal(0);
		BigDecimal impuestoSAFDisp=new BigDecimal(0); 
		BigDecimal impuesto=new BigDecimal(0); 		
		
		
		for(Map<String,Object> row:rows){
			String origenRow=(String)row.get("ORIGEN");
			if(origen.equals(origenRow)){
				
				if(origen.equals("MOS")){
					asiento="MOS Saldo A Favor";
				}else{
					asiento="CAM Saldo A Favor";
				}

				String tipoDePago=(String)row.get("TIPO_PAGO");
				if("PAGO_TAR".equals(tipoDePago)){
					asiento="MOS Saldo A Favor";
				}
				
				
				/*
				 * Inicio Modificacion de Anticipo "Total" = Importe antes de IVA y su cuenta de IVA CPG
				 */
			    total= (BigDecimal)row.get("IMPORTE");
				BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
				impuesto=MonedasUtils.calcularImpuesto(importe);
				impuesto=CantidadMonetaria.pesos(impuesto).amount();
				impuestoSAFDisp=MonedasUtils.calcularImpuesto(importe);
				impuestoSAFDisp=impuestoSAFDisp.add(CantidadMonetaria.pesos(impuestoSAFDisp).amount());				
				
				PolizaDet abonoAAcredoresDiversos=poliza.agregarPartida();
				abonoAAcredoresDiversos.setCuenta(getCuenta("203"));
				abonoAAcredoresDiversos.setHaber((BigDecimal)row.get("IMPORTE"));
				abonoAAcredoresDiversos.setDescripcion("Saldo a Favor");
				abonoAAcredoresDiversos.setDescripcion2((String)row.get("CONCEPTO"));
				abonoAAcredoresDiversos.setReferencia(origen);
				abonoAAcredoresDiversos.setReferencia2((String)row.get("SUCURSAL"));
				abonoAAcredoresDiversos.setAsiento(asiento);
				
				/*String tipoDePago2=(String)row.get("TIPO_PAGO");
				if("PAGO_TAR".equals(tipoDePago)){
					impuesto=impuesto.multiply(new BigDecimal(-1));
				}*/
				
				//Abono  a IVA de Saldo a Favor
				PolizaDet cargoaIvaSaldoaFavor=poliza.agregarPartida();
				cargoaIvaSaldoaFavor.setCuenta(getCuenta("206"));
				cargoaIvaSaldoaFavor.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
				cargoaIvaSaldoaFavor.setDescripcion2("IVA por Trasladar Saldo a Favor");
				cargoaIvaSaldoaFavor.setDebe(impuesto);
				cargoaIvaSaldoaFavor.setReferencia(origen);
				cargoaIvaSaldoaFavor.setReferencia2((String)row.get("SUCURSAL"));
				cargoaIvaSaldoaFavor.setAsiento(asiento);
				
				//Cargo  a IVA de Saldo a Favor
				PolizaDet abonoaIvaSaldoaFavor=poliza.agregarPartida();
				abonoaIvaSaldoaFavor.setCuenta(getCuenta("206"));
				abonoaIvaSaldoaFavor.setDescripcion(IVA_EN_VENTAS);
				abonoaIvaSaldoaFavor.setDescripcion2("IVA Trasladado Saldo a Favor");
				abonoaIvaSaldoaFavor.setHaber(impuesto);
				abonoaIvaSaldoaFavor.setReferencia(origen);
				abonoaIvaSaldoaFavor.setReferencia2((String)row.get("SUCURSAL"));
				abonoaIvaSaldoaFavor.setAsiento(asiento);
							
				/*if("PAGO_TAR".equals(tipoDePago)){
					continue;
				}*/
				
				asiento=asiento.concat(" IETU");
			
				PolizaDet cargoIETU=poliza.agregarPartida();
				cargoIETU.setCuenta(getCuenta("902"));
				cargoIETU.setDebe(importe);
				cargoIETU.setDescripcion("ACUMULABLE IETU SALDO A FAVOR");
				cargoIETU.setDescripcion2("IETU Acumulable por saldo a favor");
				cargoIETU.setReferencia(origen);
				cargoIETU.setReferencia2((String)row.get("SUCURSAL"));
				cargoIETU.setAsiento(asiento);
				
				PolizaDet abonoIETU=poliza.agregarPartida();
				abonoIETU.setCuenta(getCuenta("903"));
				abonoIETU.setHaber(importe);
				abonoIETU.setDescripcion("IETU ACUMULABLE SALDO A FAVOR");
				abonoIETU.setDescripcion2("IETU Acumulable por saldo a favor");
				abonoIETU.setReferencia(origen);
				abonoIETU.setReferencia2((String)row.get("SUCURSAL"));
				abonoIETU.setAsiento(asiento);
				
				
			}
			System.out.println(impuestoSAFDisp);
		}
		
		
	}
	
	private void registrarSaldosAFavorDeOtrasCarteras(){
		String SQL="select 'APLICACION' AS TIPO,A.APLICACION_ID AS ORIGEN_ID,A.FECHA ,A.CAR_SUCURSAL AS SUCURSAL,A.ABONO_ID" +
				",a.CAR_ORIGEN AS ORIGEN ,a.ABN_DESCRIPCION as CONCEPTO,(A.importe) AS IMPORTE " +
				",substr(trim(a.ABN_DESCRIPCION),1,3) AS DESCRIPCION  " +
				",(SELECT Y.CAR_ORIGEN FROM sx_cxc_aplicaciones Y WHERE Y.ABONO_ID=X.ABONO_ID AND Y.FECHA=X.SAF) AS BANCO" +
				" from sx_cxc_aplicaciones a  JOIN sx_cxc_abonos X ON(A.ABONO_ID=X.ABONO_ID) " +
				" where a.fecha=? AND A.CAR_ORIGEN IN('MOS','CAM') AND A.CAR_ORIGEN <>(SELECT MAX(Y.CAR_ORIGEN) FROM sx_cxc_aplicaciones Y WHERE Y.ABONO_ID=X.ABONO_ID AND Y.FECHA=X.SAF) " +
				" AND substr(a.ABN_DESCRIPCION,1,3)='SAF' AND A.TIPO='PAGO'";
		EventList<CIngresoContado> depositosConciliados=GlazedLists.eventList(ServiceLocator2.getJdbcTemplate().query(SQL
				,new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
				,new BeanPropertyRowMapper(CIngresoContado.class)
				));
		String asiento="Cobranza CAM"; 
		
		for(CIngresoContado c:depositosConciliados){
			BigDecimal total=c.getIMPORTE();

			if(c.getORIGEN().equals("MOS")){
				asiento="Ventas MOS";
			}
			
			
			
			PolizaDet cargo=poliza.agregarPartida();
			cargo.setCuenta(getCuenta("203"));
			cargo.setDebe(total);
			cargo.setDescripcion("ACREEDORES DIVERSOS ("+c.getBANCO()+ ")");
			cargo.setDescripcion2("SALDO A FAVOR DE ORIGEN: "+c.getBANCO());
			cargo.setReferencia(c.getBANCO());
			cargo.setReferencia2(c.getSUCURSAL());
			cargo.setAsiento(asiento);
			
			PolizaDet abono=poliza.agregarPartida();
			abono.setCuenta(getCuenta("203"));
			abono.setHaber(total);
			abono.setDescripcion("ACREEDORES DIVERSOS ("+c.getORIGEN() + ")");
			abono.setDescripcion2("TRASPASO DE SALDO A FAVOR DE ORIGEN: "+c.getBANCO());
			abono.setReferencia(c.getORIGEN());
			abono.setReferencia2(c.getSUCURSAL());
			abono.setAsiento(asiento);
		}
	}
	
	
	private void registrarCobranzaCamioneta(){
		
		String sql="SELECT 'DEPOSITO' AS TIPO,a.ABONO_ID AS ORIGEN_ID,B.FECHA,S.NOMBRE AS SUCURSAL,b.CARGOABONO_ID "+
		",case when B.ORIGEN='VENTA_CAMIONETA' THEN 'CAM' WHEN B.ORIGEN='VENTA_MOSTRADOR' THEN 'MOS' ELSE 'N/D' END AS ORIGEN "+
		",CASE WHEN A.TRANSFERENCIA<>0 THEN CONCAT('TRANSFERENCIA','-',(SELECT X.DESCRIPCION FROM sw_cuentas X  WHERE X.id=B.CUENTA_ID),' REF:',IFNULL(A.REFERENCIA,'')) ELSE CONCAT('DEPOSITO','-',(SELECT X.DESCRIPCION FROM sw_cuentas X  WHERE X.id=B.CUENTA_ID),' REF:',IFNULL(A.REFERENCIA,'')) END AS CONCEPTO "+
		",B.IMPORTE,(SELECT X.DESCRIPCION FROM sw_cuentas X  WHERE X.id=B.CUENTA_ID) AS BANCO "+ 
		" ,CASE WHEN A.TRANSFERENCIA<>0 THEN 'TRANSFERENCIA' ELSE 'DEPOSITO' END DESCRIPCION "+  
		" FROM sw_bcargoabono b join sx_cxc_abonos a on(a.ABONO_ID=b.PAGO_ID) JOIN sw_sucursales S ON(S.SUCURSAL_ID=B.SUCURSAL_ID) " +
		" where b.fecha=? and b.ORIGEN IN('VENTA_CAMIONETA') and b.conciliado=true AND A.ABONO_ID NOT IN(SELECT X.ABONO_ID FROM SX_CXC_APLICACIONES X JOIN SX_VENTAS V ON(V.CARGO_ID=X.CARGO_ID) WHERE X.ABONO_ID=A.ABONO_ID AND V.TIPO='TES')";
		EventList<CIngresoContado> depositosConciliados=GlazedLists.eventList(ServiceLocator2.getJdbcTemplate().query(sql
				,new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
				,new BeanPropertyRowMapper(CIngresoContado.class)
				));
		String asiento="CAM (Conciliados)";
		
		for(CIngresoContado c:depositosConciliados){
			BigDecimal total=c.getIMPORTE();
			//BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
			//BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			
			PolizaDet abonoCliente=poliza.agregarPartida();
			abonoCliente.setCuenta(getCuenta("105"));
			abonoCliente.setHaber(total);
			abonoCliente.setDescripcion("CLIENTES CAMIONETA");
			abonoCliente.setDescripcion2(c.getCONCEPTO());
			abonoCliente.setReferencia(c.getORIGEN());
			abonoCliente.setReferencia2(c.getSUCURSAL());
			abonoCliente.setAsiento(asiento);
			
		}
		Map<String,BigDecimal> totalConciliadoPorSucursal=new HashMap<String, BigDecimal>();
		Comparator<CIngresoContado> comparator=GlazedLists.beanPropertyComparator(CIngresoContado.class, "SUCURSAL");
		GroupingList<CIngresoContado> depositosConciladosPorSucursal=new GroupingList<CIngresoContado>(depositosConciliados,comparator);
		for(List<CIngresoContado> depositos:depositosConciladosPorSucursal){
			
			BigDecimal totalConciliado=BigDecimal.valueOf(0);
			BigDecimal importe=BigDecimal.valueOf(0);
			BigDecimal iva=BigDecimal.valueOf(0);
			
			for(CIngresoContado c:depositos){
				totalConciliado=totalConciliado.add(c.getIMPORTE());								
			}
			importe=MonedasUtils.calcularImporteDelTotal(totalConciliado,6);
			iva=MonedasUtils.calcularImpuesto(importe);
			
			importe=CantidadMonetaria.pesos(importe).amount();
			iva=CantidadMonetaria.pesos(iva).amount();
			
			if(totalConciliado.doubleValue()>0){
				CIngresoContado c=depositos.get(0);
				totalConciliadoPorSucursal.put(c.getSUCURSAL(), totalConciliado);
				PolizaDet cargoAcredores=poliza.agregarPartida();
				cargoAcredores.setCuenta(getCuenta("203"));
				cargoAcredores.setDebe(importe);
				cargoAcredores.setDescripcion("ACREEDORES DIVERSOS");
				cargoAcredores.setDescripcion2("Dep. Conciliados");
				cargoAcredores.setReferencia(c.getORIGEN());
				cargoAcredores.setReferencia2(c.getSUCURSAL());
				cargoAcredores.setAsiento(asiento);
			
				PolizaDet cargoIva=poliza.agregarPartida();
				cargoIva.setCuenta(getCuenta("206"));
				cargoIva.setDebe(iva);
				cargoIva.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
				cargoIva.setDescripcion2("Iva por Trasladado Dep. Conciliados");
				cargoIva.setReferencia(c.getORIGEN());
				cargoIva.setReferencia2(c.getSUCURSAL());
				cargoIva.setAsiento(asiento);
				
				//Cancelacion de IETU de Depositos por identificar
				PolizaDet abonoIETU=poliza.agregarPartida();
				abonoIETU.setCuenta(getCuenta("902"));
				abonoIETU.setHaber(importe);
				abonoIETU.setDescripcion("ACUMULABLE IETU DEPOSITO NO IDENTIFICADO");
				abonoIETU.setDescripcion2("Cancelacion IETU Acumulable Dep. Conciliados");
				abonoIETU.setReferencia(c.getORIGEN());
				abonoIETU.setReferencia2(c.getSUCURSAL());
				abonoIETU.setAsiento(asiento);
				
				PolizaDet cargoIETU=poliza.agregarPartida();
				cargoIETU.setCuenta(getCuenta("903"));
				cargoIETU.setDebe(importe);
				cargoIETU.setDescripcion("IETU ACUMULABLE DEPOSITO NO IDENTIFICADO");
				cargoIETU.setDescripcion2("Cancelacion IETU Acumulable Dep. Conciliados");
				cargoIETU.setReferencia(c.getORIGEN());
				cargoIETU.setReferencia2(c.getSUCURSAL());
				cargoIETU.setAsiento(asiento);
				
			}
			
		}
		
		//Cargo a Bancos
		asiento="Cobranza CAM";
		for(CIngresoContado ingreso:ingresos){
			if(ingreso.getORIGEN().equals("CAM")){
				String concepto=ingreso.getCONCEPTO();
				if( concepto.startsWith(TipoDeAplicacion.COMISION_AMEX.name())
						 ||concepto.startsWith(TipoDeAplicacion.COMISION_CREDITO.name())
						 ||concepto.startsWith(TipoDeAplicacion.COMISION_DEBITO.name()) 
						 ||concepto.startsWith(TipoDeAplicacion.IMPUESTO.name())
						 )
						continue;
				PolizaDet cargoABancos=poliza.agregarPartida();
				cargoABancos.setCuenta(getCuenta("102"));
				cargoABancos.setDebe(ingreso.getIMPORTE().abs());
				cargoABancos.setDescripcion(ingreso.getBANCO());	
				cargoABancos.setDescripcion2(ingreso.getCONCEPTO());	
				cargoABancos.setReferencia(ingreso.getORIGEN());
				cargoABancos.setReferencia2(ingreso.getSUCURSAL());
				cargoABancos.setAsiento(asiento);
				
				/*Cargo a clientes
				PolizaDet abonoAClientes=poliza.agregarPartida();
				CuentaContable clientes=getCuenta("105");
					
				abonoAClientes.setCuenta(clientes);
				abonoAClientes.setHaber(ingreso.getIMPORTE().abs());
				abonoAClientes.setDescripcion("CLIENTES");
				abonoAClientes.setReferencia(ingreso.getORIGEN());
				abonoAClientes.setReferencia2(ingreso.getSUCURSAL());
				abonoAClientes.setAsiento(asiento);
				*/
				
			}
		}
		// Abono a Clientes 
		EventList<CCobranza> source=GlazedLists.eventList(cobranzaPorOrigen.get("CAM"));
		GroupingList<CCobranza> cobranzaPorSucursal=new GroupingList<CCobranza>(source,GlazedLists.beanPropertyComparator(CCobranza.class, "sucursal"));
		for(List<CCobranza> porSucursal:cobranzaPorSucursal){	
			
			
			
			BigDecimal importe=BigDecimal.ZERO;
			BigDecimal saf=BigDecimal.ZERO;
			BigDecimal anticipos=BigDecimal.ZERO;
			//Sumarizamos por sucursal
			for(CCobranza cc:porSucursal){
				if(cc.getDescripcion().equalsIgnoreCase("EFE") 
						|| cc.getDescripcion().equalsIgnoreCase("CHE")
				//		|| cc.getDescripcion().equalsIgnoreCase("TAR")
						|| cc.getDescripcion().equalsIgnoreCase("Dep")
						|| cc.getDescripcion().equalsIgnoreCase("TRA")
						|| cc.getDescripcion().equalsIgnoreCase("SAF")
						|| cc.getDescripcion().equalsIgnoreCase("ANT")){
					System.out.println("Acumulando: "+ToStringBuilder.reflectionToString(cc));
					importe=importe.add(cc.getImporte());
					
					if(cc.getDescripcion().equalsIgnoreCase("SAF")){
						saf=saf.add(cc.getImporte());
					}
					if(cc.getDescripcion().equalsIgnoreCase("ANT")){
						anticipos=anticipos.add(cc.getImporte());
					}
				}
			}
			
			BigDecimal importeSAF=MonedasUtils.calcularImporteDelTotal(saf);
			importeSAF=CantidadMonetaria.pesos(importeSAF).amount();
			
			BigDecimal impuestoSAF=MonedasUtils.calcularImpuestoDelTotal(saf);
			impuestoSAF=CantidadMonetaria.pesos(impuestoSAF).amount();
			
			BigDecimal importeAnticipo=MonedasUtils.calcularImporteDelTotal(anticipos,6);
			BigDecimal impuestoAnticipo=MonedasUtils.calcularImpuesto(importeAnticipo);
			importeAnticipo=CantidadMonetaria.pesos(importeAnticipo).amount();
			impuestoAnticipo=CantidadMonetaria.pesos(impuestoAnticipo).amount();
			
			String sucursal=porSucursal.get(0).getSucursal();
			String descripcion="CLIENTES CAMIONETA";
				
			PolizaDet abonoaClientes=poliza.agregarPartida();
			abonoaClientes.setCuenta(getCuenta("105"));
			BigDecimal totalConciliado=totalConciliadoPorSucursal.get(sucursal);
			if(totalConciliado==null)
				totalConciliado=BigDecimal.ZERO;
			abonoaClientes.setHaber(importe.subtract(totalConciliado));
			abonoaClientes.setDescripcion(descripcion);
			abonoaClientes.setDescripcion2("Clientes Camioneta (Cobranza)");
			abonoaClientes.setReferencia("CAM");
			abonoaClientes.setReferencia2(sucursal);
			abonoaClientes.setAsiento(asiento);
			
				
			BigDecimal impuesto=MonedasUtils.calcularImpuestoDelTotal(importe);
			//BigDecimal impuestoSAFDisp=MonedasUtils.calcularImpuestoDelTotal(calcularSAFDisp(sucursal));
			//System.out.println("Impuesto original: "+impuesto);
			//System.out.println("Iva SAF:"+impuestoSAF);
			//System.out.println("Iva SAF:"+impuestoAnticipo);
			//System.out.println("Iva SAF:"+impuestoSAFDisp);
			//impuesto=impuesto.subtract(impuestoSAF).subtract(impuestoAnticipo).add(impuestoSAFDisp);
			impuesto=impuesto.subtract(impuestoSAF).subtract(impuestoAnticipo);
						
			impuesto=CantidadMonetaria.pesos(impuesto).amount();
			
			PolizaDet abonoAIvaTrasladado=poliza.agregarPartida();
			abonoAIvaTrasladado.setCuenta(getCuenta("206"));
			abonoAIvaTrasladado.setHaber(impuesto.subtract(MonedasUtils.calcularImpuestoDelTotal(totalConciliado)));
			abonoAIvaTrasladado.setDescripcion(IVA_EN_VENTAS);
			abonoAIvaTrasladado.setDescripcion2("IVA Trasladado (Cobranza)");
			abonoAIvaTrasladado.setReferencia("CAM");
			abonoAIvaTrasladado.setReferencia2(sucursal);
			abonoAIvaTrasladado.setAsiento(asiento);
			
			
			PolizaDet cargoAIvaEnVentas=poliza.agregarPartida();
			cargoAIvaEnVentas.setCuenta(getCuenta("206"));
			cargoAIvaEnVentas.setDebe(impuesto.subtract(MonedasUtils.calcularImpuestoDelTotal(totalConciliado)));
			cargoAIvaEnVentas.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
			cargoAIvaEnVentas.setDescripcion2("IVA por Trasladar (Cobranza)");
			cargoAIvaEnVentas.setReferencia("CAM");
			cargoAIvaEnVentas.setReferencia2(sucursal);
			cargoAIvaEnVentas.setAsiento(asiento);
			
			//Cargo a Acredores diversos de aplicaciones de SAF
			
			if(saf.doubleValue()>0){
				PolizaDet cargoSaldosAFavor=poliza.agregarPartida();
				cargoSaldosAFavor.setCuenta(getCuenta("203"));
				cargoSaldosAFavor.setDebe(saf);
				cargoSaldosAFavor.setDescripcion("ACREEDORES DIVERSOS");
				cargoSaldosAFavor.setDescripcion2("Aplicacion de Saldo a Favor");
				cargoSaldosAFavor.setReferencia("CAM");
				cargoSaldosAFavor.setReferencia2(sucursal);
				cargoSaldosAFavor.setAsiento(asiento);
				
				
				/*
				PolizaDet abonoAIvaTrasladadoSAF=poliza.agregarPartida();
				abonoAIvaTrasladadoSAF.setCuenta(getCuenta("206"));
				abonoAIvaTrasladadoSAF.setHaber(impuestoSAF);
				abonoAIvaTrasladadoSAF.setDescripcion(IVA_EN_VENTAS);
				abonoAIvaTrasladadoSAF.setDescripcion2("IVA Trasladado SAF");
				abonoAIvaTrasladadoSAF.setReferencia("CAM");
				abonoAIvaTrasladadoSAF.setReferencia2(sucursal);
				abonoAIvaTrasladadoSAF.setAsiento(asiento);
				
				PolizaDet cargoAIvaEnVentasSAF=poliza.agregarPartida();
				cargoAIvaEnVentasSAF.setCuenta(getCuenta("206"));
				cargoAIvaEnVentasSAF.setDebe(impuestoSAF);
				cargoAIvaEnVentasSAF.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
				cargoAIvaEnVentasSAF.setDescripcion2("IVA por Trasladar SAF");
				cargoAIvaEnVentasSAF.setReferencia("CAM");
				cargoAIvaEnVentasSAF.setReferencia2(sucursal);
				cargoAIvaEnVentasSAF.setAsiento(asiento);
				*/
				
				//CANCELACION DE IETU SALDO A FAVOR
				PolizaDet abonoSAFCancelacionIETU=poliza.agregarPartida();
				abonoSAFCancelacionIETU.setCuenta(getCuenta("902"));
				abonoSAFCancelacionIETU.setHaber(importeSAF);
				abonoSAFCancelacionIETU.setDescripcion("ACUMULABLE IETU SALDO A FAVOR");
				abonoSAFCancelacionIETU.setDescripcion2("Cancelacion de IETU Saldo a favor");
				abonoSAFCancelacionIETU.setReferencia("CAM");
				abonoSAFCancelacionIETU.setReferencia2(sucursal);
				abonoSAFCancelacionIETU.setAsiento(asiento);
				
				PolizaDet cargoSAFCancelacionIETU=poliza.agregarPartida();
				cargoSAFCancelacionIETU.setCuenta(getCuenta("903"));
				cargoSAFCancelacionIETU.setDebe(importeSAF);
				cargoSAFCancelacionIETU.setDescripcion("IETU ACUMULABLE SALDO A FAVOR");
				cargoSAFCancelacionIETU.setDescripcion2("Cancelacion de IETU Saldo a favor");
				cargoSAFCancelacionIETU.setReferencia("CAM");
				cargoSAFCancelacionIETU.setReferencia2(sucursal);
				cargoSAFCancelacionIETU.setAsiento(asiento);
								
				//IETU Aplicacion Saldo a Favor
				PolizaDet cargoIETU=poliza.agregarPartida();
				cargoIETU.setCuenta(getCuenta("902"));
				cargoIETU.setDebe(importeSAF);
				cargoIETU.setDescripcion("ACUMULABLE IETU CAMIONETA");
				cargoIETU.setDescripcion2("IETU Acumulable por cobranza Camioneta");
				cargoIETU.setReferencia("CAM");
				cargoIETU.setReferencia2(sucursal);
				cargoIETU.setAsiento(asiento);
				
				PolizaDet abonoIETU=poliza.agregarPartida();
				abonoIETU.setCuenta(getCuenta("903"));
				abonoIETU.setHaber(importeSAF);
				abonoIETU.setDescripcion("IETU ACUMULABLE CAMIONETA");
				abonoIETU.setDescripcion2("IETU Acumulable por cobranza Camioneta");
				abonoIETU.setReferencia("CAM");
				abonoIETU.setReferencia2(sucursal);
				abonoIETU.setAsiento(asiento);				
				
			}
			if(anticipos.doubleValue()>0){

				
				PolizaDet cargoAAnticipos=poliza.agregarPartida();
				cargoAAnticipos.setCuenta(getCuenta("204"));
				cargoAAnticipos.setDebe(importeAnticipo);
				cargoAAnticipos.setDescripcion("ANTICIPOS DE CLIENTES");
				cargoAAnticipos.setDescripcion2("Aplicacion de Anticipo");
				cargoAAnticipos.setReferencia("CAM");
				cargoAAnticipos.setReferencia2(sucursal);
				cargoAAnticipos.setAsiento(asiento);
				
				
				
				PolizaDet cargoAIvaTrasladadoAnticipo=poliza.agregarPartida();
				cargoAIvaTrasladadoAnticipo.setCuenta(getCuenta("206"));
				cargoAIvaTrasladadoAnticipo.setDebe(impuestoAnticipo);
				cargoAIvaTrasladadoAnticipo.setDescripcion(IVA_EN_ANTICIPO);
				cargoAIvaTrasladadoAnticipo.setDescripcion2("IVA en Anticipo");
				cargoAIvaTrasladadoAnticipo.setReferencia("CAM");
				cargoAIvaTrasladadoAnticipo.setReferencia2(sucursal);
				cargoAIvaTrasladadoAnticipo.setAsiento(asiento);			
				
				PolizaDet abonoAIvaTrasladadoAnt=poliza.agregarPartida();
				abonoAIvaTrasladadoAnt.setCuenta(getCuenta("206"));
				abonoAIvaTrasladadoAnt.setHaber(impuestoAnticipo);
				abonoAIvaTrasladadoAnt.setDescripcion(IVA_EN_VENTAS);
				abonoAIvaTrasladadoAnt.setDescripcion2("IVA Trasladado Anticipo");
				abonoAIvaTrasladadoAnt.setReferencia("CAM");
				abonoAIvaTrasladadoAnt.setReferencia2(sucursal);
				abonoAIvaTrasladadoAnt.setAsiento(asiento);
				
				
				PolizaDet cargoAIvaPorTrasladarAnt=poliza.agregarPartida();
				cargoAIvaPorTrasladarAnt.setCuenta(getCuenta("206"));
				cargoAIvaPorTrasladarAnt.setDebe(impuestoAnticipo);
				cargoAIvaPorTrasladarAnt.setDescripcion(IVA_EN_VENTAS_PENDIENTE);
				cargoAIvaPorTrasladarAnt.setDescripcion2("IVA por Trasladar Anticipo");
				cargoAIvaPorTrasladarAnt.setReferencia("CAM");
				cargoAIvaPorTrasladarAnt.setReferencia2(sucursal);
				cargoAIvaPorTrasladarAnt.setAsiento(asiento);
				
				PolizaDet abonoIETUaAnticipo=poliza.agregarPartida();
				abonoIETUaAnticipo.setCuenta(getCuenta("902"));
				abonoIETUaAnticipo.setHaber(importeAnticipo);
				abonoIETUaAnticipo.setDescripcion("ACUMULABLE IETU ANTICIPO DE CLIENTES");
				abonoIETUaAnticipo.setDescripcion2("Cancelacion de IETU Acumulable Anticipo Cliente");
				abonoIETUaAnticipo.setReferencia("CAM");
				abonoIETUaAnticipo.setReferencia2(sucursal);
				abonoIETUaAnticipo.setAsiento(asiento);
				
				PolizaDet cargoIETUaAnticipo=poliza.agregarPartida();
				cargoIETUaAnticipo.setCuenta(getCuenta("903"));
				cargoIETUaAnticipo.setDebe(importeAnticipo);
				cargoIETUaAnticipo.setDescripcion("IETU ACUMULABLE ANTICIPO DE CLIENTES");
				cargoIETUaAnticipo.setDescripcion2("Cancelacion de IETU Acumulable Anticipo Cliente");
				cargoIETUaAnticipo.setReferencia("CAM");
				cargoIETUaAnticipo.setReferencia2(sucursal);
				cargoIETUaAnticipo.setAsiento(asiento);
				
				PolizaDet cargoIETUaAntCamioneta=poliza.agregarPartida();
				cargoIETUaAntCamioneta.setCuenta(getCuenta("902"));
				cargoIETUaAntCamioneta.setDebe(importeAnticipo);
				cargoIETUaAntCamioneta.setDescripcion("ACUMULABLE IETU CAMIONETA");
				cargoIETUaAntCamioneta.setDescripcion2("IETU Acumulable Camioneta");
				cargoIETUaAntCamioneta.setReferencia("CAM");
				cargoIETUaAntCamioneta.setReferencia2(sucursal);
				cargoIETUaAntCamioneta.setAsiento(asiento);
				
				PolizaDet abonoIETUaAntCamioneta=poliza.agregarPartida();
				abonoIETUaAntCamioneta.setCuenta(getCuenta("903"));
				abonoIETUaAntCamioneta.setHaber(importeAnticipo);
				abonoIETUaAntCamioneta.setDescripcion("IETU ACUMULABLE CAMIONETA");
				abonoIETUaAntCamioneta.setDescripcion2("IETU Acumulable Camioneta");
				abonoIETUaAntCamioneta.setReferencia("CAM");
				abonoIETUaAntCamioneta.setReferencia2(sucursal);
				abonoIETUaAntCamioneta.setAsiento(asiento);
				
			}
		}
		
		
		registrarOtrosGastos("CAM",asiento);
		registrarOtrosProductos("CAM",asiento);
	}
	
	/**
	 * Se procesan las notas de credito (Por devolucion y bonificacion) 
	 * 
	 */
	private void registrarDevoluciones(){
		
		String SQL="select 'NOTAS' AS TIPO,A.ABONO_ID AS ORIGEN_ID,(CASE WHEN X.CAR_ORIGEN='MOS' THEN X.FECHA ELSE A.FECHA END) AS FECHA" +
		",S.NOMBRE AS SUCURSAL,A.ABONO_ID,a.ORIGEN AS ORIGEN,A.TOTAL AS IMPORTE" +
		",CONCAT(substr(a.TIPO_ID,6,3),' Folio:',A.FOLIO) as CONCEPTO" +
		",'' AS BANCO,substr(a.TIPO_ID,6,3) AS DESCRIPCION" +
		" from sx_cxc_abonos a join sx_cxc_aplicaciones x on(x.abono_id=a.abono_id) join sw_sucursales s on(s.SUCURSAL_ID=a.SUCURSAL_ID)  " +
		" where (CASE WHEN X.CAR_ORIGEN='MOS' THEN X.FECHA ELSE A.FECHA END)=? and a.ORIGEN in(\'CAM\',\'MOS\') AND A.TIPO_ID LIKE \'NOTA%\'";
		/*
		String SQL="select 'APLICACION' AS TIPO,A.APLICACION_ID AS ORIGEN_ID,A.FECHA,A.CAR_SUCURSAL AS SUCURSAL,A.ABONO_ID,a.CAR_ORIGEN AS ORIGEN,(importe) AS IMPORTE" +
		",CONCAT(a.ABN_DESCRIPCION,' Folio:',(SELECT X.FOLIO FROM SX_CXC_ABONOS X WHERE A.ABONO_ID=X.ABONO_ID),' Doc.:',a.CAR_DOCTO) as CONCEPTO" +
		",(SELECT C.DESCRIPCION FROM sx_cxc_abonos X JOIN sw_cuentas C ON(C.ID=X.CUENTA_ID) WHERE X.ABONO_ID=A.ABONO_ID) AS BANCO,substr(a.ABN_DESCRIPCION,1,3) AS DESCRIPCION" +
		" from sx_cxc_aplicaciones a where a.fecha=? and a.CAR_ORIGEN in(\'MOS\',\'CAM\') AND A.TIPO=\'NOTA\'";*/
		Object[] params=new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())};
		List<CCobranza> aplicaciones=ServiceLocator2.getJdbcTemplate().query(SQL, params, new BeanPropertyRowMapper(CCobranza.class));
	String asiento="CAM NOTAS (Dev-Desctos)"; 
		
		
		
		for(CCobranza cc:aplicaciones){
			String origen=cc.getOrigen();
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(cc.getImporte());
			BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			BigDecimal total=cc.getImporte();

			if(origen.equals("MOS")){
				asiento="MOS NOTAS (Dev-Desctos)";
			}
			

			
			if(origen.equals("CAM")){
				if(cc.getDescripcion().startsWith("BON")){
					// Cargo a Descuetos sobre ventas (406)
					PolizaDet cargoADescuentos=poliza.agregarPartida();
					cargoADescuentos.setCuenta(getCuenta("406"));
					cargoADescuentos.setDebe(importe);
					cargoADescuentos.setDescripcion("DESCUENTOS SOBRE VENTAS");
					cargoADescuentos.setDescripcion2(cc.getConcepto());
					cargoADescuentos.setReferencia(origen);
					cargoADescuentos.setReferencia2(cc.getSucursal());
					cargoADescuentos.setAsiento(asiento);
					
					PolizaDet cargoAIva=poliza.agregarPartida();
					cargoAIva.setCuenta(getCuenta("206"));
					cargoAIva.setDebe(iva);
					
				
					if(origen.equals("MOS")){
						cargoAIva.setDescripcion(IVA_EN_VENTAS);
					}else{
						cargoAIva.setDescripcion(IVA_EN_DESC_VTAS_PENDIENTE);
					}		
					
					
					if(origen.equals("MOS")){
						cargoAIva.setDescripcion2("Iva Trasladado Vtas");
					}else{
						cargoAIva.setDescripcion2("Iva por Trasladar en Desct. Sobre Vtas");
					}
					
					cargoAIva.setReferencia(origen);
					cargoAIva.setReferencia2(cc.getSucursal());
					cargoAIva.setAsiento(asiento);
					
					//Abono a clientes
					
					PolizaDet abonoAClientes=poliza.agregarPartida();
					abonoAClientes.setCuenta(getCuenta("105"));
					abonoAClientes.setHaber(total);
					abonoAClientes.setDescripcion("CLIENTES ".concat(origen.equals("CAM")?"CAMIONETA":"CREDITO"));
					abonoAClientes.setDescripcion2("clientes ".concat(origen.equals("CAM")?"CAMIONETA":"CREDITO").concat(" Desc. en Vtas"));
					abonoAClientes.setReferencia(origen);
					abonoAClientes.setReferencia2(cc.getSucursal());
					abonoAClientes.setAsiento(asiento);
					
				}else if(cc.getDescripcion().startsWith("DEV")){
					
					//Cargo a Devoluciones sobre venta
					PolizaDet cargoADevoluciones=poliza.agregarPartida();
					cargoADevoluciones.setCuenta(getCuenta("405"));
					cargoADevoluciones.setDebe(importe);
					cargoADevoluciones.setDescripcion("DEVOLUCIONES SOBRE VENTAS");
					cargoADevoluciones.setDescripcion2(cc.getConcepto());
					cargoADevoluciones.setReferencia(origen);
					cargoADevoluciones.setReferencia2(cc.getSucursal());
					cargoADevoluciones.setAsiento(asiento);
					
					PolizaDet cargoAIva=poliza.agregarPartida();
					cargoAIva.setCuenta(getCuenta("206"));
					cargoAIva.setDebe(iva);
					
					
					if(origen.equals("MOS")){
						cargoAIva.setDescripcion(IVA_EN_VENTAS);
					}else{
						cargoAIva.setDescripcion(IVA_EN_DEV_VTAS_PENDIENTE);
					}	
					
					if(origen.equals("MOS")){
						cargoAIva.setDescripcion2("Iva Trasladado Vtas");
					}else{
						cargoAIva.setDescripcion2("Iva por Trasladar en Dev. Sobre Vtas");
					}
					
					cargoAIva.setReferencia(origen);
					cargoAIva.setReferencia2(cc.getSucursal());
					cargoAIva.setAsiento(asiento);
					
					//Abono a clientes
					
					PolizaDet abonoAClientes=poliza.agregarPartida();
					abonoAClientes.setCuenta(getCuenta("105"));
					abonoAClientes.setHaber(total);
					abonoAClientes.setDescripcion("CLIENTES ".concat(origen.equals("CAM")?"CAMIONETA":"CREDITO"));
					abonoAClientes.setDescripcion2("clientes ".concat(origen.equals("CAM")?"CAMIONETA":"CREDITO").concat(" Dev. en Vtas"));
					abonoAClientes.setReferencia(origen);
					abonoAClientes.setReferencia2(cc.getSucursal());
					abonoAClientes.setAsiento(asiento);
				}
			}else if(origen.equals("MOS")){//______________________________________________
				if(cc.getDescripcion().startsWith("BON")){
					// Cargo a Descuetos sobre ventas (406)
					PolizaDet cargoADescuentos=poliza.agregarPartida();
					cargoADescuentos.setCuenta(getCuenta("406"));
					cargoADescuentos.setDebe(importe);
					cargoADescuentos.setDescripcion("DESCUENTOS SOBRE VENTAS");
					cargoADescuentos.setDescripcion2(cc.getConcepto());
					cargoADescuentos.setReferencia(origen);
					cargoADescuentos.setReferencia2(cc.getSucursal());
					cargoADescuentos.setAsiento(asiento);
					
					PolizaDet cargoAIva=poliza.agregarPartida();
					cargoAIva.setCuenta(getCuenta("206"));
					cargoAIva.setDebe(iva);
					
					
					if(origen.equals("MOS")){
						cargoAIva.setDescripcion(IVA_EN_VENTAS);
					}else{
						cargoAIva.setDescripcion(IVA_EN_DESC_VTAS_PENDIENTE);
					}	
					
					if(origen.equals("MOS")){
						cargoAIva.setDescripcion2("Iva Trasladado Vtas");
					}else{
						cargoAIva.setDescripcion2("Iva por Trasladar en Desct. Sobre Vtas");
					}
					
					cargoAIva.setReferencia(origen);
					cargoAIva.setReferencia2(cc.getSucursal());
					cargoAIva.setAsiento(asiento);				
					
					
				}else if(cc.getDescripcion().startsWith("DEV")){
					
					//Cargo a Devoluciones sobre venta
					PolizaDet cargoADevoluciones=poliza.agregarPartida();
					cargoADevoluciones.setCuenta(getCuenta("405"));
					cargoADevoluciones.setDebe(importe);
					cargoADevoluciones.setDescripcion("DEVOLUCIONES SOBRE VENTAS");
					cargoADevoluciones.setDescripcion2(cc.getConcepto());
					cargoADevoluciones.setReferencia(origen);
					cargoADevoluciones.setReferencia2(cc.getSucursal());
					cargoADevoluciones.setAsiento(asiento);
					
					PolizaDet cargoAIva=poliza.agregarPartida();
					cargoAIva.setCuenta(getCuenta("206"));
					cargoAIva.setDebe(iva);
					
					if(origen.equals("MOS")){
						cargoAIva.setDescripcion(IVA_EN_VENTAS);
					}else{
						cargoAIva.setDescripcion(IVA_EN_DEV_VTAS_PENDIENTE);
					}	
					
					
					if(origen.equals("MOS")){
						cargoAIva.setDescripcion2("Iva Trasladado Vtas");
					}else{
						cargoAIva.setDescripcion2("Iva por Trasladar en Dev. Sobre Vtas");
					}	
					
					cargoAIva.setReferencia(origen);
					cargoAIva.setReferencia2(cc.getSucursal());
					cargoAIva.setAsiento(asiento);
				}
			}
		}
		//Cargo a Devoluciones (405) y/o Bonificaciones sobre ventas (406)
		//Abono a Clientes Camioneta(105) o a Ventas(401)
		
	}
	
	/*private void registrarDepositosEnTransito(){
		String sql="select C.DESCRIPCION AS BANCO,B.* from sw_bcargoabono B JOIN SW_CUENTAS C ON(B.CUENTA_ID=C.ID )where fecha=? and concepto_id=737331";
		List<Map<String, Object>> rows=ServiceLocator2
			.getJdbcTemplate()
			.queryForList(sql,
					new Object[]{new SqlParameterValue(Types.DATE,poliza.getFecha())}
					);
		String asiento="Tesoreria";
		for(Map<String, Object> row:rows){
			Number val=(Number)row.get("IMPORTE");
			BigDecimal total=new BigDecimal(val.doubleValue());
			BigDecimal importe=MonedasUtils.calcularImporteDelTotal(total);
			BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
			
			
			PolizaDet cargoBancos=poliza.agregarPartida();
			cargoBancos.setCuenta(getCuenta("102"));
			cargoBancos.setDebe(total);
			cargoBancos.setDescripcion((String)row.get("BANCO"));
			cargoBancos.setDescripcion2("Depositos por identificar");
			cargoBancos.setReferencia("NA");
			cargoBancos.setReferencia2("NA");
			cargoBancos.setAsiento(asiento);
			
			PolizaDet abonoAcredores=poliza.agregarPartida();
			abonoAcredores.setCuenta(getCuenta("203"));
			abonoAcredores.setHaber(importe);
			abonoAcredores.setDescripcion("ACREEDORES DIVERSOS");
			abonoAcredores.setDescripcion2("Depositos por Identificar");
			abonoAcredores.setReferencia("NA");
			abonoAcredores.setReferencia2("NA");
			abonoAcredores.setAsiento(asiento);
			
			PolizaDet abonoAIva=poliza.agregarPartida();
			abonoAIva.setCuenta(getCuenta("206"));
			abonoAIva.setHaber(iva);
			abonoAIva.setDescripcion(IVA_EN_DEPOSITOS_IDENTIFICAR);
			abonoAIva.setDescripcion2("Iva en Dep. por Identificar");
			abonoAIva.setReferencia("NA");
			abonoAIva.setReferencia2("NA");
			abonoAIva.setAsiento(asiento);
			
			PolizaDet cargoIETU=poliza.agregarPartida();
			cargoIETU.setCuenta(getCuenta("902"));
			cargoIETU.setDebe(importe);
			cargoIETU.setDescripcion("IETU ACUMULABLE DEPOSITO NO IDENTIFICADO");
			cargoIETU.setDescripcion2("IETU Acumulable Dep. por Identificar");
			cargoIETU.setReferencia("NA");
			cargoIETU.setReferencia2("NA");
			cargoIETU.setAsiento(asiento);
			
			PolizaDet abonoIETU=poliza.agregarPartida();
			abonoIETU.setCuenta(getCuenta("903"));
			abonoIETU.setHaber(importe);
			abonoIETU.setDescripcion("ACUMULABLE IETU DEPOSITO NO IDENTIFICADO");
			abonoIETU.setDescripcion2("IETU Acumulable Dep. por Identificar");
			abonoIETU.setReferencia("NA");
			abonoIETU.setReferencia2("NA");
			abonoIETU.setAsiento(asiento);
			
		}
		
		
	}*/
	
	private void registrarFaltantes(){
		
		ServiceLocator2
				.getHibernateTemplate()
				.executeFind(new HibernateCallback() {
					public Object doInHibernate(Session session) throws HibernateException,SQLException {
							List<CorreccionDeFicha> list=session.createQuery("from CorreccionDeFicha cc " +
									"where cc.fecha=?")
									.setParameter(0, poliza.getFecha(),Hibernate.DATE)
									.list();
							for(CorreccionDeFicha cc:list){
								PolizaDet abono=poliza.agregarPartida();
								abono.setCuenta(cc.getConcepto().getCuenta());
								abono.setConcepto(cc.getConcepto());
								abono.setDebe(cc.getDiferencia().abs());
								abono.setDescripcion2("FALTANTE EN FICHA: "+cc.getFicha().getFolio());
								abono.setReferencia(cc.getFicha().getOrigen().name());
								abono.setReferencia2(cc.getSucursal().getNombre());
								abono.setAsiento(
										OrigenDeOperacion.MOS.equals(cc.getFicha().getOrigen())
										?"Venta MOS":"Cobranza CAM");
							}
							
							return null;
						}
					});				
	}
	
	private CuentaContable getCuenta(String clave){
		return ServiceLocator2.getCuentasContablesManager().buscarPorClave(clave);
	}
	
	public Poliza salvarPoliza(Poliza target){
		boolean existe=ServiceLocator2.getPolizasManager().existe(target);
		return ServiceLocator2.getPolizasManager().salvarPoliza(target);
	}
	
	
			
	public boolean eliminarPoliza(Poliza poliza) {
		ServiceLocator2.getPolizasManager().getPolizaDao().remove(poliza.getId());
		return true;
		
	}

	public static void main(String[] args) {
		DBUtils.whereWeAre();
		PolizaDeVentasManager manager=new PolizaDeVentasManager();
		manager.generarPoliza(DateUtil.toDate("01/08/2011"));
		//manager.registrarAbonosTarjetaCamioneta();
		
	}

}
