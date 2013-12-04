package com.luxsoft.siipap.tesoreria.movimientos;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;

public class EstadoDeCuentaPanel extends FilteredBrowserPanel<CargoAbono>{
	
	private final EstadoDeCuentaModel model;
	private JPanel resumenPanel;
	private JLabel saldoFinal;
	private JLabel cargos;
	private JLabel abonos;

	@SuppressWarnings("unchecked")
	public EstadoDeCuentaPanel() {
		super(CargoAbono.class);
		this.model=new EstadoDeCuentaModel();
		//model.addBeanPropertyChangeListener(new EstadoHandler());
	}
	
	protected void init(){
		addProperty("id","cuenta.banco.clave","cuenta.numero"
				,"fecha","concep"
				,"descripcion"
				,"deposito","retiro"
				,"moneda","tc"
				,"origen.name"
				,"referencia"				
				,"comentario","aplicacionDeTarjeta");
		addLabels("id","Banco","Cuenta","Fecha","Concepto"
				,"Descripcion","Deposito","Retiro","Mon","T.C"
				,"Origen"
				,"Referencia"				
				,"Comentario","Apl Tar");
		installTextComponentMatcherEditor("Concepto", "concep");
		installTextComponentMatcherEditor("Descripción", "descripcion");
		installTextComponentMatcherEditor("Importe", "importe");
		installTextComponentMatcherEditor("Referencia", "referencia");
		installTextComponentMatcherEditor("Origen", "origen.name");
		
	}
	
	protected void installFilters(final DefaultFormBuilder builder){
		super.installFilters(builder);
		builder.appendSeparator("Para Reporte");
		builder.append("Cuenta",Bindings.createCuentasBinding(model.getModel("cuenta")));
		builder.append("Fecha Ini",Binder.createDateComponent(model.getModel("fechaInicial")));
		builder.append("Fecha Fin",Binder.createDateComponent(model.getModel("fechaFinal")));
		
		
	}

	
	
	public void load(){
		if(model.isValid()){
			super.load();
		}else{
			MessageUtils.showMessage("Debe seleccionar los parametros adecuados", "Estado de cuenta");
		}
	}

	@Override
	protected List<CargoAbono> findData() {
		model.generarEstado();
		updateHeader();
		updateEstado();
		return model.getEstadoDeCuenta().getMovimientos();
	}
	

	@Override
	protected JComponent buildContent() {
		final JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				buildCuentaPanel(),
				super.buildContent());
		//sp.setResizeWeight(.3);
		return sp;
	}
	
	private Header header;
	
	private JComponent buildCuentaPanel(){		
		header=new Header("Estado de Cuenta","");
		header.setDescRows(5);
		return header.getHeader();
	}
	
	@SuppressWarnings("unchecked")
	public Action[] getActions(){
		if(actions==null)
			actions=new Action[]{getLoadAction(),getViewAction(),getActionReport()};
		return actions;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public Action getLoadAction(){
		if(loadAction==null){
			loadAction=super.getLoadAction();
			loadAction.putValue(Action.NAME, "Generar Estado");
		}
		return loadAction;
	}
	
	public Action getActionReport(){
		AbstractAction a=new AbstractAction(){

			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Cuenta"+model.getValue("cuenta"));
				if(model.getEstadoDeCuenta().getFechaInicial().equals(null) && model.getEstadoDeCuenta().getFechaFinal().equals(null)){
					MessageUtils.showMessage("Debe Capturar Fechas para ejecutar el Reporte", "Message");
				}
				if(!model.getEstadoDeCuenta().getFechaInicial().equals(null) && !model.getEstadoDeCuenta().getFechaFinal().equals(null)){
					showReport sr=new showReport();
					sr.open();
				}
				
			}
			
		};
		a.putValue(Action.NAME, "Imprimir reporte");
		return a;
	}
	
	

	
	private class showReport extends SXAbstractDialog{
		
		public showReport() {
			super("Reporte...");
		}

		public JComponent displayReport(){
			Map<String, Object>parametros=new HashMap<String, Object>();
			  parametros.put("CUENTA",model.getEstadoDeCuenta().getCuenta().getNumero().toString()+"    "+model.getEstadoDeCuenta().getCuenta().getBanco().getNombre().toString());
			  parametros.put("FECHA_INI",model.getEstadoDeCuenta().getFechaInicial());
			  parametros.put("FECHA_FIN",model.getEstadoDeCuenta().getFechaFinal());
			  parametros.put("INICIAL",model.getEstadoDeCuenta().getSaldoInicial());
			  parametros.put("CARGOS",model.getEstadoDeCuenta().getCargos());
			  parametros.put("ABONOS",model.getEstadoDeCuenta().getAbonos());
			  parametros.put("SALDO",model.getEstadoDeCuenta().getSaldoFinal());
                net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
                DefaultResourceLoader loader = new DefaultResourceLoader();
                Resource res = loader.getResource(ReportUtils.toReportesPath("Tesoreria/EstadoDecuenta.jasper"));
                try
                {
                    java.io.InputStream io = res.getInputStream();
                    try
                    {
                    	JTable table=getGrid();
                        jasperPrint = JasperFillManager.fillReport(io, parametros, new JRTableModelDataSource(table.getModel()));
                    }
                    catch(JRException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch(IOException ioe)
                {
                    ioe.printStackTrace();
                }
                JRViewer jasperViewer = new JRViewer(jasperPrint);
                jasperViewer.setPreferredSize(new Dimension(1000, 600));
                return jasperViewer;

			}

		@Override
		protected JComponent buildContent() {
			return displayReport();
		}

		@Override
		protected void setResizable() {
		setResizable(true);
		}
		
	}
	
	
	
	private SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
	
	private void updateHeader(){
		String pattern="Periodo:\t {0} al {1}\nInicial:\t {2}\nCargos:\t {3}\nAbonos\t {4}\nSaldo:\t {5}";
		String msg=MessageFormat.format(pattern
				, df.format(model.getEstadoDeCuenta().getFechaInicial())
				, df.format(model.getEstadoDeCuenta().getFechaFinal())
				,model.getEstadoDeCuenta().getSaldoInicial()
				,model.getEstadoDeCuenta().getCargos()
				,model.getEstadoDeCuenta().getAbonos()
				,model.getEstadoDeCuenta().getSaldoFinal()
				);
		String t="Cuenta: {0}  {1}  {2}  ({3})";
		NumberFormat nf=NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		header.setTitulo(MessageFormat.format(t
				,nf.format(model.getEstadoDeCuenta().getCuenta().getNumero())				
				,model.getEstadoDeCuenta().getCuenta().getBanco().getNombre()				
				,model.getEstadoDeCuenta().getCuenta().getClave()
				,model.getEstadoDeCuenta().getCuenta().getMoneda()
				));
		header.setDescripcion(msg);
	}
	
	
	
	public JPanel getTotalesPanel(){
		if(resumenPanel==null){
			FormLayout layout=new FormLayout("p,2dlu,r:max(100dlu;p)","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.getPanel().setOpaque(false);
			final NumberFormat nf=NumberFormat.getInstance();
			saldoFinal=new JLabel();
			cargos=new JLabel();
			abonos=new JLabel();
			builder.append("Saldo Inicial",BasicComponentFactory.createLabel(model.getModel("saldoInicial"),nf));
			builder.append("Cargos",cargos);
			builder.append("Abonos",cargos);
			builder.append("Saldo Final",saldoFinal);
			
			resumenPanel=builder.getPanel();
		}
		return resumenPanel;
	}
	
	private void updateEstado(){
		saldoFinal.setText(model.getEstadoDeCuenta().getSaldoFinal().toString());
		cargos.setText(model.getEstadoDeCuenta().getCargos().toString());
		abonos.setText(model.getEstadoDeCuenta().getAbonos().toString());
	}
	

}
