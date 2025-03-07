import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TableGUI extends JTable {

    TableGUI()
    {

    }

    private static DefaultTableModel loadCSV(String filePath) {
        DefaultTableModel model = new DefaultTableModel();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (isFirstLine) {
                    model.setColumnIdentifiers(data);
                    isFirstLine = false;
                } else {
                    model.addRow(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return model;
    }
}
