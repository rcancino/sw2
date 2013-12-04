package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model2.VentaContraEntrega;
import com.luxsoft.sw3.services.Services;

/**
 * Selector de facturas para un cliente, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeFacturasParaAsignacionDeEmbarques 
							extends AbstractSelector<VentaContraEntrega>{
	
	protected Periodo periodo=new Periodo(DateUtil.calcularDiaHabilAnterior(new Date(),2),new Date());
	Sucursal suc;
	
	
	public SelectorDeFacturasParaAsignacionDeEmbarques() {
		super(VentaContraEntrega.class, "Facturas");
		suc=Services.getInstance().getConfiguracion().getSucursal();
		setTitle("Facturas  por asignar    Sucursal: "+suc.getNombre());
	}
	
	@Override
	protected void installEditors(EventList<MatcherEditor<VentaContraEntrega>> editors) {
		TextFilterator textFilterator=GlazedLists.textFilterator("documento");
		TextComponentMatcherEditor e1=new TextComponentMatcherEditor(documentoField,textFilterator);
		
		TextFilterator textFilterator2=GlazedLists.textFilterator("nombre");
		TextComponentMatcherEditor e3=new TextComponentMatcherEditor(nombreField,textFilterator2);
				
		editors.add(e1);
		editors.add(e3);
		//Matcher canceladosMatcher=Matchers.beanPropertyMatcher(VentaContraEntrega.class, "cancelado", false);
		//editors.add(GlazedLists.fixedMatcherEditor(canceladosMatcher));
	}
	
	private JTextField documentoField=new JTextField(10);
	private JTextField nombreField=new JTextField(10);
	
	protected JComponent buildFilterPanel(){
		FormLayout layout=new FormLayout(
				"p,2dlu,p,2dlu," +
				"p,2dlu,p,2dlu," +
				"p,2dlu,p" +
				"","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Documento",documentoField);
		builder.append("Cliente",nombreField);
		return builder.getPanel();
	}

	@Override
	protected TableFormat<VentaContraEntrega> getTableFormat() {
		String props[]={				
				"nombre"
				,"documento"
				,"pedido"
				,"fecha"
				,"origen"
				,"total"
				,"contraEntrega"
				,"fpago"
				,"facturado"
				,"instruccion"
				,"facturista"
				};
		String labels[]={				
				"Cliente"
				,"Factura"
				,"Pedido"
				,"Fecha"
				,"Origen"
				,"Total"
				,"CE"
				,"F.pago"
				,"Facturado"
				,"Instruccion"
				,"Facturista"
				};
		return GlazedLists.tableFormat(VentaContraEntrega.class,props,labels);
	}
	
	private HeaderPanel header;
	
	protected JComponent buildHeader(){		
		header=new HeaderPanel("Facturas pendientes de asignación. "+suc.getNombre(),periodo.toString());
		return header;
	}
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	private ActionLabel periodoLabel;
	
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){			
			periodoLabel=new ActionLabel("Periodo: "+periodo.toString());
			periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	
	public void cambiarPeriodo(){
		ValueHolder holder=new ValueHolder(periodo);
		AbstractDialog dialog=Binder.createPeriodoSelector(holder);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=(Periodo)holder.getValue();			
			load();
			updatePeriodoLabel();
		}
	}
	
	@Override
	protected void setPreferedDimension(JComponent gridComponent) {
		gridComponent.setPreferredSize(new Dimension(780,400));
	}

	@Override
	protected void onWindowOpened() {
		load();
	}

	protected void updatePeriodoLabel(){
		header.setDescription("Periodo: "+periodo.toString());
	}

	@Override
	protected List<VentaContraEntrega> getData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/SelectorDeFacturasPorAsignar.sql");
		return Services.getInstance().getJdbcTemplate().query(
				sql
				,new Object[]{
						new SqlParameterValue(Types.DATE,periodo.getFechaInicial()),
						new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
						}
				, new BeanPropertyRowMapper(VentaContraEntrega.class));
		
	}
	
	public void clean(){		
		source.clear();
	}
	
	/**
	 * Regresa una y solo una venta seleccionada, pero completamente inicializada
	 * 
	 * @return
	 */
	public static Venta seleccionar(){
		SelectorDeFacturasParaAsignacionDeEmbarques selector=new SelectorDeFacturasParaAsignacionDeEmbarques();
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			VentaContraEntrega v=selector.getSelected();
			return Services.getInstance()
					.getFacturasManager()
					.getVentaDao()
					.buscarVentaInicializada(v.getId());
		}		
		return null;
	}
	
	
	/**
	public static buscarVenta(final Cliente c){
		
	}
	**/

	public static void main(String[] args) {
		SWExtUIManager.setup();
		SwingUtilities.invokeLater(new Runnable(){
			 
			public void run() {
				seleccionar();
				
				System.exit(0);
			}
			
		});
		
	}

}
