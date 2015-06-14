    package charts;

import gnucash.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Session;
import java.text.SimpleDateFormat;
import java.io.File;
import common.Date;
import java.text.ParseException;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;


public class TemporalComparison extends HttpServlet {
    HttpServletRequest _request;
    HttpServletResponse _response;
    HttpSession _session;
    ServletContext _cx;
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        _cx = getServletContext();
        request.setAttribute("ex", null);
        _request = request; _response =response;
        _session = request.getSession();
        response.setContentType("text/html;charset=UTF-8");
        
        try {

            GnuCashData gcdata =
                    GnuCashData.getInstance();
            String[] acctIds = request.getParameterValues("lstAccts");
            
            
            String strFrom = _request.getParameter("txtFrom");
            String strTo   = _request.getParameter("txtTo");
            String strReloadCache   = _request.getParameter("reload");
            if (strReloadCache != null && strReloadCache.equals("1")){
                gcdata.Load();
            }
            Date to;
            if (strTo == null || strTo.equals("")){
                to = new Date().getEOM();
                _cx.setAttribute("to", to.toString("MM/dd/yyyy"));
            }else{
                to = new Date(strTo, "MM/dd/yyyy");
                _cx.setAttribute("to", strTo);
            }
            
            if (strFrom == null || strFrom.equals("")){
                _cx.setAttribute("from", to.addWeeks(-16).toString("MM/dd/yyyy"));
            }else{
                _cx.setAttribute("from", strFrom);
            }


            if (acctIds != null){
                setSelAccts(acctIds, request);
            }
            createGraphImage(acctIds);
            _cx.setAttribute("gcdata", gcdata);
            _cx.setAttribute("rootacct", gcdata.getRootAccount());
            

        }catch(Exception ex){
            request.setAttribute("ex", ex);
        } finally {
            _cx.getRequestDispatcher("/TemporalComparison.jsp").
                                forward(request, response);
        }
    }
    private void createGraphImage (String[] acctIds) throws Exception{
        GnuCashData gcdata =
                    GnuCashData.getInstance();
        final int width = 1500;
        final int height = 500;
        String datePattern = "MM/dd/yyyy";
        Class clsTimePeriod;
        String intervalType = _request.getParameter("cboIntervalType");
        boolean weekly = intervalType == null || intervalType.equals("weekly");

        if (weekly){
            clsTimePeriod = Day.class;
        }else{
            clsTimePeriod = Month.class;
        }
        
        Date to=null, from =null;
        HttpSession session = _request.getSession();
        String strFrom = (String)_cx.getAttribute("from");
        String strTo   = (String)_cx.getAttribute("to");
        
        from = new Date(strFrom, datePattern);
        to   = new Date(strTo,   datePattern);

        Account acct;
        TimeSeries ts;
        TimeSeriesCollection tsc = new TimeSeriesCollection();
        
        int amt;
        Date sow, eow;

        if (acctIds != null){
            for (String id : acctIds){
                acct = gcdata.getAccounts().get(id);
                ts = new TimeSeries(acct.getName(), clsTimePeriod);

                if (! weekly){
                    for(Date month=from.getSOM();
                            month.lt(to.getNextMonth());
                            month=month.getNextMonth() ){
                        amt = acct.getDepositedAmount(month,
                                                     month.getEOM());
                        ts.add(new Month(month), amt>0 ? amt/100: 0);
                    }
                }else{
                    sow = new Date(from);
                    eow = (new Date(from)).addDays(8).subMilliseconds(1);
                    while (eow.lt(to)){
                        amt = acct.getDepositedAmount(sow,eow);
                        if (amt > 0)
                            ts.add(new Day(eow), amt/100);
                        sow = sow.addDays(8);
                        eow = eow.addDays(8);
                    }
                }
                tsc.addSeries(ts);
            }
        }
        session.setAttribute("TimeSeriesCollection", tsc);

        JFreeChart chart =
                ChartFactory.createTimeSeriesChart(
                    (weekly) ? "Weekly" : "Monthly" + " Deposits",
                    "Time",
                    "Dollars",
                    tsc,
                    true,
                    true,
                    false);
        XYPlot xy = chart.getXYPlot();
        DateAxis ax = (DateAxis) xy.getDomainAxis();
        ax.setTickUnit(new DateTickUnit(
                  (weekly) ? DateTickUnitType.DAY: DateTickUnitType.DAY.MONTH,
                  (weekly) ? 7 : 1,
                   new SimpleDateFormat("MMM dd ''yy")));

        ax.setVerticalTickLabels(true);
        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) xy.getRenderer();

        r.setSeriesShapesFilled(0, Boolean.TRUE);
        r.setSeriesShapesFilled(1, Boolean.FALSE);
        String relPath = getReleativeChartPath();

        session.setAttribute("relPath", relPath);
        session.setAttribute("width", width);
        session.setAttribute("height", height);

        File  chartFile = getChartFile(relPath);
        ChartUtilities.saveChartAsPNG(chartFile,
                                        chart, width, height);
        
    }
    private void setSelAccts(String[] acctIds, HttpServletRequest request) throws Exception{
        GnuCashData gcdata =
                    GnuCashData.getInstance();
        Accounts selAccts = new Accounts(gcdata);
        Account acct;
        for (String id : acctIds) {
            acct = gcdata.getAccounts().get(id);
            selAccts.add(acct);
        }
        request.getSession().setAttribute("selaccts", selAccts);
    }
    private File getChartFile(String relPath){
        String real = getServletContext().getRealPath(relPath);
        return new File(real);
    }
    private String getReleativeChartPath(){
        String path = "var/lib";
        String id = _request.getSession().getId();
        return path + "/" + id + ".png";
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
