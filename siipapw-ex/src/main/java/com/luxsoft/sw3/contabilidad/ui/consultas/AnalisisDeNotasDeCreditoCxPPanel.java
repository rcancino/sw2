package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;




public class AnalisisDeNotasDeCreditoCxPPanel extends AbstractMasterDatailFilteredBrowserPanel<CXPNota, CXPAplicacion>{
	
	
	private Date fecha;

	public AnalisisDeNotasDeCreditoCxPPanel() {
		super(CXPNota.class);
		setTitle("Notas de credito Compras y CxP");
	}
	
	protected void init(){		
		setDefaultComparator(GlazedLists.beanPropertyComparator(CXPNota.class, "documento"));		
		addProperty("nombre"
				,"documento"
				,"fecha"
				,"concepto"				
				,"total"
				,"descuento"
				,"aplicado"
				,"disponible"
				,"comentario"
				);
		addLabels("Proveedor"
				,"Documento"
				,"Fecha"
				,"Concepto"				
				,"Total"
				,"Descuento"
				,"Aplicado"
				,"Disponible"
				,"Comentario"
				);
		installTextComponentMatcherEditor("Proveedor", "nombre","clave");
		installTextComponentMatcherEditor("Documento","documento");
		installTextComponentMatcherEditor("Total","total");
		manejarPeriodo();
		setDetailTitle("Aplicaciones");
	}
	
	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props=new String[]{
				"abono.documento"
				,"fecha"		
				,"importe"
				,"cargo.documento"
				,"cargo.fecha"
				,"comentario"
				};
		String[] labels=new String[]{
				"Abono"
				,"Fecha"		
				,"Importe"
				,"Factura "
				,"F. Factura"
				,"Comentario"
				};
		return GlazedLists.tableFormat(CXPAplicacion.class,props,labels);
	}

	@Override
	public Action[] getActions() {
		if(this.actions==null){
			actions=new Action[]{
			getLoadAction()
			};
		}
		return actions;
	}
	
	@Override
	protected Model<CXPNota, CXPAplicacion> createPartidasModel() {		
		return new Model<CXPNota, CXPAplicacion>(){
			public List<CXPAplicacion> getChildren(final CXPNota parent) {
				return ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback() {
					public Object doInHibernate(Session session) throws HibernateException,SQLException {
						CXPNota nota=(CXPNota)session.get(CXPNota.class, parent.getId());
						final ArrayList<CXPAplicacion> res=new ArrayList<CXPAplicacion>();
						res.addAll(nota.getAplicaciones());
						return res;
					}
				});
				//return new ArrayList<CXPAplicacion>(parent.getAplicaciones());
			}			
		};
	}
	
	
	public void open(){
		load();
	}
	
	private JTextField documentField=new JTextField(5);
	
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Documento",documentField);
	}
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		TextFilterator docFilterator=GlazedLists.textFilterator("cargo.documento");
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(documentField,docFilterator);
		editors.add(docEditor);
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}
	

	@Override
	protected List<CXPNota> findData() {
		return ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery("from CXPNota c where c.fecha=? ")
						.setParameter(0, getFecha(),Hibernate.DATE)
						.list()
						;
			}
		});
	}	
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}	
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
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
		
		private JLabel total4=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			total4.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.appendSeparator("Resumen ");
			builder.append("Total",total1);
			builder.append("Aplicado",total2);
			builder.append("Disponible",total4);
			
			
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
			double aplicado=0;
			double disponible=0;
			
			for(Object obj:getFilteredSource()){
				CXPNota a=(CXPNota)obj;
				valorTotal1+=a.getTotal().doubleValue();				
				aplicado+=a.getAplicado().doubleValue();
				disponible+=a.getDisponible().doubleValue();
			}
			total1.setText(nf.format(valorTotal1));
			total2.setText(nf.format(aplicado));
			total4.setText(nf.format(disponible));
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
	}
	
	public static void show(String dateAsString){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha);
	}
	
	public static void show(final Date fecha){
		final AnalisisDeNotasDeCreditoCxPPanel browser=new AnalisisDeNotasDeCreditoCxPPanel();
		browser.setFecha(fecha);
		FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
		dialog.open();
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
				DBUtils.whereWeAre();
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				show("30/07/2011");
				System.exit(0);
			}

		});
	}
	
	
}
