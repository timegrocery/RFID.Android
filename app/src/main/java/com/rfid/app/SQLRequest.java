package com.rfid.app;

import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SQLRequest {
    String ip;
    public static SQLRequest SQL = new SQLRequest();
    public SQLRequest() {
        this.ip = "192.168.106.138";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public JSONObject getProductLineByID(String productLineID) throws EmptyDataException {
        if (Utils.isNullOrEmpty(productLineID)) {
            throw new EmptyDataException("Product line ID");
        }
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("product_line_id", productLineID));
        return convertToJsonObject(sendRequest("getProductLine", nameValuePairs));
    }

    public ArrayList<Product> getProduct() {
        ArrayList<Product> result = new ArrayList<>();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        JSONObject response_result = convertToJsonObject(sendRequest("getProduct", nameValuePairs));
        JSONArray response_array = new JSONArray();
        try {
            response_array = response_result.getJSONArray("product");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("Json Array:" + response_array);
        try {
            for(int i = 0; i < response_array.length(); i++) {
                JSONObject jsonObject = response_array.getJSONObject(i);
                Product product = new Product();
                product.setProduct_id(jsonObject.getInt("product_id"));
                product.setTag_id(jsonObject.getString("tag_id"));
                product.setName(jsonObject.getString("name"));
                product.setStock(jsonObject.getInt("stock"));
                product.setCount(jsonObject.getInt("count"));
                product.setColor(jsonObject.getString("color"));
                product.setCount(0);
                result.add(product);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println("get product before return: ");
        for (Product p : result) {
            System.out.println(p.toString());
        }
        return result;
    }

    public int addAuditReport() {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        String result = sendRequest("addAuditReport", nameValuePairs);
        if (result.contains("Added successfully. Last inserted audit id:")) {
            String temp = result.substring(result.indexOf("[***") + 4, result.lastIndexOf("***]"));
            System.out.println("Last inserted id: " + temp);
            int num = Integer.parseInt(temp);
            System.out.println("Last inserted id after being parsed: " + num);
            return num;
        }
        return -1;
    }

    public boolean addProductAudit(int audit_id, String tag_id) throws EmptyDataException {
        if (audit_id <= 0) {
            throw new EmptyDataException("Audit ID");
        }
        if (Utils.isNullOrEmpty(tag_id)) {
            throw new EmptyDataException("Tag ID");
        }
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("audit_id", String.valueOf(audit_id)));
        nameValuePairs.add(new BasicNameValuePair("tag_id", tag_id));
        String result = sendRequest("addProductAudit", nameValuePairs);
        System.out.println("Add product <" + audit_id + "> result:" + result);
        if (result.contains("Added successfully")) {
            System.out.println("Added product <" + audit_id + "> to product_audit");
            return true;
        } else {
            System.out.println("Failed to add product <" + audit_id + "> to product_audit");
            return false;
        }
    }

    public JSONObject getAuditReport() {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        return convertToJsonObject(sendRequest("getAuditReport", nameValuePairs));
    }

    public String sendRequest(String phpFile, List<NameValuePair> nameValuePairs) {
        String result = "";
        try {
            // params
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 5000);
            HttpConnectionParams.setSoTimeout(params, 5000);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpProtocolParams.setUseExpectContinue(params, true);
            // defaultHttpClient
            HttpClient httpclient = new DefaultHttpClient(params);
            HttpPost httppost = new HttpPost("http://" + ip + "/platform/"+ phpFile + ".php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            reader.close();
            result = sb.toString();
            result = result.replace("\\\"","'");

        } catch(Exception e) {
            Log.e("log_tag", "Error converting result " + e.toString());
            result = "No data";
        }
        return result;
    }

    public JSONObject convertToJsonObject(String result) {
        result = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
        //parse json data
        JSONObject jObj = new JSONObject();
        try {
            jObj = new JSONObject(result);
        } catch (Exception e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            try {
                jObj = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
            } catch (Exception e0) {
                Log.e("JSON Parser0", "Error parsing data [" + e0.getMessage()+"] "+ result);
                Log.e("JSON Parser0", "Error parsing data " + e0.toString());
                try {
                    jObj = new JSONObject(result.substring(1));
                } catch (Exception e1) {
                    Log.e("JSON Parser1", "Error parsing data [" + e1.getMessage()+"] "+result);
                    Log.e("JSON Parser1", "Error parsing data " + e1.toString());
                    try {
                        jObj = new JSONObject(result.substring(2));
                    } catch (Exception e2) {
                        Log.e("JSON Parser2", "Error parsing data [" + e2.getMessage()+"] "+ result);
                        Log.e("JSON Parser2", "Error parsing data " + e2.toString());
                        try {
                            jObj = new JSONObject(result.substring(3));
                        } catch (Exception e3) {
                            Log.e("JSON Parser3", "Error parsing data [" + e3.getMessage()+"] "+ result);
                            Log.e("JSON Parser3", "Error parsing data " + e3.toString());
                        }
                    }
                }
            }
        }

        // return JSON String
        System.out.println("Json object: " + jObj.toString());
        return jObj;
    }

    public JSONArray convertToJsonArray(String result) {
        result = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
        //parse json data
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new JSONArray(result);
        } catch (Exception e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            try {
                jsonArray = new JSONArray(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
            } catch (Exception e0) {
                Log.e("JSON Parser0", "Error parsing data [" + e0.getMessage()+"] "+ result);
                Log.e("JSON Parser0", "Error parsing data " + e0.toString());
                try {
                    jsonArray = new JSONArray(result.substring(1));
                } catch (Exception e1) {
                    Log.e("JSON Parser1", "Error parsing data [" + e1.getMessage()+"] "+result);
                    Log.e("JSON Parser1", "Error parsing data " + e1.toString());
                    try {
                        jsonArray = new JSONArray(result.substring(2));
                    } catch (Exception e2) {
                        Log.e("JSON Parser2", "Error parsing data [" + e2.getMessage()+"] "+ result);
                        Log.e("JSON Parser2", "Error parsing data " + e2.toString());
                        try {
                            jsonArray = new JSONArray(result.substring(3));
                        } catch (Exception e3) {
                            Log.e("JSON Parser3", "Error parsing data [" + e3.getMessage()+"] "+ result);
                            Log.e("JSON Parser3", "Error parsing data " + e3.toString());
                        }
                    }
                }
            }
        }

        // return JSON String
        System.out.println("Json array: " + jsonArray.toString());
        return jsonArray;
    }
}
