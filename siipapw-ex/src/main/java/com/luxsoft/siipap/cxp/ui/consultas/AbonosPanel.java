package com.luxsoft.siipap.cxp.ui.consultas;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxp.model.CXPAbono;
import com.luxsoft.siipap.cxp.model.CXPAnticipo;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.cxp.service.CXPAbonosManager;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.ui.CXPServices;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Panel para el mantenimiento de {@link CXPAbono}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AbonosPanel extends AbstractMasterDatailFilteredBrowserPanel<CXPAbono,CXPAplicacion>{

	public AbonosPanel() {
		super(CXPAbono.class);
	}
	
	@Override
	protected void agregarMasterProperties(){
		manejarPeriodo();
		addProperty("id","tipoId","nombre","documento","info","fecha","moneda","tc","total","diferencia","disponible","comentario");
		addLabels("Id","Tipo","Proveedor","Documento","Info","Fecha","Mon","T.C.","Total","Dif","disponible","Comentario");
		
		JTextField tipoField=new JTextField(10);
		TextFilterator<CXPAbono> tipoFilterator=new TextFilterator<CXPAbono>(){
			public void getFilterStrings(List<String> baseList, CXPAbono element) {
				try {
					String tipo=element.getTipoId();
					//String ifo=element.getInfo();
					baseList.add(tipo);
					//baseList.add(ifo);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		};
		
		JTextField infoField=new JTextField(10);
		TextFilterator<CXPAbono> infoFilterator=new TextFilterator<CXPAbono>(){
			public void getFilterStrings(List<String> baseList, CXPAbono element) {
				try {
					//String tipo=element.getTipoId();
					String ifo=element.getInfo();
					//baseList.add(tipo);
					baseList.add(ifo);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		};
		installTextComponentMatcherEditor("Proveedor", "nombre","clave");
		installTextComponentMatcherEditor("Tipo", tipoFilterator,tipoField);
		installTextComponentMatcherEditor("Info", infoFilterator,infoField);
		installTextComponentMatcherEditor("Documento", "documento");
	}
	
	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"fecha","abono.id","cargo.id","cargo.documento","cargo.total","importe","comentario"};
		String[] names={"Fecha","Abono","Cargo","Docto","Total","Aplicado","Comentario"};
		return GlazedLists.tableFormat(CXPAplicacion.class,props,names);
	}

	@Override
	protected Model<CXPAbono, CXPAplicacion> createPartidasModel() {
		return new CollectionList.Model<CXPAbono, CXPAplicacion>(){
			public List<CXPAplicacion> getChildren(CXPAbono parent) {
				return getManager().buscarAplicaciones(parent);
			}
		};
	}
	
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				//,addAction("aplicarRequisicionEnCXP", "aplicarRequisicion", "Aplicar Requisición")				
				,addAction("registrarNotaDeCreditoEnCXP", "registrarNota", "Registrar Nota C.")
				,getSecuredEditAction("cxp.ModificarNotas")
				,getViewAction()
				,addAction("cancelarAbonoEnCXP", "delete", "Cancelar abono")
				,CommandUtils.createPrintAction(this, "imprimir")
				//,addAction("cancelarNotaEnCXP", "cancelarNota", "Cancelar Nota")
				,addAction(null,"registrarDiferencia","Registrar diferencia")
				};
		return actions;
	}
	
	/*public void aplicarRequisicion(){
		MessageUtils.showMessage(
				"En Construccion! Desde esta opcion se podra seleccionar una requisicón " +
				"\ny generar automaticamente el pago ", "Aplicación de pagos");
	}*/
	
	public void registrarNota(){
		CXPNota nota=CXPServices.generarNota();
		if(nota!=null){
			source.add(nota);
		}
	}

	@Override
	protected CXPAbono doEdit(CXPAbono bean) {		
		if(bean instanceof CXPNota){
			return CXPServices.editarNota(bean.getId());
		}else if(bean instanceof CXPAnticipo){
			return CXPServices.editarAnticipo(bean.getId());
		}
		return null;
	}

	@Override
	public boolean doDelete(CXPAbono bean) {
		if(bean instanceof CXPPago){
			getManager().eliminarPago(bean.getId());
			return true;
		}
		else if(bean instanceof CXPNota){
			getManager().eliminarNota(bean.getId());
			return true;
		}	
		else 
			return false;
	}

	@Override
	protected List<CXPAbono> findData() {
		return getManager().buscarAbonos(periodo);
	}
	
	public void imprimir(){
		if(getSelectedObject()!=null)
			imprimir((CXPAbono)getSelectedObject());
	}
	
	public void imprimir(CXPAbono bean){
		Date corte=SelectorDeFecha.seleccionar();
		Map params=new HashMap();
		params.put("ID", bean.getId());
		params.put("MONEDA", bean.getMoneda().toString());
		params.put("TIPO", bean.getTipoId());
		params.put("CORTE", corte);
		String path=ReportUtils.toReportesPath("cxp/AplicacionDeAbonos.jasper");
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
	
	public void reporteAplicaciones(){
		
	}
	
	private CXPAbonosManager getManager(){
		return CXPServiceLocator.getInstance().getAbonosManager();
	}
	
	public void registrarDiferencia(){
		CXPAbono abono=(CXPAbono)getSelectedObject();
		if(abono!=null){
			int index=source.indexOf(abono);
			if(index!=-1){
				abono=getManager().registrarDiferencia(abono);
				detailSelectionModel.clearSelection();
				source.set(index, abono);
				setSelected(abono);
			}
		}
	}

}
