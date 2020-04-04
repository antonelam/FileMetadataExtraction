import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
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
	String[] allowed_fileTypes =  {".doc",".docx",".pdf"};
	String[] special_fileTypes = {".xml", ".log"};
	int count = 0; //count how many files generated
	int count_correct = 0; //count how many files are correct format
	int count_incorrect = 0; //count how many files are incorrect format
	
	public FileManipulation(String path) {
		/*
		 * This is a constructor
		 * It creates file from path given and reads xml file
		 * 
		 */
		try {
			this.path = path;
			/* NAME of the xml template
			 * NO NEED TO PUT WHOLE PATH ANYMORE
			 * LEAVE HOW IT IS
			 */
			
			this.doc = readXML("XML_Source\\generic.metadata.xml");
			// path to the .doc, .docx, .pdf files
			this.folder = new File(path);
			
		} catch (Exception e) {
			System.out.println("Invalid path location.");
			e.printStackTrace();
		}
	}

	public void startMetadataExtraction() {
		/*
		 * This is start function
		 * It will run only if invoked from Main
		 * It will first check if file/folder exists (maybe path given is incorrect)
		 * If correct, it calls recursive function listAllFiles - it goes through all files and folders within
		 * If not correct, it will stop the program and inform the user
		 */
		try {
			if(this.folder.exists()) {
				listAllFiles(this.folder);
				System.out.println("Files generated:" + this.count);
				System.out.println("Files passed verification:" + this.count_correct);
				System.out.println("Files failed verification:" + this.count_incorrect);
			}
			else
				System.out.println("The folder provided does not exist.");
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
			if("cm:created".equals(nodeAttr.getTextContent())) {
				node_.setTextContent(LocalDate.now().toString());
			}
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
			//System.out.println("Generating:" + filename + ".metadata.properties.xml");
			transformer.transform(source, result);
			//System.out.println("Done...");
			this.count ++; //little number counter that says how many files are generated
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("Done...");
		//this.count ++; //little number counter that says how many files are generated
	}

	public Document readXML(String path) throws ParserConfigurationException, SAXException, IOException {
		// reading xml file - generic script
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		/*this WAS the code i used -- it tries to detect xml template location
		 * It didn't work properly when xml was part of jar
		 * so is useless now
		 */
		/*ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("File is not found!");
        } else {
            return new File();
        	System.out.println("Location of the xml template: " + resource.getPath());
            Document doc = docBuilder.parse(resource.getFile());
            return doc;
        }*/
		// path to generic xml file
		Document doc = docBuilder.parse(path);
		return doc;
	}

	public void listAllFiles(File folder) throws IOException {
		List<String> rejected = new ArrayList<String>();//wrongly formatted names
		List <String> wrongFileTypes = new ArrayList<String>();//wrong file types
		//recursive call through the file tree
		for (File file_ : folder.listFiles(createFilter(wrongFileTypes))) {
			//get list of approved file names and directories in the current folder
			if (file_.isDirectory()) {		
				/*
				 * if reading folder name, call function listAllFiles again
				 * this way, for each folder we call listAllFiles
				 * when we are done, on the exit of the function, we return here, and continue with file name verification
				 * look up recursive calls if still does not make sense
				 */
				listAllFiles(file_);
			} else {
				File temp = new File(file_.getPath() + ".metadata.properties.xml");
				this.count_correct++;
				if (!temp.exists()) { 
					//code won't verify file name if corresponding .xml is generated -- stops overwritting
					if (VerifyFileName(file_.getName())) {
						// if name is correctly formated, generate .xml
						updateXML(this.doc, folder.getPath(), file_.getName());
					} else {
						this.count_correct--;
						this.count_incorrect++;
						rejected.add(folder.getName() +"\\"+ file_.getName());
					}
				}
			}
		}
		/* trying out new output with '|'
		 * call write function
		 */
		//Files.write(Paths.get(folder.getPath(), "Incorrect_Formating.log"), rejected));
		BufferedWriter writer = new BufferedWriter(new FileWriter(folder.getPath() +"\\"+"Incorrect_Formating.log", false));
	    writer.append(String.join("|", rejected));   	     
	    writer.close();
		//Files.write(Paths.get(folder.getPath(), "Unsupported_FileType.log"), wrongFileTypes);
	    writer = new BufferedWriter(new FileWriter(folder.getPath() +"\\"+"Unsupported_FileType.log", false));
	    writer.append(String.join("|", wrongFileTypes));   	     
	    writer.close();
		
	}

	public boolean VerifyFileName(String file_name) {
		boolean state = true;
		LocalDate date;
		// if more than one '.' the system will detect abnormality
		String[] file_n = file_name.split("\\.");
		if (file_n.length > 2)
			return false;
		/*
		 * this will filter out everything that doesn't follow requirements
		 * except for date requirement, the date must be verified	
		 *	
		 */
		state = file_n[0].matches("^[a-zA-Z]{1,2}_[0-9]+_[0-9]{6}");
		/* 
		 * here the string is split into array 
		 * part[0] => case letters
		 * part[1] => case number
		 * part[2] => date
		 */
		String[] part = file_n[0].split("_");
		//for (String part : file_n[0].split("_")) {
		for(int i = 0; i < part.length; i++){
			if (state) {
				//if (part[i].length() < 3 && part[i].matches("^[a-zA-Z]{1,2}")) {
					// state = part.matches("[A-Za-z]+");
					//values[i] = part[i];
				//} else if (part.length() == 4) {
					// state = part.matches("[0-9]+");
				//	values[1] = part;
				//} else if (part[i].length() == 6 && part[i].matches("^[0-9]{6}")) {
				if (i == 2) {
					// check date format only
					try {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
						date = LocalDate.parse(part[i], formatter);
						values[i] = date.toString();
						
					} catch (DateTimeParseException e) {
						//if not a date return false
						state = false;
					}
				} else
					//state = false;
					/*
					 * if case letter or number, just save 
					 * this variable is used in XML file update
					 */
					
					values[i] = part[i];
			}
		}
		return state;
	}

	public FilenameFilter createFilter(List <String> wrongFileTypes) {
		/*
		 * this creates filter which then filters according to the allowed file types
		 * it allows directories
		 * if not a directory and not in allowed file type, write it into wrongFileTypes list
		 * must write here because FilenameFilter only returns boolean, can't return List
		 *
		 */
		return (dir, name) -> {
			if (name.lastIndexOf('.') > 0) {
				// get last index for '.' char
				int lastIndex = name.lastIndexOf('.');
				// get extension
				String str = name.substring(lastIndex);
				// match path name extension
				for(String attachment: allowed_fileTypes) {//if file .doc, .docx, .pdf allow it
					if (str.equals(attachment))
						return true;
				}	
				for(String attachment: special_fileTypes) {//if file is .log or .xml please ignore
					if (str.equals(attachment))
						return false;
				}	
				
			}
			else if(dir.isDirectory())//if file is folder allow it		
				return true;
			this.count_incorrect++;
			wrongFileTypes.add(dir.getName() +"\\"+ name);//if nothing from above, filter it out and add to list
			return false;
		};

	}

}
