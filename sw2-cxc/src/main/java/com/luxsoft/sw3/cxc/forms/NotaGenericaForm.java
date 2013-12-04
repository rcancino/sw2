package com.luxsoft.sw3.cxc.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.old.ImporteALetra;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

/**
 * Presentacion de nota de credito para consulta
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class NotaGenericaForm extends AbstractForm{
	
	private final NotaDeCredito nota;
	
	
	public NotaGenericaForm(NotaDeCredito nota) {
		super(new DefaultFormModel(nota,true));		
		this.nota=nota;
		setModal(false);
	}
	
	protected HeaderPanel header;
	
	@Override
	protected JComponent buildHeader() {
		return getHeader();
	}
	
	protected HeaderPanel getHeader() {
		if(header==null){
			header= new HeaderPanel("Cliente"
					,""
					,getIconFromResource("images/siipapwin/cxc64.gif"));
			getHeader().setTitle(nota.getCliente().getNombreRazon());
			String pattern="{0}" 
				//+"\n Tels: {1}" 
					;
			String msg=MessageFormat.format(pattern
					, nota.getCliente().getDireccionAsString()
					//,nota.getCliente().getTelefonosRow()
					);
			getHeader().setDescription(msg);
		}		
		return header;
	}
	
	protected JComponent buildContentPane(){
		JComponent c=super.buildContentPane();
		c.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.setOpaque(false);
		return c;
	}

	@Override
	protected JComponent buildContent() {		
		JTabbedPane tabPanel=new JTabbedPane();
		
		JPanel panel=new JPanel(new BorderLayout());		
		panel.setLayout(new BorderLayout());
		panel.add(buildFormPanel(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithClose(),BorderLayout.SOUTH);
		
		tabPanel.addTab("General", panel);		
		return tabPanel;
	}
	
	protected JComponent buildButtonBarWithClose() {
		 JButton print=new JButton(CommandUtils.createPrintAction(this, "print"));
		 
		 print.setText("Imprimir");
		 print.setMnemonic('I');
       JPanel bar = ButtonBarFactory.buildRightAlignedBar(new JButton[]{
       		print
       		,createCloseButton(true)
       		
       });
       bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
       return bar;
   }	
	
	protected JComponent buildFormPanel(){
		return buildEditorPanel();		
	}
	
	private JComponent buildEditorPanel(){
		FormLayout layout=new FormLayout(
				"l:40dlu,2dlu,p, 2dlu, " +
				"l:40dlu,2dlu,p, 2dlu, " +
				"l:40dlu,2dlu,p:g"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Folio",addReadOnly("folio"));		
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("tipo",addReadOnly("info"));
		builder.nextLine();
		
		builder.append("Total",addReadOnly("total"));
		builder.append("Disponible",addReadOnly("disponible"));
		builder.nextLine();
		
		builder.append("Tipo Desc",addReadOnly("info"));
		builder.nextLine();
		builder.append("Comentario ",addReadOnly("comentario"),9);
		builder.nextLine();
		
		builder.appendSeparator("Aplicaciones");
		builder.nextLine();
		builder.append(buildAplicacionesGrid(),11);
		
		JPanel p=builder.getPanel();
		p.setEnabled(false);
		return p;
	}
	
	private JXTable aplicacionesGrid;
	
	private JComponent buildAplicacionesGrid(){
		final String[] props={"detalle.origen","cargo.sucursal.nombre","detalle.documento","detalle.fechaCargo","fecha","importe","comentario"};
		final String[] cols={"Origen (Docto)","Sucursal","Docto","Fecha(Docto)","Fecha","importe","Comentario"};
		final TableFormat<Aplicacion> tf=GlazedLists.tableFormat(Aplicacion.class,props, cols);
		
		final EventTableModel tm=new EventTableModel(GlazedLists.eventList(nota.getAplicaciones()),tf);
		aplicacionesGrid=ComponentUtils.getStandardTable();
		aplicacionesGrid.setModel(tm);
		JComponent c=ComponentUtils.createTablePanel(aplicacionesGrid);
		c.setPreferredSize(new Dimension(650,150));
		return c;		
	}
	
	public void print(){
		ComprobanteFiscal cf=ServiceLocator2.getCFDManager().cargarComprobante(nota);
		if(cf==null){
			final Map parameters=new HashMap();
			String total=ImporteALetra.aLetra(nota.getTotalCM());
			parameters.put("ABONO_ID", String.valueOf(nota.getId()));
			parameters.put("IMP_CON_LETRA", total);
			if(nota instanceof NotaDeCreditoDevolucion)
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/NotaDeCreditoDevCopia.jasper"), parameters);
			else
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/NotaDeCreditoCopia.jasper"), parameters);
		}else{
			CFDPrintServicesCxC.imprimirNotaDeCreditoElectronica(nota.getId());
		}
		
		//doClose();
	}
	
	/**
	 * 
	 * @param notaId
	 */
	public static void show(String notaId){
		String hql="from NotaDeCredito n " +
				" left join fetch n.cliente c" +
				//" left join fetch n.cliente.telefonos ctel" +
				" left join fetch n.aplicaciones a" +
				" where n.id=?";
		List<NotaDeCredito> res=ServiceLocator2.getHibernateTemplate().find(hql,notaId);
		if(!res.isEmpty()){
			
			final NotaDeCredito nota=res.get(0);
			NotaGenericaForm form=new NotaGenericaForm(nota);
			form.open();
		}
		
		
	}
	
	public static void show(final NotaDeCredito nota){
		NotaGenericaForm form=new NotaGenericaForm(nota);
		form.open();
	}

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		DBUtils.whereWeAre();
		SwingUtilities.invokeAndWait(new Runnable(){

			public void run() {
				SWExtUIManager.setup();
				show("8a8a81c7-291e29bd-0129-1e48297e-0008");
			}
			
		});
		
		
	}

}
