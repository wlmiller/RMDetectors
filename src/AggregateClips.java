/* AggregateClips.java
 * 
 * This class aggregates the student logs, complete with extracted features and
 * tagged with observations, into 20-second clips which are used as the
 * individual lines over which the detectors are fit.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AggregateClips {	
	private static int numColumns = 150;
	private static int studentColumn = 8;
	private static int clipColumn = 5;
	private static int behaviorColumn = 1;
	private static int affectColumn = 2;
	private static int durationColumn = 22;
	private static String separator = ",";
	
	private static String infile = "../../RMDetectors/Data_All_Synced_contextual_features.txt";
	private static String outfiledir = "../../RMDetectors/analytics/";
	private static String outfilesuffix = "Data_Clips.csv";
	
	// These lists contain the features for which the specific aggregation method is to be used.
	private static String[] presenceList = {"tgreaterthan80","tgreaterthan180","tlessthan3", "tSDgreaterthan1", "tSDlessthanneg1", 
			"hintused", "lowpknownohint", "highpknowhint"};
	private static String[] countList = {"theory","speedgame","problem","notesttest",
		"enterexpression","fillintheblanks","interactiveanswer","multchoicecheckbox", "multchoicecombobox", "multchoiceradiobutton", 
		"hintused", "lowpknownohint", "highpknowhint"};
	private static String[] minList = {"L0","G","S","T","pknow","difficulty",
		"durationlongwindowdiff","timeSDlongwindowdiff","difficultylongwindowdiff","pknowlongwindowdiff",
		"timescellseentotal","timescellseenconsecutive","righttimestimeSD","wrongtimestimeSD","wrongconsecutive","timeSDdiff","timeSDdiff2","time3SD","time5SD",
		"perccorrectcell","percincorrectcell","numincorrectcell","perccorrectskill","percincorrectskill","numincorrectskill",
		"prevhinttimesduration", "prevhinttimestimeSD", "numhintsskill", "numhintsorincorrectskill", "timeperhint",
		"bored_pred","concentrating_pred","offtask_pred","ontask_pred",
		"PofJmodel","PofGmodel","PofSmodel",
		"PJtimesPG","PJbeforetimesPS","PGcubed","PJcubed","PScubed",
		"avgPJ","sumPJ","areaPJ","peakPJ","2ndPeakPJ","3rdPeakPJ",
		"peakIndex","2ndPeakIndex","3rdPeakIndex","2PeakDist","2PeakRelDist","2PeakDecr","2PeakRelDecr","3PeakDecr","3PeakRelDecr"};
	private static String[] maxList = {"L0","G","S","T","pknow","difficulty",
		"durationlongwindowdiff","timeSDlongwindowdiff","difficultylongwindowdiff","pknowlongwindowdiff",
		"righttimesduration","righttimestimeSD","wrongtimesduration","wrongtimestimeSD","time3SD","time5SD",
		"timescellseentotal","timescellseenconsecutive","wrongconsecutive","timeSDdiff","timeSDdiff2",
		"perccorrectcell","percincorrectcell","numincorrectcell","perccorrectskill","percincorrectskill","numincorrectskill",
		"prevhinttimesduration", "prevhinttimestimeSD", "hints", "numhintsskill", "numhintsorincorrectskill", "timeperhint",
		"bored_pred","concentrating_pred","offtask_pred","ontask_pred",
		"PofJmodel","PofGmodel","PofSmodel",
		"PJtimesPG","PJbeforetimesPS","PGcubed","PJcubed","PScubed",
		"avgPJ","sumPJ","areaPJ","peakPJ","2ndPeakPJ","3rdPeakPJ",
		"peakIndex","2ndPeakIndex","3rdPeakIndex","2PeakDist","2PeakRelDist","2PeakDecr","2PeakRelDecr","3PeakDecr","3PeakRelDecr"};
	private static String[] avgList = {"duration","timeSD","right",
		"durationlongwindowdiff","timeSDlongwindowdiff","difficultylongwindowdiff","pknowlongwindowdiff",
		"righttimesduration","righttimestimeSD","wrongtimesduration","wrongtimestimeSD",
		"L0","G","S","T","pknow","difficulty","time3SD","time5SD",
		"perccorrectcell","percincorrectcell","numincorrectcell","perccorrectskill","percincorrectskill","numincorrectskill",
		"prevhinttimesduration", "prevhinttimestimeSD", "numhintsskill", "numhintsorincorrectskill", "timeperhint",
		"bored_pred","concentrating_pred","offtask_pred","ontask_pred",
		"PofJmodel","PofGmodel","PofSmodel",
		"PJtimesPG","PJbeforetimesPS","PGcubed","PJcubed","PScubed",
		"avgPJ","sumPJ","areaPJ","peakPJ","2ndPeakPJ","3rdPeakPJ",
		"peakIndex","2ndPeakIndex","3rdPeakIndex","2PeakDist","2PeakRelDist","2PeakDecr","2PeakRelDecr","3PeakDecr","3PeakRelDecr"};
	private static String[] stdevList = {"duration","timeSD","right",
		"durationlongwindowdiff","timeSDlongwindowdiff","difficultylongwindowdiff","pknowlongwindowdiff",
		"righttimesduration","righttimestimeSD","wrongtimesduration","wrongtimestimeSD",
		"L0","G","S","T","pknow","difficulty","time3SD","time5SD",
		"perccorrectcell","percincorrectcell","numincorrectcell","perccorrectskill","percincorrectskill","numincorrectskill",
		"prevhinttimesduration", "prevhinttimestimeSD", "numhintsskill", "numhintsorincorrectskill", "timeperhint",
		"PofJmodel","PofGmodel","PofSmodel",
		"PJtimesPG","PJbeforetimesPS","PGcubed","PJcubed","PScubed",
		"avgPJ","sumPJ","areaPJ","peakPJ","2ndPeakPJ","3rdPeakPJ",
		"peakIndex","2ndPeakIndex","3rdPeakIndex","2PeakDist","2PeakRelDist","2PeakDecr","2PeakRelDecr","3PeakDecr","3PeakRelDecr"};
	private static String[] pctList = {"theory","speedgame","problem","notestest","hintused", "lowpknownohint", "highpknowhint"};
	private static String[] pctTimeList = {"theory","speedgame","problem","notestest","hintused"};
	private static String[] firstList = {"lesson","durationavglongwindow","timeSDavglongwindow","difficultyavglongwindow",
		"pknowavglongwindow","rightavglongwindow", "numactionslongwindow", "theorytimelongwindow", "theorypcttimelongwindow", 
		"problemtimelongwindow", "problempcttimelongwindow", "problemcountlongwindow", "speedgamecountlongwindow",
		"bored_pred","concentrating_pred","offtask_pred","ontask_pred"};
	private static String[] sumList = {"hints"};
	
	private static HashMap<String,Integer[]> behaviors = new HashMap<String,Integer[]>();
	private static HashMap<String,Integer[]> affects = new HashMap<String,Integer[]>();
	
	private HashMap<String,Integer> batchMap = new HashMap<String,Integer>();
	
	private void readData() {
		try {
			affects.put("BORED",new Integer[]{8,1});
			affects.put("CONCENTRATING",new Integer[]{1,2});
			affects.put("CONFUSED",new Integer[]{43,1});
			affects.put("FRUSTRATED",new Integer[]{116,1});
			
			behaviors.put("OFF TASK",new Integer[]{16,1});
			behaviors.put("ON TASK",new Integer[]{1,3});
			behaviors.put("ON TASK CONV",new Integer[]{20,1});
			behaviors.put("PULL OUT",new Integer[]{116,1});
			BufferedReader br = new BufferedReader(new FileReader(infile));
			
			String line = "";
			String[] values = new String[numColumns];
			String clip = "";
			
			line = br.readLine(); //headers
			String[] headers = line.split("\t");
			
			try {
				PrintWriter outFile = new PrintWriter(new FileWriter(outfiledir + "all" + outfilesuffix,false)); 
				
				outFile.printf("student" + separator);
				
				outFile.printf("numactions" + separator);
				
				for ( String header : headers ) {
					if ( Arrays.asList(presenceList).contains(header) ) {
						outFile.printf(header + "present" + separator);
					}
					if ( Arrays.asList(countList).contains(header) ) {
						outFile.printf(header + "count" + separator);
					}
					if ( Arrays.asList(minList).contains(header) ) {
						outFile.printf(header + "min" + separator);
					}
					if ( Arrays.asList(maxList).contains(header) ) {
						outFile.printf(header + "max" + separator);
					}
					if ( Arrays.asList(avgList).contains(header) ) {
						outFile.printf(header + "avg" + separator);
					}
					if ( Arrays.asList(stdevList).contains(header) ) {
						outFile.printf(header + "stdev" + separator);
					}
					if ( Arrays.asList(pctList).contains(header) ) {
						outFile.printf(header + "percent" + separator);
					}
					if ( Arrays.asList(pctTimeList).contains(header) ) {
						outFile.printf(header + "percenttime" + separator);
					}
					if ( Arrays.asList(firstList).contains(header) ) {
						outFile.printf(header + "first" + separator);
					}
					if ( Arrays.asList(sumList).contains(header) ) {
						outFile.printf(header + "sum" + separator);
					}
				}
				
				outFile.printf("Batch" + separator);
				
				outFile.printf("behavior" + separator);
				outFile.printf("affect\n");
				
				outFile.close();
			} catch ( Exception e ) {
				e.printStackTrace();
			}

			for ( String behavior:behaviors.keySet() ) {
				try {
					PrintWriter outFile = new PrintWriter(new FileWriter(outfiledir + behavior.toLowerCase().replace(" ","") + outfilesuffix,false)); 
					outFile.printf("student" + separator);
					outFile.printf("numactions" + separator);
					
					for ( String header : headers ) {
						if ( Arrays.asList(presenceList).contains(header) ) {
							outFile.printf(header + "present" + separator);
						}
						if ( Arrays.asList(countList).contains(header) ) {
							outFile.printf(header + "count" + separator);
						}
						if ( Arrays.asList(minList).contains(header) ) {
							outFile.printf(header + "min" + separator);
						}
						if ( Arrays.asList(maxList).contains(header) ) {
							outFile.printf(header + "max" + separator);
						}
						if ( Arrays.asList(avgList).contains(header) ) {
							outFile.printf(header + "avg" + separator);
						}
						if ( Arrays.asList(stdevList).contains(header) ) {
							outFile.printf(header + "stdev" + separator);
						}
						if ( Arrays.asList(pctList).contains(header) ) {
							outFile.printf(header + "percent" + separator);
						}
						if ( Arrays.asList(pctTimeList).contains(header) ) {
							outFile.printf(header + "percenttime" + separator);
						}
						if ( Arrays.asList(firstList).contains(header) ) {
							outFile.printf(header + "first" + separator);
						}
						if ( Arrays.asList(sumList).contains(header) ) {
							outFile.printf(header + "sum" + separator);
						}
					}
					
					outFile.printf("Batch" + separator);
					outFile.printf(behavior.toLowerCase().replace(" ","") + "\n");
					
					outFile.close();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
				try {
					PrintWriter outFile = new PrintWriter(new FileWriter(outfiledir + behavior.toLowerCase().replace(" ","") + "_unbiased_" + outfilesuffix,false)); 
					outFile.printf("student" + separator);
					outFile.printf("numactions" + separator);
					
					for ( String header : headers ) {
						if ( Arrays.asList(presenceList).contains(header) ) {
							outFile.printf(header + "present" + separator);
						}
						if ( Arrays.asList(countList).contains(header) ) {
							outFile.printf(header + "count" + separator);
						}
						if ( Arrays.asList(minList).contains(header) ) {
							outFile.printf(header + "min" + separator);
						}
						if ( Arrays.asList(maxList).contains(header) ) {
							outFile.printf(header + "max" + separator);
						}
						if ( Arrays.asList(avgList).contains(header) ) {
							outFile.printf(header + "avg" + separator);
						}
						if ( Arrays.asList(stdevList).contains(header) ) {
							outFile.printf(header + "stdev" + separator);
						}
						if ( Arrays.asList(pctList).contains(header) ) {
							outFile.printf(header + "percent" + separator);
						}
						if ( Arrays.asList(pctTimeList).contains(header) ) {
							outFile.printf(header + "percenttime" + separator);
						}
						if ( Arrays.asList(firstList).contains(header) ) {
							outFile.printf(header + "first" + separator);
						}
						if ( Arrays.asList(sumList).contains(header) ) {
							outFile.printf(header + "sum" + separator);
						}
					}

					outFile.printf("Batch" + separator);
					outFile.printf(behavior.toLowerCase().replace(" ","") + "\n");
					
					outFile.close();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			for ( String affect:affects.keySet() ) {
				try {
					PrintWriter outFile = new PrintWriter(new FileWriter(outfiledir + affect.toLowerCase().replace(" ","") + outfilesuffix,false)); 
					outFile.printf("student" + separator);
					outFile.printf("numactions" + separator);
					
					for ( String header : headers ) {
						if ( Arrays.asList(presenceList).contains(header) ) {
							outFile.printf(header + "present" + separator);
						}
						if ( Arrays.asList(countList).contains(header) ) {
							outFile.printf(header + "count" + separator);
						}
						if ( Arrays.asList(minList).contains(header) ) {
							outFile.printf(header + "min" + separator);
						}
						if ( Arrays.asList(maxList).contains(header) ) {
							outFile.printf(header + "max" + separator);
						}
						if ( Arrays.asList(avgList).contains(header) ) {
							outFile.printf(header + "avg" + separator);
						}
						if ( Arrays.asList(stdevList).contains(header) ) {
							outFile.printf(header + "stdev" + separator);
						}
						if ( Arrays.asList(pctList).contains(header) ) {
							outFile.printf(header + "percent" + separator);
						}
						if ( Arrays.asList(pctTimeList).contains(header) ) {
							outFile.printf(header + "percenttime" + separator);
						}
						if ( Arrays.asList(firstList).contains(header) ) {
							outFile.printf(header + "first" + separator);
						}
						if ( Arrays.asList(sumList).contains(header) ) {
							outFile.printf(header + "sum" + separator);
						}
					}
					
					//outFile.printf("behavior\t");
					outFile.printf("Batch" + separator);
					outFile.printf(affect.toLowerCase().replace(" ","") + "\n");
					
					outFile.close();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
				try {
					PrintWriter outFile = new PrintWriter(new FileWriter(outfiledir + affect.toLowerCase().replace(" ","") + "_unbiased_" + outfilesuffix,false)); 
					outFile.printf("student" + separator);
					outFile.printf("numactions" + separator);
					
					for ( String header : headers ) {
						if ( Arrays.asList(presenceList).contains(header) ) {
							outFile.printf(header + "present" + separator);
						}
						if ( Arrays.asList(countList).contains(header) ) {
							outFile.printf(header + "count" + separator);
						}
						if ( Arrays.asList(minList).contains(header) ) {
							outFile.printf(header + "min" + separator);
						}
						if ( Arrays.asList(maxList).contains(header) ) {
							outFile.printf(header + "max" + separator);
						}
						if ( Arrays.asList(avgList).contains(header) ) {
							outFile.printf(header + "avg" + separator);
						}
						if ( Arrays.asList(stdevList).contains(header) ) {
							outFile.printf(header + "stdev" + separator);
						}
						if ( Arrays.asList(pctList).contains(header) ) {
							outFile.printf(header + "percent" + separator);
						}
						if ( Arrays.asList(pctTimeList).contains(header) ) {
							outFile.printf(header + "percenttime" + separator);
						}
						if ( Arrays.asList(firstList).contains(header) ) {
							outFile.printf(header + "first" + separator);
						}
						if ( Arrays.asList(sumList).contains(header) ) {
							outFile.printf(header + "sum" + separator);
						}
					}
					
					outFile.printf("Batch" + separator);
					outFile.printf(affect.toLowerCase().replace(" ","") + "\n");
					
					outFile.close();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			line = br.readLine();
			
			while (line != null ) {
				values = line.split("\t");
				
				while ( values[clipColumn].equals(".") ) {
					line = br.readLine();
					if ( line == null ) break;
					values = line.split("\t");
				}
				if ( line == null ) break;
				
				clip = values[clipColumn];
				
				List<String[]> clipLines = new ArrayList<String[]>();
				
				while ( values[clipColumn].equals(clip)) {
					clipLines.add(values);
					line = br.readLine();
					if ( line == null ) break;
					values = line.split("\t");
				}
				
				int[] presence = new int[numColumns];
				int[] count = new int[numColumns];
				double[] max = new double[numColumns];
				double[] min = new double[numColumns];
				double[] avg = new double[numColumns];
				double[] sum = new double[numColumns];
				double[] stdev = new double[numColumns];
				double[] time = new double[numColumns];
				double totalTime = 0.0;
				int allCount = 0;
				String behavior = "";
				String affect = "";
				String student = "";
				String[] first = clipLines.get(0);
			
				int firstLine = 1;
				
				for ( String[] allVals : clipLines ) {
					allCount++;
					totalTime += Double.valueOf(allVals[durationColumn]);
					
					if ( !allVals[behaviorColumn].equalsIgnoreCase("Not observed") ) {
						behavior = allVals[behaviorColumn];
						affect = allVals[affectColumn];
					}
					student = allVals[studentColumn];
					
					for (int j = 0; j < allVals.length; j++) {
						if (allVals[j].equals("1")) presence[j] = 1;
						
						if (allVals[j].equals("1")) count[j]++;
						
						if (allVals[j].equals("1")) time[j]+= Double.valueOf(allVals[durationColumn]);
						
						if ( Arrays.asList(maxList).contains(headers[j]) ) {
							if (firstLine==1)
								max[j] = Double.valueOf(allVals[j]);
							
							if (Double.valueOf(allVals[j]) > max[j])
								max[j] = Double.valueOf(allVals[j]);
								
						}
						
						if ( Arrays.asList(minList).contains(headers[j]) ) {
							if (firstLine==1)
								min[j] = Double.valueOf(allVals[j]);
							
							if (Double.valueOf(allVals[j]) < min[j])
								min[j] = Double.valueOf(allVals[j]);
								
						}
						
						if ( Arrays.asList(avgList).contains(headers[j]) || Arrays.asList(sumList).contains(headers[j]) ) 
							if ( headers[j].equals("right") && Double.valueOf(allVals[j]) < 0 ) 
								sum[j] += 0.5;
							else
								sum[j] += Double.valueOf(allVals[j]);
						
					}
					
					firstLine = 0;
				}
				
				for (int j = 0; j < clipLines.get(0).length; j++) {
					if ( Arrays.asList(avgList).contains(headers[j]) ) 
						avg[j] = sum[j]/allCount;
				}
				
				for ( String[] allVals : clipLines ) {
					for (int j = 0; j < allVals.length; j++) {
						if ( Arrays.asList(stdevList).contains(headers[j]) ) {
							if (headers[j].equals("right") && Double.valueOf(allVals[j]) < 0) 
								stdev[j] += (0.5-avg[j])*(0.5-avg[j]);
							else
								stdev[j] += (Double.valueOf(allVals[j])-avg[j])*(Double.valueOf(allVals[j])-avg[j]);
						}		
					}
				}
				
				try {
					PrintWriter outFile = new PrintWriter(new FileWriter(outfiledir + "all" + outfilesuffix,true)); 
					
					outFile.printf(student + separator);
					outFile.printf(allCount + separator);
					
					if ( !batchMap.containsKey(student) ) {
						// Assign the student to a random batch (for cross-validation).
						batchMap.put(student,(int)(Math.random()*5.+1.));
					}
						
					int randnum = batchMap.get(student);
					
					for (int j = 0; j < clipLines.get(0).length; j++) {
						if ( Arrays.asList(presenceList).contains(headers[j]) ) {
							outFile.printf(presence[j] + separator);
						}
						if ( Arrays.asList(countList).contains(headers[j]) ) {
							outFile.printf(count[j] + separator);
						}
						if ( Arrays.asList(minList).contains(headers[j]) ) {
							outFile.printf(min[j]+ separator);
						}
						if ( Arrays.asList(maxList).contains(headers[j]) ) {
							outFile.printf(max[j] + separator);
						}
						if ( Arrays.asList(avgList).contains(headers[j]) ) {
							outFile.printf(sum[j]/allCount + separator);
						}
						if ( Arrays.asList(stdevList).contains(headers[j]) ) {
							outFile.printf(Math.sqrt(stdev[j]/Double.valueOf(allCount)) + separator);
						}
						if ( Arrays.asList(pctList).contains(headers[j]) ) {
							outFile.printf(Double.valueOf(count[j])/Double.valueOf(allCount) + separator);
						}
						if ( Arrays.asList(pctTimeList).contains(headers[j]) ) {
							outFile.printf(time[j]/totalTime + separator);
						}
						if ( Arrays.asList(firstList).contains(headers[j]) ) {
							outFile.printf(first[j] + separator);
						}
						if ( Arrays.asList(sumList).contains(headers[j]) ) {
							outFile.printf(sum[j] + separator);
						}
					}
					
					outFile.printf(randnum + separator);
					
					outFile.printf(behavior + separator);
					outFile.printf(affect + "\n");
					
					outFile.close();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
				for (String eachBehavior : behaviors.keySet()) {
					try {
						PrintWriter outFile = new PrintWriter(new FileWriter(outfiledir + eachBehavior.toLowerCase().replace(" ","") + outfilesuffix,true)); 
						
						int repeat = 0;
						if ( behavior.equalsIgnoreCase(eachBehavior) ) repeat = behaviors.get(eachBehavior)[0];
						else if ( behavior.equals("?") ) repeat = 0;
						else repeat = behaviors.get(eachBehavior)[1];
						
						if ( !batchMap.containsKey(student) ) {
							batchMap.put(student,(int)(Math.random()*5.+1.));
						}
							
						int randnum = batchMap.get(student);
						
						for ( int i = 0; i < repeat; i++ ) {
							outFile.printf(student + separator);
						outFile.printf(allCount + separator);
						
						for (int j = 0; j < clipLines.get(0).length; j++) {
							if ( Arrays.asList(presenceList).contains(headers[j]) ) {
								outFile.printf(presence[j] + separator);
							}
							if ( Arrays.asList(countList).contains(headers[j]) ) {
								outFile.printf(count[j] + separator);
							}
							if ( Arrays.asList(minList).contains(headers[j]) ) {
								outFile.printf(min[j]+ separator);
							}
							if ( Arrays.asList(maxList).contains(headers[j]) ) {
								outFile.printf(max[j] + separator);
							}
							if ( Arrays.asList(avgList).contains(headers[j]) ) {
								outFile.printf(sum[j]/allCount + separator);
							}
							if ( Arrays.asList(stdevList).contains(headers[j]) ) {
								outFile.printf(Math.sqrt(stdev[j]/Double.valueOf(allCount)) + separator);
							}
							if ( Arrays.asList(pctList).contains(headers[j]) ) {
								outFile.printf(Double.valueOf(count[j])/Double.valueOf(allCount) + separator);
							}
							if ( Arrays.asList(pctTimeList).contains(headers[j]) ) {
								outFile.printf(time[j]/totalTime + separator);
							}
							if ( Arrays.asList(firstList).contains(headers[j]) ) {
								outFile.printf(first[j] + separator);
							}
							if ( Arrays.asList(sumList).contains(headers[j]) ) {
								outFile.printf(sum[j] + separator);
							}
						}
						outFile.printf(randnum + separator);
		
						outFile.printf((behavior.equalsIgnoreCase(eachBehavior) ? behavior.replace(" ","") : "NOT") + "\n");
						}
						outFile.close();
					} catch ( Exception e ) {
						e.printStackTrace();
					}
					try {
						PrintWriter outFile = new PrintWriter(new FileWriter(outfiledir + eachBehavior.toLowerCase().replace(" ","") + "_unbiased_" + outfilesuffix,true)); 
						
						int repeat = 0;
						if ( behavior.equals("?") ) repeat = 0;
						else repeat = 1;
						
						if ( !batchMap.containsKey(student) ) {
							batchMap.put(student,(int)(Math.random()*5.+1.));
						}
							
						int randnum = batchMap.get(student);
						
						for ( int i = 0; i < repeat; i++ ) {
							outFile.printf(student + separator);
						outFile.printf(allCount + separator);
						
						for (int j = 0; j < clipLines.get(0).length; j++) {
							if ( Arrays.asList(presenceList).contains(headers[j]) ) {
								outFile.printf(presence[j] + separator);
							}
							if ( Arrays.asList(countList).contains(headers[j]) ) {
								outFile.printf(count[j] + separator);
							}
							if ( Arrays.asList(minList).contains(headers[j]) ) {
								outFile.printf(min[j]+ separator);
							}
							if ( Arrays.asList(maxList).contains(headers[j]) ) {
								outFile.printf(max[j] + separator);
							}
							if ( Arrays.asList(avgList).contains(headers[j]) ) {
								outFile.printf(sum[j]/allCount + separator);
							}
							if ( Arrays.asList(stdevList).contains(headers[j]) ) {
								outFile.printf(Math.sqrt(stdev[j]/Double.valueOf(allCount)) + separator);
							}
							if ( Arrays.asList(pctList).contains(headers[j]) ) {
								outFile.printf(Double.valueOf(count[j])/Double.valueOf(allCount) + separator);
							}
							if ( Arrays.asList(pctTimeList).contains(headers[j]) ) {
								outFile.printf(time[j]/totalTime + separator);
							}
							if ( Arrays.asList(firstList).contains(headers[j]) ) {
								outFile.printf(first[j] + separator);
							}
							if ( Arrays.asList(sumList).contains(headers[j]) ) {
								outFile.printf(sum[j] + separator);
							}
						}
						outFile.printf(randnum + separator);
						outFile.printf((behavior.equalsIgnoreCase(eachBehavior) ? behavior.replace(" ","") : "NOT") + "\n");
						}
						outFile.close();
					} catch ( Exception e ) {
						e.printStackTrace();
					}
				}
					
					for (String eachAffect : affects.keySet()) {
						try {
							PrintWriter outFile = new PrintWriter(new FileWriter(outfiledir + eachAffect.toLowerCase().replace(" ","") + outfilesuffix,true)); 
							
							int repeat = 0;
							if ( affect.equalsIgnoreCase(eachAffect) && 
									(!affect.equalsIgnoreCase("CONCENTRATING") || behavior.equalsIgnoreCase("ON TASK") )) 
								repeat = affects.get(eachAffect)[0];
							else if ( affect.equals("?") ) repeat = 0;
							else repeat = affects.get(eachAffect)[1];
							
							if ( !batchMap.containsKey(student) ) {
								batchMap.put(student,(int)(Math.random()*5.+1.));
							}
							
							int randnum = batchMap.get(student);
							
							for ( int i = 0; i < repeat; i++ ) {
								outFile.printf(student + separator);
							outFile.printf(allCount + separator);
							
							for (int j = 0; j < clipLines.get(0).length; j++) {
								if ( Arrays.asList(presenceList).contains(headers[j]) ) {
									outFile.printf(presence[j] + separator);
								}
								if ( Arrays.asList(countList).contains(headers[j]) ) {
									outFile.printf(count[j] + separator);
								}
								if ( Arrays.asList(minList).contains(headers[j]) ) {
									outFile.printf(min[j]+ separator);
								}
								if ( Arrays.asList(maxList).contains(headers[j]) ) {
									outFile.printf(max[j] + separator);
								}
								if ( Arrays.asList(avgList).contains(headers[j]) ) {
									outFile.printf(sum[j]/allCount + separator);
								}
								if ( Arrays.asList(stdevList).contains(headers[j]) ) {
									outFile.printf(Math.sqrt(stdev[j]/Double.valueOf(allCount)) + separator);
								}
								if ( Arrays.asList(pctList).contains(headers[j]) ) {
									outFile.printf(Double.valueOf(count[j])/Double.valueOf(allCount) + separator);
								}
								if ( Arrays.asList(pctTimeList).contains(headers[j]) ) {
									outFile.printf(time[j]/totalTime + separator);
								}
								if ( Arrays.asList(firstList).contains(headers[j]) ) {
									outFile.printf(first[j] + separator);
								}
								if ( Arrays.asList(sumList).contains(headers[j]) ) {
									outFile.printf(sum[j] + separator);
								}
							}
							outFile.printf(randnum + separator);

							outFile.printf(
									(affect.equalsIgnoreCase(eachAffect) && 
											(!affect.equalsIgnoreCase("CONCENTRATING") || behavior.equalsIgnoreCase("ON TASK") ) 
											? affect.replace(" ","") : "NOT") + "\n");
							}
							outFile.close();
						} catch ( Exception e ) {
							e.printStackTrace();
						}
						try {
							PrintWriter outFile = new PrintWriter(new FileWriter(outfiledir + eachAffect.toLowerCase().replace(" ","") + "_unbiased_" + outfilesuffix,true)); 
							
							int repeat = 0;
							
							if ( affect.equals("?") ) repeat = 0;
							else repeat = 1;
							
							if ( !batchMap.containsKey(student) ) {
								batchMap.put(student,(int)(Math.random()*5.+1.));
							}
							
							int randnum = batchMap.get(student);
							
							for ( int i = 0; i < repeat; i++ ) {
								outFile.printf(student + separator);
							outFile.printf(allCount + separator);
							
							for (int j = 0; j < clipLines.get(0).length; j++) {
								if ( Arrays.asList(presenceList).contains(headers[j]) ) {
									outFile.printf(presence[j] + separator);
								}
								if ( Arrays.asList(countList).contains(headers[j]) ) {
									outFile.printf(count[j] + separator);
								}
								if ( Arrays.asList(minList).contains(headers[j]) ) {
									outFile.printf(min[j]+ separator);
								}
								if ( Arrays.asList(maxList).contains(headers[j]) ) {
									outFile.printf(max[j] + separator);
								}
								if ( Arrays.asList(avgList).contains(headers[j]) ) {
									outFile.printf(sum[j]/allCount + separator);
								}
								if ( Arrays.asList(stdevList).contains(headers[j]) ) {
									outFile.printf(Math.sqrt(stdev[j]/Double.valueOf(allCount)) + separator);
								}
								if ( Arrays.asList(pctList).contains(headers[j]) ) {
									outFile.printf(Double.valueOf(count[j])/Double.valueOf(allCount) + separator);
								}
								if ( Arrays.asList(pctTimeList).contains(headers[j]) ) {
									outFile.printf(time[j]/totalTime + separator);
								}
								if ( Arrays.asList(firstList).contains(headers[j]) ) {
									outFile.printf(first[j] + separator);
								}
								if ( Arrays.asList(sumList).contains(headers[j]) ) {
									outFile.printf(sum[j] + separator);
								}
							}
							outFile.printf(randnum + separator);

							outFile.printf(
									(affect.equalsIgnoreCase(eachAffect) && 
											(!affect.equalsIgnoreCase("CONCENTRATING") || behavior.equalsIgnoreCase("ON TASK") ) 
											? affect.replace(" ","") : "NOT") + "\n");
							}
							outFile.close();
						} catch ( Exception e ) {
							e.printStackTrace();
						}
					}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String arg) {
		String mainFolder = arg;
		infile = mainFolder + "/Data_All_Synced_contextual_features.txt";
		outfiledir = mainFolder + "/analytics/";
		if (!(new File(outfiledir)).exists()) {
			(new File(outfiledir)).mkdir();
		}
		AggregateClips m = new AggregateClips();
		
		m.readData();
		
	}
}