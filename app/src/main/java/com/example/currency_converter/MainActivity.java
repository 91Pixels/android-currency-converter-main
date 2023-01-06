package com.example.currency_converter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_INTERNET = 1;
    private static String[] PERMISSIONS_INTERNET = {
        Manifest.permission.INTERNET
    };
    // conexión a base de datos
    AdminDatabase sqliteDbJgh = new AdminDatabase(MainActivity.this, "Administration", null, 1);


    String[] currenciesFromInternetJgh;
    String[] ratesFromInternetJgh;

    private Spinner fromCurrencyJgh;
    private Spinner toCurrencyJgh;
    private EditText inputAmountJgh;
    private TextView outputAmountJgh;
    private Button conversionBtnJgh;
    private Button resetBtnJgh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // comprobar conexion internet, si tenemos conexion descargamos datos, si no, se extraen de bbdd
        if (checkConnection()) {
            // crear objeto downloader para descargar divisas y ratios de ocnversion
            DataDownloader dowlonaderJgh = new DataDownloader("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
            dowlonaderJgh.download();

            // inicializar los arrays de divisas y de ratios de conversion
            currenciesFromInternetJgh = new String[dowlonaderJgh.currenciesJgh.size()];
            currenciesFromInternetJgh = dowlonaderJgh.currenciesJgh.toArray(currenciesFromInternetJgh);
            ratesFromInternetJgh = new String[dowlonaderJgh.ratesJgh.size()];
            ratesFromInternetJgh = dowlonaderJgh.ratesJgh.toArray(ratesFromInternetJgh);

            try {
                // borrar datos previos de bbdd y añadir los nuevos bajados de internet
                DatabaseMaster dataMasterJgh = new DatabaseMaster(sqliteDbJgh);
                dataMasterJgh.wipeData();
                dataMasterJgh.insertData(currenciesFromInternetJgh, ratesFromInternetJgh);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // extraer datos de bbdd
            DatabaseMaster dataMasterJgh = new DatabaseMaster(sqliteDbJgh);
            dataMasterJgh.retrieveData();
            currenciesFromInternetJgh = new String[dataMasterJgh.currenciesFromDbJgh.size()];
            currenciesFromInternetJgh = dataMasterJgh.currenciesFromDbJgh.toArray(currenciesFromInternetJgh);
            ratesFromInternetJgh = new String[dataMasterJgh.ratesFromDbJgh.size()];
            ratesFromInternetJgh = dataMasterJgh.ratesFromDbJgh.toArray(ratesFromInternetJgh);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // solicitar permisos de internet por java
        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_INTERNET, REQUEST_EXTERNAL_INTERNET);
        /* Inicializar los campos de texto y el boton de conversión apuntando al
        elemento correspondiente en el activity_main
         */
        inputAmountJgh = (EditText) findViewById(R.id.inputAmount);
        outputAmountJgh = (TextView) findViewById(R.id.outputAmountTxtView);
        conversionBtnJgh = (Button) findViewById(R.id.converseButton);
        resetBtnJgh = (Button) findViewById(R.id.resetButton);

        /* Inicializar los spinner con las divisas  apuntando al spinner correspondiente en
        activity_main
         */
        fromCurrencyJgh = (Spinner) findViewById(R.id.fromCurrencySpinner);
        toCurrencyJgh = (Spinner) findViewById(R.id.toCurrencySpinner);

        // creación de un adapter para rellenar el spinner con los datos de divisas.
        ArrayAdapter<String> adapterJgh = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, currenciesFromInternetJgh);
        fromCurrencyJgh.setAdapter(adapterJgh);
        toCurrencyJgh.setAdapter(adapterJgh);


    }

    public void makeConversionOnClick(View view) {
        String inputValueJgh = String.valueOf(inputAmountJgh.getText());

        // control para evitar crash de la app en caso de click en convertir sin ningun input
        if (inputValueJgh == null || inputValueJgh == "" || inputValueJgh.isEmpty()) {
            displayToaster("Please provide an amount to convert");
            return;
        }

        // recoger los datos que se hayan dado como input
        double initialAmountJgh = Double.parseDouble(inputValueJgh);
        String initialCurrencyJgh = String.valueOf(fromCurrencyJgh.getSelectedItem());
        String finalCurrencyJgh = String.valueOf(toCurrencyJgh.getSelectedItem());

        // realizar conversion
        double amountInEurosJgh = makeConversionToEuro(initialAmountJgh, initialCurrencyJgh);
        double amountInFinalCurrencyJgh = makeConversionToFinalCurrency(amountInEurosJgh, finalCurrencyJgh);

        // redondear el resultado a dos decimales
        DecimalFormat df = new DecimalFormat("###.###");

        // mostrar el resultado por pantalla
        outputAmountJgh.setText(df.format(amountInFinalCurrencyJgh));

    }

    public double makeConversionToEuro(double amountJgh, String initialCurrencyJgh) {
        int currencyIndexJgh = getEquivalenceIndex(initialCurrencyJgh);
        double conversionRateJgh = Double.parseDouble(ratesFromInternetJgh[currencyIndexJgh]);
        return conversionRateJgh * amountJgh;
    }

    public double makeConversionToFinalCurrency(double amountEurosJgh, String finalCurrencyJgh){
        int currencyIndexJgh = getEquivalenceIndex(finalCurrencyJgh);
        double conversionRateJgh = Double.parseDouble(ratesFromInternetJgh[currencyIndexJgh]);
        return amountEurosJgh/conversionRateJgh;
    }

    public int getEquivalenceIndex(String currencyJgh) {
        return Arrays.asList(currenciesFromInternetJgh).indexOf(currencyJgh);
    }

    public void resetAll(View view) {
        String input = String.valueOf(inputAmountJgh.getText());
        String output = String .valueOf(outputAmountJgh.getText());

        // no hacer nada si no hay input
        if ((input.isEmpty() || input.equals("") || input.equals(null)) && (output.isEmpty() || output.equals("")) || output.equals(null)) {
            return;
        }

        // vaciar el EditText y el textView
        inputAmountJgh.setText("");
        outputAmountJgh.setText("");
        displayToaster("Data reset");
    }

    public void displayToaster(String messageJgh) {
        Toast toaster;
        toaster = Toast.makeText(getApplicationContext(), messageJgh, Toast.LENGTH_SHORT);
        toaster.setGravity(Gravity.TOP, 0, 900);
        toaster.show();
    }



    public boolean checkConnection() {
        boolean connection = false;
        ConnectivityManager conManagerJgh = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkJgh = conManagerJgh.getAllNetworkInfo();
        for (NetworkInfo niJgh : networkJgh) {
            // consideramos al dispositivo conectado si algun elemento tiene conexión
            if (niJgh.getState() == NetworkInfo.State.CONNECTED) {
                connection = true;
                break;
            }
        }
        return connection;
    }





}