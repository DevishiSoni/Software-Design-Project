package TourCatSystem;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

//The location reader class will be the class responsible for reading from
//the csv file.
public class LocationReader {

    private DefaultTableModel tableModel;

    public LocationReader (File file) {

        try (CSVReader reader = new CSVReader(new FileReader(file))) {

            String[] header = reader.readNext();

            tableModel = new DefaultTableModel();

            if (header != null) {
                tableModel.setColumnIdentifiers(header);
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                tableModel.addRow(line);
            }


        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public DefaultTableModel getTableModel () {
        return this.tableModel;
    }

    static void hideColumn (TableColumnModel tableColumnModel, int i) {
        tableColumnModel.getColumn(i).setMinWidth(0);
        tableColumnModel.getColumn(i).setMaxWidth(0);
        tableColumnModel.getColumn(i).setPreferredWidth(0);
    }

    public static void hideColumns (TableColumnModel tcm, int[] vals) {
        for (int i : vals) {
            hideColumn(tcm, i);
        }
    }

    public static void main (String[] args) throws FileNotFoundException {

    }
}
