package com.luxsoft.sw3.cfdi;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.UUID;

import javax.imageio.ImageIO;

import mx.gob.sat.cfd.x3.ComprobanteDocument;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.luxsoft.sw3.cfdi.model.TimbreFiscal;

public class QRCodeUtils {
	
	public static Image generarQR(Comprobante cfdi) {
		try {
			TimbreFiscal timbre=new TimbreFiscal(cfdi);
			BigDecimal total=cfdi.getTotal();
			String pattern="?re={0}&rr={1}&tt={2,number,##########.######}&id,{3}";
			String qq=MessageFormat.format(pattern, cfdi.getEmisor().getRfc(),cfdi.getReceptor().getRfc(),cfdi.getTotal(),timbre.UUID);
			
			File file=QRCode.from(qq).to(ImageType.GIF).withSize(250, 250).file();
			
			BufferedImage img=ImageIO.read(file);
			return img;
		} catch (Exception e) {
			throw new RuntimeException(ExceptionUtils.getRootCauseMessage(e),e);
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		
		URL url=new URL("file:/C:/gasoc/cfdi/xml/FAC_C_1125.xml");
		ComprobanteDocument document=ComprobanteDocument.Factory.parse(url);
		Image img=generarQR(document.getComprobante());
		System.out.println(img);

	}

}
