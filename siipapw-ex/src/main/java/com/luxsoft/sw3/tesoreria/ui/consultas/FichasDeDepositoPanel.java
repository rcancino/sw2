package com.luxsoft.sw3.tesoreria.ui.consultas;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FichaDet;
import com.luxsoft.siipap.cxc.service.DepositosManager;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reportes.ComisionTarjetasReportForm;
import com.luxsoft.siipap.reportes.DepositosEnEfectivo;
import com.luxsoft.siipap.reports.ImportesTraslado;
import com.luxsoft.siipap.reports.RelacionDeFichas;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.embarques.ui.consultas.AutorizacionDeAsignacionForm;
import com.luxsoft.sw3.tesoreria.TESORERIA_ROLES;
import com.luxsoft.sw3.tesoreria.model.CorreccionDeFicha;
import com.luxsoft.sw3.tesoreria.ui.forms.CorreccionDeFichaForm;
import com.luxsoft.sw3.tesoreria.ui.forms.RegistroIngresoEfectivoForm;




public class FichasDeDepositoPanel extends AbstractMasterDatailFilteredBrowserPanel<Ficha, FichaDet>{

	public FichasDeDepositoPanel() {
		super(Ficha.class);
	}
	
	public void init(){
		manejarPeriodo();
		addProperty("origen","fecha","sucursal.nombre","folio","total","cuenta","tipoDeFicha"
				,"corte","ingreso.id","comentario","cancelada");
		addLabels("Origen","Fecha","Suc","Folio","Total","Cuenta","Tipo(Ficha)"
				,"Corte","Ingreso","Comentario","Cancelada");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Folio", "folio");
		
		
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Total", "total");
		installTextComponentMatcherEditor("Tipo Ficha", "tipoDeFicha");
	}
	protected void manejarPeriodo(){
		periodo=Periodo.hoy();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"pago.nombre","pago.fecha","pago.info","importe","banco"};
		String[] labels={"Cliente","Fecha","Pago","Importe","Banco"};
		return GlazedLists.tableFormat(FichaDet.class,props, labels);
	}
	
	private JCheckBox pendientesBox;
	
	public JComponent[] getOperacionesComponents(){
		if(pendientesBox==null){
			pendientesBox=new JCheckBox("Pendientes",true);
			pendientesBox.setOpaque(false);
		}
		return new JComponent[]{pendientesBox};
	}

	@Override
	protected Model<Ficha, FichaDet> createPartidasModel() {		
		return new CollectionList.Model<Ficha, FichaDet>(){
			public List<FichaDet> getChildren(final Ficha parent) {
				return ServiceLocator2
				.getHibernateTemplate()
				.find("from FichaDet det left join fetch det.pago p where det.ficha.id=?", parent.getId());
			}
		};
	}
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addRoleBasedContextAction(null, TESORERIA_ROLES.CONTROL_DE_INGRESOS.name(),this, "correccionDeFicha", "Corrección de Ficha")
				//,addRoleBasedContextAction(null, TESORERIA_ROLES.CONTROL_DE_INGRESOS.name(),this, "cancelar", "Cancelar")
				,addRoleBasedContextAction(null, TESORERIA_ROLES.CONTROL_DE_INGRESOS.name(),this, "reporteDeFichas", "Reporte de Fichas")
				,addRoleBasedContextAction(null, TESORERIA_ROLES.CONTROL_DE_INGRESOS.name(),this, "reporteDepositosEfectivo", "Reporte Depositos Efe.")
				};
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "registrarIngreso", "Registrar Ingreso"));
		procesos.add(addAction("", "registrarIngresoEfectivo", "Registrar Ingreso Efectivo"));
		
		return procesos;
	}
	
	public void cancelar(){
		Ficha selected=(Ficha)getSelectedObject();
		if(selected!=null){
			int index=source.indexOf(selected);
			if(index!=-1){
				Ficha res=getManager().cancelarDeposito(selected.getId());
				source.set(index, res);
				selectionModel.setSelectionInterval(index, index);
			}			
		}		
	}

	@Override
	protected String getDeleteMessage(Ficha bean) {
		return "Seguro que desea cancelar el deposito: "+bean;
	}

	@Override
	protected List<Ficha> findData() {
		if(pendientesBox.isSelected()){
			String hql="from Ficha f where f.fecha between ? and ?  " +
					" and f.corte is null and f.cancelada is null " +
					" and f.origen in(\'MOS\',\'CAM\')";
			Object[] values={periodo.getFechaInicial(),periodo.getFechaFinal()};
			return ServiceLocator2.getHibernateTemplate().find(hql, values);
		}else{
			String hql="from Ficha f where f.fecha " +
					" between ? and ? and f.origen in(\'MOS\',\'CAM\') ";
			Object[] values={periodo.getFechaInicial(),periodo.getFechaFinal()};
			return ServiceLocator2.getHibernateTemplate().find(hql, values);
		}
		
	}
	
	public void reporteDeFichas(){
		RelacionDeFichas report=new RelacionDeFichas();
		report.actionPerformed(null);
	}
	
	public void reporteDepositosEfectivo(){
		DepositosEnEfectivo.run();
	}
	

	public void registrarIngreso(){
		if(!getSelected().isEmpty()){
			List<Ficha> fichas=new ArrayList<Ficha>(getSelected());
			
			for(Ficha f:fichas){
				
				//Validar si procede
				if(f.getCancelada()!=null)
					continue;
				if(f.getTotal().doubleValue()<=0)
					continue;
				if(f.getCorte()!=null)
					continue;
				
				int index=source.indexOf(f);
				if(index!=-1){
					if(MessageUtils.showConfirmationMessage("Registrar ingreso a bancos\n "+f.toString()+ "Total: "+f.getTotal(), "Ingreso a bancos")){
						f=ServiceLocator2.getIngresosManager().registrarIngresoPorFicha(f);
						source.set(index, f);
					}
				}
			}
			selectionModel.clearSelection();
		}
	}
	
	
	public void registrarIngresoEfectivo(){
		RegistroIngresoEfectivoForm.registrar();
		MessageUtils.showMessage("Registro de Ingresos realizado", "Registro de ingresos");
	}
	
	
	private DepositosManager getManager(){
		return ServiceLocator2.getDepositosManager();
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
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			total3.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.appendSeparator("Resumen ");
			builder.append("Total",total1);
			//builder.append("Ventas (Prom)",total2);
			//builder.append("Por Pedir",total3);
			
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
			//double toneladasVentas=0;
			//double toneladasPorPedir=0;
			
			for(Object obj:getFilteredSource()){
				Ficha a=(Ficha)obj;
				valorTotal1+=a.getTotal().doubleValue();
				//toneladasVentas+=a.getToneladasPromVenta();
				//toneladasPorPedir+=a.getToneladasPorPedir();
			}
			total1.setText(nf.format(valorTotal1));
			//total2.setText(nf.format(toneladasVentas));
			//total3.setText(nf.format(toneladasPorPedir));
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
	}
	
	public void correccionDeFicha(){
		Ficha ficha=(Ficha)getSelectedObject();
		if(ficha!=null){
			CorreccionDeFicha cc=CorreccionDeFichaForm.showForm(ficha.getId());
			if(cc!=null){
				ServiceLocator2.getIngresosManager().registrarCorreccionDeFicha(cc);
				if(cc!=null){
					selectionModel.clearSelection();
					int index=source.indexOf(cc.getFicha());
					if(index!=-1){
						source.set(index, cc.getFicha());
					}
				}
			}
			
		}
		
	}
	
}
