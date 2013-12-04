package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.contabilidad.AsientoContable;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;

public class PolizaCobranzaCreditoModel {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private DateFormat df=new SimpleDateFormat("dd-MMM-yyyy");
	
	/**
	 * Genera las polizas contables de cobranza de credito para el periodo indicado 
	 * 
	 * @param p
	 * @return
	 */
	public List<Poliza> generarPoliza(final Periodo p){
		List<Poliza> polizas=new ArrayList<Poliza>();
		for(Date dia:p.getListaDeDias()){
			try {
				Poliza res=generarPoliza(dia);
				polizas.add(res);
			} catch (Exception e) {
				logger.error("No genero la poliza para el dia: "+dia+ " \nMsg: "+ExceptionUtils.getRootCauseMessage(e)
						,e);
				e.printStackTrace();
			}
		}
		return polizas;
	}
	
	private Poliza generarPoliza(final Date dia){
		final Poliza pol=new Poliza();
		pol.setConcepto("Cobranza de credito "+df.format(dia));
		pol.setFecha(dia);
		pol.setFolio(0);
		pol.setTipo("Ig");
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery("from Pago p where p.fecha=? and p.origen=\'CRE\'")
				.setParameter(0, dia,Hibernate.DATE)
				.scroll();
				while(rs.next()){
					Pago p=(Pago)rs.get()[0];
					if(p instanceof PagoConTarjeta)
						continue;
					Cuenta cuenta=p.getCuenta();
					if(cuenta==null){
						if(p.getDeposito()!=null){
							cuenta=p.getDeposito().getFicha().getCuenta();
						}else{
							logger.info("No encontro cuenta de bancos para el pago:" +p);
							continue;
						}
					}
					AsientoContable ac=new AsientoContable();
					ac.setConcepto(getConcepto(p));
					ac.setCuenta(cuenta.getCuentaContable());
					ac.setDebe(p.getTotalCM());
					ac.setDescripcion(p.getInfo());
					ac.setDescripcion3(p.getDepositoInfo());
					ac.setDescripcion2(p.getCliente().getNombreRazon());
					ac.setAgrupador("A");
					pol.getRegistros().add(ac);
				}				
				return null;
			}			
		});
		AsientoContable iva1=getIvaEnVentas(dia);
		iva1.setAgrupador("X");
		AsientoContable iva2=new AsientoContable();
		iva2.setDebe(iva1.getHaber());
		iva2.setAgrupador("X");
		iva2.setHaber(CantidadMonetaria.pesos(0));
		iva2.setConcepto(iva1.getConcepto());
		iva2.setCuenta("206-0002-001");
		pol.getRegistros().add(iva1);
		pol.getRegistros().add(iva2);
		agregarIetu(pol);
		agregarPagosConTarjeta(pol);
		agregarAplicacionesDeVentas(pol);
		agregarAbonosDeOtrosGastosAlCliente(pol);
		agregarNotasGeneradas(pol);
		agregarNotasDeCargo(pol);
		agregarSaldosAFavor(pol);
		agregarCargoDeOtrosGastos(pol);
		agregarIvanEnVentasOtrosIngresos(pol);
		agregarDescuentosSobreVentas(pol);
		agregarDevolucionesSobreVentas(pol);
		agregarSaldosAFavorDeNotas(pol);
		pol.sincronizar();
		ListIterator<AsientoContable> iter=pol.getRegistros().listIterator();
		while(iter.hasNext()){
			AsientoContable as=iter.next();
			if( (as.getDebe().amount().doubleValue()==0) && (as.getHaber().amount().doubleValue()==0))
				iter.remove();
		}
		return pol;
	}
	
	private String getConcepto(Pago pago){
		String banco="SIN BANCO";
		String desc="";
		if(pago instanceof PagoConCheque){
			PagoConCheque pc=(PagoConCheque)pago;
			if(pago.getCuenta()!=null){
			//if(pc.getDeposito()!=null){
				banco=pago.getCuenta().getBanco().getClave();
				desc="Dep: "+pc.getDeposito().getFicha().getFolio();
			}
		}else if(pago instanceof PagoConTarjeta){
			PagoConTarjeta pt=(PagoConTarjeta)pago;
			banco=pago.getBanco();
			desc=pt.getInfo();
		}else if(pago instanceof PagoConDeposito){
			PagoConDeposito dep=(PagoConDeposito)pago;
			BigDecimal transferencia=dep.getTransferencia();
			banco=dep.getBanco();
			if(StringUtils.isBlank(banco))
				banco="SIN_BANCO";
			if(transferencia.doubleValue()>0)
				desc=" Transf:"+dep.getReferenciaBancaria();
			else
				desc=" Dep:"+dep.getReferenciaBancaria();
			
		}else if(pago instanceof PagoConEfectivo){
			desc="Efectivo";
		}
		String pattern="{0}{1} {2} ";
		String res= MessageFormat.format(pattern, ""
				,banco
				,desc);
		return StringUtils.substring(res, 0, 27);
	}
	
	private AsientoContable getIvaEnVentas(final Date dia){
		AsientoContable ac=new AsientoContable();
		ac.setCuenta("206-0001-001");
		CantidadMonetaria importe=(CantidadMonetaria)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {				
				String hql="from  Pago p " +
				" where p.fecha=? " +
				" and p.origen=\'CRE\'" +
				" ";
				List<Pago> pagos= session.createQuery(hql)
					.setParameter(0, dia,Hibernate.DATE)
					.list();
				CantidadMonetaria total=CantidadMonetaria.pesos(0);
				for(Pago p:pagos){
					if(p instanceof PagoDeDiferencias)
						continue;
					total=total.add(p.getImporteCM());
				}
				return total;
			}
		});
		CantidadMonetaria iva=MonedasUtils.calcularImpuesto(importe);
		ac.setHaber(iva);
		ac.setConcepto("Cobranza credito "+df.format(dia));
		return ac;
	}
	
	private void agregarIetu(final Poliza poliza){
		AsientoContable a1=new AsientoContable();
		a1.setConcepto("ACUMULABLE IETU (CRE)");
		a1.setCuenta("902-0003-000");
		
		AsientoContable a2=new AsientoContable();
		a2.setConcepto("IETU ACUMULABLE (CRE)");
		a2.setCuenta("903-0003-000");
		
		CantidadMonetaria imp=getAplicacionesDePagos(poliza.getFecha());		
		a1.setDebe(imp);
		a1.setAgrupador("X");
		a2.setHaber(imp);
		a2.setAgrupador("X");
		poliza.getRegistros().add(a1);
		poliza.getRegistros().add(a2);
	}
	
	private CantidadMonetaria aplicacionesDePagoaplicadas=null;
	
	private CantidadMonetaria getAplicacionesDePagos(final Date dia){
		if(aplicacionesDePagoaplicadas==null){
			aplicacionesDePagoaplicadas=(CantidadMonetaria)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
				public Object doInHibernate(Session session)throws HibernateException, SQLException {
					
					
					String hql="from  Pago p " +
							" where p.fecha=? " +
							" and p.origen=\'CRE\'" +
							" ";
					List<Pago> pagos= session.createQuery(hql)
					.setParameter(0, dia,Hibernate.DATE)
					.list();
					CantidadMonetaria total=CantidadMonetaria.pesos(0);
					for(Pago p:pagos){
						if(p instanceof PagoDeDiferencias)
							continue;
						total=total.add(p.getImporteCM());
					}
					return total;
				}
			});
		}
		return aplicacionesDePagoaplicadas;
	}
	
	private void agregarPagosConTarjeta(final Poliza poliza){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql=" from PagoConTarjeta pago " +
						" where pago.fecha=? " +
						" and pago.origen=\'CRE\'";
				List<PagoConTarjeta> pagos=session.createQuery(hql).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				for(PagoConTarjeta pago:pagos){
					AsientoContable as=new AsientoContable();
					as.setCuenta("203-D002-000");
					String dta="CTA";
					if(!StringUtils.isBlank(pago.getCliente().getCuentaContable()))
						dta=StringUtils.substring(pago.getCliente().getCuentaContable(), 4, 8);
					String concepto="Acredores diversos "+dta;
					as.setConcepto(concepto);
					as.setDescripcion(pago.getInfo());
					as.setDescripcion2(pago.getCliente().getNombreRazon());					
					as.setDebe(pago.getTotalCM());
					as.setAgrupador("A");
					poliza.getRegistros().add(as);
				}
				return null;
			}
			
		});
	}
	
	private void agregarAplicacionesDeVentas(final Poliza poliza){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from AplicacionDePago ap " +
						" where ap.fecha=? " +
						" and ap.detalle.formaDePago not like '%AJUSTE%' " +
						" and ap.abono.origen=\'CRE\'" +
						" order by ap.abono.cliente desc";
				List<AplicacionDePago> aplicaciones=session.createQuery(hql).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				for(AplicacionDePago aplic:aplicaciones){
					AsientoContable as=new AsientoContable();
					
					Pago pago=aplic.getPago();
					as.setDescripcion(pago.getInfo());
					as.setAgrupador("A");
					String dta="SIN CTA";
					if(!StringUtils.isBlank(pago.getCliente().getCuentaContable()))
						dta=pago.getCliente().getCuentaContable();
					String concepto="PAGO DE Fac No:"+aplic.getCargo().getDocumento();
					
					if(!DateUtils.isSameDay(pago.getFecha(), aplic.getFecha())){
						concepto=concepto+" (Otros)";
						as.setDescripcion("SAF "+pago.getInfo());
						as.setAgrupador("F");
					}
					as.setCuenta(dta);
					as.setConcepto(concepto);
					
					as.setDescripcion2(pago.getCliente().getNombreRazon());
					as.setDescripcion3(String.valueOf(aplic.getCargo().getDocumento()));
					as.setHaber(aplic.getImporteCM());
					poliza.getRegistros().add(as);
				}
				return null;
			}
			
		});
	}
	
	private void agregarAbonosDeOtrosGastosAlCliente(final Poliza poliza){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from AplicacionDePago ap " +
						" where ap.fecha=? " +
						" and ap.detalle.formaDePago like '%AJUSTE%' " +
						" and ap.abono.origen=\'CRE\'" +
						" order by ap.abono.cliente desc";
				List<AplicacionDePago> aplicaciones=session.createQuery(hql).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				for(AplicacionDePago aplic:aplicaciones){
					AsientoContable as=new AsientoContable();
					Pago pago=aplic.getPago();
					if(!StringUtils.isBlank(pago.getCliente().getCuentaContable()))
						as.setCuenta(pago.getCliente().getCuentaContable());
					String dta="SIN CTA";
					if(!StringUtils.isBlank(pago.getCliente().getCuentaContable()))
						dta=pago.getCliente().getCuentaContable();
					String concepto="OTROS GASTOS "+dta;
					
					as.setConcepto(concepto);
					as.setAgrupador("G");
					as.setDescripcion("DIF "+pago.getInfo());
					as.setDescripcion2(pago.getCliente().getNombreRazon());
					as.setHaber(aplic.getImporteCM());
					poliza.getRegistros().add(as);
				}
				return null;
			}
			
		});
	}
	
	AsientoContable snAplDesc=new AsientoContable();
	AsientoContable snAplDevo=new AsientoContable();
	
	private void agregarNotasGeneradas(final Poliza poliza){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from NotaDeCredito nota " +
						" where nota.fecha=? " +
						" and nota.origen=\'CRE\' " +
						" order by nota.cliente desc";
				List<NotaDeCredito> notas=session.createQuery(hql).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				CantidadMonetaria snAplicDescuentos=CantidadMonetaria.pesos(0);
				CantidadMonetaria snAplicDevo=CantidadMonetaria.pesos(0);
				for(NotaDeCredito nota:notas){
					AsientoContable as=new AsientoContable();					
					if(!StringUtils.isBlank(nota.getCliente().getCuentaContable()))
						as.setCuenta(nota.getCliente().getCuentaContable());
					String concepto="NC "+StringUtils.substring(nota.getTipo(),5,8)+" "+nota.getFolio();
					
					as.setConcepto(concepto);
					as.setAgrupador("D");
					if(nota instanceof NotaDeCreditoDevolucion )
						as.setAgrupador("V");
					as.setDescripcion(nota.getInfo());
					as.setDescripcion2(nota.getCliente().getNombreRazon());
					as.setHaber(nota.getTotalCM());
					poliza.getRegistros().add(as);
					if(nota.getAplicaciones().isEmpty()){
						if(nota instanceof NotaDeCreditoDevolucion )
							snAplicDevo=snAplicDevo.add(nota.getTotalCM());
						else
							snAplicDescuentos=snAplicDescuentos.add(nota.getTotalCM());
					}
				}
				snAplDesc.setDebe(MonedasUtils.calcularImporteDelTotal(snAplicDescuentos));
				snAplDevo.setDebe(MonedasUtils.calcularImporteDelTotal(snAplicDevo));
				return null;
			}
			
		});
	}
	
	private void agregarNotasDeCargo(final Poliza poliza){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from NotaDeCargo nota " +
						" where nota.fecha=?  " +
						" and nota.origen=\'CRE\'" +
						" order by nota.cliente desc";
				List<NotaDeCargo> cargos=session.createQuery(hql).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				CantidadMonetaria intereses=CantidadMonetaria.pesos(0);
				for(NotaDeCargo cargo:cargos){
					AsientoContable as=new AsientoContable();					
					if(!StringUtils.isBlank(cargo.getCliente().getCuentaContable()))
						as.setCuenta(cargo.getCliente().getCuentaContable());
					String concepto="Nota de Cargo "+cargo.getDocumento();
					as.setConcepto(concepto);
					as.setDescripcion("Intereses");
					as.setDescripcion2(cargo.getCliente().getNombreRazon());
					as.setDebe(cargo.getTotalCM());
					as.setAgrupador("C");
					poliza.getRegistros().add(as);
					intereses=intereses.add(cargo.getTotalCM());
				}
				AsientoContable as=new AsientoContable();
				as.setHaber(intereses);
				as.setConcepto("Intereses Moratorios");
				as.setCuenta("701-0002-001");
				as.setHaber(MonedasUtils.calcularImporteDelTotal(intereses));
				as.setAgrupador("C");
				poliza.getRegistros().add(as);
				
				AsientoContable as2=new AsientoContable();
				as2.setHaber(intereses);
				as2.setConcepto("Iva otros ingresos pend  trasladar");
				as2.setCuenta("206-0002-004");
				as2.setHaber(MonedasUtils.calcularImpuesto(as.getHaber()));
				as2.setAgrupador("C");
				poliza.getRegistros().add(as2);
				
				return null;
			}			
		});
	}
	
	private void agregarSaldosAFavor(final Poliza poliza){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from Pago pago " +
						" where pago.fecha=?  " +
						" and pago.origen=\'CRE\'" +
						" order by pago.cliente desc";
				List<Pago> pagos=session.createQuery(hql).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				for(Pago pago:pagos){
					if(pago.isAnticipo()|| (pago instanceof PagoDeDiferencias))
						continue;
					CantidadMonetaria totalAplicado=CantidadMonetaria.pesos(0);					
					for(Aplicacion a:pago.getAplicaciones()){
						if(a==null){
							System.out.println("Err: "+pago.getId());
							System.out.println("Aplic: "+pago.getAplicaciones().size());
							System.out.println("ToString: "+ToStringBuilder.reflectionToString(a));
							continue;
						}
						if(DateUtils.isSameDay(a.getFecha(), pago.getFecha()))
							totalAplicado=totalAplicado.add(a.getImporteCM());
					}
					String dta="CTA";
					if(!StringUtils.isBlank(pago.getCliente().getCuentaContable()))
						dta=StringUtils.substring(pago.getCliente().getCuentaContable(), 4, 8);
					CantidadMonetaria saldoAFavor=pago.getTotalCM().subtract(totalAplicado);
						
					if(saldoAFavor.amount().doubleValue()>0){
						AsientoContable as=new AsientoContable();
						as.setHaber(saldoAFavor);
						as.setConcepto("Acredores diversos "+dta);
						as.setCuenta("203-D002-000");
						as.setDescripcion(pago.getInfo());
						as.setDescripcion2(pago.getCliente().getNombreRazon());
						as.setAgrupador("A");
						poliza.getRegistros().add(as);
					}
				}
				String hql2="from AplicacionDePago ap " +
						" where ap.fecha=? " +
						" and ap.detalle.formaDePago not like '%AJUSTE%' " +
						" and ap.abono.origen=\'CRE\'" +
						" order by ap.abono.cliente desc";
				List<AplicacionDePago> aplicaciones=session.createQuery(hql2).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				for(AplicacionDePago aplic:aplicaciones){					
					Pago pago=aplic.getPago();
					String dta="CTA";
					if(!StringUtils.isBlank(pago.getCliente().getCuentaContable()))
						dta=StringUtils.substring(pago.getCliente().getCuentaContable(), 4, 8);					
					if(!DateUtils.isSameDay(pago.getFecha(), aplic.getFecha())){
						AsientoContable as=new AsientoContable();
						as.setCuenta("203-D002-000");
						as.setConcepto("Pago con saldo:"+dta);
						as.setDescripcion(pago.getInfo());
						as.setDescripcion2(pago.getCliente().getNombreRazon());
						as.setDescripcion3(String.valueOf(aplic.getCargo().getDocumento()));
						as.setDebe(aplic.getImporteCM());
						as.setAgrupador("F");
						poliza.getRegistros().add(as);
					}
					
				}
				
				return null;
			}			
		});
	}
	
	private void agregarCargoDeOtrosGastos(final Poliza poliza){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from AplicacionDePago ap " +
						" where ap.fecha=? " +
						" and ap.detalle.formaDePago like '%AJUSTE%' " +
						" and ap.abono.origen=\'CRE\'" +
						" order by ap.abono.cliente desc";
				List<AplicacionDePago> aplicaciones=session.createQuery(hql).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				for(AplicacionDePago aplic:aplicaciones){
					Pago pago=aplic.getPago();
					String dta="CTA";
					if(!StringUtils.isBlank(pago.getCliente().getCuentaContable()))
						dta=StringUtils.substring(pago.getCliente().getCuentaContable(), 4, 8);		
					AsientoContable as=new AsientoContable();
					as.setCuenta("704-0001-000");
					as.setConcepto("Otros Gastos "+dta );
					as.setDescripcion(pago.getInfo());
					as.setDescripcion2(pago.getCliente().getNombreRazon());
					as.setDebe(aplic.getImporteCM());
					as.setAgrupador("G");
					poliza.getRegistros().add(as);
				}
				return null;
			}			
		});
	}
	
	private void agregarIvanEnVentasOtrosIngresos(final Poliza poliza){
		
		CantidadMonetaria acredores=CantidadMonetaria.pesos(0);
		CantidadMonetaria saldosAFavor=CantidadMonetaria.pesos(0);
		CantidadMonetaria pagosConSaldo=CantidadMonetaria.pesos(0);
		CantidadMonetaria diferencias=CantidadMonetaria.pesos(0);
		
		for(AsientoContable asiento:poliza.getRegistros()){
			if(asiento.getCuenta()==null) continue;
			if(asiento.getCuenta().equals("203-D002-000")){
				saldosAFavor=saldosAFavor.add(asiento.getHaber());
				pagosConSaldo=pagosConSaldo.add(asiento.getDebe());
			}else if(asiento.getCuenta().equals("704-0001-000")){
				diferencias=diferencias.add(asiento.getDebe());
			}else if(asiento.getCuenta().equals("")){
				
			} 
		}
		
		CantidadMonetaria res=acredores
			.add(saldosAFavor)
			.subtract(pagosConSaldo)
			.subtract(diferencias);
		
		res=MonedasUtils.calcularImporteDelTotal(res);
		res=MonedasUtils.calcularImpuesto(res);
		
		AsientoContable as=new AsientoContable();
		as.setCuenta("206-0001-001");
		as.setConcepto("Iva en ventas por otros ingresos");
		as.setHaber(res);
		as.setAgrupador("X");
		
		AsientoContable as2=new AsientoContable();
		as2.setCuenta("206-0002-001");
		as2.setConcepto("Iva en ventas pendiente");
		as2.setDebe(res);
		as2.setAgrupador("X");
		
		poliza.getRegistros().add(as);
		poliza.getRegistros().add(as2);
		
	}
	
	private void agregarDescuentosSobreVentas(final Poliza poliza){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from AplicacionDeNota ap " +
						" where ap.abono.fecha=? " +
						" and ap.abono.origen=\'CRE\'" +
						" order by ap.abono.cliente desc";
				List<AplicacionDeNota> aplicaciones=session.createQuery(hql).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				
				Map<Sucursal, CantidadMonetaria> map=new HashMap<Sucursal, CantidadMonetaria>();
				
				for(AplicacionDeNota aplic:aplicaciones){
					NotaDeCredito nota=aplic.getNota();
					if((nota instanceof NotaDeCreditoDescuento)||(nota instanceof NotaDeCreditoBonificacion)){
						Sucursal suc=aplic.getCargo().getSucursal();
						CantidadMonetaria importePorSucursal=map.get(suc);
						
						if(importePorSucursal==null){
							importePorSucursal=aplic.getImporteCM();
							map.put(suc, importePorSucursal);
						}else{
							importePorSucursal=importePorSucursal.add(aplic.getImporteCM());
							map.put(suc, importePorSucursal);
						}
					}
				}
				
				CantidadMonetaria importe=CantidadMonetaria.pesos(0);
				
				for(Map.Entry<Sucursal, CantidadMonetaria> e:map.entrySet()){
					AsientoContable as=new AsientoContable();
					Sucursal s=e.getKey();
					String clave=String.valueOf(s.getClaveContable()); 
					as.setCuenta("406-0002-"+StringUtils.leftPad(clave, 3,'0'));
					as.setConcepto("Descuentos sobre ventas");
					as.setDebe(MonedasUtils.calcularImporteDelTotal(e.getValue()));
					as.setAgrupador("D");
					poliza.getRegistros().add(as);
					importe=importe.add(as.getDebe());
				}
				
				snAplDesc.setCuenta("404-0002-000");
				snAplDesc.setAgrupador("D");
				snAplDesc.setConcepto("DesctoYBonif/VentasXIdentificar");
				snAplDesc.setDescripcion3("Disponible de nota");
				poliza.agregarAsiento(snAplDesc);
				importe=importe.add(snAplDesc.getDebe());
				
				AsientoContable as=new AsientoContable();
				as.setAgrupador("D");
				as.setCuenta("206-0002-003");
				as.setConcepto("Iva Descuentos");
				as.setDebe(MonedasUtils.calcularImpuesto(importe));
				poliza.getRegistros().add(as);
				
				return null;
			}
			
		});
	}
	
	private void agregarDevolucionesSobreVentas(final Poliza poliza){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from AplicacionDeNota ap " +
						" where ap.abono.fecha=? " +
						" and ap.abono.origen=\'CRE\' " +
						" order by ap.abono.cliente desc";
				List<AplicacionDeNota> aplicaciones=session.createQuery(hql).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				
				Map<Sucursal, CantidadMonetaria> map=new HashMap<Sucursal, CantidadMonetaria>();
				
				for(AplicacionDeNota aplic:aplicaciones){
					NotaDeCredito nota=aplic.getNota();
					if((nota instanceof NotaDeCreditoDevolucion)){
						Sucursal suc=aplic.getCargo().getSucursal();
						CantidadMonetaria importePorSucursal=map.get(suc);
						
						if(importePorSucursal==null){
							importePorSucursal=aplic.getImporteCM();
							map.put(suc, importePorSucursal);
						}else{
							importePorSucursal=importePorSucursal.add(aplic.getImporteCM());
							map.put(suc, importePorSucursal);
						}
					}
				}
				CantidadMonetaria importe=CantidadMonetaria.pesos(0);
				
				for(Map.Entry<Sucursal, CantidadMonetaria> e:map.entrySet()){
					AsientoContable as=new AsientoContable();
					as.setAgrupador("V");
					Sucursal s=e.getKey();
					String clave=String.valueOf(s.getClaveContable()); 
					as.setCuenta("405-0002-"+StringUtils.leftPad(clave, 3,'0'));
					as.setConcepto("Devoluciones sobre ventas");
					as.setDebe(MonedasUtils.calcularImporteDelTotal(e.getValue()));
					importe=importe.add(as.getDebe());
					poliza.getRegistros().add(as);
				}
				snAplDevo.setCuenta("404-0001-000");
				snAplDevo.setAgrupador("D");
				snAplDevo.setConcepto("Devoluciones/VentasXIdentificar");
				snAplDevo.setDescripcion3("Disponible de nota (Devo)");
				poliza.agregarAsiento(snAplDevo);
				importe=importe.add(snAplDevo.getDebe());
				
				AsientoContable as=new AsientoContable();
				as.setAgrupador("V");
				as.setCuenta("206-0002-002");
				as.setConcepto("Iva Devoluciones");
				as.setDebe(MonedasUtils.calcularImpuesto(importe));
				poliza.getRegistros().add(as);
				
				return null;
			}
			
		});
	}
	
	private void agregarSaldosAFavorDeNotas(final Poliza poliza){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {				
				String hql2="from AplicacionDeNota ap where ap.fecha=? order by ap.abono.cliente desc";
				List<AplicacionDeNota> aplicaciones=session.createQuery(hql2).setParameter(0, poliza.getFecha(),Hibernate.DATE).list();
				for(AplicacionDeNota aplic:aplicaciones){					
					NotaDeCredito nota=aplic.getNota();
					Date fechaAbono=DateUtil.truncate(nota.getFecha(),Calendar.DATE);
					Date inicio=DateUtil.toDate("21/05/2009");
					if(fechaAbono.before(inicio))
						continue;
					String dta="CTA";
					if(!StringUtils.isBlank(nota.getCliente().getCuentaContable()))
						dta=StringUtils.substring(nota.getCliente().getCuentaContable(), 4, 8);					
					if(!DateUtils.isSameDay(nota.getFecha(), aplic.getFecha())){
						
						AsientoContable cargoANotas=new AsientoContable();
						String clave=String.valueOf(aplic.getCargo().getSucursal().getClaveContable()); 
						
						cargoANotas.setCuenta("406-0002-"+StringUtils.leftPad(clave, 3,'0'));
						if(aplic.getNota() instanceof NotaDeCreditoDevolucion)
							cargoANotas.setCuenta("405-0002-"+StringUtils.leftPad(clave, 3,'0'));
						cargoANotas.setConcepto("Pago con SF nota:"+dta);
						cargoANotas.setDescripcion(nota.getInfo()+"F:"+df.format(nota.getFecha()));
						cargoANotas.setDescripcion2(nota.getCliente().getNombreRazon());
						cargoANotas.setDescripcion3(String.valueOf(aplic.getCargo().getDocumento()));
						cargoANotas.setHaber(aplic.getImporteCM());
						cargoANotas.setAgrupador("N");
						poliza.getRegistros().add(cargoANotas);
						
						AsientoContable abonoAcredores=new AsientoContable();
						
						abonoAcredores.setCuenta("404-0002-000");
						if(aplic.getNota() instanceof NotaDeCreditoDevolucion)
							abonoAcredores.setCuenta("404-0001-000");
						abonoAcredores.setConcepto("Pago con SF nota:"+dta);
						abonoAcredores.setDescripcion(nota.getInfo()+"F:"+df.format(nota.getFecha()));
						abonoAcredores.setDescripcion2(nota.getCliente().getNombreRazon());
						abonoAcredores.setDescripcion3(String.valueOf(aplic.getCargo().getDocumento()));
						abonoAcredores.setDebe(aplic.getImporteCM());
						abonoAcredores.setAgrupador("N");
						poliza.getRegistros().add(abonoAcredores);
						
					}					
				}				
				return null;
			}			
		});
	}
	
	public static void main(String[] args) {
		PolizaCobranzaCreditoModel model=new PolizaCobranzaCreditoModel();
		model.generarPoliza(DateUtil.toDate("28/05/2009"));
	}

}
