package com.luxsoft.siipap.cxc.ui.consultas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.rules.CXCUtils;
import com.luxsoft.siipap.cxc.rules.RecepcionDeDocumentosRules;
import com.luxsoft.siipap.cxc.rules.RevisionDeCargosRules;
import com.luxsoft.siipap.cxc.ui.form.RecepcionDeDocumentosForm;
import com.luxsoft.siipap.cxc.ui.form.RegistrarRevisionForm;
import com.luxsoft.siipap.cxc.ui.form.RevisionDeCargosForm;
import com.luxsoft.siipap.cxc.ui.model.CuentasPorCobrarModel;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;

/**
 * Panel para el mantenimiento de las cuentas por cobrar
 * 
 * @author Ruben Cancino
 *
 */
public class CXCRevisionPanel extends FilteredBrowserPanel<Cargo> implements PropertyChangeListener{
		
	
	private final CuentasPorCobrarModel model;
	
	private final DateFormat df=new SimpleDateFormat("dd/MM/yyyy");

	public CXCRevisionPanel(final CuentasPorCobrarModel model) {
		super(Cargo.class);
		this.model=model;
		this.model.addPropertyChangeListener(this);
	}

	@Override
	protected EventList getSourceEventList() {
		return model.getCuentasPorCobrar();
	}


	protected void init(){		
		addProperty("tipoDocto","clave","nombre","cobrador.id","sucursal.nombre","precioBruto","documento","numeroFiscal","fecha","revisada","revision","recibidaCXC","fechaRecepcionCXC","fechaRevisionCxc","vencimiento","reprogramarPago","saldo","atraso","atrasoReal","comentarioRepPago","comentario2");
		addLabels("Tipo","Cliente","Nombre","Cob","Suc","P.Bruto","Dcto","Fiscal","Fecha","Revisada","Revisión","Recibida Cargo","F.Rec Cargo","Fecha RevCxc","Vto","Rep.Pago","Saldo","Atraso","At Real","Com Rev","Com Rec");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("N.Fiscal", "numeroFiscal");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Cobrador", "cobrador.id");
		
		TextFilterator<Cargo> fechaFilterator=new TextFilterator<Cargo>(){
			public void getFilterStrings(List<String> baseList, Cargo element) {
				baseList.add(df.format(element.getFecha()));
			}			
		};
		JTextField fechaField=new JTextField(12);
		installTextComponentMatcherEditor("Fecha ", fechaFilterator,fechaField);
		
		TextFilterator<Cargo> fechaRevision=new TextFilterator<Cargo>(){
			public void getFilterStrings(List<String> baseList, Cargo element) {
				if(element.getFechaRevisionCxc()!=null)
					baseList.add(df.format(element.getFechaRevisionCxc()));
				if(element.getReprogramarPago()!=null)
					baseList.add(df.format(element.getReprogramarPago()));
			}			
		};
		
		installTextComponentMatcherEditor("Rev / Cobro ", fechaRevision,new JTextField(10));
		
		CheckBoxMatcher<Cargo> recibidaMatcher=new CheckBoxMatcher<Cargo>(){			
			protected Matcher<Cargo> getSelectMatcher(Object... obj) {				
				return new Matcher<Cargo>(){
					public boolean matches(Cargo item) {						
						boolean res=item.isRecibidaCXC();
						return !res;
					}					
				};
			}			
		};
		installCustomMatcherEditor("Por recibir CxC", recibidaMatcher.getBox(), recibidaMatcher);
		
		CheckBoxMatcher<Cargo> porRevisarMatcher=new CheckBoxMatcher<Cargo>(){			
			protected Matcher<Cargo> getSelectMatcher(Object... obj) {				
				return new Matcher<Cargo>(){
					public boolean matches(Cargo item) {						
						boolean res=item.isRevisada();
						return !res;
					}					
				};
			}			
		};
		installCustomMatcherEditor("Por revisar", porRevisarMatcher.getBox(), porRevisarMatcher);
	}
	
	protected void adjustMainGrid(final JXTable grid){
		grid.getColumnExt("Com Rev").setVisible(false);
		grid.getColumnExt("Com Rec").setVisible(false);
		if(source.size()>0)
			grid.packAll();
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{				
				addAction(CXCActions.AdministracionRevisionCobro.getId(), "revision", "Mantenimiento")
				,addAction(CXCActions.RecepcionDeCuentasPorCobrar.getId(), "recepcion", "Recepción")
				,addAction(CXCActions.CancelarRecepcionDeCuentasPorCobrar.getId(), "cancelarRecepcion", "Cancelar Recepción")				
				,addAction(CXCActions.AdministracionRevisionCobro.getId(), "revisada", "Marcar Revisión")				
				,addAction(CXCActions.AdministracionRevisionCobro.getId(), "cancelarRevisada", "Cancelar Revisión")
				,addContextAction(new SelectionPredicate(),CXCActions.RecalcularRevisionCobro.getId(), "actualizarRevision", "Actualizar revisión" )
				};
		return actions;
	}
	
	public void actualizarRevision(){
		ExecutionSelectionTemplate<Cargo> template=new ExecutionSelectionTemplate<Cargo>(){
			public List<Cargo> execute(List<Cargo> selected) {
				if(selected!=null)
					RevisionDeCargosRules.instance().actualizar(selected);
				return selected;
			}			
		};
		execute(template);
	}
	
	public void revision(){
		ExecutionSelectionTemplate<Cargo> template=new ExecutionSelectionTemplate<Cargo>(){
			public List<Cargo> execute(List<Cargo> selected) {
				//RevisionDeCargosRules.instance().validate(selected);				
				selected=RevisionDeCargosForm.showForm(selected);
				if(selected!=null)
					RevisionDeCargosRules.instance().actualizar(selected);
				return selected;
			}			
		};
		execute(template);
	}
	
	/**
	 * Procesa la recepcion de documentos seleccionados
	 *  (Recepcion de documentos en el departamento de cuentas por cobrar)
	 *  
	 */
	public void recepcion(){
		if(!getSelected().isEmpty()){
			boolean res1=RecepcionDeDocumentosRules.instance().validarSinRecibirCXC(getSelected());
			if(!res1){				
				JOptionPane.showMessageDialog(getControl()
						, "La seleccion no corresponde facturas sin recibir en Cuentas por Cobrar"
						,"Mantenimiento de Cuentas por pagar",JOptionPane.WARNING_MESSAGE);
				return;
			}else{
				List res=RecepcionDeDocumentosForm.showForm(getSelected());
				if(res!=null){
					persistir(res);
				}
			}
		}
	}
	
	public void cancelarRecepcion(){
		if(!getSelected().isEmpty()){
			String pattern="Cancelar la recepcion de {0} documentos?";
			int res=JOptionPane.showConfirmDialog(getControl(), MessageFormat.format(pattern, getSelected().size()),"Cancelar recepción",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE );			
			if(res==JOptionPane.OK_OPTION){	
				RecepcionDeDocumentosRules.instance().cancelarRecepcion(getSelected());
				persistir(getSelected());
			}
		}
	}
	
	/**
	 * Registra la revision de uno o mas documentos
	 * 
	 */
	public void revisada(){
		ExecutionSelectionTemplate<Cargo> template=new ExecutionSelectionTemplate<Cargo>(){
			public List<Cargo> execute(List<Cargo> selected) {
				RevisionDeCargosRules.instance().validarRecibidasCxC(selected);
				RegistrarRevisionForm.showForm(selected);
				RevisionDeCargosRules.instance().marcarRevision(selected, true);
				return selected;
			}		
		};
		execute(template);
	}
	
	/**
	 * Cancela la revision de uno o mas documentos
	 * 
	 */
	public void cancelarRevisada(){
		ExecutionSelectionTemplate<Cargo> template=new ExecutionSelectionTemplate<Cargo>(){
			public List<Cargo> execute(List<Cargo> selected) {
				RevisionDeCargosRules.instance().marcarRevision(selected, false);
				return RegistrarRevisionForm.showForm(selected);
			}		
		};
		execute(template);
	}	
	
	@Override
	protected void doExecute(ExecutionSelectionTemplate<Cargo> template) {
		List<Cargo> res=CXCUtils.obtenerCopia(getSelected());
		template.execute(res);
		persistir(res);
	}

	/**
	 * Persiste una lista de cuentas, sincronizandola con la lista fuente del browser
	 * 
	 * PUNTO CENTRAL PARA PERSISTIR TODOS LOS CAMBIOS EN ESTA PANEL
	 *  
	 * @param res
	 */
	protected void persistir(List<Cargo> res){
		if(!CollectionUtils.isEmpty(res)){
			logger.info("Cuentas modificadas: "+res);
			res=ServiceLocator2.getCXCManager().salvar(res);
			res=CXCUtils.obtenerCopia(res);
			CXCUtils.sincronizar(res, source);
		}		
	}
	
	public void propertyChange(PropertyChangeEvent evt) {					
		if(evt.getPropertyName().equals(CuentasPorCobrarModel.CUENTAS_CARGADAS)){
			if(grid!=null)
				grid.packAll();
		}		
	}
	
	public void close(){
	}
	
	public class SelectionPredicate implements Predicate{

		public boolean evaluate(Object bean) {
			Cargo c=(Cargo)bean;
			return (c!=null && (!c.isRevisada()));
		}
		
	}
}
