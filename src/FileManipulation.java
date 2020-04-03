import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FileManipulation {
	String path;
	Document doc;
	File folder;
	String[] values = new String[3];

	public FileManipulation(String path) {
		try {
			this.path = path;
			//location of the xml template
			this.doc = readXML("C:\\Users\\antonela.mrkalj\\git\\xml_generate\\generic.metadata.xml");
			// path to the .doc files
			this.folder = new File(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startMetadataExtraction() {
		try {
			listAllFiles(this.folder, createFilter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateXML(Document doc, String main_path, String filename) {
		// Get the root element
		// Node properties_ = doc.getFirstChild();
		NodeList listOfChildNodes = doc.getElementsByTagName("entry");
		// loop the entry child nodes
		for (int i = 0; i < listOfChildNodes.getLength(); i++) {
			Node node_ = listOfChildNodes.item(i);
			NamedNodeMap attr = node_.getAttributes();
			// System.out.println(node_.getTextContent());
			Node nodeAttr = attr.getNamedItem("key");
			if ("acn:HCOCaseType".equals(nodeAttr.getTextContent())) {
				// System.out.println("I am in");
				node_.setTextContent(values[0]);
			}
			if ("acn:HCOCaseNumber".equals(nodeAttr.getTextContent())) {
				// System.out.println("I am in");
				node_.setTextContent(values[1]);
			}
			if ("acn:HCOCaseDate".equals(nodeAttr.getTextContent())) {
				// System.out.println("I am in");
				node_.setTextContent(values[2]);
			}
			// System.out.println(nodeAttr.getTextContent());
		}
		writeXML(main_path, filename, doc);
	}

	public void writeXML(String path, String filename, Document doc) {
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path + "\\" + filename + ".metadata.properties.xml"));
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}

	public Document readXML(String path) throws ParserConfigurationException, SAXException, IOException {
		// readin xml file - generic script
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		// path to generic xml file
		Document doc = docBuilder.parse(path);
		return doc;
	}

	public void listAllFiles(File folder, FilenameFilter filter) throws IOException {
		List<String> rejected = new ArrayList<String>();
		for (File file_ : folder.listFiles(filter)) {
			if (file_.isDirectory()) {
				listAllFiles(file_, filter);
			} else {
				//System.out.println(file_.getName());
				File temp = new File(file_.getPath() + ".metadata.properties.xml");
				if (!temp.exists()) { // false
					// System.out.println(temp);
					if (VerifyFileName(file_.getName())) {
						// System.out.println(file_.getName());
						updateXML(this.doc, folder.getPath(), file_.getName());
					} else {
						rejected.add(file_.getPath());
					}
				}
			}
		}
		// call write function
		Files.write(Paths.get(folder.getPath(), "Incorrect_Formating.txt"), rejected);
	}

	public boolean VerifyFileName(String file_name) {
		boolean state = true;
		LocalDate date;
		// if more than one '.' the system will detect abnormality
		//
		String[] file_n = file_name.split("\\.");
		if (file_n.length > 2)
			return false;
		state = file_n[0].matches("^[a-zA-Z]{1,2}_[0-9]{4}_[0-9]{6}");
		for (String part : file_n[0].split("_")) {
			if (state) {
				if (part.length() < 3) {
					// state = part.matches("[A-Za-z]+");
					values[0] = part;
				} else if (part.length() == 4) {
					// state = part.matches("[0-9]+");
					values[1] = part;
				} else if (part.length() == 6) {
					// check date format
					try {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
						date = LocalDate.parse(part, formatter);
						values[2] = date.toString();
						// System.out.println(date);
					} catch (DateTimeParseException e) {
						// e.printStackTrace();
						state = false;
					}
				} else
					state = false;
			}
		}
		return state;
	}

	public FilenameFilter createFilter() {
		// create new filename filter
		return (dir, name) -> {
			if (name.lastIndexOf('.') > 0) {
				// get last index for '.' char
				int lastIndex = name.lastIndexOf('.');
				// get extension
				String str = name.substring(lastIndex);
				// match path name extension
				if (str.equals(".docx") || str.equals(".doc"))
					return true;
			}
			else if(dir.isDirectory())		
				return true;
			return false;
		};

	}

}
