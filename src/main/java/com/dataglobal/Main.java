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




        log.info("Connect ");

        RestApiFacade facade= new RestApiFacade("https://dataglobal-cs/RESTfulAPI/csrest/v1.1");


        Map<String,String> index= new HashMap<>();
        index.put("NAME","SAMPLE");

        facade.login("Administrator","Data230Global");



        facade.listDepartments().forEach((k,v)-> log.info(k+" :: "+v));

        Table.Builder tableBuilder = new Table.Builder().addRow("Column 1","column 2");
        for (int i=0;i<=10;i++)
        {
            tableBuilder.addRow(i,"This is row nr.:"+i);
        }
        int docID = facade.createNewDocument(1571768983,"my_new_md_doc.txt",tableBuilder.build().toString());
        log.info("upload successfull "+docID);

        int docID2=facade.createNewDocument(1571768983,new File("C:\\tmp\\sample.jpg"));


        facade.indexDocument(1571768983,docID,"BAUAKTE",index);
        facade.queryDocuments("CONTENT",index,"BAUAKTE").forEach(e-> log.info("docid:"+e));

        facade.downloadContent(1571768983, docID2,new File("test.jpg"));

    }
}
