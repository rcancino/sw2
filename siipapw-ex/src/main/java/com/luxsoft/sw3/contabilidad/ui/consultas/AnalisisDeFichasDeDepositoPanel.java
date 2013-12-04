package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

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
import com.luxsoft.siipap.reports.RelacionDeFichas;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.tesoreria.TESORERIA_ROLES;




public class AnalisisDeFichasDeDepositoPanel extends AbstractMasterDatailFilteredBrowserPanel<Ficha, FichaDet>{
	
	private Date fecha=new Date();
	private String origen="MOS";
	private String sucursal;

	public AnalisisDeFichasDeDepositoPanel() {
		super(Ficha.class);
		setTitle("Fichas de deposito");
	}
	
	public void init(){
		addProperty("origen","fecha","sucursal.nombre","folio","total","cuenta","tipoDeFicha","corte","ingreso.id","comentario","cancelada");
		addLabels("Origen","Fecha","Suc","Folio","Total","Cuenta","Tipo(Ficha)","Corte","Ingreso","Comentario","Cancelada");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Folio", "folio");
		
		
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Total", "total");
		installTextComponentMatcherEditor("Tipo Ficha", "tipoDeFicha");
	}
	

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"pago.nombre","pago.fecha","pago.info","importe","banco"};
		String[] labels={"Cliente","Fecha","Pago","Importe","Banco"};
		return GlazedLists.tableFormat(FichaDet.class,props, labels);
	}
	
	/*
	private JCheckBox pendientesBox;
	
	public JComponent[] getOperacionesComponents(){
		if(pendientesBox==null){
			pendientesBox=new JCheckBox("Pendientes",true);
			pendientesBox.setOpaque(false);
		}
		return new JComponent[]{pendientesBox};
	}
	*/

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
				//,addRoleBasedContextAction(null, TESORERIA_ROLES.CONTROL_DE_INGRESOS.name(),this, "cancelar", "Cancelar")
				,addRoleBasedContextAction(null, TESORERIA_ROLES.CONTROL_DE_INGRESOS.name(),this, "reporteDeFichas", "Reporte de Fichas")
				};
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		//procesos.add(addAction("", "registrarIngreso", "Registrar Ingreso"));
		return procesos;
	}
	
	@Override
	protected String getDeleteMessage(Ficha bean) {
		return "Seguro que desea cancelar el deposito: "+bean;
	}

	@Override
	protected List<Ficha> findData() {
		String hql="from Ficha f where f.fecha=?  " +
				" and f.corte is not null " +
				" and f.cancelada is null " +
				" and f.origen like \'@ORIGEN\'";
		hql=hql.replaceAll("@ORIGEN", origen);
		
		Object[] values={fecha};
		if(StringUtils.isNotBlank(sucursal)){
			hql+=" and v.sucursal.nombre=?";
			values=new Object[]{getFecha(),getSucursal()};
		}
		return ServiceLocator2.getHibernateTemplate().find(hql, values);
	}
	
	public void reporteDeFichas(){
		RelacionDeFichas report=new RelacionDeFichas();
		report.actionPerformed(null);
	}	
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	public void open(){
		load();
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

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}
	
	
	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	public static void show(String dateAsString,final String origen){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha, origen);
	}
	
	public static void show(String dateAsString,final String origen,String sucursal){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha, origen);
	}
	
	//private static ;
	
	public static void show(final Date fecha,final String origen){
		final AnalisisDeFichasDeDepositoPanel browser=new AnalisisDeFichasDeDepositoPanel();
		browser.setOrigen(origen);
		browser.setFecha(fecha);
		FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
		dialog.open();
	}
	
	public static void show(final Date fecha,final String origen,String sucursal){
		final AnalisisDeFichasDeDepositoPanel browser=new AnalisisDeFichasDeDepositoPanel();
		browser.setOrigen(origen);
		browser.setFecha(fecha);
		browser.setSucursal(sucursal);
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
				show("11/04/2011", "CAM","ANDRADE");
				System.exit(0);
			}

		});
	}
	
	
}
