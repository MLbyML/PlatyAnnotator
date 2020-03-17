package bdv.ui.panel.uicomponents.ProcessTimeFrame;


import Jama.Matrix;
import bdv.ui.panel.BigDataViewerUI;
import io.scif.img.ImgOpener;
import net.imagej.ops.OpService;
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
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.event.EventSubscriber;
import org.scijava.thread.ThreadService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.concurrent.Callable;

public class TransformImagePanel<I extends IntegerType<I>, T extends NumericType<T>, L> extends JPanel {
    /* List of Images in bdvui Viewer Panel*/
    private JLabel imageListLabel;

    /* List of Images in bdvui Viewer Panel*/
    private JComboBox imageList;

    private JLabel loadAffineTransformLabel;

    private JButton browseAffineTransformButton;

    private JTextField affineTransformTextField;

    private static JButton runButton;

    private JTextField A00;


    private JTextField A01;
    private JTextField A02;
    private JTextField A03;
    private JTextField A10;
    private JTextField A11;

    private JTextField A12;

    private JTextField A13;

    private JTextField A20;

    private JTextField A21;

    private JTextField A22;

    private JTextField A23;

    private JCheckBox reflect;

    private JButton generate;
    private JLabel saveAffineTransformLabel;

    private JButton saveAffineTransformButton;

    private final JFileChooser fc = new JFileChooser("/home/manan/Desktop/08_SampleData/02_Images/01_Platyneries/16hpf");


    private EventService es;

    private CommandService cs;

    private ThreadService ts;

    private OpService ops;

    private List<EventSubscriber<?>> subs;

    private BlockRealMatrix sourceMatrix;

    private BlockRealMatrix targetMatrix;

    private Matrix affineTransform;


    public TransformImagePanel(final CommandService cs, final EventService es, final ThreadService ts, final OpService ops, final BigDataViewerUI bdvUI) {
        this.es = es;
        this.cs = cs;
        this.ts = ts;
        this.ops = ops;
        subs = this.es.subscribe(this);
        imageList = new JComboBox();
        imageListLabel = new JLabel("Image");
        loadAffineTransformLabel = new JLabel("Load Affine Transform");
        browseAffineTransformButton = new JButton("Browse");
        affineTransformTextField = new JTextField("", 20);
        saveAffineTransformLabel = new JLabel("Save Affine Transform");
        saveAffineTransformButton = new JButton("Browse");
        generate= new JButton("Generate");
        reflect = new JCheckBox("Reflect ", true);
        reflect.setFont(new Font("Serif", Font.BOLD, 14));
        setupBrowseAffineTransformButton();
        setupSaveAffineTransformButton();
        runButton = new JButton("Run");
        A00=new JTextField(" ", 10);
        A01=new JTextField(" ", 10);
        A02=new JTextField(" ", 10);
        A03=new JTextField(" ", 10);
        A10=new JTextField(" ", 10);
        A11=new JTextField(" ", 10);
        A12=new JTextField(" ", 10);
        A13=new JTextField(" ", 10);
        A20=new JTextField(" ", 10);
        A21=new JTextField(" ", 10);
        A22=new JTextField(" ", 10);
        A23=new JTextField(" ", 10);
        setupGenerateButton(bdvUI);
        setupRunButton(bdvUI);

        setupPanel();
        this.add(imageListLabel);
        this.add(imageList, "wrap");
        this.add(loadAffineTransformLabel);
        this.add(browseAffineTransformButton, "wrap");
        this.add(affineTransformTextField, "wrap");
        this.add(reflect, "wrap");
        this.add(generate, "wrap");
        this.add(new JSeparator(), "growx, spanx, wrap");
        this.add(new JLabel("<html> A<sub>00</sub> </html>"));
        this.add(A00, "wrap");
        this.add(new JLabel("<html> A<sub>01</sub> </html>"));
        this.add(A01, "wrap");
        this.add(new JLabel("<html> A<sub>02</sub> </html>"));
        this.add(A02, "wrap");
        this.add(new JLabel("<html> A<sub>03</sub> </html>"));
        this.add(A03, "wrap");
        this.add(new JLabel("<html> A<sub>10</sub> </html>"));
        this.add(A10, "wrap");
        this.add(new JLabel("<html> A<sub>11</sub> </html>"));
        this.add(A11, "wrap");
        this.add(new JLabel("<html> A<sub>12</sub> </html>"));
        this.add(A12, "wrap");
        this.add(new JLabel("<html> A<sub>13</sub> </html>"));
        this.add(A13, "wrap");
        this.add(new JLabel("<html> A<sub>20</sub> </html>"));
        this.add(A20, "wrap");
        this.add(new JLabel("<html> A<sub>21</sub> </html>"));
        this.add(A21, "wrap");
        this.add(new JLabel("<html> A<sub>22</sub> </html>"));
        this.add(A22, "wrap");
        this.add(new JLabel("<html> A<sub>23</sub> </html>"));
        this.add(A23, "wrap");
        this.add(runButton, "wrap");
        this.add(saveAffineTransformLabel);
        this.add(saveAffineTransformButton, "wrap");


    }


    private void setupGenerateButton(BigDataViewerUI bdvUI) {


        generate.setBackground(Color.WHITE);
        generate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == generate) {
                    ts.getExecutorService().submit(new Callable<T>() {

                        @Override
                        public T call() throws Exception {
                        if(reflect.isSelected()){
                            double[][] array = {{-1.,0.,0, 0},{0, 1, 0, 0},{0, 0, 1, 0.}, {0, 0, 0, 1}};
                            Matrix reflect = new Matrix(array);
                            affineTransform=reflect.times(affineTransform);
                        }

                            A00.setText(String.valueOf(affineTransform.get(0, 0)));
                            A01.setText(String.valueOf(affineTransform.get(0, 1)));
                            A02.setText(String.valueOf(affineTransform.get(0, 2)));
                            A03.setText(String.valueOf(affineTransform.get(0, 3)));

                            A10.setText(String.valueOf(affineTransform.get(1, 0)));
                            A11.setText(String.valueOf(affineTransform.get(1, 1)));
                            A12.setText(String.valueOf(affineTransform.get(1, 2)));
                            A13.setText(String.valueOf(affineTransform.get(1, 3)));

                            A20.setText(String.valueOf(affineTransform.get(2, 0)));
                            A21.setText(String.valueOf(affineTransform.get(2, 1)));
                            A22.setText(String.valueOf(affineTransform.get(2, 2)));
                            A23.setText(String.valueOf(affineTransform.get(2, 3)));

                        return null;

                        }
                    });
                }
            }

        });

    }

    private void setupSaveAffineTransformButton() {
        saveAffineTransformButton.setBackground(Color.WHITE);
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose Directory");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        saveAffineTransformButton.addActionListener(e -> {
            if (e.getSource() == saveAffineTransformButton) {

                int returnVal = fc.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    writeAffineTransformToCSV(file.getAbsolutePath());

                }

            }
        });

    }




    private void setupBrowseAffineTransformButton() {

        browseAffineTransformButton.setBackground(Color.WHITE);
        browseAffineTransformButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == browseAffineTransformButton) {
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        affineTransformTextField.setText(file.getAbsolutePath());
                        readAffineTransformCSV(file.getAbsolutePath());


                    }

                }
            }
        });
    }



    private void setupPanel() {

        this.setBackground(Color.white);
        this.setBorder(new TitledBorder(""));
        this.setLayout(new MigLayout("fillx", "", ""));
    }




    private void setupRunButton(BigDataViewerUI bdvUI) {
        runButton.setBackground(Color.WHITE);
        runButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == runButton) {
                    ts.getExecutorService().submit(new Callable<T>() {

                        @Override
                        public T call() throws Exception {


                            System.out.println("start calculation");
                            Img<UnsignedShortType> source = (Img<UnsignedShortType>)
                                    new ImgOpener().openImgs((String) imageList.getSelectedItem()).get(0);
                            Img<UnsignedShortType> target = ArrayImgs.unsignedShorts(source.dimension(0),source.dimension(1), source.dimension(2));
                            AffineTransform3D affineTransform3D = new AffineTransform3D();
                            affineTransform3D.set(Double.parseDouble(A00.getText()), 0, 0);
                            affineTransform3D.set(Double.parseDouble(A01.getText()), 0, 1);
                            affineTransform3D.set(Double.parseDouble(A02.getText()), 0, 2);
                            affineTransform3D.set(Double.parseDouble(A03.getText()), 0, 3);

                            affineTransform3D.set(Double.parseDouble(A10.getText()), 1, 0);
                            affineTransform3D.set(Double.parseDouble(A11.getText()), 1, 1);
                            affineTransform3D.set(Double.parseDouble(A12.getText()), 1, 2);
                            affineTransform3D.set(Double.parseDouble(A13.getText()), 1, 3);

                            affineTransform3D.set(Double.parseDouble(A20.getText()), 2, 0);
                            affineTransform3D.set(Double.parseDouble(A21.getText()), 2, 1);
                            affineTransform3D.set(Double.parseDouble(A22.getText()), 2, 2);
                            affineTransform3D.set(Double.parseDouble(A23.getText()), 2, 3);
                            RealRandomAccessible<UnsignedShortType>
                                    interpolated = Views.interpolate(Views.extendZero(source), new NLinearInterpolatorFactory<>());
                            RealRandomAccessible<UnsignedShortType>
                                    transformed = RealViews.affine(interpolated, affineTransform3D);
                            RandomAccessibleInterval<UnsignedShortType>
                                    rai = Views.interval(Views.raster(transformed), target);
                            //ImageJFunctions.show(rai).setDisplayRange(0, 1200);


                            //IJ.save(ImageJFunctions.wrap(rai, "transformed"), "/home/manan/Desktop/imgTransformed.tif");
                            bdvUI.addImage(rai, "transformedimage", Color.white);
                            System.out.println("done calculation");
                            return null;


                        }
                    });
                }
            }

        });

    }


    public void addImage(String name) {
        imageList.addItem(name);


    }

    private void readAffineTransformCSV(String filename) {
        try {

            affineTransform = new Matrix(4, 4);
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String l = reader.readLine();
            int row = 0;
            while(l != null ){
                if(l.contains("#")){
                    l = reader.readLine();
                }else {
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

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeAffineTransformToCSV(String path) {
        final String COMMA_DELIMITER = " ";
        final String NEW_LINE_SEPARATOR = "\n";
        FileWriter fileWriter = null;

        final String fileName = path + "/AffineTransform_" + java.time.LocalDateTime.now() + ".csv";

        try {
            fileWriter = new FileWriter(fileName);
            for (int i = 0; i < affineTransform.getRowDimension(); i++) {
                for (int j = 0; j < affineTransform.getColumnDimension(); j++) {
                    fileWriter.append(String.valueOf(affineTransform.get(i, j)));
                    fileWriter.append(COMMA_DELIMITER);

                }
                fileWriter.append(NEW_LINE_SEPARATOR);

            }


        } catch (Exception e) {
            System.out.println("Error in CSVFileWriter !!!");
            e.printStackTrace();
        } finally {

            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }


    public String getImageOne() {
        return (String) this.imageList.getSelectedItem();

    }


}
