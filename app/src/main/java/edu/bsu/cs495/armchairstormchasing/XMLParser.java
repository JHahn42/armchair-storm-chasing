package edu.bsu.cs495.armchairstormchasing;

import android.util.Xml;

import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class XMLParser {

    private static final String ns = null;

    public ArrayList<Folder> Parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private ArrayList<Folder> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Folder> folders = new ArrayList<>();
        Boolean isFirstFolder = true;

        parser.require(XmlPullParser.START_TAG, ns, "kml");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Folder") && isFirstFolder) {
                parser.nextTag();
                isFirstFolder = false;
                name = parser.getName();
            }
            if (name.equals("Folder") && !isFirstFolder) {
                folders.add(readFolder(parser));
            } else {
                skip(parser);
            }
        }

        return folders;
    }

    private Folder readFolder(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Folder");
        String name = null;
        String coordinates;
        ArrayList<ArrayList<GeoPoint>> polygons = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String entryName = parser.getName();
            if (entryName.equals("name")) {
                name = readName(parser);
            } else if (entryName.equals("Placemark")) {
                coordinates = readPlacemark(parser);
                ArrayList<GeoPoint> polygon = createPolygon(coordinates);
                polygons.add(polygon);
            } else {
                skip(parser);
            }
        }

        return new Folder(name, polygons);
    }

    private String readPlacemark(XmlPullParser parser) throws XmlPullParserException, IOException {
        String coordinates = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String entryName = parser.getName();
            if (entryName.equals("MultiGeometry")) {
                coordinates = readMultiGeometry(parser);
            } else {
                skip(parser);
            }
        }

        return coordinates;
    }

    private String readMultiGeometry(XmlPullParser parser) throws XmlPullParserException, IOException {
        String coordinates = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String entryName = parser.getName();
            if (entryName.equals("Polygon")) {
                coordinates = readPolygon(parser);
            } else {
                skip(parser);
            }
        }

        return coordinates;
    }

    private String readPolygon(XmlPullParser parser) throws XmlPullParserException, IOException {
        String coordinates = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String entryName = parser.getName();
            if (entryName.equals("outerBoundaryIs")) {
                coordinates = readOuterBoundary(parser);
            } else {
                skip(parser);
            }
        }

        return coordinates;
    }

    private String readOuterBoundary(XmlPullParser parser) throws XmlPullParserException, IOException {
        String coordinates = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String entryName = parser.getName();
            if (entryName.equals("LinearRing")) {
                coordinates = readLinearRing(parser);
            } else {
                skip(parser);
            }
        }

        return coordinates;
    }


    private String readLinearRing(XmlPullParser parser) throws XmlPullParserException, IOException {
        String coordinates = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String entryName = parser.getName();
            if (entryName.equals("coordinates")) {
                coordinates = readCoordinates(parser);
            } else {
                skip(parser);
            }
        }

        return coordinates;
    }

    private String readCoordinates(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "coordinates");
        String coordinates = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "coordinates");
        return coordinates;
    }

    private String readName(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return name;
    }

    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }

        return result;
    }

    private ArrayList<GeoPoint> createPolygon(String polygon) {
        String[] polygonList = polygon.split(" ");
        ArrayList<GeoPoint> result = new ArrayList<>();
        for (int i = 0; i < polygonList.length; i++) {
            String[] stringPoint = polygonList[i].split(",");
            double latitude = Double.parseDouble(stringPoint[1]);
            double longitude = Double.parseDouble(stringPoint[0]);
            GeoPoint point = new GeoPoint(latitude, longitude);
            result.add(point);
        }

        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
