package com.luxsoft.sw3.bi;

import java.util.List;

import javax.swing.Action;

import org.jdesktop.swingx.JXTable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.util.SQLUtils;



/**
 * Consulta para el monitoreo de entregas
 *  
 * @author Ruben Cancino 
 *
 */
public class ClientesCreditoPanel extends FilteredBrowserPanel<ClienteCreditoRow>{

	public ClientesCreditoPanel() {
		super(ClienteCreditoRow.class);		
	}
	
	protected void init(){
		String[] props={"clave","nombre","rfc","linea","plazo","saldo","atrasoMaximo","permitirCheque","suspendido","postfechado","checkplus","vencimientoFechaFactura","modificado","usuario"};
		addProperty(props);
		addLabels("Clave","Cliente","RFC","Línea","Plazo","Saldo","Atraso","Cheque","Suspendido","Posfechado","Checkplus","VTO Fac","Modificado","Usuario");
		installTextComponentMatcherEditor("Nombre", "nombre");
		installTextComponentMatcherEditor("RFC", "rfc");
		//manejarPeriodo();
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addRoleBasedAction(CXCRoles.MODIFICACION_LINEA_DE_CREDITO.name(),"modificacionLineaDeCredito", "Linea de crédito")
				,addRoleBasedAction(CXCRoles.MODIFICACION_LINEA_DE_CREDITO.name(),"modificacionPermitirChequePosfechado", "Cheque posfechado")
				,addRoleBasedAction(CXCRoles.MODIFICACION_LINEA_DE_CREDITO.name(),"modificarPlazo", "Plazo")
				,addRoleBasedAction(CXCRoles.MODIFICACION_LINEA_DE_CREDITO.name(),"modificarDescuentoFijoCredito", "Descuento fijo")
				,addRoleBasedAction(CXCRoles.MODIFICACION_ATRASO_MAXIMO.name(),"modificarAtrsoMaximo", "Atraso máximo")
				//,addRoleBasedAction(CXCRoles.ADMINISTRACION_CHECKPLUS.name(),"modificarCheckplus", "CheckPlus")
				,addRoleBasedAction(CXCRoles.MODIFICACION_LINEA_DE_CREDITO.name(),"modificarPermitirCheque", "Permitir Cheque")
				,addRoleBasedAction(CXCRoles.MODIFICACION_LINEA_DE_CREDITO.name(),"modificarSuspendido", "Suspender")
				,addRoleBasedAction(CXCRoles.MODIFICACION_LINEA_DE_CREDITO.name(),"modificacionSuspendidoCredito", "Suspender Credito")
				,addRoleBasedAction(CXCRoles.DESBLOQUEO_POR_SALDO_CHEQUES_DEVUELTOS.name(),"desbloquearClientePorSaldoEnCheque", "Desbloqueo Cheque dev")
				,addRoleBasedAction(CXCRoles.MODIFICACION_TIPO_VENCIMIENTO.name(),"modificacionDeVencimientoFactura", "Cambio Vencimiento")
				,addRoleBasedAction(CXCRoles.MODIFICACION_LINEA_DE_CREDITO.name(),"bitacora", "Bitácora")
				
				
				};
		return actions;
	}

	@Override
	protected List<ClienteCreditoRow> findData() {
		String sql="select a.cliente_id,a.clave,a.nombre,a.rfc,b.linea,b.plazo,b.saldo,b.ATRASO_MAX as atrasoMaximo,PERMITIR_CHEQUE as permitirCheque" +
				",b.POSTFECHADO as postfechado,b.checkplus as checkplus ,b.VENCE_FACTURA as vencimientoFechaFactura" +
				",b.CRED_SUSPENDIDO as suspendido,b.modificado,b.MODIFICADO_USR as usuario " +
				"from sx_clientes a join sx_clientes_credito b on (a.CREDITO_ID=b.CREDITO_ID) order by a.nombre";
		return ServiceLocator2.getJdbcTemplate().query(sql, new BeanPropertyRowMapper(ClienteCreditoRow.class));
		
	}

	@Override
	public void open() {
		load();
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {

		//grid.getColumnExt("modificado").setCellRenderer(new DefaultTableRenderer(new Renderers.));
		
		//grid.getColumnExt("Recibió").setVisible(false);
		//grid.getColumnExt("Surtidor").setVisible(false);
		
	}
	/*
	public Action addInLineSecureAction(String actionId,String method,String label){
		Action a=new DispatchingAction(this,method);
		configAction(a, actionId,label);
		if(StringUtils.isBlank(actionId))
			return a;  //No requiere seguridad
		if(KernellSecurity.instance().isActionGranted(a))
			return a;
		return null;
	}
	*/
	public void modificacionLineaDeCredito(){
		ClienteCreditoRow row=(ClienteCreditoRow)getSelectedObject();
		if(row!=null){
			final Cliente res=ModificacionClienteForm.modificarLineaDeCredito(row.getClave());
			if(res!=null){
				refresh(res);
			}
		}
	}
	
	public void modificacionPermitirChequePosfechado(){
		ClienteCreditoRow row=(ClienteCreditoRow)getSelectedObject();
		if(row!=null){
			final Cliente res=ModificacionClienteForm.modificarChequePosFechadoCredito(row.getClave());
			if(res!=null){
				refresh(res);
			}
		}
	}
	
	public void modificarPlazo(){
		ClienteCreditoRow row=(ClienteCreditoRow)getSelectedObject();
		if(row!=null){
			final Cliente res=ModificacionClienteForm.modificarPlazoDeCredito(row.getClave());
			if(res!=null){
				refresh(res);
			}
		}
	}
	
	
	public void modificarDescuentoFijoCredito(){
		ClienteCreditoRow row=(ClienteCreditoRow)getSelectedObject();
		if(row!=null){
			final Cliente res=ModificacionClienteForm.modificarDescuentoFijoCredito(row.getClave());
			if(res!=null){
				refresh(res);
			}
		}
	}
	
	public void modificarAtrsoMaximo(){
		ClienteCreditoRow row=(ClienteCreditoRow)getSelectedObject();
		if(row!=null){
			final Cliente res=ModificacionClienteForm.modificarAtrasoMaximo(row.getClave());
			if(res!=null){
				refresh(res);
			}
		}
	}
	
	public void modificarCheckplus(){
		ClienteCreditoRow row=(ClienteCreditoRow)getSelectedObject();
		if(row!=null){
			final Cliente res=ModificacionClienteForm.modificarCheckplus(row.getClave());
			if(res!=null){
				refresh(res);
			}
		}
	}
	
	public void modificarPermitirCheque(){
		final Cliente res=AutorizacionChequeForm.modificarPermitirCheque();
		if(res!=null){
			refresh(res);
		}
	}
	public void desbloquearClientePorSaldoEnCheque(){
		final Cliente res=DesbloqueoSaldoChequesDevueltoForm.modificar();
		if(res!=null){
			refresh(res);
		}
	}
	
	public void modificarSuspendido(){
		final Cliente res=AutorizacionChequeForm.modificarSuspendido();
		if(res!=null){
			refresh(res);
		}
	}
	
	
	public void modificacionSuspendidoCredito(){
		ClienteCreditoRow row=(ClienteCreditoRow)getSelectedObject();
		if(row!=null){
			final Cliente res=ModificacionClienteForm.modificarSuspendidoCredito(row.getClave());
			if(res!=null){
				refresh(res);
			}
		}
	}
	
	public void modificacionDeVencimientoFactura(){
		ClienteCreditoRow row=(ClienteCreditoRow)getSelectedObject();
		if(row!=null){
			final Cliente res=ModificacionClienteForm.modificarVencimientoFactura(row.getClave());
			if(res!=null){
				refresh(res);
			}
		}
	}
	
	public void bitacora(){
		BitacoraClientesCredito.show();
	}
	
	public void refresh(Cliente c){
		ClienteCreditoRow row=new ClienteCreditoRow(c);
		int index=source.indexOf(row);
		if(index!=-1){
			source.set(index, row);
		}
	}
	
	
	
	public static void main(String[] args) {
		//SQLUtils.printBeanClasFromSQL("select a.cliente_id,a.clave,a.nombre,a.rfc,b.linea,b.plazo,b.saldo,b.ATRASO_MAX,PERMITIR_CHEQUE,b.POSTFECHADO,b.CRED_SUSPENDIDO,b.modificado,b.MODIFICADO_USR from sx_clientes a join sx_clientes_credito b on (a.CREDITO_ID=b.CREDITO_ID)",true);
		SQLUtils.printColumnNames("select a.cliente_id,a.clave,a.nombre,a.rfc,b.linea,b.plazo,b.saldo,b.ATRASO_MAX as atrasoMaximo,PERMITIR_CHEQUE,b.POSTFECHADO,b.CRED_SUSPENDIDO,b.modificado,b.MODIFICADO_USR from sx_clientes a join sx_clientes_credito b on (a.CREDITO_ID=b.CREDITO_ID)");
	}

}
