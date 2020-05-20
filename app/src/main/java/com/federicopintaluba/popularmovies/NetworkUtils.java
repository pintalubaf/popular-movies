package com.federicopintaluba.popularmovies;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

class NetworkUtils {

    private final static String BASE_URL = "https://api.themoviedb.org/3";
    private final static String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w185";
    private final static String API_KEY = "";
    private final static String PARAM_API_KEY = "api_key";

    static URL buildUrl(String endpoint) {
        Uri builtUri = Uri.parse(BASE_URL + endpoint).buildUpon()
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    static String buildMoviePosterPath(String originalPosterPath) {
        return BASE_IMAGE_URL + originalPosterPath;
    }

    static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
