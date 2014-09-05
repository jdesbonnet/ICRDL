package ie.wombat.icrdl;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberInputStream;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Read annotations in comments in C file and insert/update register
 * documentation.
 */

public class UpdateCFile {
	
	/** Start comments on this column if possible */
	private static final int COMMENTS_COL = 50;

	public static void main (String[] arg) throws Exception {
		
		File file = new File(arg[0]);
		SAXReader reader = new SAXReader();
		Document document = reader.read(file);
		// Need to expand templates
		
		File cFile = new File(arg[1]);
		LineNumberReader lnr = new LineNumberReader(new FileReader(cFile));
		
		String line;
		while ( (line = lnr.readLine()) != null) {
			System.out.println (line);
			if (line.startsWith(" * @rdl ")) {
				String registerPath = line.substring(7).trim();
				String[] p = registerPath.split("\\.");
				System.err.println ("plen=" + p.length + " path=" + registerPath);
				String icId = p[0];
				String regId = p[1];
				String bitFieldId = p[2];
				// is doc already there?
				line = lnr.readLine();
				if (line.startsWith(" * START")) {
				
					while (line != null) {
						line = lnr.readLine();
				
						if (line.startsWith(" * END")) {
							break;
						}
					}
					// Insert updated documentation
					System.out.println (" * START");
					Element registerEl = (Element)document.selectSingleNode("/device/register[sname='" + regId + "']");
					Element bitFieldEl = (Element)registerEl.selectSingleNode("bitfield[sname='" + bitFieldId + "']");
					//System.out.println (" * registerEl=" + registerEl);
					//System.out.println (" * bitFieldEl=" + bitFieldEl);

					System.out.println (" * Register " 
							+ regId + " " + registerEl.valueOf("name")
							+ " (address " + registerEl.valueOf("address") + ")"
							);
					System.out.println (" * " + regId 
							+ "[" + bitFieldEl.valueOf("@bit") + "] "
							+ bitFieldEl.valueOf("sname")
							+ " ("
							+ bitFieldEl.valueOf("@rwmode")
							+ ") "
							+ bitFieldEl.valueOf("name")
							);
					List<Element> valueEls = bitFieldEl.selectNodes("fieldvalue");
					for (Element valueEl : valueEls) {
						System.out.println (" * "
								+ valueEl.valueOf ("value")
								+ " "
								+ valueEl.valueOf ("sname")
								+ " "
								+ valueEl.valueOf ("name")
								);
					}
					System.out.println (" * END");
				}
				
			}
		}
		
	}
}


