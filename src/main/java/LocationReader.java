import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

//The location reader class will be the class responsible for reading from
//the csv file.
public class LocationReader {

    private DefaultTableModel tableModel;

    LocationReader(File file)
    {

        try(CSVReader reader = new CSVReader(new FileReader(file))){

            String[] header = reader.readNext();

            tableModel = new DefaultTableModel();

            if (header != null)
            {
                tableModel.setColumnIdentifiers(header);
            }

            String[] line;
            while((line = reader.readNext()) != null)
            {
                tableModel.addRow(line);
            }



        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public DefaultTableModel getTableModel() {
        return this.tableModel;
    }

    static void hideColumn(TableColumnModel tableColumnModel, int i){
        tableColumnModel.getColumn(i).setMinWidth(0);
        tableColumnModel.getColumn(i).setMaxWidth(0);
        tableColumnModel.getColumn(i).setPreferredWidth(0);
    }

    static void hideColumns(TableColumnModel tcm, int[] vals)
    {
        for(int i : vals)
        {
            hideColumn(tcm, i);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {

    }
}
