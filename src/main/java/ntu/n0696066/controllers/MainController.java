package ntu.n0696066.controllers;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import Shares.CurrentShares;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import okhttp3.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class MainController {


    @FXML
    private void switchToSecondary() throws IOException {
        CurrentShares tempShare = new CurrentShares();
        CurrentShares.SharePrice tempPrice = new CurrentShares.SharePrice();

        tempPrice.setCurrency("GBR");
        tempPrice.setValue(BigDecimal.valueOf(1800.8));

        tempShare.setCompanyName("Amazon");
        tempShare.setCompanySymbol("AMZN");
        tempShare.setSharesAmount(BigInteger.valueOf(2000));
        tempShare.setSharePrice(tempPrice);
        backgroundThread task = new backgroundThread(tempShare);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        // App.setRoot("secondary");
    }

    class backgroundThread extends Task<Void> {

        CurrentShares share;
        MediaType mediaType;
        OkHttpClient client;
        JAXBContext context;
        Marshaller m;

        backgroundThread(CurrentShares share){
            try {
                this.share = share;
                client = new OkHttpClient();
                context = JAXBContext.newInstance(CurrentShares.class);
                m = context.createMarshaller();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        /**
         * Makes a POST call to the webservice to purchase a stock item using the inserted CurrentShare Object
         * @throws Exception
         */
        public void purchaseStock() throws Exception {
            mediaType = MediaType.parse("text/xml");
            StringWriter xmlString = new StringWriter();
            m.marshal(share, xmlString);

            RequestBody body = RequestBody.create(xmlString.toString(), mediaType);
            Request request = new Request.Builder()
                    .url("http://localhost:8080/purchasestock")
                    .post(body)
                    .addHeader("content-type", "text/xml")
                    .build();
            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                System.out.println(response.body().string());
            }
        }

        @Override
        protected Void call() throws Exception {
            purchaseStock();
            return null;
        }
    }
}
