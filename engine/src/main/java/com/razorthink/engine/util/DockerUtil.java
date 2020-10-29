package com.razorthink.engine.util;

/**
 * Docker Util Main Code
 *
 */
import com.google.gson.*;
import com.razorthink.engine.bean.DockerCreateParameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.razorthink.engine.exception.ResourceNotFoundException;
import com.razorthink.engine.exception.BadRequestException;

@Data
@NoArgsConstructor
public class DockerUtil {

    private String containerName;

    public DockerUtil(String name)
    {
        containerName=name;
    }

    private static final Logger logger = LoggerFactory.getLogger(DockerUtil.class);

    public void addContainer( DockerCreateParameter parameter,String ipHost,String port) throws BadRequestException
    {
        int code=0;
        try{
            System.out.println(parameter.toString());
            System.out.println("http://"+ipHost+":"+port+"/containers/create?name="+this.containerName);
            URL url = new URL ("http://"+ipHost+":"+port+"/containers/create?name="+this.containerName);
            DockerUtilsHelper helper=new DockerUtilsHelper();
            HttpURLConnection connection = helper.createConnection(url,"POST");
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String jsonInputString = gson.toJson(parameter);
            helper.writeRequest(jsonInputString,connection);
            code = connection.getResponseCode();
            helper.readResponse(connection);
            logger.info("Container created successfully");
        }
        catch(IOException e)
        {
            logger.error("Exception:",e);
            if(code==404)
            {
                logger.error("Provided image name:{} does not exist",parameter.getImage());
                throw new ResourceNotFoundException("Provided image name"+parameter.getImage()+" does not exist");
            }
            if(code==400)
            {
                logger.error("Bad parameter in request");
                throw new BadRequestException("Bad parameter in request");
            }

            if(code==409)
            {
                logger.error("Container with name: {} already exists",this.containerName);
                throw new  BadRequestException("Container with name: "+this.containerName+" already exists ");
            }

            if(code==500)
            {
                logger.error("Image name should be all lower case");
                throw new BadRequestException("Image name should be all lower case");
            }

        }
    }

    public void startContainer(String ipHost,String port) throws BadRequestException
    {
        int code=0;
        try {
            URL url = new URL("http://"+ipHost+":"+port +"/containers/" + this.containerName + "/start");
            DockerUtilsHelper helper=new DockerUtilsHelper();
            HttpURLConnection connection = helper.createConnection(url,"POST");
            String jsonInputString = "{}";
            helper.writeRequest(jsonInputString,connection);
            code = connection.getResponseCode();
            helper.readResponse(connection);
            if(code==304)
                throw new IOException();
            logger.info("Container started successfully");
        }
        catch(IOException e)
        {
            logger.error("Exception: ",e);
            if(code==304)
            {
                logger.error("Container with name: {} is already running",this.containerName);
                throw new BadRequestException("Container with name: "+ this.containerName +" is already running");
            }

            if(code ==404)
            {
                logger.error("Container with name: {} does not exists",this.containerName);
                throw new BadRequestException("Container with name: "+ this.containerName +" does not exists");
            }

        }
    }

    public void updateContainer(String ipHost,String port, long memory, long cpu)
    {
        int code=0;
        try {
            URL url = new URL("http://"+ipHost+":"+port +"/containers/" + this.containerName + "/update");
            DockerUtilsHelper helper=new DockerUtilsHelper();
            HttpURLConnection connection = helper.createConnection(url,"POST");
            Map<String,Long> map= new HashMap<>();
            map.put("Memory", memory);
            map.put("NanoCpus",cpu);
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String jsonInputString = gson.toJson(map);
            helper.writeRequest(jsonInputString,connection);
            code = connection.getResponseCode();
            helper.readResponse(connection);
            if(code==304)
                throw new IOException();
            logger.info("Container updated successfully");
        }
        catch(IOException e)
        {
            logger.error("Exception: ",e);
            if(code==400)
                logger.error("Bad parameter : ",this.containerName);
            if(code ==404)
                logger.error("Container with name: {} does not exists",this.containerName);
        }
    }

    public void stopContainer(String ipHost,String port) throws BadRequestException
    {
        int code=0;
        try {
            URL url = new URL("http://"+ipHost+":"+port +"/containers/" + this.containerName + "/stop");
            DockerUtilsHelper helper=new DockerUtilsHelper();
            HttpURLConnection connection = helper.createConnection(url,"POST");
            String jsonInputString = "{}";
            helper.writeRequest(jsonInputString,connection);
            code = connection.getResponseCode();
            helper.readResponse(connection);
            if(code==304)
                throw new IOException();
            logger.info("Container stopped successfully");
        }
        catch(IOException e)
        {
            logger.error("Exception"+e);
            if(code==304)
            {
                logger.error("Container with name: {} has already been stopped",this.containerName);
                throw new BadRequestException("Container with name: "+ this.containerName +" has already been stopped");
            }
            if(code ==404)
            {
                logger.error("Container with name: {} does not exists",this.containerName);
                throw new BadRequestException("Container with name: "+ this.containerName +" does not exists");
            }

        }
    }
    public void deleteContainer(String ipHost,String port) throws BadRequestException
    {
        int code=0;
        try {
            URL url = new URL("http://"+ipHost+":"+port +"/containers/" + this.containerName);
            DockerUtilsHelper helper=new DockerUtilsHelper();
            HttpURLConnection connection = helper.createConnection(url,"DELETE");
            String jsonInputString = "{}";
            helper.writeRequest(jsonInputString,connection);
            code = connection.getResponseCode();
            helper.readResponse(connection);
            logger.info("Container deleted successfully");
        }
        catch(IOException e)
        {
            logger.error("Exception :",e);
            if(code ==404)
            {
                logger.error("Container with name: {} does not exists",this.containerName);
                throw new BadRequestException("Container with name: "+this.containerName+"does not exists");
            }

        }
    }
    public String healthCheck(String ipHost,String port)
    {
        int code=0;
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("http://"+ipHost+":"+port +"/containers/"+this.containerName+"/json");
            HttpResponse httpresponse = httpclient.execute(httpget);
            Scanner sc = new Scanner(httpresponse.getEntity().getContent());
            StringBuilder response = new StringBuilder();
            code=httpresponse.getStatusLine().getStatusCode();
            if(code!=200)
                throw new IOException();
            while(sc.hasNext()) {
                response.append(sc.nextLine().trim());
            }
            JsonParser parser = new JsonParser();
            JsonObject parsedResponse = (JsonObject) parser.parse(response.toString());
            JsonElement result = parsedResponse.get("State");
            String resultString=result.toString();
            logger.info(resultString);
            return resultString;
        }
        catch(HttpHostConnectException e)
        {
            logger.error("Docker TCP socket is not set");
        }
        catch(IOException e)
        {
            logger.error("Exception:"+e);
            if(code==404)
                logger.error("Container with name: {} does not exists",this.containerName);
        }
        return null;
    }
}

class DockerUtilsHelper {
    /**
     * Creates a connection.
     *
     * @param url
     *            the url to connect to.
     * @param  requestMethod
     *              GET, POST or DELETE
     */
    private static final Logger logger = LoggerFactory.getLogger(DockerUtilsHelper.class);

    public HttpURLConnection createConnection(URL url, String requestMethod) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(requestMethod);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        return connection;
    }
    /**
     * Makes Request
     *
     * @param jsonInputString
     *            Request Body json.
     * @param  connection
     *              The HttpURLConnection created in createConnection method.
     */
    public void writeRequest(String jsonInputString, HttpURLConnection connection) throws IOException
    {
        try( OutputStream os = connection.getOutputStream()){
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }
    /**
     * Reads Request
     *
     * @param  connection
     *              The HttpURLConnection is created in createConnection method.
     */
    public void readResponse(HttpURLConnection connection) throws IOException
    {
        try ( BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            logger.info(response.toString());
        }
    }
}