package com.luxsoft.siipap.pos.ui.consultas;

import java.util.Date;
import java.util.List;

import javax.swing.Action;

import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.services.Services;

/**
 * Consulta para el monitoreo de entregas
 *  
 * @author Ruben Cancino 
 *
 */
public class EstadoDeEntregasPanel extends FilteredBrowserPanel<Entrega>{

	public EstadoDeEntregasPanel() {
		super(Entrega.class);		
	}
	
	protected void init(){
		String[] props={
				"embarque.transporte.chofer.id"
				,"embarque.documento"
				,"factura.documento"
				,"nombre"	
				,"factura.log.creado"
				,"surtido"
				,"log.creado"
				,"embarque.salida"				
				,"arribo"
				,"recepcion"
				,"embarque.regreso"
				,"retraso"
				,"parcial"
				,"valor"
				,"factura.contraEntrega"
				,"porCobrar"
				,"recibio"
				,"surtidor"
				
				};
		String[] labels={
				"Chofer"
				,"Embarque"
				,"Docto"
				,"Cliente"
				,"Facturado"    // 1
				,"Surtido"		// 2
				,"Asignación"	// 3
				,"Salida"		// 4
				,"Arribo"		// 5
				,"Recepción"	// 6
				,"Retorno"		// 7
				,"Retraso (hrs)"
				,"Parcial"
				,"Valor"
				,"COD"
				,"Por Cobrar"
				,"Recibió"
				,"Surtidor"
				};
		addProperty(props);
		addLabels(labels);
		installTextComponentMatcherEditor("Chofer", "embarque.transporte.chofer.nombre");
		installTextComponentMatcherEditor("Factura", "documento");
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction()
				};
		return actions;
	}

	@Override
	protected List<Entrega> findData() {
		String hql="from Entrega e where e.embarque.regreso is null or e.embarque.regreso>=?";
		Date fecha=DateUtils.addDays(new Date(), -2);
		return Services.getInstance().getHibernateTemplate().find(hql, fecha);
	}

	@Override
	public void open() {
		load();
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {
		
		grid.getColumnExt("Facturado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Surtido").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Salida").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Asignación").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Salida").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Arribo").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));		
		grid.getColumnExt("Recepción").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Retorno").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		
		grid.getColumnExt("Recibió").setVisible(false);
		grid.getColumnExt("Surtidor").setVisible(false);
		
	}
	
	

}
