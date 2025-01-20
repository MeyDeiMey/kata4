package software.ulpgc.model;

import java.time.Duration;
import java.time.Year;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class TsvTitleDeserializer implements TitleDeserializer {
    @Override
    public Optional<Title> deserialize(String line) {
        String[] columns = line.split("\t");

        if (columns.length < 9) {
            System.err.println("Línea malformada (columnas insuficientes): " + line);
            return Optional.empty();
        }

        try {
            return Optional.of(new Title(
                    columns[0], // tconst
                    Title.TitleType.valueOf(columns[1].toUpperCase()), // titleType
                    columns[2], // primaryTitle
                    columns[3], // originalTitle
                    columns[4].equals("1"), // isAdult (true/false)
                    parseYear(columns[5]), // startYear
                    parseYear(columns[6]), // endYear
                    parseDuration(columns[7]), // runtimeMinutes (ahora es Duration)
                    Arrays.stream(columns[8].split(","))
                            .map(String::trim)
                            .map(this::parseGenre)  // Usamos método auxiliar para evitar problemas con géneros
                            .collect(Collectors.toList()) // Géneros
            ));
        } catch (Exception e) {
            System.err.println("Error al deserializar la línea: " + line);
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<Year> parseYear(String column) {
        if (column.equals("\\N")) {
            return Optional.empty();
        }
        try {
            return Optional.of(Year.parse(column));
        } catch (Exception e) {
            System.err.println("Error al parsear el año: " + column);
            return Optional.empty();
        }
    }

    private Optional<Duration> parseDuration(String column) {
        if (column.equals("\\N") || column.isEmpty()) {
            return Optional.empty();
        }
        try {
            // Convertimos minutos a duración (en minutos)
            return Optional.of(Duration.ofMinutes(Long.parseLong(column)));
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear el runtime: " + column);
            return Optional.empty();
        }
    }

    private Title.Genre parseGenre(String genre) {
        try {
            return Title.Genre.valueOf(genre.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Error al parsear género desconocido: " + genre);
            return Title.Genre.valueOf(null); // Asignar un valor de respaldo si no se encuentra un género
        }
    }
}
