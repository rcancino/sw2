package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.support.hibernate.StringEnumUserType;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;




public class AnalisisDeNotasDeCargoPanel extends AbstractMasterDatailFilteredBrowserPanel<NotaDeCredito, Aplicacion>{
	
	private String origen="CRE";
	private Date fecha;

	public AnalisisDeNotasDeCargoPanel() {
		super(NotaDeCredito.class);
		setTitle("Notas de credito");
	}
	
	protected void init(){
		
		setDefaultComparator(GlazedLists.beanPropertyComparator(Abono.class, "fecha"));
		
		addProperty("nombre","fecha","sucursal.nombre","origen","tipo","folio"
				,"total","aplicado","diferencia","disponibleCalculado","liberado","info","comentario");
		addLabels("Cliente","Fecha","Sucursal","Origen","Tipo","Folio"
				,"Total","Aplicado","Otros Prod","Disponible","Liberado","Info","Comentario");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Sucursal","sucursal.nombre");
		installTextComponentMatcherEditor("Folio","folio");
		installTextComponentMatcherEditor("Tipo","tipo");
		installTextComponentMatcherEditor("Desc Tipo","tipo");
		CheckBoxMatcher<NotaDeCredito> canceladosMatcher=new CheckBoxMatcher<NotaDeCredito>(){
			protected Matcher<NotaDeCredito> getSelectMatcher(Object... obj) {				
				return new Matcher<NotaDeCredito>(){
					public boolean matches(NotaDeCredito item) {
						return (item.getTotal().doubleValue()!=0);
					}					
				};
			}
		};
		installCustomMatcherEditor("Sin cancelados", canceladosMatcher.getBox(), canceladosMatcher);
		
		manejarPeriodo();
		setDetailTitle("Aplicaciones");
	}
	
	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props=new String[]{"detalle.formaDePago","detalle.folio","fecha","detalle.documento","cargo.numeroFiscal","cargo.total"
				,"importe","detalle.origen","cargo.precioNeto"
				,"detalle.postFechado","detalle.sucursal","detalle.nombre"
				,"detalle.banco"};
		String[] labels=new String[]{"F.P","Abono F","Fecha","Docto","Fiscal","Total","Importe(Apl)","Origen","P.N"
				,"PostFech","Sucursal","Cliente"
				,"Banco"};
		return GlazedLists.tableFormat(Aplicacion.class,props,labels);
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
	protected Model<NotaDeCredito, Aplicacion> createPartidasModel() {		
		return new Model<NotaDeCredito, Aplicacion>(){
			public List<Aplicacion> getChildren(NotaDeCredito parent) {				
				return getManager().buscarAplicaciones(parent);
			}			
		};
	}
	
	
	public void open(){
		load();
	}
	
	private JTextField documentField=new JTextField(5);
	private JTextField fiscalField=new JTextField(5);
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Documento",documentField);
		builder.append("Fiscal",fiscalField);
	}
	
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		
		TextFilterator docFilterator=GlazedLists.textFilterator("detalle.documento");
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(documentField,docFilterator);
		editors.add(docEditor);
		
		TextFilterator fiscalFilterator=GlazedLists.textFilterator("cargo.numeroFiscal");
		TextComponentMatcherEditor fiscalEditor=new TextComponentMatcherEditor(fiscalField,fiscalFilterator);
		editors.add(fiscalEditor);
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}
	

	@Override
	protected List<NotaDeCredito> findData() {
		return ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Properties properties=new Properties();
				properties.put("enumClassName", "com.luxsoft.siipap.cxc.model.OrigenDeOperacion");
				return session.createQuery("from NotaDeCredito a " +
						"left join fetch a.sucursal s " +
						"left join fetch a.cliente c " +
						"where a.fecha = ? " 
						+"and a.origen=?"
						)
						.setParameter(0, getFecha(),Hibernate.DATE)
						.setParameter(1, OrigenDeOperacion.valueOf(getOrigen()),Hibernate.custom(StringEnumUserType.class,properties))
						.list()
						;
			}
			
		});
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}
	
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public CXCManager getManager(){
		return ServiceLocator2.getCXCManager();
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
			builder.append("Total",total1);
			builder.append("Aplicado",total2);
			builder.append("Otros prod.",total3);
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
			double otrosProductos=0;
			double disponible=0;
			
			for(Object obj:getFilteredSource()){
				Abono a=(Abono)obj;
				valorTotal1+=a.getTotal().doubleValue();				
				aplicado+=a.getAplicado().doubleValue();
				otrosProductos+=a.getDiferencia().doubleValue();
				disponible+=a.getDisponible().doubleValue();
			}
			total1.setText(nf.format(valorTotal1));
			total2.setText(nf.format(aplicado));
			total3.setText(nf.format(otrosProductos));
			total4.setText(nf.format(disponible));
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
	}
	
	public static void show(String dateAsString,final String origen){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha, origen);
	}
	
	//private static ;
	
	public static void show(final Date fecha,final String origen){
		final AnalisisDeNotasDeCargoPanel browser=new AnalisisDeNotasDeCargoPanel();
		browser.setOrigen(origen);
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
				show("29/07/2011", "CAM");
				System.exit(0);
			}

		});
	}
	
	
}
