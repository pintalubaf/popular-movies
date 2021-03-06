package com.federicopintaluba.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.federicopintaluba.popularmovies.model.Movie;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, MovieAdapter.MovieItemClickListener {

    TextView noFavoriteMovies;
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private ContentLoadingProgressBar progressBar;
    private String[] sortingOptions = {SortingOption.POPULAR, SortingOption.TOP_RATED, SortingOption.FAVORITE};
    private FavoriteMoviesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.movies_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);

        Spinner spinner = findViewById(R.id.sort_by_spinner);
        ArrayAdapter sortingOptionsArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortingOptions);
        sortingOptionsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(sortingOptionsArrayAdapter);
        spinner.setOnItemSelectedListener(this);

        noFavoriteMovies = findViewById(R.id.no_favorite_movies);
        progressBar = findViewById(R.id.progress_bar);

        viewModel = new ViewModelProvider(this,
                new FavoriteMoviesViewModelFactory(FavoriteMovieDatabase.getInstance(this)))
                .get(FavoriteMoviesViewModel.class);

        makeNetworkCall(sortingOptions[0]);
    }

    private void setUpAdapter(List<Movie> movies) {
        if (movieAdapter == null) {
            movieAdapter = new MovieAdapter(this, movies, this);
            recyclerView.setAdapter(movieAdapter);
        } else {
            movieAdapter.updateDataSource(movies);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!sortingOptions[position].equals(SortingOption.FAVORITE)) {
            if (noFavoriteMovies.getVisibility() == View.VISIBLE) {
                noFavoriteMovies.setVisibility(View.INVISIBLE);
            }

            makeNetworkCall(sortingOptions[position]);
        } else {
            viewModel.getFavoriteMovies().observe(this, new Observer<List<Movie>>() {
                @Override
                public void onChanged(List<Movie> favoriteMovies) {
                    if (favoriteMovies.isEmpty()) {
                        if (movieAdapter != null) {
                            movieAdapter.clear();
                        }

                        noFavoriteMovies.setVisibility(View.VISIBLE);
                    } else {
                        if (noFavoriteMovies.getVisibility() == View.VISIBLE) {
                            noFavoriteMovies.setVisibility(View.INVISIBLE);
                        }

                        setUpAdapter(favoriteMovies);
                    }
                }
            });
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void makeNetworkCall(String sortingOption) {
        new NetworkCall().execute(NetworkUtils.buildUrl(sortingOption.contains(SortingOption.POPULAR) ? NetworkEndpoint.MOVIE_POPULAR : NetworkEndpoint.MOVIE_TOP_RATED));
    }

    @Override
    public void onMovieItemClick(Movie movie) {
        Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
        intent.putExtra(IntentKeys.EXTRA_MOVIE, movie);
        startActivity(intent);
    }

    public class NetworkCall extends AsyncTask<URL, Void, String> {

        boolean noInternet = false;

        @Override
        protected void onPreExecute() {
            progressBar.show();
        }

        @Override
        protected String doInBackground(URL... urls) {
            if (NetworkUtils.isOnline()) {
                URL url = urls[0];

                String result = null;
                try {
                    result = NetworkUtils.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;
            } else {
                noInternet = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null && !s.isEmpty()) {
                List<Movie> movies = JsonUtils.parseMovieListJson(s);
                setUpAdapter(movies);
            }

            if (noInternet) {
                Toast.makeText(getApplicationContext(), R.string.no_connection_error, Toast.LENGTH_LONG).show();
                progressBar.hide();
            }

            progressBar.hide();
        }
    }
}
