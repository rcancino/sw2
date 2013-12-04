package com.luxsoft.sw3.cfd.ui.consultas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Types;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reports.VentasDiariasBI;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.ventas.ui.consultas.ReporteMensualCFI;

/**
 * Panel de comprobantes fiscales digitales
 * 
 * @author Ruben Cancino Ramos
 * 
 */
public class ComprobanteFiscalImpresosPanel extends
		FilteredBrowserPanel<ReporteMensualCFI> {

	public ComprobanteFiscalImpresosPanel() {
		super(ReporteMensualCFI.class);
	}

	protected void init() {
		String[] props = new String[] { "FECHA", "RECEPTOR", "FOLIO", "SERIE",
				"ESTADO", "TIPO", "TIPO_CFD", "RFC", "IMPUESTO", "TOTAL",
				"ANO_APROBACION", "NO_APROBACION", "EMISOR" };
		addProperty(props);
		installTextComponentMatcherEditor("RFC", "RFC");
		installTextComponentMatcherEditor("Receptor", "Receptor");
		installTextComponentMatcherEditor("Tipo", "TIPO");
		installTextComponentMatcherEditor("Serie", "Serie");
		installTextComponentMatcherEditor("Folio", "Folio");
		installTextComponentMatcherEditor("Total", "Total");
		installTextComponentMatcherEditor("Estado", "Estado");
		manejarPeriodo();
	}

	protected void manejarPeriodo() {
		periodo = Periodo.periodoDeloquevaDelMes();
	}

	public void cambiarPeriodo() {

		ValueHolder yearModel = new ValueHolder(Periodo.obtenerYear(periodo
				.getFechaInicial()));
		ValueHolder mesModel = new ValueHolder(Periodo.obtenerMes(periodo
				.getFechaFinal()));

		AbstractDialog dialog = Binder.createSelectorMesYear(yearModel,
				mesModel);
		dialog.open();
		if (!dialog.hasBeenCanceled()) {
			int year = (Integer) yearModel.getValue();
			int mes = (Integer) mesModel.getValue();
			periodo = Periodo.getPeriodoEnUnMes(mes - 1, year);
			updatePeriodoLabel();
			// nuevoPeriodo(periodo);

		}
	}

	@Override
	public Action[] getActions() {
		if (actions == null)
			actions = new Action[] { getLoadAction() };
		return actions;
	}

	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos = super.createProccessActions();
		procesos.add(addAction("", "reporteDeVentasDiarias", "Ventas Diarias"));
		procesos.add(addAction("", "generarReporteDelSAT",
				"Reporte Mensual SAT"));
		return procesos;
	}

	public void reporteDeVentasDiarias() {
		VentasDiariasBI.run();
	}

	@Override
	protected List<ReporteMensualCFI> findData() {
		String sql = "SELECT * FROM sx_comprobantes_impresos WHERE FECHA BETWEEN ? AND ?";
		Object[] args = new Object[] {
				new SqlParameterValue(Types.DATE, periodo.getFechaInicial()),
				new SqlParameterValue(Types.DATE, periodo.getFechaFinal())

		};
		return ServiceLocator2.getJdbcTemplate().query(sql, args,
				new BeanPropertyRowMapper(ReporteMensualCFI.class));

	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	public void generarReporteDelSAT() {

		String EMISOR_RFC = ServiceLocator2.getConfiguracion().getSucursal()
				.getEmpresa().getRfc();
		String patt = "{0}{1}{2}{3}";
		String fileName = MessageFormat.format(patt, 2, EMISOR_RFC,
				new SimpleDateFormat("MM").format(periodo.getFechaFinal()),
				new SimpleDateFormat("yyyy").format(periodo.getFechaFinal()));

		JFileChooser chooser = new JFileChooser("/CFD/SAT");
		chooser.setSelectedFile(new File("/CFD/SAT/" + fileName + ".txt"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Destino",
				"txt");
		chooser.setFileFilter(filter);

		int res = chooser.showDialog(getControl(), "Aceptar");
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				if (file.createNewFile()) {
					System.out.println("Archivo generado: " + file);
				} else {

					boolean borrar = MessageUtils
							.showConfirmationMessage(
									"El nombre de archivo ya existe, desea reemplazarlo",
									"Reporte mensual SAT");
					if (borrar)
						file.delete();
				}
				FileOutputStream out = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
				BufferedWriter buf = new BufferedWriter(writer);
				for (Object row : source) {
					ReporteMensualCFI cfd = (ReporteMensualCFI) row;
					String pattern = "|{0}|{1}|{2}|{3}|{4}|{5}|{6}|{7}|{8}||||";
					String line = MessageFormat.format(pattern
							, cfd.getRFC()
							, cfd.getSERIE()
							, cfd.getFOLIO()
							, cfd.getNO_APROBACION()
							, new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(cfd.getFECHA())
							, cfd.getTOTAL()
							, cfd.getIMPUESTO()
							, cfd.getESTADO()
							, cfd.getTIPO_CFD());
					buf.write(line);
					buf.newLine();
				}

				buf.flush();
				buf.close();
				writer.close();
				out.close();

			} catch (IOException e) {
				throw new RuntimeException(ExceptionUtils
						.getRootCauseMessage(e), e);
			}

		}
	}

	private TotalesPanel totalPanel;

	public JPanel getTotalesPanel() {
		if (totalPanel == null) {
			totalPanel = new TotalesPanel();
		}
		return (JPanel) totalPanel.getControl();
	}

	private class TotalesPanel extends AbstractControl implements
			ListEventListener {

		private JLabel total1 = new JLabel();
		private JLabel total2 = new JLabel();
		private JLabel total3 = new JLabel();

		private JLabel totalIva = new JLabel();
		private JLabel totalIvaCancelado = new JLabel();
		private JLabel netoIva = new JLabel();

		@Override
		protected JComponent buildContent() {
			final FormLayout layout = new FormLayout("p,2dlu,f:p:g", "");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
			totalIva.setHorizontalAlignment(SwingConstants.RIGHT);
			totalIvaCancelado.setHorizontalAlignment(SwingConstants.RIGHT);
			netoIva.setHorizontalAlignment(SwingConstants.RIGHT);

			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			total3.setHorizontalAlignment(SwingConstants.RIGHT);

			builder.appendSeparator("Impuestos ");
			builder.append("Total ", totalIva);
			builder.append("Cancelados", totalIvaCancelado);
			builder.append("Neto", netoIva);

			builder.appendSeparator("Totales ");
			builder.append("Total ", total1);
			builder.append("Cancelados", total2);
			builder.append("Neto", total3);

			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}

		public void listChanged(ListEvent listChanges) {
			if (listChanges.next()) {

			}
			updateTotales();
		}

		public void updateTotales() {

			double totalCfd = 0;
			double totalCancelado = 0;

			double totalIvaCfd = 0;
			double totalIvaCancelado = 0;

			for (Object obj : getFilteredSource()) {
				ReporteMensualCFI a = (ReporteMensualCFI) obj;
				if (a.getESTADO().equals("1")) {
					BigDecimal tot=new BigDecimal(a.getTOTAL());
					BigDecimal iva=new BigDecimal(a.getIMPUESTO());
					totalCfd += tot.doubleValue();
					totalIvaCfd += iva.doubleValue();
				} else {
					BigDecimal tot=new BigDecimal(a.getTOTAL());
					BigDecimal iva=new BigDecimal(a.getIMPUESTO());
					totalCancelado += tot.doubleValue();
					totalIvaCancelado += iva.doubleValue();
				}
			}
			total1.setText(nf.format(totalCfd));
			total2.setText(nf.format(totalCancelado));
			total3.setText(nf.format(totalCfd - totalCancelado));

			totalIva.setText(nf.format(totalIvaCfd));
			this.totalIvaCancelado.setText(nf.format(totalIvaCancelado));
			netoIva.setText(nf.format(totalIvaCfd - totalIvaCancelado));
		}

		private NumberFormat nf = NumberFormat.getNumberInstance();

	}

}
