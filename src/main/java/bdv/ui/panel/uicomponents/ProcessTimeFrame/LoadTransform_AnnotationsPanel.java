package bdv.ui.panel.uicomponents.ProcessTimeFrame;

import Jama.Matrix;
import bdv.ui.panel.BigDataViewerUI;
import bdv.util.BdvOverlaySource;
import io.scif.img.ImgOpener;
import net.imagej.ops.OpService;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.event.EventSubscriber;
import org.scijava.thread.ThreadService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoadTransform_AnnotationsPanel<I extends IntegerType<I>, T extends NumericType<T>, L> extends JPanel {
    private EventService es;
    private CommandService cs;
    private ThreadService ts;
    private OpService ops;
    private List<EventSubscriber<?>> subs;
    private JLabel imageListLabel;
    /* List of Images in bdvui Viewer Panel*/
    private JComboBox imageList;
    private JLabel openTransformLabel;
    private JButton openTransformButton;
    private JTextField openTransformTextField;
    private JLabel openAnnotationsLabel;
    private JButton openAnnotationsButton;
    private JTextField openAnnotationsTextField;
    private JButton showButton;
    private final JFileChooser fc = new JFileChooser("/home/manan/Desktop/");
    private List<Point> groundTruthLocalMinima;


    public LoadTransform_AnnotationsPanel(final CommandService cs, final EventService es, final ThreadService ts, final OpService ops, final BigDataViewerUI bdvUI) {
        this.es = es;
        this.cs = cs;
        this.ts = ts;
        this.ops = ops;
        subs = this.es.subscribe(this);
        imageListLabel = new JLabel("Image");
        imageList = new JComboBox();
        openTransformLabel = new JLabel("Choose Transform File");
        openTransformButton = new JButton("Browse");
        openTransformTextField = new JTextField(" ", 20);
        openAnnotationsLabel = new JLabel("Choose Annotations File");
        openAnnotationsButton = new JButton("Browse");
        openAnnotationsTextField = new JTextField(" ", 20);
        showButton = new JButton("Show");
        setupOpenTransformButton();
        setupOpenAnnotationsButton();
        setupShowButton(bdvUI);
        setupPanel();
        this.add(imageListLabel);
        this.add(imageList, "wrap");
        this.add(openTransformLabel);
        this.add(openTransformButton, "wrap");
        this.add(openTransformTextField, "wrap");
        this.add(openAnnotationsLabel);
        this.add(openAnnotationsButton, "wrap");
        this.add(openAnnotationsTextField, "wrap");
        this.add(showButton);

    }

    private void setupOpenAnnotationsButton() {
        openAnnotationsButton.setBackground(Color.WHITE);
        openAnnotationsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == openAnnotationsButton) {
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        openAnnotationsTextField.setText(file.getAbsolutePath());
                    }

                }
            }
        });
    }


    private void setupShowButton(BigDataViewerUI bdvui) {
        showButton.setBackground(Color.WHITE);
        showButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == showButton) {
                    createTransformedImage(bdvui);
                    createOverlay(bdvui);

                }
            }


        });

    }

    private Matrix readAffineTransformCSV(String filename) {
        try {

            Matrix affineTransform = new Matrix(4, 4);
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String l = reader.readLine();
            int row = 0;
            while (l != null) {
                if (l.contains("#")) {
                    l = reader.readLine();
                } else {
                    String[] tokens = l.split(" ");

                    affineTransform.set(row, 0, Float.parseFloat(tokens[0]));
                    affineTransform.set(row, 1, Float.parseFloat(tokens[1]));
                    affineTransform.set(row, 2, Float.parseFloat(tokens[2]));
                    affineTransform.set(row, 3, Float.parseFloat(tokens[3]));
                    System.out.println(l);
                    l = reader.readLine();
                    row++;
                }


            }
            reader.close();
            return affineTransform;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void createTransformedImage(BigDataViewerUI bdvUI) {


        Matrix affineTransform = readAffineTransformCSV(openTransformTextField.getText());
        Img<UnsignedShortType> source = (Img<UnsignedShortType>)
                new ImgOpener().openImgs(bdvUI.getOpenFilePanel().getDirectory() + "/" + (String) imageList.getSelectedItem()).get(0);
        // 0 is some remnant oof ImgOpener
        Img<UnsignedShortType> target ;
        if(bdvUI.getOpenFilePanel().getOption2()){
            target=ArrayImgs.unsignedShorts(source.dimension(0), source.dimension(1), 5*source.dimension(2));
        }else{
            target=ArrayImgs.unsignedShorts(source.dimension(0), source.dimension(1), source.dimension(2));
        }

        AffineTransform3D affineTransform3D = new AffineTransform3D();
        affineTransform3D.set(affineTransform.get(0, 0), 0, 0);
        affineTransform3D.set(affineTransform.get(0, 1), 0, 1);
        affineTransform3D.set(affineTransform.get(0, 2), 0, 2);
        affineTransform3D.set(affineTransform.get(0, 3), 0, 3);

        affineTransform3D.set(affineTransform.get(1, 0), 1, 0);
        affineTransform3D.set(affineTransform.get(1, 1), 1, 1);
        affineTransform3D.set(affineTransform.get(1, 2), 1, 2);
        affineTransform3D.set(affineTransform.get(1, 3), 1, 3);

        affineTransform3D.set(affineTransform.get(2, 0), 2, 0);
        affineTransform3D.set(affineTransform.get(2, 1), 2, 1);
        affineTransform3D.set(affineTransform.get(2, 2), 2, 2);
        affineTransform3D.set(affineTransform.get(2, 3), 2 ,3);
        RealRandomAccessible<UnsignedShortType>
                interpolated = Views.interpolate(Views.extendZero(source), new NLinearInterpolatorFactory<>());
        RealRandomAccessible<UnsignedShortType>
                transformed = RealViews.affine(interpolated, affineTransform3D);
        RandomAccessibleInterval<UnsignedShortType>
                rai = Views.interval(Views.raster(transformed), target);
        bdvUI.addImage(rai, "transformed_" + imageList.getSelectedItem(), Color.white);


    }

    private void createOverlay(BigDataViewerUI bdvUI) {
        final String COMMA_DELIMITER = " ";
        final String fileName = openAnnotationsTextField.getText();
        double minScale = 5;
        double stepScale = 1;
        double maxScale = 9;
        double samplingFactor = 1;
        int axis = 2;
        List<RichFeaturePoint> thresholdedLocalMinima = new ArrayList<>();
        groundTruthLocalMinima = new ArrayList<>();
        Random rand = new Random();
        RichFeaturePoint temp;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                String[] values = line.split(COMMA_DELIMITER);
                groundTruthLocalMinima.add(new Point((int) Double.parseDouble(values[1]), (int) Double.parseDouble(values[2]), (int) Double.parseDouble(values[3])));
                temp = new RichFeaturePoint((int) Double.parseDouble(values[1]), (int) Double.parseDouble(values[2]), (int) Double.parseDouble(values[3]), 0, -100, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
                temp.setLabel((int) Double.parseDouble(values[0]));
                thresholdedLocalMinima.add(temp);

            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("File read problem/IO!");
            e.printStackTrace();
        }

        MyOverlay myOverlay = new MyOverlay(axis, samplingFactor, minScale, stepScale, maxScale, thresholdedLocalMinima,  3);
        myOverlay.setThresholdedLocalMinima();

        BdvOverlaySource overlaySource = bdvUI.addOverlay(myOverlay, fileName, Color.white);
        bdvUI.getBDVHandlePanel().getViewerPanel().requestRepaint();
    }

    private void setupOpenTransformButton() {
        openTransformButton.setBackground(Color.WHITE);
        openTransformButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == openTransformButton) {
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        openTransformTextField.setText(file.getAbsolutePath());
                    }

                }
            }
        });
    }

    private void setupPanel() {
        this.setBackground(Color.white);
        this.setLayout(new MigLayout("fillx", "", ""));
    }

    public void addImage(String name) {
        imageList.addItem(name);
    }
}
