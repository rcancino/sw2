package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.lang.time.DateUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.pos.ui.forms.caja.PagoConDepositoForm;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.services.Services;

public class SelectorDeDepositos extends AbstractSelector<PagoConDeposito>{
	
	private Cliente cliente;
	private Sucursal sucursal;
	private Date fechaSistema;
	private boolean contado=true;

	public SelectorDeDepositos() {
		super(PagoConDeposito.class,"Depositos/Transferencias");
		this.sucursal=Services.getInstance().getConfiguracion().getSucursal();
		this.fechaSistema=Services.getInstance().obtenerFechaDelSistema();
	}

	protected void addButton(ToolBarBuilder builder){
		builder.add(CommandUtils.createEditAction(this, "modificarDeposito"));
	}

	


	@Override
	protected TextFilterator<PagoConDeposito> getBasicTextFilter() {
		return GlazedLists.textFilterator("nombre");
	}

	@Override
	protected void installEditors(EventList<MatcherEditor<PagoConDeposito>> editors) {
		super.installEditors(editors);
		Matcher<PagoConDeposito> m1=new Matcher<PagoConDeposito>(){
			public boolean matches(PagoConDeposito item) {
				if(item.getAutorizacion()!=null){
					return DateUtils.isSameDay(fechaSistema, item.getLiberado());
				}
				return true;
			}
		};
		MatcherEditor<PagoConDeposito> editor=GlazedLists.fixedMatcherEditor(m1);
		editors.add(editor);
	}



	@Override
	protected List<PagoConDeposito> getData() {
		if(getCliente()==null){
			Object[] params={sucursal.getId()};
			String hql="from PagoConDeposito p  where p.sucursal.id=? and p.total-p.aplicado>0 ";
			if(contado)
				hql=hql+" and p.origen in(\'MOS\',\'CAM\')";
			else
				hql=hql+" and p.origen=\'CRE\'";
			List<PagoConDeposito> pendientes=Services.getInstance().getHibernateTemplate().find(hql,params);
			return pendientes;
		}else{
			String hql="from PagoConDeposito p  where p.sucursal.id=?  and p.cliente.clave=? and p.total-p.aplicado>0";
			if(contado)
				hql=hql+" and p.origen in(\'MOS\',\'CAM\')";
			else
				hql=hql+" and p.origen=\'CRE\'";
			Object[] params={sucursal.getId(),getCliente().getClave()};
			return Services.getInstance().getHibernateTemplate().find(hql,params);
		}
		
	}

	@Override
	protected TableFormat<PagoConDeposito> getTableFormat() {
		String props[]={"origen","nombre","tipo","folio","fecha","fechaDeposito","total","solicito","salvoBuenCobro","autorizacionInfo","comentario"};
		String labels[]={"Origen","Cliente","Tipo","Folio","Fecha","Fecha (Dep)","Total","Solicita","SBC","Autorizado","Comentario"};
		return GlazedLists.tableFormat(PagoConDeposito.class,props,labels);
	}
	
	@Override
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(750,400));
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

	public void modificarDeposito(){
		if(getSelected()!=null){
			PagoConDeposito target=getSelected();
			target=(PagoConDeposito)Services.getInstance().getPagosManager().getAbono(target.getId());
			if(target.getAutorizacion()!=null){
				JOptionPane.showMessageDialog(this, "Este deposito no es modificable por que ha sido autorizado ");
				return;
			}
			if(target.getAplicado().doubleValue()>0){
				JOptionPane.showMessageDialog(this, "Este deposito no es modificable por que ya tiene aplicaciones");
				return;
			}
			target=PagoConDepositoForm.modificar(target);
			if(target!=null){
				int index=source.indexOf(target);
				if(index!=-1){
					//target.actualizarTotal();
					target.setComentario("");
					target=(PagoConDeposito)Services.getInstance().getPagosManager().salvar(target);
					source.set(index, target);
				}
			}
		}
	}
	
	public static PagoConDeposito buscarPago(boolean contado){
		final SelectorDeDepositos selector=new SelectorDeDepositos();
		selector.setContado(contado);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
	}

	public static PagoConDeposito buscarPago(){
		final SelectorDeDepositos selector=new SelectorDeDepositos();
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
	}
	
	public static PagoConDeposito buscarPago(final Cliente cliente){
		final SelectorDeDepositos selector=new SelectorDeDepositos();
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
				buscarPago();
				System.exit(0);
			}

		});
	}

}
