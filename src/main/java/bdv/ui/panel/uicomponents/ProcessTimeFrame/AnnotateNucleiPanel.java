package bdv.ui.panel.uicomponents.ProcessTimeFrame;

import bdv.ui.panel.BigDataViewerUI;
import bdv.ui.panel.uicomponents.SelectionAndGroupingTabs;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
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
    private JLabel thumbLabel;


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
        ImageIcon startIcon = new ImageIcon(SelectionAndGroupingTabs.class.getResource("pencil.png"), "Pencil");
        ImageIcon startIconDS = new ImageIcon(getScaledImage(startIcon.getImage(), 50, 50), "smallPencil");
        ImageIcon tickIcon = new ImageIcon(SelectionAndGroupingTabs.class.getResource("Yes.svg"), "GreenTick");
        ImageIcon tickIconDS = new ImageIcon(getScaledImage(tickIcon.getImage(), 50, 50), "smallGreenTick");
        startButton = new JButton(startIconDS);
        startButton.setFont(new Font("Serif", Font.BOLD, 14));
        addNucleiButton = new JButton("Add");
        deleteNucleiButton = new JButton("Delete");
        saveLabel = new JLabel("Save");
        saveButton = new JButton("Browse");
        thumbLabel = new JLabel();
        thumbLabel.setIcon(tickIconDS);
        thumbLabel.setVisible(false);
        nucleusBox = new JComboBox();

        nucleusBox.addItem("Nucleus 1: Macromere 4A");
        nucleusBox.addItem("Nucleus 2: Macromere 4B");
        nucleusBox.addItem("Nucleus 3: Macromere 5C");
        nucleusBox.addItem("Nucleus 4: Macromere 5D");
        nucleusBox.addItem("Nucleus 5: Left Protonephridia");
        nucleusBox.addItem("Nucleus 6: Right Protonephridia");
        nucleusBox.addItem("Nucleus 7: Ciliated Cell No. 1");
        nucleusBox.addItem("Nucleus 8: Ciliated Cell No. 2");
        nucleusBox.addItem("Nucleus 9: Ciliated Cell No. 6");
        nucleusBox.addItem("Nucleus 10: Ciliated Cell No. 7");
        nucleusBox.addItem("Nucleus 11: Ciliated Cell No. 11");
        nucleusBox.addItem("Nucleus 12: Ciliated Cell No. 12");
        setupNucleusBox();
        setupStartButton(bdvUI);
        setupAddNucleiButton(bdvUI);
        setupDeleteNucleiButton(bdvUI);
        setupSaveButton(bdvUI);
        setupPanel();
        this.add(startButton, "wrap");
        this.add(nucleusBox);
        this.add(thumbLabel, "wrap");
        this.add(addNucleiButton, "wrap");
        this.add(deleteNucleiButton, "wrap");
        this.add(new JSeparator(), "growx, spanx, wrap");
        this.add(saveLabel, "wrap");
        this.add(saveButton, "wrap");
    }


    private void setupNucleusBox() {
        nucleusBox.setBackground(Color.WHITE);
        nucleusBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkComboBox();
            }

        });
    }

    private Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }


    private void setupSaveButton(BigDataViewerUI bdvUI) {
        saveButton.setBackground(Color.white);
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose FileName");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        saveButton.addActionListener(e -> {
            if (e.getSource() == saveButton) {
                String s = bdvUI.getOpenFilePanel().getDirectory() + "/" + (String) bdvUI.getTransformImagePanel().getImageSelected();
                fc.setSelectedFile(new File(s.substring(0, s.length() - 4) + "_nuclei.csv"));
                int returnVal = fc.showSaveDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    File file = fc.getSelectedFile();
                    writeResultsToCSV(bdvUI, file.getAbsolutePath());

                }

            }
        });
    }

    private void writeResultsToCSV(BigDataViewerUI bdvUI, String path) {
        myOverlay.writeNuclei(path);


    }


    private void setupPanel() {
        this.setBackground(Color.white);
        this.setLayout(new MigLayout("fillx", "", ""));
    }

    private void setupStartButton(BigDataViewerUI bdvUI) {
        startButton.setBackground(Color.white);
        startButton.addActionListener(e -> {
            if (e.getSource() == startButton) {
                myOverlay = new MyOverlay(2, 1, 5, 1, 9, bdvUI.getThresholdedLocalMinima(), 3);
                myOverlay.setThresholdedLocalMinima();
                BdvOverlaySource overlaySource = bdvUI.addOverlay(myOverlay, String.valueOf(bdvUI.getTransformImagePanel().getImageSelected()), Color.white);
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


    private void checkComboBox() {
        int index = nucleusBox.getSelectedIndex();
        boolean isAnnotated = false;
        try {
                if (!myOverlay.getThresholdedLocalMinima().get(index).isEmpty()) {
                    isAnnotated = true;
                }


            thumbLabel.setVisible(isAnnotated);
        } catch (Exception ex) {
            System.out.println("Overlay is not initialized. Kindly first click on the Pencil icon!");
        }
    }


    private void setupAddNucleiButton(BigDataViewerUI bdvUI) {
        this.addNucleiButton.setBackground(Color.WHITE);
        addNucleiButton.addActionListener(e -> {
            if (e.getSource() == addNucleiButton) {
                if (thumbLabel.isVisible()) {
                    JOptionPane.showMessageDialog(null, "Nucleus already added!", "Alert", JOptionPane.ERROR_MESSAGE);
                } else {
                    myOverlay.add(nucleusBox.getSelectedIndex(), myOverlay.getAddedPoint(nucleusBox.getSelectedIndex()));
                    viewer.requestRepaint();
                    checkComboBox();
                }

            }
        });


    }

    private void setupDeleteNucleiButton(BigDataViewerUI bdvUI) {
        this.deleteNucleiButton.setBackground(Color.WHITE);
        deleteNucleiButton.addActionListener(e -> {

            if (e.getSource() == deleteNucleiButton) {
                if (!thumbLabel.isVisible()) {
                    JOptionPane.showMessageDialog(null, "Nucleus is not there!", "Alert", JOptionPane.ERROR_MESSAGE);
                }else{
                    myOverlay.deleteSelected(nucleusBox.getSelectedIndex() + 1); // label is always 1 + combobox.selected_item
                    viewer.requestRepaint();
                    checkComboBox();
                }

            }
        });


    }
}



