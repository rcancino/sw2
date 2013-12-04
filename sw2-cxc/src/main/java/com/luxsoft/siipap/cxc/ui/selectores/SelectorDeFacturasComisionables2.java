package com.luxsoft.siipap.cxc.ui.selectores;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.jdesktop.swingx.calendar.DateUtils;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.Cobrador;
import com.luxsoft.siipap.ventas.model.ComisionVenta;
import com.luxsoft.siipap.ventas.model.Vendedor;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Selector de facturas para un cliente, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeFacturasComisionables2 extends AbstractSelector<ComisionVenta>{
	
	protected Periodo periodo=Periodo.periodoDeloquevaDelMes();
	
	
	private SelectorDeFacturasComisionables2() {
		super(ComisionVenta.class, "Ventas comisionables");
		
	}
	
	@Override
	protected TableFormat<ComisionVenta> getTableFormat() {
		
		String props[]={
				"origen"
				,"clave"
				,"nombre"
				,"venta.sucursal.nombre"
				,"venta.fecha"
				,"vencimiento"
				,"fechaDelPago"
				,"venta.origen"				
				,"venta.documento"
				,"venta.numeroFiscal"
				,"venta.total"
				,"venta.saldoCalculado"
				,"atraso"
				,"vendedorNombre"
				,"comisionVendedor"
				,"cobradorNombre"
				,"comisionCobrador"
				};
		
		String labels[]={
				"Origen"
				,"Cliente"
				,"Nobmre"
				,"Sucursal"
				,"Fecha"
				,"Vto"
				,"FechaP"
				,"Tipo"
				,"Docto"
				,"Fiscal"
				,"Total"
				,"Saldo"
				,"Atraso"
				,"Vendedor"
				,"Com (Ven)"
				,"Cobrador"
				,"Com (Cob)"
				};
		
		return GlazedLists.tableFormat(ComisionVenta.class,props,labels);
	}
	
	
	
	@Override
	protected TextFilterator<ComisionVenta> getBasicTextFilter() {
		TextFilterator<ComisionVenta> filterator=GlazedLists.textFilterator("documento","fiscal","cobradorNombre","vendedorNombre","nombre");
		return filterator;
	}



	private HeaderPanel header;
	
	protected JComponent buildHeader(){
		String title="Facturas comisionables";
		header=new HeaderPanel(title,periodo.toString());
		return header;
	}
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(650,400));
	}
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createViewAction(this, "cambiarPeriodo");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar facturas en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	private ActionLabel periodoLabel;
	
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){			
			periodoLabel=new ActionLabel("Periodo: "+periodo.toString());
			periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	public void cambiarPeriodo(){
		ValueHolder holder=new ValueHolder(periodo);
		AbstractDialog dialog=Binder.createPeriodoSelector(holder);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=(Periodo)holder.getValue();			
			load();
			updatePeriodoLabel();
		}
	}
	
	public boolean loadOnOpen=false;
	
	@Override
	protected void onWindowOpened() {
		if(loadOnOpen)
			load();
	}

	protected void updatePeriodoLabel(){
		header.setDescription("Periodo: "+periodo.toString());
	}

	@Override
	protected List<ComisionVenta> getData() {
		
		//Generar beans ComisionVenta para ventas de credito
		String sql="SELECT " +
				"V.CARGO_ID" +
				",MAX(A.FECHA) AS FECHAPAG" +
				",(SELECT SUM(A.IMPORTE) FROM sx_cxc_aplicaciones A JOIN sx_cxc_abonos B ON(A.ABONO_ID=B.ABONO_ID) JOIN sx_ventas X ON(X.CARGO_ID=A.CARGO_ID) WHERE X.CARGO_ID=V.CARGO_ID AND A.FECHA <=? AND B.TIPO_ID IN(\'PAGO_DEP\', \'PAGO_CHE\',\'PAGO_TAR\',\'PAGO_EFE\') AND  A.CAR_ORIGEN=\'CRE\') AS PAGO_COMISIONABLE " +
				" FROM sx_cxc_aplicaciones A	" +
				" JOIN sx_cxc_abonos B ON(A.ABONO_ID=B.ABONO_ID)		" +
				" JOIN sx_ventas V ON(V.CARGO_ID=A.CARGO_ID)		" +
				" WHERE A.FECHA BETWEEN ? AND ? " +
				"   AND A.TIPO=\'PAGO\' AND B.TIPO_ID<>\'PAGO_DIF\'  " +
				"   AND A.CAR_ORIGEN=\'CRE\' AND V.TIPO=\'FAC\'" +
				"   AND V.CARGO_ID NOT IN(SELECT X.CARGO_ID FROM SX_COMISIONES X)" +
				"   AND V.CARGO_ID NOT IN(SELECT X.CARGO_ID FROM SX_juridico X)" +
				"	GROUP BY V.CARGO_ID,V.TOTAL  " +
				" HAVING V.TOTAL-(SELECT SUM(A.IMPORTE) FROM sx_cxc_aplicaciones A JOIN sx_ventas X ON(X.CARGO_ID=A.CARGO_ID) " +
				"			WHERE X.CARGO_ID=V.CARGO_ID  AND A.CAR_ORIGEN='CRE') <=5";
		System.out.println(sql);
		SqlParameterValue p1=new SqlParameterValue(Types.DATE,periodo.getFechaFinal());		
		SqlParameterValue p2=new SqlParameterValue(Types.DATE,periodo.getFechaInicial());
		SqlParameterValue p3=new SqlParameterValue(Types.DATE,periodo.getFechaFinal());
		
		final List<Map<String,Object>> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,new Object[]{p1,p2,p3});
		final List<ComisionVenta> res=new ArrayList<ComisionVenta>();
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				for(Map<String, Object> row:rows){
					//System.out.println(row);
					try {
						String id=(String)row.get("CARGO_ID");						
						Date pago=(Date)row.get("FECHAPAG");
						BigDecimal importe=(BigDecimal)row.get("PAGO_COMISIONABLE");
						if(importe==null)
							continue;
						
						Venta v=(Venta)session.get(Venta.class, id);
						if(v.getCobrador()==null){
							Cobrador c=v.getCliente().getCobrador();
							if(c==null)
								c=(Cobrador)session.get(Cobrador.class, new Long(1));
							v.setCobrador(c);
						}
						if( (v.getVendedor()==null) || (!v.getVendedor().isActivo())){
							Vendedor vend=v.getVendedor();
							if(vend==null)
								vend=(Vendedor)session.get(Vendedor.class, new Long(1));
							v.setVendedor(vend);
						}
						
						if( (v.getCobrador()!=null) && (v.getCobrador().getId()==1L)){
							if( (v.getVendedor()!=null) && (v.getVendedor().getId()==1L))
								continue;
						}
						if(v.getCliente().getCuotaMensualComision()!=null && v.getCliente().getCuotaMensualComision().doubleValue()>0)
							continue;
						ComisionVenta c=new ComisionVenta(v);
						c.setFechaInicial(periodo.getFechaInicial());
						c.setFechaFinal(periodo.getFechaFinal());
						c.setFechaDelPago(pago);
						c.setPagoComisionable(importe);
						c.actualizarComisiones();
						res.add(c);
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e);
					}
				}
				return null;
			}
		});
		
		//Generar beans ComisionVenta para ventas de contado
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery("from Venta v where v.fecha between ? and ? and origen in(\'MOS\',\'CAM\')" +
						" and v.vendedor is not null" +
						" and v.vendedor.id!=1" +
						" and v.id not in(select x.venta.id from ComisionVenta x)" +
						" and v.id not in(select j.cargo.id from Juridico j)")
				.setParameter(0,periodo.getFechaInicial(),Hibernate.DATE)
				.setParameter(1,periodo.getFechaFinal(),Hibernate.DATE)
				.scroll();
				while(rs.next()){
					Venta v=(Venta)rs.get()[0];
					if(v.getVendedor()!=null)
						if(!v.getVendedor().isActivo())
							continue;
					if(v.getCliente().getCuotaMensualComision()!=null && v.getCliente().getCuotaMensualComision().doubleValue()>0)
						continue;
					ComisionVenta c=new ComisionVenta(v);
					c.setFechaInicial(periodo.getFechaInicial());
					c.setFechaFinal(periodo.getFechaFinal());
					c.setFechaDelPago(v.getFecha());
					BigDecimal importe=v.getTotal().subtract(v.getDevoluciones()).subtract(v.getBonificaciones());
					c.setPagoComisionable(importe);
					c.actualizarComisiones();
					res.add(c);
				}
				return null;
			}
		});
		
		//Actualizar su comision en funcion del atraso
		for(ComisionVenta c:res){
			
			//Se calcula el atraso en funcion del pago
			Date fp=c.getFechaDelPago();
			Date vto=c.getVencimiento();
			int dias=0;
			int valid=vto.compareTo(fp);
			if(valid<=0){
				dias=DateUtils.getDaysDiff(fp.getTime(), vto.getTime());
			}else{				
				dias=0;
			}
			c.setAtraso(dias);
			
			//Se evalua el atraso para recalcular el importe de la comision
			double comision=.5;//c.getComisionVendedor();
			
			if(c.getAtraso()>=30 && c.getAtraso()<45)
				comision=.4;
			else if(c.getAtraso()>=45 && c.getAtraso()<60){
				comision=.3;
			}else if(c.getAtraso()>=60 && c.getAtraso()<75){
				comision=.2;
			}else if(c.getAtraso()>=75 && c.getAtraso()<90){
				comision=.1;
			}else if(c.getAtraso()>=90){
				comision=.0;
			}
			c.setComisionVendedor(comision);
			c.actualizarComisiones();
			
			if(c.getVenta().getClave().equals("U050008")){
				c.setComisionCobrador(.05);
				BigDecimal pago=c.getPagoComisionable();
				c.setImpComisionCob(
						pago.multiply(BigDecimal.valueOf(.05/100)));
			}
		}

		return res;
	}
	
	public void clean(){
		source.clear();
	}
	
	public void open(Periodo p){
		periodo=p;
		loadOnOpen=true;
		open();
	}
	
	public static List<ComisionVenta> buscarVentas(){
		SelectorDeFacturasComisionables2 selector=new SelectorDeFacturasComisionables2();
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<ComisionVenta> ventas=new ArrayList<ComisionVenta>();
			ventas.addAll(selector.getSelectedList());
			selector.clean();
			return ventas;
		}		
		return new ArrayList<ComisionVenta>(0);
		
	}
	
	public static List<ComisionVenta> buscarVentas(Periodo p){
		SelectorDeFacturasComisionables2 selector=new SelectorDeFacturasComisionables2();
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open(p);
		if(!selector.hasBeenCanceled()){
			List<ComisionVenta> ventas=new ArrayList<ComisionVenta>();
			ventas.addAll(selector.getSelectedList());
			selector.clean();
			return ventas;
		}		
		return new ArrayList<ComisionVenta>(0);
		
	}
	
	
	/**
	public static buscarVenta(final Cliente c){
		
	}
	**/

	public static void main(String[] args) {
		
		/*Date vto=DateUtil.toDate("11/03/2009");
		Date pago=DateUtil.toDate("15/05/2009");
		int dias=DateUtils.getDaysDiff(pago.getTime(), vto.getTime());
		System.out.println("Dias: "+dias);*/
		
		
		
		
		
	
		
		SWExtUIManager.setup();
		SwingUtilities.invokeLater(new Runnable(){
			 
			public void run() {
				//buscarVentas();
				buscarVentas(new Periodo("01/08/2013","31/08/2013"));
				System.exit(0);
			}
			
		});
		
	}

}
