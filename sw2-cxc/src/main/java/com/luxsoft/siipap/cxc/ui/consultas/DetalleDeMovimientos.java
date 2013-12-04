package com.luxsoft.siipap.cxc.ui.consultas;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.CXCRow;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.util.ClientePicker;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.TaskUtils;

public class DetalleDeMovimientos extends FilteredBrowserPanel<CXCRow>{

	private ClientePicker clientePicker;

	public DetalleDeMovimientos() {
		super(CXCRow.class);
		clientePicker=new ClientePicker();		
		clientePicker.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				updateHeader();
			}
		});
	}
	
	private void updateHeader(){
		if(clientePicker.getCliente()!=null){
			header.setTitle(clientePicker.getCliente().getNombreRazon());
		}
		else{
			header.setTitle("Seleccione un Cliente");
		}
	}


	@Override
	public void open() {
		updateHeader();
	}

	@Override
	protected EventList getSourceEventList() {		
		return new SortedList(super.getSourceEventList(),new MyComparator());
	}
	
	
	protected void init(){		
		addProperty(				
				"cargoId"
				,"abonoId"
				,"origen"
				,"sucursal"
				,"fecha"
				,"vencimiento"
				,"atraso"
				,"tipo"
				,"tipoDesc"
				//,"pagoRef"
				,"cargo"
				,"saldoCargo"
				,"abono"
				,"disponible"
				,"saldoAcumulado"
				,"moneda"
				,"tc"
				,"fechaDocto"
				
				);
		addLabels(				
				"Cargo"
				,"Abono"
				,"Origen"
				,"Sucursal"				
				,"Fecha"
				,"Vto"
				,"Atr"
				,"Tipo"
				,"Tipo(Desc)"
				//,"P.Ref"
				,"Cargo"   
				,"Saldo"
				,"Abono"
				,"Disponible"
				,"Saldo (Acu)"				
				,"Moneda"
				,"TC"
				,"Fecha(Dcto)"
				
				);
		installTextComponentMatcherEditor("Cargo", "cargoId");
		installTextComponentMatcherEditor("Abono", "abonoId");
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Tipo Desc", "tipoDesc");
		installTextComponentMatcherEditor("Pago Ref", "pagoRef");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		
	}
	
	private HeaderPanel header;

	@Override
	protected JComponent buildContent() {
		JComponent parent=super.buildContent();
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(parent,BorderLayout.CENTER);
		header=new HeaderPanel("","");
		panel.add(header,BorderLayout.NORTH);
		return panel;
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				addAction(CXCActions.CXC_EstadoMovimientos.getId(), "buscarMovimientos", "Buscar")
				};
		return actions;
	}	

	public ClientePicker getClientePicker() {
		return clientePicker;
	}

	private TotalesPanel totalesPanel;
	
	public JPanel getTotalesPanel(){
		if(totalesPanel==null){
			totalesPanel=new TotalesPanel();
		}
		return (JPanel)totalesPanel.getControl();
	}


	protected DefaultFormBuilder getFilterPanelBuilder(){
		if(filterPanelBuilder==null){
			FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.getPanel().setOpaque(false);
			filterPanelBuilder=builder;
		}
		return filterPanelBuilder;
	}
	
	public void buscarMovimientos(){
		final ClientePicker.PickerForm form=new ClientePicker.PickerForm(this.clientePicker);
		form.open();
		if(!form.hasBeenCanceled()){			
			load();
		}
	}
	
	public void load(){
		source.clear();
		Loader worker=new Loader();		
		TaskUtils.executeSwingWorker(worker);
	}
	
	protected void afterLoad(){
		acumulado=getSupportBean().getSaldoInicial();
		
		BigDecimal cargos=BigDecimal.ZERO;		
		BigDecimal abonos=BigDecimal.ZERO;
		
		getSupportBean().setAbonos(BigDecimal.ZERO);
		getSupportBean().setCargos(BigDecimal.ZERO);
		getSupportBean().setSaldoFinal(BigDecimal.ZERO);
		
		
		if(source.isEmpty()) return;		
		for(int index=0;index<source.size();index++){
			CXCRow row=(CXCRow)source.get(index);			
			
			
			acumulado=acumulado.add(row.getCargo());
			acumulado=acumulado.subtract(row.getAbono());
			
			if(!"APL".equals(row.getTipo())){
				cargos=cargos.add(row.getCargo());
				abonos=abonos.add(row.getAbono().multiply(BigDecimal.valueOf(-1)));
			}
			row.setSaldoAcumulado(acumulado);
		}		
		getSupportBean().setAbonos(abonos);
		getSupportBean().setCargos(cargos);
		getSupportBean().actualizarSaldoFinal();
	}

	private class Loader extends SwingWorker<List<CXCRow>,String> implements HibernateCallback{
		
		private BigDecimal saldoInicial=BigDecimal.ZERO;

		@Override
		protected List<CXCRow> doInBackground() throws Exception {
			//saldoInicial=CXPServiceLocator.getInstance().getCXPManager().getSaldo(getProveedor().getProveedor(), getProveedor().getFechaInicial());
			return ServiceLocator2.getHibernateTemplate().executeFind(this);
			
		}

		@Override
		protected void done() {
			try {
				List<CXCRow> rows=get();
				System.out.println("Rows _ "+rows.size());
				source.addAll(rows);
				DetalleDeMovimientos.this.getSupportBean().setSaldoInicial(saldoInicial);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}finally{
				grid.packAll();
				header.setDescription(MessageFormat.format(
						"Movimientos del {0,date,short} al {1,date,short} Saldo Inicial: {2}"
						,getClientePicker().getFechaInicial()
						,getClientePicker().getFechaFinal()
						,saldoInicial)
						);
				afterLoad();
			}
			
		}

		public Object doInHibernate(Session session) throws HibernateException,SQLException {
			
			int buf=0;
			final List<CXCRow> rows=new ArrayList<CXCRow>();
			
			//Buscar saldo inicial
			List<Number> saldo=session.createQuery("select sum(ca.total-ca.aplicado) " +
					"from Cargo ca where ca.cliente.id=? and ca.fecha<?")
					
		/*				List<Number> saldo=session.createQuery(
			//		"select sum(x.saldo) from (" +
					"select sum(ca.total-ca.aplicado) as saldo from Cargo ca where ca.cliente.id=? and ca.fecha<?" +
					" union "	+		
					"select -sum(ca.total) as saldo from Abonos ca where ca.cliente.id=? and ca.fecha<?" 			
			//		") as x"
					)		*/
					
					
			.setLong(0, getClientePicker().getCliente().getId())
			.setParameter(1, getClientePicker().getFechaInicial(),Hibernate.DATE)
			.list();
			if(!saldo.isEmpty()){
				saldoInicial=BigDecimal.valueOf(saldo.get(0).doubleValue());
			}
			
			//Cargos
			logger.info("Procesando cargos...");
			ScrollableResults rs=session.createQuery("from Cargo ca where ca.cliente.id=? and ca.fecha between ? and ?")
			.setLong(0, getClientePicker().getCliente().getId())
			.setParameter(1, getClientePicker().getFechaInicial(),Hibernate.DATE)
			.setParameter(2, getClientePicker().getFechaFinal(),Hibernate.DATE)
			.scroll();
			
			while(rs.next()){
				Cargo ca=(Cargo)rs.get()[0];
				CXCRow row=new CXCRow(ca,getClientePicker().getFechaFinal());
				rows.add(row);
				if(buf++%20==0){
					session.flush();
					session.clear();
				}
			}
			
			//Abonos
			logger.info("Procesando abonos...");
			rs=session.createQuery("from Abono ca where ca.cliente.id=? and ca.fecha between ? and ?")
			.setLong(0, getClientePicker().getCliente().getId())
			.setParameter(1, getClientePicker().getFechaInicial(),Hibernate.DATE)
			.setParameter(2, getClientePicker().getFechaFinal(),Hibernate.DATE)
			.scroll();
			buf=0;
			
			while(rs.next()){
				Abono ca=(Abono)rs.get()[0];
				CXCRow row=new CXCRow(ca,getClientePicker().getFechaFinal());
				rows.add(row);
				if(buf++%20==0){
					session.flush();
					session.clear();
				}
			}
			
			//Aplicaciones
			logger.info("Procesando aplicaciones...");
			rs=session.createQuery("from Aplicacion ca where ca.cargo.cliente.id=? and ca.fecha between ? and ? ")
			.setLong(0, getClientePicker().getCliente().getId())
			.setParameter(1, getClientePicker().getFechaInicial(),Hibernate.DATE)
			.setParameter(2, getClientePicker().getFechaFinal(),Hibernate.DATE)
			.scroll();
			buf=0;
			while(rs.next()){
				Aplicacion aplicacion=(Aplicacion)rs.get()[0];
				CXCRow row=new CXCRow(aplicacion);
				rows.add(row);
				if(buf++%20==0){
					session.flush();
					session.clear();
				}
			}
			return rows;
		}
	}
	
	private SupportBean support;
	
	public SupportBean getSupportBean(){
		if(support==null){
			support=(SupportBean)Bean.proxy(SupportBean.class);
		}
		return support;
	}
	
	private BigDecimal acumulado=BigDecimal.ZERO;
	
	
	/**
	 * Panel para la presentacion de totales
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	private class TotalesPanel extends AbstractControl{
		
		private JLabel saldoInicial;
		private JLabel cargosField;
		private JLabel abonosField;
		private JLabel saldoField;
		
		private final PresentationModel model;
		 
		public TotalesPanel(){
			model=new PresentationModel(getSupportBean());
		}

		@Override
		protected JComponent buildContent() {
			
			Border b=null;
			
			saldoInicial=BasicComponentFactory.createLabel(model.getModel("saldoInicial"),NumberFormat.getCurrencyInstance());
			saldoInicial.setHorizontalAlignment(JLabel.RIGHT);
			saldoInicial.setBorder(b);
			
			cargosField=BasicComponentFactory.createLabel(model.getModel("cargos"),NumberFormat.getCurrencyInstance());
			cargosField.setHorizontalAlignment(JLabel.RIGHT);
			cargosField.setBorder(b);
			
			abonosField=BasicComponentFactory.createLabel(model.getModel("abonos"),NumberFormat.getCurrencyInstance());
			abonosField.setHorizontalAlignment(JLabel.RIGHT);
			abonosField.setBorder(b);
			
			saldoField=BasicComponentFactory.createLabel(model.getModel("saldoFinal"),NumberFormat.getCurrencyInstance());
			saldoField.setHorizontalAlignment(JLabel.RIGHT);
			saldoField.setBorder(b);
			
			FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Saldo Ini",saldoInicial);
			builder.append("Cargos",cargosField);
			builder.append("Abonos",abonosField);
			builder.append("Saldo Fin ",saldoField);
			builder.getPanel().setOpaque(false);
			return builder.getPanel();
		}
		
	}
	
	/**
	 * JavaBean para facilitar el manejo de los totales 
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	public static  class SupportBean {
		
		private BigDecimal saldoInicial=BigDecimal.ZERO;
		private BigDecimal cargos=BigDecimal.ZERO;
		private BigDecimal abonos=BigDecimal.ZERO;
		private BigDecimal saldoFinal=BigDecimal.ZERO;
		
		public SupportBean() {}
		
		public BigDecimal getSaldoInicial() {
			return saldoInicial;
		}
		public void setSaldoInicial(BigDecimal saldoInicial) {
			this.saldoInicial = saldoInicial;
		}
		public BigDecimal getCargos() {
			return cargos;
		}
		public void setCargos(BigDecimal cargos) {
			this.cargos = cargos;
		}
		public BigDecimal getAbonos() {
			return abonos;
		}
		public void setAbonos(BigDecimal abonos) {
			this.abonos = abonos;
		}
		public BigDecimal getSaldoFinal() {
			return saldoFinal;
		}
		public void setSaldoFinal(BigDecimal saldoFinal) {
			this.saldoFinal = saldoFinal;
		}	
		
		public void actualizarSaldoFinal(){
			setSaldoFinal(saldoInicial.add(cargos).add(abonos));
		}
	}
	
	/**
	 * Comparador para ordenar los movimientos en forma adecuada
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	private static class MyComparator implements Comparator<CXCRow>{

		public int compare(CXCRow o1, CXCRow o2) {
			int res=o1.getFecha().compareTo(o2.getFecha());
			//if(res==0){
				//return o1.getCreado().compareTo(o2.getCreado());
			//}
			return res;
		}
		
	}
}
