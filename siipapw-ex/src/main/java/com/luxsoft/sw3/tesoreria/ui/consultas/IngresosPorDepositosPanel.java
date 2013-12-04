package com.luxsoft.sw3.tesoreria.ui.consultas;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXTable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.Matcher;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.tesoreria.TESORERIA_ROLES;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

import com.luxsoft.sw3.tesoreria.ui.forms.SolicitudDeDepositoForm;

/**
 * Panel para el control de ingresos por depositos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class IngresosPorDepositosPanel extends FilteredBrowserPanel<SolicitudDeDeposito> implements ApplicationContextAware{
	
	private ApplicationContext context;
	
	private DateFormat df;
	
	public IngresosPorDepositosPanel() {
		super(SolicitudDeDeposito.class);
	}
	
	public void init(){
		addProperty(
				"sucursal.nombre"
				,"pago.origenAplicacion"
				,"clave"
				,"nombre"
				,"documento"
				,"pago.primeraAplicacion"
				,"pago.info"				
				,"total"
				,"pago.ingreso"
				,"bancoOrigen.clave"
				,"cuentaDestino.numero"				
				//,"pago.comentario"
				);
		addLabels(
				"Sucursal"
				,"Origen"
				,"Cliente"
				,"Nombre"
				,"Solicitud"
				,"Cobranza"
				,"Referencia"				
				,"Total"
				,"Ingreso"
				,"Banco"
				,"Cuenta"
				//,"Comentario"
				);
		setDefaultComparator(GlazedLists.beanPropertyComparator(SolicitudDeDeposito.class, "importado"));
		df=new SimpleDateFormat("dd/MM/yyyy");
		TextFilterator<SolicitudDeDeposito> fechaRevision=new TextFilterator<SolicitudDeDeposito>(){
			public void getFilterStrings(List<String> baseList, SolicitudDeDeposito element) {
				if(element.getFechaDeposito()!=null)
					baseList.add(df.format(element.getPago().getPrimeraAplicacion()));
				
			}			
		};
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Origen", "pago.origenAplicacion");
		installTextComponentMatcherEditor("Solicitud", "documento");
		installTextComponentMatcherEditor("Total", "total");
		installTextComponentMatcherEditor("Banco", "bancoOrigen.clave");
		installTextComponentMatcherEditor("Referencia", "pago.info");
		installTextComponentMatcherEditor("Fecha Cobranza ", fechaRevision,new JTextField(10));
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.hoy();
	}
	
	@Override
	protected void installCustomComponentsInFilterPanel(DefaultFormBuilder builder) {
		
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {		
		
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null){			
			actions=new Action[]{
				getLoadAction()	
				,addAction(TESORERIA_ROLES.CONTROL_DE_INGRESOS.getId(), "registrarIngreso", "Registrar ingreso")
				};
		}
		return actions;
	}

	@Override
	protected List<SolicitudDeDeposito> findData() {
		String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
				" left join fetch s.cuentaDestino c" +
				" left join fetch s.bancoOrigen b" +
				" left join fetch s.pago p " +				
				" where s.pago is not null and date(s.pago.primeraAplicacion) between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					periodo.getFechaInicial()
					,periodo.getFechaFinal()
					}
		);
	}
	
	@Override
	protected void doSelect(Object bean) {
		SolicitudDeDeposito sol=(SolicitudDeDeposito)bean;
		SolicitudDeDepositoForm.consultar(sol);
	}
	
	

	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
		//Matcher matcher=Matchers.beanPropertyMatcher(SolicitudDeDeposito.class, "atendido", Boolean.FALSE);
		//editors.add(GlazedLists.fixedMatcherEditor(matcher));
	}
	
	public SolicitudDeDeposito refresh(String id){
		String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
		" left join fetch s.cuentaDestino c" +
		" left join fetch s.bancoOrigen b" +
		" left join fetch s.pago p " +		
		" where s.id=?";
		return (SolicitudDeDeposito)ServiceLocator2.getHibernateTemplate().find(hql,id).get(0);
	}

	
	public void registrarIngreso(){
		if(!getSelected().isEmpty()){
			EventList<SolicitudDeDeposito> eventList=GlazedLists.eventList(getSelected());
			EventList<SolicitudDeDeposito> selected=new FilterList<SolicitudDeDeposito>(eventList, new Matcher<SolicitudDeDeposito>() {
				public boolean matches(SolicitudDeDeposito item) {
					if(item!=null){
						return ((item.getPago().getIngreso()==null) && (item.getPago().getPrimeraAplicacion()!=null));
					}
					return false;
				}
				
			});
			if(selected.isEmpty())
				return;
			if(MessageUtils.showConfirmationMessage("Registrar los ingresos por "+selected.size()+" depositos", "Registro de depositos")){
				for(SolicitudDeDeposito sol:selected){
					PagoConDeposito res=ServiceLocator2.getIngresosManager().registrarIngreso(sol.getPago());
					logger.info("Ingreso por pago registrado: "+res.getInfo());
					int index=source.indexOf(sol);
					if(index!=-1){
						sol=refresh(sol.getId());
						source.set(index, sol);
					}
				}
			}
		}
	}
	
	
	
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context=applicationContext;
		
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
				SolicitudDeDeposito a=(SolicitudDeDeposito)obj;
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

}
