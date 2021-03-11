# Purpose

This code sample should simply demonstrate how to use the dataglobal RestApi
in Java by example.

# Getting started
## prerequisite

* Installing the ResApiSDK from dataglobal www.dataglobal.de
* Prepare a Single department and a Index that is accessible
* adjust the main method

```java
final String endpoint = "https://dataglobal-cs/RESTfulAPI/csrest/v1.1";
final String user = "Administrator";
final String pwd = "xxxxxx";
final int departemetnID = 1571768983;
final String departementName="CONTENT";
final String indexName="MYINDEX";
final String documentToUpload="..../../sample.jpg";
```

## compile and run

Just run maven by 
```shell
> mvn install

>java -jar ./target/*.jar 

```

##Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Twitter :
@mrebahls
<br>
Mail : enrico.bahls@dataglobal.com


## License
[MIT](https://choosealicense.com/licenses/mit/)

