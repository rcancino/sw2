package com.luxsoft.siipap.cxc.ui.selectores;

import java.util.List;

import org.springframework.instrument.classloading.glassfish.GlassFishLoadTimeWeaver;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model2.VentaContraEntrega;

public class SelectorDeDevoluciones extends AbstractSelector<DevolucionRow>{
	
	private OrigenDeOperacion origen;
	
	public SelectorDeDevoluciones() {
		super(DevolucionRow.class, "Devoluciones (Pendientes de Nota)");
	}	

	@Override
	protected List<DevolucionRow> getData() {
		/*String hql3="from Devolucion d where " +
		" d.fecha>?" +
		" and d  not in(select nd.devolucion from NotaDeCreditoDevolucion nd)" +
		//" and d.venta =\'"+"8a8a8189-250313fb-0125-0315d9d9-0191"+"\'"+
		//" and d.id =\'"+"8a8a81ef-29a81521-0129-a8e5231b-0006"+"\'";
		" and d.venta.origen=\'"+getOrigen().name()+"\'";		
		return ServiceLocator2.getHibernateTemplate().find(hql3,DateUtil.toDate("28/02/2010"));*/
		
		String sql3="SELECT D.DEVO_ID,D.numero,S.nombre as sucursal,v.nombre as cliente,d.fecha,v.docto as documento,v.origen,d.total " +
				" FROM sx_devoluciones D USE INDEX(FECHA) JOIN sx_ventas V ON (D.VENTA_ID=V.CARGO_ID)"+  
		" LEFT JOIN sx_cxc_abonos A ON(A.DEVOLUCION_ID=D.DEVO_ID) JOIN SW_SUCURSALES S ON (S.SUCURSAL_ID=V.SUCURSAL_ID)"+
		" WHERE D.FECHA>='2011/01/01' AND V.ORIGEN=\'"+getOrigen().name()+"\' AND A.DEVOLUCION_ID IS NULL and D.comentario not like '%CANCELADO%'" ;
		
		//return  ServiceLocator2.getJdbcTemplate().queryForList(sql3);
		return ServiceLocator2.getJdbcTemplate().query(sql3, new BeanPropertyRowMapper(DevolucionRow.class));
		
	}

	@Override
	protected TableFormat<DevolucionRow> getTableFormat() {
		String props[]={"numero","sucursal","cliente","fecha","documento","origen","total"};
		String labels[]={"Folio","Sucursal","Cliente","Fecha","Factura","Origen","Importe"};
		return GlazedLists.tableFormat(DevolucionRow.class,props,labels);
		
	}
	
	
	
	
	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}
	
	public static Devolucion seleccionar(OrigenDeOperacion origen){
		SelectorDeDevoluciones selector=new SelectorDeDevoluciones();
		
		selector.setOrigen(origen);
		selector.open();
		if(!selector.hasBeenCanceled()){
			
			  DevolucionRow selected=selector.getSelected();
			  Devolucion dev=(Devolucion)ServiceLocator2.getHibernateTemplate()
			  			.get(Devolucion.class,selected.getDevo_id());
			 
			return dev;
		}
		return null;
	}

	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				System.out.println(seleccionar(OrigenDeOperacion.CAM));
				System.exit(0);
			}

		});
	}

}
