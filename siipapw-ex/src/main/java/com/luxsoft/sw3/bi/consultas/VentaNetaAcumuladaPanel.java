package com.luxsoft.sw3.bi.consultas;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.VerticalLayout;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;

import com.luxsoft.siipap.security.CancelacionDeCargoForm;
import com.luxsoft.siipap.security.CancelacionDeCargoFormModel;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model.Venta;

import com.luxsoft.sw3.bi.model.VentaNetaAcumuladalRow;
import com.luxsoft.sw3.bi.model.VentaNetaMensualRow;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.ventas.Pedido;

public class VentaNetaAcumuladaPanel extends FilteredBrowserPanel<VentaNetaAcumuladalRow>{
	
	
	private Date fecha;
	public static String seleccionado;
	public static String seleccionTipo;
	public static String fechaInicial;
	public static String fechaFinal;
	public static int yearStr;
	public static int mesStr;
	public static String descripcionPanel;
	
	JTextField descripcionField;
	JTextField tipoField;

	public VentaNetaAcumuladaPanel() {
		super(VentaNetaAcumuladalRow.class);
	}
	
	protected void init(){
		String[] props=new String[]{"periodo","origenId","descripcion","ventaNeta","costo","importeUtilidad","porcentajeUtilidad","porcentajeAportacion","kilos","precio_kilos","nacional"};
		String[] names=new String[]{"Periodo","OrigenId","Descripcion","Vta. Neta","Costo","Imp. Ut.","% Ut.","% Aport.","Kilos","Precio/K","Nal"};
		addProperty(props);
		addLabels(names);
		manejarPeriodo();
		descripcionField=new JTextField();
		tipoField=new JTextField();
		installTextComponentMatcherEditor("Descripcion",descripcionField, "descripcion");
		installTextComponentMatcherEditor("Tipo Prod.",tipoField, "nacional");
		
		
	}

	@Override
	protected List<VentaNetaAcumuladalRow> findData() {
		
		
		String sql3=ventaNetaAcumulada();
		return ServiceLocator2.getJdbcTemplate().query(sql3, new BeanPropertyRowMapper(VentaNetaAcumuladalRow.class));
		
	}

	public void buscar(){
		if(periodo==null)
			periodo=Periodo.getPeriodoDelMesActual();
		cambiarPeriodo();
	}
	
	
	protected void manejarPeriodo(){
		//periodo=Periodo.getPeriodoEnUnMes(-1);
		periodo=Periodo.periodoDeloquevaDelMes();
			
	}
	
	
public void cambiarPeriodo(){
		
		ValueHolder yearModel=new ValueHolder(Periodo.obtenerYear(periodo.getFechaInicial()));
		ValueHolder mesModel=new ValueHolder(Periodo.obtenerMes(periodo.getFechaFinal()));
		
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			int year=(Integer)yearModel.getValue();
			int mes=(Integer)mesModel.getValue();
			periodo=Periodo.getPeriodoEnUnMes(mes-1, year);
			
			DateFormat df=new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			DateFormat df2=new SimpleDateFormat("yyyy-MM-dd 23:00:00");
			fechaInicial=df.format(periodo.getFechaInicial());
			fechaFinal=df2.format(periodo.getFechaFinal());
			Calendar fecha=Calendar.getInstance();
			fecha.setTime(periodo.getFechaInicial());
			yearStr= year; //fecha.get(Calendar.YEAR);
			mesStr= mes; //fecha.get(Calendar.MONTH);
			updatePeriodoLabel();
			//nuevoPeriodo(periodo);
			load();
		}
	}
	
public JPanel getFilterPanel() {
	if(filterPanel==null){
			
		
		filterPanel=new JPanel(new VerticalLayout());
					
		filterPanel.add(getFilterPanelBuilder().getPanel());
		
	//	filterPanel.add(ventaNetaAcumuladaBrowser.getFilterPanel());

		installFilters(filterPanelBuilder);
	}
	return filterPanel;
}

protected void installFilters(final DefaultFormBuilder builder){
	builder.appendSeparator("Para Generar Consulta");
	builder.append("Selector", createSeleComboBox());
//	builder.append("Tipo", createTipoComboBox());
	builder.append("Venta", createTipoVentaComboBox());
	builder.appendSeparator("Filtros");
	super.installFilters(builder);
	//builder.appendSeparator("Filtro Venta Neta Acumulada");
	
}


	public String ventaNetaAcumulada(){

		String sql=SQLUtils.loadSQLQueryFromResource("sql/bi/VentaNetaAcumulada.sql");
		//-------------*************----------
		DateFormat df2=new SimpleDateFormat("yyyy-MM-dd 23:00:00");	
	
		sql=sql.replaceAll("@YEAR", yearStr+"");
		sql=sql.replaceAll("@MES", mesStr+"");
		//sql=sql.replaceAll("@FECHA_INI", VentaNetaPanel.fechaInicial);
		sql=sql.replaceAll("@FECHA_FIN", df2.format(periodo.getFechaFinal()));
		
		// Seleccion de campos a pintar por el sql segun selector		
		if(seleccionado.equals("LINEA")){
			sql=sql.replaceAll("@DESCRIPCION",  "'LIN' AS TIPO,D.LINEA_ID AS origenId,D.LINEA");
			sql=sql.replaceAll("@INVENTARIO", "'LIN' AS TIPO,D.LINEA_ID AS origenId,D.LINEA");
		}
		if(seleccionado.equals("CLIENTE")){
			sql=sql.replaceAll("@DESCRIPCION",  "'EXI' AS TIPO,D.CLIENTE_ID AS origenId,D.CLIENTE");	
			sql=sql.replaceAll("@INVENTARIO", "'EXI' AS TIPO,8 AS origenId,'1 MOSTRADOR'");
		}
		if(seleccionado.equals("PRODUCTO")){
			sql=sql.replaceAll("@DESCRIPCION",  "'PRD' AS TIPO,D.PRODUCTO_ID AS origenId,CONCAT(D.CLAVE,' ',D.DESCRIPCION)");	
			sql=sql.replaceAll("@INVENTARIO", "'PRD' AS TIPO,D.PRODUCTO_ID AS origenId,CONCAT(D.CLAVE,' ',P.DESCRIPCION)");
		}
		if(seleccionado.equals("SUCURSAL")){
			sql=sql.replaceAll("@DESCRIPCION",  "'SUC' AS TIPO,D.SUCURSAL_ID AS origenId,D.SUC");
			sql=sql.replaceAll("@INVENTARIO", "'SUC' AS TIPO,D.SUCURSAL_ID AS origenId,(SELECT S.NOMBRE FROM SW_SUCURSALES S WHERE S.SUCURSAL_ID=D.SUCURSAL_ID)");
		}
		if(seleccionado.equals("VENTA")){
			sql=sql.replaceAll("@DESCRIPCION", "'EXI' AS TIPO,1 AS origenId,(CASE WHEN D.ORIGEN='CRE' THEN 'CREDITO' ELSE 'CONTADO' END)");	
			sql=sql.replaceAll("@INVENTARIO", "'EXI' AS TIPO,1 AS origenId,'CREDITO'");
		}
		if(seleccionado.equals("MES")){
			sql=sql.replaceAll("@DESCRIPCION", "'MES' AS TIPO,MONTH(D.FECHA) AS origenId,(SELECT M.MES_NOMBRE FROM sx_meses M WHERE M.MES=MONTH(D.FECHA))");
			sql=sql.replaceAll("@INVENTARIO", "'MES' AS TIPO,D.MES AS origenId,(SELECT M.MES_NOMBRE FROM sx_meses M WHERE M.MES=MONTH(D.FECHA))");
		}
		// Envio de Parametros para el sql de Tipo de Venta
		if(seleccionTipo.equals("TODOS")){
			sql=sql.replaceAll("@VENTA", "");
		}
		if(seleccionTipo.equals("CREDITO")){
			sql=sql.replaceAll("@VENTA", " AND (CASE WHEN D.ORIGEN='CRE' THEN 'CREDITO' ELSE 'CONTADO' END) IN('CREDITO') ");
		}
		if(seleccionTipo.equals("CONTADO")){
			sql=sql.replaceAll("@VENTA", " AND (CASE WHEN D.ORIGEN='CRE' THEN 'CREDITO' ELSE 'CONTADO' END) IN('CONTADO') ");
		}	
		return sql;
		
	}
	
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			
			//Action buscarAction=addAction("buscar.id","buscar", "Buscar");
			//buscarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/page_find.png"));
			List<Action> actions=ListUtils.predicatedList(new ArrayList<Action>(), PredicateUtils.notNullPredicate());
			//actions.add(buscarAction);
			actions.add(getLoadAction());
			actions.add(CommandUtils.createPrintAction(this, "imprimir"));
			this.actions=actions.toArray(new Action[actions.size()]);
		}
		return actions;
		
	}
	
	private JComboBox createSeleComboBox () {
		String[] data={"LINEA","CLIENTE","PRODUCTO","SUCURSAL","VENTA","MES"};
		final JComboBox box = new JComboBox(data);
		
			
		box.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent evt) {
				JComboBox combo = (JComboBox) evt.getSource() ;

			       seleccionado =(String) combo.getSelectedItem() ;
				
			}
		});
		if(seleccionado == null)
			seleccionado=(String) box.getSelectedItem();
		return box;
	}
	

	
	private JComboBox createTipoVentaComboBox () {
		String[] data={"TODOS","CREDITO","CONTADO"};
		final JComboBox box = new JComboBox(data);
		
			
		box.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent evt) {
				JComboBox combo = (JComboBox) evt.getSource() ;

			       seleccionTipo =(String) combo.getSelectedItem() ;
				
			}
		});
		if(seleccionTipo == null)
			seleccionTipo=(String) box.getSelectedItem();
		return box;
	}
	
	
	@Override
	protected void doSelect(Object bean) {
		/*Venta venta=(Venta)bean;
		FacturaForm.show(venta.getId());*/
	}
private Action imprimirAction;
	
	public Action getImprimirAction(){
		if(imprimirAction==null){
			imprimirAction=addAction("imp.id", "impr", "imprim");
			imprimirAction.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		}
		return imprimirAction;
	}

	
	
	public void imprimir(){
		java.util.Map map=new HashMap();
		map.put("SELECTOR",seleccionado);
		map.put("VENTA", seleccionTipo);
		map.put("FECHA_INI", periodo.getFechaInicial());
		map.put("FECHA_FIN", periodo.getFechaFinal());
		map.put("DESCRIPCION",descripcionField.getText());
		map.put("TIPO_PROD", tipoField.getText());
		map.put("FORMATO", "VENTA NETA (ACUMULADA)");
		ReportUtils.viewReport(ReportUtils.toReportesPath("bi/VentaNetaAcumulada.jasper"), map,grid.getModel());
	}
	

private TotalesPanel totalPanel=new TotalesPanel();
	
	public JPanel getTotalesPanel(){
		return (JPanel)totalPanel.getControl();
	}
	
	
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel ventaNeta=new JLabel();
		private JLabel costo=new JLabel();
		private JLabel impUt=new JLabel();
		private JLabel porcUt=new JLabel();
		private JLabel invCost=new JLabel();
		
		
		

		@Override
		protected JComponent buildContent() {
			
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout("p,1dlu,f:max(80dlu;p)","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			ventaNeta.setHorizontalAlignment(SwingConstants.RIGHT);
			costo.setHorizontalAlignment(SwingConstants.RIGHT);
			impUt.setHorizontalAlignment(SwingConstants.RIGHT);
			porcUt.setHorizontalAlignment(SwingConstants.RIGHT);
			invCost.setHorizontalAlignment(SwingConstants.RIGHT);
			
			
			builder.appendSeparator("Acumulado");
			builder.nextColumn();
			builder.append("Venta Neta",ventaNeta);
			builder.append("Costo",costo);
			builder.append("Imp. Ut.",impUt);
			builder.append("% Ut",porcUt);
			builder.append("Inv. Cost",invCost);
			panel.add(builder.getPanel(),BorderLayout.NORTH);
				
		
		
		//	panel.add(ventaNetaAcumuladaBrowser.getTotalesPanel1(),BorderLayout.SOUTH);

			
	
			
			
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			
			updateTotales();
			
			return panel;
			//return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		public void updateTotales(){
			BigDecimal ventaNeta=BigDecimal.ZERO;
			BigDecimal costo=BigDecimal.ZERO;
			BigDecimal impUt=BigDecimal.ZERO;
			BigDecimal porcUt=BigDecimal.ZERO;
			BigDecimal invCost=BigDecimal.ZERO;
			
		
					
			for(Object  r:getFilteredSource()){
				VentaNetaAcumuladalRow an=(VentaNetaAcumuladalRow)r;
				
			    
				ventaNeta=ventaNeta.add(new BigDecimal(an.getVentaNeta()));
				costo=costo.add(new BigDecimal(an.getCosto()));
				impUt=impUt.add(new BigDecimal(an.getImporteUtilidad()));
				//porcUt=porcUt.add(new BigDecimal(an.getPorcentajeUtilidad()));
				if(!(new BigDecimal(an.getImporteUtilidad()).equals(BigDecimal.ZERO)) && (ventaNeta.doubleValue()>0))
						porcUt=(impUt.divide(ventaNeta,3,RoundingMode.HALF_EVEN)).multiply(new BigDecimal(100));
				invCost=invCost.add(new BigDecimal(an.getInventarioCosteado()));
				
				
			}
			
			for(Object  r:getFilteredSource()){
				BigDecimal porcentajeAp=BigDecimal.ZERO;
				VentaNetaAcumuladalRow an=(VentaNetaAcumuladalRow)r;
				
				 if(! new BigDecimal(an.getImporteUtilidad()).equals(BigDecimal.ZERO))
				      porcentajeAp=new BigDecimal(an.getImporteUtilidad()).divide(impUt,3,RoundingMode.HALF_EVEN).multiply(new BigDecimal(100));
				 
			  an.setPorcentajeAportacion(porcentajeAp);			
			}
			String pattern="{0}";
			
			this.ventaNeta.setText(MessageFormat.format(pattern, ventaNeta));
			this.costo.setText(MessageFormat.format(pattern, costo));
			this.impUt.setText(MessageFormat.format(pattern, impUt));
			this.porcUt.setText(MessageFormat.format(pattern, porcUt));
			this.invCost.setText(MessageFormat.format(pattern, invCost));
			
		}
		
		
		
	}
	

	
	


}
