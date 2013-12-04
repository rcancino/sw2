package com.luxsoft.sw3.cxp.consultas;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxp.model.AnticipoDeCompra;
import com.luxsoft.siipap.cxp.model.CXPAbono;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.service.AnticipoDeComprasManager;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.service.FacturaManager;
import com.luxsoft.siipap.cxp.ui.CXPServices;
import com.luxsoft.siipap.cxp.ui.selectores.SelectorDeFacturasPorRequisitar;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.cxp.forms.AnticipoDeComprasForm;
import com.luxsoft.sw3.cxp.forms.AnticipoDeComprasFormModel;

/**
 * Panel para el control y administración de los anticipos
 * 
 * @author Ruben Cancino
 *
 */
public class AnticiposDeComprasPanel extends AbstractMasterDatailFilteredBrowserPanel<AnticipoDeCompra, CXPFactura>{
	
	
	public AnticiposDeComprasPanel() {
		super(AnticipoDeCompra.class);
	}

	@Override
	protected void agregarMasterProperties(){
		addProperty(
				"id"
				,"proveedor.nombre"
				,"fecha"
				,"factura.importe"
				,"aplicado"				
				,"disponible"
				,"diferencia"
				,"factura.documento"
				,"comentario"
				,"descuentoNota.importe"
				,"nota.importe"
				);
		addLabels(
				"Folio"
				,"Proveedor"
				,"Fecha"
				,"Importe"
				,"Aplicado"				
				,"Disponible"
				,"Diferencia"
				,"Factura"
				,"Comentario"
				,"Desc C"
				,"D.F"
				);
		installTextComponentMatcherEditor("Proveedor", "proveedor.nombre","proveedor.clave");
		installTextComponentMatcherEditor("Factura", "factura.documento");
		manejarPeriodo();		
	}
	
	protected void manejarPeriodo(){
		this.periodo=Periodo.periodoDeloquevaDelYear();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"id"
				//,"tipoDeFactura"
				,"fecha"
				,"clave"
				,"nombre"
				,"documento"
				,"importe"
				,"analizadoCosto"
				,"saldoCalculado"
				,"comentario"};
		String[] names={
				"Id"
				//,"tipoDeFactura"
				,"Fecha"
				,"Prov"
				,"Nombre"
				,"Docto"
				,"Importe"
				,"Imp Analizado"
				,"Saldo"
				,"comentario"};
		return GlazedLists.tableFormat(CXPFactura.class,props,names);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				//,getEditAction()
				,getViewAction()
				,CommandUtils.createPrintAction(this, "imprimir")
				,addAction(null,"imprimirInfo","Imprimir Resumen")
				,addAction(null,"asignarFactura","Asignar factura")
				,addAction(null,"removerFactura","Remover factura")
				,addAction(null,"editarDescuentoComercial","Editar N.C. C")
				,addAction(null,"editarDescuentoDf","Editar N.C. DF")
				//,addAction(null,"cancelarNota","cancelarAbonos")
				,addAction(null,"registrarDiferencia","Registrar Diferencia")
				
				};
		return actions;
	}

	@Override
	protected Model<AnticipoDeCompra, CXPFactura> createPartidasModel() {
		Model<AnticipoDeCompra, CXPFactura> model=new Model<AnticipoDeCompra,CXPFactura>(){
			public List<CXPFactura> getChildren(AnticipoDeCompra parent) {
				try {
					String hql="from CXPFactura c where c.anticipo.id=?";
					return ServiceLocator2.getHibernateTemplate()
						.find(hql,parent.getId());
				} catch (Exception e) {
					System.out.println(ExceptionUtils.getRootCauseMessage(e));
					return new ArrayList<CXPFactura>(0);
				}
			}
		};
		return model;
	}
	
	
	public void imprimir(){
		if(getSelectedObject()!=null)
			imprimir((AnticipoDeCompra)getSelectedObject());
	}
	
	
	public void imprimir(AnticipoDeCompra bean){
      //Date corte=SelectorDeFecha.seleccionar();
		Map params=new HashMap();
		params.put("ID", bean.getId());
		params.put("MONEDA", bean.getMoneda().toString());
	  //params.put("TIPO", bean.getTipoId());
      //params.put("CORTE", corte);
		String path=ReportUtils.toReportesPath("cxp/AnticipoCxP.jasper");
		if(!bean.getMoneda().equals(MonedasUtils.PESOS)){
			boolean res=MessageUtils.showConfirmationMessage("En moneda nacional ?", "Detalle de abono");
			if(res)
				params.put("MONEDA", MonedasUtils.PESOS.toString());
		}
		if(ReportUtils.existe(path))
			ReportUtils.viewReport(path, params);
		else
			JOptionPane.showMessageDialog(this.getControl()
					,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	}
	
	
	public void imprimirInfo(){
		if(getSelectedObject()!=null){
			AnticipoDeCompra bean=(AnticipoDeCompra)getSelectedObject();
			Map params=new HashMap();
			params.put("ID", bean.getId());
			params.put("MONEDA", bean.getMoneda().toString());		  
			String path=ReportUtils.toReportesPath("cxp/AnticipoInfoCxP.jasper");
			ReportUtils.viewReport(path, params);
		}
			
	}
	
	
	@Override
	protected List<AnticipoDeCompra> findData() {
		return getManager().buscar(this.periodo);
	}

	@Override
	protected AnticipoDeCompra doInsert() {
		final AnticipoDeComprasFormModel model=new AnticipoDeComprasFormModel();
		final AnticipoDeComprasForm form=new AnticipoDeComprasForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnticipoDeCompra res=model.commit();
			res=getManager().salvar(res);
			getManager().generarNotaDeDesucneotComercial(res);
			getManager().generarNotaDeDescuentoFinanciero(res);
			return getManager().get(res.getId());
		}
		return null;
	}
	/*
	@Override
	protected AnticipoDeCompra doEdit(AnticipoDeCompra bean) {
		AnticipoDeCompra target=getManager().get(bean.getId());
		final AnticipoDeComprasFormModel model=new AnticipoDeComprasFormModel(target);
		final AnticipoDeComprasForm form=new AnticipoDeComprasForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnticipoDeCompra res=model.commit();
			res=getManager().salvar(res);
			return getManager().get(res.getId());
		}
		return bean;
	}
	*/
	
	@Override
	protected void doSelect(Object bean) {
		/*
		AnticipoDeCompra source=(AnticipoDeCompra)bean;
		source=getManager().get(source.getId());
		final AnticipoDeComprasFormModel model=new AnticipoDeComprasFormModel(source);
		model.setReadOnly(true);
		final AnticipoDeComprasForm form=new AnticipoDeComprasForm(model);
		form.open();*/
	
	}

	@Override
	public boolean doDelete(AnticipoDeCompra bean) {
		getManager().eliminar(bean.getId());
		return true;
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	private AnticipoDeComprasManager getManager(){
		return ServiceLocator2.getAnticipoDeComprasManager();
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {
		//grid.getColumn("TC").setCellRenderer(Renderers.buildDefaultNumberRenderer());
	}
	
	
	public void asignarFactura(){
		AnticipoDeCompra anticipo=(AnticipoDeCompra)getSelectedObject();
		if(anticipo!=null){
			if(anticipo.getDisponible().doubleValue()>0){
				List<CXPFactura> facturas=SelectorDeFacturasPorRequisitar.buscarFacturasParaAnticipo(anticipo.getProveedor(), anticipo.getMoneda());
				if(!facturas.isEmpty()){
					int index=source.indexOf(anticipo);
					if(index!=-1){
						for(CXPFactura fac:facturas){
							anticipo=getManager().asignarFacturas(anticipo,fac);
						}
						source.set(index, anticipo);
					}
					
				}				
			}
		}
	}
	
	public void removerFactura(){
		AnticipoDeCompra anticipo=(AnticipoDeCompra)getSelectedObject();
		if(anticipo!=null){
			List<CXPFactura> facturas=detailSelectionModel.getSelected();
			if(!facturas.isEmpty()){
				int index=source.indexOf(anticipo);
				if(index!=-1){
					for(CXPFactura fac:facturas){
						anticipo=getManager().removerFacturas(anticipo,fac);
					}
					source.set(index, anticipo);
				}
				
			}	
		}
	}
	
	public void registrarDiferencia(){
		AnticipoDeCompra anticipo=(AnticipoDeCompra)getSelectedObject();
		if(anticipo!=null){
			if(MessageUtils.showConfirmationMessage("Registrar diferencia por: "+anticipo.getDisponible(), "Anticipo : "+anticipo.getId())){
				int index=source.indexOf(anticipo);
				if(index!=-1){
					anticipo=getManager().registrarDiferencia(anticipo);
					source.set(index, anticipo);
				}
			}	
		}
	}
	
	public void editarDescuentoComercial(){
		AnticipoDeCompra anticipo=(AnticipoDeCompra)getSelectedObject();
		if(anticipo!=null){
			if(anticipo.getNota()!=null){
				int index=source.indexOf(anticipo);
				if(index!=-1){
					CXPServices.editarNota(anticipo.getNota().getId());
					anticipo=getManager().get(anticipo.getId());
					source.set(index, anticipo);
				}
			}
			
		}
	}
	public void editarDescuentoDf(){
		AnticipoDeCompra anticipo=(AnticipoDeCompra)getSelectedObject();
		if(anticipo!=null){
			if(anticipo.getDescuentoNota()!=null){
				int index=source.indexOf(anticipo);
				if(index!=-1){
					CXPServices.editarNota(anticipo.getDescuentoNota().getId());
					anticipo=getManager().get(anticipo.getId());
					source.set(index, anticipo);
				}
			}
			
		}
	}
	
	

}
