<%@ taglib uri="/WEB-INF/lib/reports_tld.jar" prefix="rw" %> 
<%@ page language="java" import="java.io.*" errorPage="/rwerror.jsp" session="false" %>
<%@ page contentType="text/html;charset=ISO-8859-1" %>
<!--
<rw:report id="report"> 
<rw:objects id="objects">
<?xml version="1.0" encoding="UTF-8" ?>
<report name="Booking" DTDVersion="9.0.2.0.10">
  <xmlSettings xmlTag="BOOKING" xmlPrologType="text">
  <![CDATA[<?xml version="1.0" encoding="&Encoding"?>]]>
  </xmlSettings>
  <reportHtmlEscapes>
    <beforePageHtmlEscape>
    <![CDATA[#NULL#]]>
    </beforePageHtmlEscape>
  </reportHtmlEscapes>
  <data>
    <dataSource name="Q_1">
      <select canParse="no">
      <![CDATA[SELECT 
  CASE WHEN name.birth_date is NULL THEN '' END as birth, 
  name.gender, 
  name.nationality, 
  reservation_name.confirmation_no as booking_no, 
  reservation_name.arrival_date_time as arrival_date, 
  reservation_name.departure_date_time as departure_date, 
  reservation_name.UDFC03 as CT, 
  reservation_name.UDFC05 as POS, 
  reservation_name.payment_method as pm, 
  reservation_name.resv_status as status, 
  reservation_daily_elements.room, 
  reservation_daily_element_name.reservation_date as res_date, 
  reservation_daily_element_name.base_rate_amount as amount, 
  CASE WHEN reservation_daily_element_name.discount_amt is NULL THEN 0 END as disc, 
  CASE WHEN reservation_daily_element_name.discount_prcnt is NULL THEN 0 END as disc_prcnt, 
  reservation_daily_element_name.adults, 
  reservation_daily_element_name.children 
FROM 
  reservation_name 
  INNER JOIN name ON reservation_name.name_id = name.name_id 
  INNER JOIN reservation_daily_element_name ON reservation_name.resv_name_id = reservation_daily_element_name.resv_name_id 
  inner join reservation_daily_elements on reservation_daily_element_name.resv_daily_el_seq = reservation_daily_elements.resv_daily_el_seq 
WHERE 
  reservation_name.update_date >= trunc(sysdate) 
  And reservation_name.update_date < trunc(sysdate) + 1 
  and (
    reservation_name.resv_status = 'RESERVED' 
    or reservation_name.resv_status = 'CHECKED IN' 
    or reservation_name.resv_status = 'CHECKED OUT'
  ) 
ORDER BY 
  reservation_daily_element_name.reservation_date;]]>
      </select>
      <displayInfo x="0.69788" y="0.68750" width="0.69995" height="0.19995"/>
      <group name="G_BIRTH_DATE">
        <displayInfo x="0.23828" y="1.38745" width="1.61926" height="1.62695"
        />
        <dataItem name="GENDER" datatype="vchar2" columnOrder="11" width="1"
         defaultWidth="10000" defaultHeight="10000" columnFlags="33"
         defaultLabel="Gender">
          <dataDescriptor expression="GENDER" descriptiveExpression="GENDER"
           order="2" width="1"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="NATIONALITY" datatype="vchar2" columnOrder="12"
         width="20" defaultWidth="100000" defaultHeight="10000"
         columnFlags="33" defaultLabel="Nationality">
          <dataDescriptor expression="NATIONALITY"
           descriptiveExpression="NATIONALITY" order="3" width="20"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="BOOKING_NO" datatype="vchar2" columnOrder="13"
         width="50" defaultWidth="100000" defaultHeight="10000"
         columnFlags="33" defaultLabel="Booking No">
          <dataDescriptor expression="BOOKING_NO"
           descriptiveExpression="BOOKING_NO" order="4" width="50"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="ARRIVAL_DATE" datatype="date" oracleDatatype="date"
         columnOrder="14" width="9" defaultWidth="90000" defaultHeight="10000"
         columnFlags="33" defaultLabel="Arrival Date">
          <dataDescriptor expression="ARRIVAL_DATE"
           descriptiveExpression="ARRIVAL_DATE" order="5"
           oracleDatatype="date" width="9"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="DEPARTURE_DATE" datatype="date" oracleDatatype="date"
         columnOrder="15" width="9" defaultWidth="90000" defaultHeight="10000"
         columnFlags="33" defaultLabel="Departure Date">
          <dataDescriptor expression="DEPARTURE_DATE"
           descriptiveExpression="DEPARTURE_DATE" order="6"
           oracleDatatype="date" width="9"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="CT" datatype="vchar2" columnOrder="16" width="200"
         defaultWidth="100000" defaultHeight="10000" columnFlags="33"
         defaultLabel="Ct">
          <dataDescriptor expression="CT" descriptiveExpression="CT" order="7"
           width="200"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="POS" datatype="vchar2" columnOrder="17" width="200"
         defaultWidth="100000" defaultHeight="10000" columnFlags="33"
         defaultLabel="Pos">
          <dataDescriptor expression="POS" descriptiveExpression="POS"
           order="8" width="200"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="PM" datatype="vchar2" columnOrder="18" width="50"
         defaultWidth="100000" defaultHeight="10000" columnFlags="33"
         defaultLabel="Pm">
          <dataDescriptor expression="PM" descriptiveExpression="PM" order="9"
           width="50"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="STATUS" datatype="vchar2" columnOrder="19" width="20"
         defaultWidth="100000" defaultHeight="10000" columnFlags="33"
         defaultLabel="Status">
          <dataDescriptor expression="STATUS" descriptiveExpression="STATUS"
           order="10" width="20"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="ROOM" datatype="vchar2" columnOrder="20" width="20"
         defaultWidth="100000" defaultHeight="10000" columnFlags="33"
         defaultLabel="Room">
          <dataDescriptor expression="ROOM" descriptiveExpression="ROOM"
           order="11" width="20"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="RES_DATE" datatype="date" oracleDatatype="date"
         columnOrder="21" width="9" defaultWidth="90000" defaultHeight="10000"
         columnFlags="33" defaultLabel="Res Date">
          <dataDescriptor expression="RES_DATE"
           descriptiveExpression="RES_DATE" order="12" oracleDatatype="date"
           width="9"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="AMOUNT" oracleDatatype="number" columnOrder="22"
         width="22" defaultWidth="20000" defaultHeight="10000"
         columnFlags="33" defaultLabel="Amount">
          <dataDescriptor expression="AMOUNT" descriptiveExpression="AMOUNT"
           order="13" oracleDatatype="number" width="22" scale="-127"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="DISC" oracleDatatype="number" columnOrder="23"
         width="1" defaultWidth="90000" defaultHeight="10000" columnFlags="33"
         defaultLabel="Disc">
          <dataDescriptor expression="DISC" descriptiveExpression="DISC"
           order="14" oracleDatatype="number" width="1" precision="38"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="DISC_PRCNT" oracleDatatype="number" columnOrder="24"
         width="1" defaultWidth="90000" defaultHeight="10000" columnFlags="33"
         defaultLabel="Disc Prcnt">
          <dataDescriptor expression="DISC_PRCNT"
           descriptiveExpression="DISC_PRCNT" order="15"
           oracleDatatype="number" width="1" precision="38"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="ADULTS" oracleDatatype="number" columnOrder="25"
         width="22" defaultWidth="20000" defaultHeight="10000"
         columnFlags="33" defaultLabel="Adults">
          <dataDescriptor expression="ADULTS" descriptiveExpression="ADULTS"
           order="16" oracleDatatype="number" width="22" scale="-127"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="CHILDREN" oracleDatatype="number" columnOrder="26"
         width="22" defaultWidth="20000" defaultHeight="10000"
         columnFlags="33" defaultLabel="Children">
          <dataDescriptor expression="CHILDREN"
           descriptiveExpression="CHILDREN" order="17" oracleDatatype="number"
           width="22" scale="-127"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
        <dataItem name="BIRTH" datatype="character"
         oracleDatatype="aFixedChar" columnOrder="27" width="0"
         defaultWidth="10000" defaultHeight="10000" columnFlags="33"
         defaultLabel="Birth">
          <dataDescriptor expression="BIRTH" descriptiveExpression="BIRTH"
           order="1"/>
          <dataItemPrivate adtName="" schemaName=""/>
        </dataItem>
      </group>
    </dataSource>
  </data>
  <layout>
  <section name="main">
    <body>
      <frame name="M_G_BIRTH_DATE_GRPFR">
        <geometryInfo x="0.00000" y="0.00000" width="7.50000" height="0.25000"
        />
        <generalLayout verticalElasticity="variable"/>
        <visualSettings fillPattern="transparent"
         fillBackgroundColor="r100g100b100"/>
        <repeatingFrame name="R_G_BIRTH_DATE" source="G_BIRTH_DATE"
         printDirection="down" minWidowRecords="1" columnMode="no">
          <geometryInfo x="0.00000" y="0.12500" width="7.50000"
           height="0.12500"/>
          <generalLayout verticalElasticity="expand"/>
          <visualSettings fillPattern="transparent"
           fillBackgroundColor="TableCell"/>
          <field name="F_GENDER" source="GENDER" minWidowLines="1" spacing="0"
           alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="0.00000" y="0.12500" width="0.31250"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_NATIONALITY" source="NATIONALITY" minWidowLines="1"
           spacing="0" alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="0.31250" y="0.12500" width="0.37500"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_BOOKING_NO" source="BOOKING_NO" minWidowLines="1"
           spacing="0" alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="0.68750" y="0.12500" width="0.43750"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_ARRIVAL_DATE" source="ARRIVAL_DATE" minWidowLines="1"
           spacing="0" alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="1.12500" y="0.12500" width="0.68750"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_DEPARTURE_DATE" source="DEPARTURE_DATE"
           minWidowLines="1" spacing="0" alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="1.81250" y="0.12500" width="0.68750"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_CT" source="CT" minWidowLines="1" spacing="0"
           alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="2.50000" y="0.12500" width="0.37500"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_POS" source="POS" minWidowLines="1" spacing="0"
           alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="2.87500" y="0.12500" width="0.37500"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_PM" source="PM" minWidowLines="1" spacing="0"
           alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="3.25000" y="0.12500" width="0.37500"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_STATUS" source="STATUS" minWidowLines="1" spacing="0"
           alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="3.62500" y="0.12500" width="0.37500"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_ROOM" source="ROOM" minWidowLines="1" spacing="0"
           alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="4.00000" y="0.12500" width="0.37500"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_RES_DATE" source="RES_DATE" minWidowLines="1"
           spacing="0" alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="4.37500" y="0.12500" width="0.68750"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_AMOUNT" source="AMOUNT" minWidowLines="1" spacing="0"
           alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="5.06250" y="0.12500" width="0.31250"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_DISC" source="DISC" minWidowLines="1" spacing="0"
           alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="5.37500" y="0.12500" width="0.68750"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_DISC_PRCNT" source="DISC_PRCNT" minWidowLines="1"
           spacing="0" alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="6.06250" y="0.12500" width="0.68750"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_ADULTS" source="ADULTS" minWidowLines="1" spacing="0"
           alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="6.75000" y="0.12500" width="0.25000"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_CHILDREN" source="CHILDREN" minWidowLines="1"
           spacing="0" alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="7.00000" y="0.12500" width="0.31250"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
          <field name="F_BIRTH" source="BIRTH" minWidowLines="1" spacing="0"
           alignment="start">
            <font face="helvetica" size="5"/>
            <geometryInfo x="7.31250" y="0.12500" width="0.18750"
             height="0.12500"/>
            <generalLayout verticalElasticity="expand"/>
          </field>
        </repeatingFrame>
        <frame name="M_G_BIRTH_DATE_HDR">
          <geometryInfo x="0.00000" y="0.00000" width="7.50000"
           height="0.12500"/>
          <advancedLayout printObjectOnPage="allPage"
           basePrintingOn="anchoringObject"/>
          <visualSettings fillPattern="transparent"
           fillBackgroundColor="TableHeading"/>
          <text name="B_GENDER" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="0.00000" y="0.00000" width="0.31250"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="r25g50b75"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Gender]]>
              </string>
            </textSegment>
          </text>
          <text name="B_NATIONALITY" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="0.31250" y="0.00000" width="0.37500"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="r25g50b75"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Nationality]]>
              </string>
            </textSegment>
          </text>
          <text name="B_BOOKING_NO" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="0.68750" y="0.00000" width="0.43750"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="r25g50b75"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Booking No]]>
              </string>
            </textSegment>
          </text>
          <text name="B_ARRIVAL_DATE" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="1.12500" y="0.00000" width="0.68750"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="TableHeading"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Arrival Date]]>
              </string>
            </textSegment>
          </text>
          <text name="B_DEPARTURE_DATE" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="1.81250" y="0.00000" width="0.68750"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="TableHeading"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Departure Date]]>
              </string>
            </textSegment>
          </text>
          <text name="B_CT" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="2.50000" y="0.00000" width="0.37500"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="r25g50b75"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Ct]]>
              </string>
            </textSegment>
          </text>
          <text name="B_POS" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="2.87500" y="0.00000" width="0.37500"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="r25g50b75"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Pos]]>
              </string>
            </textSegment>
          </text>
          <text name="B_PM" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="3.25000" y="0.00000" width="0.37500"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="r25g50b75"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Pm]]>
              </string>
            </textSegment>
          </text>
          <text name="B_STATUS" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="3.62500" y="0.00000" width="0.37500"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="r25g50b75"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Status]]>
              </string>
            </textSegment>
          </text>
          <text name="B_ROOM" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="4.00000" y="0.00000" width="0.37500"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="r25g50b75"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Room]]>
              </string>
            </textSegment>
          </text>
          <text name="B_RES_DATE" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="4.37500" y="0.00000" width="0.68750"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="TableHeading"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Res Date]]>
              </string>
            </textSegment>
          </text>
          <text name="B_AMOUNT" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="5.06250" y="0.00000" width="0.31250"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="TableHeading"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Amount]]>
              </string>
            </textSegment>
          </text>
          <text name="B_DISC" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="5.37500" y="0.00000" width="0.68750"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="TableHeading"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Disc]]>
              </string>
            </textSegment>
          </text>
          <text name="B_DISC_PRCNT" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="6.06250" y="0.00000" width="0.68750"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="TableHeading"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Disc Prcnt]]>
              </string>
            </textSegment>
          </text>
          <text name="B_ADULTS" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="6.75000" y="0.00000" width="0.25000"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="TableHeading"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Adults]]>
              </string>
            </textSegment>
          </text>
          <text name="B_CHILDREN" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="7.00000" y="0.00000" width="0.31250"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="TableHeading"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Children]]>
              </string>
            </textSegment>
          </text>
          <text name="B_BIRTH" minWidowLines="1">
            <textSettings spacing="0"/>
            <geometryInfo x="7.31250" y="0.00000" width="0.18750"
             height="0.12500"/>
            <visualSettings fillBackgroundColor="r25g50b75"/>
            <textSegment>
              <font face="helvetica" size="5" bold="yes" textColor="TextColor"
              />
              <string>
              <![CDATA[Birth]]>
              </string>
            </textSegment>
          </text>
        </frame>
      </frame>
    </body>
  </section>
  </layout>
  <colorPalette>
    <color index="190" displayName="TextColor" value="#336699"/>
    <color index="191" displayName="TableHeading" value="#cccc99"/>
    <color index="192" displayName="TableCell" value="#f7f7e7"/>
    <color index="193" displayName="Totals" value="#ffffcc"/>
  </colorPalette>
  <reportPrivate defaultReportType="tabular" versionFlags2="0"
   templateName="rwbeige"/>
  <reportWebSettings>
  <![CDATA[#NULL#]]>
  </reportWebSettings>
</report>
</rw:objects>
-->

<html>

<head>
<meta name="GENERATOR" content="Oracle 11gR1 Reports Developer"/>
<title> Your Title </title>

<rw:style id="yourStyle">
   <!-- Report Wizard inserts style link clause here -->
</rw:style>

</head>


<body>

<rw:dataArea id="yourDataArea">
   <!-- Report Wizard inserts the default jsp here -->
</rw:dataArea>



</body>
</html>

<!--
</rw:report> 
-->
