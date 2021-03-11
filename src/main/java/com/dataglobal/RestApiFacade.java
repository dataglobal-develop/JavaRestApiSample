package com.dataglobal;


import kong.unirest.ContentType;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This Facade is just for learning purposes
 * https://<host>/RESTfulAPI/swagger/index.html swagger API documentation
 * Using API V 1.1
 *
 */
public class RestApiFacade {

    String token ;
    String endpoint ;




    private static final Logger log = Logger.getLogger(RestApiFacade.class.getName());

    /**
     * setting the RestApi endpoint
     *
     * @param endpoint https://<host>/RESTfulAPI/csrest/v1.1/
     */
    public RestApiFacade(String endpoint)
    {
        // set unirest http client to accept unsigned ssl certs
        Unirest.config().verifySsl(false);
        this.endpoint=endpoint;


    }

    /**
     * Register a Windows User and sets the Token
     * @param user
     * @param password
     * @return true if the login was succsessfull;
     */
    public boolean login(String user, String password)  {
        final String resource="/auth/logon";


        HttpResponse<String> resp=Unirest.get(this.endpoint+resource)
                .queryString("applClass","Common")
                .queryString("progId","Standard")
                .header("X-Username",user)
                .header("X-Password",password)
                .asString();

        if (resp.getStatus()!=200)
        {
            log.log(Level.SEVERE,"can't connect "+resp.getStatus()+" "+resp.getStatusText());
            return false;
        }
        this.token=resp.getHeaders().getFirst("X-ARCHIVETOKEN");
        log.log(Level.FINE,resp.getHeaders().toString());
        return true;


    }

    /**
     * Download a document
     * @param dbID
     * @param docID
     */
    public  void queryDocument(int dbID , int docID)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * upload a single file
     * @param dbID
     * @param file file to upload
     * @return docid
     */
    public int createNewDocument(int dbID, File file) throws IOException {

        byte[] fileContent= Files.readAllBytes(file.toPath());

        return createNewDocument(dbID,file.getName(),"FILE", fileContent);
    }

    /**
     * Creating a new Document note with markdown
     * @param dbID departemntid
     * @param name document name
     * @param content content with markdown markup or text
     * @return  docid
     */

    public int createNewDocument(int dbID, String name , String content)
    {
        return createNewDocument(dbID,name,"TXT", content.getBytes());
    }

    public int createNewDocument(int dbID, String name, String format, byte[] data)
    {
        log.log(Level.FINE,"using token:"+this.token);
        String resource="/dept/{dbId}/docs";
        HttpResponse<JsonNode> resp = Unirest.post(this.endpoint+resource)
                .routeParam("dbId",String.valueOf(dbID))
                .queryString("isSapComplient","False")
                .header("accept","*/*")
                .header("X-ARCHIVETOKEN",this.token)
                .multiPartContent()

                .field("parameter",buildHypUploadJson(name,format).toString().getBytes(), ContentType.APPLICATION_JSON,"")
                .field("datafile",data,ContentType.APPLICATION_OCTET_STREAM,name)

                .asJson();

        if (resp.getStatus()!=201)
        {
            log.log(Level.SEVERE,"can't upload to "+this.endpoint+resource+" "+resp.getStatus()+" "+resp.getStatusText());
            return -1;
        }
        return resp.getBody().getObject().getInt("docId");

    }

    /**
     * List some Departements
     * @return Map <Integer, String> dbID departementName or null in case of an error
     */
    public Map<Integer, String> listDepartments()
    {
        String resouce = "/common/departments";

        HttpResponse<JsonNode> resp = Unirest.get(this.endpoint+resouce)
                .header("X-ARCHIVETOKEN",this.token)
                .asJson();

            if (resp.getStatus()!=200)
            {
                log.log(Level.SEVERE,"can't connect to "+this.endpoint+resouce+" "+resp.getStatus()+" "+resp.getStatusText());
                return null;
            }

            // convert the Json array to a java map obect
           JSONArray array=resp.getBody().getArray();
            return StreamSupport.stream(array.spliterator(),false)
                    .collect( Collectors.toMap(obj -> ((JSONObject) obj).getInt("id"), obj -> ((JSONObject) obj).getString("name")));

    }

    /**
     * index a single document
     * @param dbId
     * @param docId
     * @param indexName
     * @param indexData
     * @return
     */
    public boolean indexDocument(Integer dbId,Integer docId,String indexName,Map<String,String> indexData)
    {
        String resource = "/dept/{dbId}/docs/{docId}/indexes/{stampName}/";

        // prepare body

        JSONArray index=new JSONArray();
        indexData.forEach((k,v) -> index
                .put(new JSONObject()
                        .put("fieldName",k.toUpperCase())
                        .put("fieldValue",v)));

        log.info(index.toString(2));

        var resp = Unirest.post(this.endpoint+resource)
                .routeParam("dbId",dbId.toString())
                .routeParam("docId",docId.toString())
                .routeParam("stampName",indexName)
                .queryString("language","DE-DE")
                .header("X-ARCHIVETOKEN",this.token)
                .header("accept","*/*")
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .body(index).asEmpty();

        if (resp.getStatus()!=200)
        {
            log.log(Level.SEVERE,"docID "+docId+" "+resp.getStatusText()+" "+resp.getStatus());
        }

        return resp.getStatus()==200;
    }

    /**
     * query documents
     * @param dbID department id
     * @param query field value list whiche represents the query
     * @return a list of docIds an empty list if nothing was found
     */
    public List<Integer> queryDocuments(String departmentName, Map<String,String> query, String indexName)
    {
        String resource="/query/search";
        JSONObject queryJson=new JSONObject()
                .put("domain",departmentName.toUpperCase())
                .put("stampName",indexName)
                .put("fieldValues",
                        query.entrySet().stream()
                                .map(e -> new JSONObject()
                                        .put("fieldName",e.getKey().toUpperCase())
                                        .put("fieldValue",e.getValue())).collect(Collectors.toList()));

        log.info(queryJson.toString(2));

        var resp=Unirest.post(this.endpoint+resource)
                .header("X-ARCHIVETOKEN",this.token)
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .body(queryJson)
                .asJson();

        if (resp.getStatus()!=200)
        {
            log.log(Level.SEVERE,resp.getStatusText()+" "+resp.getStatus());
            return new ArrayList<>();
        }

        return StreamSupport.stream(resp.getBody().getObject().getJSONArray("docIdents").spliterator(),false)
                .map(e -> ((JSONObject) e).getInt("docId")).collect(Collectors.toList());




    }

    /**
     * returns an InputStream
     * @param dbID departement id
     * @param docID document id
     * @param file destination file
     */
    public void downloadContent(Integer dbID, Integer docID, File file)
    {
        String resource ="/dept/{dbId}/docs/{docId}/data";
        var resp=Unirest.get(this.endpoint+resource)
                .routeParam("dbId",dbID.toString())
                .routeParam("docId",docID.toString())
                .header("X-ARCHIVETOKEN",this.token)
                .asBytes();

        if (resp.getStatus()!=200)
        {
            log.log(Level.SEVERE,resp.getStatusText()+" "+resp.getStatus());

        }

        try {
            Files.write(file.toPath(),resp.getBody());
        } catch (IOException e) {
           log.log(Level.SEVERE,e.getMessage());
        }
    }




    private JSONObject buildHypUploadJson(String documentName,String format)
    {
        JSONObject hypMetaObj=new JSONObject();
        hypMetaObj
                .put("docName",documentName)
                .put("filename", documentName)
                .put("format", format)
        // this should be not 0 if a doctpe is used
                .put("docTypeId",0)
                .put( "docClass" ,new JSONObject().put("value","FILE").put("flag",0))
                .put("isVersionable",0);

        log.info(hypMetaObj.toString(2));
        return hypMetaObj;
    }


}
