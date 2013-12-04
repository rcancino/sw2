package com.luxsoft.siipap.inventario.ui.consultas;

import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.inventario.InventariosActions;
import com.luxsoft.siipap.inventario.ui.forms.ExistenciaGeneralForm;
import com.luxsoft.siipap.inventario.ui.reports.InventarioCosteadoReportForm;
import com.luxsoft.siipap.inventarios.model.ExistenciaGlobal;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.SQLUtils;

/**
 * Consulta de disponiblidad de inventarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DisponibilidadPanel extends FilteredBrowserPanel<ExistenciaGlobal> implements PropertyChangeListener{

	public DisponibilidadPanel() {
		super(ExistenciaGlobal.class);
		setTitle("Existencias globales");
	}
	
	private HeaderPanel header;
	
	
	
	@Override
	protected JComponent buildHeader(){
		if(header==null){
			header=new HeaderPanel("Existencias globales ","Año:  Mes:");
			updateHeader();
		}
		return header;
	}
	
	private void updateHeader(){
		if(header!=null)
			header.setDescription(df.format(periodo.getFechaFinal()));
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		updateHeader();
	}



	protected void init(){
		Date fecha=new Date();
		yearModel=new ValueHolder(Periodo.obtenerYear(fecha));
		yearModel.addValueChangeListener(this);
		mesModel=new ValueHolder(Periodo.obtenerMes(fecha)+1);
		mesModel.addValueChangeListener(this);
		updateHeader();
		addProperty(
				"clave"
				,"descripcion"
				,"cantidad"
				,"recorte"
				,"linea"
				,"marca"
				,"clase"
				,"tipo"
				);
		addLabels(
				"Producto"
				,"Descripción"
				,"Existencia"
				,"Recorte"
				,"Línea"
				,"Marca"
				,"Clase"
				,"Tipo"
				);
		installTextComponentMatcherEditor("Producto", "clave","descripcion");
		installTextComponentMatcherEditor("Linea", "linea");
		installTextComponentMatcherEditor("Marca", "marca");
		installTextComponentMatcherEditor("Clase", "clase");
		installTextComponentMatcherEditor("Existencia", "cantidad");
		installTextComponentMatcherEditor("Tipo", "tipo");
		manejarPeriodo();
	}
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteDeKardex", "Kardex")
				//,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteInventarioCosteado", "Rep Inv. Costeado")
				};
		return actions;
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.getColumnExt("Línea").setMaxWidth(150);
		grid.getColumnExt("Marca").setMaxWidth(100);
		grid.getColumnExt("Clase").setMaxWidth(100);
		grid.getColumnExt("Descripción").setMaxWidth(400);
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorkerInDialog(worker,"Cargando existencias","Cargando existencias");
	}
	

	@Override
	protected List<ExistenciaGlobal> findData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/inventarios/existencias_generales.sql");
		List res=ServiceLocator2.getJdbcTemplate().query(sql, new Object[]{getYear(),getMes()}, new BeanPropertyRowMapper(ExistenciaGlobal.class));
		//Maquila
		String maquilaSql=SQLUtils.loadSQLQueryFromResource("sql/inventarios/existencias_generales_maquila.sql");
		List maquilaList=ServiceLocator2.getJdbcTemplate().query(maquilaSql,  new BeanPropertyRowMapper(ExistenciaGlobal.class));
		res.addAll(maquilaList);
		return res;
	}
	
	public void reporteInventarioCosteado(){
		InventarioCosteadoReportForm.run();
	}
	
	
	@Override
	protected void manejarPeriodo() {
		periodo=Periodo.getPeriodoDelMesActual();
		
	}
	private DateFormat df=new SimpleDateFormat("MMMM - yyyy");
	
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){
			manejarPeriodo();
			periodoLabel=new ActionLabel("Periodo: "+df.format(periodo.getFechaFinal()));
			//periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	
	public void cambiarPeriodo(){
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=Periodo.getPeriodoEnUnMes(getMes(), getYear());
			load();
			updatePeriodoLabel();
		}
	}
	
	protected void updatePeriodoLabel(){
		periodoLabel.setText(df.format(periodo.getFechaFinal()));
	}
	
	private ValueModel yearModel;
	
	private ValueModel mesModel;

	
	public Integer getMes(){
		return (Integer)mesModel.getValue();
	}
	public Integer getYear(){
		return (Integer)yearModel.getValue();
	}
	
	@Override
	protected void doSelect(Object bean) {
		ExistenciaGlobal e=(ExistenciaGlobal)bean;
		//ExistenciaGeneralForm.showForm(e.getClave(), e.getYear(), e.getMes());
		ExistenciaGeneralForm.showForm(e);
	}

}
