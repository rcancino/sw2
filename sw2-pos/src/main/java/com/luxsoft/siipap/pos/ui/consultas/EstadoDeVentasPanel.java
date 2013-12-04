package com.luxsoft.siipap.pos.ui.consultas;

import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

import com.luxsoft.sw3.ventas.EstadoDeVenta;

public class EstadoDeVentasPanel extends FilteredBrowserPanel<EstadoDeVenta>{
	
	

	public EstadoDeVentasPanel() {
		super(EstadoDeVenta.class);
	}
	
	protected void init(){
		String[] props=new String[]{
				"venta.origen"
				,"venta.pedido.folio"
				,"venta.documento"
				,"venta.fecha"
				,"venta.clave"
				,"venta.nombre"
				,"venta.total"
				,"venta.saldoCalculado"
				,"venta.facturista"
				,"surtido"
				,"venta.cancelado"
				};
		String[] names=new String[]{
				"Tipo"
				,"Pedido"
				,"Factura"
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Total"
				,"Saldo"
				,"Facturista"
				,"Surtido"
				,"Cancelado"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Origen", "venta.origen");
		installTextComponentMatcherEditor("Documento", "venta.documento");
		installTextComponentMatcherEditor("Cliente", "venta.nombre","venta.clave");
		
		manejarPeriodo();
	}
	
	@Override
	protected void manejarPeriodo() {
		periodo=Periodo.getPeriodo(-2);
	}

	@Override
	protected List<EstadoDeVenta> findData() {
		List res= Services.getInstance().getHibernateTemplate().find(
				"from EstadoDeVenta e " +
				" left join fetch e.venta v " +
				" left join fetch v.pedido p " +
				" where date(v.fecha) between ? and ? " +
				" order by v.log.creado" 
				,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
		return res;
	}
	
	@Override
	protected EventList getFilteredList(EventList list) {
		matcherEditors.add(GlazedLists.fixedMatcherEditor(Matchers.beanPropertyMatcher(EstadoDeVenta.class, "venta.cancelado", false)));
		return super.getFilteredList(list);
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.getColumnExt("Surtido").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		//grid.getColumnExt("Cortado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
	}

	
	
	@Override
	protected void doSelect(Object bean) {
		Venta venta=(Venta)bean;
		FacturaForm.show(venta.getId());
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addAction(null, "registrarCorte", "Registrar corte")
				,addAction(null, "registrarSurtido", "Registrar surtido")
			};
		return actions;
	}
	

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorkerInDialog(worker, "Buscando ventas", "Procesando");
	}
	
	public void registrarSurtido(){
		Object selected=getSelectedObject();
		if(selected!=null){
			int index=source.indexOf(selected);
			EstadoDeVenta e=(EstadoDeVenta)selected;
			if(MessageUtils.showConfirmationMessage("Registrar surtido para la factura/venta:"+e.getVenta().getDocumento(), "Registro de surtido")){
				e.setSurtido(new Date());
				User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
				KernellSecurity.instance().registrarAddressLog(e, "addresLog");
				KernellSecurity.instance().registrarUserLog(user, e, "log");
				e=(EstadoDeVenta)Services.getInstance().getHibernateTemplate().merge(e);
				
				if(index!=-1){
					if(e!=null){
						List res=Services.getInstance().getHibernateTemplate()
						.find("from EstadoDeVenta e " +
								" left join fetch e.venta  v  " +
								" left join fetch v.pedido p " +
								" where  e.id=?", e.getId());
						if(!res.isEmpty()){
							source.set(index, res.get(0));
						}
						
					}
					
				}
				
			}
		}
	}
	
	

}
