package gnucash;

import java.util.ArrayList;


public class Splits extends ArrayList{


    public Splits(GnuCashData gcdata){
        _gcdata = gcdata;
    }

    private Transaction _tx = null;
    public Transaction getTransaction(){ return _tx; }

    private GnuCashData _gcdata;
    public void        setGCData(GnuCashData gcdata){ _gcdata = gcdata; }
    public GnuCashData getGCData(){ return _gcdata; }

}
