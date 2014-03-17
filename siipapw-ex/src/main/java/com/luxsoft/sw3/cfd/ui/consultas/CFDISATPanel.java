package com.luxsoft.sw3.cfd.ui.consultas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.Matcher;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.cfdi.CFDIPrintUI;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
//import com.luxsoft.siipap.pos.ui.forms.FacturaForm;
//import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;

import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.ventas.ui.consultas.ReporteMensualCFD;
//import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.ui.consultas.ReporteMensualCFI;




public class CFDISATPanel extends FilteredBrowserPanel<CFDIRow>{
	
	
	CheckBoxMatcher<CFDIRow> canceladosMatcher;

	public CFDISATPanel() {
		super(CFDIRow.class);
		setTitle("Comprobantes fiscales digitales por Internet(CFDI)");		
	}
	
	public void init(){
	
		addProperty(
				"serie","tipo","folio","cliente"
				,"fecha","impuesto","total","estado","timbrado","uuid"
				,"rfc","comentarioCfdi","cancelacionSat","fechaCan","comentarioCan"
				);
		addLabels(
				"serie","tipo","folio","cliente"
				,"fecha","impuesto","total","estado","timbrado","uuid"
				,"rfc","comentarioCfdi","cancelacionSat","fechaCan","comentarioCan"
				);
		canceladosMatcher=new CheckBoxMatcher<CFDIRow>(false) {			
			protected Matcher<CFDIRow> getSelectMatcher(Object... obj) {				
				return new Matcher<CFDIRow>() {					
					public boolean matches(CFDIRow item) {
						return (item.isCancelado()||item.isCanceladoSat());
					}					
				};
			}
		};
		installCustomMatcherEditor("Cancelados", canceladosMatcher.getBox(), canceladosMatcher);
		installTextComponentMatcherEditor("Serie", "serie");
		installTextComponentMatcherEditor("Folio", "folio");
		installTextComponentMatcherEditor("Cliente", "cliente");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Total", "total");
		manejarPeriodo();
	}
	
	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		header =new Header("Comprobante Fiscal Digital CFDI", periodo.getFechaInicial() +" - "+periodo.getFechaFinal()); //new Header("Comprobante Fiscal Digital CFDI  "+periodo.getFechaInicial() +" - "+periodo.getFechaFinal());
		return header.getHeader();
	}
	
	public void updateHeader() {
			header.setTitulo("Comprobante Fiscal Digital CFDI");
			header.setDescripcion( periodo.getFechaInicial() +" - "+periodo.getFechaFinal());
		
	}
	
	@Override
	protected void afterGridCreated() {		
		super.afterGridCreated();
		JPopupMenu popup=new JPopupMenu("Operaciones");
		for(Action a:getActions()){
			popup.add(a);
		}
		getGrid().setComponentPopupMenu(popup);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
		
				};
		return actions;
	}
	

	
	protected void afterLoad(){
		super.afterLoad();
		updateHeader();
	}

	@Override
	protected List<CFDIRow> findData() {
		
		
		
		
		String sql="SELECT c.cfd_id,c.serie,c.tipo,c.folio,c.receptor as cliente,c.creado as fecha,c.impuesto,c.total" +
				",c.estado,c.timbrado,c.uuid,c.rfc,c.comentario as comentarioCfdi,x.cargo_id,date(c.cancelacion) as cancelacionSat" +
				",date(x.creado) as fechaCan,x.comentario as comentarioCan" +
				" FROM sx_cfdi c left join sx_cxc_cargos_cancelados x on(x.CARGO_ID=c.ORIGEN_ID) " +
				" where date(c.CREADO) BETWEEN ? and ?";
		
		return ServiceLocator2.getJdbcTemplate().query(sql, new Object[]{periodo.getFechaInicial()
				,periodo.getFechaFinal()},new BeanPropertyRowMapper(CFDIRow.class));
		
	}
	
	
	
	
	@Override
	protected List<Action> createProccessActions(){
		List<Action> res=super.createProccessActions();
		
		return res;
	}
	


	@Override
	protected void doSelect(Object bean) {
		CFDIRow row=(CFDIRow)bean;
		String hql="from CFDI where id=?";
		List<CFDI> cfdis= ServiceLocator2.getHibernateTemplate().find(hql
				, new Object[]{row.getCfd_id()}
				);
		CFDI cfdi=cfdis.get(0);
		if(cfdi.getTipo().equals("FACTURA")){

			Venta venta=ServiceLocator2.getFacturasManager().buscarVentaInicializada(cfdi.getOrigen());
			Date time=ServiceLocator2.obtenerFechaDelSistema();
			CFDIPrintUI.impripirComprobante(venta, cfdi, " ", time,ServiceLocator2.getHibernateTemplate(),true);
		}else if(cfdi.getTipo().equals("NOTA_CREDITO")){
			NotaDeCredito nota=CXCUIServiceFacade.buscarNotaDeCreditoInicializada(cfdi.getOrigen());
			Date time=ServiceLocator2.obtenerFechaDelSistema();
			CFDIPrintUI.impripirComprobante(nota, cfdi, "", time, true);
		}else if(cfdi.getTipo().equals("NOTA_CARGO")){
			NotaDeCargo nota=CXCUIServiceFacade.buscarNotaDeCargoInicializada(cfdi.getOrigen());
			Date time=ServiceLocator2.obtenerFechaDelSistema();
			CFDIPrintUI.impripirComprobante(nota, cfdi, "", time, true);
		}
		
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
	
	
	private JLabel totalIva=new JLabel();
	private JLabel totalIvaCancelado=new JLabel();
	private JLabel netoIva=new JLabel();
	

	@Override
	protected JComponent buildContent() {
		final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		totalIva.setHorizontalAlignment(SwingConstants.RIGHT);
		totalIvaCancelado.setHorizontalAlignment(SwingConstants.RIGHT);
		netoIva.setHorizontalAlignment(SwingConstants.RIGHT);
		
		total1.setHorizontalAlignment(SwingConstants.RIGHT);
		total2.setHorizontalAlignment(SwingConstants.RIGHT);
		total3.setHorizontalAlignment(SwingConstants.RIGHT);
		
		builder.appendSeparator("Impuestos ");
		builder.append("Total ",totalIva);
		builder.append("Cancelados",totalIvaCancelado);
		builder.append("Neto",netoIva);
		
		builder.appendSeparator("Totales ");
		builder.append("Total ",total1);
		builder.append("Cancelados",total2);
		builder.append("Neto",total3);
		
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
		
		double totalCfd=0;
		double totalCancelado=0;
		
		double totalIvaCfd=0;
		double totalIvaCancelado=0;
		
		
		for(Object obj:getFilteredSource()){
			CFDIRow a=(CFDIRow)obj;
			
			BigDecimal tot=a.getTotal();
			BigDecimal iva= a.getImpuesto();	
			totalCfd+=tot.doubleValue();
			totalIvaCfd+=iva.doubleValue();
			
			if(a.getFechaCan()!=null){
				BigDecimal totcan=a.getTotal();
				BigDecimal ivacan=a.getImpuesto();	
				totalCancelado+=totcan.doubleValue();
				totalIvaCancelado+=ivacan.doubleValue();
			}
		}
		total1.setText(nf.format(totalCfd));
		total2.setText(nf.format(totalCancelado));
		total3.setText(nf.format(totalCfd-totalCancelado));
		
		totalIva.setText(nf.format(totalIvaCfd));
		this.totalIvaCancelado.setText(nf.format(totalIvaCancelado));
		netoIva.setText(nf.format(totalIvaCfd-totalIvaCancelado));
	}
	
	private NumberFormat nf=NumberFormat.getNumberInstance();
	
}
	
	
	

	

}
