/* RMGenerateFeatures.java
 * 
 * This class performs the entire feature generation.  It aggregates the 
 * relevant data files, does some initial data cleanup, and calculates a large
 * feature-set.  It then aggregates the data into clips in preparation for 
 * fitting.
 * 
 * @author Neal Miller
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RMGenerateFeatures {
	List<Action> allActions = new ArrayList<Action>();
	private final int studentColumn = 0;
	private static final int startTimeColumn = 1;
	private static final int lessonColumn = 2;
	private static final int cellColumn = 3;
	private static final int itemTypeColumn = 6;
	private static final int outcomeColumn = 7;
	private static final int skillColumn = 11;
	private static final int varietyColumn = 4;
	private static final int datasetColumn = 5;
	private static final int durationColumn = 12;
	private static final int answerFormatColumn = 8;
	private static final int hintsColumn = 10;
	private static Map<String,String[]> obsMap = new HashMap<String,String[]>();
	private static Map<String,Double> totalTimeMap = new HashMap<String,Double>();
	private static Map<String,Double> timeVarianceMap = new HashMap<String,Double>();
	private static Map<String,Integer> itemCountMap = new HashMap<String,Integer>();
	private static Map<String,Integer> studentItemCountMap = new HashMap<String,Integer>();
	private static Map<String,Integer> itemRightCount = new HashMap<String,Integer>();
	private static Map<String,Integer> itemWrongCount = new HashMap<String,Integer>();
	private static long secsBefore = 0;
	private static long obsLength = 20;
	private static long secsAfter = 0;
	
	private static long secsBefore2 = 2*60*60;
	
	private static int numColumns = 16;
	
	private static int linenum = 0;
	private static double avgDifficulty = 0.0;
	private static double avgDuration = 0.0;
	
	private static String mainFolder = "";
	
	private static String infile = "../../RMDetectors/allData.txt";
	private static String outfile = "../../RMDetectors/Data_All_Synced.txt";
	private static String obsFile = "../../RMDetectors/syncedDataAll.txt";
	private static String BKTfile = "../../RMDetectors/BKTParams.txt";
	
	private static HashMap<String,Double[]> BKTParams = new HashMap<String,Double[]>();
	private static HashMap<String,Double> Pknow = new HashMap<String,Double>();
	
	private static int L0 = 0;
	private static int G = 1;
	private static int S = 2;
	private static int T = 3;
	
	private String formatSkill(String skill) {
		String newskill = skill.replace(" ","");
		newskill = newskill.replace(",","");
		newskill = newskill.replace("Review:","");
		newskill = newskill.replace(":","");
		newskill = newskill.replace("(","");
		newskill = newskill.replace(")","");
		newskill = newskill.replace("/","");
		newskill = newskill.replace("-","");
		newskill = newskill.replace("\"","");
		newskill = newskill.replace("CircleandDisk","CirclesandDisks");
		return newskill;
	}
	
	private void separateActions() {
		/* Separates instances in which multiple actions were condensed into one line.
		 * In particular, this occurs often in Theory blocks, where the log line has, e.g.,
		 * "correct;incorrect;correct;correct"
		 */
		try {
			BufferedReader br = new BufferedReader(new FileReader(infile));
			PrintWriter pw = new PrintWriter(new File(infile.replaceAll(".txt","_temp.txt")));
			
			String line = br.readLine();
			pw.println(line);
			
			while ( (line = br.readLine()) != null ) {
				String[] values = line.split("\t");
				if ( !values[outcomeColumn].contains(";") ) {
					pw.println(line);
				} else {
					Integer i = 0;
					Integer numActions = values[outcomeColumn].split(";").length;
					
					SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
					Calendar startTime = Calendar.getInstance();
					startTime.setTime(parser.parse(values[startTimeColumn]));
					Double totalDuration = Double.valueOf(values[durationColumn]);
					
					for ( String outcome : values[outcomeColumn].split(";") ) {
						for ( Integer j = 0; j < values.length; j++ ) {
							switch (j) {
							case startTimeColumn: 
								pw.printf(parser.format(startTime.getTime()) + "\t");
								startTime.add(Calendar.MILLISECOND,(int)(1000.*(double)totalDuration/(double)numActions));
								break;
							case cellColumn:
								pw.printf(values[j] + i + "\t");
								break;
							case outcomeColumn:
								pw.printf(outcome + "\t");
								break;
							case durationColumn:
								pw.printf(((double)totalDuration/(double)numActions) + "\t");
								break;
							default:
								pw.printf(values[j] + "\t");
								break;
							}
						}
						pw.printf("\n");
						i++;
					}
				}
			}
			br.close();
			pw.close();
			
			File oldFile = new File(infile);
			oldFile.delete();
			
			File tempFile = new File(infile.replaceAll(".txt","_temp.txt"));
			
			Files.move(tempFile.toPath(), new File(infile).toPath());
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	private void getBKTParams() {
		/* Read the BKT parameters */
		try {
			BufferedReader br = new BufferedReader(new FileReader(BKTfile));
			
			br.readLine();
			
			String line = "";
			String[] values = new String[7];
			
			while ( (line = br.readLine()) != null ) {
				values = line.split("\t");
				BKTParams.put(values[0],new Double[]{Double.parseDouble(values[L0+1]),Double.parseDouble(values[G+1]),Double.parseDouble(values[S+1]),Double.parseDouble(values[T+1])});
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private List<String> getStudentList() {
		/* Get a list of all students in the file */
		List<String> studentList = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(infile));
		
			String line = "";
			String[] values = new String[numColumns];

			br.readLine(); //headers
			while ((line = br.readLine()) != null ) {
				values = line.split("\t");
				if ( values.length > skillColumn ) {
					if ( !studentList.contains(values[studentColumn]) ) {
						studentList.add(values[studentColumn]);
					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return studentList;
	}
	
	private void getItemTimes() {
		/* Get all of the item times in the log.
		 * 
		 * This is used to calculate the values that are references to aggregates, e.g.
		 * the time in units of standard deviations from the mean.
		 */
		try {
			BufferedReader br = new BufferedReader(new FileReader(infile));
		
			String line = "";
			String[] values = new String[numColumns];
			
			int totalRight = 0;
			int totalWrong = 0;

			br.readLine(); //headers
			while ((line = br.readLine()) != null ) {

				values = line.split("\t");
				if ( values.length > durationColumn ) {
					if (!values[durationColumn].equals(".")) {
						Double duration = Double.valueOf(values[durationColumn]);
						String cell = values[cellColumn];
						if ( !totalTimeMap.containsKey(cell) ) {
							totalTimeMap.put(cell,0.0);
							timeVarianceMap.put(cell,0.0);
							itemCountMap.put(cell,0);
						}
						totalTimeMap.put(cell,totalTimeMap.get(cell)+duration);
						itemCountMap.put(cell,itemCountMap.get(cell)+1);
						
						String outcome = values[outcomeColumn];
						if ( !outcome.equals(".") ) {
							if ( !itemRightCount.containsKey(cell) ) {
								itemRightCount.put(cell,0);
								itemWrongCount.put(cell,0);
							}
							for ( String value : outcome.split(";") ) {
								if ( value.equals("correct") || value.equals("passed") ) {
									itemRightCount.put(cell,itemRightCount.get(cell)+1);
									totalRight++;
								} else if ( value.equals("incorrect") || value.equals("not passed") ) {
									itemWrongCount.put(cell,itemWrongCount.get(cell)+1);
									totalWrong++;
								}
							}	
						}
					}
				}
			}
			
			Double durationSum = 0.0;
			Integer totalCount = 0;
			for ( String cell : totalTimeMap.keySet() ) {
				durationSum += totalTimeMap.get(cell);
				totalCount += itemCountMap.get(cell);
			}
			avgDuration = durationSum/totalCount;
			
			avgDifficulty = (double)totalWrong/(double)(totalRight+totalWrong);
			br.close();
			
			br = new BufferedReader(new FileReader(infile));
			
			line = "";
			values = new String[numColumns];

			br.readLine(); //headers
			while ((line = br.readLine()) != null ) {

				values = line.split("\t");
				if ( values.length > durationColumn ) {
					if (!values[durationColumn].equals(".")) {
						Double duration = Double.valueOf(values[durationColumn]);
						String cell = values[cellColumn];
						Double avgTime = totalTimeMap.get(cell)/itemCountMap.get(cell);
						timeVarianceMap.put(cell,timeVarianceMap.get(cell) + (duration-avgTime)*(duration-avgTime));
					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setClips(List<Action> allActions) {
		/* Set the "clip id" for all observed actions.
		 */
		for ( int i = allActions.size() - 1; i >= 0; i-- )
			if ( !allActions.get(i).getBehavior().equals("Not Observed") ) {
				
				String clip = "";
				
				if ( allActions.get(i).getClip().equals(".") ) clip = "clip" + allActions.get(i).getClipId();
				else clip = allActions.get(i).getClip();
				
				allActions.get(i).setClip(clip);
		}
	}
	
	private double getTimeSD(String cell, String duration) {
		/* Convert a specific action duration into units of standard deviations from the mean.
		 */
		double avgTime = totalTimeMap.get(cell)/itemCountMap.get(cell);
		double timeSD = Math.sqrt(timeVarianceMap.get(cell)/itemCountMap.get(cell));
		if ( duration.equals(".") )
			return 0;
		else if (timeSD > 0)
			return (Double.valueOf(duration) - avgTime)/timeSD;
		else
			return 0;
	}
	
	private void setObs() {
		/* Correctly sync the observations to the log file.
		 * 
		 * The original observation file was synced incorrectly.
		 */
		obsMap = new HashMap<String,String[]>();
		
		List<String> students = getStudentList();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(obsFile));
			PrintWriter pw = new PrintWriter(new File(obsFile.replace(".txt","_new.txt")));
			String line = "";
		
			BufferedReader br2 = new BufferedReader(new FileReader(infile)); 
			pw.println(br2.readLine() + "BEHAVIOR\tAFFECT\tTSTART\tTEND");
			br2.close();
			pw.close();

			line = br.readLine(); //headers
			String[] values = line.split("\t");
			
			Integer matched = 0;
			Integer total = 0;
			Integer i = 0;
			
			List<String> prevObs = new ArrayList<String>();
			
			while ((line = br.readLine()) != null ) {
				values = line.split("\t");
				
				i++;
				
				String obsId = values[0] + values[1].split(" ")[0] + values[14];
				
				if ( !values[12].equals("Not Observed") && students.contains(values[0]) && !prevObs.contains(obsId) ) {
					prevObs.add(obsId);
					String student = values[0];
					
					BufferedReader logFile = new BufferedReader(new FileReader(infile));
					
					SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

					Date tstart = parser.parse(values[1].split(" ")[0] + " " + values[14]);
					
					String line2 = logFile.readLine();
					
					Boolean found = false;
					
					// Loop through the log file for each observation (this takes a while)
					while ( (line2 = logFile.readLine()) != null ) {
					
						String[] values2 = line2.split("\t");
						
						String student2 = values2[studentColumn];
						
						if ( (student.equals(student2)) && values[1].split(" ")[0].equals(values2[startTimeColumn].split(" ")[0]) ) {
							Date starttime = parser.parse(values2[startTimeColumn]);
							
							Double duration = values2[durationColumn].equals(".") ? 0 : Double.valueOf(values2[durationColumn]);
							
							// If the clip starts or end within the observation window...
							if ( (starttime.getTime() + duration*1000. > tstart.getTime() - secsBefore*1000.) && 
									(starttime.getTime() < tstart.getTime() + obsLength*1000. + secsAfter*1000.) ) {
								found = true;
								
								pw = new PrintWriter(new FileWriter(obsFile.replace(".txt","_new.txt"),true));
								pw.println(line2 + values[12] + "\t" + values[13] + "\t" + values[14] + "\t" + values[15]);
								pw.close();
								
								obsMap.put(values2[0]+values2[1],new String[]{values[12],values[13],values[14],values[15]});
							} else if ( found ) {
								// Break out of the loop once we've found the window and left it.
								break;
							}
						}
					}
					logFile.close();
					if ( found ) matched++;
					total++;
				}
			}
			
			br.close();
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		obsFile = obsFile.replace(".txt","_new.txt");
	}
	
	private void getObs() {
		/* Get the observations from the observation file.
		 */
		obsMap = new HashMap<String,String[]>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(obsFile));
			
			String line = br.readLine();;

			line = br.readLine(); //headers
			
			while ((line = br.readLine()) != null ) {
				String[] values = line.split("\t");
				
				obsMap.put(values[0] + values[1],new String[]{values[13],values[14],values[15],values[16]});
			}
			br.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	private void readData() {
		/* This method reads the data, and outputs it plus several features.
		 */
		try {
			BufferedReader br = new BufferedReader(new FileReader(infile));
			allActions = new ArrayList<Action>();
			
			Map<String,Integer> studentCellRightCount = new HashMap<String,Integer>();
			Map<String,Integer> studentCellWrongCount = new HashMap<String,Integer>();
			Map<String,Integer> studentSkillRightCount = new HashMap<String,Integer>();
			Map<String,Integer> studentSkillWrongCount = new HashMap<String,Integer>();
			Map<String,Integer> studentSkillHintsCount = new HashMap<String,Integer>();
			
			String prevCell = "";
			Integer cellCountConsecutive = 0;
			Integer wrongConsecutive = 0;
			
			String line = "";
			String[] values = new String[numColumns];
			String student = "";
			String day = "";
			
			Integer numactionsDay = 0;
			Map<String, List<Double>> roughDetectorMap = new HashMap<String,List<Double>>();
			
			roughDetectorMap.put("wrongtimestimeSD",new ArrayList<Double>());
			roughDetectorMap.put("perccorrectskill",new ArrayList<Double>());
			roughDetectorMap.put("tgreaterthan180",new ArrayList<Double>());
			roughDetectorMap.put("wrongtimestimeSD",new ArrayList<Double>());
			roughDetectorMap.put("timeSDlongwindowdiff",new ArrayList<Double>());
			roughDetectorMap.put("prevhinttimestimeSD",new ArrayList<Double>());
			
			Map<String, Double> sumMap = new HashMap<String,Double>();
			
			sumMap.put("right",0.);
			sumMap.put("timeSD",0.);
			sumMap.put("tSDgreaterthan1",0.);
			sumMap.put("perccorrectskill",0.);
			sumMap.put("wrongtimestimeSD",0.);
			sumMap.put("righttimestimeSD",0.);
			sumMap.put("prevhinttimestimeSD",0.);
			
			Boolean prevhint = false;
			
			List<String> itemList = new ArrayList<String>();
			
			br.readLine(); //headers
			while ((line = br.readLine()) != null ) {
				values = line.split("\t");
				if ( values.length > studentColumn ) {
					// If we've changed students, print everything for the previous student...
					if ( !values[studentColumn].equals(student) ) {
						try {
							PrintWriter outFile = new PrintWriter(new FileWriter(outfile,true)); 
						 
							for ( Action action : allActions ) {
								outFile.printf(action.getNum() + "\t");
								outFile.printf(action.getBehavior() + "\t");
								outFile.printf(action.getAffect() + "\t");
								outFile.printf(action.getTStart() + "\t");
								outFile.printf(action.getTEnd() + "\t");
								outFile.printf(action.getClip() + "\t");
								outFile.printf(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(action.getStartTime()) + "\t");
								outFile.printf(action.getLesson() + "\t");
								outFile.printf(action.getStudent() + "\t");
								outFile.printf(action.getSkill() + "\t");
								outFile.printf(action.getCell() + "-" + action.getVariety() + "-" + action.getDataset() + "\t");
								outFile.printf(action.getRight() + "\t");
								
								outFile.printf(action.getItemType().equals("Theory") ? "1\t" : "0\t");
								outFile.printf(action.getItemType().equals("SpeedGame") ? "1\t" : "0\t");
								outFile.printf(action.getItemType().equals("Problem") ? "1\t" : "0\t");
								outFile.printf(action.getItemType().equals("NotesTests") ? "1\t" : "0\t");
								
								outFile.printf(action.getAnswerFormat().contains("Enter an expression") ? "1\t" : "0\t");
								outFile.printf(action.getAnswerFormat().contains("Fill-in-the-blanks") ? "1\t" : "0\t");
								outFile.printf(action.getAnswerFormat().contains("Interactive answer") ? "1\t" : "0\t");
								outFile.printf(action.getAnswerFormat().contains("Multiple choice for Check Boxes") ? "1\t" : "0\t");
								outFile.printf(action.getAnswerFormat().contains("Multiple choice with Combo Boxes") ? "1\t" : "0\t");
								outFile.printf(action.getAnswerFormat().contains("Multiple choice with Radio Button") ? "1\t" : "0\t");
								
								outFile.printf(action.getDuration() + "\t");
								if ( action.getDuration().equals(".") ) {
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
								} else {
									Double time = Double.valueOf(action.getDuration());
									outFile.printf(getTimeSD(action.getCell(),action.getDuration()) + "\t");
									outFile.printf((action.getRight() < 0  ? "0" : action.getRight()*time) + "\t");
									outFile.printf((action.getRight() < 0  ? "0" : action.getRight()*getTimeSD(action.getCell(),action.getDuration())) + "\t");
									outFile.printf((action.getRight() < 0  ? "0" : (1.0-action.getRight())*time) + "\t");
									outFile.printf((action.getRight() < 0  ? "0" : (1.0-action.getRight())*getTimeSD(action.getCell(),action.getDuration())) + "\t");
									outFile.printf(time > 80 ? "1\t" : "0\t");
									outFile.printf(time > 180 ? "1\t" : "0\t");
									outFile.printf(time < 3 ? "1\t" : "0\t");
									
									Double timeSDdiff = getTimeSD(action.getCell(),action.getDuration());
									
									outFile.printf(timeSDdiff > 1 ? "1\t" : "0\t");
									outFile.printf(timeSDdiff < -1 ? "1\t" : "0\t");
									
									// Calculate averages across several previous actions
									Double timeSDdiff2 = getTimeSD(action.getCell(),action.getDuration());
									Double time3SD = getTimeSD(action.getCell(),action.getDuration());
									Double time5SD = time3SD;
									if ( allActions.indexOf(action) > 0 ) {
										Integer idx = allActions.indexOf(action) - 1;
										Action prevAction = allActions.get(idx);
										timeSDdiff = getTimeSD(action.getCell(),action.getDuration()) - getTimeSD(prevAction.getCell(),prevAction.getDuration());
										timeSDdiff2 = timeSDdiff;
										time3SD += getTimeSD(prevAction.getCell(),prevAction.getDuration());
										time5SD = time3SD;
										if ( allActions.indexOf(action) > 1 ) {
											Action prev2Action = allActions.get(idx - 1);
											Double timeSDdiffprev = getTimeSD(prevAction.getCell(),prevAction.getDuration()) - getTimeSD(prev2Action.getCell(),prev2Action.getDuration());
											timeSDdiff2 = timeSDdiff - timeSDdiffprev;
											time3SD += getTimeSD(prev2Action.getCell(),prev2Action.getDuration());
											time5SD = time3SD;
											if ( allActions.indexOf(action) > 2) {
												time5SD += getTimeSD(allActions.get(idx - 2).getCell(),allActions.get(idx - 2).getDuration());
												if ( allActions.indexOf(action) > 3) {
													time5SD += getTimeSD(allActions.get(idx - 3).getCell(),allActions.get(idx - 3).getDuration());
												}
											}
										}
									}
									
									outFile.printf(time3SD + "\t");
									outFile.printf(time5SD + "\t");
									outFile.printf(timeSDdiff + "\t");
									outFile.printf(timeSDdiff2 + "\t");
								}
								
								long obsTime = action.getTStartDate().getTime();
								
								int j = allActions.indexOf(action);
								Double sumDuration = 0.;
								Double sumTimeSD = 0.;
								Double sumRight = 0.;
								Double sumDifficulty = 0.;
								Double sumPknow = 0.;
								Double sumTheoryDuration = 0.;
								Double sumProblemDuration = 0.;
								int problemCount = 0;
								int speedGameCount = 0;
								int count = 0;
								int durationcount = 0;
								int rightcount = 0;
								// Aggregate over the previous "long period" (default 2 hours)
								while ( j >= 0 && (obsTime - allActions.get(j).getStartTime().getTime() <= secsBefore2*1000 )) {
									if ( !allActions.get(j).getDuration().equals(".") ) {
										sumDuration += Double.valueOf(allActions.get(j).getDuration());
										
										sumTheoryDuration += allActions.get(j).getItemType().equals("Theory") ? Double.valueOf(allActions.get(j).getDuration()) : 0.;
										sumProblemDuration += allActions.get(j).getItemType().equals("Problem") ? Double.valueOf(allActions.get(j).getDuration()) : 0.;
												
										sumTimeSD += getTimeSD(allActions.get(j).getCell(),allActions.get(j).getDuration());
										durationcount++;
									}
									
									if ( !(Double.valueOf(allActions.get(j).getRight()) < 0) ) {
										sumRight += Double.valueOf(allActions.get(j).getRight());
										rightcount++;
									}
									
									double difficulty = 0.0;
									if ( itemRightCount.containsKey(allActions.get(j).getCell()) )
										difficulty = ((double)itemWrongCount.get(allActions.get(j).getCell())/(double)(itemWrongCount.get(allActions.get(j).getCell()) + itemRightCount.get(allActions.get(j).getCell())));
									else
										difficulty = avgDifficulty;
									
									sumDifficulty += difficulty;
									sumPknow += Double.valueOf(allActions.get(j).getPknow());
									
									if ( allActions.get(j).getItemType().equals("Problem") ) problemCount++;
									if ( allActions.get(j).getItemType().equals("SpeedGame") ) speedGameCount++;
									
									count++;
									
									j--;
								}
								
								double difficulty = 0.0;
								if ( itemRightCount.containsKey(action.getCell()) )
									difficulty = ((double)itemWrongCount.get(action.getCell())/(double)(itemWrongCount.get(action.getCell()) + itemRightCount.get(action.getCell())));
								else
									difficulty = avgDifficulty;
								
								if ( count > 0 ) {
									outFile.printf(sumDuration/durationcount + "\t");
									String duration = action.getDuration();
									if ( duration.equals(".") )
										outFile.printf("0\t");
									else
										outFile.printf((Double.valueOf(duration) - sumDuration/durationcount) + "\t");
									outFile.printf(sumTimeSD/durationcount + "\t");
									if ( duration.equals(".") )
										outFile.printf("0\t");
									else
										outFile.printf((getTimeSD(action.getCell(),duration) - sumTimeSD/durationcount) + "\t");
									outFile.printf(sumDifficulty/count + "\t");
									outFile.printf((difficulty - sumDifficulty/count) + "\t");
									outFile.printf(sumPknow/count + "\t");
									outFile.printf((Double.valueOf(action.getPknow()) - sumPknow/count) + "\t");
									if ( rightcount > 0 ) 
										outFile.printf(sumRight/rightcount + "\t");
									else
										outFile.printf((1-avgDifficulty) + "\t");
									outFile.printf(count + "\t");
									if (sumDuration > 0) {
										outFile.printf(sumTheoryDuration  + "\t");
										outFile.printf(sumTheoryDuration/sumDuration  + "\t");
										outFile.printf(sumProblemDuration  + "\t");
										outFile.printf(sumProblemDuration/sumDuration  + "\t");
									} else {
										outFile.printf("0\t");
										outFile.printf("0\t");
										outFile.printf("0\t");
										outFile.printf("0\t");
									}
									outFile.printf(problemCount  + "\t");
									outFile.printf(speedGameCount  + "\t");
								} else {
									outFile.printf(avgDuration + "\t");
									String duration = action.getDuration();
									if ( duration.equals(".") )
										outFile.printf("0\t");
									else
										outFile.printf((Double.valueOf(duration) - avgDuration) + "\t");
									outFile.printf("0\t");
									if ( duration.equals(".") )
										outFile.printf("0\t");
									else
										outFile.printf(getTimeSD(action.getCell(),duration) + "\t");
									outFile.printf(avgDifficulty + "\t");
									outFile.printf((difficulty - avgDifficulty) + "\t");
									outFile.printf("0.5\t");
									outFile.printf((Double.valueOf(action.getPknow()) - 0.5) + "\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
								}
								
								outFile.printf(action.getL0() + "\t");
								outFile.printf(action.getG() + "\t");
								outFile.printf(action.getS() + "\t");
								outFile.printf(action.getT() + "\t");
								outFile.printf(action.getPknow() + "\t");
					
								outFile.printf(difficulty + "\t");
								
								outFile.printf(action.getCellCount() + "\t");
								outFile.printf(action.getCellCountConsecutive() + "\t");
								outFile.printf(action.getWrongConsecutive() + "\t");
								
								if ( action.getCellRightCount() + action.getCellWrongCount() == 0 ) {
									outFile.printf((1-avgDifficulty) + "\t");
									outFile.printf(avgDifficulty + "\t");
								} else {
									double percentCorrect = (double) action.getCellRightCount() / (double)(action.getCellRightCount() + action.getCellWrongCount());
									outFile.printf(percentCorrect + "\t");
									outFile.printf((1.0-percentCorrect) + "\t");
								}
								
								outFile.printf(action.getCellWrongCount() + "\t");
								
								if ( action.getSkillRightCount() + action.getSkillWrongCount() == 0 ) {
									outFile.printf((1-avgDifficulty) + "\t");
									outFile.printf(avgDifficulty + "\t");
								} else {
									double percentCorrect = (double) action.getSkillRightCount() / (double)(action.getSkillRightCount() + action.getSkillWrongCount());
									outFile.printf(percentCorrect + "\t");
									outFile.printf((1.0-percentCorrect) + "\t");
								}
								
								outFile.printf(action.getSkillWrongCount() + "\t");
								
								outFile.printf(action.getHints() + "\t");	//Number of hints
								outFile.printf((action.getHints() > 0 ? "1" : "0") + "\t");		//Hint used? Binary.
								outFile.printf( ( ((action.getPknow() < 0.7) && (action.getHints() == 0) && (action.getRight() == 0) ) ? "1" : "0") + "\t" );	//Low pknow, no hint requested, wrong answer
								outFile.printf( ( ((action.getPknow() > 0.9) && (action.getHints() > 0)) ? "1" : "0") + "\t" );									//High pknow, hint requested
								
								
								Integer prevstephintused = 0;
								
								if ( allActions.indexOf(action) > 0 ) 
									prevstephintused = allActions.get(allActions.indexOf(action)-1).getHints() > 0 ? 1 : 0;
										
								if ( action.getDuration().equals(".") ) {
									outFile.printf("0\t");
									outFile.printf("0\t");
									outFile.printf("0\t");
								} else {
									outFile.printf( Double.valueOf(action.getDuration())/(action.getHints()+1) + "\t" );		//Time per hint (plus attempt)
									outFile.printf( (prevstephintused*Double.valueOf(action.getDuration())) + "\t" );
									outFile.printf( (prevstephintused*getTimeSD(action.getCell(),action.getDuration())) + "\t" );
								}
								
								outFile.printf( action.getSkillHintsCount() + "\t");
								
								outFile.printf( (action.getSkillHintsCount() + action.getSkillWrongCount()) + "\t");
								
								outFile.printf(action.getPred("bored") + "\t");
								outFile.printf(action.getPred("concentrating") + "\t");
								outFile.printf(action.getPred("offtask") + "\t");
								outFile.printf(action.getPred("ontask") + "\t");
								
								Boolean firstattempt = false;
								if ( action.getFirstAttempt() ) {
									Integer nextans1 = -1;
									Integer currentIdx = allActions.indexOf(action);
									Integer Idxplus1 = currentIdx;
									firstattempt = false;
									while ( (nextans1 == -1) || (!firstattempt) || (!allActions.get(Idxplus1).getSkill().equals(action.getSkill()))) {	
										//Avoid Theory block without answers and additional attempts
										if ( Idxplus1 < allActions.size()-1 ) {
											Idxplus1++;
											nextans1 = (int)allActions.get(Idxplus1).getRight();
											firstattempt = allActions.get(Idxplus1).getFirstAttempt();
										} else {
											break;
										}
									}
									
									
									Integer nextans2 = -1;
									Integer Idxplus2 = Idxplus1;
									firstattempt = false;
									while ( (nextans2 == -1) || (!firstattempt) || (!allActions.get(Idxplus2).getSkill().equals(action.getSkill()))) {
										if ( Idxplus2 < allActions.size()-1 ) {
											Idxplus2++;
											nextans2 = (int)allActions.get(Idxplus2).getRight();
											firstattempt = allActions.get(Idxplus2).getFirstAttempt();
										} else {
											break;
										}
									}				
								}
								
								outFile.printf("eol\n");
							}
							itemList = new ArrayList<String>();
							outFile.close();
						} catch (Exception e ) {
							e.printStackTrace();
						}

						allActions = new ArrayList<Action>();
						student = values[studentColumn];
						Pknow = new HashMap<String,Double>();
						studentItemCountMap = new HashMap<String,Integer>();
						itemList = new ArrayList<String>();
						studentCellRightCount = new HashMap<String,Integer>();
						studentCellWrongCount = new HashMap<String,Integer>();
						studentSkillRightCount = new HashMap<String,Integer>();
						studentSkillWrongCount = new HashMap<String,Integer>();
						studentSkillHintsCount = new HashMap<String,Integer>();
						prevCell = "";
						cellCountConsecutive = 0;
						wrongConsecutive = 0;
						day = "";
					}
					
					// If we haven't changed students, aggregate and read the relevant data
					if ( values.length > durationColumn ) {
						List<String> prevSkills = new ArrayList<String>();
						String studentValue = values[studentColumn];
						if ( studentValue.equals(student) ) {
							if ( !values[startTimeColumn].split(" " )[0].equals(day) ) {
								day = values[startTimeColumn].split(" " )[0];

								sumMap.put("right",0.);
								sumMap.put("timeSD",0.);
								sumMap.put("tSDgreaterthan1",0.);
								sumMap.put("perccorrectskill",0.);
								sumMap.put("wrongtimestimeSD",0.);
								sumMap.put("righttimestimeSD",0.);
								sumMap.put("prevhinttimestimeSD",0.);
								
								prevhint = false;
								
								numactionsDay = 0;
							}
								
							for ( String skillValue : values[skillColumn].split(";") ) {
								if ( !prevSkills.contains(formatSkill(skillValue)) /*&& (values[itemTypeColumn].equals("Problem")  || values[itemTypeColumn].equals("SpeedGame") || values[itemTypeColumn].equals("Theory"))*/) {
									Double[] BKT = {0.0,0.0,0.0,0.0};
									Double pknow = 0.0;
									String cell = values[cellColumn];
									
									if (!studentCellRightCount.containsKey(values[cellColumn])) {
										studentCellRightCount.put(values[cellColumn],0);
										studentCellWrongCount.put(values[cellColumn],0);
										studentItemCountMap.put(values[cellColumn],0);
									}
									if (!studentSkillRightCount.containsKey(formatSkill(skillValue))) {
										studentSkillRightCount.put(formatSkill(skillValue),0);
										studentSkillWrongCount.put(formatSkill(skillValue),0);
										studentSkillHintsCount.put(formatSkill(skillValue),0);
									}
									
									if ( !cell.equals(prevCell) ) {
										prevCell = cell;
										cellCountConsecutive = 0;
									}
									Boolean firstattempt = false;
									if (BKTParams.containsKey(formatSkill(skillValue))) {
										if (!Pknow.containsKey(formatSkill(skillValue)))
											Pknow.put(formatSkill(skillValue),BKTParams.get(formatSkill(skillValue))[L0]);
	
										pknow = Pknow.get(formatSkill(skillValue));
										BKT = BKTParams.get(formatSkill(skillValue));
										double correct = 0;
										
										double right = 0;
										double total = 0;
										if ( values[itemTypeColumn].equals("Theory") ) {
											if ( values[outcomeColumn].equals(".") ) {
												correct = -1;
											} else {
												for ( String value : values[outcomeColumn].split(";") ) {
													if ( value.equals("correct") && values[hintsColumn].equals("0") ) {
														right++;
													}
													total++;
												}
												correct = right/total;
											}
										} else {
											if ( (values[outcomeColumn].equals("correct") || values[outcomeColumn].equals("passed")) && values[hintsColumn].equals("0")  ) {
												correct = 1;
											} else {
												correct = 0;
											}
										}
										
										if ( correct == 0 )
											wrongConsecutive++;
										else
											wrongConsecutive = 0;
										
										double pknowgivenanswer = pknow;
										
										if (!itemList.contains(values[skillColumn] + values[cellColumn] + "-" + values[varietyColumn] + "-" + values[datasetColumn])) {
											if ( correct != -1 ) {
												pknowgivenanswer = correct*((pknow * (1.0-BKT[S])) / ((pknow * (1-BKT[S])) + ((1.0 - pknow) * BKT[G])));
												pknowgivenanswer += (1.0-correct)*((pknow*BKT[S]) / ((pknow * BKT[S]) + ((1.0-pknow) * (1.0 - BKT[G]))));
											}
											itemList.add(values[skillColumn] + values[cellColumn] + "-" + values[varietyColumn] + "-" + values[datasetColumn]);
											firstattempt = true;
										}
										
										Pknow.put(formatSkill(skillValue),pknowgivenanswer + (1.0 - pknowgivenanswer)*BKT[T]);
									}
									
									String outcome = values[outcomeColumn];
									cellCountConsecutive += 1;
									studentItemCountMap.put(cell,studentItemCountMap.get(cell)+1);
									Integer correct = 0;
									if ( !outcome.equals(".") ) {
										for ( String value : outcome.split(";") ) {
											if ( (value.equals("correct") || value.equals("passed")) && values[hintsColumn].equals("0") ) {
												correct = 1;
												studentCellRightCount.put(cell,studentCellRightCount.get(cell)+1);
												studentSkillRightCount.put(formatSkill(skillValue),studentSkillRightCount.get(formatSkill(skillValue))+1);
											} else if ( value.equals("incorrect") || value.equals("not passed") || !values[hintsColumn].equals("0") ) {
												studentCellWrongCount.put(cell,studentCellWrongCount.get(cell)+1);
												studentSkillWrongCount.put(formatSkill(skillValue),studentSkillWrongCount.get(formatSkill(skillValue))+1);
											}
										}	
									}
									studentSkillHintsCount.put(formatSkill(skillValue),studentSkillHintsCount.get(formatSkill(skillValue)) + Integer.valueOf(values[hintsColumn]));
									
									sumMap.put("right",
											sumMap.get("right") + correct);
									
									Double timeSD = getTimeSD(values[cellColumn],values[durationColumn]);
									sumMap.put("wrongtimestimeSD",
											sumMap.get("wrongtimestimeSD") + (1-correct) * timeSD);
									sumMap.put("righttimestimeSD",
											sumMap.get("righttimestimeSD") + (correct) * timeSD);
									
									if ( studentSkillRightCount.get(formatSkill(skillValue)) + studentSkillWrongCount.get(formatSkill(skillValue)) > 0 )
											sumMap.put("perccorrectskill",
													sumMap.get("perccorrectskill") + 
													(double)studentSkillRightCount.get(formatSkill(skillValue))/(double)(studentSkillRightCount.get(formatSkill(skillValue)) + studentSkillWrongCount.get(formatSkill(skillValue))));
									
									
									sumMap.put("tSDgreaterthan1",
											sumMap.get("tSDgreaterthan1") + (timeSD > 1 ? 1. : 0.));
									
									sumMap.put("timeSD",
											sumMap.get("timeSD") + timeSD );
									sumMap.put("prevhinttimestimeSD",
											sumMap.get("prevhinttimestimeSD") + (prevhint ? timeSD : 0.));
									prevhint = values[hintsColumn].equals("0") ? false : true;
									numactionsDay++;

									Double boredpred =			- 0.129*sumMap.get("right")/(double)numactionsDay
																+ 0.201;
		
									Double concentratingpred = 	0.299*sumMap.get("right")/(double)numactionsDay
																- 0.287*sumMap.get("timeSD")/(double)numactionsDay
																+ 0.714*sumMap.get("tSDgreaterthan1")/(double)numactionsDay
																+ 0.393;

									Double offTaskpred = 		0.232*sumMap.get("wrongtimestimeSD")/(double)numactionsDay
																+ 0.049;
	
									Double onTaskpred = 		- 0.324*sumMap.get("timeSD")/(double)numactionsDay
																+ 0.274*sumMap.get("righttimestimeSD")/(double)numactionsDay
																+ 0.187*sumMap.get("perccorrectskill")/(double)numactionsDay
																- 0.658*sumMap.get("prevhinttimestimeSD")/(double)numactionsDay
																+ 0.652;
									
									if (obsMap.containsKey(values[0]+values[1])) {
										String[] obs = obsMap.get(values[0]+values[1]);
										// These constructors are kind of crazy... sorry.
										allActions.add(new Action(linenum,values[lessonColumn],
												values[studentColumn],skillValue,
												values[cellColumn],values[varietyColumn],values[datasetColumn],
												values[answerFormatColumn],
												values[outcomeColumn],values[startTimeColumn],
												values[itemTypeColumn],values[durationColumn],
												BKT[L0],BKT[G],BKT[S],BKT[T],pknow,
												studentItemCountMap.get(cell), cellCountConsecutive, wrongConsecutive,
												studentCellRightCount.get(cell),studentCellWrongCount.get(cell),
												studentSkillRightCount.get(formatSkill(skillValue)),studentSkillWrongCount.get(formatSkill(skillValue)),
												obs[0],obs[1],obs[2],obs[3], Integer.valueOf(values[hintsColumn]), studentSkillHintsCount.get(formatSkill(skillValue)), 
												boredpred, concentratingpred, offTaskpred, onTaskpred, firstattempt));
									} else {
										allActions.add(new Action(linenum,values[lessonColumn],
												values[studentColumn],skillValue,
												values[cellColumn], values[varietyColumn],values[datasetColumn],
												values[answerFormatColumn],
												values[outcomeColumn],values[startTimeColumn],
												values[itemTypeColumn],values[durationColumn],
												BKT[L0],BKT[G],BKT[S],BKT[T],pknow,
												studentItemCountMap.get(cell), cellCountConsecutive, wrongConsecutive,
												studentCellRightCount.get(cell),studentCellWrongCount.get(cell),
												studentSkillRightCount.get(formatSkill(skillValue)),studentSkillWrongCount.get(formatSkill(skillValue)),
												"Not Observed",".",".",".",Integer.valueOf(values[hintsColumn]), studentSkillHintsCount.get(formatSkill(skillValue)), 
												boredpred, concentratingpred, offTaskpred, onTaskpred, firstattempt));
									}
									linenum++;
								}
								setClips(allActions);
							}
						}
					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void contextualFeatures() {
		/* The contextual feature generation is done using Python.
		*  This method calls those Python scripts.
		 */
		try {
			String command = "cmd.exe /c";
			command += " cd " + mainFolder;
			command += " && start python generatecontextual.py";
			String command2 = "cmd.exe /c";
			command2 += " cd " + mainFolder;
			command2 += " && start python contextualfeatures.py";
			
			Process p1 = Runtime.getRuntime().exec(command);
			String s = "";
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p1.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
	            
			while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
			
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
			
			Process p2 = Runtime.getRuntime().exec(command2);
			
			stdInput = new BufferedReader(new InputStreamReader(p2.getInputStream()));

			stdError = new BufferedReader(new InputStreamReader(p2.getErrorStream()));
	            
			while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
			
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		if (args.length < 1) {
			System.err.println("Please specify the directory containing the log files.");
		} else {
			mainFolder = args[0];
			infile = mainFolder + "/allData.txt";
			outfile = mainFolder + "/Data_All_Synced.txt";
			obsFile = mainFolder + "/syncedDataAll.txt";
			BKTfile = mainFolder + "/BKTParams.txt";
			try {
				PrintWriter outFile = new PrintWriter(new FileWriter(outfile)); 
			
				outFile.printf("num\t" +
						"behavior\t" +
						"affect\t" +
						"tstart\t" +
						"tend\t" +
						"clip\t" +
						"starttime\t" +
						"lesson\t" +
						"student\t" +
						"skill\t" +
						"cell\t" +
						"right\t" +
						"theory\t" +
						"speedgame\t" +
						"problem\t" +
						"notestest\t" +
						"enterexpression\t" +
						"fillintheblanks\t" +
						"interactiveanswer\t" +
						"multchoicecheckbox\t" +
						"multchoicecombobox\t" +
						"multchoiceradiobutton\t" +
						"duration\t" +
						"timeSD\t" +
						"righttimesduration\t" + 
						"righttimestimeSD\t" +
						"wrongtimesduration\t" + 
						"wrongtimestimeSD\t" +
						"tgreaterthan80\t" +
						"tgreaterthan180\t" +
						"tlessthan3\t" +
						"tSDgreaterthan1\t" + 
						"tSDlessthanneg1\t" + 
						"time3SD\t" + 
						"time5SD\t" + 
						"timeSDdiff\t" + 
						"timeSDdiff2\t" + 
						"durationavglongwindow\t" + 
						"durationlongwindowdiff\t" + 
						"timeSDavglongwindow\t" + 
						"timeSDlongwindowdiff\t" + 
						"difficultyavglongwindow\t" + 
						"difficultylongwindowdiff\t" + 
						"pknowavglongwindow\t" + 
						"pknowlongwindowdiff\t" + 
						"rightavglongwindow\t" + 
						"numactionslongwindow\t" + 
						"theorytimelongwindow\t" + 
						"theorypcttimelongwindow\t" + 
						"problemtimelongwindow\t" + 
						"problempcttimelongwindow\t" + 
						"problemcountlongwindow\t" + 
						"speedgamecountlongwindow\t" + 
						"L0\t" +
						"G\t" +
						"S\t" +
						"T\t" +
						"pknow\t" +
						"difficulty\t" +
						"timescellseentotal\t" + 
						"timescellseenconsecutive\t" + 
						"wrongconsecutive\t" +
						"perccorrectcell\t" +
						"percincorrectcell\t" +
						"numincorrectcell\t" +
						"perccorrectskill\t" +
						"percincorrectskill\t" +
						"numincorrectskill\t" +
						"hints\t" + 
						"hintused\t" + 
						"lowpknownohint\t" + 
						"highpknowhint\t" + 
						"timeperhint\t" + 
						"prevhinttimesduration\t" + 
						"prevhinttimestimeSD\t" + 
						"numhintsskill\t" + 
						"numhintsorincorrectskill\t" + 
						"bored_pred\t" + 
						"concentrating_pred\t" + 
						"offtask_pred\t" + 
						"ontask_pred\t" + 
						"eol\n");
				
				outFile.close();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
			
			System.out.print("Compiling data...");
			CompileData.main(mainFolder);
			System.out.println(" Done.");
			
			RMGenerateFeatures m = new RMGenerateFeatures();
			
			System.out.print("Separating actions...");
			m.separateActions();
			System.out.println(" Done.");
			
			// This step takes a long time!  I recommend only doing it once.
			System.out.print("Setting correct observations...");
			m.setObs();
			System.out.println(" Done.");
			
			System.out.print("Getting observations...");
			m.getObs();
			System.out.println(" Done.");
			
			System.out.print("Getting item times...");
			m.getItemTimes();
			System.out.println(" Done.");
			
			System.out.print("Getting BKT params...");
			m.getBKTParams();
			System.out.println(" Done.");
			
			System.out.print("Generating log features...");
			m.readData();
			System.out.println(" Done.");
			
			System.out.print("Generating contextual features...");
			m.contextualFeatures();
			System.out.println(" Done.");
			
			System.out.print("Aggregating clips...");
			AggregateClips.main(new String[]{mainFolder});
			System.out.println(" Done.");
		}
	}
}
