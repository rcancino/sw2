package com.luxsoft.sw3.cxc.consultas;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.rules.RecepcionDeDocumentosRules;
import com.luxsoft.siipap.cxc.rules.RevisionDeCargosRules;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.cxc.ui.consultas.CargoView;
import com.luxsoft.siipap.cxc.ui.consultas.FacturaForm;
import com.luxsoft.siipap.cxc.ui.form.RecepcionDeDocumentosForm;
import com.luxsoft.siipap.cxc.ui.form.RevisionDeCargosForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Panel para el mantenimiento de revision y cobro de cuentas por cobrar
 * 
 * @author Ruben Cancino
 *
 */
public class RevisionCobroPanel extends FilteredBrowserPanel<CargoRow2> {
		
	
	
	
	private final DateFormat df=new SimpleDateFormat("dd/MM/yyyy");

	public RevisionCobroPanel() {
		super(CargoRow2.class);
		setTitle("Revisión y Cobro");
	}	


	protected void init(){		
		addProperty(
				"tipo"
				,"clave"
				,"nombre"
				,"cobradorId"
				,"sucursalNombre"
				,"documento"
				,"numeroFiscal"
				,"fecha"
				,"revisada"
				,"revision"
				,"recibidaCXC"
				,"fechaRecepcionCXC"
				,"fechaRevisionCxc"
				,"vencimiento"
				,"reprogramarPago"
				,"saldo"
				,"atraso"
				//,"atrasoReal"
				,"comentarioRepPago"
				,"comentario2"
				);
		
		addLabels("Tipo"
				,"Cliente"
				,"Nombre"
				,"Cob"
				,"Suc"				
				,"Dcto"
				,"Fiscal"
				,"Fecha"
				,"Revisada"
				,"Revisión"
				,"Recibida Cargo"
				,"F.Rec Cargo"
				,"Fecha RevCxc"
				,"Vto"
				,"Rep.Pago"
				,"Saldo"
				,"Atraso"
				//,"At Real"
				,"Com Rev"
				,"Com Rec"
				);
		installTextComponentMatcherEditor("Sucursal", "sucursalNombre");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("N.Fiscal", "numeroFiscal");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Cobrador", "cobradorId");
		
		TextFilterator<CargoRow2> fechaFilterator=new TextFilterator<CargoRow2>(){
			public void getFilterStrings(List<String> baseList, CargoRow2 element) {
				baseList.add(df.format(element.getFecha()));
			}			
		};
		JTextField fechaField=new JTextField(12);
		installTextComponentMatcherEditor("Fecha ", fechaFilterator,fechaField);
		
		TextFilterator<CargoRow2> fechaRevision=new TextFilterator<CargoRow2>(){
			public void getFilterStrings(List<String> baseList, CargoRow2 element) {
				if(element.getFechaRevisionCxc()!=null)
					baseList.add(df.format(element.getFechaRevisionCxc()));
				if(element.getReprogramarPago()!=null)
					baseList.add(df.format(element.getReprogramarPago()));
			}			
		};
		
		installTextComponentMatcherEditor("Rev / Cobro ", fechaRevision,new JTextField(10));
		
		CheckBoxMatcher<CargoRow2> recibidaMatcher=new CheckBoxMatcher<CargoRow2>(){			
			protected Matcher<CargoRow2> getSelectMatcher(Object... obj) {				
				return new Matcher<CargoRow2>(){
					public boolean matches(CargoRow2 item) {						
						boolean res=item.isRecibidaCXC();
						return !res;
					}					
				};
			}			
		};
		installCustomMatcherEditor("Por recibir CxC", recibidaMatcher.getBox(), recibidaMatcher);
		
		CheckBoxMatcher<CargoRow2> porRevisarMatcher=new CheckBoxMatcher<CargoRow2>(){			
			protected Matcher<CargoRow2> getSelectMatcher(Object... obj) {				
				return new Matcher<CargoRow2>(){
					public boolean matches(CargoRow2 item) {						
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
	protected List<CargoRow2> findData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/Cuentas_x_cobrar.sql");
		Object[] params=new Object[]{DateUtil.toDate("31/12/2008"),"CRE"};
		List<CargoRow2> cargos=ServiceLocator2.getJdbcTemplate().query(sql,params, new BeanPropertyRowMapper(CargoRow2.class));
		return cargos;
	}	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addAction("", "refreshSelection", "Refrescar(Sel)" )	
				,addAction(CXCActions.AdministracionRevisionCobro.getId(), "revision", "Mantenimiento")
				,addAction(CXCActions.RecepcionDeCuentasPorCobrar.getId(), "recepcion", "Recepción")
				,addAction(CXCActions.CancelarRecepcionDeCuentasPorCobrar.getId(), "cancelarRecepcion", "Cancelar Recepción")				
				,addAction(CXCActions.AdministracionRevisionCobro.getId(), "revisada", "Marcar Revisión")				
				//,addAction(CXCActions.AdministracionRevisionCobro.getId(), "cancelarRevisada", "Cancelar Revisión")
				,addAction(CXCActions.RecalcularRevisionCobro.getId(), "actualizarRevision", "Actualizar revisión" )
				};
		return actions;
	}
	
	@Override
	protected void doSelect(Object o) {
		CargoRow2 row=(CargoRow2)o;
		Cargo bean=getManager().getCargo(row.getId());
		if(bean!=null && (bean instanceof Venta) ){
			Venta v=(Venta)bean;
			boolean res=FacturaForm.show(v.getId());
			if(res)
				refreshSelection();
		}else if(bean!=null && (bean instanceof NotaDeCargo)){
			NotaDeCargo cargo=(NotaDeCargo)bean;
			CargoView.show(cargo.getId());
		}
	}
	
	public void actualizarRevision(){		
		final List<CargoRow2> selected=new ArrayList<CargoRow2>();
		int res=JOptionPane.showConfirmDialog(getControl(), "Todas las cuentas", "Actualización de revisión y cobro", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		switch (res) {
		case JOptionPane.YES_OPTION:
			selected.addAll(source);
			break;
		case JOptionPane.NO_OPTION:
			selected.addAll(getSelected());
			break;
		case JOptionPane.CANCEL_OPTION:
			return;
		}
		getLoadAction().setEnabled(false);
		SwingWorker<String, String> worker=new SwingWorker<String, String>(){
			protected String doInBackground() throws Exception {
				for(Object o:selected){
					CargoRow2 row=(CargoRow2)o;
					/*if(row.isRevisada())
						continue;*/
					Cargo cargo=getManager().getCargo(row.getId());
					RevisionDeCargosRules.instance().actualizar(cargo);
					persistir(cargo);			
				}
				return "OK";
			}
			protected void done() {
				try {
					get();
					load();
				} catch (Exception e) {
					MessageUtils.showMessage(
							"Error procesando actualización \n "+ExceptionUtils.getRootCauseMessage(e)
							, "Actualización de revisión y cobro");
				}finally{
					getLoadAction().setEnabled(true);
				}
			}
		};
		TaskUtils.executeSwingWorker(worker);
		
	}
	
	public void revision(){
		List<Cargo> selected=new ArrayList<Cargo>();
		for(Object o:getSelected()){
			CargoRow2 row=(CargoRow2)o;
			Cargo cargo=getManager().getCargo(row.getId());
			selected.add(cargo);
			
		}
		selected=RevisionDeCargosForm.showForm(selected);
		if(selected==null)
			return;
		try {
			RevisionDeCargosRules.instance().validate(selected);
		} catch (Exception e) {
			MessageUtils.showMessage("Selección incorrecta:\n "+e.getMessage(), "Revisión");
			return;
		}
		
		if(!selected.isEmpty()){
			RevisionDeCargosRules.instance().actualizar(selected);
			for(Cargo c:selected){
				persistir(c);
			}
			refreshSelection();
		}
		
	}
	
	/**
	 * Procesa la recepcion de documentos seleccionados
	 *  (Recepcion de documentos en el departamento de cuentas por cobrar)
	 *  
	 */
	public void recepcion(){
		
		List<Cargo> selected=new ArrayList<Cargo>();
		for(Object o:getSelected()){
			CargoRow2 row=(CargoRow2)o;
			Cargo cargo=getManager().getCargo(row.getId());
			selected.add(cargo);
			
		}
		if(!selected.isEmpty()){
			boolean res1=RecepcionDeDocumentosRules.instance().validarSinRecibirCXC(selected);
			if(!res1){				
				JOptionPane.showMessageDialog(getControl()
						, "La seleccion no corresponde facturas sin recibir en Cuentas por Cobrar"
						,"Mantenimiento de Cuentas por pagar",JOptionPane.WARNING_MESSAGE);
				return;
			}else{
				List<Cargo> res=RecepcionDeDocumentosForm.showForm(selected);
				if(res!=null){
					for(Cargo c:res)
						persistir(c);
				}
			}
			refreshSelection();
		}
	}
	
	public void cancelarRecepcion(){
		if(!getSelected().isEmpty()){
			String pattern="Cancelar la recepcion de {0} documentos?";
			int res=JOptionPane.showConfirmDialog(getControl(), MessageFormat.format(pattern, getSelected().size()),"Cancelar recepción",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE );			
			if(res==JOptionPane.OK_OPTION){
				for(Object o:getSelected()){
					CargoRow2 row=(CargoRow2)o;
					Cargo cargo=getManager().getCargo(row.getId());
					RecepcionDeDocumentosRules.instance().cancelarRecepcion(cargo);
					persistir(cargo);
					
				}
				refreshSelection();
			}
		}
	}
	
	/**
	 * Registra la revision de uno o mas documentos
	 * 
	 */
	public void revisada(){
		
		for(Object o:getSelected()){
			CargoRow2 row=(CargoRow2)o;
			Cargo cargo=getManager().getCargo(row.getId());
			if(cargo.isRecibidaCXC()){
				//RegistrarRevisionForm.showForm(cargo);
				cargo.setRevisada(true);
				persistir(cargo);
			}
		}
		refreshSelection();
	}
	
	/**
	 * Cancela la revision de uno o mas documentos
	 * 
	 */
	public void cancelarRevisada(){
		for(Object o:getSelected()){
			CargoRow2 row=(CargoRow2)o;
			Cargo cargo=getManager().getCargo(row.getId());
			if(cargo.isRecibidaCXC()){
				//RegistrarRevisionForm.showForm(cargo);
				cargo.setRevisada(false);
				persistir(cargo);
			}
		}
		refreshSelection();
	}	
	
	
	
	private CargoRow2 refresh(CargoRow2 row){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/Cuentas_x_cobrar_row.sql");
		Object[] params=new Object[]{row.getId()};
		List<CargoRow2> target=ServiceLocator2.getJdbcTemplate().query(sql,params, new BeanPropertyRowMapper(CargoRow2.class));
		return target.isEmpty()?row:target.get(0);
	}
	
	public void refreshSelection(){
		for(Object row:getSelected()){
			CargoRow2 old=(CargoRow2)row;
			int index=source.indexOf(old);
			CargoRow2 fresh=refresh(old);
			if(index!=-1){
				logger.info("Cargo refrescado:"+fresh);
				source.set(index,fresh);
			}
		}
	}
	
	protected CargoRow2 getCurrentRow(){
		return (CargoRow2)getSelectedObject();
	}
	
	protected Cargo getSelectedCargo(){
		CargoRow2 row=(CargoRow2)getSelectedObject();
		if(row!=null){
			return getManager().getCargo(row.getId());
		}else
			return null;
	}

	/**
	 * Persiste una lista de cuentas, sincronizandola con la lista fuente del browser
	 * 
	 * PUNTO CENTRAL PARA PERSISTIR TODOS LOS CAMBIOS EN ESTA PANEL
	 *  
	 * @param res
	 */
	protected void persistir(Cargo cargo){
		try {
				getManager().save(cargo);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getRootCause(e),e);
		}
		
	}
	
	public CXCManager getManager(){
		return ServiceLocator2.getCXCManager();
	}
	
	public class SelectionPredicate implements Predicate{

		public boolean evaluate(Object bean) {
			CargoRow2 c=(CargoRow2)bean;
			return (c!=null && (!c.isRevisada()));
		}
		
	}
}
