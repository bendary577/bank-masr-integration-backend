package com.sun.supplierpoc.components;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.opera.booking.OccupancyDetails;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OccupancyXMLHelper {
    private OccupancyDetails readOccupancyRow(File file) {
        int totalRooms;
        OccupancyDetails occupancyDetails = new OccupancyDetails();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("G_TOTAL_ROOMS");

            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(0);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    totalRooms = Integer.parseInt(element.getElementsByTagName("TOTAL_ROOMS").item(0).getTextContent());
                    occupancyDetails.roomsAvailable = Integer.parseInt(element.getElementsByTagName("ROOMS_AVAILABLE").item(0).getTextContent());
                    occupancyDetails.roomsOccupied = Integer.parseInt(element.getElementsByTagName("ROOMS_OCCUPIED").item(0).getTextContent());
                    occupancyDetails.roomsOnMaintenance = Integer.parseInt(element.getElementsByTagName("ROOMS_ON_MAINTENANCE").item(0).getTextContent());
                    occupancyDetails.roomsBooked = Integer.parseInt(element.getElementsByTagName("ROOMS_BOOKEED").item(0).getTextContent());
                    occupancyDetails.totalRooms = occupancyDetails.roomsAvailable + occupancyDetails.roomsOccupied +
                            occupancyDetails.roomsOnMaintenance + occupancyDetails.roomsBooked;
                }

            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return occupancyDetails;
    }
}
