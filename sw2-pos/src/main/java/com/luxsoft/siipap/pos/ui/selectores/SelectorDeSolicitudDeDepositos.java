package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

import com.jgoodies.uif.builder.ToolBarBuilder;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.pos.ui.forms.caja.SolicitudDeDepositoForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

public class SelectorDeSolicitudDeDepositos extends AbstractSelector<DepositosRow>{
	
	private Cliente cliente;
	private Sucursal sucursal;
	private Date fechaSistema;
	private boolean contado=true;

	public SelectorDeSolicitudDeDepositos() {
		super(DepositosRow.class,"Solicitudes de autorizacion de depositos");
		this.sucursal=Services.getInstance().getConfiguracion().getSucursal();
		this.fechaSistema=Services.getInstance().obtenerFechaDelSistema();
	}

	protected void addButton(ToolBarBuilder builder){
		builder.add(CommandUtils.createEditAction(this, "modificarSolicitud"));
	}

	@Override
	protected TextFilterator<DepositosRow> getBasicTextFilter() {
		return GlazedLists.textFilterator("nombre","documento");
	}

/*	@Override
	protected void installEditors(EventList<MatcherEditor<DepositosRow>> editors) {
		super.installEditors(editors);
		Matcher<SolicitudDeDeposito> m1=new Matcher<SolicitudDeDeposito>(){
			public boolean matches(SolicitudDeDeposito item) {
				if(item.getPago()!=null){
					return DateUtils.isSameDay(fechaSistema, item.getPago().getLiberado());
				}
				return true;
			}
		};
		MatcherEditor<Dep> editor=GlazedLists.fixedMatcherEditor(m1);
		editors.add(editor);
	}*/



	@Override
	protected List<DepositosRow> getData() {
		String sql3=" SELECT D.sol_id,D.nombre " +
				",CASE WHEN D.TRANSFERENCIA>0 THEN 'TRANSFERENCIA' WHEN D.CHEQUE>0 AND D.EFECTIVO>0 THEN 'MIXTO' WHEN D.CHEQUE>0 THEN 'CHEQUE' ELSE 'EFECTIVO' END AS tipo " +
				",D.documento,D.fecha,D.FECHA_DEPOSITO as fechaDeposito,D.total,D.solicita,D.comentario,D.SALVO_COBRO as salvoBuenCobro " +
				",CONCAT((CASE WHEN D.TRANSFERENCIA>0 THEN 'TRANSFERENCIA: ' ELSE 'DEPOSITO: ' END),D.REFERENCIA) AS pagoInfo " +
				",null as  liberado " +
				"FROM sx_solicitudes_deposito D WHERE d.fecha>? and D.SUCURSAL_ID=? AND D.ABONO_ID IS NULL " +
				" union " +
				" SELECT D.sol_id,D.nombre " +
				",CASE WHEN D.TRANSFERENCIA>0 THEN 'TRANSFERENCIA' WHEN D.CHEQUE>0 AND D.EFECTIVO>0 THEN 'MIXTO' WHEN D.CHEQUE>0 THEN 'CHEQUE' ELSE 'EFECTIVO' END AS tipo " +
				",D.documento,D.fecha,D.FECHA_DEPOSITO as fechaDeposito,D.total,D.solicita,D.comentario,D.SALVO_COBRO as salvoBuenCobro " +
				",CONCAT((CASE WHEN D.TRANSFERENCIA>0 THEN 'TRANSFERENCIA: ' ELSE 'DEPOSITO: ' END),D.REFERENCIA) AS pagoInfo " +
				",a.fecha as  liberado " +
				"FROM sx_solicitudes_deposito D join sx_cxc_abonos a on (a.ABONO_ID=d.ABONO_ID) WHERE D.SUCURSAL_ID=? AND a.FECHA=?" ;
		 Date fecha1= DateUtils.addDays(new Date(),-15);
		 Date fecha= new Date();
		 Long sucursal= Services.getInstance().getConfiguracion().getSucursalLocalId();
		 Object[] args={
				 new SqlParameterValue(Types.DATE, fecha1)
				 ,sucursal
				 ,sucursal
				 ,new SqlParameterValue(Types.DATE, fecha)
				 }
		 ;
		
		 return Services.getInstance().getJdbcTemplate().query(sql3, args,new BeanPropertyRowMapper(DepositosRow.class));
		 
		
	}

	@Override
	protected TableFormat<DepositosRow> getTableFormat() {
		String props[]={"nombre","tipo","documento","fecha","fechaDeposito","total","solicita","comentario","salvoBuenCobro","pagoInfo","liberado"};
		String labels[]={"Cliente","Tipo","Folio","Fecha","Fecha (Dep)","Total","Solicita","Comentario","SBC","Pago","Liberado"};
		return GlazedLists.tableFormat(DepositosRow.class,props,labels);
	}
	
	@Override
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(850,400));
	}
	
	@Override
	protected void onWindowOpened() {
		load();
	}
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	
	
	public boolean isContado() {
		return contado;
	}

	public void setContado(boolean contado) {
		this.contado = contado;
	}

	public void modificarSolicitud(){
		if(getSelected()!=null){
			DepositosRow row=getSelected();
			SolicitudDeDeposito target=(SolicitudDeDeposito)Services.getInstance()
					.getSolicitudDeDepositosManager().get(row.getSol_id());
			if( (target.getPago()!=null) || StringUtils.isBlank(target.getComentario())){
				SolicitudDeDepositoForm.consultar(target);
				return;
			}			
			target=SolicitudDeDepositoForm.modificar(target);
			
			if(target!=null){
				int index=source.indexOf(row);
				if(index!=-1){					
					target.setComentario("");
					target=Services.getInstance().getSolicitudDeDepositosManager().save(target);
					DepositosRow rows=new DepositosRow(target);
					source.set(index, rows);
				}
			}
		}
	}
	
	public static DepositosRow buscar(boolean contado){
		final SelectorDeSolicitudDeDepositos selector=new SelectorDeSolicitudDeDepositos();
		selector.setContado(contado);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
	}

	public static DepositosRow buscar(){
		final SelectorDeSolicitudDeDepositos selector=new SelectorDeSolicitudDeDepositos();
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
	}
	
	public static DepositosRow buscar(final Cliente cliente){
		final SelectorDeSolicitudDeDepositos selector=new SelectorDeSolicitudDeDepositos();
		selector.setCliente(cliente);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				buscar();
				System.exit(0);
			}

		});
	}

}
