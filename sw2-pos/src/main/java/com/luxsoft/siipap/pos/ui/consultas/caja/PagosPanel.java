package com.luxsoft.siipap.pos.ui.consultas.caja;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.springframework.beans.factory.annotation.Autowired;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.forms.caja.FichasFormModel;
import com.luxsoft.siipap.pos.ui.forms.caja.GeneracionDeFichas;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Mantenimiento de pagos en el PUNTO DE VENTA
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PagosPanel extends AbstractMasterDatailFilteredBrowserPanel<Pago, Aplicacion>{
	
	@Autowired
	private CajaController controller;
	
	//private Sucursal sucursal;

	public PagosPanel() {
		super(Pago.class);
		//sucursal=Services.getInstance().getConfiguracion().getSucursal();
		
	}
	
	protected void init(){
		super.init();
		addProperty(
				"info"
				,"origenAplicacion"
				,"nombre"
				//,"sucursal.nombre"
				,"fecha"
				,"total"
				,"diferencia"
				,"disponible"
				,"banco"
				,"depositoInfo"
				,"comentario"
				,"anticipo"
				);
		addLabels(
				"Referencia"
				,"Origen"
				,"Cliente"
				//,"Sucursal"
				,"Fecha"
				,"Total"
				,"Dif O.P."
				,"Disponible"
				,"Banco"
				,"Deposito"
				,"Comentario"
				,"Anticipo"
				);
		//manejarPeriodo();
		//periodo=Periodo.hoy();
		setDetailTitle("Aplicaciones");
		installTextComponentMatcherEditor("Cliente", "cliente.nombre");
		//installTextComponentMatcherEditor("Tipo", "info");
		//installTextComponentMatcherEditor("Banco", "banco");
		installTextComponentMatcherEditor("Total", "total");
		final JTextField tf=new JTextField(10);
		final TextComponentMatcherEditor<Pago> e1=new TextComponentMatcherEditor<Pago>(tf,new TextFilterator<Pago>(){
			public void getFilterStrings(List<String> baseList, Pago element) {
				try {
					baseList.add(element.getInfo());
				} catch (Exception e) {
					System.out.println("Error en : "+element.getId());
				}				
			}			
		});
		installCustomMatcherEditor("Info", tf, e1);
		installTextComponentMatcherEditor("Origen", "origenAplicacion");
		CheckBoxMatcher<Pago> c1=new CheckBoxMatcher<Pago>(){
			protected Matcher<Pago> getSelectMatcher(Object... obj) {
				return new Matcher<Pago>(){
					public boolean matches(Pago item) {
						if(item!=null)
							return item.getDepositoInfo().equalsIgnoreCase("PENDIENTE");
						return true;
					}
				};
			}
		};
		installCustomMatcherEditor("Pendiente por depositar", c1.getBox(), c1);
		
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.hoy();
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "cobrar", "Registrar Abono")
				,addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "cancelarPago", "Cancelar abono")
				,addAction("", "generarReporteDePagos", "Resumen de pagos")
				,addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "generarDepositos", "Depositos (Fichas)")
				,addAction("","print","Imprimir Anticipo")
				};
		return actions;
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"fecha","cargo.origen","cargo.documento","cargo.fecha","cargo.total","importe"};
		String[] names={"Fecha","Origen","Docto","Fecha Docto.","Total Fac","Imp (Aplic)"};
		return GlazedLists.tableFormat(Aplicacion.class, props,names);
	}

	@Override
	protected Model<Pago, Aplicacion> createPartidasModel() {
		return new Model<Pago, Aplicacion>(){
			public List<Aplicacion> getChildren(Pago parent) {
				return buscarAplicaciones(parent);
			}
		};
	}
	
	/**
	 * 
	 */
	public void cobrar(){
		controller.cobrar();
		load();
	}
	
	public void cancelarPago(){
		if(getSelectedObject()!=null){			
			Pago a=(Pago)getSelectedObject();
			if(MessageUtils.showConfirmationMessage("Cancelar  :\n"+a.toString()
					, "Cancelación de documentos")){				
				cancelar(a.getId());				
			}
		}	
	}
	
	public void generarDepositos(){	
		if(getSelected().isEmpty()) return;
		final FichasFormModel model=new FichasFormModel();
		
		model.generarFichas(getSelected());
		
		if(model.getPartidas().isEmpty()){
			MessageUtils.showMessage("No existe pagos depositables pendientes en el periodo"
					, "Registro de depositos");
			return;
		}
		model.setValue("cuenta", Services.getInstance().getConfiguracion().getCuentaPreferencial());
		GeneracionDeFichas form=new GeneracionDeFichas(model);
		form.open();
		if(!form.hasBeenCanceled()){
			model.comit();
			for(Ficha f:model.getFichas()){				
				f=Services.getInstance().getDepositosManager().save(f);				
				actualizarPagos(f);
			}				
		}else{
			model.cancel();
		}
	}
	
	private void actualizarPagos(final Ficha f){
		try {
			List<String> pagos=Services.getInstance().getHibernateTemplate().find(
					"select d.pago.id  from FichaDet d where d.ficha.id=?", f.getId());
			for(String id:pagos)
				refrescarAbono(id);
		} catch (Exception e) {
			logger.error(e);
		}
		
	}
	
	/**
	 * Refresca desde la base de datos el abono indicado
	 * 
	 * @param id
	 */
	public void refrescarAbono(String id){
		try {
			Pago a=(Pago)Services.getInstance().getUniversalDao().get(Pago.class, id);
			for(int index=0;index<source.size();index++){
				Pago other=(Pago)source.get(index);
				if(other.getId().equals(a.getId()))
					source.set(index, a);
			}			
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}		
	}
	
	private List<Aplicacion> buscarAplicaciones(final Pago pago){
		String hql="from Aplicacion  a where a.abono.id=? ";
		return Services.getInstance().getHibernateTemplate().find(hql,pago.getId());
	}
	
	private void cancelar(String id){
		refrescarAbono(id);
	}
	
	public void generarReporteDePagos(){
		MessageUtils.showMessage("Pendiente....", "");
	}

	@Override
	protected List<Pago> findData() {
		Sucursal sucursal=Services.getInstance().getConfiguracion().getSucursal();
		String hql="from Pago p where p.sucursal.id=? and p.fecha = ? and p.origen in(\'MOS\',\'CAM\')";
		Object[] params={sucursal.getId(),new Date()};
		return Services.getInstance().getHibernateTemplate().find(hql,params);
	}
	
	
	public void print(){
		if(getSelectedObject()!=null){
			Pago m=(Pago)getSelectedObject();
			if (m.getAnticipo()!=false && m.getAnticipo()!=null){
			Map params=new HashMap();
			params.put("ABONO_ID", m.getId());
			ReportUtils2.runReport("ventas/AnticipoDeClientes.jasper", params);
			}
			else
				MessageUtils.showMessage("El pago no es un anticipo"
						, "Imprimir anticipo");
		}
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
}
