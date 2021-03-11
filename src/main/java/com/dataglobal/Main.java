package com.dataglobal;

import kong.unirest.json.JSONObject;
import net.steppschuh.markdowngenerator.table.Table;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Main {
    private static Logger log = null;

    static {
        InputStream stream = Main.class.getClassLoader().
                getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            log= Logger.getLogger(Main.class.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }



    }



    public static void main(String[] args) throws  IOException {


        final String endpoint = "https://dataglobal-cs/RESTfulAPI/csrest/v1.1";
        final String user = "Administrator";
        final String pwd = "xxxxxx";
        final int departemetnID = 1571768983;
        final String departementName="CONTENT";
        final String indexName="MYINDEX";
        final String documentToUpload="..../../sample.jpg";

        // how to login
        log.info("Start ");
        RestApiFacade facade = new RestApiFacade("https://dataglobal-cs/RESTfulAPI/csrest/v1.1");
        facade.login(user,pwd);

        // how to list departments
        facade.listDepartments().forEach((k,v) -> log.info("dbID:"+k+" name:"+v));

        // how to upload Documents
        int docID=facade.createNewDocument(departemetnID,new File(documentToUpload));
        log.info("docid:"+docID);

        // how to index a single document
        Map<String,String> index=new HashMap<>();
        index.put("NAME","hello world");
        facade.indexDocument(departemetnID,docID,indexName,index);


        // how to query documents
        facade.queryDocuments(departementName,index,indexName)
                .forEach(e -> log.info("docId"+e));


        // how to create mark down documents
        Table.Builder tableBuilder = new Table.Builder().addRow("Column 1","column 2");
        for (int i=0;i<=10;i++)
        {
            tableBuilder.addRow(i,"This is row nr.:"+i);
        }
        docID = facade.createNewDocument(departemetnID,"my_new_md_doc.txt",tableBuilder.build().toString());
        log.info("upload successfull "+docID);


    }
}
