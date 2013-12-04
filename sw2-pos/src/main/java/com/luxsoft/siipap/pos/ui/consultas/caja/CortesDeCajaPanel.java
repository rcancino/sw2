package com.luxsoft.siipap.pos.ui.consultas.caja;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.PredicateUtils;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.forms.caja.CorteDeCajaChequeForm;
import com.luxsoft.siipap.pos.ui.forms.caja.CorteDeCajaDepositoForm;
import com.luxsoft.siipap.pos.ui.forms.caja.CorteDeCajaEfectivoForm;
import com.luxsoft.siipap.pos.ui.forms.caja.CorteDeCajaTarjetaForm;
import com.luxsoft.siipap.pos.ui.reports.ArqueoCaja;
import com.luxsoft.siipap.pos.ui.reports.CierreCaja;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.sw3.caja.Caja;
import com.luxsoft.sw3.services.Services;





public class CortesDeCajaPanel extends FilteredBrowserPanel<Caja>{
	
	private CajaController controller;

	public CortesDeCajaPanel() {
		super(Caja.class);
		controller=new CajaController();
	}	
	
	protected void init(){
		addProperty("folio","sucursal.nombre","origen","fecha","caja","deposito","pagos","cortesAcumulados","corte","tipo","concepto","comentario","chequeNumero","chequeNombre");
		addLabels("Folio","Sucursal","Origen","Fecha","Caja","Deposito","Pagos","Acumulados","Corte","Tipo","Concepto","Comentario","Cheque (#)","Cheque Nombre");
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Concepto", "concepto");
		installTextComponentMatcherEditor("Deposito", "deposito");
		manejarPeriodo();
	}	
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodoDelMesActual();
		//periodo=Periodo.hoy();
	}	
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			
			List<Action> actions=ListUtils.predicatedList(new ArrayList<Action>(), PredicateUtils.notNullPredicate());
			
			actions.add(getLoadAction());
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "corteEfectivo", "Corte efectivo"));
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "corteCheque", "Corte cheque"));
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "corteTarjeta", "Corte tarjeta"));
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),this, "corteDeposito", "Corte deposito"));
			
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "generarFichasDeDeposito", "Fichas"));
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "registroDeGasto", "Gasto"));
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "cancelacionDeGasto", "Cancelar Gasto"));
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "rembolso", "Rembolso"));
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "morralla", "Morralla"));		
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "cambiarChequePorEfectivo", "Cambio de cheque"));
			actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "cambiarTarjetaPorEfectivo", "Cambio de tarjeta"));
			this.actions=actions.toArray(new Action[actions.size()]);
		}
		return actions;
	}
	
	
	
	@Override
	protected List<Action> createProccessActions() {
		
		List<Action> actions=ListUtils.predicatedList(new ArrayList<Action>(), PredicateUtils.notNullPredicate());
		
		actions.add(addAction("", "reporteCierreCaja", "Cierre"));
		actions.add(addAction("", "reporteArqueoCaja", "Arqueo"));
		
		 
		return actions;
	}
	
	@Override
	protected List<Caja> findData() {
		String hql="from Caja c where c.fecha between ? and ?";
		return Services.getInstance().getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}
	
	public void corteEfectivo(){
		Caja bean=CorteDeCajaEfectivoForm.registrarCorte();
		if(bean!=null){
			source.add(bean);
			afterInsert(bean);
		}
	}
	public void corteCheque(){
		Caja bean=CorteDeCajaChequeForm.registrarCorte();
		if(bean!=null){
			source.add(bean);
			afterInsert(bean);
		}
	}
	public void corteTarjeta(){
		Caja bean=CorteDeCajaTarjetaForm.registrarCorte();
		if(bean!=null){
			source.add(bean);
			afterInsert(bean);
		}
	}
	public void corteDeposito(){
		Caja bean=CorteDeCajaDepositoForm.registrarCorte();
		if(bean!=null){
			source.add(bean);
			afterInsert(bean);
		}
	}
	
	public void reporteCierreCaja(){
		CierreCaja.run();
	}
	
	public void reporteArqueoCaja(){
		ArqueoCaja.run();
	}

	public CajaController getController() {
		return controller;
	}

	public void setController(CajaController controller) {
		this.controller = controller;
	}
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		
		private JLabel total1=new JLabel();
		private JLabel total2=new JLabel();
		private JLabel total3=new JLabel();
		private JLabel total4=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			total3.setHorizontalAlignment(SwingConstants.RIGHT);
			total4.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.appendSeparator("Resumen ");
			builder.append("Caja",total1);
			builder.append("Deposito",total2);
			builder.append("Pagos",total3);
			builder.append("Acumulados",total4);
			
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		public void updateTotales(){
			
			double valorTotal1=0;
			double valor2=0;
			double valor3=0;
			double valor4=0;
			
			for(Object obj:getFilteredSource()){
				Caja a=(Caja)obj;
				valorTotal1+=a.getCaja().doubleValue();
				
				valor2+=a.getDeposito().doubleValue();
				valor3+=a.getPagos().doubleValue();
				valor4+=a.getCortesAcumulados().doubleValue();
				
			}
			total1.setText(nf.format(valorTotal1));
			total2.setText(nf.format(valor2));
			total3.setText(nf.format(valor3));
			total4.setText(nf.format(valor4));
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
	}

}
