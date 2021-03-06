package org.indiv.dls.games.vocabrecall.feature.db;

import android.content.ContentValues;
import android.database.Cursor;

public class GameWord {
    public static final String TABLE_NAME = "GAME_WORD";
    public static final String WORD = "WORD"; // PK, FK
    public static final String ROW = "GRID_ROW"; // of starting square  
    public static final String COL = "GRID_COL"; // of starting square 
    public static final String ACROSS = "ACROSS";  // 0 or 1 for down or across
    public static final String USER_TEXT = "USER_TEXT";  // user's answer (may or may not be correct)
    public static final String CONFIDENT = "CONFIDENT";  // user's confidence in answer (0 or 1  for tentative or confident)
    public static final String GAME_NO = "GAME_NO";  // 
	
    public static final String TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
			            		WORD 		+ " TEXT 	 NOT NULL, " +
			            		GAME_NO	    + " INTEGER  NOT NULL, " +
			            		ROW 		+ " INTEGER  NOT NULL, " +
			            		COL 		+ " INTEGER  NOT NULL, " +
			            		ACROSS 		+ " INTEGER  NOT NULL CHECK (ACROSS in (0,1)), " +
			            		USER_TEXT 	+ " TEXT, " +
			            		CONFIDENT 	+ " INTEGER CHECK (CONFIDENT in (0,1)), " + 
        		"CONSTRAINT GW_PK PRIMARY KEY (" + WORD + "," + GAME_NO + "), " +
        		"CONSTRAINT GW_GAME_FK FOREIGN KEY (" + GAME_NO + ") REFERENCES " + Game.TABLE_NAME + "(" + Game.GAME_NO + ")," +
        		"CONSTRAINT GW_WORD_FK FOREIGN KEY (" + WORD + ") REFERENCES " + Word.TABLE_NAME + "(" + Word.WORD + ")" +
    		");";
    
    public static final String FK_INDEX_CREATE = "CREATE INDEX GW_WORD_FK_I ON " + TABLE_NAME + " (" + WORD + ")";
    
    private String word; // PK, FK
    private int gameNo; 
    private int row; 
    private int col; 
    private boolean across; 
    private String userText; 
    private boolean confident;
    private Word wordInfo;
    private Game game;
    
    // constructor
	public GameWord() {}
	
    // constructor
	public GameWord(String word) {
		this.word = word;
	}
	
    // constructor
	public GameWord(Word word, int gameNo, int row, int col, boolean across) {
		super();
		this.wordInfo = word;
		this.word = word.getWord();
		this.gameNo = gameNo;
		this.row = row;
		this.col = col;
		this.across = across;
	}

    // constructor
    public GameWord(Cursor c) {
    	this.setWord(c);
    	this.setGameNo(c);
        this.setRow(c);
        this.setCol(c);
        this.setAcross(c);
        this.setUserText(c);
        this.setConfident(c);
    }    

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(WORD, getWord());
        values.put(GAME_NO, getGameNo());
        values.put(ROW, getRow());
        values.put(COL, getCol());
        values.put(ACROSS, isAcross());
        values.put(USER_TEXT, getUserText());
        values.put(CONFIDENT, isConfident());
        return values;
    }    
	
    public ContentValues getUserEntryContentValues() {
        ContentValues values = new ContentValues();
        values.put(USER_TEXT, getUserText());
        values.put(CONFIDENT, isConfident());
        return values;
    }    

    // getters and setters
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public void setWord(Cursor c) {
		int i = c.getColumnIndex(WORD);
		if (i != -1) { setWord(c.getString(i)); }
	}
	
	public int getGameNo() {
		return gameNo;
	}
	public void setGameNo(int gameNo) {
		this.gameNo = gameNo;
	}
	public void setGameNo(Cursor c) {
		int i = c.getColumnIndex(GAME_NO);
    	if (i != -1) { setGameNo(c.getInt(i)); }
	}

	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public void setRow(Cursor c) {
		int i = c.getColumnIndex(ROW);
    	if (i != -1) { setRow(c.getInt(i)); }
	}

	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public void setCol(Cursor c) {
		int i = c.getColumnIndex(COL);
    	if (i != -1) { setCol(c.getInt(i)); }
	}

	public boolean isAcross() {
		return across;
	}
	public void setAcross(boolean across) {
		this.across = across;
	}
	public void setAcross(Cursor c) {
		int i = c.getColumnIndex(ACROSS);
		if (i != -1) { setAcross(c.getInt(i)==1); }
	}
	
	public String getUserText() {
		return userText;
	}
	public void setUserText(String userText) {
		this.userText = userText;
	}
	public void setUserText(Cursor c) {
		int i = c.getColumnIndex(USER_TEXT);
		if (i != -1) { setUserText(c.getString(i)); }
	}
	
	public boolean isConfident() {
		return confident;
	}
	public void setConfident(boolean confident) {
		this.confident = confident;
	}
	public void setConfident(Cursor c) {
		int i = c.getColumnIndex(CONFIDENT);
		if (i != -1) { setConfident(c.getInt(i)==1); }
	}

	public Word getWordInfo() {
		return wordInfo;
	}
	public void setWordInfo(Word wordInfo) {
		this.wordInfo = wordInfo;
	}

	public Game getGame() {
		return game;
	}
	public void setGame(Game game) {
		this.game = game;
	}


	public String get3LetterHint() {
		return word.substring(0, Math.min(3, word.length()));
	}

	public String toString() {
		return getWord();
	}

}
