package com.luxsoft.sw3.cxp.consultas;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingWorker;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.service.FacturaManager;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Panel para la administracion de las facturas (Cuentas por pagar)
 * 
 * @author Ruben Cancino
 *
 */
public class FacturaDeComprasPanel extends AbstractMasterDatailFilteredBrowserPanel<CXPFactura, CXPAplicacion>{
	
	
	public FacturaDeComprasPanel() {
		super(CXPFactura.class);
	}

	@Override
	protected void agregarMasterProperties(){
		addProperty(
				"id"
				,"tipoDeFactura"
				,"fecha"
				,"clave"
				,"nombre"
				,"documento"
				,"fecha"
				,"vencimiento"
				,"moneda"
				,"tc"
				,"total"
				,"totalAnalizadoConFlete"
				,"bonificado"
				,"importeDescuentoFinanciero"
				,"pagos"
				,"diferencia"
				,"saldoCalculado"
				//,"saldoReal"
				,"requisitado"
				,"autorizacion.fechaAutorizacion"
				,"comentario"
				);
		addLabels(
				"CXP_ID"
				,"Tipo"
				,"Fecha"
				,"Prov"
				,"Nombre"
				,"Docto"
				,"F Docto"
				,"Vto"
				,"Mon"
				,"TC"
				,"Facturado"
				,"Analizado"
				,"Bonificado"
				,"D.F."
				,"Pagos"
				,"Dif"
				,"Saldo"
				//,"Por Requisitar"
				,"Requisitado"
				,"Autorizado"
				,"Comentario"
				);
		installTextComponentMatcherEditor("Proveedor", "nombre");
		installTextComponentMatcherEditor("Factura", "documento");
		installTextComponentMatcherEditor("Analisis", "id");
		manejarPeriodo();		
	}
	
	protected void manejarPeriodo(){
		this.periodo=Periodo.getPeriodoDelMesActual();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"fecha","abono.id","cargo.id","cargo.documento","cargo.total","importe"};
		String[] names={"Fecha","Abono","Cargo","Docto","Total","Aplicado"};
		return GlazedLists.tableFormat(CXPAplicacion.class,props,names);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				//,getSecuredInsertAction("insertarFactura")
				,getDeleteAction()
				,addAction(null, "registrarDiferencia", "Registrar diferencia")
				//,getEditAction()
				//,getViewAction()
				};
		return actions;
	}

	@Override
	protected Model<CXPFactura, CXPAplicacion> createPartidasModel() {
		Model<CXPFactura, CXPAplicacion> model=new Model<CXPFactura,CXPAplicacion>(){
			public List<CXPAplicacion> getChildren(CXPFactura parent) {
				try {
					String hql="from CXPAplicacion c where c.cargo.id=?";
					return ServiceLocator2.getHibernateTemplate()
						.find(hql,parent.getId());
				} catch (Exception e) {
					System.out.println(ExceptionUtils.getRootCauseMessage(e));
					return new ArrayList<CXPAplicacion>(0);
				}
			}
		};
		return model;
	}
	
	@Override
	protected List<CXPFactura> findData() {
		return getManager().buscarFacturas(this.periodo);
	}

	@Override
	public boolean doDelete(CXPFactura bean) {
		getManager().remove(bean.getId());
		return true;
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	private FacturaManager getManager(){
		return CXPServiceLocator.getInstance().getFacturasManager();
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {
		//grid.getColumn("TC").setCellRenderer(Renderers.buildDefaultNumberRenderer());
	}
	
	public void registrarDiferencia(){
		CXPFactura fac=(CXPFactura)getSelectedObject();
		if(fac!=null){
			int index=source.indexOf(fac);
			if(index!=-1){
				String pattern="Saldar diferencia del documento: {0}  prov: {1} Saldo: {2}";
				String msg=MessageFormat.format(pattern, fac.getDocumento(),fac.getNombre(),fac.getSaldoCalculado());
				if(MessageUtils.showConfirmationMessage(msg, "Saldar Factura")){
					fac=getManager().registrarDiferencia(fac);
					source.set(index, fac);
				}
			}
		}
	}

}
