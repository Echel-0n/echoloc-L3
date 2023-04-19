package com.example.echoloc;

public class GameException extends Exception{
    public final static int OUT_OF_CITY_RANGE = 0;
    public final static int INEXISTANT_COUNTRY_ERROR = 1;
    public final static int PARSING_ERROR = 2;
    public final static int URL_ERROR = 3;
    public final static int CONNECTION_ERROR = 4;
    public final static int PROTOCOL_ERROR = 5;
    public final static int DATA_READ_ERROR = 6;
    private final int exceptionType;
    private final String message;
    private final Object data;

    public GameException(int type){
        this.exceptionType = type;
        this.message = null;
        this.data = null;
    }
    public GameException(int type, String message){
        this.exceptionType = type;
        this.message = message;
        this.data = null;
    }

    public GameException(int type, String message, Object data){
        this.exceptionType = type;
        this.message = message;
        this.data = data;
    }
    public int getExceptionType() {
        return exceptionType;
    }

    public String getMessage() {
        return message;
    }
    public Object getData() {
        return data;
    }
}

