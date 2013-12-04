package com.luxsoft.siipap.gastos.operaciones;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.gastos.GasActions;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.RequisicionesUtils;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;

public class FacturasView extends FilteredBrowserPanel<GFacturaPorCompra>{
	
	private Action generarRequisicion;
	private Action aplicarAnticipo;
	public FechaMayorAMatcher fechaInicialSelector;
	public FechaMenorAMatcher fechaFinalSelector;
	EstadoSaldoMatcherEditor edoMatcherEditor;

	@SuppressWarnings("unchecked")
	public FacturasView() {
		super(GFacturaPorCompra.class);		

		addProperty("id","compra.id","compra.proveedor.nombreRazon","compra.fecha","compra.total","documento","totalMN.amount","fecha","requisicionesIds","saldoCalculado.amount","pagado.amount","requisitado.amount","porRequisitar.amount");
		addLabels("id","Compra","Proveedor",",Fecha C","Total C","Factura","Total F","Fecha F","Requisiciones","Saldo","Pagado","Requisitado","Por Requisitar");

		installTextComponentMatcherEditor("Factura", "documento");
		installTextComponentMatcherEditor("Proveedor", "compra.proveedor.nombreRazon");
		//installTextComponentMatcherEditor("Estado", "requisiciondet.requisicion.estado");
		fechaInicialSelector=new FechaMayorAMatcher();
		fechaFinalSelector=new FechaMenorAMatcher();
		installCustomMatcherEditor("F.Inicial", fechaInicialSelector.getFechaField(), fechaInicialSelector);
		installCustomMatcherEditor("F.Final", fechaFinalSelector.getFechaField(), fechaFinalSelector);
		edoMatcherEditor=new EstadoSaldoMatcherEditor();
		installCustomMatcherEditor("Estado", edoMatcherEditor.getSelector(), edoMatcherEditor);
		
	}
	
	private void initActions(){
		generarRequisicion=new AbstractAction("generarRequisicion"){
			public void actionPerformed(ActionEvent e) {
				generarRequisicion();
			}			
		};
		CommandUtils.configAction(generarRequisicion, GasActions.GenerarRequisiciones.getId(), null);
		aplicarAnticipo=new AbstractAction("aplicarAnticipo"){
			public void actionPerformed(ActionEvent e) {
				aplicarAnticipo();
			}			
		};
		CommandUtils.configAction(aplicarAnticipo, GasActions.AplicarAnticipoDePago.getId(), null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Action[] getActions() {
		if(actions==null)
			initActions();
			actions=new Action[]{
				getLoadAction(),
				//getInsertAction()
				getDeleteAction()
				,generarRequisicion
				,aplicarAnticipo
				,getEditAction()
				//,getViewAction()
				};
		return actions;
	}
	
	
	
	
	@Override
	protected List<GFacturaPorCompra> findData() {
		if(periodo==null)
			manejarPeriodo();
		return ServiceLocator2.getGCompraDao().buscarFacturas(periodo);
	}
	
	
	@Override
	public boolean doDelete(GFacturaPorCompra bean) {
		ServiceLocator2.getUniversalDao().remove(GFacturaPorCompra.class, bean.getId());
		return true;
	}

	@Override
	protected void doSelect(Object bean) {
		final RecepcionDeFacturasFormModel model=new RecepcionDeFacturasFormModel(bean,true);
		final RecepcionDeFacturasForm form=new RecepcionDeFacturasForm(model);
		form.open();
		form.dispose();
	}
	
	

	@Override
	protected GFacturaPorCompra doEdit(GFacturaPorCompra bean) {
		if(bean.isActualizable()){
			final RecepcionDeFacturasFormModel model=new RecepcionDeFacturasFormModel(bean,false);
			final RecepcionDeFacturasForm form=new RecepcionDeFacturasForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				try {
					return ServiceLocator2.getComprasDeGastosManager().salvarFactura(bean);
				} catch (Exception e) {
					e.printStackTrace();
					MessageUtils.showMessage("Error: "+e.getMessage(), "Facturas");
					return null;
				}
			}
		}else{
			MessageUtils.showMessage("La factura no es actualizable, tiene pagos aplicados ", "Facturas");
			
		}
		return null;
	}

	/**
	 * Genera una requisicion en funcion de las 
	 *
	 */
	@SuppressWarnings("unchecked")
	private void generarRequisicion(){
		
		if(!selectionModel.isSelectionEmpty()){
			
			final List<GFacturaPorCompra> facturas=new ArrayList<GFacturaPorCompra>();
			for(Object  o:getSelected()){
				GFacturaPorCompra fac=(GFacturaPorCompra)o;
				//if(fac.getRequisiciondet()==null)
				if(fac.puedeRequisitar())
					facturas.add(fac);
			}
			if(facturas.isEmpty()){
				MessageUtils.showMessage("La selección realizada no contiene facturas por requisitar","Requisición");
				return;
			}
			 
			Requisicion req=RequisicionesUtils.generarRequisicion(facturas);
			logger.debug("Req  Imp:"+req.getImporte());
			logger.debug("Req  Tax:"+req.getImpuesto());
			logger.debug("Req  Tot:"+req.getTotal());
			RequisicionAutomaticaModel model=new RequisicionAutomaticaModel(req);
			RequisicionAutomaticaForm form=new RequisicionAutomaticaForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				ServiceLocator2.getRequisiciionesManager().save(req);
				for(GFacturaPorCompra fac:facturas){
					int index=source.indexOf(fac);
					fac=ServiceLocator2.getGCompraDao().buscarFactura(fac.getId());
					if(fac!=null)
						source.set(index, fac);
				}
			}else{
				for(GFacturaPorCompra fac:facturas){
					fac.eliminarRequisiciones();
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void aplicarAnticipo(){
		if(!selectionModel.isSelectionEmpty()){
			final GFacturaPorCompra fac=(GFacturaPorCompra)getSelectedObject();
			try {
				//Localizar posibles anticipos y vincularlos
				if(fac.getSaldoCalculado().amount().doubleValue()<=0){
					boolean confirm=MessageUtils.showConfirmationMessage("La factura ya esta pagada, un abono mas generará un saldo negatico" +
							"\n¿Desea continuar?", "Aplicación de anticipo");
					if(!confirm)
						return;
					
				}
				final SelectorDeAnticipos selector=new SelectorDeAnticipos(){
					protected List<RequisicionDe> getData() {
						return ServiceLocator2.getRequisiciionesManager().buscarAnticiposPendientes(fac.getCompra().getProveedor());
					}
				};
				selector.open();
				if(!selector.hasBeenCanceled()){
					RequisicionDe res=selector.getSelected();					
					fac.agregarRequisicion(res);
					res.setFacturaDeGasto(fac);
					GFacturaPorCompra resultado=ServiceLocator2.getComprasDeGastosManager().salvarFactura(fac);
					int index=source.indexOf(fac);
					source.set(index, resultado);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	
	public class EstadoSaldoMatcherEditor extends AbstractMatcherEditor<GFacturaPorCompra> implements ActionListener{
		
		private JComboBox selector;
		
		public EstadoSaldoMatcherEditor(){
			String[] vals={"Todas","Con Saldo","Sin Saldo"};
			selector=new JComboBox(vals);
			selector.addActionListener(this);
		}
		
		public JComboBox getSelector(){
			return selector;
		}
		
		
	

		public void actionPerformed(ActionEvent e) {
			String val=selector.getSelectedItem().toString();
			if("Con Saldo".equalsIgnoreCase(val)){
				fireChanged(new Matcher<GFacturaPorCompra>(){
					public boolean matches(GFacturaPorCompra item) {
						return item.getSaldoCalculado().amount().doubleValue()!=0;
					}
					
				});
						
			}else if("Sin Saldo".equalsIgnoreCase(val)){
				fireChanged(new Matcher<GFacturaPorCompra>(){
					public boolean matches(GFacturaPorCompra item) {
						return item.getSaldoCalculado().amount().doubleValue()==0;
					}
					
				});
			}
			else
				fireMatchAll();
				
			
		}
		
		class AtendidasMatcher implements Matcher<GFacturaPorCompra> {
			public boolean matches(GFacturaPorCompra item) {								
				return !StringUtils.isBlank(item.getCompra().getFactura());
			}
		};
		
		
				
		
	}


	public EstadoSaldoMatcherEditor getEdoMatcherEditor() {
		return edoMatcherEditor;
	}
	
	private abstract class SelectorDeAnticipos extends AbstractSelector<RequisicionDe>{		

		public SelectorDeAnticipos() {
			super(RequisicionDe.class, "Selector de anticipos pendientes");
		}

		@Override
		protected TableFormat<RequisicionDe> getTableFormat() {
			String[] props={"id","requisicion.fecha","requisicion.concepto.descripcion","total"};
			String[] names={"Id","Fecha","Concepto","Total"};
			return GlazedLists.tableFormat(RequisicionDe.class, props,names);
		}
		
	}

}
