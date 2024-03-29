package com.clipsoft.cson;

import com.clipsoft.cson.util.NoSynchronizedStringReader;
import com.clipsoft.cson.util.NumberConversionUtil;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class PureJsonTest {


    @Test
    public void dutyJSONArray() {
        String testJSON = "[10,20,\n" +
                "    {\n" +
                "      \"name\": \"Alice\",\n" +
                "      \"age\": 30,\n" +
                "      \"address\": {\n" +
                "        \"street\": \"123 Main St\",\n" +
                "        \"city\": \"Wonderland\",\n" +
                "        \"country\": \"Fairyland\"\n" +
                "      },\n" +
                "      \"contacts\": [\n" +
                "        {\n" +
                "          \"type\": \"email\",\n" +
                "          \"contact\": \"alice@example.com\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"phone\",\n" +
                "          \"contact\": \"+123456789\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Bob\",\n" +
                "      \"age\": 25,\n" +
                "      \"address\": {\n" +
                "        \"street\": \"456 Elm St\",\n" +
                "        \"city\": \"Dreamville\",\n" +
                "        \"country\": \"Imaginationland\"\n" +
                "      },\n" +
                "      \"contacts\": [\n" +
                "        {\n" +
                "          \"type\": \"email\",\n" +
                "          \"contact\": \"bob@example.com\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"phone\",\n" +
                "          \"contact\": \"+987654321\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Charlie\",\n" +
                "      \"age\": 35,\n" +
                "      \"address\": {\n" +
                "        \"street\": \"789 Oak St\",\n" +
                "        \"city\": \"Fantasytown\",\n" +
                "        \"country\": \"Whimsyville\"\n" +
                "      },\n" +
                "      \"contacts\": [\n" +
                "        {\n" +
                "          \"type\": \"email\",\n" +
                "          \"contact\": \"charlie@example.com\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"phone\",\n" +
                "          \"contact\": \"+246813579\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]";


        CSONArray csonArraryOrigin = new CSONArray(testJSON, StringFormatOption.jsonPure());
        CSONArray csonArrary = new CSONArray(testJSON, StringFormatOption.json());

        assertEquals(csonArraryOrigin.toString(), csonArrary.toString());

        System.out.println(csonArraryOrigin.toString());

    }

    @Test
    public void NumberConversion() {
        String testJSON = "[" +Long.MIN_VALUE  + ", -0" + ", 10.11]";
        CSONArray csonArraryOrigin = new CSONArray(testJSON, StringFormatOption.jsonPure());

        System.out.println(csonArraryOrigin.toString());


    }

    @Test
    public void dutyJSON() throws IOException {
        String testJSON = "{\n" +
                "  \"user\": {\n" +
                "    \"id\": 12345,\n" +
                "    \"username\": \"mysteriousCoder\",\n" +
                "    \"email\": \"mysterious@example.com\",\n" +
                "    \"profile\": {\n" +
                "      \"name\": \"Mr. Mysterious\",\n" +
                "      \"age\": 30,\n" +
                "      \"description\": \"A person of enigmatic nature\",\n" +
                "      \"address\": {\n" +
                "        \"street\": \"Shadowy Lane\",\n" +
                "        \"city\": \"Crypticville\",\n" +
                "        \"country\": \"Enigmatica\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"preferences\": {\n" +
                "      \"theme\": \"Dark\",\n" +
                "      \"notifications\": {\n" +
                "        \"email\": true,\n" +
                "        \"push\": true,\n" +
                "        \"sms\": false\n" +
                "      },\n" +
                "      \"settings\": [\n" +
                "        {\n" +
                "          \"name\": \"Display\",\n" +
                "          \"value\": \"Night mode\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"Language\",\n" +
                "          \"value\": \"Cryptic\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"Sounds\",\n" +
                "          \"value\": \"Eerie\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"orders\": [\n" +
                "      {\n" +
                "        \"id\": \"ORD001\",\n" +
                "        \"date\": \"2023-12-01\",\n" +
                "        \"total\": 00150.25,\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"name\": \"Mystery Box\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"price\": 75.50\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"Cryptic\\nScroll\",\n" +
                "            \"quantity\": 2,\n" +
                "            \"price\": 37.75,\n" +
                "            \"zero\": -0,\n" +
                "            \"zeroPoint\": -0.0\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"ORD002\",\n" +
                "        \"date\": \"2023-12-10\",\n" +
                "        \"total\": 255,\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"name\": \"Enigmatic Puzzle\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"price\": 180.0\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"Secret Cipher\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"price\": 40.0\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";


        testJSON = testJSON.replace(" ", "").replace("\n", "");

        CSONElement csonElement = new CSONObject(testJSON, StringFormatOption.jsonPure());
        CSONObject csonObject = (CSONObject) csonElement;
        System.out.println(csonObject.toString());
        //assertEquals(testJSON.replace("0xff", "255").replace("75.50", "75.5"), csonObject.toString());

        NoSynchronizedStringReader stringReader2 = new NoSynchronizedStringReader(testJSON);
        CSONObject csonObjectPure = (CSONObject)PureJSONParser.parsePureJSON(stringReader2,NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        stringReader2.close();



        CSONObject obj = new CSONObject(StringFormatOption.json5());

        // You can change the default options. (It will be applied to all CSONObject and CONSArray)
        // CSONObject.setDefaultJSONOptions(StringFormatOption.json5());
        // Even if you change the default options, you can specify the options when creating the object.

        obj.put("name", "John");
        obj.put("age", 25);
        CSONArray friends = new CSONArray();
        friends.put("Nancy");
        friends.put("Mary");
        friends.put("Tom", "Jerry");
        obj.put("friends", friends);

        // You can add comments before and after the key, or before and after the value.
        obj.setCommentForKey("friends", "Lists only people's names.");
        obj.setCommentAfterValue("friends", "A total of 4 friends");

        obj.setCommentThis("This is a comment for this object.");
        obj.setCommentAfterThis("This is a comment after this object.");

        String yourInfo = obj.toString();
        System.out.println(yourInfo);
        //  //This is a comment for this object.
        //  {
        //      name:'John',
        //      age:25,
        //      //Lists only people's names.
        //      friends:['Nancy','Mary','Tom','Jerry']/* A total of 4 friends */
        //  }
        //  //This is a comment after this object.



    }

    @Test
    public void parsingCommentJson() {
        Exception err = null;
        String json = "{\"key\": \"5\"/*주석입니다*/,\"a\":\"b\",}";
        CSONObject csonObject = new CSONObject(json, JSONOptions.json()
                .setAllowTrailingComma(true)
                .setAllowComments(true)
                .setSkipComments(false));

        assertEquals("주석입니다", csonObject.getCommentObjectOfValue ("key").getAfterComment());

    }

    @Test
    public void wrongJsonParsingTest2() {
        Exception err = null;
        String json = "{\"key\": \"5\",\"a\":\"b\",}";
        try {
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;
    }
    @Test
    public void wrongJsonParsingTest() {
        Exception err = null;
        String json = "{\"key\": \"5\",,\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, StringFormatOption.jsonPure());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;
        try {
            CSONObject csonObject = new CSONObject(json, StringFormatOption.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;


        json = "{key: \"5\",\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;
        try {
            CSONObject csonObject = new CSONObject(json, StringFormatOption.jsonPure());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;
        json = "{'key': \"5\",\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);



        err = null;
        json = "{\"key\": \"5\",\"a\":\"b\",}";
        try {
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;




        err = null;
        json = "{\"key\": \"5\",\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNull(err);
        err = null;

    }


    @Test
    public void testPureJson2() {
        String testJSON = "{\n" +
                "    \"styleLineList\":[\n" +
                "      {\n" +
                "          \"style\":-1094297962,\n" +
                "          \"width\":977899340\r,\n" +
                "          \"color\":285955561\n" +
                "      }" +
                "    ]\n" +
                " ,\"styleLineList2\":[\n" +
                "      true" +
                " \n]\n," +
                "    \"styleLineList3\":[\n" +
                "      \"ok\"" +
                " \n]\n" +
                "}";

        CSONObject csonObject = new CSONObject(testJSON, StringFormatOption.jsonPure());
        System.out.println(csonObject.toString());

        CSONObject csonObject2 = new CSONObject(testJSON, StringFormatOption.json());

        CSONObject csonObject3 = new CSONObject(testJSON, StringFormatOption.json5());

        assertEquals(csonObject.toString(StringFormatOption.jsonPretty()), csonObject2.toString(StringFormatOption.jsonPretty()));
        assertEquals(csonObject.toString(StringFormatOption.jsonPretty()), csonObject3.toString(StringFormatOption.jsonPretty()));


    }
}
