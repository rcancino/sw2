package com.luxsoft.siipap.cxp.ui.consultas;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.luxor.utils.BasicJavaBean;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPCargoAbono;

import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.ui.reportes.EstadoDeCuentaReport;
import com.luxsoft.siipap.cxp.ui.selectores.BuscadorDeProveedor;
import com.luxsoft.siipap.cxp.util.ProveedorPicker;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.TaskUtils;

public class DetalleDeMovimientos extends FilteredBrowserPanel<CXPRow>{

	
	private ProveedorPicker proveedorPicker;

	public DetalleDeMovimientos() {
		super(CXPRow.class);		
		proveedorPicker=ProveedorPicker.getNewInstance();
		BasicJavaBean bean=(BasicJavaBean)proveedorPicker;
		bean.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {				
				if(evt.getNewValue() instanceof Proveedor){
					Proveedor p=(Proveedor)evt.getNewValue();
					header.setTitle(p.getNombreRazon());
					header.setDescription("Estado de movimientos");
				}
			}
		});
	}


	@Override
	protected EventList getSourceEventList() {		
		return new SortedList(super.getSourceEventList(),new MyComparator());
	}
	
	
	protected void init(){		
		addProperty(
				"id"
				,"documento"
				,"cargoId"
				,"abonoId"
				,"fecha"
				,"vencimiento"
				,"atraso"
				,"tipo"
				,"tipoDesc"
				,"pagoRef"
				,"cargo"
				,"saldoCargo"
				,"abono"
				,"disponible"
				,"saldoAcumulado"
				,"importeAnalisis"
				,"saldoAnalisis"
				,"moneda"
				,"tc"
				,"importeOrigen"
				
				);
		addLabels(
				"ID"
				,"Dcto"
				,"CargoId"
				,"AbonoId"
				,"Fecha"
				,"Vto"
				,"Atr"
				,"Tipo"
				,"Tipo(Desc)"
				,"P.Ref"
				,"Cargo"
				,"Saldo"
				,"Abono"
				,"Disponible"
				,"Saldo (Acu)"
				,"Importe (A)"				
				,"Saldo   (A)"
				,"Moneda"
				,"TC"
				,"Importe Orig"
				
				);
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("Cargo", "cargoId");
		installTextComponentMatcherEditor("Abono", "abonoId");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Tipo Desc", "tipoDesc");
		installTextComponentMatcherEditor("Pago Ref", "pagoRef");
		
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
			printAction=CommandUtils.createPrintAction(this, "print");
			actions=new Action[]{
				addAction("seleccionarProveedor", "buscarMovimientos", "Buscar"),
				printAction
				};
		return actions;
	}
	
	private Action printAction;
	


	public void load(){
		source.clear();
		Loader worker=new Loader();		
		TaskUtils.executeSwingWorker(worker);
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
		BuscadorDeProveedor dialog=new BuscadorDeProveedor(proveedorPicker);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			source.clear();
			load();
		}
	}
	
	public ProveedorPicker getProveedor(){
		return proveedorPicker;
	}
	
	
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.getColumnExt("TC").setCellRenderer(Renderers.getTipoDeCambioRenderer());
	}

	public void print(){
		/*BuscadorDeProveedor dialog=new BuscadorDeProveedor(proveedorPicker);
		dialog.setSeleccionDeTipo(true);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			Proveedor prov=dialog.getProveedor();
			Periodo per=dialog.getPeriodo();
			String tipo=dialog.getTipo();
			Map params=new HashMap();
			params.put("PROVEEDOR", prov.getClave());
			params.put("FECHA_INI", per.getFechaInicial());
			params.put("FECHA_FIN", per.getFechaFinal());
			if("GENERAL".equals(tipo)){
				params.put("TIPO", "GENERAL");
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxp/EstadoDeCuentaGeneral.jaster"), params);
			}else{
				params.put("TIPO", "DETALLE");
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxp/EstadoDeCuentaDetalle.jaster"), params);
			}
		}*/
		EstadoDeCuentaReport action=new EstadoDeCuentaReport();
		action.execute();
	}

	protected void afterLoad(){
		acumulado=getSupportBean().getSaldoInicial();
		
		BigDecimal cargos=BigDecimal.ZERO;		
		BigDecimal abonos=BigDecimal.ZERO;
		
		getSupportBean().setAbonos(BigDecimal.ZERO);
		getSupportBean().setCargos(BigDecimal.ZERO);
		getSupportBean().setSaldoFinal(BigDecimal.ZERO);
		
		
		
		for(int index=0;index<source.size();index++){
			CXPRow row=(CXPRow)source.get(index);			
			
			
			acumulado=acumulado.add(row.getCargo());
			acumulado=acumulado.subtract(row.getAbono());
			
			if(!"APL".equals(row.getTipo())){
				cargos=cargos.add(row.getCargo());
				abonos=abonos.add(row.getAbono().multiply(BigDecimal.valueOf(-1)));
			}
			
			row.setSaldoAcumulado(acumulado);
			//
			//System.out.println(" Row: "+row.getId()+" Index: "+index+ "Acumulado: "+acumulado+ "Tipo: "+row.getTipo());
			
		}
		
		getSupportBean().setAbonos(abonos);
		getSupportBean().setCargos(cargos);
		getSupportBean().actualizarSaldoFinal();
	}

	private class Loader extends SwingWorker<List<CXPRow>,String> implements HibernateCallback{
		
		private BigDecimal saldoInicial=BigDecimal.ZERO;

		@Override
		protected List<CXPRow> doInBackground() throws Exception {
			saldoInicial=CXPServiceLocator.getInstance().getCXPManager().getSaldo(getProveedor().getProveedor(), getProveedor().getFechaInicial());
			return ServiceLocator2.getHibernateTemplate().executeFind(this);
			
		}

		@Override
		protected void done() {
			
			source.clear();
			try {
				source.addAll(get());
				DetalleDeMovimientos.this.getSupportBean().setSaldoInicial(saldoInicial);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}finally{
				grid.packAll();
				header.setDescription(MessageFormat.format("Movimientos del {0,date,short} al {1,date,short}",getProveedor().getFechaInicial(),getProveedor().getFechaFinal()));
				afterLoad();
			}
			
		}

		public Object doInHibernate(Session session) throws HibernateException,SQLException {
			ScrollableResults rs=session.createQuery("from CXPCargoAbono ca where ca.proveedor.id=? and ca.fecha between ? and ?")
			.setLong(0, getProveedor().getProveedor().getId())
			.setParameter(1, getProveedor().getFechaInicial(),Hibernate.DATE)
			.setParameter(2, getProveedor().getFechaFinal(),Hibernate.DATE)
			.scroll();
			int buf=0;
			List<CXPRow> rows=new ArrayList<CXPRow>();
			while(rs.next()){
				CXPCargoAbono ca=(CXPCargoAbono)rs.get()[0];
				if(ca instanceof CXPFactura){
					CXPFactura fac=(CXPFactura)ca;
					if(fac.getAnticipo()!=null)
						continue;
					CXPRow row=new CXPRow((CXPFactura)ca,proveedorPicker.getFechaFinal());
					rows.add(row);
				}else if(ca instanceof CXPNota){
					CXPRow row=new CXPRow((CXPNota)ca,proveedorPicker.getFechaFinal());
					rows.add(row);
				}else if(ca instanceof CXPPago){
					CXPRow row=new CXPRow((CXPPago)ca,proveedorPicker.getFechaFinal());
					rows.add(row);
				}
				
				if(buf++%20==0){
					session.flush();
					session.clear();
				}
			}
			
			rs=session.createQuery("from CXPAplicacion ca where ca.cargo.proveedor.id=? and ca.fecha between ? and ? ")
			.setLong(0, getProveedor().getProveedor().getId())
			.setParameter(1, getProveedor().getFechaInicial(),Hibernate.DATE)
			.setParameter(2, getProveedor().getFechaFinal(),Hibernate.DATE)
			.scroll();
			buf=0;
			while(rs.next()){
				CXPAplicacion aplicacion=(CXPAplicacion)rs.get()[0];
				CXPRow row=new CXPRow(aplicacion);
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
	
	private static class MyComparator implements Comparator<CXPRow>{

		public int compare(CXPRow o1, CXPRow o2) {
			int res=o1.getFecha().compareTo(o2.getFecha());
			if(res==0){
				return o1.getCreado().compareTo(o2.getCreado());
			}
			return res;
		}
		
	}
}
