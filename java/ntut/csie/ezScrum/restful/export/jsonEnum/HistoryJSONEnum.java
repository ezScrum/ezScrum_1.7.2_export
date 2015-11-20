package ntut.csie.ezScrum.restful.export.jsonEnum;

public class HistoryJSONEnum {
	public static final String ISSUE_TYPE = "issue_type";
	public static final String HISTORY_TYPE = "type";
	public static final String OLD_VALUE = "old_value";
	public static final String NEW_VALUE = "new_value";
	public static final String CREATE_TIME = "create_time";
	public final static int TYPE_CREATE = 1;
	public final static int TYPE_NAME = 2;
	public final static int TYPE_ESTIMATE = 3;
	public final static int TYPE_REMAIMS = 4;
	public final static int TYPE_ACTUAL = 5;
	public final static int TYPE_IMPORTANCE = 6;
	public final static int TYPE_VALUE = 7;
	public final static int TYPE_ATTACH_FILE = 11;
	public final static int TYPE_STATUS = 12;
	public final static int TYPE_HANDLER = 13;
	public final static int TYPE_SPECIFIC_TIME = 14;
	public final static int TYPE_DROP = 15; // Drop from parent (for task
	                                        // history)
	public final static int TYPE_APPEND = 16; // Append to parent (for task
	                                          // history)
	public final static int TYPE_ADD = 17; // Add Child (for story history)
	public final static int TYPE_REMOVE = 18; // Remove Child (for story
	                                          // history)
	public final static int TYPE_NOTE = 19;
	public final static int TYPE_HOWTODEMO = 20;
	public final static int TYPE_PARTNERS = 21;
	public final static int TYPE_SPRINTID = 22; 
	
	// Task, Unplan status in new version
	public final static String TASK_STATUS_UNCHECK = "1";
	public final static String TASK_STATUS_CHECK = "2";
	public final static String TASK_STATUS_DONE = "3";
	
	// Story status in new version
	public final static String STORY_STATUS_UNCHECK = "1";
	public final static String STORY_STATUS_DONE = "2";
	
	// Retrospective status in new version
	public final static String RETROSPECTIVE_STATUS_NEW = "new";
	public final static String RETROSPECTIVE_STATUS_CLOSED = "closed";
	public final static String RETROSPECTIVE_STATUS_RESOLVED = "resolved";
	public final static String RETROSPECTIVE_STATUS_ASSIGNED = "assigned";
}
