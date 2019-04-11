package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class Controller implements Initializable {

    @FXML TableColumn<ImageProcessingJob, String> imageNameColumn;
    @FXML TableColumn<ImageProcessingJob, Double> progressColumn;
    @FXML TableColumn<ImageProcessingJob, String> statusColumn;
    @FXML private Label uploadingTime;
    @FXML private Label thread;
    @FXML ChoiceBox<String> threadNumber;

    @FXML
    TableView tableView;
    List<ImageProcessingJob> jobs;
    File target = null;
    int chosenOption = 1;
    //ExecutorService executor = Executors.newSingleThreadExecutor();
    ExecutorService executor;
    ForkJoinPool common; //pożądana liczba wątków
    ExecutorService pool;

    @FXML
    void processFiles(ActionEvent event) {
        long start = System.currentTimeMillis();
        if(chosenOption == 1) executor = Executors.newSingleThreadExecutor();
        else if(chosenOption == 2) common = new ForkJoinPool();
        else if(chosenOption == 3) {
            if(threadNumber.getValue() == "2 wątki")
                pool = Executors.newFixedThreadPool(2);
            else if(threadNumber.getValue() == "4 wątki")
                pool = Executors.newFixedThreadPool(4);
            else if(threadNumber.getValue() == "8 wątków")
                pool = Executors.newFixedThreadPool(8);
        }
         //zwraca aktualny czas [ms]
       /* for (ImageProcessingJob job: jobs) {
            this.tableView.getItems().add(job);
        }*/

        for (ImageProcessingJob job:jobs) {
            job.target = target;
            if(chosenOption == 1) executor.submit(job);
            else if(chosenOption == 2) common.submit(job);
            else if(chosenOption == 3) pool.submit(job);
        }
        long end = System.currentTimeMillis(); //czas po zakończeniu operacji [ms]
        long duration = end-start; //czas przetwarzania [ms]
        uploadingTime.setText("Przetworzono w: " + duration + "ms");

    }
    //metoda uruchamiana w tle (w tej samej klasie)
   /* private void backgroundJob(){
    //...operacje w tle...
        //jobs.stream().forEach(...);
    }*/


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        imageNameColumn.setCellValueFactory( //nazwa pliku
                p -> new SimpleStringProperty(p.getValue().getFile().getName()));
        statusColumn.setCellValueFactory( //status przetwarzania
                p -> p.getValue().getStatusProperty());
        progressColumn.setCellFactory( //wykorzystanie paska postępu
                ProgressBarTableCell.<ImageProcessingJob>forTableColumn());
        progressColumn.setCellValueFactory( //postęp przetwarzania
                p -> p.getValue().getProgressProperty().asObject());


        jobs = new ArrayList<>();

        threadNumber.getItems().addAll("2 wątki", "4 wątki", "8 wątków");


//...dalsze inicjalizacje...
    }

    @FXML
    public void selectFiles(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JPG images", "*.jpg"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

        for (File image:selectedFiles) {
            ImageProcessingJob img = new ImageProcessingJob(image);
            jobs.add(img);
            this.tableView.getItems().add(img);
        }


    }

    @FXML
    public void selectTarget(ActionEvent event){
        DirectoryChooser directoryChooser = new DirectoryChooser();

        target = directoryChooser.showDialog(null);

    }

    @FXML
    public void single(ActionEvent event){
        chosenOption = 1;
        thread.setText("Aktualnie wybrane przetwarzanie: sekwencyjne");
    }

    @FXML
    public void common(ActionEvent event){
        chosenOption = 2;
        thread.setText("Aktualnie wybrane przetwarzanie: CommonPool");
    }

    @FXML
    public void join(ActionEvent event){
        chosenOption = 3;
        thread.setText("Aktualnie wybrane przetwarzanie: JoinPool");
    }
}
