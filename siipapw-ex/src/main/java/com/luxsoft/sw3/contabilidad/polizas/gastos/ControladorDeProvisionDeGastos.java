package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxp.dao.FacturaDao;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.TipoDeCompra;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.Poliza.Tipo;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

/**
 * Controlador para el mantenimiento de polizas de anticipo de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeProvisionDeGastos extends ControladorDinamico{
	
	
	public  ControladorDeProvisionDeGastos() {
		setClase("PROVISION_GASTOS");
	}
	
	
	@Override
	public Poliza generar(final Date fecha1, String referencia) {
		final Periodo per=Periodo.getPeriodoDelMesActual(fecha1);
		final Date fecha=per.getFechaFinal();
		final Poliza p=new Poliza();
		p.setTipo(Tipo.DIARIO);
		p.setFecha(fecha1);
		p.setClase("PROVISION_GASTOS");
		p.setReferencia(referencia);
		p.setDescripcion(MessageFormat.format("Provision de facturas", DateUtil.convertDateToString(fecha)));
		final String asiento="PROVISION GASTOS";
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				final List<GFacturaPorCompra> facs=session.createQuery(
						"from GFacturaPorCompra fac where fac.fecha between ? and ? and compra.tipo in('NORMAL','REEMBOLSO','SEGURO') ")
						.setParameter(0, per.getFechaInicial(),Hibernate.DATE)
						.setParameter(1, per.getFechaFinal(),Hibernate.DATE)
						.list();
				ListIterator<GFacturaPorCompra> iter=facs.listIterator();
			
				
				
				
				while(iter.hasNext()){
					
					GFacturaPorCompra ff=iter.next();	
					
					BigDecimal totalFactura=ff.getTotatMN().amount();
					BigDecimal totalGasto=BigDecimal.ZERO;
					Date fechaPago=null;
					if(!ff.getRequisiciones().isEmpty()){
						RequisicionDe req=ff.getRequisiciones().iterator().next();
						if(req.getRequisicion().getPago()!= null){
							System.out.println("requisicion_  "  +  req.getRequisicion().getId());							
							fechaPago=req.getRequisicion().getPago().getFecha();		
						}					
					}
					
					if( (fechaPago==null || fechaPago.compareTo(fecha)>0) ){
						BigDecimal saldo=ff.getSaldoCalculadoAlCorte(fecha).amount();						
						if(saldo.doubleValue()>0){
							GCompra compra=ff.getCompra();
							String pattern="FAC:{0} {1,date,short}";			
							String desc2=MessageFormat.format(pattern, ff.getDocumento(),ff.getFecha());
							String ref1=ff.getProveedor();
							String ref2=ff.getCompra().getSucursal().getNombre();
							
							BigDecimal iva=BigDecimal.ZERO;
							BigDecimal retencion1=BigDecimal.ZERO;
							BigDecimal retencion2=BigDecimal.ZERO;
							
							final EventList<GCompraDet> eventList=GlazedLists.eventList(ff.getCompra().getPartidas());
							final Comparator<GCompraDet> c1=GlazedLists.beanPropertyComparator(GCompraDet.class, "rubro.id");
							final Comparator<GCompraDet> c2=GlazedLists.beanPropertyComparator(GCompraDet.class, "sucursal.clave");
							Comparator<GCompraDet>[] comps=new Comparator[]{c1,c2};
							final GroupingList groupList=new GroupingList(eventList,GlazedLists.chainComparators(Arrays.asList(comps)));
							
							for(int index=0;index<groupList.size();index++){
								List<GCompraDet> gastos=groupList.get(index);
								GCompraDet gasto=gastos.get(0);
								ConceptoDeGasto rubro=gasto.getRubro();
								String concepto="";
								String cuenta="";
								if(rubro!=null){
									System.out.println("--------------" +rubro.getId());
									rubro=rubro.getRubroSegundoNivel(rubro);
									cuenta=StringUtils.substring(rubro.getCuentaContable(),0,3);
									concepto= rubro.getId().toString();
								}
								
								BigDecimal  importePorRubro=BigDecimal.ZERO;
								
								for(GCompraDet g:gastos){
									importePorRubro=importePorRubro.add(g.getImporteBrutoMN());
									
									retencion1=retencion1.add(g.getRetencion1MN().amount());
									retencion2=retencion2.add(g.getRetencion2MN().amount());
									totalGasto=totalGasto.add(g.getTotalMN().amount());									
									iva=iva.add(g.getImpuestoMN().amount());
									iva=iva.subtract(g.getRetencion1MN().amount());
									
									
									
								}
								
								//Cargo al gasto
								PolizaDet cargoGasto=PolizaDetFactory.generarPolizaDet(p, cuenta,concepto , true, importePorRubro, desc2, ref1
										, gasto.getSucursal().getNombre()
										, asiento
										);
								if(cargoGasto.getConcepto()==null &&(rubro!=null)){
									ConceptoContable cc=PolizaDetFactory.generarConceptoContable(concepto, rubro.getDescripcion(), cuenta);
									cargoGasto.setConcepto(cc);
								}
								
								
							}
							
							
							/*
							for(GCompraDet gasto:compra.getPartidas()){
								
								
								ConceptoDeGasto rubro=gasto.getRubro();
								String concepto="";
								String cuenta="";
								if(rubro!=null){	
									rubro=rubro.getRubroSegundoNivel(rubro);
									cuenta=StringUtils.substring(rubro.getCuentaContable(),0,3);
									concepto= rubro.getId().toString();
								}
								
								//Cargo al gasto
								PolizaDet cargoGasto=PolizaDetFactory.generarPolizaDet(p, cuenta,concepto , true, gasto.getImporteBrutoMN(), desc2, ref1
										, gasto.getSucursal().getNombre()
										, asiento
										);
								if(cargoGasto.getConcepto()==null &&(rubro!=null)){
									ConceptoContable cc=PolizaDetFactory.generarConceptoContable(concepto, rubro.getDescripcion(), cuenta);
									cargoGasto.setConcepto(cc);
								}
								
								retencion1=retencion1.add(gasto.getRetencion1MN().amount());
								retencion2=retencion2.add(gasto.getRetencion2MN().amount());
								totalGasto=totalGasto.add(gasto.getTotalMN().amount());
								
								iva=iva.add(gasto.getImpuestoMN().amount());
								iva=iva.subtract(gasto.getRetencion1MN().amount());
							}
							*/
							// Abono a provision
							
							if(ff.getCompra().getProveedor().getId()==245346L ){  //PAPER IMPORTS
								PolizaDetFactory.generarPolizaDet(p, "200", "P095", false, saldo, desc2, ref1, ref2, asiento);
							}else if(ff.getCompra().getProveedor().getId()==753345L){ //IMPAP
								PolizaDetFactory.generarPolizaDet(p, "200", "I001", false, saldo, desc2, ref1, ref2, asiento);
							}else{
								
								if(ff.getCompra().getTipo().equals(TipoDeCompra.SEGURO))
									PolizaDetFactory.generarPolizaDet(p, "203", "DIVR04", false,saldo, desc2, ref1, ref2, asiento);
								else
									PolizaDetFactory.generarPolizaDet(p, "212", "PRVG03", false,saldo, desc2, ref1, ref2, asiento);				
						
							}
							// Cargo al iva por acreditar
							PolizaDetFactory.generarPolizaDet(p, "117","IVAG02", true, iva, desc2, ref1, ref2, asiento);
							// Cargo a Retencio pendiente por acreditar
							PolizaDetFactory.generarPolizaDet(p, "117","IVAR02", true, retencion1, desc2, ref1, ref2, asiento);
							PolizaDetFactory.generarPolizaDet(p, "205","IMPR03", false, retencion1, desc2, ref1, ref2, asiento);
							// Abono a Retencion pendiente por pagar
							PolizaDetFactory.generarPolizaDet(p, "205","IMPR04", false, retencion2, desc2, ref1, ref2, asiento);
							//PolizaDetFactory.generarPolizaDet(p, cuenta,concepto , true, saldo, desc2, ref1, ref2, asiento);
							
							BigDecimal diferencia=totalFactura.subtract(totalGasto);
							if(diferencia.doubleValue()>0){
								PolizaDetFactory.generarPolizaDet(p, "702", "OING01", true, diferencia.abs(), desc2,ref1, ref2, asiento);
							}else if(diferencia.doubleValue()<0){
								PolizaDetFactory.generarPolizaDet(p, "704", "OGST01", false, diferencia.abs(), desc2, ref1, ref2, asiento);
							}
						}
					}				
					
				}
				
				
				
				p.actualizar();
				return p;
			}
		});
		return p;
	}
	
	
	
	public static void main(String[] args) {
		ControladorDeProvisionDeGastos c=new ControladorDeProvisionDeGastos();
		c.generar(DateUtil.toDate("29/02/2012"));
		
	}

		
}


