package com.luxsoft.siipap.cxc.ui.consultas;

import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.matchers.Matcher;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.cfd.task.GeneradorDePdf;
import com.luxsoft.sw3.mail.CorreoForm;



public class CFDPanel2 extends FilteredBrowserPanel<ComprobanteFiscal>{
	
	private GeneradorDePdf generador=new GeneradorDePdf();

	public CFDPanel2() {
		super(ComprobanteFiscal.class);
		setTitle("Comprobantes fiscales digitales (CFD)");		
	}
	
	public void init(){
		addProperty(
				"serie","tipo","folio","log.creado","receptor","rfc","total","estado"
				);
		addLabels(
				"xxxxxxxxxxSerie","Tipo","Folio","Fecha","Receptor","RFC","Total","Estado"
				);
		installTextComponentMatcherEditor("Serie", "serie");
		installTextComponentMatcherEditor("Folio", "folio");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Total", "total");
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-1);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction(),getViewAction()};
		return actions;
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
	protected List<ComprobanteFiscal> findData() {
		
		String hql="from ComprobanteFiscal c where date(c.log.creado) " +
				"between ? and ? and and c.serie like ?" +
				" and c.origen not in(select cc.cargo.id from CancelacionDeCargo cc)";
		List<ComprobanteFiscal> res= ServiceLocator2.getHibernateTemplate().find(hql
				, new Object[]{periodo.getFechaInicial()
				,periodo.getFechaFinal()
				,"%CRE"
				}
				);
		for(ComprobanteFiscal cfdi:res){
			try {
				cfdi.loadComprobante();
			} catch (Exception e) {
			}
			
		}
		return res;
	}
	
	
	@Override
	protected List<Action> createProccessActions(){
		List<Action> res=super.createProccessActions();
		res.add(addAction(null, "mandarPorCorreoElectronico", "Mandar por Correo"));
		return res;
	}

	@Override
	protected void doSelect(Object bean) {
		ComprobanteFiscal cfd=(ComprobanteFiscal)bean;
		if(cfd.getTipo().equals("FACTURA")){
			FacturaForm.show(cfd.getOrigen());
		}
	}

	public void mandarPorCorreoElectronico(){
		if(!getSelected().isEmpty()){
			
			EventList<ComprobanteFiscal> cfdisTimbrados=GlazedLists.eventList(getSelected());
			for(ComprobanteFiscal cfdi:cfdisTimbrados){
				try {
					String pdfURL=StringUtils.replace( cfdi.getXmlPath(), ".xml", ".pdf");
					URL pdfUrl=new URL(pdfURL);
					File pdf=new File(pdfUrl.toURI());
					if(!pdf.exists()){
						pdf=generador.generarPdfFile(cfdi);
						System.out.println("PDF generado: "+pdf.getPath());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			// Agrupar por cliente
			Comparator<ComprobanteFiscal> c=new Comparator<ComprobanteFiscal>() {
				public int compare(ComprobanteFiscal o1, ComprobanteFiscal o2) {
					return o1.getRfc().compareTo(o2.getRfc());
				}
			};
			GroupingList<ComprobanteFiscal> groupList=new GroupingList<ComprobanteFiscal>(cfdisTimbrados,c);
			
			//Iterar por cada lista - Una por cliente 
			for(List<ComprobanteFiscal> cl:groupList){
				Cliente cliente=ServiceLocator2.getClienteManager().buscarPorRfc(cl.get(0).getRfc());
				CorreoForm.mandarCorreo(cliente, cl);
			}
		}
	}
	
	
	

}
