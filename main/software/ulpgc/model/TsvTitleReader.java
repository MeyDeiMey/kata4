package software.ulpgc.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TsvTitleReader implements TitleReader {

    private static final Logger logger = Logger.getLogger(TsvTitleReader.class.getName());

    private final File file;
    private final boolean hasHeader;

    public TsvTitleReader(File file, boolean hasHeader) {
        this.file = file;
        this.hasHeader = hasHeader;
    }

    @Override
    public List<Title> read() throws IOException {
        if (!file.exists()) {
            throw new IOException("El archivo no existe: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IOException("El archivo no se puede leer: " + file.getAbsolutePath());
        }

        List<Title> titles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Leer cabecera si es necesario
            if (hasHeader) {
                String headerLine = reader.readLine();
                logger.info("Cabecera del archivo: " + headerLine);
            }

            // Procesar líneas del archivo
            String line;
            int lineNumber = hasHeader ? 2 : 1; // Ajustar el número según si hay cabecera
            while ((line = reader.readLine()) != null) {
                try {
                    logger.info("Leyendo línea " + lineNumber + ": " + line.substring(0, Math.min(line.length(), 50)) + "...");
                    Optional<Title> titleOpt = new TsvTitleDeserializer().deserialize(line);
                    if (titleOpt.isPresent()) {
                        titles.add(titleOpt.get());
                    } else {
                        logger.warning("Error al deserializar la línea " + lineNumber + ": " + line);
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Excepción al procesar la línea " + lineNumber + ": " + line, ex);
                }
                lineNumber++;
            }

            logger.info("Total de líneas procesadas correctamente: " + titles.size());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al leer el archivo TSV: " + file.getAbsolutePath(), e);
            throw e; // Repropagar la excepción
        }
        return titles;
    }
}
