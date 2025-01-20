package software.ulpgc.view;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import software.ulpgc.model.Title;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartGenerator {
    private final List<Title> titles;

    public ChartGenerator(List<Title> titles) {
        this.titles = titles;
    }

    public void displayCharts() {
        JFrame frame = new JFrame("Movie Statistics");
        frame.setLayout(new GridLayout(2, 2));
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(createTitleTypeChart());
        frame.add(createGenreChart());
        frame.add(createYearlyTrendsChart());
        frame.add(createGenreByTypeChart());

        frame.setVisible(true);
    }

    private ChartPanel createTitleTypeChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<Title.TitleType, Long> typeCounts = titles.stream()
                .collect(Collectors.groupingBy(Title::type, Collectors.counting()));

        typeCounts.forEach((type, count) ->
                dataset.addValue(count, "Count", type.toString()));

        JFreeChart chart = ChartFactory.createBarChart(
                "Distribution of Title Types",
                "Type",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        return new ChartPanel(chart);
    }

    private ChartPanel createGenreChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<Title.Genre, Long> genreCounts = titles.stream()
                .flatMap(title -> title.genres().stream())
                .collect(Collectors.groupingBy(genre -> genre, Collectors.counting()));

        genreCounts.forEach((genre, count) ->
                dataset.setValue(genre.toString(), count));

        JFreeChart chart = ChartFactory.createPieChart(
                "Distribution of Genres",
                dataset,
                true, true, false);

        return new ChartPanel(chart);
    }

    private ChartPanel createYearlyTrendsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<Integer, Long> yearCounts = titles.stream()
                .filter(title -> title.startYear().isPresent())
                .collect(Collectors.groupingBy(
                        title -> title.startYear().get().getValue(),
                        Collectors.counting()));

        yearCounts.forEach((year, count) ->
                dataset.addValue(count, "Titles", year.toString()));

        JFreeChart chart = ChartFactory.createLineChart(
                "Titles per Year",
                "Year",
                "Number of Titles",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        return new ChartPanel(chart);
    }

    private ChartPanel createGenreByTypeChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Title.TitleType type : Title.TitleType.values()) {
            Map<Title.Genre, Long> genreCounts = titles.stream()
                    .filter(title -> title.type() == type)
                    .flatMap(title -> title.genres().stream())
                    .collect(Collectors.groupingBy(genre -> genre, Collectors.counting()));

            genreCounts.forEach((genre, count) ->
                    dataset.addValue(count, type.toString(), genre.toString()));
        }

        JFreeChart chart = ChartFactory.createStackedBarChart(
                "Genres by Title Type",
                "Genre",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        return new ChartPanel(chart);
    }
}