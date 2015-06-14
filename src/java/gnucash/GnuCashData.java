package gnucash;
import java.io.FileInputStream;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import java.io.File;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamReader;
import org.apache.naming.factory.TransactionFactory;
import common.Date;
import java.text.*;


public class GnuCashData {
    private static GnuCashData _instance;

    protected GnuCashData() throws IOException, Exception{
        this.Load();
        this.Sort();
    }
    public void Sort(){
        this.getAccounts().Sort();
    }

    public static GnuCashData getInstance() throws Exception{
        if (_instance == null) _instance = new GnuCashData();
        return _instance;
    }
    private File getGCFile() throws IOException, Exception{
            File cashfile = new File("/tmp/cash");
            String cmd = "svn export svn://cbg/jjh/trunks/var/db/cash " +
                        cashfile.getAbsolutePath();
            cmd += " --username jhogan --password jhogansvn";

            cashfile.delete();

            Process ps = Runtime.getRuntime().exec(cmd);

            if (ps.waitFor() != 0){
                throw new Exception("exec returned " + ps.exitValue() +
                                    " (" + "Failed exporting from svn" + ")");
            }
            if (!cashfile.exists()){
                throw new Exception(
                        cashfile.getAbsolutePath() + " wasn't exported");
            }
            return cashfile;

    }
    private Transactions _txs;
    public Transactions getTransactions(){ return _txs; }

    private Accounts _accts;
    public Accounts getAccounts(){ return _accts; }

    public Account getRootAccount(){
        Account acct;
        for(Object o : this.getAccounts()){
            acct = (Account)o;
            if (acct.getType().equals("ROOT")){
                return acct;
            }
        }
        return null;
    }
    public void initCache(){
        this._accts = new Accounts(this);
        this._txs = new Transactions(this);
    }
    
    public void Load ()throws IOException, Exception{
        final String URI = "http://www.gnucash.org/XML/";
        final String GNC = URI + "gnc";
        final String ACT = URI + "act";
        final String CMDTY = URI + "cmdty";
        final String SLOT = URI + "slot";
        final String TRN = URI + "trn";
        final String TS = URI + "ts";
        final String SPLIT = URI + "split";
        final String TS_PATTERN = "yyyy-MM-dd H:mm:ss Z";
        this.initCache();
        XMLStreamReader r = null;
        boolean dateEntered = false, datePosted = false;
        try{
            Account acct = null;
            String uri;
            String name, element = "";
            String val;
            Transaction tx = null;
            Split split = null;
            File gcfile = this.getGCFile();

            XMLInputFactory i = XMLInputFactory.newInstance();
          
            r = i.createXMLStreamReader(
                    gcfile.getAbsolutePath(),
                    new FileInputStream(gcfile));

           for(int event = r.next(); r.hasNext(); event=r.next()){
               switch(event){
                   case XMLStreamReader.START_ELEMENT:
                       name = r.getLocalName();
                       uri = r.getNamespaceURI();
                       if (uri!=null){
                           if (uri.equals(GNC)){
                               if (name.equals("account")){
                                    acct = new Account(this);
                                    _accts.add(acct);
                                    /* Naivly assume version is first attrib */
                                    acct.setVersion(r.getAttributeValue(0));
                               }else if (name.equals("transaction")){
                                   tx = new Transaction(this);
                                   _txs.add(tx);

                                   /* Naivly assume version is first attrib */
                                   tx.setVersion(r.getAttributeValue(0));
                               }
                           }else if (uri.equals(TRN)){
                               if (name.equals("split"))
                                    split = tx.newSplit();
                               else if (name.equals("date-posted"))
                                    datePosted=true;
                               else if (name.equals("date-entered"))
                                    dateEntered=true;
                                                          }
                           element = uri + ":" + name;
                       }
                       break;
                   case XMLStreamReader.END_ELEMENT:
                       name = r.getLocalName();
                       uri = r.getNamespaceURI();
                       if (uri != null){
                           if (uri.equals(GNC)){
                               if (name.equals("account"))
                                   acct=null;
                               else if (name.equals("transaction"))
                                   tx=null;
                           }else if (uri.equals(TRN)){
                               if (name.equals("date-posted"))
                                    datePosted=false;
                               else if (name.equals("date-entered"))
                                    dateEntered=false;
                               else
                                    if (name.equals("split"))
                                        split = null;
                           }
                       }
                       break;
                   case XMLStreamReader.CHARACTERS:
                    if (acct != null){
                        val = r.getText();
                        if (element.equals(ACT + ":name"))
                            acct.setName(val);
                        if (element.equals(ACT + ":parent"))
                            acct.setParentId(val);
                        else if (element.equals(ACT + ":id"))
                            acct.setId(val);
                        else if (element.equals(ACT + ":type"))
                            acct.setType(val);
                        else if (element.equals(CMDTY + ":space"))
                            acct.setCommoditySpace(val);
                        else if (element.equals(CMDTY + ":id"))
                            acct.setCommodityId(val);
                        else if (element.equals(ACT + ":commodity-scu"))
                            acct.setCommodityScu(val);
                        else if (element.equals(SLOT + ":key"))
                            acct.setSlotKey(val);
                        else if (element.equals(SLOT + ":value"))
                            acct.setSlotValue(val);
                   }else if (tx != null){
                        val = r.getText();
                        if (split == null){
                            if (element.equals(TRN + ":id"))
                                tx.setId(val);
                            else if (element.equals(CMDTY + ":space"))
                                tx.setCommoditySpace(val);
                            else if (element.equals(CMDTY + ":id"))
                                tx.setCommodityId(val);
                            else if (datePosted && element.equals(TS + ":date"))
                                tx.setPosted(new Date(val, TS_PATTERN));
                            else if (dateEntered && element.equals(TS + ":date"))
                                tx.setEntered(new Date(val, TS_PATTERN));
                            else if (dateEntered && element.equals(TS + ":ns"))
                                tx.setNS(Integer.parseInt(val));
                            else if (element.equals(TRN + ":description"))
                                tx.setDescription(val);
                        }else{
                            if (element.equals(SPLIT + ":id"))
                                split.setId(val);
                            else if (element.equals(SPLIT + ":reconciled-state"))
                                split.setReconciledState(val);
                            else if (element.equals(SPLIT + ":value"))
                                split.setValue(this.parseAmount(val));
                            else if (element.equals(SPLIT + ":quantity"))
                                split.setQuantity(this.parseAmount(val));
                            else if (element.equals(SPLIT + ":account"))
                                split.setAccountId(val);
                        }
                   }
                   element="";
                   break;
                   
               }
           }
       
        }
        finally{
            try{
                if (r != null) r.close();
            }
            catch (Exception ex){
                System.err.print(ex);
            }
        }

    }
    private int parseAmount(String amt){
        int ix = amt.indexOf('/');
        amt = amt.substring(0, ix);
        return Integer.parseInt(amt);
    }
    
}
