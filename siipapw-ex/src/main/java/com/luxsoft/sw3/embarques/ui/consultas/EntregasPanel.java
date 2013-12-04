package com.luxsoft.sw3.embarques.ui.consultas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.cxc.old.ImporteALetra;
import com.luxsoft.siipap.cxc.ui.consultas.FacturaForm;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reportes.AnalisisDeEmbarqueForm;
import com.luxsoft.siipap.reportes.ComisionPorChofer;
import com.luxsoft.siipap.reportes.ComisionPorFacturista;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.sw3.embarque.ClientePorTonelada;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.embarques.ui.catalogos.ClientesPorToneladaBrowser;
import com.luxsoft.sw3.embarques.ui.forms.ComisionPorTrasladoForm;
import com.luxsoft.sw3.embarques.ui.forms.ComisionPorTrasladoFormModel;

/**
 * Panel para el mantenimiento de las comisiones de choferes sobre traslados
 * de materiales
 * 
 * @author Ruben Cancino
 *
 */
public class EntregasPanel extends FilteredBrowserPanel<Entrega>{

	public EntregasPanel() {
		super(Entrega.class);	
		setTitle("Entregas por chofer");
	}
	
	protected void init(){
		addProperty(
				"embarque.sucursal"
				//,"embarque.transporte.chofer.id"
				,"embarque.chofer"
				,"embarque.documento"
				,"embarque.regreso"
				,"documento"
				,"origen"
				,"factura.fecha"
				,"factura.importe"
				,"factura.flete"
				,"factura.cargos"
				,"clave"
				,"nombre"				
				,"parcial"				
				,"valor"
				,"porCobrar"
				,"atrasoEnCobranza"
				,"fechaComision"
				,"kilos"
				,"comisionPorTonelada"
				,"comision"
				,"importeComision"
				,"comentarioComision"
				,"arribo"
				,"recepcion"
				,"recibio"
				,"comentario"
				);
		addLabels(
				"Sucursal"
				//,"Chofer ID"
				,"Chofer"
				,"Embarque"
				,"Regreso (Emb)"
				,"Docto"
				,"Tipo"
				,"Fecha (Fac)"
				,"Sub Tot (Fac)"
				,"Flete"
				,"Cargos"
				,"Cliente"
				,"Nombre"				
				,"Parcial"				
				,"Valor"
				,"Por Cobrar"
				,"Atraso"
				,"Revisión"
				,"Kgr"
				,"Precio x TON"
				,"Comision (%)"
				,"Comision ($)"
				,"Comentario (Com)"
				,"Arribo"
				,"Recepción"
				,"Recibió"
				,"Comentario"
				);
		installTextComponentMatcherEditor("Sucursal","embarque.sucursal");
		installTextComponentMatcherEditor("Tipo","origen");
		installTextComponentMatcherEditor("Embarque","embarque.documento");
		installTextComponentMatcherEditor("Chofer", "embarque.transporte.chofer.nombre");
		//installTextComponentMatcherEditor("Chofer Id","embarque.chofer.id");		
		installTextComponentMatcherEditor("Cliente","nombre","clave");
		installTextComponentMatcherEditor("Documento","documento");		
		
		manejarPeriodo();
		periodo=Periodo.getPeriodoDelMesActual();
	}
	
	protected void adjustMainGrid(final JXTable grid){

		grid.getColumnExt("Cargos").setVisible(false);	
		grid.getColumnExt("Cliente").setVisible(false);	
		grid.getColumnExt("Por Cobrar").setVisible(false);	
		grid.getColumnExt("Precio x TON").setVisible(false);
		grid.getColumnExt("Comentario (Com)").setVisible(false);
		grid.getColumnExt("Arribo").setVisible(false);
		grid.getColumnExt("Recepción").setVisible(false);
		grid.getColumnExt("Recibió").setVisible(false);
		grid.getColumnExt("Comentario").setVisible(false);
		
		grid.getColumnExt("Precio x TON").setCellRenderer(Renderers.buildBoldDecimalRenderer(0));
		//grid.getColumnExt("Revisión").setCellRenderer(Renderers.buildBoldDecimalRenderer(0));
		grid.getColumnExt("Kgr").setCellRenderer(Renderers.buildBoldDecimalRenderer(3));
		grid.getColumnExt("Comision (%)").setCellRenderer(Renderers.buildBoldDecimalRenderer(2));
		grid.getColumnExt("Comision ($)").setCellRenderer(Renderers.buildBoldDecimalRenderer(2));
		
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
	protected List<Entrega> findData() {
		if(pendientesBox.isSelected()){
			String hql="from Entrega  e left join fetch e.embarque t  where e.fechaComision is null and date(t.regreso) between ? and ?";
			Object[] values={periodo.getFechaInicial(),periodo.getFechaFinal()};
			return getHibernateTemplate().find(hql,values);
		}else{
			String hql="from Entrega  e left join fetch e.embarque t  where  date(t.regreso) between ? and ? ";
			Object[] values={periodo.getFechaInicial(),periodo.getFechaFinal()};
			return getHibernateTemplate().find(hql,values);
		}
		
	}
	
	
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()				
				,getViewAction()
				,addAction("", "aplicarComision", "Aplicar comisión")
				,addAction("","clientesPorTonelada","Clientes X Tonelada")
				,addAction("","mostrarFactura","Ver Factura")
				,addAction("","autorizarAsignacion","Autorizar asignación")
				,addAction("","reimprimirFactura","Imprimir Factura")
				};
		return actions;
	}
	
	
	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("","reporteDeComisiones", "Reporte de Comisiones"));
		procesos.add(addAction("","reporteDeComisionesFac", "Reporte de Com. Facturista"));
		procesos.add(addAction("","reporteAnalisisDeEmbarques", "Reporte de Analisis"));
		return procesos;
	}

	public void aplicarComision(){
		if(!selectionModel.isSelectionEmpty()){
			ComisionPorTrasladoFormModel model=new ComisionPorTrasladoFormModel();
			ComisionPorTrasladoForm form=new ComisionPorTrasladoForm(model);
			form.setPorFactura(true);
			form.open();
			if(!form.hasBeenCanceled()){
				//List<Entrega> entregas=new ArrayList<Entrega>(getSelected());
				//System.out.println("Entregas a procesar: "+entregas.size()+ "time: "+System.currentTimeMillis());
				System.out.println("Entregas a procesar: "+getSelected().size()+ "time: "+System.currentTimeMillis());
				//for(Entrega e:entregas){					
				for(Object o:getSelected()){
					Entrega e=(Entrega)o;
					int index=source.indexOf(e);					
					if(index!=-1){
						if(e.isParcial()){
							//System.out.println("Procesando entrega parcial: "+e.getDocumento()+" Time: "+System.currentTimeMillis());
							e=getEntregaParcial(e);
							//System.out.println("Terminó    entrega parcial: "+e.getDocumento()+" Time: "+System.currentTimeMillis());
						}
						else{
							//System.out.println("Procesando entrega: "+e.getDocumento()+" Time: "+System.currentTimeMillis());
							e=getEntregaNormal(e);
							//System.out.println("Terminó    entrega: "+e.getDocumento()+" Time: "+System.currentTimeMillis());
						}
						ClientePorTonelada ct=comisionEspecial(e);
						if(ct!=null){
							e.setComision(0);
							e.setComentarioComision(model.getTraslado().getComentario());
							e.setFechaComision(new Date());
							e.setReplicado(null);							
							e.setComisionPorTonelada(ct.getPrecio());
							e.actualizarComision();
							e=save(e);
							source.set(index, e);
						}
						else{
							e.setComision(model.getTraslado().getComision());
							e.setComentarioComision(model.getTraslado().getComentario());
							e.setFechaComision(new Date());
							e.setReplicado(null);
							e.actualziarValor();
							e.actualizarComision();
							e=save(e);
							source.set(index, e);
						}
						if(e!=null){
							
						}						
					}
				}
				//selectionModel.clearSelection();
			}
		}
	}
	
	
	
	private ClientePorTonelada comisionEspecial(final Entrega e){
		String hql="from ClientePorTonelada c where c.cliente.id=?";
		List<ClientePorTonelada> data=getHibernateTemplate().find(hql,e.getCliente().getId());
		return data.isEmpty()?null:data.get(0);
	}
	
	
	public void clientesPorTonelada(){
		ClientesPorToneladaBrowser.openDialog();
	}
	
	
	public void mostrarFactura(){
		mostrarFactura(getSelectedObject());
	}
	
	private void mostrarFactura(Object obj){
		if(obj!=null){
			Entrega e=(Entrega)obj;
			FacturaForm.show(e.getFactura().getId());
		}
	}
	
	public void autorizarAsignacion(){
		AutorizacionDeAsignacionForm.autorizar();
	}
	
	@Override
	protected void doSelect(Object bean) {
		mostrarFactura(getSelectedObject());
	}

	public void reimprimirFactura(){
		Entrega e=(Entrega)getSelectedObject();
		if(e!=null){
			final Map parameters=new HashMap();
			String total=ImporteALetra.aLetra(e.getFactura().getTotalCM());
			parameters.put("CARGO_ID", String.valueOf(e.getFactura().getId()));
			parameters.put("IMP_CON_LETRA", total);
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/FacturaCopia.jasper"), parameters);
		}
		
		
	}
	
	public void reporteDeComisiones(){
		ComisionPorChofer.run();
	}
		
	public void reporteDeComisionesFac(){
		ComisionPorFacturista.run();
	}
	
	public void reporteAnalisisDeEmbarques(){
		AnalisisDeEmbarqueForm.run();
	}
	private Entrega save(Entrega e){
		
		return (Entrega)getHibernateTemplate().merge(e);
	}
	
	private Entrega getEntregaParcial(final Entrega e){
		Entrega res=(Entrega)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				Entrega ee=(Entrega)session.get(Entrega.class, e.getId());
				getHibernateTemplate().initialize(ee.getFactura());
				getHibernateTemplate().initialize(ee.getFactura().getPartidas());
				getHibernateTemplate().initialize(ee.getPartidas());
				//ee.getFactura().getPartidas().iterator().next();//.size();
				return ee;
			}
			
		});
		return res;
		/*
		List<Entrega> res=getHibernateTemplate().find(
				"from Entrega e " +
				" left join fetch e.factura f " +
				//" left join fetch f.partidas det" +
				" where e.id=?",e.getId());
		return res.get(0);*/
	}
	
	private Entrega getEntregaNormal(final Entrega e){
		String hql="from Entrega e" +
				" left join fetch e.factura f" +
				" left join fetch f.partidas det" +
				" where e.id=?";
		return (Entrega)getHibernateTemplate().find(hql,e.getId()).get(0);
		/*
		Entrega res=(Entrega)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				Entrega ee=(Entrega)session.get(Entrega.class, e.getId());
				ee.getFactura().getPartidas().size();
				return ee;
			}
			
		});
		return res;*/
	}
	
	private HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
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
		private JLabel total11=new JLabel();
		private JLabel total2=new JLabel();
		private JLabel total22=new JLabel();
		private JLabel total3=new JLabel();
		
		private JLabel total4=new JLabel();
		private JLabel total5=new JLabel();
		private JLabel total6=new JLabel();
		
		private JLabel total7=new JLabel();
		private JLabel total8=new JLabel();
		private JLabel total9=new JLabel();
		
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total11.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			total22.setHorizontalAlignment(SwingConstants.RIGHT);
			total3.setHorizontalAlignment(SwingConstants.RIGHT);
			
			total4.setHorizontalAlignment(SwingConstants.RIGHT);
			total5.setHorizontalAlignment(SwingConstants.RIGHT);
			total6.setHorizontalAlignment(SwingConstants.RIGHT);
			
			total7.setHorizontalAlignment(SwingConstants.RIGHT);
			total8.setHorizontalAlignment(SwingConstants.RIGHT);
			total9.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.appendSeparator("Comisión ");
			builder.append("Contado (Normal)",total2);	
			builder.append("Contado (Ton)",total22);
			builder.append("Crédito (Normal)",total1);
			builder.append("Crédito (Ton)",total11);
			builder.append("Total",total3);
			
			builder.appendSeparator("Kilos");
			builder.append("Contado",total5);	
			builder.append("Crédito",total4);
			builder.append("Total",total6);
			
			builder.appendSeparator("Valor");
			builder.append("Contado",total8);
			builder.append("Crédito",total7);
			builder.append("Total",total9);
			ActionLabel actualizar=new ActionLabel("Actualizar");
			actualizar.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					updateTotales();
				}
				
			});
			builder.append(actualizar);
			
			builder.getPanel().setOpaque(false);
			//getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		public void updateTotales(){
			
			double comisionCredito=0;
			double comisionCreditoTonelada=0;
			double comisionContado=0;
			double comisionContadoTonelada=0;
			
			double kilosCredito=0;
			double kilosContado=0;
			
			
			double valorCredito=0;
			double valorContado=0;
			
			
			for(Object obj:getFilteredSource()){
				Entrega a=(Entrega)obj;
				if(a.getOrigen().equals("CRE")){
					if(a.getComision()>0){
						comisionCredito+=a.getImporteComision().doubleValue();
						
					}else{
						comisionCreditoTonelada+=a.getImporteComision().doubleValue();
					}
					kilosCredito+=a.getKilos();
					valorCredito+=a.getValor().doubleValue();
				}
				else{
					if(a.getComision()>0){
						comisionContado+=a.getImporteComision().doubleValue();
					}else{
						comisionContadoTonelada+=a.getImporteComision().doubleValue();
					}
					
					kilosContado+=a.getKilos();
					valorContado+=a.getValor().doubleValue();					
				}
			}
			
			total1.setText(nf.format(comisionCredito));
			total11.setText(nf.format(comisionCreditoTonelada));
			total2.setText(nf.format(comisionContado));
			total22.setText(nf.format(comisionContadoTonelada));
			total3.setText(nf.format(comisionCredito+comisionCreditoTonelada+comisionContado));
			
			total4.setText(nf.format(kilosCredito));
			total5.setText(nf.format(kilosContado));
			total6.setText(nf.format(kilosCredito+kilosContado));
			
			total7.setText(nf.format(valorCredito));
			total8.setText(nf.format(valorContado));
			total9.setText(nf.format(valorCredito+valorContado));
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
	}
	
	

}
