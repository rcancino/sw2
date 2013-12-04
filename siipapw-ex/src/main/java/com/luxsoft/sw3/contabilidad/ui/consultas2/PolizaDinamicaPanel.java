package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.services.PolizasManager;
import com.luxsoft.sw3.contabilidad.ui.consultas.PanelGenericoDePolizasMultiples;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaForm;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaFormModel;

public class PolizaDinamicaPanel extends PanelGenericoDePolizasMultiples{
	
	protected final ControladorDinamico controller;

	public PolizaDinamicaPanel(ControladorDinamico controller) {
		this.controller=controller;
		setClase(controller.getClase());
	}
	
	@Override
	protected void agregarMasterProperties() {
		addProperty("folio","tipo","descripcion","fecha","referencia","debe","haber","cuadre","clase","id");
		addLabels("Folio","Tipo","Descripción","Fecha","Referencia","Debe","Haber","Cuadre","Clase","Id");
		installTextComponentMatcherEditor("Descripción", "descripcion");
		installTextComponentMatcherEditor("Referencia", "referencia");
		installTextComponentMatcherEditor("Tipo", "tipo");
		manejarPeriodo();
	}
	

	@Override
	public List<Poliza> generarPolizas(Date fecha) {
		return controller.generar(fecha);
	}
	
	@Override
	public Poliza salvar(Poliza poliza){
		return controller.salvar(poliza);
	}
	
	@Override
	protected void manejarPeriodo() {
		periodo=Periodo.getPeriodoDelMesActual(new Date());
	}
	
	@Override
	public void open() {
		load();
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addAction(null,"generar","Generar poliza")
				,addAction(null, "salvar", "Salvar póliza")
				//,addAction(null, "cuadrar", "Otros (Ingreso/Gasto)")
				,getEditAction()
				,getDeleteAction()
				
				,CommandUtils.createPrintAction(this, "imprimirPoliza")
												};
		return actions;
	}
	
	public void delete(){
		Poliza selected=(Poliza)getSelectedObject();
		if(selected!=null){
			
			getHibernateTemplate().delete(selected);
			load();
		}
	}
	
	public void generar(){
		Date fecha=SelectorDeFecha.seleccionar();
		if(fecha!=null){
			try {
				List<Poliza> res=controller.generar(fecha);
				insertarPolizas(res);
			} catch (Exception e) {				
				logger.error(e);
				e.printStackTrace();
				MessageUtils.showMessage("Error: "+ExceptionUtils.getRootCauseMessage(e), "Generación de poliza");
			}
			
		}
	}
	
	protected void insertarPolizas(List<Poliza> polizas){
		if(source.isEmpty())
			source.addAll(polizas);
		else{
			for(final Poliza p:polizas){
				int index=source.indexOf(p);
				if(index==-1){
					source.add(p);
				}
			}
		}
		grid.packAll();
	}
	
	protected void afterGridCreated(){
		JPopupMenu pop=new JPopupMenu();
		Action recargaParcial=addAction(null, "recargarParcial", "Recargar parcial");
		recargaParcial.setEnabled(false);
		pop.add(recargaParcial);
		pop.add(addAction(null, "recargarTotal", "Recargar total"));
		pop.add(addAction(null, "limpiar", "Limpar consulta"));
		grid.setComponentPopupMenu(pop);
		grid.getColumnExt("Id").setVisible(false);
		grid.getColumnExt("Clase").setVisible(false);
		
	}
	public void recargarParcial(){
		Poliza selected=(Poliza)getSelectedObject();
		if(selected!=null)
			controller.recargar(selected, false);
	}
	
	public void recargarTotal(){
		Poliza selected=(Poliza)getSelectedObject();
		if(selected!=null)
			controller.recargar(selected, true);
	}
	
	public void limpiar(){
		source.clear();
	}
	
	public void cuadrar(){
		Poliza selected=(Poliza)getSelectedObject();
		if(selected!=null && selected.getCuadre().doubleValue()!=0){
			if(MessageUtils.showConfirmationMessage("Cuadrar póliza por: "+selected.getCuadre(), "Cuadre de póliza")){
				int index=source.indexOf(selected);
				if(index!=-1){
					Poliza res=controller.cuadrar(selected);
					if(res!=null){
						source.set(index, res);
						clearSelection();
					}					
					//setSelected(res);
				}
				
			}
			
		}
			
	}
	
	@Override
	protected Poliza doEdit(Poliza bean) {
		
		final Poliza source=getPolizasManager().getPolizaDao().get(bean.getId());		
		final PolizaFormModel model=new PolizaFormModel(source);		
		final PolizaForm form=new PolizaForm(model);			
		form.open();
		if(!form.hasBeenCanceled()){
			Poliza res=model.getPoliza();
			return getPolizasManager().salvarPoliza(res);
		}
		return source;
	}
	
	private PolizasManager getPolizasManager(){
		return ServiceLocator2.getPolizasManager();
	}
	
	protected HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}

}
