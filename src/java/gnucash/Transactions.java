package gnucash;

import java.util.ArrayList;


public class Transactions extends ArrayList{


    public Transactions(GnuCashData gcdata){
        this._gcdata=gcdata;
    }

    private GnuCashData _gcdata;
    public void        setGCData(GnuCashData gcdata){ _gcdata = gcdata; }
    public GnuCashData getGCData(){ return _gcdata; }
}
