package com.example.currency_converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DataDownloader {
    boolean dataDownloadFinishedJgh = false;
    String urlJgh;
    ArrayList<String> currenciesJgh = new ArrayList<String>();
    ArrayList<String> ratesJgh = new ArrayList<String>();

    public DataDownloader(String url) {
        this.urlJgh = url;
    }

    public void download() {

        Thread downloadData = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    DocumentBuilderFactory docFactoryJgh = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilderJgh = docFactoryJgh.newDocumentBuilder();
                    URL URIJgh = new URL(urlJgh);
                    Document document = docBuilderJgh.parse(URIJgh.openStream());
                    NodeList cubeListJgh = (NodeList) document.getElementsByTagName("Cube");
                    for (int i = 0; i < cubeListJgh.getLength(); i++) {
                        Element elementoDivisaJgh = (Element) cubeListJgh.item(i);
                        if (elementoDivisaJgh.hasAttribute("currency")) {
                            String elementCurrencyJgh = (String) elementoDivisaJgh.getAttribute("currency");
                            String elementRateJgh =(String) elementoDivisaJgh.getAttribute("rate");
                            // añadir divisa y ratio de conversion a los arrayLists
                            currenciesJgh.add(elementCurrencyJgh);
                            ratesJgh.add(elementRateJgh);
                        }
                    }

                    dataDownloadFinishedJgh = true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        downloadData.start();
        // esperar a que termine de descargar datos para continuar
        try{
            while (dataDownloadFinishedJgh == false) {
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
