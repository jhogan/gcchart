package gnucash;

public class Split {

    public Split(GnuCashData gcdata){
        this._gcdata=gcdata;
    }


    String _id;
    public String   getId()         { return _id; }
    public void     setId(String v) { _id = v;}


    String _acctId;
    public String getAccountId()         { return _acctId; }
    public void   setAccountId(String v) {_acctId = v;}

    int _value ;
    public int   getValue()      { return _value; }
    public void  setValue(int v) { _value = v;}

    int _quantity ;
    public int   getQuantity()      { return _quantity; }
    public void  setQuantity(int v) { _quantity = v;}

    String _reconciled ;
    public String   getReconciledState()          { return _reconciled; }
    public void      setReconciledState(String v) { _reconciled = v;}

    private GnuCashData _gcdata;
    public void        setGCData(GnuCashData gcdata){ _gcdata = gcdata; }
    public GnuCashData getGCData(){ return _gcdata; }

}
