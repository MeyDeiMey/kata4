package software.ulpgc;

import software.ulpgc.model.*;
import software.ulpgc.view.ChartGenerator;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            File currentDir = new File("").getAbsoluteFile();
            System.out.println("Directorio actual: " + currentDir.getAbsolutePath());

            File file = new File("src/main/software/ulpgc/resources/title.basics.tsv");
            System.out.println("Buscando archivo en: " + file.getAbsolutePath());
            System.out.println("¿El archivo existe? " + file.exists());
            System.out.println("¿El archivo se puede leer? " + file.canRead());

            if (!file.exists()) {
                throw new RuntimeException("El archivo no existe en la ruta especificada");
            }

            TitleReader tsvReader = new TsvTitleReader(file, true);
            List<Title> titlesFromFile = tsvReader.read();
            System.out.println("Datos leídos del archivo TSV: " + titlesFromFile.size() + " títulos");

            if (titlesFromFile.isEmpty()) {
                System.out.println("Advertencia: No se leyeron títulos del archivo");
            } else {
                System.out.println("Primer título leído: " + titlesFromFile.get(0).primaryTitle());
            }

            TitleLoader sqliteLoader = new SqliteTitleLoader("titles.db");
            sqliteLoader.save(titlesFromFile);
            System.out.println("Datos guardados en la base de datos SQLite");

            List<Title> titlesFromDB = sqliteLoader.load();
            System.out.println("Datos cargados desde SQLite: " + titlesFromDB.size() + " títulos");

            if (!titlesFromDB.isEmpty()) {
                ChartGenerator chartGenerator = new ChartGenerator(titlesFromDB);
                chartGenerator.displayCharts();
            } else {
                System.out.println("No hay datos para mostrar en los gráficos");
            }

        } catch (Exception e) {
            System.err.println("Error en la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}