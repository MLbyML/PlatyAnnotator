package bdv.ui.panel.uicomponents.ProcessTimeFrame;

import bdv.ui.panel.BigDataViewerUI;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ops.OpService;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.event.EventSubscriber;
import org.scijava.thread.ThreadService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

public class OpenFilePanel<I extends IntegerType<I>, T extends NumericType<T>, L> extends JPanel {

    /*Browse button*/
    private JButton browse;

    /*Open File button*/
    private JButton open;

    /*Text Area for filename*/
    private JTextField textField;
    private JRadioButton option1;
    private JRadioButton option2;

    /*File chooser*/
    private final JFileChooser fc = new JFileChooser("/home/manan/Data/Platynereis/Landmark_Annotations/04/15.03.2020/");

    private EventService es;

    private CommandService cs;

    private ThreadService ts;

    private OpService ops;

    private List<EventSubscriber<?>> subs;


    public OpenFilePanel(final CommandService cs, final EventService es, final ThreadService ts, final OpService ops, final BigDataViewerUI bdvUI) {
        this.es = es;
        this.cs = cs;
        this.ts = ts;
        this.ops = ops;
        subs = this.es.subscribe(this);
        option1 = new JRadioButton("In-situ", true);
        option2 = new JRadioButton("Live", false);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(option1);
        buttonGroup.add(option2);

        textField = new JTextField("", 20);
        browse = new JButton("Browse");
        setupBrowseButton();
        open = new JButton("Open");
        setupOpenButton(bdvUI);
        option1.setBackground(Color.WHITE);
        option2.setBackground(Color.WHITE);
        setupPanel();
        this.add(option1);
        this.add(option2, "wrap");
        //this.add(buttonGroup);
        this.add(textField);
        this.add(browse, "wrap");
        this.add(open, "wrap");

    }

    private void setupBrowseButton() {
        browse.setBackground(Color.WHITE);
        browse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == browse) {
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getCurrentDirectory();
                        textField.setText(file.getAbsolutePath());
                    }

                }
            }
        });
    }


    private void setupOpenButton(final BigDataViewerUI bdvui) {
        open.setBackground(Color.WHITE);
        open.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == open) {
                    //openImage(bdvui);
                    File[] files = openDirectory(bdvui);
                    for (int i = 0; i < files.length; i++) {
                        openImage(bdvui, files[i].getAbsolutePath());
                    }


                }
            }


        });

    }

    private File[] openDirectory(final BigDataViewerUI bdvUI) {
        open.setEnabled(false);
        Context context = new Context();
        File dir = new File(textField.getText());

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".tif");
            }
        });


    }

    private void openImage(final BigDataViewerUI bdvUI, String filename) {
        open.setEnabled(false);
        Context context = new Context();
        DatasetIOService ioService = context.service(DatasetIOService.class);
        try {
            //Dataset img = ioService.open(textField.getText());
            Dataset img = ioService.open(filename);
            String[] filesplit = filename.split("/");
            AffineTransform3D affineTransform3D = new AffineTransform3D();
            if(option1.isSelected()){
                affineTransform3D.set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
            }else{
                affineTransform3D.set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 4.999, 0, 0, 0, 0, 1);
            }


            if (filename.endsWith("red.tif")) {
                bdvUI.addImage(img, filesplit[filesplit.length - 1], Color.red, affineTransform3D);
            } else if (filename.endsWith("green.tif")) {
                bdvUI.addImage(img, filesplit[filesplit.length - 1], Color.green, affineTransform3D);
            } else {
                bdvUI.addImage(img, filesplit[filesplit.length - 1], Color.white, affineTransform3D);
            }

            bdvUI.getTransformImagePanel().addImage(filesplit[filesplit.length - 1]);
            bdvUI.getLoadTransform_AnnotationsPanel().addImage(filesplit[filesplit.length - 1]);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            open.setEnabled(true);
        }
    }


    private void setupPanel() {
        this.setBackground(Color.white);
        this.setBorder(new TitledBorder(""));
        this.setLayout(new MigLayout("fillx", "", ""));
    }


    public String getDirectory() {
        return textField.getText();
    }

    public boolean getOption2() {
        return this.option2.isSelected();
    }
}

