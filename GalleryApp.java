package cs1302.gallery;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Random;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import cs1302.gallery.ItunesResponse;
import cs1302.gallery.ItunesResult;
import java.lang.Exception;
import java.io.IOException;
import java.lang.InterruptedException;
import javafx.event.ActionEvent;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.animation.Timeline;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.MenuBar;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javafx.scene.layout.TilePane;
import javafx.event.EventHandler;
import java.lang.Thread;
import java.lang.Runnable;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.geometry.Pos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Represents an iTunes Gallery App.
 */
public class GalleryApp extends Application {

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    public static final String ITUNES_API = "https://itunes.apple.com/search";
    //search encoder

    // SetUp
    private Stage stage;
    private Scene scene;
    private HBox root;

    private HBox hbox;
    private HBox imageHolder;
    private VBox vbox;
    private Button playButton;
    private Label search;
    private Label url;
    private TextField enterSearch;
    private ComboBox <String> category;
    private Button getImage;
    private HBox tellProgress;
    private Label infoAPI;
    private ImageView defaultImage;

    private ImageView gallery;
    private Label postRequest;
    private TilePane grid;
    private ProgressBar progressBar;
    private ItunesResponse itunesResponse;
    private Boolean canPlay;

    // make sure the term search has a + if theres spaces


    /**
     * Constructs a {@code GalleryApp} object}.
     */
    public GalleryApp() {
        this.stage = null;
        this.scene = null;
        this.root = new HBox();
    } // GalleryApp

    /** {@inheritDoc} */
    @Override

    public void init() {
        // feel free to modify this method
        hbox = new HBox(8);
        tellProgress = new HBox(8);
        vbox = new VBox(8);
        playButton = new Button("Play");
        search = new Label("Search:");
        infoAPI = new Label("Images Provided by iTunes Search API.");
        enterSearch = new TextField("daft punk");
        getImage = new Button("Get Images");
        grid = new TilePane();
        hbox.setPrefWidth(530);
        grid.setPrefHeight(410);
        grid.setPrefWidth(530);
        search.setPrefHeight(22);
        canPlay = false;
        playButton.setDisable(true);
        defaultImage = new ImageView(new Image("file:resources/default.png"));
        progressBar = new ProgressBar(0.0F);
        progressBar.setProgress(0.0);
        progressBar.setPrefWidth(250);
        for (int i = 0; i < 20; i++) {
            gallery = new ImageView();
            gallery.setFitWidth(106);
            gallery.setImage(new Image("file:resources/default.png"));
            grid.getChildren().addAll(gallery);
        }


        url = new Label("Type in a term, select a media type, then click the button.");
        EventHandler<ActionEvent> loadSomething = (e) -> {

            runThread(() -> this.imageLinks());

        };

        EventHandler<ActionEvent> switchImages = (e) -> {
            runThread(() -> this.imageChange());
        };
        getImage.setOnAction(loadSomething);
        playButton.setOnAction(switchImages);
        category = new ComboBox<String>();
        String [] list = {"movie", "podcast", "music", "musicVideo", "audiobook",
            "shortFilm", "tvShow", "software", "ebook", "all"};
        category.setValue("music");
        category.getItems().addAll(list);

        hbox.getChildren().addAll(playButton, search, enterSearch, category, getImage);
        tellProgress.getChildren().addAll(progressBar, infoAPI);
        vbox.getChildren().addAll(hbox, url, grid, tellProgress);
        getImage.setOnAction(loadSomething);
        root.getChildren().add(vbox);

        System.out.println("init() called");
    } // init

    /**
     * This method losds the images.
     */
    public void  imageLinks () {
        Platform.runLater(() -> url.setText("Getting images..."));
        progressBar.setProgress(0.0);
        double f = 0.1;
        EventHandler<ActionEvent> handler = event -> {
            setProgress(f); // increases the progress bar per frame
            return;
        };
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), handler); //each frame takes 1s
        Timeline timeline = new Timeline();
        timeline.setCycleCount(10); // frames are done 10 times
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
        String newUri = ""; // solves issues with alertCall method
        try {
            // form URI
            String term = URLEncoder.encode(enterSearch.getText(), StandardCharsets.UTF_8);
            String media = URLEncoder.encode(category.getValue(), StandardCharsets.UTF_8);
            String limit = URLEncoder.encode("200", StandardCharsets.UTF_8);
            String query = String.format("?term=%s&media=%s&limit=%s", term, media, limit);
            String uri = ITUNES_API + query;
            newUri = uri;
            HttpRequest request = HttpRequest.newBuilder() // build request
                .uri(URI.create(uri))
                .build();
            HttpResponse<String> response = HTTP_CLIENT //receive response in the form of a String
                .send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) { //ensure the reques is okay
                throw new IOException(response.toString());
            } // if
            String jsonString = response.body();// get request body (the content we requested)
            // parse the JSON-formatted string using GSON
            itunesResponse = GSON
                .fromJson(jsonString, ItunesResponse.class);
            // print info about the response
            exceptionthrow();// method that throws exceptions for alerts
            playButton.setDisable(false);
            try {
                Thread.sleep(11000); //sets the delay for images
            } catch (InterruptedException ie) {
                System.err.println(ie);
            }
            Platform.runLater(() -> grid.getChildren().clear());
            final String[] onlyOne = new String[20];
            grid.setPrefColumns(5);
            grid.setPrefRows(4);
            int i = 0;
            int index = 0;
            Platform.runLater(() ->
                upload(i, index, onlyOne, gallery, grid, itunesResponse));// loophole for lambda
            Platform.runLater(() -> url.setText(uri));
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            // 1. an I/O error occurred when sending or receiving;
            // 2. the operation was interrupted; or 3. the Image class could not load the image.
            Platform.runLater(() -> url.setText("Last attempt to get image failed..."));
            alertCall(newUri, e);
            progressBar.setProgress(1);
        } // try
        canPlay = true;//you can shuffle the images now!
    } //loadImages

    /**
     * This method makes alerts.
     * @param  s
     * @param  e
     */
    private void alertCall(final String s, final Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert( AlertType.ERROR, "URI:" + s + "\n" + e.getMessage());//display
            alert.setWidth(600);
            alert.setHeight(250);
            alert.show();
            return;
        });

    }

     /**
     * This method checks if the reault image is unique an at least 21
     * unique images or it throws an exception.
     */

    private void exceptionthrow() {
        //less than 20 images error
        int count = 0;
        int count2 = 0;
        String [] emp = new String[21];
        for (int start = 0, comp = 0; start < itunesResponse.results.length && comp < 21;
             start++) {
            count2 = 0;
            for (int next = 0; next < emp.length; next++) {
                if (!itunesResponse.results[start].artworkUrl100.equals(emp[next])) {
                    count2++;
                }

            }
            if (count2 == 21) {
                count++;
                emp[comp] = itunesResponse.results[start].artworkUrl100;
                comp++;

            }
        }

        if (count < 21) {
            throw new IllegalArgumentException("\n\nException: java.lang.IllegalArgumentException: "
            + count +  " distinct results found, but 21 or more needed.");
        }

    }

    /**
     * This method handles the issuess with local varables with lambda issues.
     * It also makes sure that all images don't form too many duplicates.
     * @param i
     * @param index
     * @param onlyOne
     * @param gallery
     * @param grid
     * @param itunesResponse
     */
    public void upload(int i, int index, String[] onlyOne, ImageView gallery, TilePane grid,
        ItunesResponse itunesResponse) {
        for (int j = 0; j < itunesResponse.results.length && index < 20; j++) {

            ItunesResult result = itunesResponse.results[j];
            i = 0;
            for (int k = 0; k < onlyOne.length; k++) {
                if (!result.artworkUrl100.equals(onlyOne[k])) {
                    i++;
                }
            }
            if (i == 20) {
                onlyOne[index] = result.artworkUrl100;
                gallery = new ImageView();
                gallery.setFitWidth(106);
                gallery.setImage(new Image(onlyOne[index]));
                grid.getChildren().addAll(gallery);
                index++;
            }

        }


    } //upload

    /**
     * This method handles the progress bar.
     * @param progress
     */
    private void setProgress(final double progress) {
        progressBar.setProgress(progress + this.progressBar.getProgress());

    } // setProgress

    /**
     * This method changes the images when you hit play.
     * It functions the same as the imageLink method but get a random int value
     * to change the images until pressed again.
     */
    public void  imageChange () {

        Timeline timeline = new Timeline();
        if (playButton.getText().equals("Play") && canPlay) {
            Platform.runLater(() -> playButton.setText("Pause"));
            EventHandler<ActionEvent> handler = event -> {
                Random random = new Random();
                int firstRand = random.nextInt(19);
                int secRand = random.nextInt(itunesResponse.results.length);
                ImageView image = new ImageView(itunesResponse.results[secRand].artworkUrl100);
                image.setFitWidth(106);
                //image.setFitHeight(120);
                Platform.runLater(() ->
                    grid.getChildren().set(firstRand, image));
                if (playButton.getText().equals("Play")) {
                    timeline.stop();

                }
                return;
            };
            KeyFrame keyframe = new KeyFrame(Duration.seconds(2), handler);

            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.getKeyFrames().add(keyframe);
            timeline.play();

        } else {
            Platform.runLater(() -> playButton.setText("Play"));
            timeline.stop();
        }


    } //imageChange


    /**
     * this greats a thread and runs it.
     * @param runner
     */
    public static void runThread (Runnable runner) {
        Thread thread = new Thread(runner);

        thread.setDaemon(true);
        thread.start();

    } // runThread


    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(this.root,530,480);
        this.scene.setFill(Color.BLACK);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.setTitle("GalleryApp!");
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    /** {@inheritDoc} */
    @Override
    public void stop() {
        // feel free to modify this method
        System.out.println("stop() called");
    } // stop

} // GalleryApp
