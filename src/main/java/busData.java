import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
//todo 모듈화
public class busData {
    private String routeId;
    private String routeName;
    private String locationNo1;
    private String locationNo2;
    private String predictTime1;
    private String predictTime2;
    private String remainSeatCnt1;
    private String remainSeatCnt2;

    busData(String routeId, String locationNo1, String locationNo2, String predictTime1, String predictTime2, String remainSeatCnt1, String remainSeatCnt2) {
        this.routeId = routeId;
        this.locationNo1 = locationNo1;
        this.locationNo2 = locationNo2;
        this.predictTime1 = predictTime1;
        this.predictTime2 = predictTime2;
        this.remainSeatCnt1 = remainSeatCnt1;
        this.remainSeatCnt2 = remainSeatCnt2;
    }
    public static String getDataToApi(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-type", "application/json");

        BufferedReader Rd;
        if (urlConnection.getResponseCode() >= 200 && urlConnection.getResponseCode() <= 300) {
            Rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        } else {
            Rd = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String rLine;
        while ((rLine = Rd.readLine()) != null) {
            sb.append(rLine);
        }
        Rd.close();
        urlConnection.disconnect();
        String str = sb.toString();
        return str;
    }
    public static NodeList parseXml(String xml, String tagName) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory stFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder stBuilder = null;
        stBuilder = stFactory.newDocumentBuilder();
        Document stDoc = stBuilder.parse(new org.xml.sax.InputSource(new StringReader(xml)));

        stDoc.getDocumentElement().normalize();
        NodeList nList = stDoc.getElementsByTagName(tagName);
        return nList;
    }
    public static String busStop(int station) {
        String clientID = "";
        String clientSecret = "";
        String dataText = "";

        System.out.println("busstop");
        busData[] busData = new busData[0];
        try {
            String busStopServiceKey = "";
            String busStopApi = "http://apis.data.go.kr/6410000/busarrivalservice/getBusArrivalList?serviceKey=" + busStopServiceKey + "&stationId=" + station;

            String stopXml = getDataToApi(busStopApi);
            NodeList stList = parseXml(stopXml, "busArrivalList");

            busData = new busData[stList.getLength()];
            for (int i = 0; i < stList.getLength(); i++) {
                Node stNode = stList.item(i);
                if (stNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element stElement = (Element) stNode;
                    String routeId = stElement.getElementsByTagName("routeId").item(0).getTextContent();
                    String locationNo1 = stElement.getElementsByTagName("locationNo1").item(0).getTextContent();
                    String locationNo2 = stElement.getElementsByTagName("locationNo2").item(0).getTextContent();
                    String predictTime1 = stElement.getElementsByTagName("predictTime1").item(0).getTextContent();
                    String predictTime2 = stElement.getElementsByTagName("predictTime2").item(0).getTextContent();
                    String remainSeatCnt1 = stElement.getElementsByTagName("remainSeatCnt1").item(0).getTextContent();
                    String remainSeatCnt2 = stElement.getElementsByTagName("remainSeatCnt2").item(0).getTextContent();

                    busData[i] = new busData(routeId, locationNo1, locationNo2, predictTime1, predictTime2, remainSeatCnt1, remainSeatCnt2);
                }
            }
            for (int i = 0; i < busData.length; i++) {
                int busId = Integer.parseInt(busData[i].routeId);
                String busInfoServiceKey = "";
                String busInfoApi = "http://apis.data.go.kr/6410000/busrouteservice/getBusRouteInfoItem?serviceKey=" + busInfoServiceKey + "&routeId=" + busId;

                String biXml = getDataToApi(busInfoApi);
                NodeList biList = parseXml(biXml,"busRouteInfoItem");

                for (int j = 0; j < biList.getLength(); j++) {
                    Node biNode = biList.item(j);
                    if (biNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element biElement = (Element) biNode;
                        String routeName = biElement.getElementsByTagName("routeName").item(0).getTextContent();
                        busData[i].routeName = routeName;
                    }
                }
            }
            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 기준");
            String formatedNow = now.format(formatter);

            if(station == 204000060){
                dataText += ":clipboard:\t*삼평교 방면 버스 정보 (" + formatedNow + ")*\n\n";
            }
            else{
                dataText += ":clipboard:\t*나라기록관.코이카 방면 버스 정보 ("+ formatedNow + ")*\n\n";
            }
            dataText += ">>>";
            for (int i = 0; i < busData.length; i++) {
                dataText += ":small_blue_diamond:*" + busData[i].routeName + "번*\n";
                dataText += "=============================\n";
                if(!Objects.equals(busData[i].remainSeatCnt1, "-1")) {
                    dataText += busData[i].predictTime1 + " 분 후 도착\t" + busData[i].locationNo1 + "정류장\t" + busData[i].remainSeatCnt1 + "석\n";
                }
                else{
                    dataText += busData[i].predictTime1 + " 분 후 도착\t" + busData[i].locationNo1 + "정류장\n";
                }

                if (busData[i].predictTime2.length() != 0) {
                    if(!Objects.equals(busData[i].remainSeatCnt2, "-1")) {
                        dataText += busData[i].predictTime2 + " 분 후 도착\t" + busData[i].locationNo2 + "정류장\t" + busData[i].remainSeatCnt2 + "석\n";
                    }
                    else{
                        dataText += busData[i].predictTime2 + " 분 후 도착\t" + busData[i].locationNo2 + "정류장\n";
                    }
                }
                dataText += "=============================\n\n\n";
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return dataText;
    }
}
