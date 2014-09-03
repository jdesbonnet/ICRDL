package ie.wombat.icrdl;

import java.io.File;
import java.util.HashMap;
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
		System.out.println (" * C/C++ header file for " + icId);
		System.out.println (" * Auto generated by ICRDL v0.1. ");
		System.out.println (" * Do not edit. Instead edit ICRDL XML file and regenerate.");
		System.out.println (" */");
		
		
		List<Element> registersEls = document.selectNodes("/device/register");
		for (Element registerEl : registersEls) {
			processRegisterEl(icId, registerEl,null);
		}
		
		//
		// Templates
		// 
		System.out.println ("");
		System.out.println ("");

		List<Element> useEls = document.selectNodes("/device/use");
		for (Element useEl : useEls) {
			System.err.println (useEl);
			String templateId = useEl.valueOf("@template_id");
			
			
			System.out.println ("/* Generated from template " 
			+ templateId + " with the following variable substitutions:");

			// Get set of substitution vars
			HashMap<String,String> vars = new HashMap<String, String>();
			List<Element> varEls = useEl.selectNodes("var");
			for (Element varEl : varEls) {
				String name = varEl.valueOf("@name");
				String value = varEl.valueOf("@value");
				System.out.println (" * " + name + " -> " + value);
				vars.put(name,value);
			}
			
			System.out.println ("*/");
			

			
			// Find template element
			Element templateEl = (Element)document.selectSingleNode("/device/template[@id='" + templateId + "']");
			List<Element> registerEls = templateEl.selectNodes("register");
			for (Element registerEl : registerEls) {
				processRegisterEl(icId, registerEl, vars);
			}
			
		}
		
	}
	
	private static void processRegisterEl (String icId, Element registerEl, HashMap<String,String> vars) {
		
		String line;

		String regId = substituteVars(registerEl.valueOf("sname"),vars).toUpperCase();
		String regAddr = substituteVars(registerEl.valueOf("address"),vars);
		String regName = substituteVars(registerEl.valueOf("name"),vars);
		
		// Comment for start of register defines block
		System.out.println ("");
		System.out.println ("/** Register " 
				+ icId 
				+ " " + regId
				+ " " + regName
				+ " **/");

		// Register address
		line = "#define " + icId + "_" + regId + " (" + regAddr + ")";
		line = substituteVars(line, vars);
		System.out.print(line);
		outSpaces (line);
		System.out.println ("/* " + regId + " register address */");
		
		// Bit field of register
		List<Element> bitFields = registerEl.selectNodes("bitfield");
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
			

			// TODO: not sure what's best policy for single bit fields
			if (bitFieldWidth == 1) {
				line = "#define " 
						+ icId 
						+ "_" + regId
						+ "_" + bitFieldId
						+ " (1<<"
						+ bitFieldShift
						+ ")";
				System.out.print(line);
				outSpaces(line);
				System.out.println ("/* " + bitFieldEl.valueOf("name") + " */");
				continue;
			}
			
			// Bit field mask: only needed with field width > 1 and < wordLength
			if (bitFieldWidth > 1) {
				int bitFieldMask = (1<<bitFieldWidth) - 1;				
				line = "#define "
					+ icId
					+ "_" + regId
					+ (bitFieldId.length()>0 ? "_" + bitFieldId : "")
					+ "_MASK"
					+ " (0x" + Integer.toHexString(bitFieldMask) 
					+ (bitFieldShift>0 ? "<<" + bitFieldShift : "")
					+ ")" 
					;
				System.out.print(line);
				outSpaces(line);
				System.out.println ("/* " + bitFieldId + " bit mask */");
			}
			
			// Constant for each bit field value
			List<Element>fieldValueEls = bitFieldEl.selectNodes("fieldvalue");
			for (Element fieldvalueEl : fieldValueEls) {
				line = "#define " 
						+ icId 
						+ "_" + regId
						+ (bitFieldId.length()>0 ? "_" + bitFieldId : "")
						+ "_" + fieldvalueEl.valueOf("sname")
						+ " ("
						+ fieldvalueEl.valueOf("value")
						+ (bitFieldShift>0 ? "<<" + bitFieldShift : "")
						+ ")";
				System.out.print(line);
				outSpaces(line);
				System.out.println ("/* " + fieldvalueEl.valueOf("name") + " */");
			}
			
		}
	}
	
	/**
	 * Given set of vars replace instances of those substrings in input string 'in'.
	 * 
	 * @param in
	 * @param vars
	 * @return
	 */
	private static String substituteVars (String in, HashMap<String,String>vars) {
		if (vars == null) {
			return in;
		}
		for (String name : vars.keySet()) {
			String value = vars.get(name);
			in = in.replace("${"+name+"}", value);
		}
		
		return in;
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
