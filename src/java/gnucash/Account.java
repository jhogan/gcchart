package gnucash;

import common.Date;

public class Account implements Comparable {

    public Account(GnuCashData gcdata){ _gcdata=gcdata; }

    String _version;
    public String getVersion()         { return _version; }
    public void   setVersion(String v) { _version=v; }

    String _name;
    public String getName()         { return _name; }
    public void   setName(String v) { _name=v; }

    String _guid;
    public String getId()       { return _guid;}
    public void setId(String v) { _guid=v; }


    String _type;
    public String getType()         { return _type; }
    public void   setType(String v) { _type=v; }

    String _commoditySpace;
    public String getCommoditySpace()         { return _commoditySpace; }
    public void   setCommoditySpace(String v) { _commoditySpace=v; }

    String _commodityId;
    public String getCommodityId()         { return _commodityId; }
    public void   setCommodityId(String v) { _commodityId=v; }

    /* This may be an int; they were all 100 */
    String _commodityScu;
    public String getCommodityScu()         { return _commodityScu; }
    public void   setCommodityScu(String v) { _commodityScu=v; }

    String _slotKey;
    public String getSlotKey()         { return _slotKey; }
    public void   setSlotKey(String v) { _slotKey=v; }

    String _slotValue;
    public String getSlotValue()         { return _slotValue; }
    public void   setSlotValue(String v) { _slotValue=v; }
    
    String _parentId;
    public String getParentId()         { return _parentId; }
    public void   setParentId(String v) { _parentId=v; }

    Account _parent = null;
    public Account getParent(){
        if (_parent == null){
            Accounts accts = this.getGCData().getAccounts();
            Account acct;
            for(Object o : accts){
                acct = (Account) o;
                if (acct.getId().equals(this.getParentId())){
                    _parent = acct;
                    break;
                }
            }
        }
        return _parent;
    }
    
    private Transactions _txs;

    public Transactions getTransactions(){
        return this.getTransactions(false);
    }
    public Transactions getTransactions(boolean refresh){
        if (refresh || _txs == null){
            _txs = new Transactions(this.getGCData());
            GnuCashData gcfile = this.getGCData();
            Transaction tx;
            Split s;
            for (Object o : gcfile.getTransactions()){
                tx = (Transaction) o;
                for (Object o0 : tx.getSplits()){
                    s = (Split) o0;
                    if (s.getAccountId().equals(this.getId())){
                        _txs.add(tx);
                    }
                }

            }
        }
        return _txs;

    }
    public Transactions getTransactions(Date from, Date to){
        /* Get all txs for this account between from and to
         * date */
        Transactions txs = new Transactions(this.getGCData());
        Transaction tx;
        Date dp;
        for (Object o : this.getTransactions()){
            tx = (Transaction) o;
            dp = tx.getPosted();
            if ((dp.equals(from) || dp.after(from))
                    && (dp.equals(to) || dp.before(to))){
                txs.add(tx);
            }
        }
        return txs;
    }

    private GnuCashData _gcdata;
    public void        setGCData(GnuCashData gcdata){ _gcdata = gcdata; }
    public GnuCashData getGCData(){ return _gcdata; }

    Accounts _children = null;
    public Accounts getChildren() throws Exception{
        if (_children == null){
            Account acct, rent;
            GnuCashData gcdata = GnuCashData.getInstance();
            _children = new Accounts(gcdata);
            Accounts accts = gcdata.getAccounts();
            for(Object o : accts){
                acct = (Account)o;
                rent = acct.getParent();
                if (rent != null){
                    if (rent.getId().equals(this.getId())){
                        _children.add(acct);
                    }
                }
            }
        }
        return _children;
    }

    public int getDepth(){
        Account acct = this; int i = 0;
        while((acct = acct.getParent()) != null) i++;
        return i;
    }
    public boolean isIn(Accounts accts){
        Account acct;
        for(Object o : accts){
            acct = (Account)o;
            if (acct.getId().equals(this.getId())){
                return true;
            }
        }
        return false;
    }

    public int getDepositedAmount(Date from, Date to) throws Exception{
        int ret = 0;
        Splits ss = this.getSplits(from, to);
        Split s;
        for(Object o : ss) {
            s = (Split) o;
            System.out.println(s.getValue());
            ret += s.getValue();
        }
        int childamt = this.getChildren().getDepositedAmount(from, to);
        return ret + childamt;
    }

    public Splits getSplits(Date from, Date to){
        /* Get all splits in the txs of this acct that
         * pertain to this account */
        Transactions txs = this.getTransactions(from, to);
        Transaction tx; Splits ss; Split s;
        Splits ret = new Splits(this.getGCData());
        for(Object o : txs) {
            tx = (Transaction) o;
            ss = tx.getSplits(this);
            for(Object o0 : ss){
                s = (Split) o0;
                if (s.getAccountId().equals(this.getId())){
                    ret.add(s);
                }
            }
        }
        return ret;
    }
    public int compareTo(Object o){
        Account acct = (Account) o;
        return this.getName().compareTo(acct.getName());
    }

}

