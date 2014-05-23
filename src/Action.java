/* Action.java
 * 
 * Represents a single student action, and its relevant features.
 */

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class Action {
		private int num;
		private String lesson;
		private String student;
		private String skill;
		private String cell;
		private String outcome;
		private String startTime;
		private String itemType;
		private String behavior;
		private String affect;
		private String tStart;
		private String tEnd;
		private String variety;
		private String dataset;
		private String duration;
		private Double L0;
		private Double G;
		private Double S;
		private Double T;
		private Double Pknow;
		private String answerformat;
		private Integer cellRightCount;
		private Integer cellWrongCount;
		private Integer skillRightCount;
		private Integer skillWrongCount;
		private Integer cellCount;
		private Integer cellCountConsecutive;
		private String clip;
		private Integer wrongConsecutive;
		private Integer hints;
		private Integer skillHintsCount;
		private Double boredpred;
		private Double concentratingpred;
		private Double offTaskpred;
		private Double onTaskpred;
		private Boolean firstattempt;
		
		public Action(int num, String lesson, String student, String skill, String cell, 
				String variety, String dataset, String answerformat, String outcome,String startTime,
				String itemType,String duration,Double L0, Double G, Double S, Double T, Double Pknow, 
				Integer cellCount, Integer cellCountConsecutive, Integer wrongConsecutive,
				Integer cellRightCount, Integer cellWrongCount, Integer skillRightCount, Integer skillWrongCount,
				String behavior, String affect, String tStart, String tEnd, Integer hints, Integer skillHintsCount,
				Double boredpred, Double concentratingpred, Double offTaskpred, Double onTaskpred, Boolean firstattempt) {
			super();
			this.num = num;
			this.lesson = lesson;
			this.student = student;
			this.skill = skill;
			this.cell = cell;
			this.variety = variety;
			this.dataset = dataset;
			this.outcome = outcome;
			this.startTime = startTime;
			this.itemType = itemType;
			this.behavior = behavior;
			this.affect = affect;
			this.tStart = tStart;
			this.tEnd = tEnd;
			this.duration = duration;
			this.L0 = L0;
			this.G = G;
			this.S = S;
			this.T = T;
			this.Pknow = Pknow;
			this.answerformat = answerformat;
			this.cellCount = cellCount;
			this.cellCountConsecutive = cellCountConsecutive;
			this.wrongConsecutive = wrongConsecutive;
			this.cellRightCount = cellRightCount;
			this.cellWrongCount = cellWrongCount;
			this.skillRightCount = skillRightCount;
			this.skillWrongCount = skillWrongCount;
			this.clip = ".";
			this.hints = hints;
			this.skillHintsCount = skillHintsCount;
			this.boredpred = Math.max(0,Math.min(boredpred,1.0));
			this.concentratingpred = Math.max(0,Math.min(concentratingpred,1.0));
			this.offTaskpred = Math.max(0,Math.min(offTaskpred,1.0));
			this.onTaskpred = Math.max(0,Math.min(onTaskpred,1.0));
			this.firstattempt = firstattempt;
		}
		
		public Action(int num, String lesson, String student, String skill, String cell, 
				String outcome,String startTime,
				String itemType, Integer hints) {
			super();
			this.num = num;
			this.lesson = lesson;
			this.student = student;
			this.skill = skill;
			this.cell = cell;
			this.outcome = outcome;
			this.startTime = startTime;
			this.itemType = itemType;
			this.hints = hints;
		}
		
		public Boolean getFirstAttempt() {
			return this.firstattempt;
		}

		public Double getPred(String label) {
			if ( label.equals("bored") )
				return this.boredpred;
			
			if ( label.equals("concentrating") )
				return this.concentratingpred;
			
			if ( label.equals("offtask") )
				return this.offTaskpred;
			
			if ( label.equals("ontask") )
				return this.onTaskpred;
			
			return 0.0;
		}
		
		public Integer getHints() {
			return this.hints;
		}
		
		public void setClip(String clip) {
			this.clip = clip;
		}
		
		public String getClip() {
			return this.clip;
		}
		
		public String getAnswerFormat() {
			return this.answerformat;
		}
		
		public int getNum() {
			return this.num;
		}
		
		public int getCellCount() {
			return this.cellCount;
		}
		
		public int getCellCountConsecutive() {
			return this.cellCountConsecutive;
		}
		
		public int getWrongConsecutive() {
			return this.wrongConsecutive;
		}
		
		
		public Double getL0() {
			return this.L0;
		}
		public Double getG() {
			return this.G;
		}
		public Double getS() {
			return this.S;
		}
		public Double getT() {
			return this.T;
		}
		public Double getPknow() {
			return this.Pknow;
		}
		public Integer getCellRightCount() {
			return this.cellRightCount;
		}
		public Integer getCellWrongCount() {
			return this.cellWrongCount;
		}
		public Integer getSkillRightCount() {
			return this.skillRightCount;
		}
		public Integer getSkillWrongCount() {
			return this.skillWrongCount;
		}
		public Integer getSkillHintsCount() {
			return this.skillHintsCount;
		}
		
		public String getLesson() {
			String lesson = this.lesson;
			lesson = lesson.replace(" ","");
			lesson = lesson.replace(",","");
			lesson = lesson.replace(";","");
			lesson = lesson.replace(":","");
			lesson = lesson.replace("(","");
			lesson = lesson.replace(")","");
			lesson = lesson.replace("/","");
			lesson = lesson.replace("-","");
			lesson = lesson.replace("\"","");
			return lesson;
		}
		
		public String getItemType() {
			String itemType = this.itemType;
			itemType = itemType.replace(" ","");
			itemType = itemType.replace(",","");
			itemType = itemType.replace(";","");
			itemType = itemType.replace(":","");
			itemType = itemType.replace("(","");
			itemType = itemType.replace(")","");
			itemType = itemType.replace("/","");
			itemType = itemType.replace("-","");
			itemType = itemType.replace("\"","");
			return itemType;
		}
		
		public String getStudent() {
			return "s" + this.student;
		}
		
		public String getBehavior() {
			return this.behavior;
		}
		
		public void resetObs() {
			this.behavior = "Not observed";
			//this.affect = ".";
			//this.tStart = ".";
			//this.tEnd = ".";
		}
		
		public String[] getObsData() {
			return new String[]{this.behavior, this.affect, this.tStart, this.tEnd};
		}
		
		public void setObsData(String[] obsData) {
			this.behavior = obsData[0];
			this.affect = obsData[1];
			this.tStart = obsData[2];
			this.tEnd = obsData[3];
		}

		public String getAffect() {
			return this.affect;
		}
		
		public String getDuration() {
			return this.duration;
		}
		
		public String getSkill() {
			String skill = this.skill;
			skill = skill.replace(" ","");
			skill = skill.replace(",","");
			//skill = skill.replace(";","");
			skill = skill.replace("Review:","");
			skill = skill.replace(":","");
			skill = skill.replace("(","");
			skill = skill.replace(")","");
			skill = skill.replace("/","");
			skill = skill.replace("-","");
			skill = skill.replace("\"","");
			skill = skill.replace("CircleandDisk","CirclesandDisks");
			return skill;
		}
		
		public String getCell() {
			return this.cell;
		}
		
		public String getUnique() {
			return this.cell + "-" + this.variety + "-" + this.dataset;
		}
		
		public String getVariety() {
			return this.variety;
		}
		
		public String getDataset() {
			return this.dataset;
		}
		
		public double getRight() {
			double right = 0;
			double total = 0;
			if ( this.itemType.equals("Theory") ) {
				if ( this.outcome.equals(".") ) {
					return -1;
				} else {
					if ( this.hints == 0 ) {
						for ( String value : this.outcome.split(";") ) {
							if ( value.equals("correct") ) right++;
							total++;
						}
						return right/total;
					} else return 0;
					
				}
			}
			
			if ((this.outcome.equals("correct") || this.outcome.equals("passed")) && this.hints == 0 ) 
				return 1;
			else
				return 0;
		}
		
		public Date getStartTime() {
			SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
			try {
				return parser.parse(this.startTime);
			} catch ( Exception e ) {
				e.printStackTrace();
				return null;
			}
		}
		
		public String getClipId() {
			String clipid = this.student;
			clipid += "_" + this.startTime.split(" ")[0];
			clipid += "_" + this.tStart;
			return clipid;
		}
		
		public Date getTStartDate() {
			String tStartDate = this.startTime.split(" ")[0];
			SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
			try {
				return parser.parse(tStartDate + " " + this.tStart);
			} catch ( Exception e ) {
				try {
					parser = new SimpleDateFormat("dd.MM.yyyy .");
					return parser.parse(tStartDate + " " + this.tStart);
				} catch ( Exception e2 ) {
					e2.printStackTrace();
					return null;
				}
			}
		}
		
		public String getTStart() {
			//SimpleDateFormat parser = new SimpleDateFormat("kk:mm:ss");
			//try {
				//return parser.parse(this.tStart);
			//} catch ( Exception e ) {
				//e.printStackTrace();
				//return null;
			//}
			return this.tStart;
		}
		
		public String getTEnd() {
			//SimpleDateFormat parser = new SimpleDateFormat("kk:mm:ss");
			//try {
				//return parser.parse(this.tEnd);
			//} catch ( Exception e ) {
				//e.printStackTrace();
				//return null;
			//}
			return this.tEnd;
		}
		
		public static Comparator<Action> SkillComparator = new Comparator<Action>() {
			public int compare(Action action1, Action action2) {
				return action1.getSkill().toUpperCase().compareTo(action2.getSkill().toUpperCase());
			}
		};
		
		public static Comparator<Action> StudentComparator = new Comparator<Action>() {
			public int compare(Action action1, Action action2) {
				try { 
					String student1 = action1.getStudent().toUpperCase();
					String student2 = action2.getStudent().toUpperCase();
					return student1.compareTo(student2);
				} catch (Exception e) {
					return 1;
				}
			}
		};
		
		public static Comparator<Action> StartTimeComparator = new Comparator<Action>() {
			public int compare(Action action1, Action action2) {
				return action1.getStartTime().compareTo(action2.getStartTime());
			}
		};
	}
