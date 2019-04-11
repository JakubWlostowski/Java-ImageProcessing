package sample;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageProcessingJob extends Task<Void>{
    File file;
    File target;
    SimpleStringProperty status;
    DoubleProperty progress;
    SimpleStringProperty time;

    public ImageProcessingJob(File fileUpl){
        file = fileUpl;
        target = null;
        progress = new SimpleDoubleProperty();
        status = new SimpleStringProperty();
        time = new SimpleStringProperty();
        status.set("Oczekuję");
    }

    public File getFile(){
        return this.file;
    }

    public SimpleStringProperty getStatusProperty(){
        return this.status;
    }

    public DoubleProperty getProgressProperty(){
        return this.progress;
    }

    @Override
    protected Void call(){

        status.set("Konwertuję...");
        try {
            //wczytanie oryginalnego pliku do pamięci
            BufferedImage original = ImageIO.read(file);


            //przygotowanie bufora na grafikę w skali szarości
            BufferedImage grayscale = new BufferedImage(
                    original.getWidth(), original.getHeight(), original.getType());
            //przetwarzanie piksel po pikselu
            for (int i = 0; i < original.getWidth(); i++) {
                for (int j = 0; j < original.getHeight(); j++) {
                    //pobranie składowych RGB
                    int red = new Color(original.getRGB(i, j)).getRed();
                    int green = new Color(original.getRGB(i, j)).getGreen();
                    int blue = new Color(original.getRGB(i, j)).getBlue();
                    //obliczenie jasności piksela dla obrazu w skali szarości
                    int luminosity = (int) (0.21*red + 0.71*green + 0.07*blue);
                    //przygotowanie wartości koloru w oparciu o obliczoną jaskość
                    int newPixel =
                            new Color(luminosity, luminosity, luminosity).getRGB();
                    //zapisanie nowego piksela w buforze
                    grayscale.setRGB(i, j, newPixel);
                }
                //obliczenie postępu przetwarzania jako liczby z przedziału [0, 1]
                double progress2 = (1.0 + i) / original.getWidth();
                //aktualizacja własności zbindowanej z paskiem postępu w tabeli
                Platform.runLater(() -> progress.set(progress2));
            }
            //przygotowanie ścieżki wskazującej na plik wynikowy
            Path outputPath =
                    Paths.get(target.getAbsolutePath(), file.getName());

            //zapisanie zawartości bufora do pliku na dysku
            ImageIO.write(grayscale, "jpg", outputPath.toFile());


        } catch (IOException ex) {
            //translacja wyjątku
            status.set("Skonczone");
            throw new RuntimeException(ex);
        }
        status.set("Skonczone");
        return null;
    }

}
