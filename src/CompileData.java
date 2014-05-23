/* CompileData.java
 * 
 * This class combines two log sources into one, with some simple correction 
 * of formatting.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CompileData {
	
	private static int loginColumn = 1;
	private static int answerColumn = 9;
	private static int hintColumn = 12;
	private static int startTimeColumn = 2;
	
	public static void main(String arg) {
		String headers = "";	
		String mainFolder = arg;
		
		try {
			PrintWriter outFile = new PrintWriter(new FileWriter(mainFolder + "/allData.txt")); 
			
			List<String> infiles = new ArrayList<String>();
			infiles.add("baker_ses_classes.csv");
			infiles.add("vw_classes.csv");
			
			Boolean first = true;
			
			for ( String file : infiles ) {
				String delimiter = "\",\"";
				
				BufferedReader br = new BufferedReader(new FileReader(mainFolder + "/" + file));
					
				String line = "";
				
				if ( first ) {
					delimiter = "\",\"";
					headers = br.readLine();
					String[] headvals = headers.split(delimiter);
					int i = 0;
					
					for ( String value : headvals ) {
						if ( i != loginColumn && i != answerColumn ) {
							outFile.printf(value.replace("\"","") + "\t");
						}
						i++;
					}
					outFile.printf("\n");

					first = false;
				}
	
				while ((line = br.readLine()) != null ) {
					while ( line.split(delimiter).length < 15 ) {
						line += br.readLine();
					}
					String[] values = line.split(delimiter);
					int i = 0;
					if ( values.length > 0 ) {
						for ( String value : values ) {
							if ( i != loginColumn && i != answerColumn ) {
								if ( i == startTimeColumn ) 
									outFile.printf(value.substring(0,2) + "." + value.substring(2,4) + "." + value.substring(4,8) + " " + 
											value.substring(8,10) + ":" + value.substring(10,12) + ":" + value.substring(12,14) + "\t");
								else	
									if ( value.replace("\"","").equals("") ) 
										if ( i == hintColumn )
											outFile.printf("0\t");
										else
											outFile.printf(".\t");
									else
										outFile.printf(value.replace("\"","") + "\t");
							}
						i++;
						}
					}
					outFile.printf("\n");
				}
				
				br.close();
			}
			outFile.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
}
