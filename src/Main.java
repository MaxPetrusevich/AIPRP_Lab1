import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Main extends Frame implements ActionListener {
    Button bex = new Button("Exit");
    Button sea = new Button("Search");
    TextField searchField = new TextField();
    TextArea resultArea = new TextArea();

    public Main() {
        super("Medicine Finder");
        setLayout(null);
        setBackground(new Color(102, 22, 100));
        setSize(450, 320);
        add(bex);
        add(sea);
        add(searchField);
        add(resultArea);

        bex.setBounds(110, 280, 100, 20);
        bex.addActionListener(this);

        sea.setBounds(110, 250, 100, 20);
        sea.addActionListener(this);

        searchField.setBounds(20, 50, 300, 25);
        resultArea.setBounds(20, 85, 400, 150);

        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == bex) {
            System.exit(0);
        } else if (ae.getSource() == sea) {
            String inputSymptoms = searchField.getText().trim();
            String[] symptoms = inputSymptoms.split(",");
            if (symptoms.length < 1 || symptoms.length > 3) {
                resultArea.setText("Введите от 1 до 3 симптомов, разделенных запятой");
                return;
            }
            String results = findMedicines(symptoms);
            resultArea.setText(results);
        }
    }

    private String findMedicines(String[] symptoms) {
        StringBuilder resultBuilder = new StringBuilder();
        Set<String> symptomSet = new HashSet<>();
        for (String symptom : symptoms) {
            symptomSet.add(symptom.trim().toLowerCase());
        }

        File dir = new File("resources");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files != null) {
            String bestFile = null;
            int maxMatches = 0;
            Map<String, List<String>> medicationMap = new HashMap<>();

            for (File file : files) {
                MatchResult matchResult = readMedicinesFromFile(file, symptomSet);
                resultBuilder.append(file + ": " + matchResult.count + " совпадений найдено\n");

                if (matchResult.count > maxMatches) {
                    maxMatches = matchResult.count;
                    bestFile = file.getAbsolutePath();
                    medicationMap = matchResult.medicationMap;
                }
            }

            // Create HTML file for the best results
            if (bestFile != null) {
                createHtmlFile(bestFile, medicationMap);
                resultBuilder.append("\nФайл с наибольшим кол-вом лекарств: ").append(bestFile).append("\n\n");
                for (Map.Entry<String, List<String>> entry : medicationMap.entrySet()) {
                    resultBuilder.append("Симптом: ").append(entry.getKey()).append(" - Лекарства: ")
                            .append(String.join(", ", entry.getValue())).append("\n");
                }
            }
        } else {
            resultBuilder.append("Файлы не найдены.");
        }
        return resultBuilder.toString();
    }

    private MatchResult readMedicinesFromFile(File file, Set<String> symptomSet) {
        int count = 0;
        Map<String, List<String>> medicationMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String symptom = parts[0].trim().toLowerCase();
                    String[] medicineList = parts[1].split(",");
                    if (symptomSet.contains(symptom)) {
                        count++;
                        medicationMap.putIfAbsent(symptom, new ArrayList<>());
                        for (String medicine : medicineList) {
                            medicationMap.get(symptom).add(medicine.trim());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка прочтения файла: " + e.getMessage());
        }
        return new MatchResult(count, medicationMap);
    }

    private void createHtmlFile(String filePath, Map<String, List<String>> medicationMap) {
        String htmlFilePath = "result.html";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFilePath))) {

            writer.write("<pre>");

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }

            writer.write("</pre></body></html>");
            writer.flush();

            // Open the newly created HTML file in the browser
            Desktop.getDesktop().browse(new File(htmlFilePath).toURI());
        } catch (IOException e) {
            System.out.println("Ошибка создания HTML файла: " + e.getMessage());
        }
    }


    static class MatchResult {
        int count;
        Map<String, List<String>> medicationMap;

        MatchResult(int count, Map<String, List<String>> medicationMap) {
            this.count = count;
            this.medicationMap = medicationMap;
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}
