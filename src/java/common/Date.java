package common;

import java.text.SimpleDateFormat;

public class Date extends java.util.Date{
    public Date(){
        super();
    }
    public Date (int year, int month, int date){
        super(year, month, date);
    }
    public Date(long date){
        super(date);
    }
    public Date(java.util.Date date){
        this.setTime(date.getTime());
    }
    public Date(String date, String pattern) throws Exception{
        this(new SimpleDateFormat(pattern).parse(date));
    }
    public long getMillisecondsBetween(common.Date to){
        long ms_from = this.getTime();
        long ms_to = to.getTime();
        return ms_to - ms_from;
    }
    public long getSecondsBetween(common.Date to){
        return this.getMillisecondsBetween(to) / 1000;
    }
    public long getMinutesBetween(common.Date to){
        return this.getSecondsBetween(to) / 60;
    }
    public long getHoursBetween(common.Date to){
        return this.getMinutesBetween(to) / 60;
    }
    public int getDaysBetween(common.Date to){
        return (int) this.getHoursBetween(to) / 24;
    }
    public int getWeeksBetween(common.Date to){
        return this.getDaysBetween(to) / 7;
    }

    public Date addMilliseconds(long number){
        return new Date(this.getTime() + number);
    }
    public Date addSeconds(long number){
        return this.addMilliseconds(number * 1000);
    }
    public Date addMinutes(long number){
        return this.addSeconds(number * 60);
    }
    public Date addHours(long number){
        return this.addMinutes(number * 60);
    }
    public Date addDays(long number){
        return this.addHours(number * 24);
    }
    public Date addWeeks(long number){
        return this.addDays(number * 7);
    }
    public Date getNextMonth(){
        int month = this.getMonth();
        int year = this.getYear();
        return new Date(year, month+1, 1);
    }


    public Date getPrevMonth(){
        int month = this.getMonth();
        int year = this.getYear();
        return new Date(year, month-1, 1);
    }

    public Date getEOM(){
        return getNextMonth().subMilliseconds(1);
    }
    public Date getSOM(){
        int month = this.getMonth();
        int year = this.getYear();
        return new Date(year, month, 1);
    }
    
    public String toString(String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(this);
    }
    public boolean lt(java.util.Date what){
        return this.getTime() < what.getTime() ;
    }
    public boolean gt(java.util.Date what){
        return !this.lt(what);
    }
    public Date subMilliseconds(int number){
        return new Date(this.getTime() - number);
    }
}
