<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="common.Date, gnucash.*,  org.jfree.data.time.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Temporal Comparison</title>
        <link rel="stylesheet" type="text/css" href="main.css"
              <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
              <%@page import="common.Date, gnucash.*,  org.jfree.data.time.*" %>
              <%!
                  private void printAccountSelect(Accounts accts,
                          Accounts selAccts,
                          JspWriter out){
                      try{
                          boolean root = false;
                          boolean beenHere;
                          String id;
                          String selTag;
                          CString dent = new CString();
                          if (accts.size() > 0){
                              root = ((Account)accts.get(0))
                                          .getParent()
                                          .getType().equals("ROOT");

                              if (root)
                                  out.println("<select id=\"lstAccts\" " +
                                                       "name = \"lstAccts\" " +
                                                       "multiple=\"multiple\">");

                              beenHere = false;
                              for(Object o : accts){
                                  Account acct = (Account) o;
                                  id = acct.getId();
                                  if (!beenHere){
                                      dent.setValue("&nbsp;&nbsp;&nbsp;",
                                              acct.getDepth()-1);
                                      beenHere = true;
                                  }
                                  if (selAccts != null && acct.isIn(selAccts))
                                      selTag = " selected=\"selected\" ";
                                  else selTag = " ";
                                  out.println("<option" +
                                                 selTag +
                                                 "value=\"" + id + "\">" +
                                                  dent +
                                                  acct.getName() +
                                              "</option>");
                                  printAccountSelect(acct.getChildren(), selAccts,
                                          out);
                              }

                              if (root)
                                  out.println("</select>");
                          }
                      }catch (Exception ex){
                          System.out.println(ex);
                      }
                  }
              %>
    </head>

    <body>
        <a id="aReloadCache" href="/gccharts/TemporalComparison?reload=1">reload cache</a>
        <h1>Temporal Comparison Charts</h1>
        

        <c:if test="${!empty ex}">
            <div id="divEx">
                ${ex}
            </div>
        </c:if>

        
        
        <div id="divData">
            <img id="imgChart" width="${width}"
                 height="${height}" src="${relPath}"/>
            <div id="divTextView">

                <%
                    TimeSeriesCollection tsc = (TimeSeriesCollection)
                            session.getAttribute("TimeSeriesCollection");
                    if (tsc != null){
                        TimeSeries ts;
                        TimeSeriesDataItem di;
                        String tab = "&nbsp;"+"&nbsp;"+"&nbsp;"+"&nbsp;";
                        String start, end;
                        Date dtmStart=null, dtmEnd = null;
                        final String PATTERN = "MMM dd ''yy";
                        for (Object o: tsc.getSeries()){
                            ts = (TimeSeries)o;
                            out.println(ts.getKey() + "<br/>");
                            for (Object o0 : ts.getItems()){
                                di = (TimeSeriesDataItem) o0;
                                dtmStart = new Date(di.getPeriod().getStart());
                                dtmEnd   = new Date(di.getPeriod().getEnd());
                                start = dtmStart.toString(PATTERN);
                                end   = dtmEnd.toString(PATTERN);
                                out.println(
                                            tab + start   + " " + "<br/>" +
                                            tab +   end     + " " +
                                            di.getValue() +
                                            "<br/>");
                            }
                        }
                     }
                %>
            </div>
        </div>
        <form action="/gccharts/TemporalComparison" method="POST">
            <div id="divForm">
                <%
                    ServletContext cx = getServletContext();
                    try{
                        Account rootAcct = (Account) cx.getAttribute("rootacct");
                        Accounts selAccts = (Accounts) session.
                                                getAttribute("selaccts");
                        printAccountSelect(rootAcct.getChildren(), selAccts, out);
                    }
                    catch(Exception ex){
                        System.out.println(ex);
                    }
                %>
                 <span id="spnFrom">
                    <span class="lable">From: </span>
                    <input id="txtFrom" name="txtFrom" type="text"
                           value="${from}">
                </span>
                <span id="spnTo">
                    <span class="lable">To: </span>
                    <input id="txtTo" name="txtTo" type="text" value="${to}" />
                </span>
                <select id="cboIntervalType" name="cboIntervalType">
                    <option value="monthly"
                        ${param.cboIntervalType == "monthly" ?
                          "selected=\"selected\"": ""}>
                            Monthly
                    </option>
                    <option value="weekly"
                        ${param.cboIntervalType == "weekly" ?
                          "selected=\"selected\"": ""}>
                        Weekly
                    </option>
                </select>
                <input id="btnSubmit" type="submit" value="Submit"/>


            </div>
        </form>
    </body>
</html>
