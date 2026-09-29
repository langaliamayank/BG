package com.androidtv.bhagavadgita.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIInterface {

    String CDN_BASE_URL = "https://cdn.jsdelivr.net/gh/langaliamayank/BG@master/database/";
    String API_BASE_URL = "https://raw.githubusercontent.com/langaliamayank/BG/refs/heads/master/database/";
    public static String DARSHAN_IMAGE_BASE_PATH = API_BASE_URL + "shrinathji/posters/portrait_compress/";
    public static String DARSHAN_IMAGE_LAND_BASE_PATH = API_BASE_URL + "shrinathji/posters/landscape_compress/";
    public static String OTHER_IMAGE_BASE_PATH = API_BASE_URL + "shrinathji/posters/other_compress/";
    public static String CHAPTER_IMAGE_BASE_PATH = API_BASE_URL + "bg_posters/old/";
    public static String CHAPTER_IMAGE_NUMBER_BASE_PATH = API_BASE_URL + "bg_posters/old/chapter_";
    public static String VERSES_BASE_PATH = API_BASE_URL + "verse_recitation/";

    @Headers("Accept: application/json")
    @GET(API_BASE_URL + "chapters.json")
    public Call<ResponseBody> getChapters();

    @Headers("Accept: application/json")
    @GET(API_BASE_URL + "verse.json")
    public Call<ResponseBody> getVerses();

    @Headers("Accept: application/json")
    @GET(API_BASE_URL + "translation.json")
    public Call<ResponseBody> getTranslation();

    @Headers("Accept: application/json")
    @GET(API_BASE_URL + "commentary.json")
    public Call<ResponseBody> getCommentary();

    /*------------------------------------------*/

    @Headers("Accept: application/json")
    @GET(API_BASE_URL + "shrinathji/darshan.json")
    public Call<ResponseBody> getDarshan();

    @Headers("Accept: application/json")
    @GET(API_BASE_URL + "shrinathji/history.json")
    public Call<ResponseBody> getHistory();

    @Headers("Accept: application/json")
    @GET(API_BASE_URL + "shrinathji/pushtimarg.json")
    public Call<ResponseBody> getPushtimarg();

    @Headers("Accept: application/json")
    @GET(API_BASE_URL + "shrinathji/vallabhacharya.json")
    public Call<ResponseBody> getVallabhacharya();

    /*------------------------------------------*/
    String BASE_URL = "https://saavn.sumit.co/api/";
    String SEARCH_URL = BASE_URL + "search";
    String SONGS = "/songs";
    String ALBUMS = "/albums";
    String ARTISTS = "/artists";
    String PLAYLISTS = "/playlists";
    String SONGS_URL = BASE_URL + "songs";
    String ALBUMS_URL = BASE_URL + "albums";
    String ARTISTS_URL = BASE_URL + "artists";
    String PLAYLISTS_URL = BASE_URL + "playlists";

    @Headers("Accept: application/json")
    @GET(SEARCH_URL + SONGS)
    public Call<ResponseBody> getPopularSongAPI(
            @Query("query") String query,
            @Query("page") Integer page,
            @Query("limit") Integer limit);

    @Headers("Accept: application/json")
    @GET(SEARCH_URL + ARTISTS)
    public Call<ResponseBody> getPopularArtistAPI(
            @Query("query") String query,
            @Query("page") Integer page,
            @Query("limit") Integer limit);

    @Headers("Accept: application/json")
    @GET(SEARCH_URL + ALBUMS)
    public Call<ResponseBody> getPopularAlbumsAPI(
            @Query("query") String query,
            @Query("page") Integer page,
            @Query("limit") Integer limit);

    @Headers("Accept: application/json")
    @GET(SEARCH_URL + PLAYLISTS)
    public Call<ResponseBody> getPopularPlaylistAPI(
            @Query("query") String query,
            @Query("page") Integer page,
            @Query("limit") Integer limit);

    @Headers("Accept: application/json")
    @GET(PLAYLISTS_URL)
    public Call<ResponseBody> retrievePlaylistById(
            @Query("id") String id, /*albumItem.id()*/
            @Query("page") Integer page,
            @Query("limit") Integer limit);

    @Headers("Accept: application/json")
    @GET(ALBUMS_URL)
    public Call<ResponseBody> retrieveAlbumById(
            @Query("id") String id /*albumItem.id()*/);

    @Headers("Accept: application/json")
    @GET(ARTISTS_URL + "/{id}/songs")
    public Call<ResponseBody> retrieveArtistSongs(
            @Path("id") String id, /*artist.id()*/
            @Query("page") Integer page,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder);
}
