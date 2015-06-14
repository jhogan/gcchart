package gnucash;

import java.util.ArrayList;
import common.Date;
import java.util.Collection;
import java.util.Collections;


public class Accounts extends ArrayList{

    
    public Accounts(GnuCashData gcdata){
        this._gcdata=gcdata;
    }

    private GnuCashData _gcdata;
    public void        setGCData(GnuCashData gcdata){ _gcdata = gcdata; }
    public GnuCashData getGCData(){ return _gcdata; }

    public void Sort(){
        Collections.sort(this);
    }
    
    public Account get(String id){
        Account acct;
        for (Object o: this){
            acct = (Account) o;
            if (acct.getId().equals(id))
                return acct;
        }
        return null;
    }
    public int getDepositedAmount(Date from, Date to) throws Exception{
        int amt = 0;
        int amt0;
        Account acct;
        for (Object o: this){
            acct = (Account) o;
            amt0 = acct.getDepositedAmount(from, to);
            amt += amt0;
        }
        return amt;
    }

}
