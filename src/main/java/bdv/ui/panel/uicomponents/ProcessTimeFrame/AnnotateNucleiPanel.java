package bdv.ui.panel.uicomponents.ProcessTimeFrame;

import bdv.ui.panel.BigDataViewerUI;
import bdv.util.BdvOverlaySource;
import bdv.viewer.ViewerPanel;
import net.imagej.ops.OpService;
import net.imglib2.RealPoint;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.miginfocom.swing.MigLayout;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.event.EventSubscriber;
import org.scijava.thread.ThreadService;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnnotateNucleiPanel<I extends IntegerType<I>, T extends NumericType<T>, L> extends JPanel {
    private EventService es;
    private CommandService cs;
    private ThreadService ts;
    private OpService ops;
    private List<EventSubscriber<?>> subs;
    private MyOverlay myOverlay;
    private ViewerPanel viewer;

    private JButton startButton;
    private JButton addNucleiButton;
    private JButton deleteNucleiButton;
    private JButton saveButton;
    private JLabel saveLabel;
    private JComboBox nucleusBox;



    /* List of Images in bdvui Viewer Panel*/
    private JComboBox imageList;
    private JLabel imageListLabel;
    public AnnotateNucleiPanel(final CommandService cs, final EventService es, final ThreadService ts, final OpService ops, final BigDataViewerUI bdvUI) {
        this.es = es;
        this.cs = cs;
        this.ts = ts;
        this.ops = ops;
        subs = this.es.subscribe(this);

        imageListLabel = new JLabel("Image");
        imageList = new JComboBox();
        startButton = new JButton("Start");
        startButton.setFont(new Font("Serif", Font.BOLD, 14));
        addNucleiButton = new JButton("Add");
        addNucleiButton.setFont(new Font("Serif", Font.ITALIC, 14));
        deleteNucleiButton = new JButton("Delete");
        deleteNucleiButton.setFont(new Font("Serif", Font.ITALIC, 14));
        saveLabel=new JLabel("Save");
        saveButton = new JButton("Browse");
        saveButton.setFont(new Font("Serif", Font.BOLD, 14));
        nucleusBox =new JComboBox();
        nucleusBox.addItem("Nucleus 1");
        nucleusBox.addItem("Nucleus 2");
        nucleusBox.addItem("Nucleus 3");
        nucleusBox.addItem("Nucleus 4");
        nucleusBox.addItem("Nucleus 5");
        nucleusBox.addItem("Nucleus 6");
        nucleusBox.addItem("Nucleus 7");
        nucleusBox.addItem("Nucleus 8");
        nucleusBox.addItem("Nucleus 9");
        nucleusBox.addItem("Nucleus 10");
        nucleusBox.addItem("Nucleus 11");
        nucleusBox.addItem("Nucleus 12");
        setupStartButton(bdvUI);
        setupAddNucleiButton(bdvUI);
        setupDeleteNucleiButton(bdvUI);
        setupSaveButton(bdvUI);
        setupPanel();
        this.add(startButton, "wrap");
        this.add(nucleusBox, "wrap");
        this.add(addNucleiButton, "wrap");
        this.add(deleteNucleiButton, "wrap");
        this.add(new JSeparator(), "growx, spanx, wrap");
        this.add(saveLabel, "wrap");
        this.add(saveButton, "wrap");
    }


    private void setupSaveButton(BigDataViewerUI bdvUI) {
       saveButton.setBackground(Color.white);
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose Directory");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        saveButton.addActionListener(e -> {
            if (e.getSource() == saveButton) {

                int returnVal = fc.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    writeResultsToCSV(file.getAbsolutePath());

                }

            }
        });
    }

    private void writeResultsToCSV(String path) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose Directory");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        myOverlay.writeNuclei(path);
        myOverlay.writeToCSV(path);
        myOverlay.createTGMM(path);

    }


    private void setupPanel() {
        this.setBackground(Color.white);
        this.setLayout(new MigLayout("fillx", "", ""));
    }

    private void setupStartButton(BigDataViewerUI bdvUI) {
        startButton.setBackground(Color.white);
        startButton.addActionListener(e -> {
            if (e.getSource() == startButton) {
                float startThreshold=-100;
                float currentThreshold=-100;
                List<RichFeaturePoint> localMinima = new ArrayList<>();
                localMinima.add(new RichFeaturePoint(0, 0, 0, 0, -150, 200, 200, 200));
                myOverlay=new MyOverlay(0, 1, 5, 1, 9, localMinima, startThreshold, currentThreshold, 3);
                myOverlay.setThresholdedLocalMinima(startThreshold);
                BdvOverlaySource overlaySource = bdvUI.addOverlay(myOverlay, String.valueOf("transformed image"), Color.white);
                bdvUI.getBDVHandlePanel().getViewerPanel().requestRepaint();
                viewer = bdvUI.getBDVHandlePanel().getViewerPanel();
                viewer.getDisplay().addHandler(new MyListener());


            }
        });


    }


    private class MyListener extends MouseInputAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            toggleSelected(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            updateStart(e);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            updateSize(e);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            updateSize(e);
        }


    }

    private void toggleSelected(MouseEvent e) {
        final RealPoint pos = new RealPoint(3);
        viewer.getGlobalMouseCoordinates(pos);

        myOverlay.toggleSelected(pos);
        viewer.requestRepaint();

    }

    void updateStart(MouseEvent e) {
        final RealPoint pos = new RealPoint(3);
        viewer.getGlobalMouseCoordinates(pos);
        myOverlay.setXY(pos);

    }

    void updateSize(MouseEvent e) {
        final RealPoint pos = new RealPoint(3);
        viewer.getGlobalMouseCoordinates(pos);

        myOverlay.setWidthHeight(pos);
        viewer.requestRepaint();



    }

    // Correct the code below so that it is shorter and such that it is not hard coded which axis requires the sampling factor
    // note pos is already in the isotropic space, hence the sampling factor is not applied to it!


    private void setupAddNucleiButton(BigDataViewerUI bdvUI) {
        this.addNucleiButton.setBackground(Color.WHITE);
        addNucleiButton.addActionListener(e -> {
            if (e.getSource() == addNucleiButton) {
                myOverlay.add(myOverlay.getAddedPoint(nucleusBox.getSelectedIndex()));
               viewer.requestRepaint();
            }
        });


    }

    private void setupDeleteNucleiButton(BigDataViewerUI bdvUI) {
        this.deleteNucleiButton.setBackground(Color.WHITE);
        deleteNucleiButton.addActionListener(e -> {
            if (e.getSource() == deleteNucleiButton) {
                myOverlay.deleteSelected(nucleusBox.getSelectedIndex() + 1); // label is always 1+ combobox.selected_item
                viewer.requestRepaint();
            }
        });


    }
}



