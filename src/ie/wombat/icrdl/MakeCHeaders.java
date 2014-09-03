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
	
	/** Start comments on this column if possible */
	private static final int COMMENTS_COL = 60;

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
		
		String defineLine;
		
		List<Element> registers = document.selectNodes("//register");
		for (Element rEl : registers) {
			String regId = rEl.valueOf("sname").toUpperCase();
			String regAddr = rEl.valueOf("address");
			
			// Comment for start of register defines block
			System.out.println ("");
			System.out.println ("/** Register " + icId + " " + regId + " **/");

			// Register address
			defineLine = "#define " + icId + "_" + regId + " " + regAddr;
			System.out.print(defineLine);
			outSpaces (defineLine);
			System.out.println ("/* " + regId + " register address */");
			
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
				
				// Bit field mask
				int bitFieldMask = 1<<bitFieldWidth - 1;				
				System.out.println ("#define "
						+ icId
						+ "_" + regId
						+ "_" + bitFieldId
						+ "_MASK"
						+ " (" + bitFieldMask + "<<" + bitFieldShift + ")" 
						);

				// Constant for each bit field value
				List<Element>fieldValueEls = bitFieldEl.selectNodes("fieldvalue");
				for (Element fieldvalueEl : fieldValueEls) {
					defineLine = "#define " 
							+ icId 
							+ "_" + regId
							+ "_" + bitFieldId
							+ "_" + fieldvalueEl.valueOf("sname")
							+ " ("
							+ fieldvalueEl.valueOf("value")
							+ "<<"
							+ bitFieldShift
							+ ")";
					System.out.print(defineLine);
					outSpaces(defineLine);
					System.out.println ("/* " + fieldvalueEl.valueOf("name") + " */");
				}
				
			}
		}
	}
	
	/** 
	 * Append spaces to end of line to aid formatting of comments.
	 * 
	 * @param line
	 */
	private static void outSpaces (String line) {
		if (line.length() < COMMENTS_COL) {
			for (int i = 0; i < COMMENTS_COL - line.length(); i++) {
				System.out.print(" ");
			}
		}
	}
	
}
