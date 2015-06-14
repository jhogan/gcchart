package gnucash;
import common.Date;
public class Transaction {

    public Transaction(GnuCashData gcdata){
        this._gcdata=gcdata;
    }

    private GnuCashData _gcdata;
    public void        setGCData(GnuCashData gcdata){ _gcdata = gcdata; }
    public GnuCashData getGCData(){ return _gcdata; }


    String _version;
    public String getVersion()         { return _version; }
    public void   setVersion(String v) { _version=v; }


    Transactions _txs;
    public Transactions getTransactions(){ return _txs; }
    public void     setTransactions(Transactions txs) { _txs = txs;}

    String _id;
    public String   getId(){ return _id; }
    public void     setId(String v) { _id = v;}


    String _commoditySpace;
    public String getCommoditySpace()         { return _commoditySpace; }
    public void   setCommoditySpace(String v) { _commoditySpace=v; }

    String _commodityId;
    public String getCommodityId()         { return _commodityId; }
    public void   setCommodityId(String v) { _commodityId=v; }
    
    Date _posted;
    public Date   getPosted(){ return _posted; }
    public void   setPosted(Date v) { _posted = v; }
    
    
    Date _entered;
    public Date   getEntered(){ return _entered; }
    public void     setEntered(Date v) { _entered = v; }

    int _ns ;
    public int   getNS(){ return _ns; }
    public void     setNS(int v) { _ns = v;}
    
    String _desc;
    public String   getDescription(){ return _desc; }
    public void     setDescription(String v) { _desc = v;}

    Splits _splits = new Splits(this.getGCData());

    public Splits getSplits()         { return _splits; }
    
    public Splits getSplits(Account acct){
        /* Get the splits in this tx that correspond to
         * acct */
        Splits ss = new Splits(this.getGCData());
        Split s;
        for (Object o : this.getSplits()){
            s = (Split) o;
            if (s.getAccountId().equals(acct.getId())){
                ss.add(s);
            }
        }
        return ss;
    }
    public void   setSplits(Splits v) { _splits = v; }
    public Split  newSplit(){
        Split s = new Split(this.getGCData());
        _splits.add(s);
        return s;
    }
}
