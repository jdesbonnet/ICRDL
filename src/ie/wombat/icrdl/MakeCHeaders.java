package ie.wombat.icrdl;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Generate a C header file from a ICRDL XML file.
 */

public class MakeCHeaders {

	public static void main (String[] arg) throws Exception {
		
		File file = new File(arg[0]);
		SAXReader reader = new SAXReader();
		Document document = reader.read(file);
		
		String icId = document.valueOf("/device/sname");
		
		System.out.println ("/**");
		System.out.println (" * Header file for " + icId);
		System.out.println (" * Auto generated by ICRDL v0.1. ");
		System.out.println (" * Do not edit. Edit ICRDL file and regenerate.");
		System.out.println (" */");
		
		List<Element> registers = document.selectNodes("//register");
		for (Element rEl : registers) {
			String regId = rEl.valueOf("sname").toUpperCase();
			String regAddr = rEl.valueOf("address");
			
			System.out.println ("");
			System.out.println ("/** Register " + icId + " " + regId + "**/");

			System.out.println ("#define " + icId + "_" + regId + " " + regAddr);
			List<Element> bitFields = rEl.selectNodes("bitfield");
			for (Element bitFieldEl : bitFields ) {
				String bitFieldId = bitFieldEl.valueOf("sname");
				// What is LSB of field?
				String bitExpr = bitFieldEl.valueOf("@bit");
				String[] uprlwr = bitExpr.split(":");
				int bitFieldShift;
				int bitFieldWidth = 1;
				if (uprlwr.length == 2) {
					bitFieldShift = Integer.parseInt(uprlwr[1]);
					int bitFieldTop = Integer.parseInt(uprlwr[0]);
					bitFieldWidth = bitFieldTop - bitFieldShift + 1;
				} else {
					bitFieldShift = Integer.parseInt(uprlwr[0]);
					bitFieldWidth = 1;
				}
				
				int bitFieldMask = 1<<bitFieldWidth - 1;
				//bitFieldMask <<= bitFieldShift;
				
				System.out.println ("#define "
						+ icId
						+ "_" + regId
						+ "_" + bitFieldId
						+ "_MASK"
						+ " (" + bitFieldMask + "<<" + bitFieldShift + ")" 
						);

				if (bitFieldEl.valueOf("@type").equals("onoff")) {
					System.out.println ("#define " 
						+ icId 
						+ "_" + regId
						+ "_" + bitFieldId
						+ "_ON"
						+ " (1<<" + bitFieldShift + ")"
						+ " /* " + bitFieldEl.valueOf("name") + " ON */"
					);
				}

				List<Element>fieldValueEls = bitFieldEl.selectNodes("fieldvalue");
				for (Element fieldvalueEl : fieldValueEls) {
					System.out.println ("#define " 
					+ icId 
					+ "_" + regId
					+ "_" + bitFieldId
					+ "_" + fieldvalueEl.valueOf("sname")
					+ " ("
					+ fieldvalueEl.valueOf("value")
					+ "<<"
					+ bitFieldShift
					+ ")"
					+ " /* " + fieldvalueEl.valueOf("name") + " */"
					);
				}
				
			}
		}
	}
}
