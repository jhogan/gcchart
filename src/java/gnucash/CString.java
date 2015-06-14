package gnucash;
public class CString{
    StringBuilder _str;
    public CString(){
        _str = new StringBuilder();
    }
    public void setValue(String val, int times){
        _str.setLength(0);
        for (int i = 0; i < times; i++) _str.append(val);
    }

    @Override
    public String toString(){
        return _str.toString();
    }
}
