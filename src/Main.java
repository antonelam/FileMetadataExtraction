public class Main {

	public static void main(String[] args) {
		try {
			/*
			 * The main function always expects ONE argument
			 * String [] args is empty if nothing is given
			 * When running program from eclipse, you are not giving arguments, therefore program will stop
			 * You must run from command line to test functionality
			 * Or comment out if statement and replace args[0] with your file path
			 */
			if(args.length == 1) {			
				System.out.println(args[0]);
				String main_path = args[0];//"C:\\Users\\antonela.mrkalj\\git\\FileMetadataExtraction\\TEST";
				FileManipulation filegenerate = new FileManipulation(main_path);
				filegenerate.startMetadataExtraction();
			}
			else
				System.out.println("Expected input: one \nType: full path to the target folder.");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
