package com.openclassrooms.realestatemanager.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Philippe on 21/02/2018.
 */

public class Utils {

    /**
     * Conversion d'un prix d'un bien immobilier (Dollars vers Euros)
     * NOTE : NE PAS SUPPRIMER, A MONTRER DURANT LA SOUTENANCE
     * @param dollars
     * @return euros
     */
    public static int convertDollarToEuro(int dollars){
        return (int) Math.round(dollars / Constants.CONVERSION_RATE_EUROS_DOLLARS);
    }

    /**
     * Conversion d'un prix d'un bien immobilier (Euros vers Dollars )
     * NOTE : NE PAS SUPPRIMER, A MONTRER DURANT LA SOUTENANCE
     * @param euros
     * @return dollar
     */
    public static int convertEuroToDollar(int euros) {
        return (int) Math.round(euros * Constants.CONVERSION_RATE_EUROS_DOLLARS);
    }

    /**
     * Conversion de la date d'aujourd'hui en un format plus approprié
     * NOTE : NE PAS SUPPRIMER, A MONTRER DURANT LA SOUTENANCE
     * @return String
     */
    public static String getTodayDate(){
        return formatDate(new Date());
    }

    public static String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static Date fromStringToDate(String string) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return dateFormat.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Vérification de la connexion réseau
     * NOTE : NE PAS SUPPRIMER, A MONTRER DURANT LA SOUTENANCE
     * @return boolean
     */
    public synchronized static Boolean isInternetAvailable(){
//        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
//        return wifi.isWifiEnabled();
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("8.8.8.8", 53), Constants.TIMEOUT_INTERNET_CONNECTION);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
