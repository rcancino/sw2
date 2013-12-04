package com.luxsoft.siipap.cxc.ui.selectores;

import java.awt.Dimension;
import java.sql.SQLException;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Example.PropertySelector;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;


/**
 * Selector de facturas para un cliente, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeCargos extends AbstractSelector<Cargo>{
	
	
	public SelectorDeCargos() {
		super(Cargo.class, "Cargos");
		
	}
	
	@Override
	protected TableFormat<Cargo> getTableFormat() {
		String[] props=new String[]{
				"tipoDocto"
				,"documento"
				,"numeroFiscal"
				,"precioNeto"
				,"fecha"
				,"vencimiento"
				,"reprogramarPago"
				,"atraso"
				,"sucursal.nombre"
				,"clave"
				,"nombre"
				,"total"
				,"devoluciones"
				,"bonificaciones"
				,"descuentos"
				,"descuentoNota"
				,"pagos"
				,"saldoCalculado"
				,"saldo"
				,"origen"
				,"cargosAplicados"
				,"cargosPorAplicar"
				,"cargosImpPorAplicar"};
		String[] names=new String[]{
				"Tipo"
				,"Docto"
				,"N.Fiscal"
				,"PN"
				,"Fecha"
				,"Vto"
				,"Rep. Pago"
				,"Atr"
				,"Suc"
				,"Cliente"
				,"Nombre"
				,"Total"
				,"Devs"
				,"Bonific"
				,"Descuentos"
				,"Desc (Nota)"
				,"Pagos"
				,"Saldo"
				,"Saldo Oracle"
				,"Origen"
				,"Car (Aplic)"
				,"Car(%)"
				,"Car($)"};
		return GlazedLists.tableFormat(Cargo.class,props,names);
	}
	
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(850,500));
	}
	
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createViewAction(this, "buscar");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar facturas en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}	
	
	
	
	@Override
	protected void onWindowOpened() {
		load();
	}

	@Override
	protected List<Cargo> getData() {		
		
		return ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Example ex=Example
				.create(example)
				.ignoreCase()
				.enableLike(MatchMode.ANYWHERE)
				//.excludeZeroes()
				.setPropertySelector(new PropertySelector(){
					public boolean include(Object propertyValue,String propertyName, org.hibernate.type.Type type) {						
						return ArrayUtils.contains(searchProperties, propertyName);
					}
				})
				;
				
				return session.createCriteria(Cargo.class)
				.add(ex)
				.setMaxResults(100)
				.setFetchSize(20)
				.list();
				
			}
			
		});
		
	}
	
	public void clean(){
		source.clear();
	}
	
	private Cargo example;
	
	private String[] searchProperties={"documento"};
	
	
	public Cargo getExample() {
		return example;
	}

	public void setExample(Cargo example) {
		this.example = example;
	}
	

	public String[] getSearchProperties() {
		return searchProperties;
	}

	public void setSearchProperties(String[] searchProperties) {
		this.searchProperties = searchProperties;
	}
	
	public static List<Cargo> buscar(final Cliente c){
		Venta example=new Venta();
		example.setCliente(c);
		SelectorDeCargos selector=new SelectorDeCargos();		
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.setExample(example);
		selector.setSearchProperties(new String[]{"clave"});
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelectedList();
		}		
		return ListUtils.EMPTY_LIST;
	}
	
	public static List<Cargo> buscarEspecial(final Cliente c){
		SelectorDeCargos selector=new SelectorDeCargos(){

			@Override
			protected List<Cargo> getData() {
				String hql="from Cargo v where v.clave=? and YEAR(v.fecha)=2009 " +
						//"and (v.total-v.aplicado<>0)" +
						" and v.id not in(select c.venta.id from NotaDeCargoDet c where cargo=1.0 and YEAR(c.notaDeCargo.fecha)=2010)";
				return ServiceLocator2.getHibernateTemplate().find(hql,c.getClave());
				 
			}
			
		};		
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelectedList();
		}		
		return ListUtils.EMPTY_LIST;
	}

	public static Cargo buscar(final Cargo example){
		SelectorDeCargos selector=new SelectorDeCargos();		
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.setExample(example);
		selector.setSearchProperties(new String[]{"clave"});
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}		
		return null;
	}
	
	public static Cargo buscar(final Cargo example,String...props){
		SelectorDeCargos selector=new SelectorDeCargos();		
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.setExample(example);
		selector.setSearchProperties(props);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}		
		return null;
	}
	
	public static Cargo buscar(final Long docto,final Long sucursalId,final Integer folioFiscal){
		SelectorDeCargos selector=new SelectorDeCargos(){
			protected List<Cargo> getData() {
				String hql="from Cargo c where c.documento=? and c.sucursal.id=?";
				if(folioFiscal!=null && folioFiscal.intValue()>0){
					hql+=" and c.numeroFiscal=?";
					return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{docto,sucursalId,folioFiscal});
				}else{
					return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{docto,sucursalId});
				}
			}
			
			
		};		
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}		
		return null;
	}
	

	public static void main(String[] args) {
		SWExtUIManager.setup();
		SwingUtilities.invokeLater(new Runnable(){			 
			public void run() {
				//Venta example=new Venta();
				//example.setFecha(null);
				//example.setDocumento(13719L);
				buscar(2L,5289L,null);
				System.exit(0);
			}
		});
		
	}
 
}
