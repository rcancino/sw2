package com.luxsoft.siipap.inventario.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.jdesktop.swingx.VerticalLayout;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.ExistenciaGlobal;
import com.luxsoft.siipap.model.Sucursal;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;


/**
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@SuppressWarnings("serial")
public class ExistenciaGeneralForm extends AbstractForm {
	
	

	public ExistenciaGeneralForm(final IFormModel model) {
		super(model);
		setTitle(model.getValue("clave").toString());
	}
	

	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header(model.getValue("descripcion").toString(),"Existencia por almacenes");
		}
		return header.getHeader();
	}
	

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				" p,2dlu,120dlu:g(.5), 3dlu" +
				",p,2dlu,120dlu:g(.5),3dlu" +
				",20dlu "
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Existencia global",addReadOnly("cantidad"));
		panel.add(builder.getPanel());
		//panel.add(buildToolbarPanel());
		panel.add(buildGridPanel());
		return panel;
	}
	
	protected void ajustarActions(JPanel panel){
		getOKAction().putValue(Action.NAME, "Salvar [F10]");
		getOKAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/edit/save_edit.gif"));
		getCancelAction().putValue(Action.NAME, "Cancelar");
		ComponentUtils.addAction(panel, new AbstractAction(){			
			public void actionPerformed(ActionEvent e) {
				if(getOKAction().isEnabled())
					getOKAction().actionPerformed(null);
			}
		}, 
		KeyStroke.getKeyStroke("F10"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		ComponentUtils.addInsertAction(panel, getInsertAction());
	}
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		return null;
	}
	
	
	protected JPanel buildToolbarPanel(){
		JButton buttons[]={
				new JButton(getImprimirAction())
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	private Action imprimirAction;
	public Action getImprimirAction(){
		if(imprimirAction==null){
			imprimirAction=CommandUtils.createPrintAction(this, "imprimir");
			imprimirAction.putValue(Action.NAME, "Imprimir [F12]");
		}
		return imprimirAction;
	}
	
	
	private JTable grid;
	
	private EventSelectionModel selectionModel;
	
	private EventList partidasList=new BasicEventList();
	
	public void cargarExistencias(){
		String hql="from Existencia e where e.clave=? and e.year=? and e.mes=?";
		Object[] params={model.getValue("clave"),model.getValue("year"),model.getValue("mes")};
		List res=ServiceLocator2.getHibernateTemplate().find(hql, params);
		partidasList.clear();
		partidasList.addAll(res);
		for(Object row:partidasList){
			Existencia ee=(Existencia)row;
			ee.setPendientes(calcularPendientes(ee));
		}
		
		//Cargar la maquila
		String maquilaSql=SQLUtils.loadSQLQueryFromResource("sql/inventarios/existenciaGeneralMaquila.sql");
		List maqs=ServiceLocator2.getJdbcTemplate().query(maquilaSql, new Object[]{model.getValue("clave"),model.getValue("clave")}
			, new BeanPropertyRowMapper(Existencia.class));
		partidasList.addAll(maqs);
		
		//Cargar las pendientes importacion
		Existencia oficinas=(Existencia)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				final String hql2="from CompraUnitaria d where d.clave=? and d.sucursal.id=1 and d.depurado=0";
				ScrollableResults rs=session.createQuery(hql2)
						.setParameter(0, model.getValue("clave")).scroll();
				double pendiente=0;
				
				while(rs.next()){
					CompraUnitaria bean=(CompraUnitaria)rs.get()[0];
					pendiente+=bean.getPendiente();
				}
				if(pendiente>0){
					Sucursal ofi=(Sucursal)session.get(Sucursal.class, 1L);
					Existencia exis=new Existencia();
					exis.setSucursal(ofi);
					exis.setPendientes(pendiente);
					return exis;
				}
				return null;
			}
		});
		if(oficinas!=null){
			partidasList.add(oficinas);
		}
	}
	
	Double calcularPendientes(final Existencia exis){
		return (Double)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				final String hql2="from CompraUnitaria d where d.clave=? and d.sucursal.id=? and d.depurado=0";
				ScrollableResults rs=session.createQuery(hql2)
						.setParameter(0, exis.getClave())
						.setParameter(1, exis.getSucursal().getId())
						.scroll();
				double pendiente=0;
				
				while(rs.next()){
					CompraUnitaria bean=(CompraUnitaria)rs.get()[0];
					pendiente+=bean.getPendiente();
				}
				return pendiente;
			}
		});
	}
	
	List buscarPendientes(final Existencia exis){
		final String clave=model.getValue("clave").toString();
		final Long sucursalId=exis.getSucursal().getId();
		return ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				final String hql2="from CompraUnitaria d where d.clave=? and d.sucursal.id=? and d.depurado=0 order by d.compra.fecha asc";
				ScrollableResults rs=session.createQuery(hql2)
						.setParameter(0, clave)
						.setParameter(1, sucursalId)
						.scroll();
				List res=new ArrayList();
				while(rs.next()){
					CompraUnitaria bean=(CompraUnitaria)rs.get()[0];
					if(bean.getPendiente()>0)
						res.add(bean);
				}
				return res;
			}
		});
	}
	
	
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={
				"almacen"
				,"disponible"
				,"pendientes"
				,"recorte"
				,"recorteComentario"
				,"modificado"
				};
		String[] columnLabels={
				"Sucursal"
				,"Disponible"
				,"Pendientes"
				,"Recorte"
				,"Rec comentario"
				,"Modificado"
				};
		
		final TableFormat tf=GlazedLists.tableFormat(Existencia.class,propertyNames, columnLabels);
		SortedList sorted=new SortedList(partidasList,null);
		final EventTableModel tm=new EventTableModel(sorted,tf);
		grid=new JTable(tm);
		selectionModel=new EventSelectionModel(sorted);
		grid.setSelectionModel(selectionModel);
		TableComparatorChooser.install(grid, sorted, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					view();
			}			
		});
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);
		gridComponent.setPreferredSize(new Dimension(750,300));
		return gridComponent;
	}
	
	public void view(){
		if(!selectionModel.isSelectionEmpty()){
			Existencia res=(Existencia)selectionModel.getSelected().get(0);
			if(res!=null){
				ComprasPendientesForm form=new ComprasPendientesForm();
				form.getPartidasList().addAll(buscarPendientes(res));
				form.open();
			}
			
		}
	}
	
	public static void showForm(ExistenciaGlobal source){
		ExistenciaGlobal target=(ExistenciaGlobal)Bean.proxy(ExistenciaGlobal.class);
		BeanUtils.copyProperties(source, target);
		DefaultFormModel model=new DefaultFormModel(target, true);
		ExistenciaGeneralForm form=new ExistenciaGeneralForm(model);
		form.cargarExistencias();
		form.open();
	}
	
	public static void showForm(String clave,int year,int mes){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/inventarios/existenciaGeneralBean.sql");
		Object source=ServiceLocator2.getJdbcTemplate().queryForObject(sql, new Object[]{clave,year,mes}
		, new BeanPropertyRowMapper(ExistenciaGlobal.class));
		if(source!=null){
			ExistenciaGlobal target=(ExistenciaGlobal)Bean.proxy(ExistenciaGlobal.class);
			BeanUtils.copyProperties(source, target);
			DefaultFormModel model=new DefaultFormModel(target, true);
			ExistenciaGeneralForm form=new ExistenciaGeneralForm(model);
			form.cargarExistencias();
			form.open();
		}else{
			MessageUtils.showMessage("No localizo existencia ", "");
		}
	}
	
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//showForm("PEGB4",2013,10);
				//System.exit(0);
				SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy HH:mm");
				try {
					Date fechaInicial=format.parse("19/09/2013 12:10");
					Date fechaFinal=format.parse("30/09/2013 19:36");
					
					System.out.println(fechaInicial);
					System.out.println(fechaFinal);
					//DateFormatUtils.format(fechaFinal.getTime()-fechaInicial.getTime(), "");
					String res=DurationFormatUtils.formatDuration(fechaFinal.getTime()-fechaInicial.getTime(),"d' dias 'H' hrs 'm' min ");
					System.out.println(res);
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}

		});
	}

	
	

}
