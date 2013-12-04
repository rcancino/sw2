package com.luxsoft.siipap.dao.gastos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GCompraRow;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;

@SuppressWarnings("unchecked")
public class GCompraDaoImpl extends GenericDaoHibernate<GCompra, Long> implements GCompraDao{

	public GCompraDaoImpl() {
		super(GCompra.class);
	}

	@Override
	public GCompra get(Long id) {
		List list=getHibernateTemplate().find("from GCompra c left join fetch c.partidas where c.id=?", id);
		return list.isEmpty()?null:(GCompra)list.get(0);
	}
	
	@Override
	@Transactional (propagation=Propagation.REQUIRED)
	public GCompra save(GCompra c) {
		//getHibernateTemplate().update(c);
		c.setMes(Periodo.obtenerMes(c.getFecha())+1);
		c.setYear(Periodo.obtenerYear(c.getFecha()));
		c.actualizarTotal();		
		return super.save(c);
	}
	
	public List<GCompra> buscarPorProveedor(final GProveedor p){
		return getHibernateTemplate().find(
				"from GCompra g where g.proveedor=?", p);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<GCompra> buscarPendientesDeRequisicion(final GProveedor p){
		return getHibernateTemplate().find(
				"from GCompra g where g.proveedor=? and g.requisicionDet is null", p);
	}
	
	public List<GCompraRow> buscarComprasRow(){
		return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery("from GCompra c" +
						" left join fetch c.partidas")
						.scroll();
				//final List<GCompraRow> rows=new ArrayList<GCompraRow>();
				final List<GCompraRow> rows=new UniqueList<GCompraRow>(new BasicEventList<GCompraRow>(),GlazedLists.beanPropertyComparator(GCompraRow.class, "id"));
				while(rs.next()){
					GCompra compra=(GCompra)rs.get()[0];
					GCompraRow row=new GCompraRow(compra);					
					rows.add(row);
				}
				return rows;
			}
			
		});
	}
	
	

	public List<GCompraRow> buscarComprasRow(final Periodo p) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery("from GCompra c" +
						" left join fetch c.partidas" +
						" where c.fecha between ? and ?")
						.setDate(0, p.getFechaInicial())
						.setDate(1, p.getFechaFinal())
						.scroll();
				//final List<GCompraRow> rows=new ArrayList<GCompraRow>();
				final List<GCompraRow> rows=new UniqueList<GCompraRow>(new BasicEventList<GCompraRow>(),GlazedLists.beanPropertyComparator(GCompraRow.class, "id"));
				//if(rs.getRowNumber()==0)
					//return rows;
				while(rs.next()){
					GCompra compra=(GCompra)rs.get()[0];
					GCompraRow row=new GCompraRow(compra);					
					rows.add(row);
				}
				return rows;
			}
			
		});
	}

	public Map<ConceptoDeGasto,BigDecimal> acumularGastos(final String rubroClave,final int year,final int mes,final int sucursal) {
		final String hql="from GCompraDet det " +
				" left join fetch det.compra com" +
				" left join fetch det.rubro r " +
				" where det.sucursal.clave=?" +
				" and com.year=?" +
				" and com.mes=? "
				;
		return (Map<ConceptoDeGasto, BigDecimal>) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				List<GCompraDet> list=session.createQuery(hql)
				.setInteger(0, sucursal)
				.setInteger(1, year)
				.setInteger(2, mes)
				.list();
				final Map<ConceptoDeGasto, BigDecimal> totales=new HashMap<ConceptoDeGasto, BigDecimal>();
				int buff=0;
				for(GCompraDet det:list){
					ConceptoDeGasto rubro=findRubro(det.getRubro(),rubroClave);
					BigDecimal total=totales.get(rubro);
					if(total==null){
						totales.put(rubro, det.getImporte());
						continue;
					}else{
						total=total.add(det.getImporte());
						totales.put(rubro, total);
					}
					if(buff++%20==0){
						session.flush();
						session.clear();
					}						
				}
				
				for(Map.Entry<ConceptoDeGasto,BigDecimal> entry:totales.entrySet()){
					System.out.println(entry.getKey()+" - "+"CuentaContable: "+entry.getKey().getCuentaContable()+"  :"+entry.getValue());
				}
				
				return totales;
			}			
		});		
	}
	
	private ConceptoDeGasto findRubro(final ConceptoDeGasto rubro,String claveMayor){		
		if(rubro.getParent()==null){
			return rubro;
		}
		if(rubro.getParent().getClave().equals(claveMayor)){
			return rubro;
		}else
			return findRubro(rubro.getParent(),claveMayor);
	}
	
	
	
	public GFacturaPorCompra buscarFactura(final Long id){
		List l= getHibernateTemplate().find("" +
				"from GFacturaPorCompra f " +
				" left join fetch f.compra com" +
				" left join fetch com.proveedor prov" +
				//" left join fetch f.requisiciondet det" +
				//" left join fetch det.requisicion req" +
				" where f.id=?",id);
		return l.isEmpty()?null:(GFacturaPorCompra)l.get(0);
	}

	
	public List<GFacturaPorCompra> buscarFacturas() {
		return getHibernateTemplate().find("" +
				"from GFacturaPorCompra f " +
				" left join fetch f.compra com" +
				" left join fetch com.proveedor prov" +
				//" left join fetch f.requisiciondet det" +
				//" left join fetch det.requisicion req" +
				"");
	}
	
	
	
	public List<GFacturaPorCompra> buscarFacturas(Periodo p) {
		return getHibernateTemplate().find("" +
				"from GFacturaPorCompra f " +
				" left join fetch f.compra com" +
				" left join fetch com.proveedor prov" +
				" where f.fecha between ? and ?" +
				"",new Object[]{p.getFechaInicial(),p.getFechaFinal()});
	}

	public GFacturaPorCompra salvarFactura(final GFacturaPorCompra fac){
		return (GFacturaPorCompra)getHibernateTemplate().merge(fac);
	}

	public List<GFacturaPorCompra> buscarFacturas(final Date fecha){		
		return getHibernateTemplate().executeFind(new HibernateCallback(){			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {				
				List<GFacturaPorCompra> facs=session.createQuery(
						"from GFacturaPorCompra fac where fac.fecha=?")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
				return facs;
			}			
		});		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.dao.gastos.GCompraDao#buscarFacturasConstaldo(java.util.Date)
	 */
	public List<GFacturaPorCompra> buscarFacturasConstaldo(final Periodo p){
		return getHibernateTemplate().executeFind(new HibernateCallback(){			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {				
				List<GFacturaPorCompra> facs=session.createQuery(
						"from GFacturaPorCompra fac where fac.fecha between ? and ?")
						.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
						.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
						.list();
				ListIterator<GFacturaPorCompra> iter=facs.listIterator();
				while(iter.hasNext()){
					GFacturaPorCompra ff=iter.next();					
					double saldo=ff.getSaldoCalculadoAlCorte(p.getFechaFinal()).amount().doubleValue();
					if(saldo<=0)
						iter.remove();
				}
				return facs;
			}			
		});
	}
	
	public List<GFacturaPorCompra> buscarFacturasFechaContable(final Date fecha){
		return getHibernateTemplate().executeFind(new HibernateCallback(){			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {				
				List<GFacturaPorCompra> facs=session.createQuery(
						"from GFacturaPorCompra fac where fac.fechaContable=?")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
				return facs;
			}			
		});
	}

	public static void main(String[] args) {
		List<GFacturaPorCompra> facs=ServiceLocator2.getGCompraDao().buscarFacturas(DateUtil.toDate("02/06/2008"));
		for(GFacturaPorCompra f:facs){
			String pattern="Fac:{0} Importe:{1} Impuesto:{2} Total:{3} Fecha:{4}";
			System.out.println("ANTES:"+MessageFormat.format(pattern, f.getDocumento(),f.getImporte(),f.getImpuesto(),f.getTotatMN(),f.getFecha()));
			System.out.println("DESPUES"+MessageFormat.format(pattern, f.getDocumento(),f.getCompra().getImporte()
					,f.getCompra().getImpuesto()
					,f.getTotatMN(),f.getFecha()));
		}
	}

	
	
}
